package org.laputa.rivulet.common.mvc.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JQH
 * @since 下午 6:38 22/10/23
 */
public abstract class RvInterceptor implements HandlerInterceptor {
    public List<String> getExcludePathPatterns() {
        return new ArrayList<>();
    }
    public List<String> getPathPatterns() {
        return new ArrayList<>();
    }
}
