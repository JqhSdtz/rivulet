package org.laputa.rivulet.common.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 6:33 22/08/27
 */
@Component
public class JavaNative {
    @Resource
    private EntityManager entityManager;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private HttpServletRequest request;

    public HttpServletRequest getRequest() {
        return request;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
