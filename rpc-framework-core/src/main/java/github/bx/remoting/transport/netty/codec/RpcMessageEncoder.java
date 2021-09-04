package github.bx.remoting.transport.netty.codec;

import github.bx.exception.RpcException;
import github.bx.factory.SingletonFactory;
import github.bx.remoting.constants.RpcConstants;
import github.bx.remoting.dto.RpcMessage;
import github.bx.serialize.Serializer;
import github.bx.serialize.kryo.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {

        try {
            out.writeBytes(RpcConstants.MAGIC_CODE);
            out.writeByte(RpcConstants.VERSION);
            // 将 写索引 向前移动4个字节，带后面计算后放入 full_length
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = msg.getMessageType();
            out.writeByte(messageType);
            /*
            * 写入codec（编码类型）和compress（解压缩类型）
            * TODO：编码类型和加压缩功能
            * */
            out.writeByte(1);
            out.writeByte(1);
            // 写入 msgId （4个字节，对应int类型）
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // fullLength是指整个报文的长度
            int fullLength = RpcConstants.HEAD_LENGTH;
            byte[] bytes = null;
            /*
            * 消息类型是 rpc 请求或响应中的一种，进行序列化
            * 如果不是这 2 钟之一，且不是心跳机制类型，则抛出异常
            * */
            if (messageType == RpcConstants.REQUEST_TYPE || messageType == RpcConstants.RESPONSE_TYPE) {
                Object data = msg.getData();
                Serializer serializer = SingletonFactory.getInstance(KryoSerializer.class);
                bytes = serializer.serialize(data);
                fullLength += bytes.length;
            } else if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                throw new RpcException("no such message type: " + messageType);
            }

            if (bytes != null)
                out.writeBytes(bytes);

            int lastIndex = out.writerIndex();
            out.writerIndex(lastIndex - fullLength + RpcConstants.MAGIC_CODE.length + 1);
            out.writeInt(fullLength);
            // 把写索引又移动到最后了
            out.writerIndex(lastIndex);
        } catch (Exception e) {
            log.info("encode error:[{}]", e);
        }
    }
}
