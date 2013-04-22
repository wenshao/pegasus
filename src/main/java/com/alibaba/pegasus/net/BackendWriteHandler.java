package com.alibaba.pegasus.net;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BackendWriteHandler implements CompletionHandler<Integer, Session> {

    private final static Log LOG = LogFactory.getLog(BackendWriteHandler.class);

    @Override
    public void completed(Integer count, Session session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeBytes : " + count);
        }

        ByteBuffer buf = session.getFrontBuffer();
        buf.clear();

        MySqlProxy proxy = session.getProxy();

        FrontReadHandler frontReadHandler = proxy.getFrontReadHandler();
        AsynchronousSocketChannel frontChannel = session.getFrontChannel();

        frontChannel.read(buf, session, frontReadHandler);
    }

    @Override
    public void failed(Throwable exc, Session attachment) {

    }

}
