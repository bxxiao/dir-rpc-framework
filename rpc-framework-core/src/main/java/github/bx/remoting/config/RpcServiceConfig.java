package github.bx.remoting.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcServiceConfig {

    private String version = "";

    private String group = "";

    private Object service;

    public String getRpcServiceName() {
        return getServiceInterfaceName() + group + version;
    }

    public String getServiceInterfaceName() {
        /*
        * getInterfaces() 获取对象实现的所有接口的 Class 数组
        * getCanonicalName() 获取的是指定 Class 对象对应类/接口的全限定名
        * */
        return service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
