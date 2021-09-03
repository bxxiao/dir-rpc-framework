package github.bx.serialize;

import github.bx.remoting.dto.RpcRequest;
import github.bx.serialize.Serializer;
import github.bx.serialize.kryo.KryoSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class KryoTest {
    @Test
    public void testKryo(){
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId("233")
                .interfaceName("UserService")
                .methodName("getUserId")
                .build();
        log.info("创建了对象：{}", rpcRequest);
        Serializer serializer = new KryoSerializer();
        byte[] bytes = serializer.serialize(rpcRequest);
        RpcRequest rpcRequest1 = serializer.deserialize(bytes, RpcRequest.class);
        log.info("反序列化后的对象：{}", rpcRequest1);
    }
}
