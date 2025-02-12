package org.laputa.rivulet.common.config;

import org.laputa.rivulet.ddl.LiquibaseDdlExecutor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.annotation.Resource;
import jakarta.persistence.Column;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.lang.reflect.Field;

/**
 * @author JQH
 * @since 下午 10:38 22/09/05
 */
@Configuration(proxyBeanMethods = false)
public class JpaConfig {
    @Resource
    private LiquibaseDdlExecutor ddlExecutor;

    @Bean
    @Primary
    @ConfigurationProperties("spring.jpa")
    public JpaProperties firstJpaProperties() {
        return new JpaProperties();
    }

    private JpaVendorAdapter createJpaVendorAdapter(JpaProperties jpaProperties) {
        // ... map JPA properties as needed
        return new HibernateJpaVendorAdapter();
    }

    @Bean("entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean firstEntityManagerFactory(DataSource firstDataSource, JpaProperties firstJpaProperties) {
        EntityManagerFactoryBuilder builder = createEntityManagerFactoryBuilder(firstJpaProperties);
        LocalContainerEntityManagerFactoryBean factoryBean = builder.dataSource(firstDataSource).packages(Order.class).persistenceUnit("firstDs").build();
        factoryBean.setPackagesToScan("org.laputa.rivulet.**.entity");
        return factoryBean;
    }

    private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(JpaProperties jpaProperties) {
        JpaVendorAdapter jpaVendorAdapter = createJpaVendorAdapter(jpaProperties);
        EntityManagerFactoryBuilder factoryBuilder =  new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProperties.getProperties(), null);
        factoryBuilder.setPersistenceUnitPostProcessors(pui -> {
            pui.getManagedClassNames().forEach(className -> {
                try {
                    Class entityClass = Class.forName(className);
                    processEntityClass(entityClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        return factoryBuilder;
    }

    private void processEntityClass(Class entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field: fields) {
            Column column = field.getAnnotation(Column.class);
//            System.out.println(column.columnDefinition());
//            System.out.println(ddlExecutor.convertDataTypeToSqlType(column.columnDefinition(), false));
        }
    }
}
