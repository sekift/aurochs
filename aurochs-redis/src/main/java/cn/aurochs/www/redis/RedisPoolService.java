package cn.aurochs.www.redis;


import redis.clients.jedis.JedisPool;

public interface RedisPoolService {
	public void setItemLocateAlgorithm(String itemLocateAlgorithm);

	public void setRedisServerMapping(String redisServerMapping);

	public void setRedisPoolConfig(String redisPoolConfig);

	public void initializePlugin();

	public String getItemLocateAlgorithm();

	public String getRedisServerMapping();

	public String getRedisPoolConfig();

	public JedisPool getPoolByKemataHash(String rwMode, String key)
			throws Exception;
	
	public JedisPool getPool(String rwMode, long identityHashCode)
			throws Exception;

	public JedisPool getPool(String alias) throws Exception;
}
