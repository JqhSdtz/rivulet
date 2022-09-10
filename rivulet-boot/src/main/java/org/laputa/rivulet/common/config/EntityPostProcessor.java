package org.laputa.rivulet.common.config;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 10:04 22/09/05
 */
@Component("PersistenceUnitPostProcessor")
public class EntityPostProcessor implements PersistenceUnitPostProcessor {

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        System.out.println("pui");
        pui.getManagedClassNames().forEach(System.out::println);
    }
}
