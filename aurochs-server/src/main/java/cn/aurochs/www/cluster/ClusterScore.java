package cn.aurochs.www.cluster;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 集群得分
 * 1 对比组内：集群组内节点在同一区域评分为0，不同评分8 
 * 2 对比多个集群：集群之间的组按不同区域的不同给1/2/3/4分
 * 
 * @author:luyz
 * @time:2018-5-18 下午02:59:17
 * @version: 1.0.0
 *
 */
public class ClusterScore {
	private static final Logger logger = LoggerFactory.getLogger(ClusterScore.class);

	// 评价总得分
	public static int calculateClusterScore(List<List<String>> localClusterList, List<List<String>> machineList) {
		int score = 0;
		score += pointInZone(localClusterList, machineList);
		score += groupInZone(localClusterList, machineList);
		return score;
	}

	// 评价集群的组内节点是否在同一区域
	public static int pointInZone(List<List<String>> localClusterList, List<List<String>> machineList) {
		int score = 0;
		for (List<String> list : localClusterList) {
			for (int part = 0; part < list.size() - 1; part += 2) {
				if (!isPointInZone(list.get(part), list.get(part + 1), machineList)) {
					score += 8;
				}
			}
			logger.info("localClusterList:" + localClusterList);
			logger.info("集群的组内节点是否在同一区域：" + score);
		}
		return score;
	}

	// 是否在同一区域
	public static boolean isPointInZone(String v1, String v2, List<List<String>> machineList) {
		// logger.info("v1,v2:"+v1+" "+v2);
		boolean result = false;
		for (List<String> list : machineList) {
			if (list.contains(v1) && list.contains(v2)) {
				result = true;
				break;
			}
		}
		return result;
	}

	// 评价集群组的区域是否相同
	public static int groupInZone(List<List<String>> localClusterList, List<List<String>> machineList) {
		int score = 0;
		// 分组
		int localGroupListSize = 0;
		for (List<String> list : localClusterList) {
			List<List<String>> localGroupList = new ArrayList<List<String>>();
			for (int part = 0; part < list.size() - 1; part += 2) {
				List<String> localList = new ArrayList<String>();
				localList.add(list.get(part));
				localList.add(list.get(part + 1));
				localGroupList.add(localList);
			}
			localGroupListSize = localGroupList.size();
			if (localGroupListSize == 1) {
				score = +1;
			} else {
				for (int i = 0; i < localGroupListSize; i++) {
					for (int j = i + 1; j < localGroupListSize; j++) {
						score += isGroupInZone(localGroupList.get(i), localGroupList.get(j), machineList);
					}
				}
			}
			logger.info("localGroupList:" + localGroupList);
		}
		return score;
	}

	/**
	 * 同一集群组的区域是否相同 
	 * 1 4个不同，记4分 
	 * 2 3个不同，记3分 
	 * 3 2个不同，记2分 
	 * 4 1都相同，记1分
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static int isGroupInZone(List<String> list1, List<String> list2, List<List<String>> machineList) {
		int score = 0;
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < machineList.size(); i++) {
			if (machineList.get(i).contains(list1.get(0))) {
				list.add(i);
			} else if (machineList.get(i).contains(list1.get(1))) {
				list.add(i);
			} else if (machineList.get(i).contains(list2.get(0))) {
				list.add(i);
			} else if (machineList.get(i).contains(list2.get(1))) {
				list.add(i);
			}
		}
		score = list.size();
		logger.info("同一集群组的区域是否相同得分：" + score);
		return score;
	}

}
