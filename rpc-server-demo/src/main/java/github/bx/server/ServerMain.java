package github.bx.server;

import github.bx.remoting.config.RpcServiceConfig;
import github.bx.remoting.transport.netty.server.NettyRpcServer;
import github.bx.service.HelloService;
import github.bx.service.impl.HelloServiceImpl;

public class ServerMain {
    public static void main(String[] args) {
        NettyRpcServer server = new NettyRpcServer();
        HelloService helloService = new HelloServiceImpl();
        RpcServiceConfig serviceConfig = RpcServiceConfig.builder()
                .service(helloService)
                .group("group1")
                .version("v1")
                .build();
        server.registryService(serviceConfig);
        server.start();
    }
}
