package com.alibaba.sqlwall.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class FrontHanlder extends SimpleChannelUpstreamHandler {

    static Log                     LOG = LogFactory.getLog(FrontHanlder.class);

    private final MySqlProxyServer proxyServer;

    FrontHanlder(MySqlProxyServer mySqlProxyServer){
        proxyServer = mySqlProxyServer;
    }

    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        long acceptedCount = proxyServer.acceptedCount.incrementAndGet();
        proxyServer.incrementSessionCount();

        Channel channel = e.getChannel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("accepted " + channel.getRemoteAddress() + " " + acceptedCount);
        }
        
        
        ProxySession session = new ProxySession(channel);
        ProxySession.setCurrent(session);
        channel.setAttachment(session);

        proxyServer.connectRemote();

        ctx.sendUpstream(e);
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        proxyServer.closedCount.incrementAndGet();
        proxyServer.decrementSessionCount();

        ctx.sendUpstream(e);
    }

    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ctx.sendUpstream(e);

        String message = (String) e.getMessage();
        ctx.getChannel().write(message);
    }
}
