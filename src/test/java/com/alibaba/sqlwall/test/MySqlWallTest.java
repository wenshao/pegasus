package com.alibaba.sqlwall.test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.pegasus.PegasusServer;
import com.alibaba.pegasus.config.MySqlProxyConfig;

public class MySqlWallTest extends TestCase {

    public void test_connect() throws Exception {
        int listenPort = 3306;
        String dbHost = "hbase-01";
        int dbPort = 3306;

        MySqlProxyConfig proxyConfig = new MySqlProxyConfig(listenPort, dbHost, dbPort);

        PegasusServer server = new PegasusServer();
        server.getConfig().getProxyList().add(proxyConfig);
        server.start();

        // jdbc:mysql://scuritytest.mysql.rds.aliyuncs.com:3306/mysql
        Driver driver = new com.mysql.jdbc.Driver();
        Properties info = new Properties();
        info.put("user", "sonar");
        info.put("password", "sonar");
        info.put("useServerPrepStmts", "true");
        Connection conn = driver.connect("jdbc:mysql://127.0.0.1:3306/sonar", info);

        {
            String sql = "select * from users where id = 1 OR 1 = 1";
            // String sql = "select * from usersxx";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            JdbcUtils.printResultSet(rs);
            rs.close();
            stmt.close();
        }

        conn.close();
    }
}
