package github.bx.remoting.registrar;

import java.net.InetSocketAddress;

public interface ServiceRegistrar {

    void registerService(String serviceName, InetSocketAddress address);
}
