package github.bx.remoting.transport.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ChannelProvider {
    private Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new HashMap<>();
    }

    public Channel getChannel(InetSocketAddress address) {
        String addrStr = address.toString();
        return channelMap.get(addrStr);
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
