package com.alibaba.pegasus.config;

import java.util.ArrayList;
import java.util.List;

public class PegasusConfig {

    private int               workerThreadCount = 2;
    private List<ProxyConfig> proxyList         = new ArrayList<ProxyConfig>();

    public PegasusConfig(){

    }

    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public List<ProxyConfig> getProxyList() {
        return proxyList;
    }

    public void setProxyList(List<ProxyConfig> proxyList) {
        this.proxyList = proxyList;
    }

}
