package org.laputa.rivulet.common.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 来自 <a href="https://blog.csdn.net/qq_53316135/article/details/122195566">...</a>
 *
 * @author sqd
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBodyParam {
    /**
     * 解析时用到的 JSON 中的 key
     */
    String value() default "";
}

