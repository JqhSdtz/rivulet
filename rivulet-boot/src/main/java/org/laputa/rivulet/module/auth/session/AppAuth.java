package org.laputa.rivulet.module.auth.session;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

/**
 * @author JQH
 * @since 上午 11:15 22/10/23
 */
@Component
@Scope(value = SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppAuth {
    @Resource
    private AuthSessionAccessor authSessionAccessor;
    @Getter
    private RvAdmin currentAdmin;

    @PostConstruct
    private void postConstruct() {
        this.currentAdmin = authSessionAccessor.getCurrentAdmin();
    }
}
