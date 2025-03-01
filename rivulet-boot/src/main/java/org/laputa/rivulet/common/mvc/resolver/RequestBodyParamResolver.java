package org.laputa.rivulet.common.mvc.resolver;

import cn.hutool.core.io.IoUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.laputa.rivulet.common.mvc.annotation.RequestBodyParam;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 来自 https://blog.csdn.net/qq_53316135/article/details/122195566
 * MultiRequestBody 解析器
 * 1、支持通过注解的 value 指定 JSON 的 key 来解析对象
 * 2、支持通过注解无 value，直接根据参数名来解析对象
 * 3、支持基本类型的注入
 * 4、支持通过注解无 value 且参数名不匹配 JSON 串的 key 时，根据属性解析对象
 *
 * @author sqd
 */
public class RequestBodyParamResolver implements HandlerMethodArgumentResolver {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String JSON_REQUEST_BODY = "JSON_REQUEST_BODY";

    public RequestBodyParamResolver() {
        // 允许使用不带引号的字段
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 忽略未定义的字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 支持的方法参数类型
     *
     * @see RequestBodyParam
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBodyParam.class);
    }

    /**
     * 参数解析
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String requestBody = getRequestBody(webRequest);
        RequestBodyParam requestBodyParam = parameter.getParameterAnnotation(RequestBodyParam.class);
        JsonNode rootNode = objectMapper.readTree(requestBody);
        if (rootNode == null || requestBodyParam == null) {
            return null;
        }
        String key = requestBodyParam.value();
        // 根据注解 value 解析 JSON 串，如果没有根据参数的名字解析 JSON
        if (StringUtils.isBlank(key)) {
            key = parameter.getParameterName();
        }
        Object value = rootNode.get(key);
        Class<?> paramType = parameter.getParameterType();
        if (value == null) {
            return null;
        }
        String valueStr = value.toString();
        if ("{}".equals(valueStr)) {
            return null;
        }
        try {
            return objectMapper.readValue(valueStr, paramType);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * 获取请求 JSON 字符串
     */
    private String getRequestBody(NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        String jsonBody = (String) webRequest.getAttribute(JSON_REQUEST_BODY, NativeWebRequest.SCOPE_REQUEST);
        if (StringUtils.isEmpty(jsonBody)) {
            try (BufferedReader reader = servletRequest.getReader()) {
                jsonBody = IoUtil.read(reader);
                webRequest.setAttribute(JSON_REQUEST_BODY, jsonBody, NativeWebRequest.SCOPE_REQUEST);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonBody;
    }

}
