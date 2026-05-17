package org.laputa.rivulet.common.hibernate;

import cn.hutool.core.util.StrUtil;
import jakarta.persistence.*;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvBaseEntity;
import org.laputa.rivulet.common.state.AppState;
import org.laputa.rivulet.common.util.CustomDataModelUtil;
import org.laputa.rivulet.common.util.SpringBeanUtil;
import org.laputa.rivulet.module.dbms_model.entity.inter.DataModelEntityInterface;
import org.laputa.rivulet.module.jpa_model.entity.RvProperty;
import org.laputa.rivulet.module.jpa_model.entity.RvPrototype;
import org.laputa.rivulet.module.jpa_model.enums.PropertyType;
import org.laputa.rivulet.module.jpa_model.repository.RvPrototypeRepository;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RvAdditionalMappingContributor implements AdditionalMappingContributor {
    RvPrototypeRepository rvPrototypeRepository;
    AppState appState;

    @Override
    public String getContributorName() {
        return getClass().getSimpleName();
    }

    private static String underline(String str) {
        return StrUtil.toUnderlineCase(str);
    }

    private static CascadeType[] cascadeTypes(String str) {
        if (str == null) return new CascadeType[]{};
        String[] parts = str.split(",");
        return Arrays.stream(parts).map(name -> CascadeType.valueOf(name.toUpperCase())).toArray(CascadeType[]::new);
    }

    @SneakyThrows
    private static void setAnnotation(RvProperty rvProperty, AnnotationDescription.Builder builder, String[] attrNames) {
        for (String attrName : attrNames) {
            if (Strings.unique.equals(attrName) && rvProperty.getUnique() != null) {
                builder.define(Strings.unique, rvProperty.getUnique());
            } else if (Strings.nullable.equals(attrName) && rvProperty.getNullable() != null) {
                builder.define(Strings.nullable, rvProperty.getNullable());
            } else if (Strings.insertable.equals(attrName) && rvProperty.getInsertable() != null) {
                builder.define(Strings.insertable, rvProperty.getInsertable());
            } else if (Strings.updatable.equals(attrName) && rvProperty.getUpdatable() != null) {
                builder.define(Strings.updatable, rvProperty.getUpdatable());
            } else if (Strings.columnDefinition.equals(attrName) && rvProperty.getColumnDefinition() != null) {
                builder.define(Strings.columnDefinition, rvProperty.getColumnDefinition());
            } else if (Strings.referencedColumnName.equals(attrName) && rvProperty.getReferencedColumnName() != null) {
                builder.define(Strings.referencedColumnName, rvProperty.getReferencedColumnName());
            } else if (Strings.targetEntityClassName.equals(attrName) && rvProperty.getTargetEntityClassName() != null) {
                builder.define(Strings.targetEntityClassName, Class.forName(rvProperty.getTargetEntityClassName()));
            } else if (Strings.cascade.equals(attrName) && rvProperty.getCascade() != null) {
                builder.defineEnumerationArray(Strings.cascade, CascadeType.class, cascadeTypes(rvProperty.getCascade()));
            } else if (Strings.table.equals(attrName) && rvProperty.getTable() != null) {
                builder.define(Strings.table, rvProperty.getTable());
            } else if (Strings.length.equals(attrName) && rvProperty.getLength() != null) {
                builder.define(Strings.length, rvProperty.getLength());
            } else if (Strings.precision.equals(attrName) && rvProperty.getPrecision() != null) {
                builder.define(Strings.precision, rvProperty.getPrecision());
            } else if (Strings.scale.equals(attrName) && rvProperty.getScale() != null) {
                builder.define(Strings.scale, rvProperty.getScale());
            } else if (Strings.fetch.equals(attrName) && rvProperty.getFetch() != null) {
                builder.define(Strings.fetch, FetchType.valueOf(rvProperty.getFetch().toUpperCase()));
            } else if (Strings.optional.equals(attrName) && rvProperty.getOptional() != null) {
                builder.define(Strings.optional, rvProperty.getOptional());
            } else if (Strings.mappedBy.equals(attrName) && rvProperty.getMappedBy() != null) {
                builder.define(Strings.mappedBy, rvProperty.getMappedBy());
            } else if (Strings.orphanRemoval.equals(attrName) && rvProperty.getOrphanRemoval() != null) {
                builder.define(Strings.orphanRemoval, rvProperty.getOrphanRemoval());
            }
        }
    }

    @SneakyThrows
    private Class<?> convertRvPrototypeToEntityClass(RvPrototype rvPrototype) {
        String className = CustomDataModelUtil.getCustomDataModelClassName(rvPrototype.getCode());
        DynamicType.Builder<?> builder = new ByteBuddy().subclass(RvBaseEntity.class).name(className)
                .implement(DataModelEntityInterface.class)
                .annotateType(AnnotationDescription.Builder.ofType(Entity.class).build())
                .annotateType(AnnotationDescription.Builder.ofType(DynamicInsert.class).build())
                .annotateType(AnnotationDescription.Builder.ofType(DynamicUpdate.class).build())
                .annotateType(AnnotationDescription.Builder.ofType(Table.class)
                        .define(Strings.name, underline(rvPrototype.getCode())).build());
        List<RvProperty> rvProperties = rvPrototype.getProperties();
        for (RvProperty rvProperty : rvProperties) {
            List<AnnotationDescription> annotationDescriptions = new ArrayList<>();
            if (rvProperty.getType() == PropertyType.Id || rvProperty.getType() == PropertyType.Attribute) {
                AnnotationDescription.Builder columnAnnotation = AnnotationDescription.Builder.ofType(Column.class)
                        .define(Strings.name, underline(rvProperty.getCode()));
                setAnnotation(rvProperty, columnAnnotation, new String[]{Strings.unique, Strings.nullable,
                        Strings.insertable, Strings.updatable, Strings.columnDefinition, Strings.table,
                        Strings.length, Strings.precision, Strings.scale});
                if (rvProperty.getType() == PropertyType.Id) {
                    annotationDescriptions.add(AnnotationDescription.Builder.ofType(Id.class).build());
                    annotationDescriptions.add(AnnotationDescription.Builder.ofType(UuidGenerator.class).build());
                }
                annotationDescriptions.add(columnAnnotation.build());
            } else if (rvProperty.getType() == PropertyType.OneToOne || rvProperty.getType() == PropertyType.ManyToOne) {
                AnnotationDescription.Builder joinColumnAnnotation = AnnotationDescription.Builder.ofType(JoinColumn.class)
                        .define(Strings.name, underline(rvProperty.getCode()));
                setAnnotation(rvProperty, joinColumnAnnotation, new String[]{Strings.unique, Strings.nullable,
                        Strings.insertable, Strings.updatable, Strings.columnDefinition, Strings.table,
                        Strings.referencedColumnName});
                annotationDescriptions.add(joinColumnAnnotation.build());
                if (rvProperty.getType() == PropertyType.OneToOne) {
                    AnnotationDescription.Builder oneToOneAnnotation = AnnotationDescription.Builder.ofType(OneToOne.class);
                    setAnnotation(rvProperty, oneToOneAnnotation, new String[]{
                            Strings.targetEntityClassName, Strings.cascade, Strings.fetch,
                            Strings.optional, Strings.mappedBy, Strings.orphanRemoval});
                    annotationDescriptions.add(oneToOneAnnotation.build());
                } else {
                    AnnotationDescription.Builder manyToOneAnnotation = AnnotationDescription.Builder.ofType(ManyToOne.class);
                    setAnnotation(rvProperty, manyToOneAnnotation, new String[]{
                            Strings.targetEntityClassName, Strings.cascade, Strings.fetch, Strings.optional});
                    annotationDescriptions.add(manyToOneAnnotation.build());
                }
            } else if (rvProperty.getType() == PropertyType.OneToMany) {
                AnnotationDescription.Builder oneToManyAnnotation = AnnotationDescription.Builder.ofType(OneToMany.class);
                setAnnotation(rvProperty, oneToManyAnnotation, new String[]{
                        Strings.targetEntityClassName, Strings.cascade, Strings.fetch, Strings.mappedBy, Strings.orphanRemoval
                });
                annotationDescriptions.add(oneToManyAnnotation.build());
            } else if (rvProperty.getType() == PropertyType.ManyToMany) {
                AnnotationDescription.Builder manyToManyAnnotation = AnnotationDescription.Builder.ofType(ManyToMany.class);
                setAnnotation(rvProperty, manyToManyAnnotation, new String[]{
                        Strings.targetEntityClassName, Strings.cascade, Strings.fetch, Strings.mappedBy
                });
                annotationDescriptions.add(manyToManyAnnotation.build());
            }
            Class<?> valueClass = Class.forName(rvProperty.getValueClassName());
            // defineProperty会自动生成getter和setter，并且为private
            builder = builder.defineProperty(rvProperty.getCode(), valueClass).annotateField(annotationDescriptions);
        }
        Instrumentation instrumentation = ByteBuddyAgent.install();
        Optional<Class> existedClass = Arrays.stream(instrumentation.getAllLoadedClasses()).filter(c -> c.getName().equals(className)).findFirst();
        if (existedClass.isPresent()) {
            instrumentation.redefineClasses(
                    new ClassDefinition(existedClass.get(), builder.make().getBytes()));
            return existedClass.get();
        } else {
            return builder.make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
        }
    }

    @SneakyThrows
    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector metadata, ResourceStreamLocator resourceStreamLocator, MetadataBuildingContext buildingContext) {
        if (SpringBeanUtil.contextInitialized()) {
            if (appState == null) {
                appState = SpringBeanUtil.getBean(AppState.class);
            }
            if (!appState.getAllLoadedDataModelSynced().getCurrentValue()) {
                // 有变动的模型还未同步到数据库，则不能读取自定义的模型
                return;
            }
            if (rvPrototypeRepository == null) {
                rvPrototypeRepository = SpringBeanUtil.getBean(RvPrototypeRepository.class);
            }
            List<RvPrototype> rvPropertyList = rvPrototypeRepository.findAllWithProperties();
            rvPropertyList.forEach(rvPrototype -> contributions.contributeEntity(convertRvPrototypeToEntityClass(rvPrototype)));
        }
    }
}
