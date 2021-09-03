package github.bx.remoting.registrar;

import github.bx.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {

    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
