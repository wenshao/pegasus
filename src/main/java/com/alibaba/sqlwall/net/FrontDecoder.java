package com.alibaba.sqlwall.net;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

public class FrontDecoder extends LengthFieldBasedFrameDecoder {

    private final static Log LOG                  = LogFactory.getLog(FrontDecoder.class);

    private final static int maxFrameLength       = 1024 * 1024 * 32;                     // 1m
    private final static int lengthFieldOffset    = 0;
    private final static int lengthFieldLength    = 3;

    private final AtomicLong receivedBytes        = new AtomicLong();
    private final AtomicLong receivedMessageCount = new AtomicLong();

    public FrontDecoder(){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 1, 0);
    }

    public long getRecevedBytes() {
        return receivedBytes.get();
    }

    public long getReceivedMessageCount() {
        return receivedMessageCount.get();
    }

    public void resetStat() {
        receivedBytes.set(0);
        receivedMessageCount.set(0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        ChannelBuffer frame = null;
        try {
            frame = (ChannelBuffer) super.decode(ctx, channel, buffer);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            channel.close();
            return null;
        }

        if (frame == null) {
            return null;
        }

        receivedMessageCount.incrementAndGet();

        ProxySession session = (ProxySession) channel.getAttachment();
        session.getBackendChannel().write(frame);
        return null;
    }
}
