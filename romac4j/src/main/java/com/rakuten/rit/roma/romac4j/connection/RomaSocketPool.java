package com.rakuten.rit.roma.romac4j.connection;

import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

public class RomaSocketPool {
    protected static Logger log = Logger.getLogger(RomaSocketPool.class
            .getName());
    private static RomaSocketPool instance = null;

    private RomaSocketPool() {
        poolMap = Collections.synchronizedMap(new HashMap<String, GenericObjectPool<Connection>>());
    }

    public static RomaSocketPool getInstance() {
        if(instance == null) {
            log.error("getInstance() : Pool has not been yet initialized.");
            throw new RuntimeException("Pool has not been yet initialized.");
        }
        return instance;
    }

    private Map<String, GenericObjectPool<Connection>> poolMap = null;
    private int maxActive = GenericObjectPool.DEFAULT_MAX_ACTIVE;
    private int maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;
    private int timeout = 1000;
    private int bufferSize = 1024;

    public static synchronized void init() {
        init(GenericObjectPool.DEFAULT_MAX_ACTIVE,
                GenericObjectPool.DEFAULT_MAX_IDLE, 1000, 1024);
    }
    
    public static synchronized void init(int maxActive, int maxIdle, int timeout, int bufferSize) {
        if(instance != null) {
            log.error("init() : init() was already called.");
            throw new RuntimeException("init() was already called.");
        }
        instance = new RomaSocketPool();
        instance.maxActive = maxActive;
        instance.maxIdle = maxIdle;
        instance.timeout = timeout;
        instance.bufferSize = bufferSize;
    }

    public synchronized Connection getConnection(String nodeId) throws Exception {
        GenericObjectPool<Connection> pool = poolMap.get(nodeId);
        if (pool == null) {
            PoolableObjectFactory<Connection> factory = 
                    new SocketPoolFactory(nodeId, bufferSize);
            pool = new GenericObjectPool<Connection>(factory);
            pool.setMaxActive(maxActive);
            pool.setMaxIdle(maxIdle);
            pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
            poolMap.put(nodeId, pool);
        }
        Connection con = pool.borrowObject();
        con.setSoTimeout(timeout);

        return con;
    }

    public void deleteConnection(String nodeId) {
        GenericObjectPool<Connection> pool = poolMap.get(nodeId);
        if (pool != null) {
            synchronized(pool) {
                pool.clear();
                poolMap.remove(nodeId);
            }
        }
    }

    public void invalidateConnection(Connection con) {
        GenericObjectPool<Connection> pool = poolMap.get(con.getNodeId());
        if (pool != null) {
            synchronized(pool) {
                if(poolMap.containsKey(con.getNodeId())) {
                    try {
                        pool.invalidateObject(con);
                        con.forceClose();
                        return;
                    } catch (Exception e) {
                        log.error("invalidateConnection() : " + e.getMessage());
                    }
                }
            }
        }
    }

    public void returnConnection(Connection con) {
        try {
            con.setSoTimeout(0);
        } catch (SocketException e) {
            log.warn("returnConnection() : " + e.getMessage());
            con.forceClose();
            return;
        }
        
        GenericObjectPool<Connection> pool = poolMap.get(con.getNodeId());
        if (pool != null) {
            synchronized(pool) {
                if(poolMap.containsKey(con.getNodeId())) {
                    try {
                        pool.returnObject(con);
                        return;
                    } catch (Exception e) {
                        log.error("returnConnection() : " + e.getMessage());
                    }
                }
            }
        }
        log.info("returnConnection() : close connection");
        con.forceClose();
    }
}
