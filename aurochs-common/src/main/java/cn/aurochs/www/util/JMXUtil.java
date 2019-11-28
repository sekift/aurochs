package cn.aurochs.www.util;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.rmi.RMIConnector;
import javax.management.remote.rmi.RMIServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMX工具类
 * 
 */
public class JMXUtil {

	private static Logger logger = LoggerFactory.getLogger(JMXUtil.class);

	/**
	 * 得到JMX的连接
	 * 
	 * @param ip
	 *            网址
	 * @param port
	 *            端口
	 * @param environment
	 *            环境
	 * @return JMX连接
	 */
	public static JMXConnector getConnect(String ip, String port, Map<String, ?> environment) {
		if (StringUtil.isNullOrBlank(ip) || StringUtil.isNullOrBlank(port)) {
			logger.error("ip和端口不能为空.");
			return null;
		}
		try {
			Registry registry = LocateRegistry.getRegistry(ip, Integer.parseInt(port));
			RMIServer stub = (RMIServer) registry.lookup("jmxrmi");
			JMXConnector jmxc = new RMIConnector(stub, environment);
			jmxc.connect();
			return jmxc;
		} catch (Exception e) {
			logger.error("连接JMX服务器失败.");
		}
		return null;
	}

	/**
	 * 得到JMX的连接
	 * 
	 * @param ip
	 *            网址
	 * @param port
	 *            端口
	 * @return JMX连接
	 */
	public static JMXConnector getConnect(String ip, String port) {
		return getConnect(ip, port, null);
	}

	/**
	 * 注册MBean
	 * 
	 * @param mbeanObject
	 * @param objectName
	 */
	public static boolean registerMBean(Object mbeanObject, String objectName) {
		ObjectName oname = null;
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			oname = new ObjectName(objectName);
			if (mbs.isRegistered(oname)) {
				logger.info("已有注册MBean[{}]", objectName);
				return true;
			} else {
				mbs.registerMBean(mbeanObject, oname);
				logger.info("成功注册MBean[{}]", objectName);
				return true;
			}
		} catch (Exception e) {
			logger.error("注册MBean[" + objectName + "]失败", e);
		}
		return false;
	}

	/**
	 * 查询远程MBean
	 * 
	 * @param mbsConnection
	 * @param mbeanName
	 *            object name pattern, eg.
	 *            "cn.abc.www.mbean:*,func=AliveCheck"
	 * @return
	 */
	public static Set<ObjectName> queryMBeanNames(MBeanServerConnection msc, String mbeanName) {
		try {
			ObjectName objectName = new ObjectName(mbeanName);
			return msc.queryNames(objectName, null);
		} catch (Exception e) {
			logger.error("查询MBean Name列表[" + mbeanName + "]失败", e);
		}
		return null;
	}

	/**
	 * 
	 * 创建对象名
	 * 
	 * @param onameString
	 *            -- 对象名字符串
	 * @return 返回ObjectName
	 * @exception NullPointerException
	 *                -- 如果 onameString 参数为 null
	 * @exception MalformedObjectNameException
	 *                -- 如果作为参数传递的字符串格式不正确
	 **/
	public static ObjectName createObjectName(String oname) throws MalformedObjectNameException, NullPointerException {
		if (StringUtil.isNullOrBlank(oname)) {
			throw new NullPointerException("onameString can't be null");
		}
		ObjectName objectName = null;
		objectName = new ObjectName(oname);
		return objectName;
	}

	/**
	 * 
	 * 创建对象名字符串.
	 * 
	 * @param domain
	 *            -- ObjectName的域名
	 * @param type
	 *            -- ObjectName中的type,例如type=ICE
	 * @param serviceId
	 *            -- ObjectName中的服务ID(serviceId),例如serviceId=ala_demo
	 * @param name
	 *            -- ObjectName中的name,MBean的名称
	 * @return 返回对象名字符串
	 * @exception 异常信息的描述
	 */
	public static String createObjectNameString(String domain, String type, String serviceId, String name) {
		StringBuilder sb = new StringBuilder(domain);
		sb.append(":type=");
		sb.append(type);

		if (!StringUtil.isNullOrBlank(serviceId)) {
			sb.append(",serviceId=");
			sb.append(serviceId);
		}
		if (!StringUtil.isNullOrBlank(name)) {
			sb.append(",name=");
			sb.append(name);
		}
		return sb.toString();
	}

}
