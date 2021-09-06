package github.bx.remoting.transport.netty.client;

import github.bx.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequest {
    private Map<String, CompletableFuture<RpcResponse<Object>>> unprocessedMap;

    public UnprocessedRequest() {
        unprocessedMap = new ConcurrentHashMap<>();
    }

    public void add(String requestId, CompletableFuture future) {
        this.unprocessedMap.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedMap.remove(requestId);
    }

    public boolean complete(RpcResponse rpcResponse) {
        String requestId = rpcResponse.getRequestId();
        CompletableFuture<RpcResponse<Object>> future = unprocessedMap.remove(requestId);
        if (future == null)
            return false;

        future.complete(rpcResponse);
        return true;
    }
}
