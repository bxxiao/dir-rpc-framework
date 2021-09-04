package github.bx.remoting.proxy;

import github.bx.enums.RpcResponseCodeEnum;
import github.bx.exception.RpcException;
import github.bx.remoting.config.RpcServiceConfig;
import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.dto.RpcResponse;
import github.bx.remoting.transport.RpcTransport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class ClientProxy implements InvocationHandler {

    private RpcServiceConfig serviceConfig;
    private RpcTransport rpcTransport;

    public ClientProxy(RpcServiceConfig serviceConfig, RpcTransport rpcTransport) {
        this.serviceConfig = serviceConfig;
        this.rpcTransport = rpcTransport;
    }

    public <T> T getClientProxy(Class<T> clazz) {
        /*
        * 注意这里传入的clazz对应的是一个服务接口，而不是一个具体实现类
        * 服务消费端（客户端）通过clazz指定服务接口，通过RpcServiceConfig来指定具体实现类（group和version字段）
        * 因为clazz就是接口对应的Class对象，所以newProxyInstance第二个参数不是使用clazz.getInterfaces()，而是直接clazz本身作为参数
        * */
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据 serviceConfig 和 调用的方法（method），封装对应的 RpcRequest
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .version(serviceConfig.getVersion())
                .group(serviceConfig.getGroup())
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .parameters(args)
                .build();
        /*
         * 这里默认rpcTransport的具体实现是使用netty，若是扩展了其它实现方式
         * 这部分代码需要修改
         * */
        CompletableFuture<RpcResponse<Object>> future = (CompletableFuture<RpcResponse<Object>>) rpcTransport.sendRpcRequest(rpcRequest);
        /*
        * 这里使用的是CompletableFuture，所以仍然是会同步的，不能发挥CompletableFuture的优点
        * */
        RpcResponse<Object> rpcResponse = future.get();
        checkResponse(rpcRequest, rpcResponse);

        return rpcResponse.getData();
    }

    private void checkResponse(RpcRequest rpcRequest, RpcResponse<Object> rpcResponse) {
        if (rpcResponse == null)
            throw new RpcException("服务调用失败" + " interfaceName: " + rpcRequest.getInterfaceName());

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId()))
            throw new RpcException("返回结果和rpc请求不匹配" + " interfaceName: " + rpcRequest.getInterfaceName());

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode()))
            throw new RpcException("服务调用失败" + " interfaceName: " + rpcRequest.getInterfaceName());
    }
}
