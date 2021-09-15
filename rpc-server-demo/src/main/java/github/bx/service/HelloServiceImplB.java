package github.bx.service;

import github.bx.annotation.RpcService;

@RpcService(group = "group2", version = "v2")
public class HelloServiceImplB implements HelloService {
    @Override
    public String sayHello(String name, Integer age) {
        return "Hello, " + name + "! " + "this is the response from HelloServiceImplB";
    }

    @Override
    public String methodNoArgs() {
        return null;
    }
}
