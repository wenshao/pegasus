package com.alibaba.sqlwall.test;

import junit.framework.TestCase;

import com.alibaba.pegasus.PegasusServer;
import com.alibaba.pegasus.config.MySqlProxyConfig;

public class PegasusServerTest extends TestCase {

    public void test_connect() throws Exception {
        int listenPort = 3306;
        String dbHost = "hbase-01";
        int dbPort = 3306;

        MySqlProxyConfig proxyConfig = new MySqlProxyConfig(listenPort, dbHost, dbPort);

        PegasusServer server = new PegasusServer();
        server.getConfig().getProxyList().add(proxyConfig);
        server.start();

        server.waitForStop();
    }
}
