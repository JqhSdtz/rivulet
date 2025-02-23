package liquibase.ext.hibernate.annotation;

import java.lang.annotation.*;

/**
 * !!!该注解用于给表增加注释
 * @author JQH
 * @since 下午 8:01 22/10/27
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableComment {
    String value();
}
