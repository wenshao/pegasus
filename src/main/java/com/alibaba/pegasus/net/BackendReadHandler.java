package com.alibaba.pegasus.net;

import static com.alibaba.pegasus.ProxySessionStat.*;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.pegasus.mysql.protocol.mysql.CharsetUtil;
import com.alibaba.pegasus.mysql.protocol.mysql.HandshakePacket;

public class BackendReadHandler implements CompletionHandler<Integer, Session> {

    private final static Log LOG = LogFactory.getLog(BackendReadHandler.class);

    @Override
    public void completed(Integer countObject, Session session) {
        int count = countObject.intValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("readBytes : " + count);
        }

        ByteBuffer buf = session.getBackendBuffer();

        int len = Bits.getUnsignedMedium(buf, 0);
        byte status = buf.get(4);
        int packetLen = len + 4;

        if (count == packetLen) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("===== len " + len + ", pos " + buf.position());
            }

            writePacket(buf, packetLen, session);
            return;
        }

        if (count < packetLen) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("<<<<<<<<<< len " + len + ", pos " + buf.position());
            }
            writePacket(buf, packetLen, session);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(">>>>>>>> len " + len + ", pos " + buf.position());
            }

            writePacket(buf, packetLen, session);
        }
    }

    private void writePacket(ByteBuffer buf, int len, Session session) {
        buf.flip();

        int stat = session.getState();
        switch (stat) {
            case STAT_INIT: {
                HandshakePacket packet = new HandshakePacket();
                packet.read(buf);

                byte charsetIndex = packet.serverCharsetIndex;
                String charset = CharsetUtil.getCharset(charsetIndex);
                session.setCharset(charset);
                session.setState(STAT_HANDSHAKE);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("$RSP handshake, packetId " + packet.packetId);
                }
            }
                break;
            default: {

            }
                break;
        }

        buf.rewind();

        MySqlProxy proxy = session.getProxy();
        FrontWriteHandler frontWriteHandler = proxy.getFrontWriteHandler();
        AsynchronousSocketChannel frontChannel = session.getFrontChannel();
        frontChannel.write(buf, session, frontWriteHandler);
        return;
    }

    @Override
    public void failed(Throwable exc, Session session) {

    }

}
