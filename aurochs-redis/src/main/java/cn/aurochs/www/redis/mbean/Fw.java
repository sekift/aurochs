package cn.aurochs.www.redis.mbean;

import cn.aurochs.www.Version;

/**
 * 获取框架的版本
 * 
 */
public class Fw implements FwMBean {

	/**
	 * 可用性监控domain
	 */
	public static String FW_USABILITY_DOMAIN = "cn.alauwahios.front.redis.mbean.usability";

	@Override
	public String getVersion() {
		return Version.VERSION;
	}

}
