package com.alibaba.pegasus.listener;

import com.alibaba.pegasus.ProxySession;


public interface ExecuteBeforeListener {
    boolean executeBefore(ProxySession session, String sql);
}
