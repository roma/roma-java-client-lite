package com.rakuten.rit.roma.romac4j;

import java.util.ArrayList;
import java.util.HashMap;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MapcountPluginTest extends TestCase {
    static RomaClient rc = null;
    static MapcountPluginClient mcc = null;

    static void oneTimeSetUp() throws Exception {
        rc = new RomaClient("localhost_11311");
        mcc = new MapcountPluginClient(rc);
    }

    static void oneTimeTearDown() throws Exception {
        rc.destroy();
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(MapcountPluginTest.class);
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
        for (int i = 0; i < 100; i++)
            rc.delete("key" + i);
    }

    protected void tearDown() throws Exception {
        for (int i = 0; i < 100; i++)
            rc.delete("key" + i);
    }

    public void testCountup01() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        keys.put("bb", 1);
        keys.put("ccc", 1);
        HashMap<String, Object> result = mcc.countup("key1", keys);
        assertEquals(4, result.size());
        assertEquals(1, result.get("a"));
        assertEquals(1, result.get("bb"));
        assertEquals(1, result.get("ccc"));
    }

    public void testCountup02() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        HashMap<String, Object> result = null;
        for (int i = 0; i < 100; i++) {
            result = mcc.countup("key2", keys);
        }
        assertEquals(2, result.size());
        assertEquals(100, result.get("a"));
    }

    public void testCountup03() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 3);
        HashMap<String, Object> result = mcc.countup("key3", keys);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf("3"), result.get("a"));
    }

    public void testCountup04() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        keys.put("bb", 1);
        keys.put("ccc", 1);
        HashMap<String, Object> result = mcc.countup("key4", keys);
        assertEquals(4, result.size());
        assertEquals(Integer.valueOf("1"), result.get("a"));
        assertEquals(Integer.valueOf("1"), result.get("bb"));
        assertEquals(Integer.valueOf("1"), result.get("ccc"));
    }

    public void testCountup05() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        keys.put("bb", 1);
        keys.put("ccc", 1);
        HashMap<String, Object> result = mcc.countup("key5", keys, 0);
        assertEquals(4, result.size());
        assertEquals(Integer.valueOf("1"), result.get("a"));
        assertEquals(Integer.valueOf("1"), result.get("bb"));
        assertEquals(Integer.valueOf("1"), result.get("ccc"));
    }

    public void testCountup06() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        HashMap<String, Object> result = null;
        for (int i = 0; i < 100; i++) {
            result = mcc.countup("key6", keys, 0);
        }
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf("100"), result.get("a"));
    }

    public void testCountup07() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 3);
        HashMap<String, Object> result = mcc.countup("key7", keys, 0);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf("3"), result.get("a"));
    }

    public void testCountup08() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        keys.put("bb", 1);
        keys.put("ccc", 1);
        HashMap<String, Object> result = mcc.countup("key8", keys, 0);
        assertEquals(4, result.size());
        assertEquals(Integer.valueOf("1"), result.get("a"));
        assertEquals(Integer.valueOf("1"), result.get("bb"));
        assertEquals(Integer.valueOf("1"), result.get("ccc"));
    }

    public void testCountup09() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        HashMap<String, Object> result = mcc.countup("key9", keys, 1);
        Thread.sleep(2000);
        result = mcc.get("key9", null);
        assertNull(result);
    }

    public void testGet01() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        keys.put("b", 2);
        keys.put("c", 3);

        ArrayList<String> subKeys = new ArrayList<String>();
        subKeys.add("a");
        subKeys.add("b");
        subKeys.add("c");

        HashMap<String, Object> result = mcc.countup("key10", keys, 0);
        String lt = (String) result.get("last_updated_date");

        result = mcc.get("key10", subKeys);
        assertEquals(lt, (String) result.get("last_updated_date"));
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
        assertEquals(3, result.get("c"));

        result = mcc.get("key10", subKeys);
        assertEquals(lt, (String) result.get("last_updated_date"));
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
        assertEquals(3, result.get("c"));
    }

    public void testGet02() throws Exception {
        ArrayList<String> subKeys = new ArrayList<String>();
        subKeys.add("a");

        HashMap<String, Object> result = null;
        result = mcc.get("key11");
        assertNull(result);
        result = mcc.get("key11", subKeys);
        assertNull(result);
    }

    public void testUpdate01() throws Exception {
        HashMap<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("a", 1);
        ArrayList<String> subKeys = new ArrayList<String>();
        subKeys.add("a");

        HashMap<String, Object> result = null;
        result = mcc.countup("key12", keys);
        String lt = (String) result.get("last_updated_date");

        result = mcc.update("key12", subKeys, 0);
        assertNotSame(lt, (String) result.get("last_updated_date"));
        assertEquals(1, result.get("a"));

        result = mcc.update("key12", subKeys, 0);
        assertNotSame(lt, (String) result.get("last_updated_date"));
        assertEquals(1, result.get("a"));
    }
}
