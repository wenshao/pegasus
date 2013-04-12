package com.alibaba.sqlwall.net.protocol.mysql;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class MySqlEncoder extends OneToOneEncoder {

    private final AtomicLong sentBytes        = new AtomicLong();
    private final AtomicLong sentMessageCount = new AtomicLong();

    @Override
    protected Object encode(ChannelHandlerContext arg0, Channel arg1, Object arg2) throws Exception {
        return null;
    }

    public long getSentMessageCount() {
        return sentMessageCount.get();
    }

    public long getSentBytes() {
        return sentBytes.get();
    }

    public void resetStat() {
        sentBytes.set(0);
        sentMessageCount.set(0);
    }
}
