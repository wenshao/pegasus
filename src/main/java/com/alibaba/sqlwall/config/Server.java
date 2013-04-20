package com.alibaba.sqlwall.config;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private List<SocketAddress> addresses         = new ArrayList<SocketAddress>();

    private Db                  db;

    public Server(){

    }

    public List<SocketAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<SocketAddress> addresses) {
        this.addresses = addresses;
    }

    public Db getDb() {
        return db;
    }

    public void setDb(Db db) {
        this.db = db;
    }
}
