package org.laputa.rivulet.access_limit.annotation;

import java.lang.annotation.*;

/**
 * @author JQH
 * @since 下午 8:30 21/03/01
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimits {
    AccessLimit[] value();
}
