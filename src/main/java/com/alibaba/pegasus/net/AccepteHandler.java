package com.alibaba.pegasus.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.pegasus.config.DbConfig;

public class AccepteHandler implements CompletionHandler<AsynchronousSocketChannel, MySqlProxy> {

    private final static Log LOG = LogFactory.getLog(AccepteHandler.class);

    @Override
    public void completed(AsynchronousSocketChannel channel, MySqlProxy proxy) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("accepted : " + channel.getRemoteAddress());
            }

            DbConfig dbConfig = proxy.getConfig().getDb();

            AsynchronousSocketChannel backendChannel = AsynchronousSocketChannel.open(proxy.getThreadGroup());

            Session session = new Session(proxy, channel, backendChannel);
            InetSocketAddress address = new InetSocketAddress(dbConfig.getHost(), dbConfig.getPort());
            backendChannel.connect(address, session, proxy.getConnectHandler());
        } catch (IOException ex) {
            LOG.error("open backendChannel error", ex);
        }
    }

    @Override
    public void failed(Throwable exc, MySqlProxy proxy) {

    }

}
