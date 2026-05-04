package org.laputa.rivulet.common.hibernate;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
public class EntityManagerFactoryProxyPostProcessor implements BeanPostProcessor {

    // 保存 handler 以便后续调用 swap
    private volatile SwitchableEntityManagerFactoryProxy handler;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof EntityManagerFactory original && !Proxy.isProxyClass(bean.getClass())) {
            SwitchableEntityManagerFactoryProxy proxyHandler = new SwitchableEntityManagerFactoryProxy(original);
            EntityManagerFactory proxy = (EntityManagerFactory) Proxy.newProxyInstance(
                    EntityManagerFactory.class.getClassLoader(),
                    new Class<?>[]{EntityManagerFactory.class},
                    proxyHandler
            );
            this.handler = proxyHandler;
            return proxy;  // 用代理替换原始工厂
        }
        return bean;
    }

    // 供外部调用的切换方法
    public void replaceEntityManagerFactory(EntityManagerFactory newFactory) {
        if (handler != null) {
            handler.swap(newFactory);
        }
    }
}