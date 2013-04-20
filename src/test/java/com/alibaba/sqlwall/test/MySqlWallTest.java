package com.alibaba.sqlwall.test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.sqlwall.ProxyServer;

public class MySqlWallTest extends TestCase {

    public void test_connect() throws Exception {
        ProxyServer server = new ProxyServer("hbase-01", 3306);
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
//            String sql = "select * from usersxx";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            JdbcUtils.printResultSet(rs);
            rs.close();
            stmt.close();
        }

        conn.close();
    }
}
