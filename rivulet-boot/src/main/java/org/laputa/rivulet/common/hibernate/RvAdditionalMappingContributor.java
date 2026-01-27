package org.laputa.rivulet.common.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
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
import org.laputa.rivulet.module.dbms_model.entity.inter.DataModelEntityInterface;

public class RvAdditionalMappingContributor implements AdditionalMappingContributor {

    @Override
    public String getContributorName() {
        return getClass().getSimpleName();
    }

    @SneakyThrows
    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector metadata, ResourceStreamLocator resourceStreamLocator, MetadataBuildingContext buildingContext) {
        if (!Strings.RIVULET.equals(buildingContext.getCurrentContributorName())) return;
        Class<?> entityClass = new ByteBuddy().subclass(RvBaseEntity.class).name("org.laputa.rivulet.module.dict.entity.RvTest")
                .implement(DataModelEntityInterface.class)
                .annotateType(AnnotationDescription.Builder.ofType(Entity.class).build())
                .annotateType(AnnotationDescription.Builder.ofType(DynamicInsert.class).build())
                .annotateType(AnnotationDescription.Builder.ofType(DynamicUpdate.class).build())
                .annotateType(AnnotationDescription.Builder.ofType(Table.class)
                        .define("name", "rv_test").build())
                .defineField("id", String.class, Visibility.PRIVATE)
                .annotateField(AnnotationDescription.Builder.ofType(Id.class).build())
                .annotateField(AnnotationDescription.Builder.ofType(UuidGenerator.class).build())
                .annotateField(AnnotationDescription.Builder.ofType(Column.class)
                        .define("name", "id").define("nullable", false).define("length", 64).build())
                .defineMethod("getId", String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofBeanProperty())
                .defineMethod("setId", void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofBeanProperty())
                .defineField("val", String.class, Visibility.PRIVATE)
                .annotateField(AnnotationDescription.Builder.ofType(Column.class)
                        .define("name", "val").define("nullable", false).define("length", 64).build())
                .defineMethod("getVal", String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofBeanProperty())
                .defineMethod("setVal", void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofBeanProperty())
                .make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
        contributions.contributeEntity(entityClass);
    }
}
