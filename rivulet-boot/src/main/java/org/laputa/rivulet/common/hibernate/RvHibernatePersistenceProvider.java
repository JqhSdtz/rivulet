package org.laputa.rivulet.common.hibernate;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.hibernate.bytecode.enhance.spi.EnhancementContext;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.springframework.core.NativeDetector;
import org.springframework.orm.jpa.persistenceunit.SmartPersistenceUnitInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * copy from org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider
 */
public class RvHibernatePersistenceProvider extends HibernatePersistenceProvider {

    @Override
    @SuppressWarnings({"unchecked"})  // on Hibernate 6
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
        final List<String> mergedClassesAndPackages = new ArrayList<>(info.getManagedClassNames());
        if (info instanceof SmartPersistenceUnitInfo smartInfo) {
            mergedClassesAndPackages.addAll(smartInfo.getManagedPackages());
        }
        return new RvEntityManagerFactoryBuilderImpl(
                new PersistenceUnitInfoDescriptor(info) {
                    @Override
                    public List<String> getManagedClassNames() {
                        return mergedClassesAndPackages;
                    }

                    @Override
                    public void pushClassTransformer(EnhancementContext enhancementContext) {
                        if (!NativeDetector.inNativeImage()) {
                            super.pushClassTransformer(enhancementContext);
                        }
                    }
                }, properties).build();
    }

}
