package com.alibaba.sqlwall;

import com.alibaba.sqlwall.config.DbConfig;

public class DbServer {

    private DbConfig config;

    public DbServer(DbConfig config){
        this.config = config;
    }

    public DbConfig getConfig() {
        return config;
    }

    public void setConfig(DbConfig config) {
        this.config = config;
    }
}
