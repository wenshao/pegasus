package com.alibaba.sqlwall.net;

import org.jboss.netty.channel.Channel;

public class ProxySession {

    public static final ThreadLocal<ProxySession> currentLocal = new ThreadLocal<ProxySession>();

    private final Channel                         frontChannel;
    private Channel                               backendChannel;

    public ProxySession(Channel frontChannel){
        this.frontChannel = frontChannel;
    }

    public Channel getFrontChannel() {
        return frontChannel;
    }

    public Channel getBackendChannel() {
        return backendChannel;
    }

    public void setBackendContext(Channel backendChannel) {
        this.backendChannel = backendChannel;
    }

    public static void setCurrent(ProxySession current) {
        currentLocal.set(current);
    }

    public static ProxySession current() {
        return currentLocal.get();
    }
}
