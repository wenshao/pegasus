package com.alibaba.sqlwall.mysql;

import static com.alibaba.sqlwall.ProxySessionStat.*;
import static com.alibaba.sqlwall.mysql.protocol.mysql.CommandPacket.COM_QUERY;
import static com.alibaba.sqlwall.mysql.protocol.mysql.CommandPacket.COM_STMT_CLOSE;
import static com.alibaba.sqlwall.mysql.protocol.mysql.CommandPacket.COM_STMT_EXECUTE;
import static com.alibaba.sqlwall.mysql.protocol.mysql.CommandPacket.COM_STMT_PREPARE;
import static com.alibaba.sqlwall.mysql.protocol.mysql.MySQLPacket.COM_QUIT;


import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

import com.alibaba.druid.wall.WallProvider;
import com.alibaba.sqlwall.ProxySession;
import com.alibaba.sqlwall.mysql.protocol.mysql.AuthPacket;
import com.alibaba.sqlwall.mysql.protocol.mysql.CommandPacket;

public class FrontDecoder extends LengthFieldBasedFrameDecoder {

    private final static Log   LOG                  = LogFactory.getLog(FrontDecoder.class);

    private final static int   maxFrameLength       = 1024 * 1024 * 32;                     // 1m
    private final static int   lengthFieldOffset    = 0;
    private final static int   lengthFieldLength    = 3;

    private final AtomicLong   receivedBytes        = new AtomicLong();
    private final AtomicLong   receivedMessageCount = new AtomicLong();

    private final WallProvider provider;

    public FrontDecoder(WallProvider provider){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 1, 0);
        this.provider = provider;
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

        int packetId = frame.getByte(3);
        ProxySession session = (ProxySession) channel.getAttachment();

        session.setState(STAT_UNKOWN);
        if (session.getPhase() == ProxySession.PHASE_AUTH) {
            if (packetId == 1) {
                AuthPacket packet = new AuthPacket();
                packet.read(frame.array());

                session.setUser(packet.user);
                System.out.println("<- req auth : " + packetId + " user " + packet.user);
            } else {
                System.out.println("<- req other : " + packetId);
            }
        } else if (session.getPhase() == ProxySession.PHASE_COMMAND) {
            if (packetId == 0) {

                CommandPacket packet = new CommandPacket();
                packet.read(frame.array());
                final byte command = packet.command;
                switch (command) {
                    case COM_QUERY: {
                        String sql = new String(packet.arg, session.getCharset());
                        session.setSql(sql);

                        System.out.println("<- req cmd_query : " + sql);

                        long nano = System.nanoTime();
                        session.setState(STAT_CMD_QUERY);
                        session.setFieldCount((short) -1);
                        session.setFieldIndex((short) -1);
                        session.setRowIndex(-1);
                        session.setCommandQueryStartNano(nano);

                        provider.check(sql);
                    }
                        break;
                    case COM_STMT_EXECUTE: {
                        long nano = System.nanoTime();
                        
                        int stmtId = frame.getInt(5);
                        session.setState(STAT_CMD_QUERY);
                        session.setFieldCount((short) -1);
                        session.setFieldIndex((short) -1);
                        session.setRowIndex(-1);
                        session.setCommandQueryStartNano(nano);
                        System.out.println("<- req cmd_stmt_exec " + stmtId);
                    }
                        break;
                    case COM_STMT_PREPARE: {
                        session.setState(STAT_CMD_STMT_PREPARE);
                        String sql = new String(packet.arg, session.getCharset());
                        session.setSql(sql);
                        System.out.println("<- req cmd_stmt_prepare : " + sql);
                    }
                        break;
                    case COM_STMT_CLOSE: {
                        int stmtId = frame.getInt(5);
                        session.remoteStmt(stmtId);
                        System.out.println("<- req cmd_stmt_close " + stmtId);
                    }
                        break;
                    case COM_QUIT:
                        System.out.println("<- req cmd_quit");
                        break;
                    default:
                        System.out.println("<- req cmd : " + command);
                        break;
                }
            } else {
                System.out.println("<- req other : " + packetId);
            }

            // CommandPacket
        } else {
            System.out.println("<- req other : " + packetId);
        }

        receivedMessageCount.incrementAndGet();

        session.getBackendChannel().write(frame);
        return null;
    }
}
