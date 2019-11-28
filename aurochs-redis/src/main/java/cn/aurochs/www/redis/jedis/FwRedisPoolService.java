package cn.aurochs.www.redis.jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.algorithm.HashAlgorithms;
import cn.aurochs.www.algorithm.NodeLocator;
import cn.aurochs.www.algorithm.NodeLocators;
import cn.aurochs.www.algorithm.NodeLocators.ConsistentHashLocator;
import cn.aurochs.www.redis.RedisPoolConfig;
import cn.aurochs.www.redis.RedisPoolService;
import cn.aurochs.www.redis.mbean.Fw;
import cn.aurochs.www.redis.mbean.RedisHealthCheck;
import cn.aurochs.www.util.JMXUtil;
import redis.clients.jedis.JedisPool;

public final class FwRedisPoolService implements RedisPoolService {

	private static final Logger logger = LoggerFactory
			.getLogger(FwRedisPoolService.class);

	// //**********注入属性，一般用于spring配置的注入
	private String redisPoolConfig;
	private String redisServerMapping;
	private String itemLocateAlgorithm;
	/**
	 * 服务Id
	 */
	private String serviceId;

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public String getRedisPoolConfig() {
		return redisPoolConfig;
	}

	@Override
	public void setRedisPoolConfig(String redisPoolConfig) {
		this.redisPoolConfig = redisPoolConfig;
	}

	@Override
	public String getRedisServerMapping() {
		return redisServerMapping;
	}

	@Override
	public void setRedisServerMapping(String redisServerMapping) {
		this.redisServerMapping = redisServerMapping;
	}

	@Override
	public String getItemLocateAlgorithm() {
		return itemLocateAlgorithm;
	}

	@Override
	public void setItemLocateAlgorithm(String itemLocateAlgorithm) {
		this.itemLocateAlgorithm = itemLocateAlgorithm;
	}

	// //**********

	/**
	 * initial flag
	 */
	private boolean initFlag = false;

	/**
	 * 数据库初始化标记
	 */
	private static boolean initPoolFlag = false;

	private NodeLocator<RedisPoolMasterSlaveGroup> groupLocator = null;

	/**
	 * 随机数生成器
	 */
	private static Random random = new Random();

	/**
	 * 保存当前失败(发生故障的从库别名集合)
	 */
	private Set<String> failSlaves = new HashSet<String>();

	private class RedisPoolMasterSlaveGroup {

		/**
		 * 主节点别名
		 */
		String masterAlias = null;

		/**
		 * 从节点别名数组
		 */
		String[] slaveAliases = null;

		/**
		 * 主节点+从节点 数组
		 */
		String[] allAliases = null;

		public RedisPoolMasterSlaveGroup(String group) {

			String[] g = group.trim().split(":");
			masterAlias = g[0];
			slaveAliases = g[1].split(";");
			allAliases = group.replace(":", ";").split(";");
		}

		public JedisPool getRandom() throws Exception {
			int ss = allAliases.length;
			int nextStart = random.nextInt(ss);
			return RedisPoolProvider.getPool(allAliases[nextStart]);
		}

		/**
		 * 获取从库的数据库连接资料(DbConnectProfile), 带故障转移功能
		 * 
		 * @return -- 数据库连接资料(DbConnectProfile)
		 * @throws Exception
		 */
		@SuppressWarnings("unused")
		public JedisPool getSlave() throws Exception {
			int ss = slaveAliases.length;
			int nextStart = random.nextInt(ss);
			return RedisPoolProvider.getPool(slaveAliases[nextStart]);
		}

		public JedisPool getMaster() throws Exception {
			return RedisPoolProvider.getPool(masterAlias);
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder();
			sb.append("RedisPoolMasterSlaveGroup{");
			sb.append("masterAlias:").append(masterAlias).append(",");
			for (int i = 0; i < slaveAliases.length; i++) {
				sb.append("slaveAliases[").append(i).append("]:")
						.append(slaveAliases[i]);
				if (i != slaveAliases.length) {
					sb.append(",");
				}
			}
			sb.append("}");
			return sb.toString();
		}
	}

	/**
	 * 插件配置参数集合
	 */
	public void initializePlugin() {
		// 同步所有插件初始化过程(初始化串行化)
		synchronized (FwRedisPoolService.class) {
			logger.info("====初始化redis连接服务(FwRedisPoolService)插件开始===");

			// 初始化连接池
			if (!initPoolFlag) {
				initPoolFlag = true;

				String redisPoolConfig = this.getRedisPoolConfig();
				RedisPoolConfig.init(redisPoolConfig);
				RedisPoolProvider.initial(redisPoolConfig);
				logger.info("redispool配置文件初始化全局配置");
			}
			if (!initFlag) {
				initFlag = true;

				// 初始化插件上下文

				String locateAlgorithm = this.getItemLocateAlgorithm();
				locateAlgorithm = (null != locateAlgorithm && !"".equals(locateAlgorithm)) ? locateAlgorithm : "consistent-hash"; // 默认值

				String mapping = this.getRedisServerMapping();//
				if (null == mapping || "".equals(mapping)) {
					throw new RuntimeException("必须配置redisServerMapping属性！");
				}
				
				// 只用一致性哈希进行分库
				String[] items = mapping.trim().split(",");
				ConsistentHashLocator<RedisPoolMasterSlaveGroup> locator = new NodeLocators.ConsistentHashLocator<RedisPoolMasterSlaveGroup>();
				// 设置统一哈希算法
				locator.setHashAlgorithm(HashAlgorithms.KEMATA_HASH);
				long[] candidates = locator.nextCandidates(items.length);
				Map<Long, RedisPoolMasterSlaveGroup> nodes = new HashMap<Long, RedisPoolMasterSlaveGroup>();
				for (int i = 0; i < items.length; i++) {
					RedisPoolMasterSlaveGroup group = new RedisPoolMasterSlaveGroup(
							items[i]);
					Long groupNo = Long.valueOf(candidates[i]);
					nodes.put(groupNo, group);
				}
				locator.setNodes(nodes);
				groupLocator = locator;
			}
			
			// 注册MBean
			initRedisHealthMBean();
			logger.info("====初始化redis连接服务(FwRedisPoolService)插件结束===");
		}

	}

	/**
	 * 现在使用的方法
	 * @param rwMode
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public JedisPool getPoolByKemataHash(String rwMode, String key)
			throws Exception {
		long identityHashCode = HashAlgorithms.KEMATA_HASH.hash(key);
		return getPool(rwMode, identityHashCode);
	}

	public JedisPool getPool(String rwMode, long identityHashCode)
			throws Exception {
		RedisPoolMasterSlaveGroup group = groupLocator.locate(identityHashCode,
				NodeLocator.NULL_STRATEGY);
		JedisPool p = null;
		if ("w".equalsIgnoreCase(rwMode)) {
			p = group.getMaster();
		} else { // 当前实现不区分: rw 和 r模式, 相同处理
			p = group.getRandom();
		}
		return p;
	}

	public JedisPool getPool(String alias) throws Exception {
		if (failSlaves.contains(alias)) {
			return null;
		}
		return RedisPoolProvider.getPool(alias);
	}
	
	/**
	 * 初始化Redis可用性检查MBean
	 * 
	 */
	private void initRedisHealthMBean() {
		RedisHealthCheck redisHealthCheck;
		RedisHealthCheck redisHealthCheckTmp;

		try {
			// 拆分主从配置，根据alias，获取连接url地址
			// 从一个appid配置中拆分出主从组
			String[] masterSlaverGroup = this.getRedisServerMapping().split(",");
			// 对主从组循环，
			for (int i = 0; i < masterSlaverGroup.length; i++) {
				// 拆分出主节点，和从节点串
				String[] masterSlaver = masterSlaverGroup[i].split(":");
				// 针对同一个appid的同一个主从组，循环，
				List<RedisHealthCheck> redisHealthCheckList = new ArrayList<RedisHealthCheck>();
				for (int j = 0; j < masterSlaver.length; j++) {
					// 拆分出每个节点，包括主节点和从节点.masterSlaver[0]是master
					String[] alias = masterSlaver[j].split(";");
					for (int k = 0; k < alias.length; k++) {
						// 下面是实例化mbean的入口点
						// 先获取IP
						Map<String, String> redisUrlMap = RedisPoolConfig.getRedisUrl(alias[k]);
						if (null == redisUrlMap || redisUrlMap.isEmpty()) {
							break;
						}
						
						// 构建一个临时list，在接下来的步骤中用来剔重
						redisHealthCheckTmp = new RedisHealthCheck(serviceId, redisUrlMap.get("ip"), redisUrlMap.get("port"), Long.valueOf(redisUrlMap.get("timeout")));
						redisHealthCheckList.add(redisHealthCheckTmp);
						redisHealthCheckTmp = null;
					}
				}
				// 剔除重复的代码,先把第一个注册，并添加到redisHealthCheckList中，然后对其余的遍历剔除重复；
				RedisHealthCheck compareObj = redisHealthCheckList.get(0);
				redisHealthCheck= new RedisHealthCheck(serviceId, compareObj.getTargetIp(), compareObj.getTargetPort(),  compareObj.getTargetTimeout());
				String oname = JMXUtil.createObjectNameString(Fw.FW_USABILITY_DOMAIN, RedisHealthCheck.TYPE, serviceId, compareObj.getTargetIp() + "-" + compareObj.getTargetPort());
				JMXUtil.registerMBean(redisHealthCheck, oname);
				logger.info(" RedisHealthCheck 注册:" + oname);
				redisHealthCheck = null;

				for (int m = 1; m < redisHealthCheckList.size(); m++) {
					RedisHealthCheck tmp = redisHealthCheckList.get(m);
					if (compareObj.equals(tmp)) {
						continue;
					}
					redisHealthCheck = new RedisHealthCheck(serviceId, tmp.getTargetIp(), tmp.getTargetPort(), tmp.getTargetTimeout());
					oname = JMXUtil.createObjectNameString(Fw.FW_USABILITY_DOMAIN, RedisHealthCheck.TYPE, null, tmp.getTargetIp() + "-" + tmp.getTargetPort());
					JMXUtil.registerMBean(redisHealthCheck, oname);
					logger.info(" RedisHealthCheck 注册:" + oname);
					redisHealthCheck = null;
				}

			}

		} catch (Throwable e) {
			logger.error("initRedisHealthMBean error", e);
		}

	}

}
