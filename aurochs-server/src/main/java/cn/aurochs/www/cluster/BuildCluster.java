package cn.aurochs.www.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.algorithm.MathAlgorithms;
import cn.aurochs.www.util.CheckUtil;
import cn.aurochs.www.util.StringUtil;

/**
 * 自动集群分配
 * 名词解释：
 * 集群：cluster
 * 组：group
 * 节点：point
 * 区：zone
 * 机器：machine
 * 
 * 组内节点数为2个，暂时不支持更大的
 * @author:luyz
 * @time:2018-5-18 下午02:59:17
 * @version: 1.0.0
 */
public class BuildCluster {

	private static final Logger logger = LoggerFactory.getLogger(BuildCluster.class);

	// 容量总数，机器数
	private int capacity = 0;
	// 分配的集群个数
	private int clusterSize = 0;
	// 步长[此数必须与n互素，而且一个生产周期必须相同]，系统自动选取
	private int step = 0;

	// 机器排布，zone之间使用分号;分隔，机器之间使用,分隔
	private String machineZone = "";
	// 机器排布，处理后的list
	private List<List<String>> machineList = new ArrayList<List<String>>();
	// 大小集群机器分配:集群名称，占用数量
	private Map<String, Integer> clusterNameAndSize = new TreeMap<String, Integer>();
	// 保存每个step与分配结果的map
	private Map<Integer, String> stepAndCluster = new HashMap<Integer, String>();
	// 保存step与分配结果的集群list
	private Map<Integer, Map<String, List<String>>> stepAndClusterList = new HashMap<Integer, Map<String, List<String>>>();
	// 返回最终集群分配结果：集群内组使用;分隔，组内节点使用,分隔
	private Map<String, String> resultCluster = new HashMap<String, String>();
	// step与各集群得分
	Map<Integer, Integer> stepAndScore = new HashMap<Integer, Integer>();
	
	public BuildCluster(String machineZone, Map<String, Integer> clusterNameAndSize) {
		this.machineList = buildMachineList(machineZone);
		this.clusterSize = clusterNameAndSize.size();
		this.machineZone = machineZone;
		this.clusterNameAndSize = clusterNameAndSize;
		this.resultCluster = buildCluster();
	}
	
	// 处理machineZone到machineList,machineScoreList
	private List<List<String>> buildMachineList(String machineZone) {
		List<List<String>> machineListLocal = new ArrayList<List<String>>();
		if (StringUtil.isNullOrBlank(machineZone)) {
			logger.error("[BuildCluster]构造集群错误，机器输入为空。");
			return machineListLocal;
		}

		int capacity = 0;
		String[] zoneStr = machineZone.trim().split(";");
		for (String zone : zoneStr) {
			List<String> list = new ArrayList<String>();
			String[] zoneElement = zone.trim().split(",");
			for (String ele : zoneElement) {
				list.add(ele);
				capacity++;
			}
			machineListLocal.add(list);
		}
		this.capacity = capacity;
		return machineListLocal;
	}
	
	// 构造算法
	public Map<String, String> buildCluster() {
		// 检查参数
		if (!CheckUtil.check(capacity, clusterSize, clusterNameAndSize)) {
			return resultCluster;
		}

		// 先合并
		List<String> machineListGroup = new ArrayList<String>();
		for (List<String> machine : machineList) {
			machineListGroup.addAll(machine);
		}
		logger.info("集群总机器machineList：" + machineList);

		// 小集群直接构造
		if (capacity <= 2) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < capacity; i++) {
				sb.append(machineListGroup.get(i) + " ");
				this.stepAndCluster.put(step, sb.toString().trim());
			}
		} else {
			// 圆内反射 构造算法
			int number = 0;
			for (int step = 3; step < capacity; step += 2) {
				if (MathAlgorithms.gcd(step, capacity)) {
					this.step = step;
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < capacity; i++) {
						number = 1 + i * step;
						number = Math.abs(number) % capacity;
						if (number == 0) {
							number = capacity;
						}
						sb.append(machineListGroup.get(number - 1) + " ");
						stepAndCluster.put(step, sb.toString().trim());
					}
				}
			}
		}
		return evaluateClusterFunction();
	}

	/**
	 * 评价函数： 
	 * 1 对比组内：集群组内节点在同一区域评分为0，不同评分8
	 * 2 对比多个集群：集群之间的组按不同区域的不同给1/2/3/4分
	 * 3 最后：选分值最大而且step最小的
	 */
	private Map<String, String> evaluateClusterFunction() {
		logger.info("stepAndCluster:" + stepAndCluster);
		logger.info("clusterSize:" + clusterSize);
		logger.info("clusterNameAndSize:" + clusterNameAndSize);

		if (capacity == 1) {// 只有一台不用评
			resultCluster.put(clusterNameAndSize.keySet().iterator().next(), stepAndCluster.get(step));
		} else if (capacity == 2) {// 只有两台也不同评
			if (clusterSize == 1) {
				resultCluster.put(clusterNameAndSize.keySet().iterator().next(),
						stepAndCluster.get(step).replace(" ", ","));
			} else {
				String[] localClusterStr = stepAndCluster.get(step).split(" ");
				int i = 0;
				for (String key : clusterNameAndSize.keySet()) {
					resultCluster.put(key, localClusterStr[i]);
					i++;
				}
			}
		} else if (capacity > 3) {
			for (int localStep : stepAndCluster.keySet()) {
				// 分集群
				List<List<String>> localClusterList = new ArrayList<List<String>>();
				String localCluster = stepAndCluster.get(localStep);
				logger.info("localCluster:"+localCluster);

				String[] localClusterStr = localCluster.split(" ");
				Map<String, List<String>> nameAndClusterMap = new HashMap<String, List<String>>();

				int sum = 0;
				for (String key : clusterNameAndSize.keySet()) {
					int partLength = clusterNameAndSize.get(key);
					List<String> list = new ArrayList<String>();
					for (int i = sum; i < sum + partLength; i++) {
						list.add(localClusterStr[i]);
					}
					sum += partLength;
					localClusterList.add(list);
					nameAndClusterMap.put(key, list);
				}
				//logger.info("localClusterList:"+localClusterList);
				stepAndScore.put(localStep, ClusterScore.calculateClusterScore(localClusterList, machineList));
				stepAndClusterList.put(localStep, nameAndClusterMap);
			}
		}
		logger.info("stepAndClusterList:" + stepAndClusterList);
		logger.info("stepAndScore:" + stepAndScore);
		return getClusterByStep();
	}
	
	// 选取选分值最大而且step最小集群
	private Map<String, String> getClusterByStep() {
		// 先取分值最大，再取step最小
		int maxScore = MathAlgorithms.getMaxValueScore(stepAndScore);
		int minStep = MathAlgorithms.getMinKey(stepAndScore, capacity, maxScore);
		
		logger.info("maxScore:" + maxScore);
		logger.info("minStep:" + minStep);
		return packageCluster(stepAndClusterList.get(minStep));
	}

	// 整理分配后的集群
	private Map<String, String> packageCluster(Map<String, List<String>> resultClusterList) {
		for (String key : resultClusterList.keySet()) {
			StringBuffer sb = new StringBuffer();
			List<String> list = resultClusterList.get(key);
			for (int i = 0; i < list.size(); i++) {
				if (i % 2 == 0) {
					sb.append(list.get(i) + ",");
				} else if (i % 2 != 0) {
					sb.append(list.get(i) + ";");
				}
			}
			logger.info("sb="+sb.substring(0, sb.length()-1).toString());
			resultCluster.put(key, sb.substring(0, sb.length() - 1).toString());
		}

		return resultCluster;
	}
	
	
	public Map<String, String> getResultCluster() {
		return resultCluster;
	}

	public List<List<String>> getMachineList() {
		return machineList;
	}

	public Map<String, Integer> getClusterNameAndSize() {
		return clusterNameAndSize;
	}

	public String getMachineZone() {
		return machineZone;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getClusterSize() {
		return clusterSize;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}
	
}
