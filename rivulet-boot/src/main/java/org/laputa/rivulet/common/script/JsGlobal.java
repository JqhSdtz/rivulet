package org.laputa.rivulet.common.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

/**
 * @author JQH
 * @since 下午 6:33 22/08/27
 */
@Component
public class JsGlobal {
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
