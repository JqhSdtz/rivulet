package org.laputa.rivulet.common.hibernate;

import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmBasicAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmHibernateMapping;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmRootEntityType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;

public class RvAdditionalMappingContributor implements AdditionalMappingContributor {

    @Override
    public String getContributorName() {
        return getClass().getSimpleName();
    }

    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector metadata, ResourceStreamLocator resourceStreamLocator, MetadataBuildingContext buildingContext) {
        JaxbHbmHibernateMapping mapping = new JaxbHbmHibernateMapping();
        JaxbHbmRootEntityType bookEntity = new JaxbHbmRootEntityType();
        bookEntity.setEntityName("Book");
        JaxbHbmSimpleIdType bookId = new JaxbHbmSimpleIdType();
        bookId.setName("isbn");
        bookId.setTypeAttribute("string");
        bookId.setLength(32);
        bookId.setColumnAttribute("isbn");
        bookEntity.setId(bookId);
        JaxbHbmBasicAttributeType titleAttribute = new JaxbHbmBasicAttributeType();
        titleAttribute.setName("title");
        titleAttribute.setTypeAttribute("string");
        titleAttribute.setLength(50);
        bookEntity.getAttributes().add(titleAttribute);
        JaxbHbmBasicAttributeType authorAttribute = new JaxbHbmBasicAttributeType();
        authorAttribute.setName("author");
        authorAttribute.setTypeAttribute("string");
        authorAttribute.setLength(50);
        bookEntity.getAttributes().add(authorAttribute);
        mapping.getClazz().add(bookEntity);
        // orm.xml模型不支持dynamic-model，所以仍然使用hbm.xml模型
        contributions.contributeBinding(mapping);
    }
}
