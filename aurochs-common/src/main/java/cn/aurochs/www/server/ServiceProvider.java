package cn.aurochs.www.server;

public interface ServiceProvider {
  
	
	/**
	 * 初始化
	 */
	public void init();
	
	/**
	 * 根据ID获取服务
	 * @param serviceID
	 * @return
	 */
	public <T> T getService(String serviceID); 
	
}
