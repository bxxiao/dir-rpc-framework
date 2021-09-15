package github.bx.client;

import github.bx.annotation.RpcScan;
import github.bx.controller.HelloController;
import github.bx.remoting.config.RpcServiceConfig;
import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.dto.RpcResponse;
import github.bx.remoting.proxy.ClientProxy;
import github.bx.remoting.transport.netty.client.NettyRpcClient;
import github.bx.service.HelloService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RpcScan(basePackage = "github.bx.controller")
public class ClientMain {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ClientMain.class);
        HelloController controller = context.getBean("helloController", HelloController.class);
        controller.testHello1();
        controller.testHello2();
        controller.testHello3();
    }

    /*
    * 使用原生的sendRpcRequest发送rpc请求
    * */
    public static void test1() throws ExecutionException, InterruptedException {
        NettyRpcClient client = new NettyRpcClient();
        Object[] params = new Object[]{"靓仔", 20};
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName("sayHello")
                .interfaceName("github.bx.service.HelloService")
                .parameters(params)
                .paramTypes(new Class[]{String.class, Integer.class})
                .group("group1")
                .version("v1")
                .requestId("6667")
                .build();
        CompletableFuture<RpcResponse<Object>> result = (CompletableFuture<RpcResponse<Object>>) client.sendRpcRequest(rpcRequest);
        RpcResponse<Object> rpcResponse = result.get();
        System.out.println(rpcResponse.getData());
    }

    /*
    * 通过代理对象发送rpc请求
    * */
    public static void test2() {
        NettyRpcClient client = new NettyRpcClient();
        RpcServiceConfig config = RpcServiceConfig.builder()
                .group("group1")
                .version("v1")
                .build();
        ClientProxy serviceProxy = new ClientProxy(config, client);
        HelloService helloService = serviceProxy.getClientProxy(HelloService.class);
        String result = helloService.sayHello("张三", 100);
        System.out.println(result);
    }
}
