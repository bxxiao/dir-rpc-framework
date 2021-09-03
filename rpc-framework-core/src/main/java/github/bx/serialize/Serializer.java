package github.bx.serialize;

/**
 * 序列化接口
 * 可以使用不同的序列化工具实现这个接口
 */
public interface Serializer {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
