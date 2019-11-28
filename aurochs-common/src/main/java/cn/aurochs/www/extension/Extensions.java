package cn.aurochs.www.extension;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Extensions {
	
	private static Logger logger = LoggerFactory.getLogger(Extensions.class);
	
	private static ConcurrentHashMap<String, Object> EXT_INSTANCE_CACHE=new  ConcurrentHashMap<String,Object>();
	 
	/**
	 * 加载实现类对象
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T loadExtension(Class<T> clazz){
		String clazzName=clazz.getName();
		Object inst=EXT_INSTANCE_CACHE.get(clazzName);
		if(inst==null){
			ServiceLoader<T>  serviceLoader=ServiceLoader.load(clazz);
			Iterator<T> serviceIterator=serviceLoader.iterator();
			//找到一个实现即返回
			if(serviceIterator.hasNext()){
				inst=serviceIterator.next();
				EXT_INSTANCE_CACHE.putIfAbsent(clazzName, inst);
				logger.info("获得"+clazz.getName()+"的实现类"+inst.getClass().getName()+".");
			}else{
				logger.error("未找到"+clazz.getName()+"合适的实现");
			} 
		}
		return (T)inst;
	}
	
}
