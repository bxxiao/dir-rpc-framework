package github.bx.factory;

import github.bx.remoting.transport.netty.client.ChannelProvider;
import github.bx.remoting.transport.netty.client.UnprocessedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FactoryTest {

    @Test
    public void testSingletonFactory(){
        UnprocessedRequest unprocessedRequest1 = SingletonFactory.getInstance(UnprocessedRequest.class);
        ChannelProvider provider1 = SingletonFactory.getInstance(ChannelProvider.class);
        UnprocessedRequest unprocessedRequest2 = SingletonFactory.getInstance(UnprocessedRequest.class);
        ChannelProvider provider2 = SingletonFactory.getInstance(ChannelProvider.class);
        log.info("provider1==provider2? [{}]", provider1 == provider2);
        log.info("unprocessedRequest1==unprocessedRequest2? [{}]", unprocessedRequest1 == unprocessedRequest2);
    }
}
