package cn.aurochs.www.api.distributedcache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 分布式缓存服务
 * 
 * @author sekift
 * @date 2019-10-18
 */
@SuppressWarnings("rawtypes")
public interface DistributedCacheService {
	/**
	 * 根据key,将value保存到缓存服务
	 * 
	 * @param key
	 *            -- 缓存对象的标记key
	 * @param value
	 *            -- 缓存对象，缓存对象不能为null
	 * @return -- true, 如果执行了操作, 但是不能保证真正已经保存近底层的缓存 这里需要依赖具体的实现.
	 */
	boolean set(String key, Object value);

	/**
	 * 根据key获取缓存对象
	 * 
	 * @param key
	 *            -- 缓存key
	 * @return -- 缓存对象
	 */
	Object get(String key);

	/**
	 * 根据key获取缓存对象, 并指定一个刷新源
	 * 
	 * <pre>
	 * 备注: 当根据key获取缓存对象为null时, 调用refreshSource获取最新的可缓存对象
	 * 将其保存到缓存服务.
	 * 等价代码:
	 *     Object result = cacheServcie.get(key);
	 *     if (null == result) {
	 *         
	 *         result = refreshSource.call();
	 *         cacheServcie.set(key, result);
	 *     }
	 *     return result;
	 * </pre>
	 * 
	 * @param key
	 *            -- 缓存key
	 * @param refreshSource
	 *            -- 刷新源
	 * @return -- 缓存对象
	 */
	Object get(String key, Callable refreshSource);

	/**
	 * 根据key,将缓存对象从缓存服务中删除
	 * 
	 * @param key
	 *            -- 缓存key
	 * @return -- true, 如果执行了操作,但是不能保证真正已经删除了缓存对象 这里需要依赖具体的实现.
	 */
	boolean delete(String key);

	/**
	 * 根据key获取缓存对象,同时将这个key设置成另外一个对象
	 * 
	 * <pre>
	 * 等价代码:
	 *      Object result = cacheServcie.get(key);
	 *      cacheServcie.set(key, newValue);
	 *      return result;
	 * </pre>
	 * 
	 * @param key
	 *            -- 缓存key
	 * @param newValue
	 *            -- 新的缓存对象，缓存对象不能为null
	 * @return -- 旧的缓存对象(未设置新的对象前的那个缓存对象)
	 */
	Object getAndSet(String key, Object newValue);

	/**
	 * 对特定的key, 比较缓存服务中的对象如果等于指定的对象, 用一个新的对象将其设置成新值
	 * 
	 * <pre>
	 * 等价代码:
	 *     Object cacheObj = cacheServcie.get(key);
	 *     if (null != cacheObj && cacheObj.equals(oldValue)) {
	 *     
	 *     	   return cacheServcie.set(key, newValue);
	 *     }
	 *     return false;
	 * </pre>
	 * 
	 * @param key
	 *            -- 缓存key
	 * @param oldValue
	 *            -- 用于比较的对象
	 * @param newValue
	 *            -- 如果比较相等了, 将会被设置进缓存服务的对象，缓存对象不能为null
	 * @return -- true, 如果执行了比较并设置了新值.
	 */
	boolean compareAndSet(String key, Object oldValue, Object newValue);

	/**
	 * 保存对象,在设置的时长后过期
	 * 
	 * @param key
	 *            -- 缓存key
	 * @param value
	 *            -- 缓存对象
	 * @param expiry
	 *            -- 过期时长, 单位:毫秒
	 * @return
	 */
	boolean set(final String key, final Object value, long expiry);

	/**
	 * 根据key获取缓存对象, 并指定一个刷新源, 如果没有命中使用刷新源重新获取数据，并根据指定过期时间 重新保存到缓存中
	 * 
	 * @param key
	 *            -- 缓存key
	 * @param refreshSource
	 *            -- 刷新源
	 * @return -- 缓存对象
	 *         <p>
	 *         备注:
	 *         若调用refreshSource抛出异常，该方法会捕捉并重新抛出一个RuntimeException提示“执行刷新缓存对象失败”
	 *         等价代码: Object result = cacheServcie.get(key); if (null == result)
	 *         {
	 * 
	 *         result = refreshSource.call(); cacheServcie.set(key, result,
	 *         expiry); } return result;
	 */
	Object get(String key, Callable refreshSource, long expiry);

	/**
	 * 根据key的集合,一次获取多个key
	 * 
	 * @param keys
	 *            -- key的集合
	 * @return 缓存结果集合
	 */
	Map<String, Object> getMulti(Collection<String> keys);

}
