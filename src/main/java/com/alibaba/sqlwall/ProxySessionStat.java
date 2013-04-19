package com.alibaba.sqlwall;

public interface ProxySessionStat {

    public static final int STAT_UNKOWN                           = -1;

    public static final int STAT_INIT                             = 100;
    public static final int STAT_HANDSHAKE                        = 200;
    public static final int STAT_AUTH                             = 300;
    public static final int STAT_AUTH_OK                          = 301;
    public static final int STAT_AUTH_ERROR                       = 302;

    public static final int STAT_CMD_QUERY                        = 1001;
    public static final int STAT_CMD_QUERY_RESP_FIELD             = 1002;

    public static final int STAT_CMD_QUERY_RESP_ROW               = 1010;
    public static final int STAT_CMD_QUERY_RESP_ROW_EOF           = 1011;

    public static final int STAT_CMD_QUERY_RESP_ERROR             = 1098;
    public static final int STAT_CMD_QUERY_RESP_EOF               = 1099;

    public static final int STAT_CMD_STMT_PREPARE                 = 2001;
    public static final int STAT_CMD_STMT_PREPARE_RESP_COLUMN     = 2002;
    public static final int STAT_CMD_STMT_PREPARE_RESP_COLUMN_EOF = 2003;
    public static final int STAT_CMD_STMT_PREPARE_RESP_PARAM      = 2004;

    public static final int STAT_CLOSED                           = 9999;
}
