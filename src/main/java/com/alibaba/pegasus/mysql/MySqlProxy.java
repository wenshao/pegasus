package com.alibaba.pegasus.mysql;

import com.alibaba.pegasus.DbProxy;
import com.alibaba.pegasus.PegasusServer;
import com.alibaba.pegasus.config.ProxyConfig;

public class MySqlProxy extends DbProxy {
    public MySqlProxy(PegasusServer server, ProxyConfig config){
        super(server, config);
    }
}
