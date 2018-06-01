package cn.aurochs.www.algorithm;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * 
 * 有关数字的运算
 * @author:sekift
 * @time:2018-05-18 下午02:59:17
 * @version:1.0.0
 *
 */
public class MathAlgorithms {
	
	/**
	 * 判断两个整数是否互质：最大公约数n为1时两数互质
	 * @param m
	 * @param n
	 * @return boolean
	 */
	public static boolean gcd(int m, int n) {
		while (m > 0) {
			int c = n % m;
			n = m;
			m = c;
		}
		if (n == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 选取Map中最大的值
	 * @param stepAndScore
	 * @return
	 */
	public static int getMaxValueScore(Map<Integer, Integer> map) {
		int resutl = 0;
		resutl = Collections.max(map.values(), new Comparator<Integer>() {
			@Override
			public int compare(Integer v1, Integer v2) {
				if (v1 > v2) {
					return 1;
				} else if (v1 < v2) {
					return -1;
				}
				return 0;
			}
		});
		return resutl;
	}
	
	/**
	 * 获取相同value最小的Key
	 * @param capacity
	 * @param stepAndScore
	 * @param maxScore
	 * @return int
	 */
	public static int getMinKey(Map<Integer, Integer> map, int max, int equalValue) {
		int minStep = max;
		for (Integer key : map.keySet()) {
			int value = map.get(key);
			if (value == equalValue) {
				if (key < minStep) {
					minStep = key;
				}
			}
		}
		return minStep;
	}

}
