package github.bx.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例工厂（简单工厂模式）
 */
public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    public static <T> T getInstance(Class<T> clazz) {
        if (clazz == null)
            throw new IllegalArgumentException();

        String key = clazz.toString();
        Object targetObj;
        if (OBJECT_MAP.containsKey(key))
            targetObj = OBJECT_MAP.get(key);
        else {
            targetObj = OBJECT_MAP.computeIfAbsent(key, k -> {
                try {
                    /*
                    * 注意这里用的是无参构造器来创建对象
                    * */
                    System.out.println();
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
        }

        return clazz.cast(targetObj);
    }
}
