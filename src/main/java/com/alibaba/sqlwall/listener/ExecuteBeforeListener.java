package com.alibaba.sqlwall.listener;

import com.alibaba.sqlwall.ProxySession;


public interface ExecuteBeforeListener {
    boolean executeBefore(ProxySession session, String sql);
}
