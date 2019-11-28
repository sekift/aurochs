package cn.aurochs.www.redis;


import cn.aurochs.www.Constants;
import cn.aurochs.www.util.JedisCloseUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * jedis-redis操作封装
 * @author luyz
 * @date 2019-01-14
 */
public class RedisOperate {
	
	public RedisOperate() {
	}
	
	static cn.aurochs.www.redis.RedisPoolService redisPool = cn.aurochs.www.server.ServiceFactory
			.getService("FwRedisPoolService");
	
    /**
     *  插入对象
     * @param k
     * @param v
     * @param t
     */
    public static void set(String k, String v, int t) {
    	JedisPool jp = null;
    	Jedis jd = null;
    	try {
    		jp = redisPool.getPoolByKemataHash(Constants.ALIAS_MASTER_FLAG, k);
    		jd = jp.getResource();
    		jd.setex(k, t, v);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally{
    		JedisCloseUtil.returnResourceSilently(jp, jd);
    	}
    }

    /**
     *  删除对象
     * @param k
     */
    public static void deleteByKey(String k) {
    	JedisPool jp = null;
    	Jedis jd = null;
    	try {
    		jp = redisPool.getPoolByKemataHash(Constants.ALIAS_MASTER_FLAG, k);
    		jd = jp.getResource();
    		jd.del(k);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally{
    		JedisCloseUtil.returnResourceSilently(jp, jd);
    	}
    }

    /**
     *  修改对象
     * @param k
     * @param v
     */
    public static void updateByKey(String k, String v) {
    	JedisPool jp = null;
    	Jedis jd = null;
    	try {
    		jp = redisPool.getPoolByKemataHash(Constants.ALIAS_MASTER_FLAG, k);
    		jd = jp.getResource();
            jd.set(k, v);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally{
    		JedisCloseUtil.returnResourceSilently(jp, jd);
    	}
    }

    /**
     *  查询对象
     * @param k
     * @return
     */
    public static String getValueByKey(String k) {
    	JedisPool jp = null;
    	Jedis jd = null;
    	String result = null;
    	try {
    		jp = redisPool.getPoolByKemataHash(Constants.ALIAS_SLAVE_FLAG, k);
    		jd = jp.getResource();
    		result = jd.get(k);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally{
    		JedisCloseUtil.returnResourceSilently(jp, jd);
    	}
    	return result;
    }

}
