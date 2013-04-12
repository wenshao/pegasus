package com.alibaba.sqlwall.net.protocol.mysql;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

public class MySqlDecoder extends LengthFieldBasedFrameDecoder {
    private final static Log LOG                  = LogFactory.getLog(MySqlDecoder.class);
    
    private final static int maxFrameLength       = 1024 * 1024 * 32;     // 1m
    private final static int lengthFieldOffset    = 0;
    private final static int lengthFieldLength    = 3;

    private final AtomicLong receivedBytes        = new AtomicLong();
    private final AtomicLong receivedMessageCount = new AtomicLong();

    public MySqlDecoder(){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 4, 0);
    }

    public MySqlDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
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

        byte[] bytes = frame.array();

        throw new Exception("not support format");
    }
}
