package org.laputa.rivulet.common.mvc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author JQH
 * @since 下午 8:59 22/08/20
 */
@Configuration
public class SerializerConfig {
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = converter.getObjectMapper();
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        // hibernate5Module可以避免jackson的序列化操作触发hibernate的懒加载
        mapper.registerModule(hibernate6Module);
        return converter;
    }
}
