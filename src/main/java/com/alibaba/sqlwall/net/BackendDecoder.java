package com.alibaba.sqlwall.net;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

import com.alibaba.sqlwall.net.protocol.mysql.CharsetUtil;
import com.alibaba.sqlwall.net.protocol.mysql.HandshakePacket;

public class BackendDecoder extends LengthFieldBasedFrameDecoder {

    private final static Log   LOG                  = LogFactory.getLog(BackendDecoder.class);

    private final static int   maxFrameLength       = 1024 * 1024 * 32;                       // 1m
    private final static int   lengthFieldOffset    = 0;
    private final static int   lengthFieldLength    = 3;

    private final AtomicLong   receivedBytes        = new AtomicLong();
    private final AtomicLong   receivedMessageCount = new AtomicLong();

    private final ProxySession session;

    public BackendDecoder(ProxySession session){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 1, 0);
        this.session = session;
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

        byte packetId = frame.getByte(3);

        if (session.getPhase() == ProxySession.PHASE_AUTH) {
            if (packetId == 0) {
                HandshakePacket packet = new HandshakePacket();
                packet.read(frame.array());

                byte charsetIndex = packet.serverCharsetIndex;
                String charset = CharsetUtil.getCharset(charsetIndex);
                session.setCharset(charset);
            } else if (packetId == 2) {
                byte status = frame.getByte(4);
                if (status == 0) {
                    session.setPhase(ProxySession.PHASE_COMMAND);
                } else if (status == (byte) 0xFF) {
                    session.setPhase(ProxySession.PHASE_AUTH_ERROR);
                }
            }
        } else if (session.getPhase() == ProxySession.PHASE_COMMAND) {
            
        }

        receivedMessageCount.incrementAndGet();

        Channel frontChannel = session.getFrontChannel();
        frontChannel.write(frame);

        return null;
    }
}
