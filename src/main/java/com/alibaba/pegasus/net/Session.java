package com.alibaba.pegasus.net;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import static com.alibaba.pegasus.ProxySessionStat.*;

public class Session {

    private final MySqlProxy                proxy;
    private final AsynchronousSocketChannel frontChannel;
    private final AsynchronousSocketChannel backendChannel;

    private ByteBuffer                      backendBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer                      frontBuffer   = ByteBuffer.allocate(1024);

    private int                             state         = STAT_INIT;
    private String                          charset;
    private String                          user;

    public Session(MySqlProxy proxy, AsynchronousSocketChannel frontChannel, AsynchronousSocketChannel backendChannel){
        this.proxy = proxy;
        this.frontChannel = frontChannel;
        this.backendChannel = backendChannel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ByteBuffer getBackendBuffer() {
        return backendBuffer;
    }

    public ByteBuffer getFrontBuffer() {
        return frontBuffer;
    }

    public void setReadBuffer(ByteBuffer readBuffer) {
        this.backendBuffer = readBuffer;
    }

    public AsynchronousSocketChannel getFrontChannel() {
        return frontChannel;
    }

    public AsynchronousSocketChannel getBackendChannel() {
        return backendChannel;
    }

    public MySqlProxy getProxy() {
        return proxy;
    }

}
