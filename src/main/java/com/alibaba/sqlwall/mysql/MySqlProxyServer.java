package com.alibaba.sqlwall.mysql;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.wall.WallProvider;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import com.alibaba.sqlwall.listener.ExecuteBeforeListener;
import com.alibaba.sqlwall.stat.ProxyServerStat;

public class MySqlProxyServer {

    static Log                            LOG                    = LogFactory.getLog(MySqlProxyServer.class);

    private ServerBootstrap               bootstrap;
    private ThreadPoolExecutor            bossExecutor;
    private ThreadPoolExecutor            workerExecutor;

    private int                           workerThreadCount      = Runtime.getRuntime().availableProcessors();

    private NioServerSocketChannelFactory channelFactory;

    private FrontDecoder                  decoder;

    private ClientBootstrap               client;

    private String                        remoteHost;
    private int                           remotePort;

    private int                           listenPort             = 3306;

    private WallProvider                  wallProvider;

    private ProxyServerStat               serverStat             = new ProxyServerStat(JdbcConstants.MYSQL);

    private List<ExecuteBeforeListener>   executeBeforeListeners = new CopyOnWriteArrayList<ExecuteBeforeListener>();

    private ChannelBufferFactory          bufferFactory          = new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN);

    public MySqlProxyServer(String remoteHost, int remotePort){
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;

        wallProvider = new MySqlWallProvider();

    }

    public ProxyServerStat getProxyStat() {
        return this.serverStat;
    }

    public WallProvider getWallProvider() {
        return wallProvider;
    }

    public List<ExecuteBeforeListener> getExecuteBeforeListeners() {
        return executeBeforeListeners;
    }

    public void start() {
        decoder = new FrontDecoder(this);

        bossExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                              new SynchronousQueue<Runnable>());
        workerExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                                new SynchronousQueue<Runnable>());

        channelFactory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor, workerThreadCount);
        bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setOption("child.bufferFactory", bufferFactory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(decoder, //
                                         new FrontHanlder(MySqlProxyServer.this) //
                );
            }

        });

        SocketAddress address = new InetSocketAddress("0.0.0.0", listenPort);
        bootstrap.bind(address);
        if (LOG.isInfoEnabled()) {
            LOG.info("Leviathan Server listening " + address);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Leviathan Server started.");
        }

        client = new ClientBootstrap();
        client.setOption("bufferFactory", bufferFactory);
        client.setPipelineFactory(new BackendPipelineFactory(this));
        client.setFactory(new NioClientSocketChannelFactory());
    }

    public ChannelBufferFactory getBufferFactory() {
        return bufferFactory;
    }

    public ClientBootstrap getBackendBootstrap() {
        return client;
    }

    public void connectRemote() {
        client.connect(new InetSocketAddress(remoteHost, remotePort));
    }

    public void stop() {
        bootstrap.shutdown();
        if (LOG.isInfoEnabled()) {
            LOG.info("Leviathan Server stoped.");
        }
    }

    public long getReceivedBytes() {
        return this.decoder.getRecevedBytes();
    }

    public long getReceivedMessageCount() {
        return this.decoder.getReceivedMessageCount();
    }

    public void resetStat() {
        this.decoder.resetStat();
        this.getProxyStat().reset();
    }
}
