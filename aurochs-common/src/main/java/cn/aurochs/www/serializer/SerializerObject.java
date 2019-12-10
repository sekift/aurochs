package cn.aurochs.www.serializer;

import java.nio.ByteBuffer;

/**
 * 序列化成字符
 * 
 * @author sekift
 * @date 2019-10-14
 */
public interface SerializerObject {
	
	/**
	 * 反序列化
	 * @param buffer 对象序列后的Byte
	 * @param t 类
	 * @return 对象
	 */
	public <T> T deserialize(ByteBuffer buffer, Class<T> t);

	/**
	 * 序列化
	 * @param obj 序列化对象
	 * @return 序列化后的Byte
	 */
	public ByteBuffer serialize(Object obj);

}
