package cn.aurochs.www.server;

import cn.aurochs.www.extension.Extensions;

public class ServiceProviderFactory {
	
	/**
	 * 通过名称获取serviceProvider实例
	 * @param providerName
	 * @return
	 */
	public static ServiceProvider getServiceProvider(String providerName){
		ServiceProvider serviceProvider = Extensions.loadExtension(ServiceProvider.class);
		serviceProvider.init();
		return serviceProvider;
	}

}
