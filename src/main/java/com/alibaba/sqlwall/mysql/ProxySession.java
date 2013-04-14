package com.alibaba.sqlwall.mysql;

import org.jboss.netty.channel.Channel;

public class ProxySession {

    public static final ThreadLocal<ProxySession> currentLocal                          = new ThreadLocal<ProxySession>();

    private final Channel                         frontChannel;
    private Channel                               backendChannel;
    private String                                charset;
    private String                                user;

    public static final int                       PHASE_AUTH                            = 0;
    public static final int                       PHASE_AUTH_ERROR                      = 100;
    public static final int                       PHASE_COMMAND                         = 1001;

    private volatile int                          phase                                 = PHASE_AUTH;

    public static final int                       STAT_CMD_QUERY                        = 1001;
    public static final int                       STAT_CMD_QUERY_RESP_FIELD             = 1002;

    public static final int                       STAT_CMD_QUERY_RESP_ROW               = 1010;
    public static final int                       STAT_CMD_QUERY_RESP_ROW_EOF           = 1011;

    public static final int                       STAT_CMD_QUERY_RESP_ERROR             = 1098;
    public static final int                       STAT_CMD_QUERY_RESP_EOF               = 1099;

    public static final int                       STAT_CMD_STMT_PREPARE                 = 2001;
    public static final int                       STAT_CMD_STMT_PREPARE_RESP_COLUMN     = 2002;
    public static final int                       STAT_CMD_STMT_PREPARE_RESP_COLUMN_EOF = 2003;

    private volatile int                          state;

    private volatile int                          fieldCount;
    private volatile int                          fieldIndex;

    private volatile int                          rowIndex;

    public ProxySession(Channel frontChannel){
        this.frontChannel = frontChannel;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int incrementAndGetRowIndex() {
        return ++this.rowIndex;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public int incrementAndGetFieldIndex() {
        return ++this.fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Channel getFrontChannel() {
        return frontChannel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Channel getBackendChannel() {
        return backendChannel;
    }

    public void setBackendContext(Channel backendChannel) {
        this.backendChannel = backendChannel;
    }

    public static void setCurrent(ProxySession current) {
        currentLocal.set(current);
    }

    public static ProxySession current() {
        return currentLocal.get();
    }

    public boolean check(String sql) {
        return true;
    }
}
