package github.bx.remoting.transport.codec;

import github.bx.exception.RpcException;
import github.bx.factory.SingletonFactory;
import github.bx.remoting.constants.RpcConstants;
import github.bx.remoting.dto.RpcMessage;
import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.dto.RpcResponse;
import github.bx.serialize.Serializer;
import github.bx.serialize.kryo.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      Maximum frame length. It decide the maximum length of data that can be received.
     *                            If it exceeds, the data will be discarded.
     * @param lengthFieldOffset   Length field offset. The length field is the one that skips the specified length of byte.
     * @param lengthFieldLength   The number of bytes in the length field.
     * @param lengthAdjustment    The compensation value to add to the value of the length field
     * @param initialBytesToStrip Number of bytes skipped.
     *                            If you need to receive all of the header+body data, this value is 0
     *                            if you only want to receive the body data, then you need to skip the number of bytes consumed by the header.
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.HEAD_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        // 检查魔数和版本号
        checkMagicCode(in);
        checkVersion(in);
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType)
                .codec(in.readByte())
                .compress(in.readByte())
                .msgId(in.readInt())
                .build();

        /*
        * 如果是心跳类型数据，直接放入 PING 或 PONG 即可
        * */
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        } else if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        } else if (messageType != RpcConstants.REQUEST_TYPE && messageType != RpcConstants.RESPONSE_TYPE) {
            throw new RpcException("message type error: " + messageType);
        }

        /*
        * 根据fullLength计算出数据的大小，读取出byte数组，进行反序列化
        * */
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] dataBytes = new byte[bodyLength];
            in.readBytes(dataBytes);
            Serializer serializer = SingletonFactory.getInstance(KryoSerializer.class);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest rpcRequest = serializer.deserialize(dataBytes, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            } else {
                RpcResponse rpcResponse = serializer.deserialize(dataBytes, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }
        }

        return rpcMessage;
    }

    private void checkMagicCode(ByteBuf in) {
        byte[] magicCode = new byte[4];
        in.readBytes(magicCode);
        for (int i = 0; i < magicCode.length; i++) {
            if (magicCode[i] != RpcConstants.MAGIC_CODE[i])
                throw new RpcException("magic code error: {" + Arrays.toString(magicCode) + "}");
        }
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcConstants.VERSION)
            throw new RpcException("rpc message version error: " + version + ". current version: " + RpcConstants.VERSION);
    }
}
