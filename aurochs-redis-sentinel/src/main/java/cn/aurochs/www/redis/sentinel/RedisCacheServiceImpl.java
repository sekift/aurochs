package cn.aurochs.www.redis.sentinel;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.algorithm.HashAlgorithms;
import cn.aurochs.www.algorithm.NodeLocator;
import cn.aurochs.www.algorithm.NodeLocators;
import cn.aurochs.www.api.distributedcache.RedisCacheService;
import cn.aurochs.www.serializer.JavaSerializer;
import cn.aurochs.www.util.JedisCloseUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.SafeEncoder;

/**
 * Redis Cache 服务
 * 
 * @author sekift
 * @date 2014-1-14
 */
public class RedisCacheServiceImpl implements RedisCacheService {

	private static String STATUSCODE_OK = "OK";

	private static Logger logger = LoggerFactory
			.getLogger(RedisCacheServiceImpl.class);

	/**
	 * JedisSentinelPool定位器（组定位器）
	 */
	private NodeLocator<JedisSentinelPool> proxyLocator;

	/**
	 * Redis分组的组对象集合 备注: 一个组就是一个redis的主从集
	 */
	private List<JedisSentinelPool> groups;

	public void setGroups(List<JedisSentinelPool> groups) {
		this.groups = groups;
	}

	/**
	 * 根据key统一哈希获取proxyLocator对象，第一次使用初始化Redis分布式缓存插件
	 * 
	 * @param key
	 *            -- 操作的key
	 * @return -- JedisSentinelPool对象
	 */
	private JedisSentinelPool getJedisSentinelPool(String key) {
		if (proxyLocator == null) {
			synchronized (logger) {
				if (proxyLocator == null) {
					// 创建Ketama统一哈希NodeLocator
					proxyLocator = NodeLocators.newKetamaConsistentHashLocator(
							HashAlgorithms.KEMATA_HASH, groups);
				}
			}
		}
		return proxyLocator.locate(key, 0);
	}

	@Override
	public boolean set(String key, Object value) {
		if (value != null) {
			ByteBuffer setValue = JavaSerializer.getInstance().serialize(value);
			JedisSentinelPool jedisSentinelPool = getJedisSentinelPool(key);
			Jedis jedis = null;
			try {
				jedis = jedisSentinelPool.getResource();
				String result = jedis.set(SafeEncoder.encode(key),
						setValue.array());
				if (result != null && result.equals(STATUSCODE_OK)) {
					return true;
				}
			} catch (Exception ex) {
				RedisCacheException.throwError("set数据到redis出错", ex);
			} finally {
				if (jedis != null) {
					JedisCloseUtil.returnResourceSilently(jedisSentinelPool, jedis);
				}
			}
		}
		return false;
	}

	@Override
	public Object get(String key) {
		JedisSentinelPool jedisSentinelPool = getJedisSentinelPool(key);
		Jedis jedis = null;
		try {
			jedis = jedisSentinelPool.getResource();
			byte[] resultByte = jedis.get(SafeEncoder.encode(key));
			if (resultByte != null) {

				return JavaSerializer.getInstance().deserialize(
						ByteBuffer.wrap(resultByte), Object.class);
			}
		} catch (Exception ex) {
			RedisCacheException.throwError("get redis数据出错", ex);
		} finally {
			if (jedis != null) {
				JedisCloseUtil.returnResourceSilently(jedisSentinelPool, jedis);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object get(String key, Callable refreshSource) {
		Object value = get(key);
		if (null == value) {
			try {
				value = refreshSource.call();
				if (null != value) {
					set(key, value);
				}
			} catch (Exception ex) {
				logger.error("保存数据到redis异常", ex);
			}
		}
		return value;
	}

	@Override
	public boolean delete(String key) {
		JedisSentinelPool jedisSentinelPool = getJedisSentinelPool(key);
		Jedis jedis = null;
		try {
			jedis = jedisSentinelPool.getResource();
			jedis.del(SafeEncoder.encode(key));
		} catch (Exception ex) {
			RedisCacheException.throwError("delete redis 数据出错", ex);
		} finally {
			if (jedis != null) {
				JedisCloseUtil.returnResourceSilently(jedisSentinelPool, jedis);
			}
		}
		return true;
	}

	@Override
	public Object getAndSet(String key, Object newValue) {
		if (newValue == null) {
			if (delete(key)) {
				return null;
			} else {
				RedisCacheException.throwError("getAndSet空值时失败.");
			}
		}
		JedisSentinelPool jedisSentinelPool = getJedisSentinelPool(key);
		Jedis jedis = null;
		try {
			jedis = jedisSentinelPool.getResource();
			ByteBuffer setValue = JavaSerializer.getInstance().serialize(
					newValue);
			byte[] resultByte = jedis.getSet(SafeEncoder.encode(key),
					setValue.array());
			if (resultByte != null) {
				return JavaSerializer.getInstance().deserialize(
						ByteBuffer.wrap(resultByte), Object.class);
			}
		} catch (Exception ex) {
			RedisCacheException.throwError("getAndSet redis 数据出错", ex);
		} finally {
			if (jedis != null) {
				JedisCloseUtil.returnResourceSilently(jedisSentinelPool, jedis);
			}
		}
		return null;
	}

	@Override
	public boolean compareAndSet(String key, Object oldValue, Object newValue) {
		Object cacheObj = get(key);
		if (null != cacheObj && cacheObj.equals(oldValue)) {
			return set(key, newValue);
		}
		return false;
	}

	@Override
	public boolean set(String key, Object value, long expiry) {
		if (value == null) {
			return false;
		}
		ByteBuffer setValue = JavaSerializer.getInstance().serialize(value);
		JedisSentinelPool jedisSentinelPool = getJedisSentinelPool(key);
		Jedis jedis = null;
		try {
			jedis = jedisSentinelPool.getResource();
			String result = jedis
					.set(SafeEncoder.encode(key), setValue.array());
			jedis.expire(SafeEncoder.encode(key), Long.valueOf(expiry / 1000)
					.intValue());
			if (result != null && result.equals(STATUSCODE_OK)) {
				return true;
			}
		} catch (Exception ex) {
			RedisCacheException.throwError("getAndSet redis 数据出错", ex);
		} finally {
			if (jedis != null) {
				JedisCloseUtil.returnResourceSilently(jedisSentinelPool, jedis);
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object get(String key, Callable refreshSource, long expiry) {
		Object value = get(key);
		if (null == value) {
			try {
				value = refreshSource.call();
				if (null != value) {
					set(key, value, expiry);
				}
			} catch (Exception ex) {
				logger.error("保存数据到redis异常", ex);
			}
		}
		return value;
	}

	@Override
	public Map<String, Object> getMulti(Collection<String> keys) {
		Map<JedisSentinelPool, Collection<byte[]>> s = new LinkedHashMap<JedisSentinelPool, Collection<byte[]>>();
		for (String k : keys) {
			JedisSentinelPool p = getJedisSentinelPool(k);
			Collection<byte[]> c = s.get(p);
			if (null == c) {
				c = new LinkedList<byte[]>();
				s.put(p, c);
			}
			c.add(SafeEncoder.encode(k));
		}
		Map<String, Object> rtv = new HashMap<String, Object>();
		for (JedisSentinelPool p : s.keySet()) {
			Collection<byte[]> coll = s.get(p);

			byte[][] bytes = coll.toArray(new byte[0][]);
			Jedis jedis = null;
			try {
				jedis = p.getResource();
				List<byte[]> results = jedis.mget(bytes);
				Map<String, Object> r = new HashMap<String, Object>();
				for (int i = 0; i < bytes.length; i++) {
					String subKey = SafeEncoder.encode(bytes[i]);
					Object subValue = JavaSerializer.getInstance().deserialize(
							ByteBuffer.wrap(results.get(i)), Object.class);
					r.put(subKey, subValue);
				}
				rtv.putAll(r);
			} catch (Exception ex) {
				RedisCacheException.throwError("getMulti redis 数据出错", ex);
			} finally {
				if (jedis != null) {
					JedisCloseUtil.returnResourceSilently(p, jedis);
				}
			}
		}
		return rtv;
	}
}
