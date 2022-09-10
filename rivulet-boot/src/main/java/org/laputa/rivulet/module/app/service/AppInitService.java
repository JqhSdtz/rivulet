package org.laputa.rivulet.module.app.service;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.util.TimeUnitUtil;
import org.laputa.rivulet.module.app.model.AppInitialData;
import org.laputa.rivulet.module.app.model.AppState;
import org.laputa.rivulet.module.app.property.InitKeyProperty;
import org.laputa.rivulet.module.app.session.AppSessionAccessor;
import org.laputa.rivulet.module.auth.entity.RvUser;
import org.laputa.rivulet.module.auth.entity.dict.UserType;
import org.laputa.rivulet.module.auth.session.AuthSessionAccessor;
import org.laputa.rivulet.module.auth.util.PasswordUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author JQH
 * @since 下午 8:48 22/03/31
 */
@Service
@Order(1)
@Slf4j
public class AppInitService implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private InitKeyProperty initKeyProperty;

    @Resource
    private AppSessionAccessor appSessionAccessor;

    @Resource
    private AuthSessionAccessor authSessionAccessor;

    /**
     * 应用是否初始化的标志
     */
    private boolean isAppInitialized = false;

    /**
     * 初始密钥的Redisson对象桶
     */
    private RBucket<String> initKeyBucket;

    /**
     * 创建初始用户的分布式锁，用于避免创建多个初始用户
     */
    private RLock createInitialUserLock;

    @PostConstruct
    private void postConstruct() {
        initKeyBucket = redissonClient.getBucket("initKey");
        createInitialUserLock = redissonClient.getLock("createInitialUser");
    }

    @Override
    public void run(ApplicationArguments args) {
        // 启动后先判断应用是否已经初始化
        isAppInitialized = testAppInitialized();
        // 若应用未初始化，则启动后获取一个初始化密钥
        if (!isAppInitialized) {
            String timeStr = TimeUnitUtil.format(initKeyProperty.getTimeout(), initKeyProperty.getTimeUnit());
            log.info("应用的初始化密钥为: {}，请在{}内进行初始化操作", generateInitKey(), timeStr);
        }
    }

    /**
     * 获取应用初始数据
     *
     * @return
     */
    public AppInitialData getAppInitialData() {
        AppInitialData appInitialData = new AppInitialData();
        AppState appState = new AppState();
        appState.setAppInitialized(isAppInitialized);
        appInitialData.setAppState(appState);
        appInitialData.setCurrentUser(authSessionAccessor.getCurrentUser());
        return appInitialData;
    }

    /**
     * 判断初始密钥是否正确
     *
     * @param initKey 前端传入的初始密钥
     * @return
     */
    public boolean verifyInitKey(String initKey) {
        String originalInitKey = initKeyBucket.get();
        boolean isValid = originalInitKey != null && originalInitKey.equals(initKey);
        if (isValid) {
            appSessionAccessor.setInitKeyVerified(true);
        }
        return isValid;
    }

    /**
     * 创建初始用户
     *
     * @param rvUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createInitialUser(RvUser rvUser) {
        if (!appSessionAccessor.isInitKeyVerified()) {
            return Result.fail("InitKeyHasNotBeenVerified", "非法操作，未校验初始密钥，无法创建初始用户");
        }
        Result result = null;
        // 创建初始用户是一次性操作，所以如果已经被锁定则无需再执行任何操作
        if (createInitialUserLock.tryLock()) {
            try {
                if (!testAppInitialized()) {
                    rvUser.setPassword(PasswordUtil.encode(rvUser.getPassword()));
                    rvUser.setUserType(UserType.INITIAL_USER);
                    entityManager.persist(rvUser);
                    // 更改应用初始化状态
                    this.isAppInitialized = true;
                    // 将创建的用户设为当前登录的用户，即实现直接登录
                    authSessionAccessor.setCurrentUser(rvUser);
                } else {
                    result = Result.fail("InitialUserExists", "创建失败，已存在初始用户");
                }
            } finally {
                createInitialUserLock.unlock();
            }
        } else {
            result = Result.fail("InitialUserIsLocked", "创建失败，其他线程或服务正在创建初始用户");
        }
        return result == null ? Result.succeed() : result;
    }

    /**
     * 通过查询是否有初始用户来判断应用是否已经初始化
     *
     * @return
     */
    private boolean testAppInitialized() {
        Query query = entityManager
                .createQuery("select count(id) from RvUser where userType = :userType")
                .setParameter("userType", UserType.INITIAL_USER);
        long count = (Long) query.getSingleResult();
        return count > 0;
    }

    /**
     * 创建一个初始密钥，并同步更新redis
     *
     * @return
     */
    private String generateInitKey() {
        // 获取Redis中的初始密钥
        String initKey = initKeyBucket.get();
        // 如果没有，则创建一个初始密钥，并尝试设置Redis中的对应值
        if (initKey == null) {
            String tmpKey = RandomUtil.randomString(initKeyProperty.getRandomBase(), initKeyProperty.getLength());
            // 可能存在并发问题，所以使用trySet
            if (!initKeyBucket.trySet(tmpKey, initKeyProperty.getTimeout(), initKeyProperty.getTimeUnit())) {
                tmpKey = initKeyBucket.get();
            }
            initKey = tmpKey;
        } else {
            // 如果有了，则刷新过期时间
            initKeyBucket.expire(TimeUnitUtil.toDuration(initKeyProperty.getTimeout(), initKeyProperty.getTimeUnit()));
        }
        return initKey;
    }

}
