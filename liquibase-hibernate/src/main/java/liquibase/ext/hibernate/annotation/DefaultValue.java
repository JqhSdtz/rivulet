package liquibase.ext.hibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * !!! 该注解为新增，用于在字段中设置默认值
 * @author JQH
 * @since 上午 10:15 22/10/21
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
public @interface DefaultValue {
    String value();
}
