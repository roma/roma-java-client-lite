package com.rakuten.rit.roma.romac4j;

import junit.framework.Test;
import junit.framework.TestCase;

import java.lang.Thread;

import com.rakuten.rit.roma.romac4j.connection.Connection;
import com.rakuten.rit.roma.romac4j.RomaClient;

/**
 * This is test class when pooled socked is already closed.
 *
 * If client keep ROMA conection long time, ROMA server close connectoin
 * when socket is opened more than expired time.
 *
 * So this is test case that make sure to run properly even if socket is
 * closed by server side.
 */
public class ClosedSocketTest extends TestCase {
    class TestRomaClient extends RomaClient {
        public TestRomaClient(String node) {
            super(node);
        }

        // emulate that socket is closed by server side
        public void closeSocketForTest() throws Exception {
            while(routing == null) {
                Thread.yield();
            }

            // routing
            Connection con = routing.getConnection("localhost_11311");
            con.close();
            routing.returnConnection(con);
            con = routing.getConnection("localhost_11411");
            con.close();
            routing.returnConnection(con);
        }
    }

    public void testClosedSocket() throws Exception {
        TestRomaClient rc = null;

        try {
            rc = new TestRomaClient("localhost_11311");
            rc.closeSocketForTest();

            assertTrue(rc.set("key", "", 0));
            assertEquals("", rc.getString("key"));
            assertTrue(rc.set("key", "test", 0));
            assertEquals("test", rc.getString("key"));
        } finally {
            if(rc != null) {
                rc.destroy();
            }
        }
    }
}
