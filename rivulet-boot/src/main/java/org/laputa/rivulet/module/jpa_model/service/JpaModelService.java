package org.laputa.rivulet.module.jpa_model.service;

import jakarta.persistence.*;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Comment;
import org.laputa.rivulet.module.jpa_model.entity.RvProperty;
import org.laputa.rivulet.module.jpa_model.entity.RvPrototype;
import org.laputa.rivulet.module.jpa_model.enums.PropertyType;
import org.laputa.rivulet.module.jpa_model.enums.PropertyValueClassType;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.hibernate.annotations.Cache;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Order()
@Slf4j
public class JpaModelService implements ApplicationRunner {
    private static List<RvPrototype> rvPrototypes = new CopyOnWriteArrayList<>();

    private static String cascadeTypesToString(CascadeType[] cascadeTypes) {
        StringBuilder sb = new StringBuilder();
        for (CascadeType cascadeType : cascadeTypes) {
            sb.append(cascadeType.name());
        }
        return sb.toString();
    }

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
            int fieldCount = entityClass.getDeclaredFields().length;
            for (int i = 0; i < fieldCount; i++) {
                Field field = entityClass.getDeclaredFields()[i];
                RvProperty rvProperty = new RvProperty();
                rvProperty.setPrototype(rvPrototype);
                rvProperty.setOrderNum(i);
                Class<?> fieldClassType = field.getType();
                if (ClassUtils.isPrimitiveOrWrapper(fieldClassType)) {
                    rvProperty.setValueClassType(PropertyValueClassType.Basic);
                } else if (Collection.class.isAssignableFrom(fieldClassType)) {
                    rvProperty.setValueClassType(PropertyValueClassType.Collector);
                } else {
                    rvProperty.setValueClassType(PropertyValueClassType.Business);
                }
                rvProperty.setValueClassName(fieldClassType.getName());
                Title fieldTitleAnnotation = field.getAnnotation(Title.class);
                if (fieldTitleAnnotation != null) {
                    rvProperty.setTitle(fieldTitleAnnotation.value());
                }
                Comment fieldCommentAnnotation = field.getAnnotation(Comment.class);
                if (fieldCommentAnnotation != null) {
                    rvProperty.setRemark(fieldCommentAnnotation.value());
                }
                DefaultValue defaultValueAnnotation = field.getAnnotation(DefaultValue.class);
                if (defaultValueAnnotation != null) {
                    rvProperty.setDefaultValue(defaultValueAnnotation.value());
                }
                Cache cacheAnnotation = field.getAnnotation(Cache.class);
                if (cacheAnnotation != null) {
                    rvProperty.setUseCache(true);
                }
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null) {
                    rvProperty.setType(PropertyType.Attribute);
                    rvProperty.setCode(columnAnnotation.name());
                    rvProperty.setUnique(columnAnnotation.unique());
                    rvProperty.setNullable(columnAnnotation.nullable());
                    rvProperty.setInsertable(columnAnnotation.insertable());
                    rvProperty.setUpdatable(columnAnnotation.updatable());
                    rvProperty.setColumnDefinition(columnAnnotation.columnDefinition());
                    rvProperty.setLength(columnAnnotation.length());
                    rvProperty.setPrecision(columnAnnotation.precision());
                    rvProperty.setScale(columnAnnotation.scale());
                }
                // 不使用foreignKey属性，外键关联交给JPA自动维护
                JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
                if (joinColumnAnnotation != null) {
                    rvProperty.setCode(joinColumnAnnotation.name());
                    rvProperty.setUnique(joinColumnAnnotation.unique());
                    rvProperty.setNullable(joinColumnAnnotation.nullable());
                    rvProperty.setInsertable(joinColumnAnnotation.insertable());
                    rvProperty.setUpdatable(joinColumnAnnotation.updatable());
                    rvProperty.setColumnDefinition(joinColumnAnnotation.columnDefinition());
                }
                // 不使用@JoinTable注解，关联关系交给JPA自动维护，关联表也由JPA自动生成
                OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
                if (oneToOneAnnotation != null) {
                    rvProperty.setType(PropertyType.OneToOne);
                    rvProperty.setTargetEntityClassName(oneToOneAnnotation.targetEntity().getName());
                    rvProperty.setCascadeTypes(cascadeTypesToString(oneToOneAnnotation.cascade()));
                    rvProperty.setFetchType(oneToOneAnnotation.fetch().name());
                    rvProperty.setOptional(oneToOneAnnotation.optional());
                    rvProperty.setMappedBy(oneToOneAnnotation.mappedBy());
                }
                OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
                if (oneToManyAnnotation != null) {
                    rvProperty.setType(PropertyType.OneToMany);
                    rvProperty.setTargetEntityClassName(oneToManyAnnotation.targetEntity().getName());
                    rvProperty.setCascadeTypes(cascadeTypesToString(oneToManyAnnotation.cascade()));
                    rvProperty.setFetchType(oneToManyAnnotation.fetch().name());
                    rvProperty.setMappedBy(oneToManyAnnotation.mappedBy());
                    rvProperty.setOrphanRemoval(oneToManyAnnotation.orphanRemoval());
                }
                ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
                if (manyToOneAnnotation != null) {
                    rvProperty.setType(PropertyType.ManyToOne);
                    rvProperty.setTargetEntityClassName(manyToOneAnnotation.targetEntity().getName());
                    rvProperty.setCascadeTypes(cascadeTypesToString(manyToOneAnnotation.cascade()));
                    rvProperty.setFetchType(manyToOneAnnotation.fetch().name());
                    rvProperty.setOptional(manyToOneAnnotation.optional());
                }
                ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
                if (manyToManyAnnotation != null) {
                    rvProperty.setType(PropertyType.ManyToMany);
                    rvProperty.setTargetEntityClassName(manyToManyAnnotation.targetEntity().getName());
                    rvProperty.setCascadeTypes(cascadeTypesToString(manyToManyAnnotation.cascade()));
                    rvProperty.setFetchType(manyToManyAnnotation.fetch().name());
                    rvProperty.setMappedBy(manyToManyAnnotation.mappedBy());
                }
            }
        }
    }
}
