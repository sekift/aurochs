package cn.aurochs.www.algorithm;

/**
 * hash算法接口
 * @author:sekift
 * @time:2018-05-18 下午02:59:17
 * @version:1.0.0
 */
public interface HashAlgorithm {

	/**
	 * 对key进行hash运行,获取hash值
	 * @param key -- 被进行hash的key
	 * @return -- hash值
	 */
	long hash(final String key);
}
