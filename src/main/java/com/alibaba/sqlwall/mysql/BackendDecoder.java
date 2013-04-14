package com.alibaba.sqlwall.mysql;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

import com.alibaba.sqlwall.mysql.protocol.mysql.CharsetUtil;
import com.alibaba.sqlwall.mysql.protocol.mysql.EOFPacket;
import com.alibaba.sqlwall.mysql.protocol.mysql.FieldPacket;
import com.alibaba.sqlwall.mysql.protocol.mysql.HandshakePacket;
import com.alibaba.sqlwall.mysql.protocol.mysql.OkPacket;

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
                System.out.println("-> rsp auth : " + packetId);
            } else if (packetId == 2) {
                byte status = frame.getByte(4);
                if (status == 0) {
                    session.setPhase(ProxySession.PHASE_COMMAND);
                    System.out.println("-> rsp auth_ok : " + packetId);
                } else if (status == (byte) 0xFF) {
                    session.setPhase(ProxySession.PHASE_AUTH_ERROR);
                    System.out.println("-> rsp auth_err : " + packetId);
                }
            } else {
                System.out.println("-> rsp other : " + packetId);
            }
        } else if (session.getPhase() == ProxySession.PHASE_COMMAND) {
            int stat = session.getState();
            int len = frame.array().length;

            final int OK = 0;
            final int ERROR = -1;
            final int EOF = -2;

            byte status = frame.getByte(4);

            switch (stat) {
                case ProxySession.STAT_CMD_QUERY: {
                    if (status == ERROR) {
                        System.out.println("-> resp error : " + packetId + " / " + status + ", " + len);
                        session.setState(ProxySession.STAT_CMD_QUERY_RESP_ERROR);
                    } else if (status == EOF) {
                        EOFPacket packet = new EOFPacket();
                        packet.read(frame.array());
                        System.out.println("-> resp eof : " + packetId + " / " + status + ", " + len);
                    } else if (status == OK) {
                        OkPacket packet = new OkPacket();
                        packet.read(frame.array());
                        session.setState(ProxySession.STAT_CMD_QUERY_RESP_EOF);
                        System.out.println("-> resp ok : " + packetId + " / " + status + ", " + len);
                    } else {
                        byte fieldCount = frame.getByte(4);

                        session.setFieldCount(fieldCount);
                        session.setState(ProxySession.STAT_CMD_QUERY_RESP_FIELD);

                        System.out.println("-> resp field_count : " + packetId + " / " + status + ", " + len + " -> "
                                           + fieldCount);
                    }
                }
                    break;
                case ProxySession.STAT_CMD_QUERY_RESP_FIELD: {
                    int fieldCount = session.incrementAndGetFieldIndex();

                    if (fieldCount < session.getFieldCount()) {
                        FieldPacket packet = new FieldPacket();
                        packet.read(frame.array());
                        String fieldName = new String(packet.name, session.getCharset());
                        System.out.println("-> resp field : " + packetId + ", index " + session.getFieldIndex()
                                           + " -> " + fieldName);
                    } else {
                        System.out.println("-> resp field : " + packetId + ", index " + session.getFieldIndex() + ", "
                                           + len + ", " + status);
                    }
                    if (fieldCount == session.getFieldCount()) {
                        session.setState(ProxySession.STAT_CMD_QUERY_RESP_ROW);
                    }
                }
                    break;
                case ProxySession.STAT_CMD_QUERY_RESP_ROW: {
                    if (status == EOF) {
                        session.setState(ProxySession.STAT_CMD_QUERY_RESP_ROW_EOF);
                        System.out.println("-> resp row eof " + packetId + ", " + status);
                    } else {
                        int rowCount = session.incrementAndGetRowIndex();
                        System.out.println("-> resp row : " + packetId + ", index " + rowCount);
                    }
                }
                    break;
                case ProxySession.STAT_CMD_STMT_PREPARE: {
                    if (status == OK) {
                        session.setState(ProxySession.STAT_CMD_STMT_PREPARE_RESP_COLUMN);
                        short columnCount = frame.getShort(5);
                        System.out.println("-> resp stmt_prepare : " + packetId + ", columsn " + columnCount);
                    } else {
                        System.out.println("-> resp stmt_prepare error " + packetId);
                    }
                }
                    break;
                // case ProxySession.STAT_CMD_STMT_PREPARE_RESP_COLUMN:
                // System.out.println("-> resp stmt_prepare_column " + packetId);
                // if (status == EOF) {
                // session.setState(ProxySession.STAT_CMD_STMT_PREPARE_RESP_COLUMN_EOF);
                // }
                // break;
                default: {
                    System.out.println("-> resp status : " + packetId + " / " + status + ", " + len + ", stat " + stat);
                }
                    break;
            }
        } else {
            System.out.println("-> resp other : " + packetId);
        }

        receivedMessageCount.incrementAndGet();

        Channel frontChannel = session.getFrontChannel();
        frontChannel.write(frame);

        return null;
    }
}
