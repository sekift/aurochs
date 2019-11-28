package cn.aurochs.www;

import cn.aurochs.www.util.JedisCloseUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Main {
	static cn.aurochs.www.redis.RedisPoolService redisPool = cn.aurochs.www.server.ServiceFactory
			.getService("FwRedisPoolService");

	public static void main(String[] args) throws Exception {
		String key1 = "key11";
		String key2 = "key21";
		try {
			JedisPool jp = redisPool.getPoolByKemataHash("w", key1);
			Jedis jd = jp.getResource();
			jd.auth("123456");
			JedisPool jp1 = redisPool.getPoolByKemataHash("r", key1);
			Jedis jd1 = jp1.getResource();
			jd1.auth("123456");
			System.out.println(jd1.setex(key2, 600, "cba21"));
			System.out.println(jd.get("afe"));
			System.out.println(jd.get("key11"));
			System.out.println(jd.get("key21"));
			
			JedisCloseUtil.returnResourceSilently(jp, jd);
			JedisCloseUtil.returnResourceSilently(jp1, jd1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
