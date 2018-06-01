package cn.aurochs.www.algorithm;

import java.util.Map;

/**
 * 定位器接口
 * <p>备注:策略设计模式控制器
 * <p>对节点定位器初始化节点集合,然后应用hash算法{@link HashAlgorithm},定位特定节点
 * @author:sekift
 * @time:2018-05-18 下午02:59:17
 * @version:1.0.0
 * @param <T> -- 多态类型(节点对象类型)
 */
public interface NodeLocator <T> {
	
	/**
	 * 没有策略(代码)
	 */
	public static final int NULL_STRATEGY = 0;

	/**
	 * 设置定位器应用的节点集合
	 * @param ns -- 节点集合
	 */
	void setNodes(Map<Long, T> ns);
	
	/**
	 * 获取定位器应用的节点集合
	 * @return -- 节点集合
	 */
	Map<Long, T> getNodes();
	
	/**
	 * 设置定位器使用的hash算法
	 * @param hash -- hash算法
	 */
	void setHashAlgorithm(HashAlgorithm hash);
	
	/**
	 * 获取定位器使用的hash算法
	 * @return -- hash算法
	 */
	HashAlgorithm getHashAlgorithm();
		
	/**
	 * 通过一个key和应用指定某种查找方式 定位节点
	 * @param key -- 定位的key
	 * @param strategyCode -- 查找策略代码
	 * @return -- 节点
	 */
	T locate(String key, int strategyCode);
	
	/**
	 * 通过一个已经运算好的hash值(上层代码自己计算hash值)和应用指定某种查找方式 定位节点
	 * <br>备注: 上层代码自己计算hash值, 不应用绑定在定位器上的hash算法
	 * @param hashCode -- 已经运算好的hash值
	 * @param strategyCode -- 查找策略代码
	 * @return -- 节点
	 */
	T locate(long hashCode, int strategyCode);
}
