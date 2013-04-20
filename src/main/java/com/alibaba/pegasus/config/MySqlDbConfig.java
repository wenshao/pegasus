package com.alibaba.pegasus.config;

public class MySqlDbConfig extends DbConfig {

    public MySqlDbConfig(){
        super();
    }

    public MySqlDbConfig(String host, int port){
        super(host, port);
    }
    
}
