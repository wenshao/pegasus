package com.alibaba.sqlwall.mysql;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.alibaba.druid.wall.WallProvider;
import com.alibaba.druid.wall.spi.MySqlWallProvider;

public class MySqlProxyServer {

    static Log                            LOG               = LogFactory.getLog(MySqlProxyServer.class);

    private ServerBootstrap               bootstrap;
    private ThreadPoolExecutor            bossExecutor;
    private ThreadPoolExecutor            workerExecutor;

    private int                           workerThreadCount = Runtime.getRuntime().availableProcessors();

    private NioServerSocketChannelFactory channelFactory;

    final AtomicLong                      acceptedCount     = new AtomicLong();
    final AtomicLong                      closedCount       = new AtomicLong();
    private final AtomicLong              sessionCount      = new AtomicLong();
    private final AtomicLong              runningMax        = new AtomicLong();

    private FrontDecoder                  decoder;

    private ClientBootstrap               client;

    private String                        remoteHost;
    private int                           remotePort;

    private int                           listenPort        = 3306;

    private WallProvider                  wallProvider;

    public MySqlProxyServer(String remoteHost, int remotePort){
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;

        wallProvider = new MySqlWallProvider();
        decoder = new FrontDecoder(wallProvider);
    }

    public WallProvider getWallProvider() {
        return wallProvider;
    }

    public void start() {

        bossExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                              new SynchronousQueue<Runnable>());
        workerExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                                new SynchronousQueue<Runnable>());

        channelFactory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor, workerThreadCount);
        bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setOption("child.bufferFactory", new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));

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
        client.setOption("bufferFactory", new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN));
        client.setPipelineFactory(new BackendPipelineFactory());
        client.setFactory(new NioClientSocketChannelFactory());
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

    void decrementSessionCount() {
        this.sessionCount.decrementAndGet();
    }

    void incrementSessionCount() {
        long current = this.sessionCount.incrementAndGet();
        for (;;) {
            long max = this.runningMax.get();
            if (current > max) {
                boolean success = this.runningMax.compareAndSet(max, current);
                if (success) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public long getSessionCount() {
        return sessionCount.get();
    }

    public long getClosedCount() {
        return this.closedCount.get();
    }

    public long getAcceptedCount() {
        return this.acceptedCount.get();
    }

    public long getReceivedBytes() {
        return this.decoder.getRecevedBytes();
    }

    public long getReceivedMessageCount() {
        return this.decoder.getReceivedMessageCount();
    }

    public void resetStat() {
        this.decoder.resetStat();

        this.acceptedCount.set(0);
        this.closedCount.set(0);
    }
}
