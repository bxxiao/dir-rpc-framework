package github.bx.remoting.constants;

public class RpcConstants {
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
    public static final byte[] MAGIC_CODE = new byte[]{(byte)6, (byte)6, (byte)6, (byte)6};
    public static final byte VERSION = (byte)1;
    public static final int HEAD_LENGTH = 16;

    // 消息类型
    public static final byte REQUEST_TYPE = (byte)1;
    public static final byte RESPONSE_TYPE = (byte)2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = (byte)3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = (byte)4;

    /*
    * 心跳机制的协议内容
    * */
    public static final String PING = "ping";
    public static final String PONG = "pong";
}
