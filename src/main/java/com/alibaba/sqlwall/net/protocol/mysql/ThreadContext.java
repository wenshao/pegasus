package com.alibaba.sqlwall.net.protocol.mysql;

import org.jboss.netty.channel.Channel;

public class ThreadContext {

    public static final ThreadLocal<Channel> frontChannelLocal = new ThreadLocal<Channel>();

    public static void setFrontChannel(Channel channel) {
        frontChannelLocal.set(channel);
    }
    
    public static Channel getFrontChannel() {
        return frontChannelLocal.get();
    }
}
