package github.bx.remoting.transport.netty.server;


import github.bx.exception.RpcException;
import github.bx.factory.SingletonFactory;
import github.bx.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class RpcRequestProcessor {
    private ServiceProvider serviceProvider;

    public RpcRequestProcessor() {
        serviceProvider = SingletonFactory.getInstance(ServiceProvider.class);
    }

    /**
     * 处理远程调用
     */
    public Object processRpcRequest(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        Object serviceObj = serviceProvider.getServiceObj(rpcServiceName);
        /*
        * 这里异常应该在 getServiceObj 进行抛出处理，体现面向对象
        * */
        // if (serviceObj == null)
        //     throw new RpcException("rpc request process failed: no such service [" + rpcServiceName + "]");

        Object result = invokeService(serviceObj, rpcRequest);
        return result;
    }

    /*
    * TODO：若方法无返回值，咋搞
    * */
    private Object invokeService(Object serviceObj, RpcRequest rpcRequest) {
        String methodName = rpcRequest.getMethodName();
        Class<?>[] paramTypes = rpcRequest.getParamTypes();
        Object[] parameters = rpcRequest.getParameters();
        Object invokeResult = null;
        try {
            Method method = serviceObj.getClass().getMethod(methodName, paramTypes);
            invokeResult = method.invoke(serviceObj, parameters);
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw  new RpcException(e.getMessage());
        }
        return invokeResult;
    }
}
