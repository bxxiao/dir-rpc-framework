package github.bx.remoting.transport;

import github.bx.remoting.dto.RpcRequest;

/**
 * 发送 Rpc 请求的接口，这样可以提供不同的实现
 */
public interface RpcTransport {

    /**
     * 发送 RPC 请求，返回执行结果
     * @param request
     * @return
     */
    Object sendRpcRequest(RpcRequest request);
}
