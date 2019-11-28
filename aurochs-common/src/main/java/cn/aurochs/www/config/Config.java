package cn.aurochs.www.config;

/**
 * 配置对象
 */
public interface Config {
	
	/**
	 * 获取配置项
	 * @param <T> -- 多态类型
	 * @param name -- 配置项名称
	 * @return -- 配置值
	 */
	<T> T getItem(String name); 
	
	/**
	 * 获取配置项
	 * @param <T> -- 多态类型
	 * @param name -- 配置项名称
	 * @param defaultValue -- 默认值,如果没有找到配置,返回默认值
	 * @return -- 配置值
	 */
	<T> T getItem(String name, T defaultValue);
	
}
