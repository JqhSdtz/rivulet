package org.laputa.rivulet.module.jpa_model.service;

import jakarta.persistence.*;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import liquibase.ext.hibernate.util.TableRemarkMetaInfo;
import liquibase.ext.hibernate.util.TableRemarkMetaInfoUtil;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Order(1002)
@Slf4j
public class JpaModelService implements ApplicationRunner {

    private static String cascadeTypesToString(CascadeType[] cascadeTypes) {
        StringBuilder sb = new StringBuilder();
        for (CascadeType cascadeType : cascadeTypes) {
            sb.append(cascadeType.name());
        }
        return sb.toString();
    }

    public List<RvPrototype> getRvPrototypesByReflection() {
        List<RvPrototype> rvPrototypes = new CopyOnWriteArrayList<>();
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
                TableRemarkMetaInfo metaInfo = new TableRemarkMetaInfo();
                metaInfo.setClassName(entityClass.getName());
                rvPrototype.setRemark(tableCommentAnnotation.value());
                rvPrototype.setRemark(TableRemarkMetaInfoUtil.setMetaInfo(tableCommentAnnotation.value(), metaInfo));
            }
            int fieldCount = entityClass.getDeclaredFields().length;
            List<RvProperty> rvProperties = new ArrayList<>();
            rvPrototype.setProperties(rvProperties);
            for (int i = 0; i < fieldCount; i++) {
                Field field = entityClass.getDeclaredFields()[i];
                RvProperty rvProperty = new RvProperty();
                rvProperties.add(rvProperty);
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
                Id idAnnotation = field.getAnnotation(Id.class);
                if (idAnnotation != null) {
                    rvProperty.setType(PropertyType.Id);
                }
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null) {
                    // 避免覆盖Id类型
                    if (rvProperty.getType() == null) {
                        rvProperty.setType(PropertyType.Attribute);
                    }
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
                    rvProperty.setTable(joinColumnAnnotation.table());
                    rvProperty.setReferencedColumnName(joinColumnAnnotation.referencedColumnName());
                }
                // 不使用@JoinTable注解，关联关系交给JPA自动维护，关联表也由JPA自动生成
                OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
                if (oneToOneAnnotation != null) {
                    rvProperty.setType(PropertyType.OneToOne);
                    rvProperty.setTargetEntityClassName(oneToOneAnnotation.targetEntity().getName());
                    rvProperty.setCascade(cascadeTypesToString(oneToOneAnnotation.cascade()));
                    rvProperty.setFetch(oneToOneAnnotation.fetch().name());
                    rvProperty.setOptional(oneToOneAnnotation.optional());
                    rvProperty.setMappedBy(oneToOneAnnotation.mappedBy());
                    rvProperty.setOrphanRemoval(oneToOneAnnotation.orphanRemoval());
                }
                OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
                if (oneToManyAnnotation != null) {
                    rvProperty.setType(PropertyType.OneToMany);
                    rvProperty.setTargetEntityClassName(oneToManyAnnotation.targetEntity().getName());
                    rvProperty.setCascade(cascadeTypesToString(oneToManyAnnotation.cascade()));
                    rvProperty.setFetch(oneToManyAnnotation.fetch().name());
                    rvProperty.setMappedBy(oneToManyAnnotation.mappedBy());
                    rvProperty.setOrphanRemoval(oneToManyAnnotation.orphanRemoval());
                }
                ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
                if (manyToOneAnnotation != null) {
                    rvProperty.setType(PropertyType.ManyToOne);
                    rvProperty.setTargetEntityClassName(manyToOneAnnotation.targetEntity().getName());
                    rvProperty.setCascade(cascadeTypesToString(manyToOneAnnotation.cascade()));
                    rvProperty.setFetch(manyToOneAnnotation.fetch().name());
                    rvProperty.setOptional(manyToOneAnnotation.optional());
                }
                ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
                if (manyToManyAnnotation != null) {
                    rvProperty.setType(PropertyType.ManyToMany);
                    rvProperty.setTargetEntityClassName(manyToManyAnnotation.targetEntity().getName());
                    rvProperty.setCascade(cascadeTypesToString(manyToManyAnnotation.cascade()));
                    rvProperty.setFetch(manyToManyAnnotation.fetch().name());
                    rvProperty.setMappedBy(manyToManyAnnotation.mappedBy());
                }
            }
            rvPrototypes.add(rvPrototype);
        }
        return rvPrototypes;
    }

    public void run(ApplicationArguments args) throws Exception {

    }
}
