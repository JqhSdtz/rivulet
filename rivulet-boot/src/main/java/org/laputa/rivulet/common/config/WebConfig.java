package org.laputa.rivulet.common.config;

import org.laputa.rivulet.common.interceptor.RvInterceptor;
import org.laputa.rivulet.module.data_model.interceptor.DataModelModifyConfirmInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * @author JQH
 * @since 下午 12:31 20/02/14
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private DataModelModifyConfirmInterceptor dataModelModifyConfirmInterceptor;

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registerInterceptor(registry, dataModelModifyConfirmInterceptor);
    }

    private void registerInterceptor(@NotNull InterceptorRegistry registry, RvInterceptor interceptor) {
        registry.addInterceptor(interceptor).addPathPatterns(interceptor.getPathPatterns()).excludePathPatterns(interceptor.getExcludePathPatterns());
    }
}
