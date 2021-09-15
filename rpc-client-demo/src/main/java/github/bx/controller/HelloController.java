package github.bx.controller;

import github.bx.annotation.RpcReference;
import github.bx.service.HelloService;
import org.springframework.stereotype.Component;

@Component
public class HelloController {

    @RpcReference(group = "group1", version = "v1")
    private HelloService service1;

    @RpcReference(group = "group2", version = "v2")
    private HelloService service2;

    public void testHello1() {
        System.out.println(service1.sayHello("张三", 20));
    }

    public void testHello2() {
        System.out.println(service2.sayHello("李四", 20));
    }

    public void testHello3() {
        System.out.println(service1.methodNoArgs());
    }
}
