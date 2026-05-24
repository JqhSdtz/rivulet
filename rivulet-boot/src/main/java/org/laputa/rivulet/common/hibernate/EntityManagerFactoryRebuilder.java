package org.laputa.rivulet.common.hibernate;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManagerFactory;
import org.laputa.rivulet.common.state.AppState;
import org.laputa.rivulet.common.state.EventBus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.function.Consumer;

/***
 * 这里order设为1，表示加载完毕后要立即重新加载hibernate，以便加载数据库中的prototype
 */
@Service
@Order(1)
public class EntityManagerFactoryRebuilder implements InitializingBean, ApplicationRunner {
    private final DataSource dataSource;
    private final JpaProperties jpaProperties;
    private final EntityManagerFactoryProxyPostProcessor proxyPostProcessor;
    private final AppState appState;

    public EntityManagerFactoryRebuilder(DataSource dataSource, JpaProperties jpaProperties, EntityManagerFactoryProxyPostProcessor proxyPostProcessor, AppState appState) {
        this.dataSource = dataSource;
        this.jpaProperties = jpaProperties;
        this.proxyPostProcessor = proxyPostProcessor;
        this.appState = appState;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        rebuildAndSwap(null);
    }

    /**
     加载spring框架后初始化EntityManagerFactory
     */
    @Override
    public void afterPropertiesSet() {
        rebuildAndSwap(null);
    }

    /**
     * 创建包含所有静态实体 + 动态实体的新 EntityManagerFactory，并执行切换
     */
    public void rebuildAndSwap(Consumer<Properties> jpaPropertiesCustomizer) {
        EntityManagerFactory newFactory = createNewEntityManagerFactory(jpaPropertiesCustomizer);
        // 直接切换代理底层的工厂
        proxyPostProcessor.replaceEntityManagerFactory(newFactory);
    }

    private EntityManagerFactory createNewEntityManagerFactory(Consumer<Properties> jpaPropertiesCustomizer) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan("org.laputa.rivulet.**.entity");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties properties = new Properties();
        properties.putAll(jpaProperties.getProperties()); // 核心：使用 spring.jpa 配置
        // 允许自定义覆盖
        if (jpaPropertiesCustomizer != null) {
            jpaPropertiesCustomizer.accept(properties);
        }
        factoryBean.setJpaProperties(properties);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}