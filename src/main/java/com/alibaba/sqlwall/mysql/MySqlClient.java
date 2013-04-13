package com.alibaba.sqlwall.mysql;

import java.net.SocketAddress;
import java.nio.ByteOrder;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class MySqlClient {

    private ClientBootstrap client;

    public MySqlClient(){
        client = new ClientBootstrap();
        client.setOption("bufferFactory", new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
        client.setPipelineFactory(new BackendPipelineFactory());
        client.setFactory(new NioClientSocketChannelFactory());
    }

    public ChannelFuture connect(SocketAddress remoteAddress) {
        return client.connect(remoteAddress);
    }
}
