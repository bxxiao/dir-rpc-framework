package github.bx.server;

import github.bx.annotation.RpcScan;
import github.bx.remoting.config.RpcServiceConfig;
import github.bx.remoting.transport.netty.server.NettyRpcServer;
import github.bx.service.HelloService;
import github.bx.service.HelloServiceImplA;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = "github.bx.service")
public class ServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServerMain.class);
        NettyRpcServer nettyRpcServer = context.getBean("nettyRpcServer", NettyRpcServer.class);
        nettyRpcServer.start();
    }

    public static void test1() {
        NettyRpcServer server = new NettyRpcServer();
        HelloService helloService = new HelloServiceImplA();
        RpcServiceConfig serviceConfig = RpcServiceConfig.builder()
                .service(helloService)
                .group("group1")
                .version("v1")
                .build();
        server.registryService(serviceConfig);
        server.start();
    }
}
