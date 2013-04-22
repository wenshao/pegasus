package com.alibaba.pegasus.net;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class Session {

    private final MySqlProxy                proxy;
    private final AsynchronousSocketChannel frontChannel;
    private final AsynchronousSocketChannel backendChannel;

    private ByteBuffer                      backendBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer                      frontBuffer   = ByteBuffer.allocate(1024);

    public Session(MySqlProxy proxy, AsynchronousSocketChannel frontChannel, AsynchronousSocketChannel backendChannel){
        this.proxy = proxy;
        this.frontChannel = frontChannel;
        this.backendChannel = backendChannel;
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
