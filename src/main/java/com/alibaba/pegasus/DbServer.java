package com.alibaba.pegasus;

import com.alibaba.pegasus.config.DbConfig;

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
