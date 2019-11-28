package cn.aurochs.www.redis.mbean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.util.StringUtil;
import redis.clients.jedis.Jedis;

/**
 * Memcached可用性检测MBean
 */
public class RedisHealthCheck implements RedisHealthCheckMBean {
	private static Logger logger = LoggerFactory
			.getLogger(RedisHealthCheck.class);

	/**
	 * 可用性监控类别
	 */
	public static String TYPE = "RedisHealthCheck";
	/**
	 * 链接超时
	 */
	private long CONNECT_TIMEOUT = 1000L;
	/**
	 * 操作超时
	 */
	//private long OPERATE_TIMEOUT = 2000L;
	/**
	 * 过期时间，默认为秒
	 */
	private static int EXPIRY = 60;

	/**
	 * Redis健康检测的key
	 */
	private static String AMM_REDIS_HEALTH_CHECK = "amm.redis_health_check";

	/**
	 * 服务id
	 */
	private String serviceId = null;

	/**
	 * ip地址
	 */
	private String ip = null;

	/**
	 * 端口
	 */
	private String port = null;

	/**
	 * Redis可用性检测MBean的构造函数
	 * 
	 * @param appId
	 * @param ip
	 * @param port
	 */
	public RedisHealthCheck(String serviceId, String ip, String port,
			Long contectTimeOut) {
		this.serviceId = serviceId;
		this.ip = ip;
		this.port = port;
		if (contectTimeOut != null) {
			CONNECT_TIMEOUT = contectTimeOut;
		}
	}

	@Override
	public String getTargetIp() {
		return ip;
	}

	@Override
	public String getTargetPort() {
		return port;
	}
	
	public Long getTargetTimeout() {
		return CONNECT_TIMEOUT;
	}

	@SuppressWarnings("resource")
	@Override
	public String getState() {
		String checkState = null;
		String strObject = "1";
		Jedis jedis = null;
		String resultObject = null;

		try {
			//获取一个连接
			jedis = new Jedis(ip, Integer.parseInt(port), (int) CONNECT_TIMEOUT);
			jedis.connect();
			// 最简单的检测 //向Redis发送ping命令，结果为PONG说明Redis健康，返回OK；结果非PONG或超时则会抛出异常
			//写入值为"1"的串，过期时间为60s
			jedis.setex(AMM_REDIS_HEALTH_CHECK, EXPIRY, strObject);
			// 尚未等待任务完成，检查任务完成状态 -- 操作时间缺失
			/*if (!operationFuture.get(OPERATE_TIMEOUT, TimeUnit.MILLISECONDS)
					.booleanValue() == true) {
				checkState = "2 seconds timeout";
				return checkState;
			}*/
			resultObject = jedis.get(AMM_REDIS_HEALTH_CHECK);
		} catch (Throwable e) {
			checkState = StringUtil.getExceptionAsStr(e);
			logger.error("Redis check error", e);
			return checkState;
		} finally {
			jedis.quit();
		}
		if (resultObject != null && resultObject.equals(strObject)) {
			checkState = "1";
		} else {
			checkState = "geting value is null or is not euqal to seting value";
		}
		return checkState;
	}

	@Override
	public String getUri() {
		String className = "RedisHealthCheck";
		String methodName = "getState";
		String slash = "/";
		StringBuffer sb = new StringBuffer();
		sb.append(slash).append(serviceId).append(slash).append(className)
				.append(slash).append(methodName);
		return sb.toString();
	}

	@Override
	public String getServiceId() {
		return this.serviceId;
	}
	
	/**
	 * 
	 * 比较两个对象是否相等,如果IP地址、端口和appid都相等,表示相同
	 * 
	 * @param o
	 *            -- 比较的对象
	 * 
	 */
	@Override
	public boolean equals(Object o) {
		boolean res = false;

		if (o != null && RedisHealthCheck.class.isAssignableFrom(o.getClass())) {
			RedisHealthCheck s = (RedisHealthCheck) o;
			res = new EqualsBuilder().append(ip, s.getTargetIp()).append(port, s.getTargetPort()).isEquals();
		}
		return res;
	}

	/**
	 * 
	 * 重写hashcode以比较对象是否相同
	 * 
	 * @return int 哈希码
	 */
	@Override
	public int hashCode() {
		//省略了.append(alias)
		return new HashCodeBuilder(11, 39).append(ip).append(port).toHashCode();
	}

	/**
	 * 冲洗toString方便输出
	 * 
	 * @return String对象的字符串表达
	 * 
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("ip", ip).append("port", port).toString();
	}


}
