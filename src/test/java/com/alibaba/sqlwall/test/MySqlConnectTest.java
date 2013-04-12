package com.alibaba.sqlwall.test;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import junit.framework.TestCase;

import com.alibaba.sqlwall.net.MySqlProxyServer;

public class MySqlConnectTest extends TestCase {

    public void test_connect() throws Exception {
        MySqlProxyServer server = new MySqlProxyServer();
        server.start();
        
        //jdbc:mysql://scuritytest.mysql.rds.aliyuncs.com:3306/mysql
        Driver driver = new com.mysql.jdbc.Driver();
        Properties info = new Properties();
        info.put("user", "root");
        info.put("password", "hello123");
        Connection conn = driver.connect("jdbc:mysql://127.0.0.1:3306/mysql", info);
        conn.close();
    }
}
