package com.rakuten.rit.roma.romac4j;

import java.util.Date;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AListPluginTest extends TestCase {
    static RomaClient rc = null;
    static AListPluginClient ac = null;

    static void oneTimeSetUp() throws Exception {
        rc = new RomaClient("localhost_11311");
        ac = new AListPluginClient(rc);
    }

    static void oneTimeTearDown() throws Exception {
        rc.destroy();
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AListPluginTest.class);
        TestSetup wrapper = new TestSetup(suite) {
            public void setUp() throws Exception {
                oneTimeSetUp();
            }

            public void tearDown() throws Exception {
                oneTimeTearDown();
            }
        };
        return wrapper;
    }

    protected void setUp() throws Exception {
        for(int i = 0;i < 100; i++) rc.delete("key" + i);
    }
    
    protected void tearDown() throws Exception {
        for(int i = 0;i < 100; i++) rc.delete("key" + i);
    }

    public void testPushGets01() throws Exception {
        String[] res;
        assertEquals(0, ac.getsString("key1").length); // the key dose't exist
        
        assertTrue(ac.push("key1", "0")); // first data
        res = ac.getsString("key1");
        assertEquals(1, res.length);
        assertEquals("0", res[0]);
        
        for(int i = 0;i < 9; i++){
            assertTrue(ac.push("key1", "" + (i + 1))); // add data
        }
        res = ac.getsString("key1");
        assertEquals(10, res.length);
        for(int i = 0;i < 10; i++){
            assertEquals("" + i, res[i]);
        }
    }
    
    public void testGetsWithTime() throws Exception {
        assertEquals(0, ac.getsWithTime("key1").length); // the key dose't exist
        Date rightNow = new Date();
        long epochtime = rightNow.getTime() / 1000;
        
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
        }

        AListPluginClient.VauleTime[] vt = ac.getsWithTime("key1");
        assertEquals(10, vt.length);
        for(int i = 0;i < 10; i++){
            assertTrue(Math.abs(vt[i].getEpochTime() - epochtime) < 86400); // less than a day
            assertEquals("" + i, vt[i].getValueString());
        }
    }
    
    public void testAt01() throws Exception {
        for(int i = 0;i < 10; i++){
            assertNull(ac.atString("key1", i)); // the key dose't exist
        }
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
        }
        for(int i = 0;i < 10; i++){
            assertEquals("" + i, ac.atString("key1", i));
        }
        assertNull(ac.atString("key1", 11)); // out of range
        assertEquals("9" ,ac.atString("key1", -1)); // returns last
        assertEquals("8" ,ac.atString("key1", -2)); // returns second to last
        assertEquals("7" ,ac.atString("key1", -3)); // returns third to last
    }
    
    public void testClearEmpty01() throws Exception {
        assertEquals(2, ac.isEmpty("key1")); // 2:the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
        }
        assertEquals(0, ac.isEmpty("key1")); // 0:not empty
        assertTrue(ac.clear("key1"));
        assertEquals(1, ac.isEmpty("key1")); // 1:empty
    }
    
    public void testClearEmpty02() throws Exception {
        assertEquals(2, ac.isEmpty("key1")); // 2: the key dose't exist
        assertFalse(ac.clear("key1")); // returns false when the key dose't exist
    }
    
    public void testIsInclude01() throws Exception {
        assertEquals(-2, ac.isInclude("key1", "0"));  // -2 : the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
        }
        for(int i = 0;i < 10; i++){
            assertEquals(1, ac.isInclude("key1", "" + i));  // 1 : true
            assertEquals(0, ac.isInclude("key1", "a" + i));  // 0 : false
        }
    }
    
    public void testIndex01() throws Exception {
        assertEquals(-2, ac.index("key1", "0"));  // -2 : the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
        }
        for(int i = 0;i < 10; i++){
            assertEquals(i, ac.index("key1", "" + i));
        }
        assertEquals(-3, ac.index("key1", "a0"));  // -3 : The value does not exist.
    }
    
    public void testInsert01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        assertTrue(ac.insert("key1", 0, "1"));
        assertEquals("1", ac.join("key1"));
        assertTrue(ac.insert("key1", 0, "0"));
        assertEquals("0,1", ac.join("key1"));
        assertTrue(ac.insert("key1", 5, "5"));        
        assertEquals("0,1,,,,5", ac.join("key1"));
        assertTrue(ac.insert("key1", 3, "3"));        
        assertEquals("0,1,,3,,,5", ac.join("key1"));
        
        assertEquals("0", ac.atString("key1", 0));
        assertEquals("1", ac.atString("key1", 1));
        assertEquals("", ac.atString("key1", 2));
        assertEquals("3", ac.atString("key1", 3));
    }
    
    public void testSizedInsert01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        String res = "";
        for(int i = 0;i < 10; i++){
            assertTrue(ac.sizedInsert("key1", 0, "" + i));  // unlimited
            if(i == 0) res = "" + i;
            else       res = i + "," + res;
            assertEquals(res, ac.join("key1"));
        }
        for(int i = 0;i < 5; i++){
            assertTrue(ac.sizedInsert("key2", 5, "" + i));  // list size = 5
            if(i == 0) res = "" + i;
            else       res = i + "," + res;
            assertEquals(res, ac.join("key2"));
        }
        assertTrue(ac.sizedInsert("key2", 5, "5"));
        assertEquals("5,4,3,2,1", ac.join("key2"));
        assertTrue(ac.sizedInsert("key2", 5, "6"));
        assertEquals("6,5,4,3,2", ac.join("key2"));
        assertTrue(ac.sizedInsert("key2", 5, "7"));
        assertEquals("7,6,5,4,3", ac.join("key2"));
        assertTrue(ac.sizedInsert("key2", 5, "8"));
        assertEquals("8,7,6,5,4", ac.join("key2"));
        assertTrue(ac.sizedInsert("key2", 5, "9"));
        assertEquals("9,8,7,6,5", ac.join("key2"));
    }
    
    public void testSwapAndInsert01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.swapAndInsert("key1", "" + i));
        }
        assertEquals("9,8,7,6,5,4,3,2,1,0", ac.join("key1"));
        assertTrue(ac.swapAndInsert("key1", "0"));
        assertEquals("0,9,8,7,6,5,4,3,2,1", ac.join("key1"));
        assertTrue(ac.swapAndInsert("key1", "5"));
        assertEquals("5,0,9,8,7,6,4,3,2,1", ac.join("key1"));
        assertTrue(ac.swapAndInsert("key1", "5"));
        assertEquals("5,0,9,8,7,6,4,3,2,1", ac.join("key1"));        
    }
    
    public void testSwapAndSizedInsert01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        String res = "";
        for(int i = 0;i < 10; i++){
            assertTrue(ac.swapAndSizedInsert("key1", 0, "" + i));  // unlimited
            if(i == 0) res = "" + i;
            else       res = i + "," + res;
            assertEquals(res, ac.join("key1"));
        }
        assertTrue(ac.swapAndSizedInsert("key1", 5, "0"));  // unlimited
        assertEquals("0,9,8,7,6", ac.join("key1"));
        assertTrue(ac.swapAndSizedInsert("key1", 5, "7"));  // unlimited
        assertEquals("7,0,9,8,6", ac.join("key1"));
        assertTrue(ac.swapAndSizedInsert("key1", 5, "1"));  // unlimited
        assertEquals("1,7,0,9,8", ac.join("key1"));
        assertTrue(ac.swapAndSizedInsert("key1", 5, "1"));  // unlimited
        assertEquals("1,7,0,9,8", ac.join("key1"));
    }    
    
    public void testExpiredSwapAndInsert01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        assertTrue(ac.push("key1", "0"));
        assertTrue(ac.expiredSwapAndInsert("key1", 10, "1"));
        assertEquals("1,0", ac.join("key1"));
        assertTrue(ac.expiredSwapAndInsert("key1", 0, "2"));
        assertEquals("2", ac.join("key1"));
    }

    public void testExpiredSwapAndInsert02() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndInsert("key1", 0, "" + i));
            assertEquals("" + i, ac.join("key1"));
        }
    }

    public void testExpiredSwapAndInsert03() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndInsert("key1", 10, "" + i));
        }
        assertEquals("9,8,7,6,5,4,3,2,1,0", ac.join("key1"));
    }

    public void testExpiredSwapAndInsert04() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndInsert("key1", 10, "" + i));
        }
        assertEquals("9,8,7,6,5,4,3,2,1,0", ac.join("key1"));
        
        assertTrue(ac.expiredSwapAndInsert("key1", 10, "0"));
        assertEquals("0,9,8,7,6,5,4,3,2,1", ac.join("key1"));
        assertTrue(ac.expiredSwapAndInsert("key1", 10, "7"));
        assertEquals("7,0,9,8,6,5,4,3,2,1", ac.join("key1"));
        assertTrue(ac.expiredSwapAndInsert("key1", 10, "1"));
        assertEquals("1,7,0,9,8,6,5,4,3,2", ac.join("key1"));
        assertTrue(ac.expiredSwapAndInsert("key1", 10, "1"));
        assertEquals("1,7,0,9,8,6,5,4,3,2", ac.join("key1"));
    }

    public void testExpiredSwapAndSizedInsert01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        assertTrue(ac.push("key1", "0"));
        assertTrue(ac.expiredSwapAndSizedInsert("key1", 10, 5, "1"));
        assertEquals("1,0", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedInsert("key1", 0, 5, "2"));
        assertEquals("2", ac.join("key1"));
    }

    public void testExpiredSwapAndSizedInsert02() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndSizedInsert("key1", 0, 5, "" + i));
            assertEquals("" + i, ac.join("key1"));
        }
    }

    public void testExpiredSwapAndSizedInsert03() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndSizedInsert("key1", 10, 10, "" + i));
        }
        assertEquals("9,8,7,6,5,4,3,2,1,0", ac.join("key1"));
    }
    
    public void testExpiredSwapAndSizedInsert04() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndSizedInsert("key1", 10, 10, "" + i));
        }
        assertEquals("9,8,7,6,5,4,3,2,1,0", ac.join("key1"));
        
        assertTrue(ac.expiredSwapAndSizedInsert("key1", 10, 5, "0"));
        assertEquals("0,9,8,7,6", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedInsert("key1", 10, 5, "7"));
        assertEquals("7,0,9,8,6", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedInsert("key1", 10, 5, "1"));
        assertEquals("1,7,0,9,8", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedInsert("key1", 10, 5, "1"));
        assertEquals("1,7,0,9,8", ac.join("key1"));
    }
    
    public void testJoin01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        assertTrue(ac.push("key1", "0")); // add data
        assertEquals("0", ac.join("key1"));
        for(int i = 0;i < 9; i++){
            assertTrue(ac.push("key1", "" + (i + 1))); // add data
        }
        assertEquals("0,1,2,3,4,5,6,7,8,9", ac.join("key1"));
        assertEquals("0|1|2|3|4|5|6|7|8|9", ac.join("key1", "|"));
    }
 
    public void testToS01() throws Exception {
        assertNull(ac.toS("key1")); // the key dose't exist
        assertTrue(ac.push("key1", "0")); // add data
        assertEquals("[\"0\"]", ac.toS("key1"));
        for(int i = 0;i < 9; i++){
            assertTrue(ac.push("key1", "" + (i + 1))); // add data
        }
        assertEquals("[\"0\", \"1\", \"2\", \"3\", \"4\", \"5\", \"6\", \"7\", \"8\", \"9\"]", ac.toS("key1"));
    }

    public void testDelete01() throws Exception {
        assertFalse(ac.delete("key1", "1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
        }
        assertFalse(ac.delete("key1", "10")); // the value dose't exist
        assertEquals("0,1,2,3,4,5,6,7,8,9", ac.join("key1"));

        assertTrue(ac.delete("key1", "9"));
        assertEquals("0,1,2,3,4,5,6,7,8", ac.join("key1"));

        assertTrue(ac.delete("key1", "5"));
        assertEquals("0,1,2,3,4,6,7,8", ac.join("key1"));
    }
    
    public void testDeleteAt01() throws Exception {
        assertFalse(ac.deleteAt("key1", 1)); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
        }
        assertFalse(ac.deleteAt("key1", 10)); // out of range
        
        assertTrue(ac.deleteAt("key1", 3));
        assertEquals("0,1,2,4,5,6,7,8,9", ac.join("key1"));
        
        assertTrue(ac.deleteAt("key1", -1)); // delete for second to last
        assertEquals("0,1,2,4,5,6,7,8", ac.join("key1"));
    
        assertTrue(ac.deleteAt("key1", -2)); // delete for third to last
        assertEquals("0,1,2,4,5,6,8", ac.join("key1"));
    }

    public void testFirstLast01() throws Exception {
        assertNull(ac.firstString("key1")); // the key dose't exist
        assertNull(ac.lastString("key1")); // the key dose't exist

        assertTrue(ac.push("key1", "0")); // add data
        assertEquals("0", ac.firstString("key1"));
        assertEquals("0", ac.lastString("key1"));

        assertTrue(ac.push("key1", "1")); // add data
        assertEquals("0", ac.firstString("key1"));
        assertEquals("1", ac.lastString("key1"));
        
        assertTrue(ac.push("key1", "2")); // add data
        assertEquals("0", ac.firstString("key1"));
        assertEquals("2", ac.lastString("key1"));
    }
    
    public void testLength01() throws Exception {
        assertEquals(-2, ac.length("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i)); // add data
            assertEquals(i + 1, ac.length("key1"));
        }
        assertTrue(ac.clear("key1"));
        assertEquals(0, ac.length("key1"));        
    }
    
    public void testPop01() throws Exception {
        assertNull(ac.popString("key1"));
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i));
        }
        for(int i = 9;i >= 0; i--){
            assertEquals("" + i, ac.popString("key1"));
        }
        assertNull(ac.popString("key1"));
    }
    
    public void testSizedPush01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        String res = "";
        for(int i = 0;i < 10; i++){
            assertTrue(ac.sizedPush("key1", 10, "" + i));
            if(i == 0) res = "" + i;
            else       res += "," + i;
            assertEquals(res, ac.join("key1"));
        }
        for(int i = 0;i < 10; i++){
            assertFalse(ac.sizedPush("key1", 10, "" + i));
            assertEquals(res, ac.join("key1"));
        }
        assertTrue(ac.deleteAt("key1", 0));
        assertEquals("1,2,3,4,5,6,7,8,9", ac.join("key1"));
        assertTrue(ac.sizedPush("key1", 10, "10"));
        assertEquals("1,2,3,4,5,6,7,8,9,10", ac.join("key1"));
    }

    public void testSwapAndPush01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.swapAndPush("key1", "" + i));
        }
        assertEquals("0,1,2,3,4,5,6,7,8,9", ac.join("key1"));
        assertTrue(ac.swapAndPush("key1", "0"));
        assertEquals("1,2,3,4,5,6,7,8,9,0", ac.join("key1"));
        assertTrue(ac.swapAndPush("key1", "5"));
        assertEquals("1,2,3,4,6,7,8,9,0,5", ac.join("key1"));
        assertTrue(ac.swapAndPush("key1", "5"));
        assertEquals("1,2,3,4,6,7,8,9,0,5", ac.join("key1"));        
    }
    
    public void testSwapAndSizedPush01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        String res = "";
        for(int i = 0;i < 10; i++){
            assertTrue(ac.swapAndSizedPush("key1", 10, "" + i));
            if(i == 0) res = "" + i;
            else       res += "," + i;
            assertEquals(res, ac.join("key1"));
        }
        for(int i = 0;i < 10; i++){
            assertFalse(ac.swapAndSizedPush("key1", 10, "" + (i + 10)));
            assertEquals(res, ac.join("key1"));
        }
        assertTrue(ac.deleteAt("key1", 0));
        assertEquals("1,2,3,4,5,6,7,8,9", ac.join("key1"));
        assertTrue(ac.swapAndSizedPush("key1", 10, "10"));
        assertEquals("1,2,3,4,5,6,7,8,9,10", ac.join("key1"));

        assertTrue(ac.swapAndSizedPush("key1", 10, "5"));
        assertEquals("1,2,3,4,6,7,8,9,10,5", ac.join("key1"));
        assertTrue(ac.swapAndSizedPush("key1", 10, "6"));
        assertEquals("1,2,3,4,7,8,9,10,5,6", ac.join("key1"));
        assertTrue(ac.swapAndSizedPush("key1", 10, "6"));
        assertEquals("1,2,3,4,7,8,9,10,5,6", ac.join("key1"));
    }
    
    public void testExpiredSwapAndPush01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        assertTrue(ac.push("key1", "0"));
        assertTrue(ac.expiredSwapAndPush("key1", 10, "1"));
        assertEquals("0,1", ac.join("key1"));
        assertTrue(ac.expiredSwapAndPush("key1", 0, "2"));
        assertEquals("2", ac.join("key1"));
    }
    
    public void testExpiredSwapAndPush02() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndPush("key1", 0, "" + i));
            assertEquals("" + i, ac.join("key1"));
        }
    }

    public void testExpiredSwapAndPush03() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndPush("key1", 10, "" + i));
        }
        assertEquals("0,1,2,3,4,5,6,7,8,9", ac.join("key1"));
    }

    public void testExpiredSwapAndPush04() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndPush("key1", 10, "" + i));
        }
        assertEquals("0,1,2,3,4,5,6,7,8,9", ac.join("key1"));
        
        assertTrue(ac.expiredSwapAndPush("key1", 10, "0"));
        assertEquals("1,2,3,4,5,6,7,8,9,0", ac.join("key1"));
        assertTrue(ac.expiredSwapAndPush("key1", 10, "7"));
        assertEquals("1,2,3,4,5,6,8,9,0,7", ac.join("key1"));
        assertTrue(ac.expiredSwapAndPush("key1", 10, "1"));
        assertEquals("2,3,4,5,6,8,9,0,7,1", ac.join("key1"));
        assertTrue(ac.expiredSwapAndPush("key1", 10, "1"));
        assertEquals("2,3,4,5,6,8,9,0,7,1", ac.join("key1"));
    }
    
    public void testExpiredSwapAndSizedPush01() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        assertTrue(ac.push("key1", "0"));
        assertTrue(ac.expiredSwapAndSizedPush("key1", 10, 5, "1"));
        assertEquals("0,1", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedPush("key1", 0, 5, "2"));
        assertEquals("2", ac.join("key1"));
    }
    
    public void testExpiredSwapAndSizedPush02() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 10; i++){
            assertTrue(ac.expiredSwapAndSizedPush("key1", 0, 5, "" + i));
            assertEquals("" + i, ac.join("key1"));
        }
    }

    public void testExpiredSwapAndSizedPush03() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 5; i++){
            assertTrue(ac.expiredSwapAndSizedPush("key1", 10, 5, "" + i));
        }
        for(int i = 0;i < 5; i++){
            assertFalse(ac.expiredSwapAndSizedPush("key1", 10, 5, "" + (i + 5)));
        }
        assertEquals("0,1,2,3,4", ac.join("key1"));
    }

    public void testExpiredSwapAndSizePush04() throws Exception {
        assertNull(ac.join("key1")); // the key dose't exist
        for(int i = 0;i < 5; i++){
            assertTrue(ac.expiredSwapAndSizedPush("key1", 10, 5, "" + i));
        }
        assertEquals("0,1,2,3,4", ac.join("key1"));

        assertTrue(ac.expiredSwapAndSizedPush("key1", 10, 5, "0"));
        assertEquals("1,2,3,4,0", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedPush("key1", 10, 5, "2"));
        assertEquals("1,3,4,0,2", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedPush("key1", 10, 5, "1"));
        assertEquals("3,4,0,2,1", ac.join("key1"));
        assertTrue(ac.expiredSwapAndSizedPush("key1", 10, 5, "1"));
        assertEquals("3,4,0,2,1", ac.join("key1"));
    }

    public void testUpdateAt01() throws Exception {
        assertEquals(-2, ac.updateAt("key1", 0, "0"));  // NOT_FOUND
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i));
        }
        assertEquals("0,1,2,3,4,5,6,7,8,9", ac.join("key1"));
        assertEquals(1, ac.updateAt("key1", 0, "10")); // STORED
        assertEquals("10,1,2,3,4,5,6,7,8,9", ac.join("key1"));
        assertEquals(-2, ac.updateAt("key1", 20, "0"));  // NOT_FOUND
        assertEquals(-2, ac.updateAt("key1", -1, "20")); // NOT_FOUND
    }
    
    public void testShift01() throws Exception {
        assertNull(ac.popString("key1"));
        for(int i = 0;i < 10; i++){
            assertTrue(ac.push("key1", "" + i));
        }
        for(int i = 0;i < 10; i++){
            assertEquals("" + i, ac.shiftString("key1"));
        }
        assertNull(ac.shiftString("key1"));
    }
}
