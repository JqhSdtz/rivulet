package org.laputa.rivulet.access_limit.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author JQH
 * @since 下午 12:19 22/10/23
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimitLevel {
    enum Level {Local, Redis}
    Level value();
}
