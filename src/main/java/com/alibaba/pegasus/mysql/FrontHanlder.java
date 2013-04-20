package com.alibaba.pegasus.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.alibaba.pegasus.DbProxy;
import com.alibaba.pegasus.ProxySession;

public class FrontHanlder extends SimpleChannelUpstreamHandler {

    static Log            LOG = LogFactory.getLog(FrontHanlder.class);

    private final DbProxy proxy;

    public FrontHanlder(DbProxy proxy){
        this.proxy = proxy;
    }

    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        long acceptedCount = proxy.getServer().getProxyStat().incrementAcceptedCount();

        Channel channel = e.getChannel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("front channel bound " + channel.getRemoteAddress() + " " + acceptedCount);
        }

        ProxySession session = new ProxySession(channel);
        ProxySession.setCurrent(session);
        channel.setAttachment(session);

        proxy.connectRemote();

        ctx.sendUpstream(e);
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        proxy.getServer().getProxyStat().incrementAndGetClosedCount();

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
