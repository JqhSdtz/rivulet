package liquibase.ext.hibernate.annotation;

import java.lang.annotation.*;

/**
 * !!!为各种模型属性的title字段赋值
 * @author JQH
 * @since 上午 09:11 25/02/22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Title {
    String value() default "";
}

