package com.alibaba.pegasus.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.alibaba.pegasus.DbProxy;
import com.alibaba.pegasus.ProxySession;

public class BackendHanlder extends SimpleChannelUpstreamHandler {

    private static Log         LOG = LogFactory.getLog(BackendHanlder.class);

    private final DbProxy      proxy;
    private final ProxySession session;

    public BackendHanlder(DbProxy proxy, ProxySession session){
        this.proxy = proxy;
        this.session = session;
    }

    public ProxySession getSession() {
        return session;
    }

    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Channel channel = e.getChannel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("backend channel bound " + channel.getRemoteAddress());
        }

        session.setBackendContext(channel);

        proxy.getServer().getProxyStat().incrementSessionCount();

        ctx.sendUpstream(e);
    }

    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        ctx.sendUpstream(e);
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Channel channel = e.getChannel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("backend channel closed " + channel.getRemoteAddress());
        }

        ProxySession session = (ProxySession) channel.getAttachment();
        if (session != null) {
            Channel frontChannel = session.getFrontChannel();
            if (frontChannel != null && frontChannel.isOpen()) {
                frontChannel.close();
            }
        }

        proxy.getServer().getProxyStat().decrementSessionCount();

        ctx.sendUpstream(e);
    }
}
