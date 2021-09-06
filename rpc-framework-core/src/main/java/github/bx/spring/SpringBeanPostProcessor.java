package github.bx.spring;


import github.bx.annotation.RpcReference;
import github.bx.annotation.RpcService;
import github.bx.factory.SingletonFactory;
import github.bx.remoting.config.RpcServiceConfig;
import github.bx.remoting.proxy.ClientProxy;
import github.bx.remoting.transport.RpcTransport;
import github.bx.remoting.transport.netty.client.NettyRpcClient;
import github.bx.remoting.transport.netty.server.ServiceProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 对被RpcService和RpcReference修饰的类或成员变量进行对应处理
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ServiceProvider.class);
        this.rpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 注册服务
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig serviceConfig = RpcServiceConfig.builder()
                    .version(annotation.version())
                    .group(annotation.group())
                    .service(bean)
                    .build();
            serviceProvider.publishService(serviceConfig);
            log.info("publish service: {}", serviceConfig.getRpcServiceName());
        }
        return bean;
    }

    /**
     * 创建客户端rpc代理对象
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 注意要用Declared，否则不能获取到私有成员变量对应的Field
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if (annotation != null) {
                // 获取该成员变量对应的Class对象
                Class<?> fieldType = field.getType();
                RpcServiceConfig serviceConfig = RpcServiceConfig.builder()
                        .group(annotation.group())
                        .version(annotation.version())
                        .build();
                ClientProxy proxy = new ClientProxy(serviceConfig, rpcClient);
                Object serviceProxy = proxy.getClientProxy(fieldType);
                // 一般成员变量是 private 的，需要强制设置为可访问
                field.setAccessible(true);
                try {
                    field.set(bean, serviceProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
