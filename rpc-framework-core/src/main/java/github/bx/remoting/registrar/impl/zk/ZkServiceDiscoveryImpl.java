package github.bx.remoting.registrar.impl.zk;

import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.registrar.ServiceDiscovery;

import java.net.InetSocketAddress;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        return new InetSocketAddress("192.168.56.1", 6677);
    }
}
