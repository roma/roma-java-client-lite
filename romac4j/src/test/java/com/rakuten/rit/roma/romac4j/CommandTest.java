package com.rakuten.rit.roma.romac4j;

import java.math.BigInteger;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CommandTest extends TestCase {
    static RomaClient rc = null;

    static void oneTimeSetUp() throws Exception {
        rc = new RomaClient("localhost_11311");
    }

    static void oneTimeTearDown() throws Exception {
        rc.destroy();
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(CommandTest.class);
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

    public void testSetGet01() throws Exception {
        String value = null;
        
        for(int i = 0;i < 10;i++){
            assertTrue(rc.set("key" + i, ("val" + i).getBytes(), 0));
            value = new String(rc.get("key" + i));
            assertEquals("val" + i, value);
        }

        assertTrue(rc.set("key1", "".getBytes(), 0));
        value = new String(rc.get("key1"));
        assertEquals("", value);        
    }

    public void testSetGet02() throws Exception {
        for(int i = 0;i < 10;i++){
            assertTrue(rc.set("key" + i, "val" + i, 0));
            assertEquals("val" + i, rc.getString("key" + i));
        }

        assertTrue(rc.set("key1", "", 0));
        assertEquals("", rc.getString("key1"));        
    }

    public void testSetGets01() throws Exception {
        String[] keys = new String[10];
        for(int i = 0;i < 10; i++){
            keys[i] = "key" + i; 
            assertEquals(null, rc.getString(keys[i]));
            assertTrue(rc.set(keys[i], "val" + i, 0));
        }
        Map<String, String> m = rc.getsString(keys);
        assertEquals(10, m.size());
        for(int i = 0;i < 10; i++){
            assertEquals("val" + i, m.get(keys[i]));
        }
        
        m = rc.getsString(new String[]{"key10", "key11", "key12"});
        assertEquals(0, m.size());
        m = rc.getsString(new String[]{"key0", "key11", "key12"});
        assertEquals(1, m.size());
        m = rc.getsString(new String[]{"key0", "key1", "key12"});
        assertEquals(2, m.size());
        m = rc.getsString(new String[]{"key0", "key1", "key2"});
        assertEquals(3, m.size());
    }
    
    public void testAds01() throws Exception {
        assertNull(rc.getString("key1"));
        assertTrue(rc.add("key1", "1", 0));
        assertEquals("1", rc.getString("key1"));
        assertFalse(rc.add("key1", "2", 0));
        assertEquals("1", rc.getString("key1"));
        assertTrue(rc.delete("key1"));
        assertTrue(rc.add("key1", "2", 1));
        assertEquals("2", rc.getString("key1"));
        Thread.sleep(2000);
        assertNull(rc.getString("key1"));
        assertTrue(rc.add("key1", "3", 0));
        assertEquals("3", rc.getString("key1"));        
    }
    
    public void testReplace01() throws Exception {
        assertNull(rc.getString("key1"));
        assertFalse(rc.replace("key1", "0", 0));
        assertNull(rc.getString("key1"));
        assertTrue(rc.set("key1", "0", 0));
        assertEquals("0", rc.getString("key1"));
        assertTrue(rc.replace("key1", "1", 0));
        assertEquals("1", rc.getString("key1"));        
    }
    
    public void testAppend01() throws Exception {
        assertNull(rc.getString("key1"));
        assertFalse(rc.append("key1", "0", 0));

        assertTrue(rc.set("key1", "01", 0));
        assertTrue(rc.append("key1", "02", 0));
        assertTrue(rc.append("key1", "03", 0));
        assertTrue(rc.append("key1", "04", 0));
        assertEquals("01020304", rc.getString("key1"));
    }
    
    public void testPrepend01() throws Exception {
        assertNull(rc.getString("key1"));
        assertFalse(rc.prepend("key1", "0", 0));
        
        assertTrue(rc.set("key1", "01", 0));
        assertTrue(rc.prepend("key1", "02", 0));
        assertTrue(rc.prepend("key1", "03", 0));
        assertTrue(rc.prepend("key1", "04", 0));
        assertEquals("04030201", rc.getString("key1"));
    }
    
    public void testIncr01() throws Exception {
        assertNull(rc.getString("key1"));
        assertNull(rc.incr("key1", 1));
        
        assertTrue(rc.set("key1", "1", 0));
        assertEquals(2, (long)rc.incr("key1", 1));
        assertEquals("2", rc.getString("key1"));
        assertEquals(6, (long)rc.incr("key1", 4));

        assertTrue(rc.set("key1", "A", 0));
        assertEquals(4, (long)rc.incr("key1", 4));
    }
    
    public void testIncr02() throws Exception {
        BigInteger i = new BigInteger("fffffffffffffffe", 16);
        
        assertTrue(rc.set("key1", i.toString(), 0));
        BigInteger ret = rc.incrBigInt("key1", 1);
        assertTrue(ret.equals(new BigInteger("ffffffffffffffff", 16)));
        assertEquals(0, (long)rc.incr("key1", 1));
    }
    
    public void testDecr01() throws Exception {
        assertNull(rc.getString("key1"));
        assertNull(rc.decr("key1", 1));
        
        assertTrue(rc.set("key1", "10", 0));
        assertEquals(9, (long)rc.decr("key1", 1));
        assertEquals("9", rc.getString("key1"));
        assertEquals(5, (long)rc.decr("key1", 4));
        assertEquals(0, (long)rc.decr("key1", 10));

        assertTrue(rc.set("key1", "A", 0));
        assertEquals(0, (long)rc.decr("key1", 4));
    }

    public void testDecr02() throws Exception {
        BigInteger i = new BigInteger("ffffffffffffffff", 16);
        
        assertTrue(rc.set("key1", i.toString(), 0));
        BigInteger ret = rc.decrBigInt("key1", 1);
        assertTrue(ret.equals(new BigInteger("fffffffffffffffe", 16)));        
    }

    public void testDelete01() throws Exception {
        assertNull(rc.getString("key1"));
        assertFalse(rc.delete("key1"));
        
        assertTrue(rc.set("key1", "10", 0));
        assertEquals("10", rc.getString("key1"));
        assertTrue(rc.delete("key1"));
        assertNull(rc.getString("key1"));
    }
    
    public void testSetExpt01() throws Exception {
        assertFalse(rc.setExpt("key1", 1));
        assertTrue(rc.set("key1", "10", 0));
        assertEquals("10", rc.getString("key1"));
        assertTrue(rc.setExpt("key1", 1));
        assertEquals("10", rc.getString("key1"));
        Thread.sleep(2000);
        assertNull(rc.getString("key1"));
    }
    
    public void testCas01() throws Exception {
        assertTrue(rc.set("key1", "1", 0));
        assertEquals("1", rc.getString("key1"));
        boolean res = rc.cas("key1", 0, new Cas(null) {
            public byte[] cas(ValueReceiver rcv) {
                int n = Integer.parseInt(rcv.getValueString());
                String ret = "" + (n + 1);
                return ret.getBytes();
            }
        });
        assertTrue(res);
        assertEquals("2", rc.getString("key1"));
    }

    public void testCas02() throws Exception {
        assertEquals(null, rc.getString("key1"));   // not exist key
        boolean res = rc.cas("key1", 0, new Cas(null) {
            public byte[] cas(ValueReceiver rcv) {
                int n = Integer.parseInt(rcv.getValueString());
                String ret = "" + (n + 1);
                return ret.getBytes();
            }
        });
        assertFalse(res);
    }
}
