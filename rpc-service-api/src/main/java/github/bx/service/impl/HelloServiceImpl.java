package github.bx.service.impl;

import github.bx.service.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name, Integer age) {
        return "Hello, " + name + "! your age is " + age;
    }
}
