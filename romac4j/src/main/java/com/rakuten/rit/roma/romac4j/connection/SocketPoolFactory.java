package com.rakuten.rit.roma.romac4j.connection;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.pool.PoolableObjectFactory;

public class SocketPoolFactory implements PoolableObjectFactory<Connection> {
    private String nodeId;
    private int bufferSize;
    private int timeout;

    public SocketPoolFactory(String nid, int bufferSize, int timeout) {
        this.nodeId = nid;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
    }

    public Connection makeObject() throws IOException {
        Connection con = new Connection(nodeId, bufferSize);
        String[] host = nodeId.split("_");
        con.connect(new InetSocketAddress(host[0], Integer.valueOf(host[1])), timeout);
        return con;
    }

    public void destroyObject(Connection con) throws Exception {
        con.close();
    }

    public boolean validateObject(Connection con) {
        return con.isConnected();
    }

    public void activateObject(Connection con) throws Exception {

    }

    public void passivateObject(Connection con) throws Exception {

    }
}
