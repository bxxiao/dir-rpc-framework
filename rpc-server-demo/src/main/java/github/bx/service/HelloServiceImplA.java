package github.bx.service;

import github.bx.annotation.RpcService;

@RpcService(group = "group1", version = "v1")
public class HelloServiceImplA implements HelloService {
    @Override
    public String sayHello(String name, Integer age) {
        return "Hello, " + name + "! " + "this is the response from HelloServiceImplA";
    }

    @Override
    public String methodNoArgs() {
        return "invoke a method which no args";
    }
}
