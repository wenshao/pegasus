package com.alibaba.pegasus.config;

import java.net.InetSocketAddress;

public class MySqlProxyConfig extends ProxyConfig {

    public MySqlProxyConfig(){

    }

    public MySqlProxyConfig(int port, String remoteHost, int remotePort){
        this.getAddresses().add(new InetSocketAddress("0.0.0.0", port));
        MySqlDbConfig db = new MySqlDbConfig(remoteHost, remotePort);
        this.setDb(db);
    }
}
