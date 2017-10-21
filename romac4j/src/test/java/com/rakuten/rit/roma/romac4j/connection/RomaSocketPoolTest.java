package com.rakuten.rit.roma.romac4j.connection;

import junit.framework.TestCase;

/**
 * Created by yinchin.chen on 10/27/16.
 */
public class RomaSocketPoolTest extends TestCase {

    RomaSocketPool FIXTURE;
    String nodeId = "localhost_11211";

    public void setUp() {
        RomaSocketPool.init();
        FIXTURE = RomaSocketPool.getInstance();
    }

    public void tearDown(){
        FIXTURE.deleteConnection(nodeId);
    }

    public void testConnection() throws Exception {
        // get connection1 and return connection1
        Connection con1 = FIXTURE.getConnection(nodeId);
        FIXTURE.returnConnection(con1);

        // get connection2
        Connection con2 = FIXTURE.getConnection(nodeId);
        // con1 must equal con2
        assertEquals(con1, con2);

        // invalid con2
        FIXTURE.invalidateConnection(con2);
        // get con3
        Connection con3 = FIXTURE.getConnection(nodeId);
        assertNotSame(con2, con3);
        // test connection.
        assertTrue(con2.isClosed());
        assertTrue(con3.isConnected());
        FIXTURE.returnConnection(con3);
    }

    public void testGetConnections() throws Exception{
        Connection con1 = FIXTURE.getConnection(nodeId);
        Connection con2 = FIXTURE.getConnection(nodeId);
        Connection con3 = FIXTURE.getConnection(nodeId);
        Connection con4 = FIXTURE.getConnection(nodeId);
        FIXTURE.returnConnection(con1);
        FIXTURE.returnConnection(con2);
        FIXTURE.returnConnection(con3);
        FIXTURE.returnConnection(con4);
    }
}