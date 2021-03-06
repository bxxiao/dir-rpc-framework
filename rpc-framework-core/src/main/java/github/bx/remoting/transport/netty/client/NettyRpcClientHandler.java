package github.bx.remoting.transport.netty.client;

import github.bx.factory.SingletonFactory;
import github.bx.remoting.constants.RpcConstants;
import github.bx.remoting.dto.RpcMessage;
import github.bx.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private UnprocessedRequest unprocessedRequest;
    private NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage rpcMessage = (RpcMessage) msg;
                if (rpcMessage.getMessageType() == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.debug("heart response：{}", rpcMessage.getData());
                }
                if (rpcMessage.getMessageType() == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse response = (RpcResponse) rpcMessage.getData();
                    if (unprocessedRequest.complete(response))
                        log.info("receive rpc response, request id: {}", response.getRequestId());
                    else
                        log.error("no request for this response, request id: {}", response.getRequestId());
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 心跳机制相关
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 发送心跳包（PING）
            if (state == IdleState.WRITER_IDLE) {
                log.debug("send heart request [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                // 不用设置data，设置心跳类型即可
                RpcMessage rpcMessage = RpcMessage.builder()
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .codec((byte) 1)
                        .compress((byte) 1)
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else
            super.userEventTriggered(ctx, evt);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
