package github.bx.remoting.transport.netty.server;

import github.bx.exception.RpcException;
import github.bx.factory.SingletonFactory;
import github.bx.remoting.config.RpcServiceConfig;
import github.bx.remoting.registrar.ServiceRegistrar;
import github.bx.remoting.registrar.impl.zk.ZkServiceRegistrarImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProvider {
    private Map<String, Object> serviceMap;
    private ServiceRegistrar registrar;

    public ServiceProvider() {
        serviceMap = new ConcurrentHashMap<>();
        registrar = SingletonFactory.getInstance(ZkServiceRegistrarImpl.class);
    }

    public void addService(RpcServiceConfig config) {
        String serviceName = config.getRpcServiceName();
        if (!serviceMap.containsKey(serviceName))
            serviceMap.put(serviceName, config.getService());
    }

    public Object getServiceObj(String serviceName) {
        Object serviceObj = serviceMap.get(serviceName);
        if (serviceObj == null)
            throw new RpcException("rpc request process failed: no such service [" + serviceName + "]");

        return serviceObj;
    }

    public void publishService(RpcServiceConfig config) {
        String rpcServiceName = config.getRpcServiceName();
        if (serviceMap.containsKey(rpcServiceName))
            return;

        this.addService(config);
        try {
            // String host = InetAddress.getLocalHost().getHostAddress();
            String host = "127.0.0.1";
            registrar.registerService(rpcServiceName, new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (Exception e) {
            log.info("get local host failed");
        }
    }

}
