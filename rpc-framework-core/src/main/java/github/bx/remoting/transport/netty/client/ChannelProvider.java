package github.bx.remoting.transport.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelProvider {
    private Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel getChannel(InetSocketAddress address) {
        String addrStr = address.toString();

        if (channelMap.containsKey(addrStr)) {
            Channel channel = channelMap.get(addrStr);
            // 如果channel可用，则返回，否则移除，返回null
            if (channel != null && channel.isActive())
                return channel;
            else
                channelMap.remove(addrStr);
        }

        return null;
    }

    public void setChannel(InetSocketAddress inetSocketAddress, Channel channel) {
        String addrStr = inetSocketAddress.toString();
        channelMap.put(addrStr, channel);
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String addr = inetSocketAddress.toString();
        channelMap.remove(addr);
    }
}
