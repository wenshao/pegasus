package com.alibaba.sqlwall.net;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public final class BackendPipelineFactory implements ChannelPipelineFactory {

    public ChannelPipeline getPipeline() throws Exception {
        ProxySession session = ProxySession.current();
        BackendHanlder handler = new BackendHanlder(session);
        BackendDecoder decoder = new BackendDecoder(session);

        ChannelPipeline pipeline = Channels.pipeline(decoder, //
                                                     handler //
        );

        return pipeline;
    }
}
