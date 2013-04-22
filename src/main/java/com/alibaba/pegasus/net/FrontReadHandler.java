package com.alibaba.pegasus.net;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FrontReadHandler implements CompletionHandler<Integer, Session> {

    private final static Log LOG = LogFactory.getLog(FrontReadHandler.class);

    @Override
    public void completed(Integer count, Session session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("readBytes : " + count);
        }

        MySqlProxy proxy = session.getProxy();
        BackendWriteHandler backendWriteHandler = proxy.getBackendWriteHandler();
        ByteBuffer buf = session.getFrontBuffer();
        
        buf.flip();
        
        AsynchronousSocketChannel backendChannel = session.getBackendChannel();
        backendChannel.write(buf, session, backendWriteHandler);
    }

    @Override
    public void failed(Throwable exc, Session session) {

    }

}
