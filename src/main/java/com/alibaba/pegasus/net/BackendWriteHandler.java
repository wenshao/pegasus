package com.alibaba.pegasus.net;

import static com.alibaba.pegasus.ProxySessionStat.STAT_AUTH;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BackendWriteHandler implements CompletionHandler<Integer, Session> {

    private final static Log LOG = LogFactory.getLog(BackendWriteHandler.class);

    @Override
    public void completed(Integer countObject, Session session) {
        int count = countObject.intValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeBytes : " + count);
        }
        
        int stat = session.getState();
        if (stat == STAT_AUTH) {
            ByteBuffer buf = session.getBackendBuffer();
            buf.clear();

            MySqlProxy proxy = session.getProxy();

            BackendReadHandler backendReadHandler = proxy.getBackendReadHandler();
            AsynchronousSocketChannel backendChannel = session.getBackendChannel();

            backendChannel.read(buf, session, backendReadHandler);
            return;
        }

        // ByteBuffer buf = session.getFrontBuffer();
        // buf.clear();
        //
        // MySqlProxy proxy = session.getProxy();
        //
        // FrontReadHandler frontReadHandler = proxy.getFrontReadHandler();
        // AsynchronousSocketChannel frontChannel = session.getFrontChannel();
        //
        // frontChannel.read(buf, session, frontReadHandler);
    }

    @Override
    public void failed(Throwable exc, Session attachment) {

    }

}
