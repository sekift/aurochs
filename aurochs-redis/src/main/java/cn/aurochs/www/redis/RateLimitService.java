package cn.aurochs.www.redis;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.Constants;
import cn.aurochs.www.util.JedisCloseUtil;
import cn.aurochs.www.util.StringUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 限流器on REDIS
 * 
 * @author KC
 * 
 */
public final class RateLimitService {

	private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

	static cn.aurochs.www.redis.RedisPoolService rsm = cn.aurochs.www.server.ServiceFactory
			.getService("FwRedisPoolService");

	/**
	 * 限流器配置VO
	 * 
	 * @author KC
	 * 
	 */
	private final static class LimitProp {
		private final int time;
		private final int limit;

		LimitProp(int time, int limit) {
			this.time = time;
			this.limit = limit;
		}
	}

	private final String rateLimitName;

	private final List<LimitProp> limitList = new ArrayList<LimitProp>();

	private Boolean defaultOnError = Boolean.TRUE;

	/**
	 * 构造方法
	 * 
	 * @param rn
	 */
	private RateLimitService(String rn) {
		this.rateLimitName = rn;
	}

	/**
	 * 静态工厂方法
	 * 
	 * @param rateLimitName
	 *            限流器名
	 * @return
	 */
	public static RateLimitService getInstance(String rateLimitName) {
		if (StringUtil.isNullOrBlank(rateLimitName)) {
			throw new IllegalArgumentException(
					"rateLimitName is null or empty!");
		}
		return new RateLimitService(rateLimitName);
	}

	/**
	 * 增加配置项(注意，按照时间段大小顺序设置，时间段小的要先设置，不然效果会跟预期不一样)
	 * 
	 * @param time
	 *            单位秒
	 * @param limit
	 *            次数上限
	 * @return
	 */
	public RateLimitService addLimit(int time, int limit) {
		limitList.add(new LimitProp(time, limit));
		return this;
	}

	/**
	 * 设置出错时返回的默认值，可选项，true|false|null,不设置时默认true,设置为null时抛出RuntimeException
	 * 
	 * @param doe
	 * @return
	 */
	public RateLimitService setDefaultOnError(Boolean doe) {
		this.defaultOnError = doe;
		return this;
	}

	private static final char KEY_JOINER = '_';
	private static final String KEY_PREFIX = "ratelimiter";

	/**
	 * 根据限流器名、时间、限流key生成redis缓存key
	 * 
	 * @param rateLimitName
	 * @param time
	 * @param key
	 * @return
	 */
	private static String buildRedisKey(String rateLimitName, int time,
			String key) {
		StringBuilder cacheKey = new StringBuilder();
		cacheKey.append(KEY_PREFIX).append(KEY_JOINER).append(rateLimitName)
				.append(KEY_JOINER).append(time).append(KEY_JOINER).append(key);
		return cacheKey.toString();
	}

	/**
	 * 根据限流key返回jedispool，通过itemKey分库
	 * 
	 * @param itemKey
	 * @return
	 * @throws Exception
	 */
	private static JedisPool getJedisPool(String itemKey) throws Exception {
		JedisPool jp;
		jp = rsm.getPoolByKemataHash(Constants.ALIAS_MASTER_FLAG, itemKey);
		return jp;
	}

	private void checkArgs(String key) {
		if (limitList.isEmpty()) {
			throw new IllegalArgumentException("limitList is empty!");
		}
		if (StringUtil.isNullOrBlank(key)) {
			throw new IllegalArgumentException("input key is empty!");
		}
		if (key.indexOf(KEY_JOINER) > -1) {
			throw new IllegalArgumentException("key禁止包含" + KEY_JOINER);
		}
	}

	/**
	 * 检查是否超出频率限制
	 * 
	 * @param key
	 *            需要限流的key，例如userId,userIP等，注意key禁止包含KEY_JOINER
	 * @return 无超出返回true，超出返回false
	 * @throws Exception
	 */
	public boolean check(String key) {
		checkArgs(key);

		JedisPool jp = null;
		Jedis jd = null;
		String cacheKey = null;
		boolean setExpireError = false;
		try {
			jp = getJedisPool(key);
			jd = jp.getResource();
			for (LimitProp prop : limitList) {
				cacheKey = buildRedisKey(this.rateLimitName, prop.time, key);
				long counter = jd.incr(cacheKey);
				if (counter > prop.limit) {
					// 超过限制次数
					return false;
				}

				if (1 == counter) {
					setExpireError = true;
					// ==设置Key缓存时间==
					// 由于上面的incr和expire不是原子操作，所以存在expire失败的可能
					// expire失败了，key就永远不过期，有点危险，所以要再处理一次，为保严谨
					// 虽然这个也还不是最最最安全的做法，先这样吧。补救操作可看delKey方法的说明
					jd.expire(cacheKey, prop.time);
					setExpireError = false;
				}
			}
			return true;
		} catch (Exception e) {
			JedisCloseUtil.returnBrokenResourceSilently(jp, jd);
			if (setExpireError) {
				// 设置缓存时间失败，直接重试删除key
				delKey(cacheKey);
			}
			if (null == defaultOnError) {
				// 不写日志，由上一层处理
				throw new RuntimeException("RateLimiterService check error", e);
			}
			logger.error("RateLimiterService check error:", e);
			return defaultOnError.booleanValue();
		} finally {
			JedisCloseUtil.returnResourceSilently(jp, jd);
		}
	}

	/**
	 * 设置缓存时间失败时，直接重试删除key 当然这个方法也有可能删除失败
	 * 可以使用这个页面，查看一台redis服务器中没有过期时间的key，然后进行手工删除操作
	 * 
	 * @param cacheKey
	 */
	private static void delKey(String cacheKey) {
		JedisPool jp = null;
		Jedis jd = null;
		try {
			// 从cacheKey中解释出itemKey
			String itemKey = cacheKey.substring(cacheKey
					.lastIndexOf(KEY_JOINER) + 1);

			jp = getJedisPool(itemKey);
			jd = jp.getResource();

			jd.del(cacheKey);

		} catch (Exception e) {
			JedisCloseUtil.returnBrokenResourceSilently(jp, jd);
			logger.error("RateLimiterService delKey error,key:" + cacheKey, e);
		} finally {
			JedisCloseUtil.returnResourceSilently(jp, jd);
		}
	}

	/**
	 * 统计数减一
	 * 
	 * @param key
	 */
	public void decrease(String key) {
		checkArgs(key);

		JedisPool jp = null;
		Jedis jd = null;
		String cacheKey = null;
		try {
			jp = getJedisPool(key);
			jd = jp.getResource();
			for (LimitProp prop : limitList) {
				cacheKey = buildRedisKey(this.rateLimitName, prop.time, key);
				long counter = jd.decr(cacheKey);
				if (counter < 1) {
					// 删除缓存
					jd.del(cacheKey);
				}
			}
		} catch (Exception e) {
			JedisCloseUtil.returnBrokenResourceSilently(jp, jd);
			if (null == defaultOnError) {
				// 不写日志，由上一层处理
				throw new RuntimeException("RateLimiterService decrease error",
						e);
			}
			logger.error("RateLimiterService decrease error:", e);
		} finally {
			JedisCloseUtil.returnResourceSilently(jp, jd);
		}
	}

}
