package com.rakuten.rit.roma.romac4j;

import java.util.Arrays;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MapPluginTest extends TestCase {
	static RomaClient rc = null;
	static MapPluginClient mc = null;

	static void oneTimeSetUp() throws Exception {
		rc = new RomaClient("localhost_11311");
		mc = new MapPluginClient(rc);
	}

	static void oneTimeTearDown() throws Exception {
		rc.destroy();
	}

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(MapPluginTest.class);
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

	public void makeData(int n) throws Exception {
		String mapKey, value;
		for (int i = 0; i < n; i++) {
			mapKey = "mapKey" + i;
			value = "value" + i;
			assertTrue(mc.set("key1", mapKey, value.getBytes()));
			assertTrue(Arrays.equals(value.getBytes(), mc.get("key1", mapKey)));
		}
	}

	public void testMapSetExpt() throws Exception {
		assertTrue(mc.set("key1", "mapKey1", "value1".getBytes(), 1));
		assertTrue(Arrays
				.equals("value1".getBytes(), mc.get("key1", "mapKey1")));
		Thread.sleep(2000);
		assertNull(mc.get("key1", "mapKey1"));
	}

	public void testMapSet() throws Exception {
		assertNull(mc.get("key1", "mapkey1"));
		assertTrue(mc.set("key1", "mapKey1", "value1".getBytes()));
		assertTrue(Arrays
				.equals("value1".getBytes(), mc.get("key1", "mapKey1")));
		assertTrue(mc.set("key1", "mapKey1", "value2".getBytes()));
		assertTrue(Arrays
				.equals("value2".getBytes(), mc.get("key1", "mapKey1")));
	}

	public void testMapSetString() throws Exception {
		assertNull(mc.getString("key1", "mapkey1"));
		assertTrue(mc.set("key1", "mapKey1", "value1"));
		assertEquals("value1", mc.getString("key1", "mapKey1"));
		assertTrue(mc.set("key1", "mapKey1", "value2"));
		assertEquals("value2", mc.getString("key1", "mapKey1"));
	}

	public void testMapDelete() throws Exception {
		makeData(10);
		assertEquals(-2, mc.delete("key2", "key1"));
		assertEquals(0, mc.delete("key1", "key1"));
		assertEquals(1, mc.delete("key1", "mapKey1"));
		assertNull(mc.get("key1", "mapKey1"));
	}

	public void testMapIsEmptySizeClear() throws Exception {
		assertEquals(-2, mc.isEmpty("key1"));
		assertEquals(-2, mc.size("key1"));
		assertEquals(-2, mc.clear("key1"));
		makeData(10);
		assertEquals(0, mc.isEmpty("key1"));
		assertEquals(10, mc.size("key1"));
		assertEquals(1, mc.clear("key1"));
		assertEquals(1, mc.isEmpty("key1"));
		assertEquals(0, mc.size("key1"));
	}

	public void testMapIsKey() throws Exception {
		assertEquals(-2, mc.isKey("key1", "key1"));
		makeData(10);
		assertEquals(0, mc.isKey("key1", "key1"));
		assertEquals(1, mc.isKey("key1", "mapKey1"));
	}

	public void testMapIsValue() throws Exception {
		assertEquals(-2, mc.isValue("key1", "value1".getBytes()));
		makeData(10);
		assertEquals(0, mc.isValue("key1", "key1".getBytes()));
		assertEquals(1, mc.isValue("key1", "value1".getBytes()));
	}

	public void testMapKeys() throws Exception {
		assertEquals(0, mc.keys("key1").length);
		makeData(10);
		byte[][] ret = mc.keys("key1");
		for (int i = 0; i < 10; i++) {
			assertTrue(Arrays.equals(("mapKey" + i).getBytes(), ret[i]));
		}
	}

	public void testMapKeysString() throws Exception {
		assertEquals(0, mc.keysString("key1").length);
		makeData(10);
		String[] ret = mc.keysString("key1");
		for (int i = 0; i < 10; i++) {
			assertEquals("mapKey" + i, ret[i]);
		}
	}

	public void testMapValues() throws Exception {
		assertEquals(0, mc.values("key1").length);
		makeData(10);
		byte[][] ret = mc.values("key1");
		for (int i = 0; i < 10; i++) {
			assertTrue(Arrays.equals(("value" + i).getBytes(), ret[i]));
		}
	}

	public void testMapValuesString() throws Exception {
		assertEquals(0, mc.valuesString("key1").length);
		makeData(10);
		String[] ret = mc.valuesString("key1");
		for (int i = 0; i < 10; i++) {
			assertEquals("value" + i, ret[i]);
		}
	}

	public void testMapToS() throws Exception {
		assertNull(mc.toS("key1"));
		makeData(10);
		StringBuffer str = new StringBuffer();
		str.append("{");
		for (int i = 0; i < 10; i++) {
			str.append("\"mapKey" + i + "\"=>\"value" + i + "\", ");
		}
		str.delete(str.length() - 2, str.length());
		str.append("}");
		assertEquals(str.toString(), mc.toS("key1"));
	}
}
