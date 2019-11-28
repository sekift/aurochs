package cn.aurochs.www.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BeanUtil {

	/**
	 * 如果bean是List接口的一个实例,就直接返回
	 * 如果bean不是List接口的一个实例,将其使用List对象将其包装
	 * @param <T> -- 多态类型
	 * @param bean -- 被包装的bean
	 * @return -- 包装了bean的List
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> wrapToList(Object bean) {
		
		if (List.class.isInstance(bean)) {
			return (List<T>) bean;
		} else {
			List<T> target = new ArrayList<T>(1);
			if (null != bean) {
				target.add((T) bean);
			}
			return target; 
		}
	}
	
	/**
	 * 如果bean是Map接口的一个实例,就直接返回
	 * 如果bean不是Map接口的一个实例,将返回一个空元素(map.size()等于0)的map对象
	 * @param <K> -- 多态map的key类型
	 * @param <T> -- 多态map的value类型
	 * @param bean -- 被包装的bean
	 * @return -- 包装了bean的map
	 */
	@SuppressWarnings("unchecked")
	public static <K, T> Map<K, T> wrapToMap(Object bean) {
		
		if (Map.class.isInstance(bean)) {
			return (Map<K, T>)bean;
		} else {
			Map<K, T> target = new ConcurrentHashMap<K, T>(); 
			return target;
		} 
	}
	
	/**
	 * 将bean转成int类型
	 * @param bean -- 被转换的bean
	 * @param defaultValue -- 默认值,如果bean为null时返回
	 * @return -- bean转成的in值
	 */
	public static int toInt(Object bean, int defaultValue) { 
		try{
			return (null != bean) ? Integer.parseInt(bean.toString()) : defaultValue;
		}catch(Exception e){
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	/**
	 * 将bean转成int类型
	 * @param bean -- 被转换的bean
	 * @param defaultValue -- 默认值,如果bean为null时返回
	 * @return -- bean转成的in值
	 */
	public static int toInt(String bean, int defaultValue) {
		try{
			return (null != bean) ? Integer.parseInt(bean.toString()) : defaultValue;
		}catch(Exception e){
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	/**
	 * 将bean转成int类型
	 * @param bean -- 被转换的bean
	 * @param defaultValue -- 默认值,如果bean为null时返回
	 * @return -- bean转成的in值
	 */
	public static int toInt(Integer bean, int defaultValue) {
		return (null != bean) ? bean.intValue() : defaultValue;
	}
	
	/**
	 * 根据value来找map中的key
	 * @param <T> -- key多态类型
	 * @param value -- map中的value
	 * @param map -- 被查找的map
	 * @return -- value在map中的key
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T getKeyByValue(Object value, Map map) {
		Object v = null;
		for (Object key : map.keySet()) {
			v = map.get(key);
			if(value == null){
				if(v == null){
					return (T)key;
				} else {
					continue;
				}
			} else if(value.equals(v)){
				return (T)key;
			}
		}
		return null;
	}
	
	/**
	 * 将Object对象转成String
	 * @param obj -- Object对象 
	 * @return -- String对象
	 */
	public static String toNullableString(Object obj) {
		return (null != obj) ? obj.toString() : null;
	}
 
	/**
	 * 将Object对象转成String, 如果obj为null, 返回""字符串
	 * @param obj -- Object对象 
	 * @return -- String对象
	 */
	public static String toNotNullString(Object obj) {
		return (null != obj) ? obj.toString() : "";
	}
	
	/**
	 * 判断str是否是null或者是""字符串
	 * @param str -- 字符串
	 * @return -- true, 是null或者是""字符串
	 */
	public static boolean isNullOrEmpty(String str) {
		return null == str || "".equals(str);
	}
	
	/**
	 * 判断str非null和""字符串
	 * @param str -- 字符串
	 * @return -- true, str不为null, 并且不等于(equals)""字符串
	 */
	public static boolean isNotNullAndEmpty(String str) {
		return (null != str && !"".equals(str));
	}
	
	/**
	 * 根据一组key,和一组升降序标记,按顺序对一个map list进行排序
	 * @param list -- map list
	 * @param sortKeys -- key数组
	 * @param ascOrders -- 升降序标记数组
	 * @return -- 排序后的map list
	 */
	public static List<Map<String, Object>> sort(List<Map<String, Object>> list, final String[] sortKeys, final boolean[] ascOrders) {

		Comparator<Map<String, Object>> cmp = new Comparator<Map<String, Object>>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public int compare(Map m1, Map m2) { 
				
				for (int i = 0; i < sortKeys.length; i++) {
					Object o1 = m1.get(sortKeys[i]);
					Object o2 = m2.get(sortKeys[i]);
					if (null == o1 || null == o2) {
						throw new IllegalStateException("被排序的map的key的值不能为空");
					}
					Comparable c = (Comparable) o1;
					int r = c.compareTo(o2);
					if (0 != r) {
						return ascOrders[i] ? r : -r;
					}
				}
				return 0;
			}  
		};
		Collections.sort(list, cmp);
		return list;
	}
	
	/**
	 * 根据map中一个key的值,对一个map list进行排序, 默认顺序:asc
	 * @param list -- map list
	 * @param sortKey -- 用于排序的值对应的key
	 * @return -- 排序后的map list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Map<String, Object>> sort(List<Map<String, Object>> list, final String sortKey) {
		Comparator cmp = new Comparator<Map>() {
			public int compare(Map m1, Map m2) { 
				
				Object o1 = m1.get(sortKey);
				Object o2 = m2.get(sortKey);
				if (null == o1 || null == o2) {
					throw new IllegalStateException("被排序的map的key的值不能为空");
				}
				Comparable c = (Comparable) o1;
				return c.compareTo(o2);
			}  
		};
		Collections.sort(list, cmp);
		return list;
	} 
	
	/**
	 * 防止非法实例化
	 */
	private BeanUtil() {}
	
	/**
	 * 
	 * 根据配置文件配置的类，动态创建对象
	 * 
	 * @param className -- 类名
	 * @return 返回一个类名所代表的一个对象
	 * @exception 参数为空或者没有找到相应类的时候抛出Exception
	 */
    public static Object createBean(String className) throws Exception {
        if (StringUtil.isNullOrBlank(className)) {
            String msg = "Cannot find the class " + ":" + className;
            throw new Exception(msg);
        } else {
            try {
                return Class.forName(className).newInstance();
            } catch (Throwable ex) {
                String msg ="Cannot create this class " + ":"  + className;
                throw new Exception(msg, ex);
            }
        }
    }

}
