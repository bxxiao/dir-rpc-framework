package github.bx.client;

import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.dto.RpcResponse;
import github.bx.remoting.transport.netty.client.NettyRpcClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ClientMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
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
}
