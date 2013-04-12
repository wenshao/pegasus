package com.alibaba.sqlwall.net.protocol.mysql;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

public class MySqlDecoder extends LengthFieldBasedFrameDecoder {

    private final static int maxFrameLength       = 1024 * 1024;     // 1m
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
}
