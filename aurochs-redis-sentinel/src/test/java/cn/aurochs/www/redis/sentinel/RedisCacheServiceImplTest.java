package cn.aurochs.www.redis.sentinel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.aurochs.www.api.distributedcache.DistributedCacheService;
import cn.aurochs.www.server.ServiceFactory;
import cn.aurochs.www.util.SeqUUIDUtil;


public class RedisCacheServiceImplTest {

	protected DistributedCacheService dcs;

	private String key = "key_test";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dcs = ServiceFactory.getService("RedisCacheService");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void setStringObject() {
		dcs.set("key1", "value1", 3600*1000L);
		dcs.set("key2", "value2", 3600*1000L);
		dcs.set("key3", "value3", 3600*1000L);
		dcs.set("key4", "value4", 3600*1000L);
	}

	@Test
	public void setStringObjectLong() {
		dcs.set(key, "value1", 1000L);
		String result = null;
		result = (String) dcs.get(key);
		assertEquals("value1", result);
		try {
			Thread.sleep(1500L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result = (String) dcs.get(key);
		assertEquals(null, result);
	}

	@Test
	public void setStringObjectTooLong() {
		String key = Long.valueOf(System.currentTimeMillis())+SeqUUIDUtil.toSequenceUUID();
		dcs.delete(key);
		dcs.set(key, "value1", 31 * 24 * 3600 * 1000L);
		String result = null;
		result = (String) dcs.get(key);
		assertEquals("value1", result);
	}
	

	/**
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void getStringCallableLong() {
		Callable refreshSource = new Callable() {
			public Object call() throws Exception {
				return "value1";
			}
		};
		String result = (String) dcs.get(key, refreshSource, 1000L);
		assertEquals("value1", result);
		try {
			Thread.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result = (String) dcs.get(key);
		assertEquals(null, result);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void getStringCallableLongException() {
		Callable refreshSource = new Callable() {
			public Object call() throws Exception {
				throw new Exception("refreshSource exception");
			}
		};
		String result = (String) dcs.get(key, refreshSource, 1000L);
		assertEquals(null, result);
	}

	@Test
	public void getMulti() {
		dcs.set(key, "value1");
		dcs.set(key + "2", "value2");
		Set<String> keys = new HashSet<String>();
		keys.add(key);
		keys.add(key + "2");
		Map<String, Object> results = dcs.getMulti(keys);
		assertEquals("value1", (String) results.get(key));
		assertEquals("value2", (String) results.get(key + "2"));
	}

	@Test
	public void setNull() {
		boolean result = dcs.set(key, null);
		assertFalse(result);

	}
	
	/**
	 * Set的压力测试
	 */
	@Test
	public void setPerformance(){
		String key = Long.valueOf(System.currentTimeMillis())+SeqUUIDUtil.toSequenceUUID();
		boolean result = dcs.set(key, "value1");
		assertTrue(result);
	}
	
	/**
	 * get压力测试
	 */
	@Test
	public void getPerformance(){
		Object obj = dcs.get(key);
		assertNotNull(obj);
	}
	
}
