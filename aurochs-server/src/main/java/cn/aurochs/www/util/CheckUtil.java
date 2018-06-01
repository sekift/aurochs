package cn.aurochs.www.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查参数类
 * 
 * @author:luyz
 * @time:2018-5-18 下午02:59:17
 * @version: 1.0.0
 *
 */
public class CheckUtil {
	private static final Logger logger = LoggerFactory.getLogger(CheckUtil.class);

	// 参数判断
	public static boolean check(int capacity, int clusterSize, Map<String, Integer> clusterNameAndSize) {
		boolean result = false;
		logger.info("[BuildCluster]容量为："+capacity+"，集群数为："+clusterSize+"。");
		// 判断
		if (capacity < 1 || clusterSize < 1) {
			logger.error("[BuildCluster]构造集群错误，容量和集群数不得小于1。");
			return result;
		}
		// 判断是否符合集群要求：总数为偶数
		if (capacity > 2 && capacity % 2 != 0) {
			logger.error("[BuildCluster]构造集群错误，容量数必须为偶数。");
			return result;
		}

		// 集群与机器数
		if (capacity < clusterSize) {
			logger.error("[BuildCluster]构造集群错误，容量不得小于集群数。");
			return result;
		}

		// 判断集群的分配
		int sum = 0;
		for (int groupPerSize : clusterNameAndSize.values()) {
			sum = sum + groupPerSize;
			if (capacity > 3) {
				if (groupPerSize % 2 != 0) {
					logger.error("[BuildCluster]构造集群错误，集群分配批次错误，每个集群数要为偶数，现在是：" + groupPerSize);
					return result;
				}
			}
		}
		if (sum != capacity) {
			logger.error("[BuildCluster]构造集群错误，集群分配总个数错误，总数为：" + sum +"，但容量为：" + capacity);
			return result;
		}

		return true;
	}

}
