package cn.aurochs.www.config;

import java.util.Map;

/**
 * xml config实现
 */
public class XmlConfig implements Config {

	private Map<String, Object> data;

	public XmlConfig(Map<String, Object> data) {
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getItem(String name) {
		if (data == null) {
			return null;
		}
		return (T) data.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getItem(String name, T defaultValue) {
		T v = (T) getItem(name);
		if (v == null) {
			v = defaultValue;
		}
		return v;
	}

	@Override
	public String toString() {
		return "XmlConfig [data=" + data + "]";
	}
}
