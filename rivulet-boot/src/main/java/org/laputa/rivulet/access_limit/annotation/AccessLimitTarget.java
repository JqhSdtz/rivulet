package org.laputa.rivulet.access_limit.annotation;

import java.lang.annotation.*;

/**
 * @author JQH
 * @since 下午 8:37 21/03/01
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AccessLimitTarget {
    String byMethod() default "";
}
