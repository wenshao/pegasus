package com.alibaba.pegasus.net;

import static com.alibaba.pegasus.ProxySessionStat.*;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FrontWriteHandler implements CompletionHandler<Integer, Session> {

    private final static Log LOG = LogFactory.getLog(FrontWriteHandler.class);

    @Override
    public void completed(Integer count, Session session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeBytes : " + count);
        }

        int state = session.getState();
        switch (state) {
            case STAT_HANDSHAKE:
            case STAT_AUTH_OK: {
                ByteBuffer buf = session.getFrontBuffer();
                buf.clear();

                MySqlProxy proxy = session.getProxy();

                FrontReadHandler frontReadHandler = proxy.getFrontReadHandler();
                AsynchronousSocketChannel frontChannel = session.getFrontChannel();

                frontChannel.read(buf, session, frontReadHandler);
            }
                break;
            default:
                LOG.error("TODO : ");
                break;
        }

        // ByteBuffer buf = session.getBackendBuffer();
        // buf.clear();
        //
        // MySqlProxy proxy = session.getProxy();
        //
        // BackendReadHandler frontReadHandler = proxy.getBackendReadHandler();
        // AsynchronousSocketChannel frontChannel = session.getBackendChannel();
        //
        // frontChannel.read(buf, session, frontReadHandler);
    }

    @Override
    public void failed(Throwable exc, Session attachment) {

    }

}
