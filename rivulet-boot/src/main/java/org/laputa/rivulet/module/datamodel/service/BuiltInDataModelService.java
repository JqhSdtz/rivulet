package org.laputa.rivulet.module.datamodel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import java.util.Set;

/**
 * @author JQH
 * @since 下午 6:47 22/09/04
 */
@Service
@Order(3)
@Slf4j
public class BuiltInDataModelService implements ApplicationRunner {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void run(ApplicationArguments args) {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        System.out.println("entity");
        entities.forEach(entityType -> {
            entityType.getJavaType();
        });
    }
}
