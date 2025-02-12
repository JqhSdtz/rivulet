package org.laputa.rivulet.common.mvc.config;

import org.laputa.rivulet.common.mvc.interceptor.RvInterceptor;
import org.laputa.rivulet.common.mvc.resolver.RequestBodyParamResolver;
import org.laputa.rivulet.module.data_model.interceptor.DataModelModifyConfirmInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author JQH
 * @since 下午 12:31 20/02/14
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private DataModelModifyConfirmInterceptor dataModelModifyConfirmInterceptor;

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registerInterceptor(registry, dataModelModifyConfirmInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestBodyParamResolver());
    }

    private void registerInterceptor(@NotNull InterceptorRegistry registry, RvInterceptor interceptor) {
        registry.addInterceptor(interceptor).addPathPatterns(interceptor.getPathPatterns()).excludePathPatterns(interceptor.getExcludePathPatterns());
    }
}
