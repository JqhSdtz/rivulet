package org.laputa.rivulet.access_limit.annotation;

/**
 * @author JQH
 * @since 下午 12:19 22/10/23
 */
public @interface AccessLimitLevel {
    enum Level {Local, Redis}
    Level value();
}
