package cn.aurochs.www.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定位器工厂
 * @author:sekift
 * @time:2018-05-18 下午02:59:17
 * @version:1.0.0
 */
public final class NodeLocators {
    
    private static final Logger logger = LoggerFactory.getLogger(NodeLocators.class);
    
	/**
	 * 查找可更新节点 策略代码
	 */
	public static final int UPDATE_LOOKUP = 0x01; // 随机赋值;
	
	/**
	 * 查找只读节点 策略代码
	 */
	public static final int READONLY_LOOKUP = 0x02; // 随机赋值;
	
	
	/**
	 * 创建 取模算法 节点定位器
	 * @return 节点定位器
	 */
	public static <T> NodeLocator<T> newModLocator(HashAlgorithm ha, List<T> nodes) {
		
		Map<Long, T> ns = new HashMap<Long, T>(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			ns.put(Long.valueOf(i), nodes.get(i));
		}
		NodeLocator<T> locator =  new ModLocator<T>();
		locator.setNodes(ns);
		locator.setHashAlgorithm(ha);
		return locator;
	}
	
	/**
	 * 创建 随机算法 节点定位器
	 * @return 节点定位器
	 */
	public static <T> NodeLocator<T> newRandomLocator(List<T> nodes) {

		RandomNodeLocator<T> locator =  new RandomNodeLocator<T>();
		locator.nodes = nodes;
		return locator; 
	}
	
	/**
	 * 创建 随机算法 节点定位器
	 * @return 节点定位器
	 */
	public static <T> NodeLocator<T> newRandomLocator(Map<Long, T> nodes) {

		List<T> ns = new ArrayList<T>(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			Long index = Long.valueOf(i);
			ns.add(nodes.get(index));
		}
		RandomNodeLocator<T> locator =  new RandomNodeLocator<T>();
		locator.nodes = ns;
		return locator; 
	}
	
	/**
	 * 创建 一致性hash算法 节点定位器
	 * @param ha -- key的hash值产生算法
	 * @param nodes -- 一致性hash环上的节点集合. map的key的值为节点在环上的位置
	 * @return -- 节点定位器
	 */
	public static <T> NodeLocator<T> newConsistentHashLocator(HashAlgorithm ha, Map<Long, T> nodes) {
	
		ConsistentHashLocator<T> locator = new ConsistentHashLocator<T>();
		locator.setHashAlgorithm(ha);
		locator.setNodes(nodes);
		return locator;
	}
	
	/**
	 * 创建 主从节点不同查找算法 节点定位器
	 * @param master -- 主节点
	 * @param slaveLocator -- 从节点的 节点定位器
	 * @return -- 节点定位器
	 */
	public static <T> NodeLocator<T> newMasterSlaveLocator(T master, NodeLocator<T> slaveLocator) { 
		NodeLocator<T> locator = new MasterSlaveNodeLocator<T>(master, slaveLocator); 
		return locator;
	}
	
	/**
	 * 创建 嵌套查找算法 节点定位器
	 * @param innerLocator -- 内部被嵌套的定位器
	 * @return -- 节点定位器
	 */
	public static <T> NodeLocator<T> newNestedLookupLocator(NodeLocator<T> innerLocator) {
		 
		NodeLocator<T> locator = new NestedLookupNodeLocator<T>(innerLocator); 
		return locator;
	}
	
	/**
	 * 创建Ketama统一哈希算法定位器
	 * @param <T>
	 * @param ha
	 * @param nodes
	 * @return
	 */
	public static <T> NodeLocator<T> newKetamaConsistentHashLocator(HashAlgorithm ha, List<T> nodes) {
		KetamaConsistentHashLocator<T> locator = new KetamaConsistentHashLocator<T>(ha,nodes);
		return locator;
	}
	

	/**
	 * 防止被类被非法实例化
	 */
	private NodeLocators() {}
	
	/**
	 * 使用 取模算法 的节点定位器
	 */
	public static class ModLocator<T> implements NodeLocator<T> {
		
		/**
		 * hash算法
		 */
		protected HashAlgorithm ha = null;
		
		/**
		 * 节点集合
		 */
		protected SortedMap<Long, T> nodes = null;

		/**
		 * 定位节点
		 */
		public T locate(String key, int strategyCode) { 
			long h = ha.hash(key);
			return locate(h, strategyCode);
		}
		
		/*
		 * 定位节点
		 */
		public T locate(long hashCode, int strategyCode) {
			long iden = Math.abs(hashCode) % nodes.size(); 
			return nodes.get(Long.valueOf(iden));
		}

		public void setHashAlgorithm(HashAlgorithm hashStrategy) { 
			this.ha = hashStrategy;
		}

		public void setNodes(Map<Long, T> ns) { 
			this.nodes = new TreeMap<Long, T>(ns);
		}

		public Map<Long, T> getNodes() { 
			return nodes;
		}

		public HashAlgorithm getHashAlgorithm() { 
			return ha;
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder(256);
			sb.append("@NodeLocators$ModLocator{");
			sb.append(" ha:").append(ha).append(",");
			sb.append(" nodes:").append(nodes);
			sb.append("}");
			return sb.toString();
		}
	}

	/**
	 * 一致性hash 节点定位器
	 * <br>备注:一致性hash的值范围[0, 2^32)=[0, 4294967295]
	 */
	public static class ConsistentHashLocator<T> implements NodeLocator<T> {
		
		/**
		 * hash算法
		 */
		protected HashAlgorithm ha = null;
		
		/**
		 * 节点集合
		 */
		protected SortedMap<Long, T> nodes = null;

		/*
		 * 定位节点
		 */
		public T locate(String key, int strategyCode) {
			long h = ha.hash(key);
			return locate(h, strategyCode);
		}
		
		/*
		 * 定位节点
		 */
		public T locate(long hashCode, int strategyCode) {
			
			long h = toConsistentHash(hashCode);
			Long k = Long.valueOf(h);
			SortedMap<Long, T> tails = nodes.tailMap(k);
			if (tails.size() > 0) {
				return nodes.get(tails.firstKey());
			} else { 
				return nodes.get(nodes.firstKey());
			} 
		}

		public Map<Long, T> getNodes() { 
			return nodes;
		}

		public HashAlgorithm getHashAlgorithm() { 
			return ha;
		}
		
		/**
		 * 将hashCode转成一致性hash的值范围[0, 2^32)=[0, 4294967295]
		 * @param hashCode -- hash值
		 * @return -- 一致性hash值
		 */
		public long toConsistentHash(long hashCode) {
			return hashCode  & 0xffffffffL;
		}

		/*
		 * 使用ConsistentHash包装hashStrategy
		 */
		public void setHashAlgorithm(HashAlgorithm hashStrategy) {
			this.ha = hashStrategy;
		}

		/*
		 * 进行key的值范围检查
		 */
		public void setNodes(Map<Long, T> ns) { 	
						
			TreeMap<Long, T> n = new TreeMap<Long, T>(ns);
			if (!n.isEmpty()) {
				// 检查hash值范围
				Long firstKey = n.firstKey();
				Long lastKey = n.lastKey();
				checkHashRange(firstKey.longValue());
				checkHashRange(lastKey.longValue());			
			}
			this.nodes = n;
		}
		

		
		/**
		 * 增加节点
		 * @param no -- 节点编号
		 * @param node -- 节点
		 */
		public void addNode(long no, T node) {		
			checkHashRange(no);
			this.nodes.put(Long.valueOf(no), node);
		}
		
		/**
		 * 移除节点
		 * @param no -- 节点编号
		 * @return -- 节点, 如果没有节点返回null
		 */
		public Object removeNode(long no) {			
			checkHashRange(no);
			return this.nodes.remove(Long.valueOf(no));
		} 
		
		/**
		 * 获取环顺时针节点迭代器
		 * @param start -- 开始节点编号
		 * @param startInclude -- 迭代器是否包含开始节点
		 * @return -- 迭代器
		 */
		public Iterator<T> clockwiseNodeIterator(long start, boolean startInclude) {

			checkHashRange(start);
			long no = startInclude ? start : start + 1;
			ClockwiseNodeIterator<T> it = new ClockwiseNodeIterator<T>(no, nodes, nodes.size());
			return it;
		}
		
		/**
		 * 获取环顺时针节点迭代器
		 * @param key -- key
		 * @return -- 迭代器
		 */
		public Iterator<T> clockwiseNodeIterator(final String key) {
			
			long k = toConsistentHash(ha.hash(key));
			ClockwiseNodeIterator<T> it = new ClockwiseNodeIterator<T>(k, nodes, nodes.size());
			return it;
		}
		
		/**
		 * 获取环逆时针节点迭代器
		 * @param start -- 开始节点编号
		 * @param startInclude -- 迭代器是否包含开始节点
		 * @return -- 迭代器
		 */
		public Iterator<T> counterclockwiseNodeIterator(long start, boolean startInclude) {

			checkHashRange(start);
			long no = startInclude ? start : start - 1;
			CounterclockwiseNodeIterator<T> it = new CounterclockwiseNodeIterator<T>(no, nodes, nodes.size());
			return it;
		}
		
		/**
		 * 获取环逆时针节点迭代器
		 * @param key -- key
		 * @return -- 迭代器
		 */
		public Iterator<T> counterclockwiseNodeIterator(final String key) {
			
			long k = toConsistentHash(ha.hash(key));
			CounterclockwiseNodeIterator<T> it = new CounterclockwiseNodeIterator<T>(k, nodes, nodes.size());
			return it;
		}
		

		/**
		 * 引用一个节点编号获取下一个获选编号
		 * 备注: 用于分摊一个引用节点的请求压力时使用
		 * @param quote -- 引用节点
		 * @return -- 候选节点编号
		 */
		public long nextCandidate(long quote) {
			
			if (nodes.isEmpty()) {
				throw new IllegalStateException("一致性hash环中没有节点");
			}
			checkHashRange(quote); // 检查引用节点的值范围
			Long quoteKey = Long.valueOf(quote);
			if (!nodes.containsKey(quoteKey)) { // 引用节点不存在
				throw new IllegalStateException("一致性hash环中不存在引用节点(quote):" + quote);
			}
			
			SortedMap<Long, T> heads = nodes.headMap(quoteKey);
			if (heads.isEmpty()) { 
				if (0 == quote) {
					Long last = nodes.lastKey();
					return last.longValue() + (4294967296L - last.longValue()) / 2;  // 如果引用节点为0,则转成2^32(4294967296L)值来计算
				} else {
					return quote / 2;
				} 
			} else {
				Long last = heads.lastKey();
				return last.longValue() + (quote - last.longValue()) / 2;
			}
		}
		
		/**
		 * 返回指定数量的候选节点编号
		 * @param count -- 指定的数量
		 * @return -- 候选节点编号数组
		 */
		public long[] nextCandidates(int count) {
			
			if (count < 1) {
				throw new IllegalArgumentException("候选数据不能少于1");
			}	
			
			SortedMap<Long, Object> ns = new TreeMap<Long, Object>();
			if (null != nodes) {
				ns.putAll(nodes);
			}
			long[] candidates = new long[count];
			for (int i = 0; i < count; i++) {
				long next = nextCandidate(ns);
				candidates[i] = next;
				ns.put(Long.valueOf(next), new Object());
			}
			return candidates;
		}
		
		/**
		 * 获取下一个候选节点编号
		 * @param ns -- 环节点集合
		 * @return -- 候选节点编号
		 */
		private long nextCandidate(SortedMap<Long, Object> ns) {
			
			if (ns.isEmpty()) { // 环中没有节点
				return 0L;
			} else if (ns.size() == 1) { // 环中节点数为 1
				long first = ns.firstKey().longValue();
				if (first > 2147483648L) {  // 超过 2^32(4294967296L)值的一半了,说明下个点在对面
					return first - 2147483648L;
				} else {
					return first + 2147483648L;
				}
			}
            // 环中节点数大于1
			
			// 算出环中每两个节点的距离
			long last = ns.firstKey().longValue();
			SortedMap<Long, Object> tails = ns.tailMap(Long.valueOf(last + 1));
			long [] ranges = new long[ns.size()]; 
			int index = 0;
			for (Long key : tails.keySet()) {
				long next = key.longValue();
				long range = next - last; 
				last = next;
				ranges[index] = range;
				index++;
			}
			// 算最后一个节点与第一个节点的距离
			ranges[ranges.length - 1] = 4294967296L - ns.lastKey().longValue() + ns.firstKey().longValue();
			
			// 求对大的距离与在集合中的偏移值
			int maxIndex = 0;
			long maxRange = 0;
			for (int i = 0; i < ranges.length; i++) {
				if (ranges[i] > maxRange) {
					maxRange = ranges[i];
					maxIndex = i;
				}
			}
			// 定位最大距离的节点值
			Iterator<Long> it = ns.keySet().iterator();
			Long nodeIndex = null;
			for (int i = 0; i <= maxIndex; i++) {
				nodeIndex = it.next();
			}
			
			// 算最大距离的候选节点值
			long c = nodeIndex.longValue() + maxRange / 2;
			if (c < 4294967296L) { // 候选节点值没有超出一致性hash环值的范围
				return c;
			} else if (c > 4294967296L) {
				return ns.firstKey() - maxRange / 2;
			} else {
				return 0L;
			} 
		}
		
		/**
		 * 检查hash值是否满足一致性hash的值范围要求
		 * @param key -- 被检查的hash值
		 */
		private void checkHashRange(long hash) {
			if (hash < 0) {
				throw new IllegalArgumentException("一致性hash的hash值范围[0, 2^32)=[0, 4294967295], 不能存在小于0的节点:" + hash);
			} 
			if (hash > 4294967295L) {
				throw new IllegalArgumentException("一致性hash的hash值范围[0, 2^32)=[0, 4294967295], 不能存在大于4294967295的节点:" + hash);
			}
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder(256);
			sb.append("@NodeLocators$ConsistentHashLocator{");
			sb.append(" ha:").append(ha).append(",");
			sb.append(" nodes:").append(nodes);
			sb.append("}");
			return sb.toString();
		}
 
		/**
		 * 顺时针节点迭代器
		 */
		public static class ClockwiseNodeIterator<T> implements Iterator<T> {
			
			private int remaining = 0;
			
			private SortedMap<Long, T> nodes = null;

			private long currentKey = -1;
			
			public ClockwiseNodeIterator(long current, SortedMap<Long, T> ns, int remaining) {
				
				this.remaining = remaining;
				this.nodes = ns;
				this.currentKey = current;
			}

			public boolean hasNext() {
				return (remaining > 0);
			}

			public T next() {
				
				remaining--;
				T n = nodes.get(currentKey);
				long nextKey = currentKey + 1;
				if (null == n) {
					SortedMap<Long, T> tails = nodes.tailMap(currentKey);
					Long firstKey = null;
					if (tails.isEmpty()) {
						firstKey = nodes.firstKey();
						n = nodes.get(firstKey); 
					} else {
						firstKey = tails.firstKey();
						n = nodes.get(firstKey);
					}
					nextKey = firstKey.intValue() + 1;
				} 
				currentKey = nextKey;
				return n;
			}

			public void remove() {				
				throw new UnsupportedOperationException("不支持此操作");
			} 
						
			@Override
			public String toString() {

				StringBuilder sb = new StringBuilder();
				sb.append("ClockwiseNodeIterator{"); 
				sb.append("currentKey").append(currentKey).append(",");
				sb.append("remaining").append(remaining).append(",");
				sb.append("nodes").append(nodes);
				sb.append("}");
				return sb.toString();
			}
		}
		
		/**
		 * 逆时针节点迭代器
		 */
		public static class CounterclockwiseNodeIterator<T> implements Iterator<T> {
			
			private SortedMap<Long, T> nodes = null; 
			private long currentKey = -1;
			private int remaining = 0;
			
			public CounterclockwiseNodeIterator(long current, SortedMap<Long, T> nodes, int remaining) {
				  
				this.currentKey = current;
				this.nodes = nodes;
				this.remaining = remaining;
			}

			public boolean hasNext() {
				
				return remaining > 0;
			}

			public T next() {
				
				remaining--;
				T n = nodes.get(currentKey);
				long nextKey = currentKey - 1;
				if (null == n) {
					SortedMap<Long, T> heads = nodes.headMap(Long.valueOf(currentKey + 1));
					Long lastKey = null;
					if (heads.isEmpty()) {
						lastKey = nodes.lastKey();
						n = nodes.get(lastKey); 
					} else {
						lastKey = heads.lastKey();
						n = nodes.get(lastKey);
					}
					nextKey = lastKey.intValue() - 1;
				} 
				currentKey = nextKey;
				return n;
			}

			public void remove() {
				throw new UnsupportedOperationException("不支持移除操作");
			}	 	
			
			@Override
			public String toString() {

				StringBuilder sb = new StringBuilder();
				sb.append("CounterclockwiseNodeIterator["); 
				sb.append("currentKey").append(currentKey).append(",");
				sb.append("remaining").append(remaining).append(",");
				sb.append("nodes").append(nodes);
				sb.append("]");
				return sb.toString();
			}
		}
	}
	 	
	/**
	 * 随机算法 节点定位器
	 */
	public static class RandomNodeLocator<T> implements NodeLocator<T> {
		
		/**
		 * hash算法
		 */
		protected HashAlgorithm ha = null;
		
		/**
		 * 节点集合
		 */
		protected List<T> nodes = null;
		
		private Random random = new Random();

		public T locate(String key, int strategyCode) {
			int index = random.nextInt(nodes.size());
			return nodes.get(index);
		}
		
		public T locate(long hashCode, int strategyCode) {
			int index = random.nextInt(nodes.size());
			return nodes.get(index);
		}

		public void setHashAlgorithm(HashAlgorithm hash) {
			this.ha = hash;
		}

		public void setNodes(Map<Long, T> ns) {

			nodes = new ArrayList<T>();
			for (T item : ns.values()) {
				nodes.add(item);
			}
		}

		public Map<Long, T> getNodes() { 
			
			Map<Long, T> ns = new TreeMap<Long, T>();
			for (int i = 0; i < nodes.size(); i++) {
				ns.put(Long.valueOf(i), nodes.get(i));
			}
			return ns;
		}

		public HashAlgorithm getHashAlgorithm() { 
			return ha;
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder(256);
			sb.append("@NodeLocators$RandomNodeLocator{");
			sb.append(" ha:").append(ha).append(",");
			sb.append(" nodes:").append(nodes);
			sb.append("}");
			return sb.toString();
		}
	}

	/**
	 * 嵌套查找 节点定位器
	 */
	public static class NestedLookupNodeLocator<T> implements NodeLocator<T> {

		/**
		 * 节点定位器
		 */
		protected NodeLocator<T> locator = null;
			
		public NestedLookupNodeLocator(NodeLocator<T> loc) {
			
			locator = loc;
		}
		
		/**
		 * 查找节点, 当查找到的节点如果是节点定位器,进行嵌套查找
		 * @param key -- 查找的key
		 * @param strategyCode -- 查找策略代码
		 * @return -- 节点
		 */
		@SuppressWarnings("unchecked")
		public T locate(String key, int strategyCode) {
			 
			NodeLocator<T> customLocator = locator;
			T node = customLocator.locate(key, strategyCode);
			while (node instanceof NodeLocator) {
				customLocator = (NodeLocator<T>)node;
				node = customLocator.locate(key, strategyCode);
			}
			return (T)node;
		}
		
		/*
		 * 查找节点, 当查找到的节点如果是节点定位器,进行嵌套查找
		 */
		@SuppressWarnings("unchecked")
		public T locate(long hashCode, int strategyCode) {
			 
			NodeLocator<T> customLocator = locator;
			T node = customLocator.locate(hashCode, strategyCode);
			while (node instanceof NodeLocator) {
				customLocator = (NodeLocator<T>)node;
				node = customLocator.locate(hashCode, strategyCode);
			}
			return (T)node;
		}

		public void setHashAlgorithm(HashAlgorithm hash) { 
			
			locator.setHashAlgorithm(hash);
		}

		public void setNodes(Map<Long, T> ns) { 
			
			locator.setNodes(ns);
		}

		public Map<Long, T> getNodes() { 
			return locator.getNodes();
		}

		public HashAlgorithm getHashAlgorithm() { 
			return locator.getHashAlgorithm();
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder(256);
			sb.append("@NodeLocators$NestedLookupNodeLocator{");
			sb.append(" locator:").append(locator);
			sb.append("}");
			return sb.toString();
		}
	}
	
	/**
	 * 主从 节点定位器
	 */
	public static class MasterSlaveNodeLocator<T> implements NodeLocator<T> {
		 
		/**
		 * 主节点
		 */
		protected T master = null;
		
		/**
		 * 从节点集合的 节点定位器
		 */
		protected NodeLocator<T> slaveLocator = null;
			
		public MasterSlaveNodeLocator(final T master, final NodeLocator<T> slaveLocator) {
			
			this.master = master;
			this.slaveLocator = slaveLocator;
		}
		
		/**
		 * 查找节点
		 * @param key -- 查找的key
		 * @param strategyCode -- 查找策略代码
		 * @return -- 节点
		 */
		public T locate(String key, int strategyCode) {
			 
			if ((strategyCode & UPDATE_LOOKUP) == UPDATE_LOOKUP) {
				return master;
			} else if ((strategyCode & READONLY_LOOKUP) == READONLY_LOOKUP) {
				return slaveLocator.locate(key, strategyCode);
			} else {
				return master;
			}
		}
		
		/**
		 * 查找节点
		 * @param key -- 查找的key
		 * @param strategyCode -- 查找策略代码
		 * @return -- 节点
		 */
		public T locate(long hashCode, int strategyCode) {
			 
			if ((strategyCode & UPDATE_LOOKUP) == UPDATE_LOOKUP) {
				return master;
			} else if ((strategyCode & READONLY_LOOKUP) == READONLY_LOOKUP) {
				return slaveLocator.locate(hashCode, strategyCode);
			} else {
				return master;
			}
		}

		public void setHashAlgorithm(HashAlgorithm hash) { 
			
			slaveLocator.setHashAlgorithm(hash);
		}

		public void setNodes(Map<Long, T> slaves) { 
			
			slaveLocator.setNodes(slaves);
		}

		public Map<Long, T> getNodes() { 
			return slaveLocator.getNodes();
		}

		public HashAlgorithm getHashAlgorithm() { 
			return slaveLocator.getHashAlgorithm();
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder(256);
			sb.append("@NodeLocators$MasterSlaveNodeLocator{");
			sb.append(" master:").append(master).append(",");
			sb.append(" slaveLocator:").append(slaveLocator);
			sb.append("}");
			return sb.toString();
		}
	}
	
	/**
	 * Ketama统一哈希 （memcached spy驱动所实现的统一哈希算法）
	 * @author whx
	 *
	 * @param <T>
	 */
	public static class KetamaConsistentHashLocator<T> implements NodeLocator<T> {
		
		/**
		 * hash算法
		 */
		protected HashAlgorithm ha = null;
		
		/**
		 * 节点集合
		 */
		protected SortedMap<Long, T> circle = new TreeMap<Long, T>();
		
		/**
		 * 虚拟结点的个数，默认是200
		 */
		private int numberOfReplicas = 200;
		
		public KetamaConsistentHashLocator(HashAlgorithm ha, Collection<T> nodes){
			this.ha = ha;
			
		    for (T node : nodes) {
		        addNode(node);
		     }
		}
		
		/**
		 * 添加节点
		 * @param node
		 */
		private void addNode(T node) {
			for (int i = 0; i < numberOfReplicas; i++) {
				String nodeId = node.toString() + i;
				circle.put(ha.hash(nodeId), node);
                logger.debug("Memcached virtual nodeId = [{}], Hash value = [{}]",nodeId,ha.hash(nodeId));
			}
		}

		/* (non-Javadoc)
		 */
		public void setNodes(Map<Long, T> ns) {
			for (T item : ns.values()) {
				addNode(item);
			}
		}

		/* (non-Javadoc)
		 */
		public Map<Long, T> getNodes() {
			return circle;
		}

		/* (non-Javadoc)
		 */
		public void setHashAlgorithm(HashAlgorithm hash) {
			this.ha = hash;
		}

		/* (non-Javadoc)
		 */
		public HashAlgorithm getHashAlgorithm() {
			return ha;
		}

		/* (non-Javadoc)
		 */
		public T locate(String key, int strategyCode) {
			if (circle.isEmpty()) {
				return null;
			}
			long hash = ha.hash(key);
            logger.trace("key[{}]'s hash value = {}",key, hash);
			if (!circle.containsKey(hash)) {
				SortedMap<Long,T> tailMap = circle.tailMap(hash);
				hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
			}
			return circle.get(hash);
		}

		/* (non-Javadoc) 
		 */
		public T locate(long hashCode, int strategyCode) {
			if (circle.isEmpty()) {
				return null;
			}
			if (!circle.containsKey(hashCode)) {
				SortedMap<Long,T> tailMap = circle.tailMap(hashCode);
				hashCode = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
			}
			return circle.get(hashCode);
		}
		
	}
}
