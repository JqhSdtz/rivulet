package org.laputa.rivulet.common.hibernate;

import jakarta.persistence.EntityManagerFactory;

import java.lang.reflect.Proxy;

public class ProxyFactory {
    public static EntityManagerFactory createProxy(EntityManagerFactory realFactory) {
        SwitchableEntityManagerFactoryProxy handler = new SwitchableEntityManagerFactoryProxy(realFactory);
        return (EntityManagerFactory) Proxy.newProxyInstance(
                EntityManagerFactory.class.getClassLoader(),
                new Class<?>[]{EntityManagerFactory.class},
                handler
        );
    }
}
