package cn.aurochs.www.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.aurochs.www.config.Config;
import cn.aurochs.www.config.FwConfigService;
import cn.aurochs.www.server.ServiceProvider;


/**
 * ServiceProvider spring实现
 */
public class SpringServiceProvider implements ServiceProvider {
	
	private static Logger logger = LoggerFactory.getLogger(SpringServiceProvider.class);
	 
	private ApplicationContext appContext;
	 
	public SpringServiceProvider(){}
	 
	public SpringServiceProvider(ApplicationContext appContext){
		this.appContext=appContext;
	}
	 
	@Override
	public void init() {
		if(appContext==null){
			logger.info("开始构建spring上下文");
			Config sysConfig=FwConfigService.getSystemConfig();
			String serviceConfig=sysConfig.getItem("service.config", "fw_context.xml");
			String[] configs=serviceConfig.split(",");
			appContext=new ClassPathXmlApplicationContext(configs);
			logger.info("成功构建spring上下文,配置文件->"+serviceConfig);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(String serviceID) {
		return (T)appContext.getBean(serviceID);
	}
 
 

}
