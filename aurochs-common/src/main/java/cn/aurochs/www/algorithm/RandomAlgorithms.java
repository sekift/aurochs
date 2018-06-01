package cn.aurochs.www.algorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 
 * 伪随机生成算法
 * @author:sekift
 * @time:2018-05-18 下午02:59:17
 * @version:1.0.0
 */
public class RandomAlgorithms {

	// 随机生成元素算法
	public static void originAlg() {
		String[] beforeShuffle = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		List<String> list = Arrays.asList(beforeShuffle);
		for (int j = 0; j < 1000; j++) {
			Collections.shuffle(list);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				sb.append(list.get(i));
			}
			String afterShuffle = sb.toString();
			String result = afterShuffle.substring(4, 9);
			System.out.println(result);
		}
	}

	// 含有k参数的中序伪随机生成算法
	public static void middleAlg(int n, int m, int k) {
		Random rand = new Random();
		int randNumber = 0;
		int number = 0;
		for (int i = 1; i <= m; i++) {
			randNumber = (n == m) ? 0 : rand.nextInt(n / m - 1);
			number = (int) (n / 2 - k - (Math.pow(-1, i) * Math.floor(i / 2)) * n / m + randNumber);
			if (number < 0) {
				System.out.println((number + n));
			} else if (number >= n) {
				System.out.println((number - n));
			} else {
				System.out.println((number));
			}
		}
	}

	// 含有k、t参数的圆内反射算法的伪随机生成算法
	public static void randomFromCicleAlg(int n, int m, int k, int t,int exsit, int j) {
		Random rand = new Random();
		int randNumber = 0;
		int number = 0;
		int middle = 0;
		for (int i = exsit; i < j; i++) {
			randNumber = (n / m > 1) ? rand.nextInt(n / m - 1) : rand.nextInt(1);
			int a = ((Math.abs(k - i * t + t) / m) + 1) * m;
			int b = a + (k - i * t + t);
			middle = (b < m) ? b * n / m : (b - m) * n / m;
			number = threeAndSevenTras(middle, n, randNumber);
			number = Math.abs(number) % n;
			System.out.println(number);
		}
	}

	// TS变换
	private static int threeAndSevenTras(int middle, int n, int randNumber) {
		int result = 0;
		if (middle % 3 == 0 || (n - middle) % 3 == 0 
				|| middle % 7 == 0 || (n - middle) % 7 == 0) {
			result = (n - middle) + randNumber;
		} else {
			result = middle + randNumber;
		}
		return result;
	}

	// 含有k、t参数的跑道圆环反射算法的伪随机生成算法

	// 含有k、t参数的椭圆内反射算法的伪随机生成算法

	public static void main(String[] args) {
		int n = 10000;// 容量总数，位数
		int m = 10000;// 生成总数
		int k = 5431;// 对userId求m余后最接近的一个素数，保证不同id不同起始
		int t = 5431;// 根据已经生产的个数来做运算，当i为0个重新获取[此数必须与n互素，而且一个生产周期必须相同]
		int exsit = 0; // 已经生产数的个数
		int j = exsit + 10000; //  批量，生产第i+1到j的个数
		// RandomForMobile.middleAlg(n, m, k);
		RandomAlgorithms.randomFromCicleAlg(n, m, k, t, exsit, j);
	}
}
