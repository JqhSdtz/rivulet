package org.laputa.rivulet.common.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.laputa.rivulet.common.util.JpaUtil;
import org.laputa.rivulet.module.auth.session.AuthSessionAccessor;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 6:33 22/08/27
 */
@Getter
@Component
public class JavaNative {
    @Resource
    private EntityManager entityManager;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private HttpServletRequest request;

    @Resource
    private AuthSessionAccessor authSessionAccessor;

    @Resource
    private JpaUtil jpaUtil;

}
