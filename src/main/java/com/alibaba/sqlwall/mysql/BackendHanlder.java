package com.alibaba.sqlwall.mysql;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class BackendHanlder extends SimpleChannelUpstreamHandler {

    private final ProxySession session;

    public BackendHanlder(ProxySession session){
        this.session = session;
    }

    public ProxySession getSession() {
        return session;
    }

    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        session.setBackendContext(e.getChannel());
        
        ctx.sendUpstream(e);
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

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
    }
}
