package com.alibaba.sqlwall.test;

import junit.framework.TestCase;

import com.alibaba.sqlwall.mysql.MySqlClient;

public class MySqlClientTest extends TestCase {
	public void test_client() throws Exception {
		MySqlClient client = new MySqlClient();
		
		client.getClass();
	}
}
