package com.alibaba.pegasus.mysql;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import com.alibaba.pegasus.PegasusServer;
import com.alibaba.pegasus.ProxySession;

public final class BackendPipelineFactory implements ChannelPipelineFactory {

    private final PegasusServer proxyServer;

    public BackendPipelineFactory(PegasusServer proxyServer){
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
