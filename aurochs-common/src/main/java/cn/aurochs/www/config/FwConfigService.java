package cn.aurochs.www.config;

import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aurochs.www.util.CloseUtil;
import cn.aurochs.www.util.XmlUtil;

/**
 * 配置服务类
 */
public final class FwConfigService  {
	
	/**
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(FwConfigService.class);
	
	
	private static volatile Map<String,Object> CONFIG_CACHE;
	
	private final static String CONFIG_FILE_PATH="/fw_config.xml";
	
	/**
	 * 加载配置
	 */
	@SuppressWarnings("unchecked")
	private static void loadConfig(){
		if(CONFIG_CACHE!=null){
			return;
		}
		
		InputStream ins=null;
		try{
			ins=FwConfigService.class.getResourceAsStream(CONFIG_FILE_PATH);
			if(ins==null){
				logger.warn(CONFIG_FILE_PATH+"文件不存在");
				return;
			}
			
			Map<String,Object> xmlMap=XmlUtil.toMap(ins);
			CONFIG_CACHE=xmlMap;
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			CloseUtil.closeSilently(ins);
		}  
	}
	
	
	/**
	 * 获取配置
	 * @param configName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getConfig(String configName){
		loadConfig();
		if(CONFIG_CACHE==null){
			return null;
		} 
		Map<String,Object> config=(Map<String,Object>)CONFIG_CACHE.get(configName);
		return config;
	}
	
	
	/**
	 * 获取系统配置项
	 * @return
	 */
	public static Config getSystemConfig(){
		Map<String,Object> dataMap=getConfig("system_properties");
		Config config=new XmlConfig(dataMap);
		return config; 
	}
	
	/**
	 * 获取应用配置项
	 * @return
	 */
	public static Config getAppConfig(){
		Map<String,Object> dataMap=getConfig("app_properties");
		Config config=new XmlConfig(dataMap);
		return config;
	} 
}
