package org.laputa.rivulet.module.auth.session;

import org.laputa.rivulet.module.auth.entity.RvUser;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * @author JQH
 * @since 上午 11:15 22/10/23
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppAuth {
    @Resource
    private AuthSessionAccessor authSessionAccessor;
    private RvUser currentUser;

    @PostConstruct
    private void postConstruct() {
        this.currentUser = authSessionAccessor.getCurrentUser();
    }
    public RvUser getCurrentUser() {
        return currentUser;
    }
}
