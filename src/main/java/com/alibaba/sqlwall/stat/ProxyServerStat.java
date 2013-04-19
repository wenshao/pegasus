package com.alibaba.sqlwall.stat;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alibaba.druid.stat.JdbcSqlStat;
import com.alibaba.druid.util.LRUCache;

public class ProxyServerStat {

    private ReentrantReadWriteLock                   lock                  = new ReentrantReadWriteLock();
    public int                                       maxSize               = 1000 * 1;
    private final LinkedHashMap<String, JdbcSqlStat> sqlStatMap;

    private final AtomicLong                         clobOpenCount         = new AtomicLong();

    private final AtomicLong                         blobOpenCount         = new AtomicLong();

    private final String                             dbType;

    private final AtomicInteger                      activeConnectionCount = new AtomicInteger();

    public ProxyServerStat(String dbType){
        sqlStatMap = new LRUCache<String, JdbcSqlStat>(maxSize, 16, 0.75f, false);
        this.dbType = dbType;
    }

    public void incrementActiveConnectionCount() {
        activeConnectionCount.incrementAndGet();
    }

    public void decrementActiveConnectionCount() {
        activeConnectionCount.decrementAndGet();
    }

    public void reset() {
        blobOpenCount.set(0);
        clobOpenCount.set(0);

        lock.writeLock().lock();
        try {
            Iterator<Map.Entry<String, JdbcSqlStat>> iter = sqlStatMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, JdbcSqlStat> entry = iter.next();
                JdbcSqlStat stat = entry.getValue();
                if (stat.getExecuteCount() == 0 && stat.getRunningCount() == 0) {
                    stat.setRemoved(true);
                    iter.remove();
                } else {
                    stat.reset();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public JdbcSqlStat getSqlStat(int id) {
        return getSqlStat((long) id);
    }

    public JdbcSqlStat getSqlStat(long id) {
        lock.readLock().lock();
        try {
            for (Map.Entry<String, JdbcSqlStat> entry : this.sqlStatMap.entrySet()) {
                if (entry.getValue().getId() == id) {
                    return entry.getValue();
                }
            }

            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public JdbcSqlStat createSqlStat(String sql) {
        lock.writeLock().lock();
        try {
            JdbcSqlStat sqlStat = sqlStatMap.get(sql);
            if (sqlStat == null) {
                sqlStat = new JdbcSqlStat(sql);
                sqlStat.setDbType(this.dbType);
                sqlStatMap.put(sql, sqlStat);
            }

            return sqlStat;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
