package com.alibaba.sqlwall;

import static com.alibaba.sqlwall.ProxySessionStat.STAT_INIT;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;

import com.alibaba.druid.stat.JdbcSqlStat;

public class ProxySession {

    public static final ThreadLocal<ProxySession> currentLocal     = new ThreadLocal<ProxySession>();

    private final Channel                         frontChannel;
    private Channel                               backendChannel;
    private String                                charset;
    private String                                user;

    public static final int                       PHASE_AUTH       = 0;
    public static final int                       PHASE_AUTH_ERROR = 100;
    public static final int                       PHASE_COMMAND    = 1001;

    private volatile int                          state            = STAT_INIT;

    private volatile short                        fieldCount;
    private volatile short                        fieldIndex;

    private volatile int                          rowIndex;

    private String                                sql;
    private JdbcSqlStat                           sqlStat;

    private Map<Integer, PStmtInfo>               stmtMap          = new ConcurrentHashMap<Integer, PStmtInfo>(16,
                                                                                                               0.75f, 1);

    private volatile long                         commandQueryStartNano;
    private long                                  commandQueryExecuteEndNano;

    private long                                  connectMillis;

    public ProxySession(Channel frontChannel){
        this.frontChannel = frontChannel;
        this.connectMillis = System.currentTimeMillis();
    }

    public long getCommandQueryStartNano() {
        return commandQueryStartNano;
    }

    public void setCommandQueryStartNano(long commandQueryStartNano) {
        this.commandQueryStartNano = commandQueryStartNano;
    }

    public long getCommandQueryExecuteEndNano() {
        return commandQueryExecuteEndNano;
    }

    public void setCommandQueryExecuteEndNano(long commandQueryExecuteEndNano) {
        this.commandQueryExecuteEndNano = commandQueryExecuteEndNano;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public JdbcSqlStat getSqlStat() {
        return sqlStat;
    }

    public void setSqlStat(JdbcSqlStat sqlStat) {
        this.sqlStat = sqlStat;
    }

    public void putStmt(int stmtId, PStmtInfo stmt) {
        stmtMap.put(stmtId, stmt);
    }

    public PStmtInfo getStmt(int stmtId) {
        return stmtMap.get(stmtId);
    }

    public PStmtInfo remoteStmt(int stmtId) {
        return stmtMap.remove(stmtId);
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

    public short getFieldCount() {
        return fieldCount;
    }

    public short getFieldIndex() {
        return fieldIndex;
    }

    public int incrementAndGetFieldIndex() {
        return ++this.fieldIndex;
    }

    public void setFieldIndex(short fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public void setFieldCount(short fieldCount) {
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

    public long getConnectMillis() {
        return this.connectMillis;
    }
}
