package cn.aurochs.www.redis.mbean;

/**
 * 可用性检测MBean的基类借口
 * 
 */
public interface UsabilityMBean {

	/**
	 * 获取服务Id
	 * 
	 * @return
	 */
	public String getServiceId();

	/**
	 * 
	 * 获取目标IP地址
	 * 
	 * @return 返回目标IP地址
	 */
	public String getTargetIp();

	/**
	 * 
	 * 获取目标端口
	 * 
	 * @return 返回目标端口
	 */
	public String getTargetPort();

	/**
	 * 
	 * 获取 检测URI地址.
	 * 
	 * @return Uri字符串
	 */
	public String getUri();

}
