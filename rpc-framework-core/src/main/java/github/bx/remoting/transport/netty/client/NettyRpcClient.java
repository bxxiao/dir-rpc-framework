package github.bx.remoting.transport.netty.client;

import github.bx.exception.RpcException;
import github.bx.factory.SingletonFactory;
import github.bx.remoting.constants.RpcConstants;
import github.bx.remoting.dto.RpcMessage;
import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.dto.RpcResponse;
import github.bx.remoting.registrar.ServiceDiscovery;
import github.bx.remoting.registrar.impl.zk.ZkServiceDiscoveryImpl;
import github.bx.remoting.transport.RpcTransport;
import github.bx.remoting.transport.netty.codec.RpcMessageDecoder;
import github.bx.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcClient implements RpcTransport {

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private ChannelProvider channelProvider;
    private UnprocessedRequest unprocessedRequest;
    private ServiceDiscovery serviceDiscovery;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        /*
                        * 空闲状态处理器，每过 5s 发送心跳包，发送心跳包的逻辑在NettyClientHandler.userEventTriggered
                        * */
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
        channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        serviceDiscovery = SingletonFactory.getInstance(ZkServiceDiscoveryImpl.class);
    }

    @Override
    public Object sendRpcRequest(RpcRequest request) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress serviceAddress = serviceDiscovery.lookupService(request);
        RpcMessage rpcMessage = RpcMessage.builder()
               .messageType(RpcConstants.REQUEST_TYPE)
               .data(request)
               .build();
        Channel serverChannel = getChannel(serviceAddress);

        if (serverChannel.isActive()) {
            serverChannel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    unprocessedRequest.add(request.getRequestId(), resultFuture);
                    log.info("client send rpc request: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("rpc request send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress serviceAddress) {
        Channel channel = channelProvider.getChannel(serviceAddress);
        if (channel == null) {
            channel = doConnect(serviceAddress);
            channelProvider.setChannel(serviceAddress, channel);
        }

        return channel;
    }

    /**
     * @SneakyThrows 注解可以为方法的代码生成一个try...catch块，并把异常向上抛
     */
    @SneakyThrows
    private Channel doConnect(InetSocketAddress serviceAddress) {
        CompletableFuture<Channel> result = new CompletableFuture<>();

        bootstrap.connect(serviceAddress).addListener((ChannelFutureListener)future -> {
            if (future.isSuccess()) {
                log.info("Connect to server [{}]", serviceAddress.toString());
                result.complete(future.channel());
            } else {
                throw new RpcException("connect to server failed, server ip: {}" + serviceAddress.toString());
            }
        });

        return result.get();
    }


}
