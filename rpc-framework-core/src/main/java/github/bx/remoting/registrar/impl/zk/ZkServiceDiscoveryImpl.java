package github.bx.remoting.registrar.impl.zk;

import github.bx.exception.RpcException;
import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.registrar.ServiceDiscovery;
import github.bx.remoting.registrar.impl.zk.util.CuratorUtils;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getRpcServiceName();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(serviceName);
        if (serviceUrlList == null || serviceUrlList.size() == 0)
            throw new RpcException("no such service: " + serviceName);
        /*
        * TODO：添加负载均衡功能，这里默认获取第一个
        * */
        String[] addressStr = serviceUrlList.get(0).split(":");
        String host = addressStr[0];
        int port = Integer.parseInt(addressStr[1]);
        return new InetSocketAddress(host, port);
    }
}
