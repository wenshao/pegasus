package com.alibaba.pegasus.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.pegasus.config.MySqlProxyConfig;

public class MySqlProxy {

    private final static Log                LOG                 = LogFactory.getLog(MySqlProxy.class);

    private final MySqlProxyConfig          config;

    private AsynchronousChannelGroup        threadGroup;
    private AsynchronousServerSocketChannel server;

    private AccepteHandler                  acceptHandler       = new AccepteHandler();
    private ConnectHandler                  connectHandler      = new ConnectHandler();

    private BackendReadHandler              backendReadHandler  = new BackendReadHandler();
    private BackendWriteHandler             backendWriteHandler = new BackendWriteHandler();

    private FrontReadHandler                frontReadHandler    = new FrontReadHandler();
    private FrontWriteHandler               frontWriteHandler   = new FrontWriteHandler();

    private ExecutorService                 executor            = Executors.newCachedThreadPool();

    public MySqlProxy(MySqlProxyConfig config){
        this.config = config;
    }

    public MySqlProxyConfig getConfig() {
        return this.config;
    }

    public BackendReadHandler getBackendReadHandler() {
        return backendReadHandler;
    }

    public FrontWriteHandler getFrontWriteHandler() {
        return frontWriteHandler;
    }
    
    public BackendWriteHandler getBackendWriteHandler() {
        return backendWriteHandler;
    }
    
    public FrontReadHandler getFrontReadHandler() {
        return frontReadHandler;
    }

    public AsynchronousChannelGroup getThreadGroup() {
        return this.threadGroup;
    }

    public ConnectHandler getConnectHandler() {
        return connectHandler;
    }

    public void start() throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("mysql proxy start ...");
        }

        threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executor, 1);
        server = AsynchronousServerSocketChannel.open(threadGroup);

        List<SocketAddress> addresses = config.getAddresses();
        for (int i = 0; i < addresses.size(); ++i) {
            SocketAddress address = addresses.get(i);
            server.bind(address);
            if (LOG.isInfoEnabled()) {
                LOG.info("mysql proxy listening " + address);
            }
        }

        server.accept(this, acceptHandler);

        if (LOG.isInfoEnabled()) {
            LOG.info("mysql proxy started.");
        }
    }
}
