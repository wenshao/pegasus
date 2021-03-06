package com.alibaba.pegasus.config;

public abstract class DbConfig {

    private int    port;
    private String host;

    public DbConfig(){
    }

    public DbConfig(String host, int port){
        this.host = host;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
