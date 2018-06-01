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
	
	public static void main(String[] args) {
//		String machineZone = "g1,g2,g3,g4,g5,g6,g7,g8,g9,g10,g11,g12,g13,g14,g15,g16,g17,g18,g19,g20,g21,g22,g23,g24,g25,g26,g27,g28,g29,g30,g31,g32,g33,g34,g35,g36,g37,g38,g39,g40,g41,g42,g43,g44,g45,g46,g47,g48,g49,g50,g51,g52,g53,g54,g55,g56,g57,g58,g59,g60,g61,g62,g63,g64,g65,g66,g67,g68,g69,g70,g71,g72,g73,g74,g75,g76,g77,g78,g79,g80,g81,g82,g83,g84,g85,g86,g87,g88,g89,g90,g91,g92,g93,g94,g95,g96,g97,g98,g99,g100;" +
//				"b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11,b12,b13,b14,b15,b16,b17,b18,b19,b20,b21,b22,b23,b24,b25,b26,b27,b28,b29,b30,b31,b32,b33,b34,b35,b36,b37,b38,b39,b40,b41,b42,b43,b44,b45,b46,b47,b48,b49,b50,b51,b52,b53,b54,b55,b56,b57,b58,b59,b60,b61,b62,b63,b64,b65,b66,b67,b68,b69,b70,b71,b72,b73,b74,b75,b76,b77,b78,b79,b80,b81,b82,b83,b84,b85,b86,b87,b88,b89,b90,b91,b92,b93,b94,b95,b96,b97,b98,b99,b100;" +
//				"s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15,s16,s17,s18,s19,s20,s21,s22,s23,s24,s25,s26,s27,s28,s29,s30,s31,s32,s33,s34,s35,s36,s37,s38,s39,s40,s41,s42,s43,s44,s45,s46,s47,s48,s49,s50,s51,s52,s53,s54,s55,s56,s57,s58,s59,s60,s61,s62,s63,s64,s65,s66,s67,s68,s69,s70,s71,s72,s73,s74,s75,s76,s77,s78,s79,s80,s81,s82,s83,s84,s85,s86,s87,s88,s89,s90,s91,s92,s93,s94,s95,s96,s97,s98,s99,s100;" +
//				"h1,h2,h3,h4,h5,h6,h7,h8,h9,h10,h11,h12,h13,h14,h15,h16,h17,h18,h19,h20,h21,h22,h23,h24,h25,h26,h27,h28,h29,h30,h31,h32,h33,h34,h35,h36,h37,h38,h39,h40,h41,h42,h43,h44,h45,h46,h47,h48,h49,h50,h51,h52,h53,h54,h55,h56,h57,h58,h59,h60,h61,h62,h63,h64,h65,h66,h67,h68,h69,h70,h71,h72,h73,h74,h75,h76,h77,h78,h79,h80,h81,h82,h83,h84,h85,h86,h87,h88,h89,h90,h91,h92,h93,h94,h95,h96,h97,h98,h99,h100";//
		String machineZone = "g1,g2,g3,g4;b1,b2,b3,b4;s1,s2,s3,s4;x1,x2,x3,x4";//;h1,h2,h3,h4
//		String machineZone = "g1,g2,g2,g2,g2,g2,g2,g2,g2,g2,g2,g2,g2,g2,g2,g2";//
		Map<String, Integer> map = new TreeMap<String, Integer>();
		map.put("group1", 4);
		map.put("group2", 4);
		map.put("group3", 4);
		map.put("group4", 4);
//		map.put("group5", BigDecimal.valueOf(50.0/400));
//		map.put("group6", BigDecimal.valueOf(20.0/400));
//		map.put("group7", BigDecimal.valueOf(20.0/400));
//		map.put("group8", BigDecimal.valueOf(20.0/400));
//		map.put("group9", BigDecimal.valueOf(36.0/400));
//		map.put("group10", BigDecimal.valueOf(2.0/400));
		BuildCluster mg = new BuildCluster(machineZone, map);
		logger.info("结果：" + mg.getResultCluster());
	}
}
