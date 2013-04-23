package com.alibaba.pegasus.net;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BackendReadHandler implements CompletionHandler<Integer, Session> {

    private final static Log LOG = LogFactory.getLog(BackendReadHandler.class);

    @Override
    public void completed(Integer count, Session session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("readBytes : " + count);
        }
        
        MySqlProxy proxy = session.getProxy();
        FrontWriteHandler frontWriteHandler = proxy.getFrontWriteHandler();
        ByteBuffer buf = session.getBackendBuffer();
        buf.mark();
        
        buf.flip();
        int len = Bits.getUnsignedMediumL(buf, 0);
        if (buf.position() + 4 > len) {
            LOG.debug("len < limit, len " + len + ", pos " + buf.position());
        }
        buf.reset();
        
        buf.flip();
        
        AsynchronousSocketChannel frontChannel = session.getFrontChannel();
        frontChannel.write(buf, session, frontWriteHandler);
    }

    @Override
    public void failed(Throwable exc, Session session) {

    }

}
