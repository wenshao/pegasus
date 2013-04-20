package com.alibaba.pegasus.mysql;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import com.alibaba.pegasus.DbProxy;
import com.alibaba.pegasus.ProxySession;

public final class BackendPipelineFactory implements ChannelPipelineFactory {

    private final DbProxy proxy;

    public BackendPipelineFactory(DbProxy proxy){
        this.proxy = proxy;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ProxySession session = ProxySession.current();
        BackendHanlder handler = new BackendHanlder(proxy, session);
        BackendDecoder decoder = new BackendDecoder(session);

        ChannelPipeline pipeline = Channels.pipeline(decoder, //
                                                     handler //
        );

        return pipeline;
    }
}
