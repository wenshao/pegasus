package com.alibaba.sqlwall.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.alibaba.sqlwall.ProxySession;

public class FrontHanlder extends SimpleChannelUpstreamHandler {

    static Log                     LOG = LogFactory.getLog(FrontHanlder.class);

    private final MySqlProxyServer proxyServer;

    FrontHanlder(MySqlProxyServer mySqlProxyServer){
        proxyServer = mySqlProxyServer;
    }

    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        long acceptedCount = proxyServer.getProxyStat().incrementAcceptedCount();

        Channel channel = e.getChannel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("front channel bound " + channel.getRemoteAddress() + " " + acceptedCount);
        }
        
        ProxySession session = new ProxySession(channel);
        ProxySession.setCurrent(session);
        channel.setAttachment(session);

        proxyServer.connectRemote();

        ctx.sendUpstream(e);
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        proxyServer.getProxyStat().incrementAndGetClosedCount();
        
        Channel channel = e.getChannel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("front channel closed " + channel.getRemoteAddress());
        }
        
        ProxySession session = (ProxySession) channel.getAttachment();
        if (session != null) {
            Channel backendChannel = session.getBackendChannel();
            if (backendChannel != null && backendChannel.isOpen()) {
                backendChannel.close();
            }
        }

        ctx.sendUpstream(e);
    }
}
