package cn.aurochs.www.server;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.aurochs.www.config.Config;
import cn.aurochs.www.config.FwConfigService;


/**
 * 服务实例工厂类
 */
public class ServiceFactory {
	
	private static Lock INIT_LOCK=new ReentrantLock();
	
	private static  ServiceProvider serviceProvider;
	
	public static void setServiceProvider(ServiceProvider provider){
		serviceProvider=provider;
	}
	
	
	/**
	 * 初始化
	 */
	public static void init(){
		//doubleCheck+lock保证线程安全和性能 
		if(serviceProvider==null){
			try{
				INIT_LOCK.lock();
				if(serviceProvider==null){
					Config sysConfig=FwConfigService.getSystemConfig();
					String sp=sysConfig.getItem("service.provider", "spring");
					serviceProvider=ServiceProviderFactory.getServiceProvider(sp);
				} 
			}finally{
				INIT_LOCK.unlock();
			}
		}
	}
	
	/**
	 * 获取服务
	 * @param serviceId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(String serviceID){
		init();
		return (T)serviceProvider.getService(serviceID);
	}
	
}
