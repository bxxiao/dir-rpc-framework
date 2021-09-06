package github.bx.annotation;


import github.bx.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
/*
* 这里使用@Import导入了自定义注册类
* 当被@RpcScan修饰的类作为配置类时，CustomScannerRegistrar中的registerBeanDefinitions就会被执行
* */
@Import(CustomScannerRegistrar.class)
public @interface RpcScan {
    String[] basePackage();
}
