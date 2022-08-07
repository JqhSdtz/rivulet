package org.laputa.rivulet.ddl;

import cn.hutool.core.util.JAXBUtil;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.jaxb.hbm.spi.*;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.laputa.rivulet.module.datamodel.entity.RvPrototype;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.EntityManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

/**
 * @author JQH
 * @since 下午 8:14 22/02/08
 */
@Component
public class HibernateModelModifier {
    private MetadataSources metadataSources;

    public HibernateModelModifier(EntityManagerFactory entityManagerFactory) {
        StandardServiceRegistry serviceRegistry = entityManagerFactory.unwrap(SessionFactory.class).getSessionFactoryOptions().getServiceRegistry();
        this.metadataSources = new MetadataSources(serviceRegistry);
    }

    public JaxbHbmHibernateMapping addTable(RvPrototype prototype, @Nullable JaxbHbmHibernateMapping hibernateMapping) {
        if (hibernateMapping == null) {
            hibernateMapping = new JaxbHbmHibernateMapping();
        }
        JaxbHbmRootEntityType entityRoot = new JaxbHbmRootEntityType();
        entityRoot.setTable(prototype.getCode());
        entityRoot.setEntityName(prototype.getCode());
        hibernateMapping.getClazz().add(entityRoot);
        JaxbHbmSimpleIdType id = new JaxbHbmSimpleIdType();
        id.setName("id");
        JaxbHbmTypeSpecificationType idType = new JaxbHbmTypeSpecificationType();
        idType.setName("java.lang.String");
        id.setType(idType);
        id.setLength(64);
        entityRoot.setId(id);
        List<Serializable> attributes = entityRoot.getAttributes();
        prototype.getFields().forEach(field -> {
            JaxbHbmBasicAttributeType testColumn = new JaxbHbmBasicAttributeType();
            testColumn.setName(field.getName());
            testColumn.setColumnAttribute(field.getName());
            testColumn.setTypeAttribute("java.lang.String");
            attributes.add(testColumn);
        });
        return hibernateMapping;
    }

    public void createModel(JaxbHbmHibernateMapping hibernateMapping) {
        Metadata metadata = getMetadata(hibernateMapping);
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.createOnly(EnumSet.of(TargetType.DATABASE), metadata);
    }

    public void updateModel(JaxbHbmHibernateMapping hibernateMapping) {
        Metadata metadata = getMetadata(hibernateMapping);
        SchemaUpdate schemaUpdate = new SchemaUpdate();
        schemaUpdate.execute(EnumSet.of(TargetType.DATABASE), metadata);
    }

    private Metadata getMetadata(JaxbHbmHibernateMapping hibernateMapping) {
        String xmlStr = JAXBUtil.beanToXml(hibernateMapping);
        this.metadataSources.addInputStream(new ByteArrayInputStream(xmlStr.getBytes()));
        return this.metadataSources.buildMetadata();
    }
}
