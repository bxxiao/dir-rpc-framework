package github.bx.remoting.registrar.impl.zk;

import github.bx.remoting.registrar.ServiceRegistrar;
import github.bx.remoting.registrar.impl.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

public class ZkServiceRegistrarImpl implements ServiceRegistrar {
    @Override
    public void registerService(String serviceName, InetSocketAddress address) {
        String path = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + serviceName + address.toString();
        CuratorUtils.createPersistentNode(path);
    }
}
