package com.alibaba.pegasus.config;

import java.util.ArrayList;
import java.util.List;

public class PegasusConfig {

    private int                 workerThreadCount = 2;
    private List<PegasusConfig> servers           = new ArrayList<PegasusConfig>();

    public PegasusConfig(){

    }

    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public List<PegasusConfig> getServers() {
        return servers;
    }

    public void setServers(List<PegasusConfig> servers) {
        this.servers = servers;
    }

}
