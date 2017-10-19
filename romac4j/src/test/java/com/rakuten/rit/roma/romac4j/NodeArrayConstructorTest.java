package com.rakuten.rit.roma.romac4j;

import junit.framework.Test;
import junit.framework.TestCase;


/**
 * This is test case for nodeId array constructor.
 *
 * Some test case include dead(can't connect) node information.
 */
public class NodeArrayConstructorTest extends TestCase {
    private void checkSetGet(String[] nodeIds) throws Exception {
        RomaClient rc = null;

        try {
            rc = new RomaClient(nodeIds);

            assertTrue(rc.set("key", "test", 0));
            assertEquals("test", rc.getString("key"));
            rc.delete("key");
        } finally {
            if(rc != null) {
                rc.destroy();
            }
        }
    }

    public void testFirstNodeIsAlive() throws Exception {
        checkSetGet(new String[]{"localhost_11311", "localhost_11411"});
    }

    public void testFirstNodeIsDead() throws Exception {
        checkSetGet(new String[]{"localhost_11011", "localhost_11311"});
    }

    public void testSecondNodeIsDead() throws Exception {
        checkSetGet(new String[]{"localhost_11311", "localhost_11511"});
    }
}
