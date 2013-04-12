package com.alibaba.sqlwall.net;

import org.jboss.netty.channel.Channel;

public class ProxySession {

    public static final ThreadLocal<ProxySession> currentLocal     = new ThreadLocal<ProxySession>();

    private final Channel                         frontChannel;
    private Channel                               backendChannel;
    private String                                charset;
    private String                                user;

    public static final int                       PHASE_AUTH       = 0;
    public static final int                       PHASE_AUTH_ERROR = 100;
    public static final int                       PHASE_COMMAND    = 1001;

    private volatile int                          phase            = PHASE_AUTH;

    public ProxySession(Channel frontChannel){
        this.frontChannel = frontChannel;
    }

    public Channel getFrontChannel() {
        return frontChannel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
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
