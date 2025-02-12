package org.laputa.rivulet.module.app.service;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.util.RedissonLockUtil;
import org.laputa.rivulet.common.util.TerminalKeyUtil;
import org.laputa.rivulet.common.util.TimeUnitUtil;
import org.laputa.rivulet.module.app.model.AppInitialData;
import org.laputa.rivulet.common.state.AppState;
import org.laputa.rivulet.module.app.property.TerminalKeyProperty;
import org.laputa.rivulet.module.app.session.AppSessionAccessor;
import org.laputa.rivulet.module.auth.entity.RvUser;
import org.laputa.rivulet.module.auth.entity.dict.UserType;
import org.laputa.rivulet.module.auth.session.AuthSessionAccessor;
import org.laputa.rivulet.module.auth.util.PasswordUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 * @author JQH
 * @since 下午 8:48 22/03/31
 */
@Service
@Order(2)
@Slf4j
public class AppInitService implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedissonLockUtil redissonLockUtil;
    @Resource
    private AppSessionAccessor appSessionAccessor;
    @Resource
    private AuthSessionAccessor authSessionAccessor;
    @Resource
    private TerminalKeyProperty terminalKeyProperty;
    @Resource
    private AppState appState;
    @Resource
    private TerminalKeyUtil terminalKeyUtil;

    /**
     * 初始密钥的Redisson对象桶
     */
    private RBucket<String> initKeyBucket;

    @PostConstruct
    private void postConstruct() {
        initKeyBucket = redissonClient.getBucket("initKey");
    }

    @Override
    public void run(ApplicationArguments args) {
        appState.registerStateChangeCallback("builtInDataModelSynced", state -> {
            if (state.getCurrentValue().equals(false)) return;
            // 启动后先判断应用是否已经初始化
            appState.setAppInitialized(testAppInitialized());
            // 若应用未初始化，则启动后获取一个初始化密钥
            if (!appState.isAppInitialized()) {
                String timeStr = TimeUnitUtil.format(terminalKeyProperty.getTimeout(), terminalKeyProperty.getTimeUnit());
                log.info("应用的初始化密钥为: {}，请在{}内进行初始化操作", terminalKeyUtil.generateTerminalKey(initKeyBucket), timeStr);
            }
        });
    }

    /**
     * 获取应用初始数据
     *
     * @return
     */
    public Result<AppInitialData> getAppInitialData() {
        AppInitialData appInitialData = new AppInitialData();
        appInitialData.setAppState(this.appState);
        appInitialData.setCurrentUser(authSessionAccessor.getCurrentUser());
        return Result.succeed(appInitialData);
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
        // 在分布式锁中执行，避免创建多个初始用户
        // 创建初始用户是一次性操作，所以如果已经被锁定则无需再执行任何操作
        return redissonLockUtil.doWithLock("createInitialUser", () -> {
            if (!testAppInitialized()) {
                rvUser.setPassword(PasswordUtil.encode(rvUser.getPassword()));
                rvUser.setUserType(UserType.INITIAL_USER);
                entityManager.persist(rvUser);
                // 更改应用初始化状态
                this.appState.setAppInitialized(true);
                // 将创建的用户设为当前登录的用户，即实现直接登录
                authSessionAccessor.setCurrentUser(rvUser);
                return Result.succeed();
            } else {
                return Result.fail("InitialUserExists", "创建失败，已存在初始用户");
            }
        }, Result.fail("InitialUserIsLocked", "创建失败，其他线程或服务正在创建初始用户"));
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
}
