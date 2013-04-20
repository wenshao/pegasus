package com.alibaba.sqlwall.mysql;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import com.alibaba.sqlwall.ProxyServer;
import com.alibaba.sqlwall.ProxySession;

public final class BackendPipelineFactory implements ChannelPipelineFactory {

    private final ProxyServer proxyServer;

    public BackendPipelineFactory(ProxyServer proxyServer){
        this.proxyServer = proxyServer;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ProxySession session = ProxySession.current();
        BackendHanlder handler = new BackendHanlder(proxyServer, session);
        BackendDecoder decoder = new BackendDecoder(session);

        ChannelPipeline pipeline = Channels.pipeline(decoder, //
                                                     handler //
        );

        return pipeline;
    }
}
