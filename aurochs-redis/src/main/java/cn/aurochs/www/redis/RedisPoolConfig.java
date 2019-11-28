package cn.aurochs.www.redis;


import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.util.BeanUtil;
import cn.aurochs.www.util.StringUtil;
import cn.aurochs.www.util.XmlUtil;

/**
 * 用于保存 redis_pool配置信息,在初始化数据库连接服务时候注入配置信息
 */
public class RedisPoolConfig {
	/**
	 * logger
	 */
	private static Logger logger = LoggerFactory
			.getLogger(RedisPoolConfig.class);

	public static String DEFALUT_CONFIG = "/config/redis_pool.xml";// 默认路径

	private static List<Map<String, Object>> aliasList;

	public static List<Map<String, Object>> getAliasList() {
		return aliasList;
	}

	/**
	 * 初始化,在初始化数据库连接服务时候调用
	 */
	@SuppressWarnings("rawtypes")
	public static void init(String proxoolFilePath) {
		if (null == proxoolFilePath || proxoolFilePath.equals("")) {
			proxoolFilePath = DEFALUT_CONFIG;
		}
		try {
			InputStream in = RedisPoolConfig.class
					.getResourceAsStream(proxoolFilePath);
			Map proxoolElementmap = XmlUtil.toMap(in);
			aliasList = BeanUtil
					.wrapToList(proxoolElementmap.get("redis-pool"));
			logger.info("获取redis-pool配置文件信息,");
		} catch (Exception ex) {
			throw new RuntimeException("获取redis-pool配置文件信息异常,proxoolFilePath="+proxoolFilePath+": ", ex);
		}
	}

	/**
	 * 通过连接池的别名获取Redis url
	 * 
	 * @param alias
	 *            -- 连接池别名
	 * @return redis url
	 */
	public static Map<String, String> getRedisUrl(String alias) {
		Map<String, String> redisMap = new HashMap<String, String>();
		if (StringUtil.isNullOrBlank(alias)) {
			return redisMap;
		}
		
		String ip = "0";
		String port = "0";
		String timeout = "0";
		try {
			for (int i = 0; i < aliasList.size(); i++) {
				Map<String, Object> aliasNodemap = BeanUtil.wrapToMap(aliasList
						.get(i));
				if (aliasNodemap.containsValue(alias)) {
					// 获取IP
					ip = (String) aliasNodemap.get("server");
					// 判断IP是否正确 暂时不写
					if (StringUtil.isNullOrBlank(ip)) {
						logger.warn("getRedisUrl()提示:端口:" + ip
								+ "不存在,请检查配置是否正确配置了服务IP!");
					}
					redisMap.put("ip", ip);
					// 获取port
					port = (String) aliasNodemap.get("port");
					Pattern pattern = Pattern.compile("[0-9]*");
					Matcher isNum = pattern.matcher(port);
					if (!isNum.matches()) {
						logger.warn("getRedisUrl()提示:端口:" + port
								+ "包含非数字字符,请检查配置是否正确配置了服务端口!");
					}
					redisMap.put("port", port);
					// 获取timeout
					timeout = (String) aliasNodemap.get("timeout");
					isNum = pattern.matcher(timeout);
					if (!isNum.matches()) {
						logger.warn("getRedisUrl()提示:超时时间:" + timeout
								+ "包含非数字字符,请检查配置是否正确配置了服务超时时间!");
					}
					redisMap.put("timeout", timeout);
					break;
				}
			}
		} catch (Throwable e) {
			logger.error("getRedisUrl error", e);
		}
		return redisMap;
	}
}
