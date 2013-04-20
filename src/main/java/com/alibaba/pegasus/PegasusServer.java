package com.alibaba.pegasus;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.pegasus.config.PegasusConfig;
import com.alibaba.pegasus.config.ProxyConfig;
import com.alibaba.pegasus.listener.ExecuteBeforeListener;
import com.alibaba.pegasus.stat.ProxyServerStat;

public class PegasusServer {

    private final static Log              LOG                    = LogFactory.getLog(PegasusServer.class);

    private ThreadPoolExecutor            bossExecutor;
    private ThreadPoolExecutor            workerExecutor;

    private NioServerSocketChannelFactory channelFactory;

    private ProxyServerStat               serverStat             = new ProxyServerStat(JdbcConstants.MYSQL);

    private List<ExecuteBeforeListener>   executeBeforeListeners = new CopyOnWriteArrayList<ExecuteBeforeListener>();

    private ChannelBufferFactory          bufferFactory          = new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN);

    private PegasusConfig                 config                 = new PegasusConfig();

    private final List<DbProxy>           proxyList              = new ArrayList<DbProxy>();

    public PegasusServer(){
    }

    public PegasusConfig getConfig() {
        return config;
    }

    public ProxyServerStat getProxyStat() {
        return this.serverStat;
    }

    public List<ExecuteBeforeListener> getExecuteBeforeListeners() {
        return executeBeforeListeners;
    }

    public NioServerSocketChannelFactory getChannelFactory() {
        return channelFactory;
    }

    public void start() {

        bossExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                              new SynchronousQueue<Runnable>());
        workerExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                                new SynchronousQueue<Runnable>());

        channelFactory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor, config.getWorkerThreadCount());

        for (ProxyConfig proxyConfig : this.config.getProxyList()) {
            DbProxy proxy = new DbProxy(this, proxyConfig);
            proxy.start();

            proxyList.add(proxy);
        }
    }

    public ChannelBufferFactory getBufferFactory() {
        return bufferFactory;
    }

    public void stop() {
        for (DbProxy proxy : this.proxyList) {
            proxy.stop();
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Pegasus stoped.");
        }
    }

    public void resetStat() {
        for (DbProxy proxy : this.proxyList) {
            proxy.resetStat();
        }
        this.getProxyStat().reset();
    }
}
