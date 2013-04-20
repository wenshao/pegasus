package com.alibaba.pegasus;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.alibaba.druid.wall.WallProvider;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import com.alibaba.pegasus.config.ProxyConfig;
import com.alibaba.pegasus.mysql.BackendPipelineFactory;
import com.alibaba.pegasus.mysql.FrontDecoder;
import com.alibaba.pegasus.mysql.FrontHanlder;

public class DbProxy {

    private final static Log     LOG           = LogFactory.getLog(DbProxy.class);

    private final ProxyConfig    config;

    private final PegasusServer  server;

    private ClientBootstrap      client;

    private ServerBootstrap      bootstrap;

    private FrontDecoder         frontDecoder;

    private MySqlWallProvider    wallProvider  = new MySqlWallProvider();

    private ChannelBufferFactory bufferFactory = new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN);

    public DbProxy(PegasusServer server, ProxyConfig config){
        this.server = server;
        this.config = config;
    }

    public void start() {
        frontDecoder = new FrontDecoder(this);

        bootstrap = new ServerBootstrap(server.getChannelFactory());
        bootstrap.setOption("child.bufferFactory", bufferFactory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(frontDecoder, new FrontHanlder(DbProxy.this));
            }
        });

        for (SocketAddress address : this.config.getAddresses()) {
            bootstrap.bind(address);
            if (LOG.isInfoEnabled()) {
                LOG.info("mysql proxy listening " + address);
            }
        }

        client = new ClientBootstrap();
        client.setOption("bufferFactory", bufferFactory);
        client.setPipelineFactory(new BackendPipelineFactory(this));
        client.setFactory(new NioClientSocketChannelFactory());
    }

    public void stop() {

    }

    public ClientBootstrap getBackendBootstrap() {
        return client;
    }

    public void connectRemote() {
        InetSocketAddress dbAddress = new InetSocketAddress(this.config.getDb().getHost(), config.getDb().getPort());
        if (LOG.isDebugEnabled()) {
            LOG.debug("connect to " + dbAddress);
        }

        client.connect(dbAddress);
    }

    public ProxyConfig getConfig() {
        return config;
    }

    public PegasusServer getServer() {
        return server;
    }

    public WallProvider getWallProvider() {
        return wallProvider;
    }

    public long getReceivedBytes() {
        return this.frontDecoder.getRecevedBytes();
    }

    public long getReceivedMessageCount() {
        return this.frontDecoder.getReceivedMessageCount();
    }

    public void resetStat() {
        this.frontDecoder.resetStat();
    }
}
