package com.alibaba.sqlwall.config;

import java.util.ArrayList;
import java.util.List;

public class ProxyConfig {

    private int          workerThreadCount = 2;
    private List<Server> servers           = new ArrayList<Server>();
    
    public ProxyConfig() {
        
    }
    
    public int getWorkerThreadCount() {
        return workerThreadCount;
    }
    
    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }



    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }
}
