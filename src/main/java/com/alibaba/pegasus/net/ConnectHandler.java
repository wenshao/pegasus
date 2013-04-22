package com.alibaba.pegasus.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConnectHandler implements CompletionHandler<Void, Session> {

    private final static Log LOG = LogFactory.getLog(ConnectHandler.class);

    @Override
    public void completed(Void result, Session session) {
        try {
            AsynchronousSocketChannel frontChannel = session.getFrontChannel();
            AsynchronousSocketChannel backendChannel = session.getBackendChannel();

            if (LOG.isDebugEnabled()) {
                LOG.debug("connected " + backendChannel.getRemoteAddress());
            }

            MySqlProxy proxy = session.getProxy();

            ByteBuffer frontBuffer = session.getFrontBuffer();
            ByteBuffer backendBuffer = session.getBackendBuffer();

            FrontReadHandler frontReadHandler = proxy.getFrontReadHandler();
            BackendReadHandler backendReadHandler = proxy.getBackendReadHandler();

            frontChannel.read(frontBuffer, session, frontReadHandler);
            backendChannel.read(backendBuffer, session, backendReadHandler);
        } catch (IOException ex) {
            LOG.error("open backendChannel error", ex);
        }
    }

    @Override
    public void failed(Throwable exc, Session session) {

    }

}
