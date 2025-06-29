package org.laputa.rivulet.common.hibernate;

import cn.hutool.core.map.MapUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import lombok.SneakyThrows;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.laputa.rivulet.common.constant.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class RvAdditionalMappingContributor implements AdditionalMappingContributor {

    private static final Set<String> classLoadSet = new ConcurrentSkipListSet<>();

    @Override
    public String getContributorName() {
        return getClass().getSimpleName();
    }

    @SneakyThrows
    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector metadata, ResourceStreamLocator resourceStreamLocator, MetadataBuildingContext buildingContext) {
        if (!Strings.RIVULET.equals(buildingContext.getCurrentContributorName())) return;
        ClassPool pool = ClassPool.getDefault();
        String className = "org.laputa.rivulet.module.dict.entity.RvTest";
        CtClass entityClass = pool.getOrNull(className);
        if (entityClass == null) {
            entityClass = pool.makeClass(className);
            ClassFile entityClassFile = entityClass.getClassFile();
            ConstPool constPool = entityClassFile.getConstPool();
            addAnnotation(entityClassFile, Entity.class, null);
            addAnnotation(entityClassFile, DynamicInsert.class, null);
            addAnnotation(entityClassFile, DynamicUpdate.class, null);
            addAnnotation(entityClassFile, Table.class, MapUtil.of(
                    "name",
                    new StringMemberValue("rv_test", constPool)
            ));
            CtField idField = CtField.make("private java.lang.String id;", entityClass);
            addAnnotation(entityClassFile, idField, Id.class, null);
            addAnnotation(entityClassFile, idField, UuidGenerator.class, null);
            Map<String, MemberValue> idPropertyMap = new HashMap<>();
            idPropertyMap.put("id", new StringMemberValue("id", constPool));
            idPropertyMap.put("nullable", new BooleanMemberValue(false, constPool));
            idPropertyMap.put("length", new LongMemberValue(64, constPool));
            addAnnotation(entityClassFile, idField, Column.class, idPropertyMap);
            entityClass.addField(idField);
            CtMethod setIdMethod = CtMethod.make(
                    "public void setId(java.lang.String id) { this.id = id; }",
                    entityClass
            );
            entityClass.addMethod(setIdMethod);
            CtMethod getIdMethod = CtMethod.make("public java.lang.String getId() { return id; }", entityClass);
            entityClass.addMethod(getIdMethod);
        }
        contributions.contributeEntity(entityClass.toClass());

//            contributions.contributeTable(table);
    }

    private void addAnnotation(ClassFile entityClassFile, Class<?> annotationClass, Map<String, MemberValue> memberValueMap) {
        ConstPool constPool = entityClassFile.getConstPool();
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) entityClassFile.getAttribute(
                AnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        }
        Annotation annotation = new Annotation(annotationClass.getName(), constPool);
        if (memberValueMap != null) {
            for (Map.Entry<String, MemberValue> entry : memberValueMap.entrySet()) {
                annotation.addMemberValue(entry.getKey(), entry.getValue());
            }
        }
        annotationsAttribute.addAnnotation(annotation);
        entityClassFile.addAttribute(annotationsAttribute);
    }

    private void addAnnotation(ClassFile entityClassFile, CtField field, Class<?> annotationClass, Map<String, MemberValue> memberValueMap) {
        ConstPool constPool = entityClassFile.getConstPool();
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) entityClassFile.getAttribute(
                AnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        }
        Annotation annotation = new Annotation(annotationClass.getName(), constPool);
        annotationsAttribute.addAnnotation(annotation);
        if (memberValueMap != null) {
            for (Map.Entry<String, MemberValue> entry : memberValueMap.entrySet()) {
                annotation.addMemberValue(entry.getKey(), entry.getValue());
            }
        }
        field.getFieldInfo().addAttribute(annotationsAttribute);
    }

    private void addAnnotation(ClassFile entityClassFile, CtMethod method, Class<?> annotationClass, Map<String, MemberValue> memberValueMap) {
        ConstPool constPool = entityClassFile.getConstPool();
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) entityClassFile.getAttribute(
                AnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        }
        Annotation annotation = new Annotation(annotationClass.getName(), constPool);
        annotationsAttribute.addAnnotation(annotation);
        if (memberValueMap != null) {
            for (Map.Entry<String, MemberValue> entry : memberValueMap.entrySet()) {
                annotation.addMemberValue(entry.getKey(), entry.getValue());
            }
        }
        method.getMethodInfo().addAttribute(annotationsAttribute);
    }
}
