package cn.aurochs.www.redis.jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.redis.RedisPoolConfig;
import redis.clients.jedis.JedisPool;

public class RedisPoolProvider {

	final private static Logger logger = LoggerFactory.getLogger(RedisPoolProvider.class);
	
	private static Map<String,JedisPool> poolMap = new HashMap<String,JedisPool>();
	
	private RedisPoolProvider(){
		
	}
	
	public static synchronized void initial(String cfgFilePath){
		try {
			List<Map<String, Object>> list = RedisPoolConfig.getAliasList();
			if (null != list && !list.isEmpty()) {
				for (Map<String, Object> map : list) {
					initialPool(map);
				}
			}
		} catch (Exception e) {
			logger.error("初始化redis pool连接池失败:",e);
		}
	}
	
	private static void initialPool(Map<String,Object> cfg){
		try{
			GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<Object>();
			poolConfig.setLifo(Boolean.parseBoolean((String)cfg.get("lifo")));
			poolConfig.setMaxTotal(Integer.parseInt((String)cfg.get("maxTotal")));
			poolConfig.setMaxIdle(Integer.parseInt((String)cfg.get("maxIdle")));
			poolConfig.setMaxWaitMillis(Integer.parseInt((String)cfg.get("maxWait")));
			poolConfig.setMinEvictableIdleTimeMillis(Long.parseLong((String)cfg.get("minEvictableIdleTimeMillis")));
			poolConfig.setMinIdle(Integer.parseInt((String)cfg.get("minIdle")));
			poolConfig.setNumTestsPerEvictionRun(Integer.parseInt((String)cfg.get("numTestsPerEvictionRun")));
			poolConfig.setTestOnBorrow(Boolean.parseBoolean((String)cfg.get("testOnBorrow")));
			poolConfig.setTestOnReturn(Boolean.parseBoolean((String)cfg.get("testOnReturn")));
			poolConfig.setTestWhileIdle(Boolean.parseBoolean((String)cfg.get("testWhileIdle")));
			poolConfig.setTimeBetweenEvictionRunsMillis(Long.parseLong((String)cfg.get("timeBetweenEvictionRunsMillis")));
			poolConfig.setBlockWhenExhausted(true);
			
			String host = (String)cfg.get("server");
			int port = Integer.parseInt((String)cfg.get("port"));
			int timeout = Integer.parseInt((String)cfg.get("timeout"));
			
			JedisPool pool = new JedisPool(poolConfig, host, port, timeout);
			String alias = (String)cfg.get("alias");
			poolMap.put(alias, pool);
			
			logger.info("RedisPoolProvider>>创建redis连接池. alias:{}, proxool completeUrl:{}",
					alias, host + ":" + port);
		}catch(Exception e){
			logger.error("创建redis pool失败:",e);
		}
	}
	
	public static JedisPool getPool(String alias) throws Exception {
		JedisPool jp = poolMap.get(alias);
		if (null == jp) {
			throw new RuntimeException("redis pool 不存在. alias:" + alias);
		}
		return jp;
	}
}
