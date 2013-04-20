package com.alibaba.pegasus.config;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {

    private int                workerThreadCount = 2;
    private List<ServerConfig> servers           = new ArrayList<ServerConfig>();

    public ServerConfig() {

    }

    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public List<ServerConfig> getServers() {
        return servers;
    }

    public void setServers(List<ServerConfig> servers) {
        this.servers = servers;
    }

}
