package cn.aurochs.www.util;


import java.util.UUID;

/**
 * 有序UUID 工具类
 * 备注:有序的UUID生成工具类, UUID为32byte字符串.
 * 	<p>字符串组成:prefix_char + timestamp + uuid_mostSigBits  + uuid_leastSigBits 
 * <pre>
 * 使用实例:
 * <1>简单生成
 * String sid = SeqUUIDUtil.toSequenceUUID(); // <-- 根据调用方法的时间点,然后随机生成一个UUID对象
 * <2>根据时间戳生成
 * long current = System.currentTimeMillis();
 * String sid = SeqUUIDUtil.toSequenceUUID(current);
 * <3>根据UUID生成
 * UUID uuid = UUID.randomUUID();
 * String sid = SeqUUIDUtil.toSequenceUUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
 * <4>根据时间戳+UUID生成
 * long current = System.currentTimeMillis();
 * UUID uuid = UUID.randomUUID();
 * String sid = SeqUUIDUtil.toSequenceUUID(current, uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
 * <5>从有序的UUID中提取它生成时间点的时间戳
 * String sid = .... // <-- 一个有序的UUID字符串
 * long timestamp = SeqUUIDUtil.extractTimestamp(sid);
 * 特征:
 * long current0 = System.currentTimeMillis();
 * String sid = SeqUUIDUtil.toSequenceUUID(current0);
 * long current1 = SeqUUIDUtil.extractTimestamp(sid);
 *  存在: current0 == current1;
 * <6>从有序的UUID中提及uuid
 * UUID uuid0 = UUID.randomUUID();
 * String sid = SeqUUIDUtil.toSequenceUUID(uuid0.getMostSignificantBits(), uuid0.getLeastSignificantBits());
 * long m = SeqUUIDUtil.extractMostSignificantBits(sid);
 * long l = SeqUUIDUtil.extractLeastSignificantBits(sid);
 * UUID uuid1 = new UUID(m, l);
 * 存在: uuid0.equals(uuid1) == true;
 * </pre>
 * @author sekift
 * @teme 2019-5-19 上午11:31:56
 */
public final class SeqUUIDUtil {

	/**
	 * 默认前缀字符
	 */
	private static char DEFAULT_PREFIX_CHAR = 'o';
		
	/**
	 * 时间戳生成一个有序的uuid字符串
	 * 使用默认前缀字符(DEFAULT_PREFIX_CHAR)做为前缀
	 * @param timestamp -- 时间戳
	 * @return -- 有序的uuid字符串，长度为32位
	 */
	public static String toSequenceUUID(long timestamp) {
		return toSequenceUUID(DEFAULT_PREFIX_CHAR, timestamp);
	}
	
	/**
	 * 根据当前时间点的时间戳和一个UUID(UUID的高64位和低64位作为参数)生成一个有序的uuid字符串
	 * @param mostSigBits -- UUID的高64位
	 * @param leastSigBits -- UUID的低64位
	 * @return -- 有序的uuid字符串
	 */
	public static String toSequenceUUID(long mostSigBits, long leastSigBits) {
		return toSequenceUUID(DEFAULT_PREFIX_CHAR, System.currentTimeMillis(), mostSigBits, leastSigBits);
	}
	
	/**
	 * 根据时间戳和一个UUID(UUID的高64位和低64位作为参数)生成一个有序的uuid字符串
	 * @param timestamp -- 时间戳
	 * @param mostSigBits -- UUID的高64位
	 * @param leastSigBits -- UUID的低64位
	 * @return -- 有序的uuid字符串
	 */
	public static String toSequenceUUID(long timestamp, long mostSigBits, long leastSigBits) {
		return toSequenceUUID(DEFAULT_PREFIX_CHAR, timestamp, mostSigBits, leastSigBits);
	}
	
	/**
	 * 将一个前缀和时间戳生成一个有序的uuid字符串
	 * @param prefix -- 前缀字符, 必须是一个字节(byte)的字符
	 * @param timestamp -- 时间戳
	 * @return -- 有序的uuid字符串，长度为32位
	 */
	public static String toSequenceUUID(char prefix, long timestamp) {
		UUID u = UUID.randomUUID();
		return toSequenceUUID(prefix, timestamp, u.getMostSignificantBits(), u.getLeastSignificantBits());
	}
	
	/**
	 *  将一个[前缀,时间戳, UUID的高64bit, UUID的低64bit]生成一个有序的uuid字符串
	 * @param prefix -- 前缀字符, 必须是一个字节(byte)的字符
	 * @param timestamp -- 时间戳, 单位毫秒, 见System.currentTimeMillis();
	 * @param mostSigBits -- UUID的高64bit
	 * @param leastSigBits -- UUID的低64bit
	 * @return
	 */
	public static String toSequenceUUID(char prefix, long timestamp, long mostSigBits, long leastSigBits) {

		if (prefix > Byte.MAX_VALUE) {
			throw new IllegalArgumentException("prefix必须是一个字节(byte)的字符");
		}
		StringBuilder sb = new StringBuilder(32); 
		sb.append(prefix); 
		sb.append(BaseConvert.compressNumber(timestamp, 5));
		String m = BaseConvert.compressNumber(mostSigBits, 6);
		sb.append(m);
		if (m.length() < 11) {
			sb.append("_"); // 作为分隔符
		}
		sb.append(BaseConvert.compressNumber(leastSigBits, 6)); 
		int len = 32 - sb.length();
		if (len > 0) {
			sb.append("________________________________", 0, len); // 32 个'_' 作为补白
		}
		return sb.toString();
	}
	
	/**
	 * 根据当前时间戳为种子,生成一个有序的uuid字符串
	 * @return 有序的uuid字符串，长度为32位
	 */
	public static String toSequenceUUID() {
		return toSequenceUUID(System.currentTimeMillis());
	}
	
	/**
	 * 通过一个有序的uuid字符串中提取时间戳
	 * sequenceUUID必须通过UUIDUtil.toSequenceUUID方法生成
	 * @param sequenceUUID -- 有序uuid字符串
	 * @return -- 有序uuid中的时间戳
	 */
	public static long extractTimestamp(String sequenceUUID) {
		 
		return BaseConvert.decompressNumber(sequenceUUID.substring(1, 10), 5);
	}
	
	/**
	 * 通过一个有序的uuid字符串中提取UUID对象的高64bit
	 * @param sequenceUUID -- 有序的uuid
	 * @return -- UUID对象的高64bit
	 */
	public static long extractMostSignificantBits(String sequenceUUID) {
		
		int endIndex = sequenceUUID.indexOf((int)'_', 10);
		if (endIndex < 10) {
			endIndex = 21;
		}
		return BaseConvert.decompressNumber(sequenceUUID.substring(10, endIndex), 6);
	}
	
	/**
	 * 通过一个有序的uuid字符串中提取UUID对象的低64bit
	 * @param sequenceUUID -- 有序的uuid
	 * @return -- UUID对象的低64bit
	 */
	public static long extractLeastSignificantBits(String sequenceUUID) {

		int startIndex = sequenceUUID.indexOf((int)'_', 10) + 1; // 加1 为了跳过'_'分隔符
		if (startIndex < 10) {
			startIndex = 21;
		}
		int endIndex = sequenceUUID.indexOf((int)'_', startIndex);
		if (endIndex < 21) {
			endIndex = 32;
		}
		return BaseConvert.decompressNumber(sequenceUUID.substring(startIndex, endIndex), 6);
	}
	
	
	
	/**
	 * 进制转换类
	 *
	 */
	public static class BaseConvert {
		
		/**
		 * 进制编码表, 使用base64编码表作为基础, 使用'-'替换'+', 
		 * 目的是UUIDUtil使用生成的id可以在url不编码就可以传递
		 */
	    final static char[] digits = {  
	        '0' , '1' , '2' , '3' , '4' , '5' ,  
	        '6' , '7' , '8' , '9' , 'a' , 'b' ,  
	        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,  
	        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,  
	        'o' , 'p' , 'q' , 'r' , 's' , 't' ,  
	        'u' , 'v' , 'w' , 'x' , 'y' , 'z' ,  
	        'A' , 'B' , 'C' , 'D' , 'E' , 'F' ,  
	        'G' , 'H' , 'I' , 'J' , 'K' , 'L' ,  
	        'M' , 'N' , 'O' , 'P' , 'Q' , 'R' ,  
	        'S' , 'T' , 'U' , 'V' , 'W' , 'X' ,  
	        'Y' , 'Z' , '-' , '_' ,  
	        }; 
		
	    /** 
	     * 把10进制的数字转换成2^shift进制 字符串
	     * @param number 10进制数字
	     * @param shift 5表示32进制，6表示64进制，原理 2^shift
	     * @return 2^shift进制字符串
	     */  
	    public static String compressNumber(long number, int shift) {  
	       
	    	char[] buf = new char[64];  
	        int charPos = 64;  
	        int radix = 1 << shift;  
	        long mask = radix - 1;  
	        do {  
	            buf[--charPos] = digits[(int)(number & mask)];  
	            number >>>= shift;  
	        } while (number != 0);  
	        return new String(buf, charPos, (64 - charPos));  
	    }  
	    
	    /** 
	     * 把2^shift进制的字符串转换成10进制 
	     * @param decompStr 2^shift进制的字符串
	     * @param shift 5表示32进制，6表示64进制，原理 2^shift
	     * @return 10进制数字
	     */  
	    public static long decompressNumber(String decompStr, int shift) {  
	        
	    	long result = 0;  
	        for (int i = decompStr.length() - 1; i >= 0; i--) {  
	            if (i == decompStr.length() - 1) {  
	                result += getCharIndexNum(decompStr.charAt(i));  
	                continue;  
	            }  
	            for (int j = 0; j < digits.length; j++) {  
	                if (decompStr.charAt(i) == digits[j]) {  
	                    result += ((long)j) << shift * (decompStr.length() - 1 - i);  
	                }  
	            }  
	        }  
	        return result;  
	    }     
	    
	    /** 
	     * 将字符转成数值
	     * @param ch -- 字符
	     * @return -- 对应数值
	     */  
	    private static long getCharIndexNum(char ch) {  
	       
	    	int num = ((int)ch);  
	        if ( num >= 48 && num <= 57) {  
	            return num - 48;  
	        } else if (num >= 97 && num <= 122) {  
	            return num - 87;  
	        } else if (num >= 65 && num <= 90) {  
	            return num - 29;  
	        } else if (num == 43)  {  
	            return 62;  
	        } else if (num == 47)  {  
	            return 63;  
	        }  
	        return 0;  
	    }  
	}
}
