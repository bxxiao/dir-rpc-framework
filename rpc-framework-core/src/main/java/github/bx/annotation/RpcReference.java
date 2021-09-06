package github.bx.annotation;


import java.lang.annotation.*;

/**
 * 被修饰的成员变量会注入一个rpc代理对象
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}
