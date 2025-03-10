package org.laputa.rivulet.module.app.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.state.AppState;
import org.laputa.rivulet.common.util.DatabaseUtil;
import org.laputa.rivulet.common.util.RedissonLockUtil;
import org.laputa.rivulet.common.util.TerminalKeyUtil;
import org.laputa.rivulet.common.util.TimeUnitUtil;
import org.laputa.rivulet.module.app.model.AppInitialData;
import org.laputa.rivulet.module.app.property.TerminalKeyProperty;
import org.laputa.rivulet.module.app.session.AppSessionAccessor;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.laputa.rivulet.module.auth.entity.dict.AdminType;
import org.laputa.rivulet.module.auth.repository.RvAdminRepository;
import org.laputa.rivulet.module.auth.session.AuthSessionAccessor;
import org.laputa.rivulet.module.auth.util.PasswordUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

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
    private DataSource dataSource;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedissonLockUtil redissonLockUtil;
    @Resource
    private AppSessionAccessor appSessionAccessor;
    @Resource
    private AuthSessionAccessor authSessionAccessor;
    @Resource
    private RvAdminRepository rvAdminRepository;
    @Resource
    private TerminalKeyProperty terminalKeyProperty;
    @Resource
    private AppState appState;
    @Resource
    private TerminalKeyUtil terminalKeyUtil;

    private static final String INITIAL_ADMIN_ID_BUCKET = "INITIAL_ADMIN_ID_BUCKET";

    /**
     * 初始密钥的Redisson对象桶
     */
    private RBucket<String> initKeyBucket;

    @PostConstruct
    private void postConstruct() {
        initKeyBucket = redissonClient.getBucket("initKey");
    }

    @SneakyThrows
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
        appInitialData.setCurrentAdmin(authSessionAccessor.getCurrentAdmin());
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
     * @param rvAdmin
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createInitialAdmin(RvAdmin rvAdmin) {
        if (!appSessionAccessor.isInitKeyVerified()) {
            return Result.fail("InitKeyHasNotBeenVerified", "非法操作，未校验初始密钥，无法创建初始用户");
        }
        // 在分布式锁中执行，避免创建多个初始用户
        // 创建初始用户是一次性操作，所以如果已经被锁定则无需再执行任何操作
        return redissonLockUtil.doWithLock("createInitialAdmin", () -> {
            if (!testAppInitialized()) {
                // 设置ID后，save就会执行update操作。jpa没有单独的update
                rvAdmin.setId(getInitialAdminId());
                rvAdmin.setPassword(PasswordUtil.encode(rvAdmin.getPassword()));
                rvAdmin.setAdminType(AdminType.INITIAL_ADMIN);
                rvAdmin.setActive(true);
                rvAdminRepository.save(rvAdmin);
                // 更改应用初始化状态
                this.appState.setAppInitialized(true);
                // 将创建的用户设为当前登录的用户，即实现直接登录
                authSessionAccessor.setCurrentAdmin(rvAdmin);
                return Result.succeed();
            } else {
                return Result.fail("InitialAdminExists", "创建失败，已存在初始用户");
            }
        }, Result.fail("InitialAdminIsLocked", "创建失败，其他线程或服务正在创建初始用户")).ofClass(Void.class);
    }

    public String getInitialAdminId() {
        RBucket<String> bucket = redissonClient.getBucket(INITIAL_ADMIN_ID_BUCKET);
        String initialAdminId = bucket.get();
        if (initialAdminId == null) {
            // 如果数据库有管理员表，则检查管理员表中是否有初始管理员记录
            if (DatabaseUtil.isTableExist(dataSource, DatabaseUtil.getTableNameByClass(RvAdmin.class))) {
                List<RvAdmin> adminList = rvAdminRepository.findByAdminType(AdminType.INITIAL_ADMIN);
                if (!adminList.isEmpty()) {
                    initialAdminId = adminList.get(0).getId();
                } else {
                    initialAdminId = DatabaseUtil.generateId();
                }
            } else {
                // 否则的话，肯定是系统没有初始化，直接创建一个初始管理员放到redis
                initialAdminId = DatabaseUtil.generateId();
            }
            bucket.set(initialAdminId);
        }
        return initialAdminId;
    }

    /**
     * 通过查询是否有初始用户来判断应用是否已经初始化
     *
     * @return
     */
    private boolean testAppInitialized() {
        Query query = entityManager
                .createQuery("select count(id) from RvAdmin where adminType = :adminType and active = true")
                .setParameter("adminType", AdminType.INITIAL_ADMIN);
        long count = (Long) query.getSingleResult();
        return count > 0;
    }
}
