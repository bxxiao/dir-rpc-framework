package github.bx.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.bx.exception.SerializeException;
import github.bx.remoting.dto.RpcRequest;
import github.bx.remoting.dto.RpcResponse;
import github.bx.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 使用 Kryo 实现序列化、反序列化
 */
public class KryoSerializer implements Serializer {

    /*
    * Kryo 不是线程安全的
    * withInitial 静态方法表示每次 get 时都会
    * 使用传入的 Supplier 函数式接口实现返回自定义的对象
    * */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 注册要进行序列化的类，若没有注册，则会导致反序列化抛出异常
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream outArray = new ByteArrayOutputStream();
            Output output = new Output(outArray)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("序列化出现异常");
        } finally {
            kryoThreadLocal.remove();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(inputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            Object obj = kryo.readObject(input, clazz);
            return clazz.cast(obj);
        } catch (Exception e) {
            throw new SerializeException("反序列化出现异常");
        } finally {
            kryoThreadLocal.remove();
        }
    }
}
