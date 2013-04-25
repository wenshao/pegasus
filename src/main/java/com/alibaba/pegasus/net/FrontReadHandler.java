package com.alibaba.pegasus.net;

import static com.alibaba.pegasus.ProxySessionStat.STAT_AUTH;
import static com.alibaba.pegasus.ProxySessionStat.STAT_HANDSHAKE;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.pegasus.mysql.protocol.mysql.AuthPacket;

public class FrontReadHandler implements CompletionHandler<Integer, Session> {

    private final static Log LOG = LogFactory.getLog(FrontReadHandler.class);

    @Override
    public void completed(Integer countObject, Session session) {
        int count = countObject.intValue();

        if (LOG.isDebugEnabled()) {
            LOG.debug("readBytes : " + count);
        }

        MySqlProxy proxy = session.getProxy();
        ByteBuffer buf = session.getFrontBuffer();

        if (count == -1) {
            FrontReadHandler frontReadHandler = proxy.getFrontReadHandler();
            AsynchronousSocketChannel frontChannel = session.getFrontChannel();
            frontChannel.read(buf, session, frontReadHandler);
            return;
        }

        buf.flip();
        
        int len = Bits.getUnsignedMedium(buf, 0);
        int packetLen = len + 4;
        
        if (count == packetLen) {
            int stat = session.getState();
            
            if (stat == STAT_HANDSHAKE) {
                AuthPacket packet_x = new AuthPacket();
                packet_x.read(buf.array());
                
                AuthPacket packet = new AuthPacket();
                packet.read(buf);
                
                session.setUser(packet.user);
                session.setState(STAT_AUTH);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("auth_req, packetId " + packet.packetId + ", user " + packet.user);
                }
            } else {
                LOG.error("TODO count " + count + ", len " + len);
            }
            
            buf.rewind();

            BackendWriteHandler backendWriteHandler = proxy.getBackendWriteHandler();
            AsynchronousSocketChannel backendChannel = session.getBackendChannel();
            backendChannel.write(buf, session, backendWriteHandler);
        } else {
            LOG.error("TODO count " + count + ", len " + len);
        }
    }

    @Override
    public void failed(Throwable exc, Session session) {

    }

}
