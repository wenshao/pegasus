package com.alibaba.sqlwall.test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.sqlwall.mysql.MySqlProxyServer;

public class MySqlConnectTest extends TestCase {

    public void test_connect() throws Exception {
        MySqlProxyServer server = new MySqlProxyServer("hbase-01", 3306);
        server.start();

        // jdbc:mysql://scuritytest.mysql.rds.aliyuncs.com:3306/mysql
        Driver driver = new com.mysql.jdbc.Driver();
        Properties info = new Properties();
        info.put("user", "sonar");
        info.put("password", "sonar");
        info.put("useServerPrepStmts", "true");
        Connection conn = driver.connect("jdbc:mysql://127.0.0.1:3306/sonar", info);

        {
            PreparedStatement stmt = conn.prepareStatement("SELECT ?");
            stmt.setString(1, "xxx");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

            }
            rs.close();
            stmt.close();
        }
        {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from users");
            JdbcUtils.printResultSet(rs);
            rs.close();
            stmt.close();
        }
        {
            try {
                conn.prepareStatement("select * from xx where fid = ?");
            } catch (Exception e) {

            }
        }
        conn.setAutoCommit(false);
        {
            Statement stmt = conn.createStatement();
            try {
                stmt.executeQuery("select * from xx");
            } catch (Exception e) {

            }
            stmt.close();
        }
        conn.commit();
        conn.rollback();
        
        {
            PreparedStatement stmt = conn.prepareStatement("SELECT 1, ?, ?");
            stmt.setString(1, "xxx1");
            stmt.setString(2, "xxx2");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

            }
            rs.close();
            stmt.close();
        }

        conn.close();
    }
}
