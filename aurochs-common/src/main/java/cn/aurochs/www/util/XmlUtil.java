package cn.aurochs.www.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
 
/**
 * xml 帮助类
 * @author: sekift
 * @Create On: 2015-11-3 下午05:19:44
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class XmlUtil {
	 
	/**
	 * 将xml字符串转成map对象
	 * @param xml -- xml字符串
	 * @return -- map对象
	 */
	public static Map toMap(String xml) {
		
		ByteArrayInputStream sin = new ByteArrayInputStream(xml.getBytes());
		return toMap(sin);
	}
	
	/**
	 * 将xml输入流转成map对象
	 * @param in -- xml输入流
	 * @return -- map对象
	 */
	public static Map toMap(InputStream in) {

		try {			
			SAXReader reader = new SAXReader();
			Document doc = reader.read(in);
			return toMap(doc.getRootElement());
		} catch (Exception ex) {
			throw new RuntimeException("解析xml成map对象异常", ex);
		} 
	}
	
	/**
	 * 将dom4j的Element对象转成map对象
	 * @param elem -- dom4j Element对象
	 * @return -- map对象
	 */
	public static Map toMap(Element elem) {
		 
		if (null == elem) {
			throw new IllegalArgumentException("无效XML Element 对象, 不能为null");
		}
		List<Element> elems = elem.elements();
		Map tar = new LinkedHashMap();
		for (Element item : elems) {
			generateMap(tar, item);
		}
		return tar;
	}
	 
	/**
	 * 嵌套调用, 将一个dom4j Element对象本身及其子节点转成map对象
	 * @param container -- 当前dom4j Element对应map对象容器
	 * @param elem -- dom4j Element对象
	 * @return -- map对象
	 */
	private static Map generateMap(Map container, Element elem) {
		
		String name = elem.getName();
		Object obj = container.get(name);
		// 处理存在多个相同名称元素的情况
		if (null != obj) {
			if (!List.class.isInstance(obj)) { // 已经存在相同节点,但是非List对象(此对象是List的第一个元素)
				List<Object> newBean = new LinkedList<Object>();
				newBean.add(obj);
				container.put(name, newBean);
				generateMap(container, elem);
			} else { // 已经存在相同节点,并且是List对象(说明当前elem非List的第一个元素)
				List<Object> bean = (List<Object>)obj;
				if (elem.isTextOnly()) {
					bean.add(elem.getStringValue());
				} else {
					List<Element> subs = elem.elements();
					Map nodes = new LinkedHashMap();
					bean.add(nodes);
					for (Element item : subs) {
						generateMap(nodes, item);
					}
				}
			}
			return container;
		}
		// 处理非多个相同名称元素的情况
		if (elem.isTextOnly()) { // 只是文本xml节点,没有子节点
			container.put(name, elem.getStringValue());
		} else { 
			List<Element> subs = elem.elements();
			Map nodes = new LinkedHashMap();
			container.put(name, nodes);
			for (Element item : subs) {
				generateMap(nodes, item);
			} 
		} 
		return container;
	}
	

	
	/**
	 * 获取结果记录的xml封装
	 * @param rowList 结果记录列表：列表元素需为Map类型，Map的key值将作为结点名称
	 * @return xml
	 */
	@Deprecated
	public static String getXml(List rowList){
		if(rowList == null || rowList.isEmpty()){
			//结果集为空，返回空xml封装
			return getEmptyXml(); 
		}
		StringBuilder result = new StringBuilder();
		result.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?><dataset>");
		Iterator it = rowList.iterator();
		while(it.hasNext()){
			result.append("<row>");
			Map map = (Map)it.next();
			Set<Map.Entry> entrys = map.entrySet();
			for(Map.Entry entry:entrys){
				result.append("<"+entry.getKey()+"><![CDATA["+entry.getValue()
						+"]]></"+entry.getKey()+">");
			}
			result.append("</row>");
		}
		result.append("</dataset>");
		return result.toString();
	}
	
	/**
	 * 获取结果集为空的xml封装
	 * @return xml
	 */
	@Deprecated
	public static String getEmptyXml(){
		return "<?xml version=\"1.0\" encoding=\"utf-8\" ?><dataset></dataset>";
	}	

	/**
	 * 获取错误信息的xml封装
	 * @param errorMsg 错误信息
	 * @return xml
	 */
	@Deprecated
	public static String getErrorXml(String errorMsg){
		StringBuilder result = new StringBuilder();
		result.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?><error_msg><![CDATA[");
		result.append(errorMsg);
		result.append("]]></error_msg>");
		return result.toString();
	}
	
	/**
	 * 获取错误信息的xml封装
	 * @param errorMsg 错误信息
	 * @param ex Exception
	 * @return xml
	 */
	@Deprecated
	public static String getErrorXml(String errorMsg,Exception ex){
		StringBuilder result = new StringBuilder();
		result.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?><error_msg><![CDATA[");
        try {
            StringWriter sw = new StringWriter();  
            ex.printStackTrace(new PrintWriter(sw));  
            result.append(errorMsg);
            result.append(sw.toString());
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		result.append("]]></error_msg>");
		return result.toString();
	}	
	
    /**
     * 按指定的字段和字段类型重排结果集，并重置索引值
     * @param rowList 结果记录列表：列表元素需为Map类型
     * @param fieldSortBy 用于排序的字段名
     * @param fieldClassType	被排字段的数据类型，目前被支持的类型有：
     * Integer.class/int.class, 其他类型都会被当成String类型来处理 
     * @param inc	是否按升序
     */
	@Deprecated
    public static void orderBy(List rowList, String fieldSortBy, Class fieldClassType, boolean inc){
    	if(rowList == null || rowList.isEmpty()){
    		return;
    	}
		Comparator comp;
		if(fieldClassType == int.class || fieldClassType == Integer.class){
			//按整型排序
			comp = new ElementIntComparator(fieldSortBy, inc);
		} else {
			//按字符串型排序
			comp = new ElementStringComparator(fieldSortBy, inc);
		}
		Collections.sort(rowList, comp);
    }	
	
	/**
     * Int类型的ElementComparator
     */
	@Deprecated
    private static class ElementIntComparator implements Comparator {
    	private String fieldName;
    	private boolean inc;
    	
    	/**
    	 * 
    	 * @param fieldName 要进行排序的字段
    	 * @param inc 是否按升序，true为升序
    	 */
    	public ElementIntComparator(String fieldName, boolean inc){
    		this.fieldName = fieldName;
    		this.inc = inc;
    	}

    	public int compare(Object o1, Object o2) {
    		String f1 = (String)((Map)o1).get(fieldName);
    		String f2 = (String)((Map)o2).get(fieldName);
    		int if1 = 0;
    		int if2 = 0;
    		try{
    			if1 = Integer.parseInt(f1);
    		}catch(NumberFormatException e){
    			return -1;
    		}
    		try{
    			if2 = Integer.parseInt(f2);
    		}catch(NumberFormatException e){	
    			return 1;
    		}
    		int result = if1 - if2;
    		if(!inc){
    			result = -result;
    		}
    		return result;
    	}
    	
    }
    
	/**
     * String类型的ElementComparator
     */
	@Deprecated
    private static class ElementStringComparator implements Comparator {
    	private String fieldName;
    	private boolean inc;
    	
    	/**
    	 * 
    	 * @param fieldName 要进行排序的字段
    	 * @param inc 是否按升序，true为升序
    	 */
    	public ElementStringComparator(String fieldName, boolean inc){
    		this.fieldName = fieldName;
    		this.inc = inc;
    	}

    	public int compare(Object o1, Object o2) {
    		String f1 = (String)((Map)o1).get(fieldName);
    		String f2 = (String)((Map)o2).get(fieldName);
    		int result = f1.compareToIgnoreCase(f2);
    		if(!inc){
    			result = -result;
    		}
    		return result;
    	}
    	
    }	
}
