package com.alibaba.sqlwall.test.nio;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import com.alibaba.pegasus.config.MySqlProxyConfig;
import com.alibaba.pegasus.net.MySqlProxy;

public class MySqlWallTest extends TestCase {

    public void test_connect() throws Exception {
        MySqlProxyConfig config = new MySqlProxyConfig(3306, "hbase-01", 3306);
        MySqlProxy proxy = new MySqlProxy(config);
        proxy.start();

        // jdbc:mysql://scuritytest.mysql.rds.aliyuncs.com:3306/mysql
        Driver driver = new com.mysql.jdbc.Driver();
        Properties info = new Properties();
        info.put("user", "sonar");
        info.put("password", "sonar");
        info.put("useServerPrepStmts", "true");

        Connection conn = driver.connect("jdbc:mysql://127.0.0.1:3306/sonar", info);

        for (int i = 0; i < 1; ++i) {
            // String sql = "select * from users where id = 1 OR 1 = 1";
            String sql = "select * from users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {

            }
            // JdbcUtils.printResultSet(rs);
            rs.close();
            stmt.close();
            if (i % 100 == 0) {
                System.out.println(i);
            }
        }

        conn.close();
    }
}
