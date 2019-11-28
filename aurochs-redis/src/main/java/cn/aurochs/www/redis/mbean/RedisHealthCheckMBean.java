package cn.aurochs.www.redis.mbean;
/**
 * Redis可用性检测的MBean接口
 * @author luyz
 *
 */
public interface RedisHealthCheckMBean extends UsabilityMBean{
    
    /**
     * 
     * 执行Redis可用性检测,返回整形表示的可用性状态
     * 
     * @return 返回字符串表示的可用性状态。1表示可用，其他表示不可用的信息
     */
    public String getState();

}
