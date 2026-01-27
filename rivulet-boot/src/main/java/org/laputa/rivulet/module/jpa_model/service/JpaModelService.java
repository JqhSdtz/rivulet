package org.laputa.rivulet.module.jpa_model.service;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.module.jpa_model.entity.RvProperty;
import org.laputa.rivulet.module.jpa_model.entity.RvPrototype;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Order()
@Slf4j
public class JpaModelService implements ApplicationRunner {
    private static List<RvPrototype> rvPrototypes = new CopyOnWriteArrayList<>();

    public void run(ApplicationArguments args) throws Exception {
        Reflections reflections = new Reflections(Scanners.TypesAnnotated);
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);
        for (Class<?> entityClass : entityClasses) {
            RvPrototype rvPrototype = new RvPrototype();
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            if (tableAnnotation == null) continue;
            rvPrototype.setCode(tableAnnotation.name());
            Title titleAnnotation = entityClass.getAnnotation(Title.class);
            if (titleAnnotation != null) {
                rvPrototype.setTitle(titleAnnotation.value());
            } else {
                rvPrototype.setTitle(tableAnnotation.name());
            }
            TableComment tableCommentAnnotation = entityClass.getAnnotation(TableComment.class);
            if (tableCommentAnnotation != null) {
                rvPrototype.setRemark(tableCommentAnnotation.value());
            }
            for (Field field : entityClass.getDeclaredFields()) {
                RvProperty rvProperty = new RvProperty();
                //TODO 按照Column,ManyToOne,ManyToMany,OneToMany,OneToOne进行分类
            }
        }
    }
}
