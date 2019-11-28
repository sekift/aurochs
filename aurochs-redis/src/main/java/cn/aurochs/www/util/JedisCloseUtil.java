package cn.aurochs.www.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 关闭资源的类
 * 
 * @author:sekift
 * @time:2014-7-14 下午05:10:40
 * @version 1.0.0
 */
public class JedisCloseUtil {
	
    /**
     * 关闭jedis资源对象
     * 备注: 如果资源对象不为null, 关闭资源,不抛出任何异常
     * @param rsc -- 资源对象
     */
	/**
	 * 连接失败时调用回收资源
	 * 
	 * @param jp
	 * @param r
	 */
	public static void returnBrokenResourceSilently(JedisPool jp, Jedis r) {
		if (null != jp && null != r) {
			try {
				//jp.returnBrokenResource(r); // jedis 2.10.0+redis 2.6的关闭方式
				r.close();// jedis 3.1.0 使用
			} catch (Exception e) {
				/* 消除异常 */ 
			}
			r = null;
		}
	}

	/**
	 * 回收资源
	 * 
	 * @param jp
	 * @param r
	 */
	public static void returnResourceSilently(JedisPool jp, Jedis r) {
		if (null != jp && null != r) {
			try {
				//jp.returnResource(r); // jedis 2.10.0+redis 2.6的关闭方式
				r.close(); // jedis 3.1.0 使用
			} catch (Exception e) {
				/* 消除异常 */ 
			}
			r = null;
		}
	}
}
