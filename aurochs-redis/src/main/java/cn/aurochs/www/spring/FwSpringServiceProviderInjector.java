package cn.aurochs.www.spring;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import cn.aurochs.www.server.ServiceFactory;
import cn.aurochs.www.server.ServiceProvider;


/**
 * FWSpring上下文注入
 */
public class FwSpringServiceProviderInjector implements ApplicationContextAware {

	private static Logger logger = LoggerFactory.getLogger(FwSpringServiceProviderInjector.class);
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		ServiceProvider serviceProvider=new SpringServiceProvider(applicationContext);
		ServiceFactory.setServiceProvider(serviceProvider);
		logger.info("成功通过FwSpringServiceProviderInjector初始化ServiceFactory.");
		
	}

}
