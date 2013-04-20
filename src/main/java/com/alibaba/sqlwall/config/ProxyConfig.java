package com.alibaba.sqlwall.config;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ProxyConfig {

    private List<SocketAddress> addresses = new ArrayList<SocketAddress>();

    private DbConfig            db;

    public ProxyConfig(){

    }

    public List<SocketAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<SocketAddress> addresses) {
        this.addresses = addresses;
    }

    public DbConfig getDb() {
        return db;
    }

    public void setDb(DbConfig db) {
        this.db = db;
    }

}
