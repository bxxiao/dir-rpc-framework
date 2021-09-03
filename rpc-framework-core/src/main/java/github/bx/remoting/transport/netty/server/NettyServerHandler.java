package github.bx.remoting.transport.netty.server;

import github.bx.enums.RpcResponseCodeEnum;
import github.bx.factory.SingletonFactory;
import github.bx.remoting.constants.RpcConstants;
import github.bx.remoting.dto.RpcMessage;
import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private RpcRequestProcessor processor;

    public NettyServerHandler() {
        processor = SingletonFactory.getInstance(RpcRequestProcessor.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage responseMsg = new RpcMessage();
                Channel channel = ctx.channel();

                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    responseMsg.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    responseMsg.setData(RpcConstants.PONG);
                } else if (messageType == RpcConstants.REQUEST_TYPE) {
                    RpcResponse rpcResponse;

                    if (channel.isActive() && channel.isWritable()) {
                        RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                        Object processResult = processor.processRpcRequest(rpcRequest);
                        rpcResponse = RpcResponse.success(processResult, rpcRequest.getRequestId());
                    } else {
                        rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                    }
                    responseMsg.setMessageType(RpcConstants.RESPONSE_TYPE);
                    responseMsg.setData(rpcResponse);
                } else {
                    return;
                }

                channel.writeAndFlush(responseMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
