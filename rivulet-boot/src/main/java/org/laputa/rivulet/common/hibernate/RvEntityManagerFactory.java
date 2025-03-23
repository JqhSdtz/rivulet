package org.laputa.rivulet.common.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import liquibase.Scope;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 实现一个支持提供重新构造EntityManagerFactory对象的工厂类，用于运行时重新构造Hibernate的元数据，以实现动态增加Model
 *
 * @author JQH
 * @since 下午 10:38 22/09/05
 */
@Component
public class RvEntityManagerFactory {
    private final JpaProperties jpaProperties;
    private final PersistenceUnitInfo persistenceUnitInfo;
    private final RvHibernatePersistenceProvider persistenceProvider;

    private EntityManagerFactory entityManagerFactory;

    public RvEntityManagerFactory(DataSource dataSource, JpaProperties jpaProperties) {
        this.jpaProperties = jpaProperties;
        DefaultPersistenceUnitManager internalPersistenceUnitManager = new DefaultPersistenceUnitManager();
        internalPersistenceUnitManager.setResourceLoader(new DefaultResourceLoader(Scope.getCurrentScope().getClassLoader()));
        internalPersistenceUnitManager.setPackagesToScan("org.laputa.rivulet.**.entity");
        internalPersistenceUnitManager.setDefaultDataSource(dataSource);
        internalPersistenceUnitManager.preparePersistenceUnitInfos();
        this.persistenceUnitInfo = internalPersistenceUnitManager.obtainDefaultPersistenceUnitInfo();
        this.persistenceProvider = new RvHibernatePersistenceProvider();
        this.entityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, jpaProperties.getProperties());
    }

    public void rebuildEntityManagerFactory() {
        this.entityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, jpaProperties.getProperties());
    }

    /**
     * EntityManager本来就是Request级别的注入，正常就是每次请求都会调用createEntityManager
     *
     * @return 创建的EntityManager对象
     */
    public EntityManager getEntityManager() {
        return this.entityManagerFactory.createEntityManager();
    }

}
