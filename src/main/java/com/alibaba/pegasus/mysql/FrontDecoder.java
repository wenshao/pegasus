package com.alibaba.pegasus.mysql;

import static com.alibaba.pegasus.ProxySessionStat.STAT_AUTH;
import static com.alibaba.pegasus.ProxySessionStat.STAT_CMD_QUERY;
import static com.alibaba.pegasus.ProxySessionStat.STAT_CMD_STMT_PREPARE;
import static com.alibaba.pegasus.ProxySessionStat.STAT_HANDSHAKE;
import static com.alibaba.pegasus.ProxySessionStat.STAT_UNKOWN;
import static com.alibaba.pegasus.mysql.protocol.mysql.CommandPacket.COM_QUERY;
import static com.alibaba.pegasus.mysql.protocol.mysql.CommandPacket.COM_STMT_CLOSE;
import static com.alibaba.pegasus.mysql.protocol.mysql.CommandPacket.COM_STMT_EXECUTE;
import static com.alibaba.pegasus.mysql.protocol.mysql.CommandPacket.COM_STMT_PREPARE;
import static com.alibaba.pegasus.mysql.protocol.mysql.MySQLPacket.COM_QUIT;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

import com.alibaba.druid.stat.JdbcSqlStat;
import com.alibaba.druid.wall.WallCheckResult;
import com.alibaba.druid.wall.WallProvider;
import com.alibaba.pegasus.DbProxy;
import com.alibaba.pegasus.ProxySession;
import com.alibaba.pegasus.mysql.protocol.mysql.AuthPacket;
import com.alibaba.pegasus.mysql.protocol.mysql.CommandPacket;
import com.alibaba.pegasus.mysql.protocol.mysql.ErrorPacket;

public class FrontDecoder extends LengthFieldBasedFrameDecoder {

    private final static Log LOG                  = LogFactory.getLog(FrontDecoder.class);

    private final static int maxFrameLength       = 1024 * 1024 * 16;                     // 1m
    private final static int lengthFieldOffset    = 0;
    private final static int lengthFieldLength    = 3;

    private final AtomicLong receivedBytes        = new AtomicLong();
    private final AtomicLong receivedMessageCount = new AtomicLong();

    private final DbProxy    proxy;

    public FrontDecoder(DbProxy proxy){
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 1, 0);
        this.proxy = proxy;
    }

    public long getRecevedBytes() {
        return receivedBytes.get();
    }

    public WallProvider getWallProvider() {
        return proxy.getWallProvider();
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

        boolean error = false;

        int stat = session.getState();
        session.setState(STAT_UNKOWN);
        if (stat == STAT_HANDSHAKE) {
            AuthPacket packet = new AuthPacket();
            packet.read(frame.array());

            session.setUser(packet.user);
            session.setState(STAT_AUTH);
            if (LOG.isDebugEnabled()) {
                LOG.debug("auth_req, packetId " + packetId + ", user " + packet.user);
            }
        } else {
            if (packetId == 0) {
                CommandPacket packet = new CommandPacket();
                packet.read(frame.array());
                final byte command = packet.command;

                switch (command) {
                    case COM_QUERY: {
                        String sql = new String(packet.arg, session.getCharset());
                        JdbcSqlStat sqlStat = proxy.getServer().getProxyStat().createSqlStat(sql);

                        session.setSql(sql);
                        session.setSqlStat(sqlStat);

                        if (sqlStat != null) {
                            sqlStat.incrementRunningCount();
                        }

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("req cmd_query : " + sql);
                        }

                        long nano = System.nanoTime();
                        session.setState(STAT_CMD_QUERY);
                        session.setFieldCount((short) -1);
                        session.setFieldIndex((short) -1);
                        session.setRowIndex(-1);
                        session.setCommandQueryStartNano(nano);

                        WallCheckResult result = getWallProvider().check(sql);
                        if (result.getViolations().size() > 0) {
                            error = true;
                        }
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

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("req cmd_stmt_exec " + stmtId);
                        }
                    }
                        break;
                    case COM_STMT_PREPARE: {
                        session.setState(STAT_CMD_STMT_PREPARE);
                        String sql = new String(packet.arg, session.getCharset());
                        session.setSql(sql);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("req cmd_stmt_prepare : " + sql);
                        }
                    }
                        break;
                    case COM_STMT_CLOSE: {
                        int stmtId = frame.getInt(5);
                        session.remoteStmt(stmtId);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("req cmd_stmt_close " + stmtId);
                        }
                    }
                        break;
                    case COM_QUIT:
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("req cmd_quit");
                        }
                        break;
                    default:
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("req cmd : " + command);
                        }
                        break;
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("req other : " + packetId);
                }
            }
        }

        receivedMessageCount.incrementAndGet();

        if (error) {
            ErrorPacket errorPacket = new ErrorPacket();
            errorPacket.packetId = 1;
            errorPacket.errno = 1146;
            errorPacket.message = "sql injection error".getBytes(session.getCharset());
            errorPacket.sqlState = "12345".getBytes();

            int size = errorPacket.calcPacketSize() + 4;
            ChannelBuffer errorBuffer = proxy.getServer().getBufferFactory().getBuffer(size);
            errorPacket.write(errorBuffer);
            channel.write(errorBuffer);
        } else {
            Channel backendChannel = session.getBackendChannel();
            backendChannel.write(frame);
        }
        return null;
    }
}
