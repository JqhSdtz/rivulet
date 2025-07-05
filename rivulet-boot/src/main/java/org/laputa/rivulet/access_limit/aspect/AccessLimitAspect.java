package org.laputa.rivulet.access_limit.aspect;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.laputa.rivulet.access_limit.annotation.AccessLimit;
import org.laputa.rivulet.access_limit.annotation.AccessLimitLevel;
import org.laputa.rivulet.access_limit.annotation.AccessLimitTarget;
import org.laputa.rivulet.access_limit.annotation.AccessLimits;
import org.laputa.rivulet.common.constant.RedisPrefix;
import org.laputa.rivulet.common.entity.RvBaseEntity;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.laputa.rivulet.module.auth.session.AppAuth;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author JQH
 * @since 下午 8:38 21/03/01
 */

@Slf4j
@Component
@Aspect
@Order(1)
public class AccessLimitAspect implements ApplicationRunner {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private AppAuth appAuth;
    @Resource
    private HttpServletRequest request;
    private static final String GLOBAL_TARGET_REGISTER_SCRIPT = ResourceUtil.readUtf8Str("lua/access_limit/globalTargetRegister.lua");
    private final String methodRegisterKey = "METHOD";
    private final Map<String, Long> methodRegisterMap = new HashMap<>();
    private final Map<String, Map<String, String>> methodAccessMap = new HashMap<>();
    private boolean initialized = false;

    /**
     * 向redis中注册需要限制访问的方法
     *
     * @param args incoming application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        Reflections reflections = new Reflections(Scanners.MethodsAnnotated);
        Set<Method> methodSet = reflections.getMethodsAnnotatedWith(AccessLimit.class);
        methodSet.addAll(reflections.getMethodsAnnotatedWith(AccessLimits.class));
        List<String> redisLevelMethodNameList = new ArrayList<>();
        List<String> localLevelMethodNameList = new ArrayList<>();
        for (Method method : methodSet) {
            boolean isRedisLevel = false;
            if (method.isAnnotationPresent(AccessLimitLevel.class)) {
                AccessLimitLevel level = method.getAnnotation(AccessLimitLevel.class);
                isRedisLevel = AccessLimitLevel.Level.Redis.equals(level.value());
            }
            String methodName = method.getClass().getName() + ":" + method.getName();
            // 只将限制级别为Redis的方法放入Redis的方法映射中
            if (isRedisLevel) {
                redisLevelMethodNameList.add(methodName);
            } else {
                localLevelMethodNameList.add(methodName);
            }
        }
        long maxRedisLevelMethodCode = 0L;
        if (!redisLevelMethodNameList.isEmpty()) {
            List<List<Object>> resList = redissonClient.getScript()
                    .eval(RScript.Mode.READ_WRITE, GLOBAL_TARGET_REGISTER_SCRIPT, RScript.ReturnType.MULTI, Arrays.asList(methodRegisterKey), redisLevelMethodNameList.toArray());
            int successCnt = 0;
            for (List<Object> res : resList) {
                if (res.size() == 2) {
                    ++successCnt;
                    methodRegisterMap.put((String) res.get(0), (Long) res.get(1));
                    maxRedisLevelMethodCode = Math.max(maxRedisLevelMethodCode, (Long) res.get(1));
                }
            }
            log.info("注册Redis级别访问限制方法" + successCnt + "个");
        }
        if (!localLevelMethodNameList.isEmpty()) {
            for (int i = 0; i < localLevelMethodNameList.size(); ++i) {
                // 从redis级别的方法编号的最大值往后排号
                methodRegisterMap.put(localLevelMethodNameList.get(i), maxRedisLevelMethodCode + i + 1);
            }
        }
        this.initialized = true;
    }

    @Pointcut("@annotation(org.laputa.rivulet.access_limit.annotation.AccessLimit)")
    public void accessLimit() {
    }

    @Pointcut("@annotation(org.laputa.rivulet.access_limit.annotation.AccessLimits)")
    public void accessLimits() {
    }

    @Pointcut("@annotation(org.laputa.rivulet.access_limit.annotation.RequireLogin)")
    public void requireLoginPoint() {
    }

    @Around("requireLoginPoint()")
    public Result<?> testIfLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        RvAdmin currentAdmin = appAuth.getCurrentAdmin();
        if (currentAdmin != null && currentAdmin.getId() != null) {
            return (Result<?>) joinPoint.proceed();
        } else {
            return Result.fail("requestNeedLogin", "请求失败，该请求需要登录");
        }
    }

    @Around("accessLimit()")
    public Object parseAccessLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        return doParseAccessLimit(joinPoint, false);
    }

    @Around("accessLimits()")
    public Object parseAccessLimits(ProceedingJoinPoint joinPoint) throws Throwable {
        return doParseAccessLimit(joinPoint, true);
    }

    private Object doParseAccessLimit(ProceedingJoinPoint joinPoint, boolean isMulti) throws Throwable {
        MethodSignature sign = (MethodSignature) joinPoint.getSignature();
        Method method = sign.getMethod();
        Class<?> methodReturnType = method.getReturnType();
        if (!this.initialized) {
            if (methodReturnType.isAssignableFrom(Result.class)) {
                return Result.FAIL;
            } else {
                return null;
            }
        }
        Parameter[] parameters = method.getParameters();
        RvBaseEntity<?> target = null;
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; ++i) {
            Parameter parameter = parameters[i];
            Object argValue = args[i];
            if (parameter.isAnnotationPresent(AccessLimitTarget.class)) {
                AccessLimitTarget limitTarget = parameter.getAnnotation(AccessLimitTarget.class);
                if (argValue instanceof RvBaseEntity) {
                    target = (RvBaseEntity<?>) argValue;
                    if (!"".equals(limitTarget.byMethod())) {
                        String methodName = limitTarget.byMethod();
                        target = (RvBaseEntity<?>) target.getClass().getDeclaredMethod(methodName).invoke(target);
                    }
                }
            }
        }
//        if ((operator.getUserId() != null && operator.getUserId() < 0) || operator.isSuperAdmin()) {
//            // 未登录或超级管理员不受任何限制
//            return (Result<?>) joinPoint.proceed();
//        }
        AccessLimit[] limits;
        if (isMulti) {
            AccessLimits limitsAnnotation = method.getAnnotation(AccessLimits.class);
            Arrays.sort(limitsAnnotation.value(), Comparator.comparing(AccessLimit::unit).reversed());
            limits = limitsAnnotation.value();
        } else {
            AccessLimit limitAnnotation = method.getAnnotation(AccessLimit.class);
            limits = new AccessLimit[]{limitAnnotation};
        }
        boolean result = doAccessLimit(method, target, limits);
        if (result) {
            return joinPoint.proceed();
        } else {
            if (methodReturnType.isAssignableFrom(Result.class)) {
                return Result.fail("tooManyRequest", "请求过于频繁，请稍后再试");
            } else {
                return null;
            }
        }
    }

    /**
     * 执行访问限制，返回true表示允许访问，反之禁止访问
     *
     * @param target
     * @param limits
     * @return
     */
    private boolean doAccessLimit(Method method, RvBaseEntity<?> target, AccessLimit[] limits) {
        boolean isRedisLevel = false;
        if (method.isAnnotationPresent(AccessLimitLevel.class)) {
            AccessLimitLevel level = method.getAnnotation(AccessLimitLevel.class);
            isRedisLevel = AccessLimitLevel.Level.Redis.equals(level.value());
        }
        String methodName = method.getClass().getName() + ":" + method.getName();
        Long methodCode = methodRegisterMap.get(methodName);
        String targetId;
        if (target != null) {
            targetId = methodCode + "!" + target.getId();
        } else {
            targetId = methodCode.toString();
        }
        // 从redis中取出该用户对于该目标的访问记录，结果为字符串，格式为
        // 时间单位1@已访问次数1@计数开始时间戳1#时间单位2@已访问次数2@计数开始时间戳2
        // 时间单位为s/m/h，表示秒、分钟、小时，已访问次数为该时间单位内已访问的次数
        // 计数开始时间戳为已访问次数开始计数时的时间戳
        String redisAccessMapKey = RedisPrefix.OPERATOR_ACCESS_MAP + getCurrentUserIdOrIpAddress();
        String lastAccessStr;
        if (isRedisLevel) {
            lastAccessStr = (String) redissonClient.getMap(redisAccessMapKey).get(targetId);
        } else {
            lastAccessStr = getLocalLastAccessStr(redisAccessMapKey, targetId);
        }
        Map<String, Long[]> accessMap = new HashMap<>();
        boolean isFirstAccess = false;
        if (lastAccessStr == null) {
            isFirstAccess = true;
        } else {
            // 解析访问记录字符串
            // 获取访问记录的多个时间单位部分
            String[] accessStrParts = lastAccessStr.split("#");
            for (String str : accessStrParts) {
                // 对于每个时间单位，获取时间单位标志、已访问次数和计数开始时间戳
                String[] parts = str.split("@");
                if (parts.length < 3) {
                    continue;
                }
                // 将对于该时间单位的已访问次数和计数开始时间戳放到map中
                accessMap.put(parts[0], new Long[]{Long.valueOf(parts[1]), Long.valueOf(parts[2])});
            }
        }
        long now = System.currentTimeMillis();
        boolean result = true;
        for (AccessLimit limit : limits) {
            String limitTimeUnit = limit.duration() + limit.unit().getSymbol();
            if (isFirstAccess) {
                accessMap.put(limitTimeUnit, new Long[]{now, 0L});
                continue;
            }
            Long lastAccessTime = accessMap.get(limitTimeUnit)[0];
            Long hasAccessCount = accessMap.get(limitTimeUnit)[1];
            if (lastAccessTime == null) {
                accessMap.put(limitTimeUnit, new Long[]{now, 0L});
                continue;
            }
            int timeDiff = limit.duration() * limit.unit().getBaseValue();
            if (now - lastAccessTime > timeDiff) {
                // 已经超过访问限制间隔时间，允许访问
                accessMap.put(limitTimeUnit, new Long[]{now, 0L});
                continue;
            }
            int limitValue = limit.times();
            if (hasAccessCount <= limitValue) {
                // 没有超过允许访问次数
                accessMap.put(limitTimeUnit, new Long[]{lastAccessTime, hasAccessCount + 1});
                continue;
            }
            // 否则，没有超过限制访问间隔时间，且达到允许访问次数，则禁止访问
            result = false;
        }
        StringBuilder accessStr = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, Long[]> entry : accessMap.entrySet()) {
            String timeUnit = entry.getKey();
            Long[] accessValue = entry.getValue();
            Long accessTime = accessValue[0];
            Long accessCount = accessValue[1];
            String str = timeUnit + "@" + accessTime + "@" + accessCount;
            accessStr.append(isFirst ? "" : "#").append(str);
            isFirst = false;
        }
        if (isRedisLevel) {
            redissonClient.getMap(redisAccessMapKey).put(targetId, accessStr.toString());
        } else {
            // 在getLocalLastAccessStr中已经初始化了对应的Map，所以可以直接调用put
            methodAccessMap.get(redisAccessMapKey).put(targetId, accessStr.toString());
        }
        return result;
    }

    private String getCurrentUserIdOrIpAddress() {
        // 登录的情况下根据用户ID限制访问，未登录的情况下根据IP地址限制访问
        RvAdmin currentUser = appAuth.getCurrentAdmin();
        if (currentUser != null) {
            return "US:" + currentUser.getId();
        } else {
            return "IP:" + JakartaServletUtil.getClientIP(request);
        }
    }

    private String getLocalLastAccessStr(String accessMapKey, String targetId) {
        Map<String, String> accessMap = methodAccessMap.computeIfAbsent(accessMapKey, k -> new HashMap<>());
        return accessMap.get(targetId);
    }

    /**
     * 每天清空访问记录
     */
    @Scheduled(cron = "0 10 4 * * ?")
    public void dailyFlushRedisAccess() {
        redissonClient.getKeys().deleteByPattern(RedisPrefix.OPERATOR_ACCESS_MAP);
    }
}
