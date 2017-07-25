package com.rakuten.rit.roma.romac4j;

import junit.framework.Test;
import junit.framework.TestCase;

public class NodeArrayConstractorTest extends TestCase {
    private void checkSetGet(String[] nodeIds) throws Exception {
        RomaClient rc = null;

        try {
            rc = new RomaClient(nodeIds);

            assertTrue(rc.set("key", "test", 0));
            assertEquals("test", rc.getString("key"));
        } finally {
            if(rc != null) {
                rc.destroy();
            }
        }
    }

    public void testAliveNodes() throws Exception {
        checkSetGet(new String[]{"localhost_11211", "localhost_11311"});
    }

    public void testFirstNodeIsDead() throws Exception {
        checkSetGet(new String[]{"localhost_11111", "localhost_11211"});
    }

    public void testSecondNodeIsDead() throws Exception {
        checkSetGet(new String[]{"localhost_11211", "localhost_11311"});
    }
}
