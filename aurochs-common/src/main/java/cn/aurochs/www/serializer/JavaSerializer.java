package cn.aurochs.www.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * Java序列化
 * 
 * @author sekift
 * @date 2019-10-14
 */
public class JavaSerializer implements SerializerObject {

	private JavaSerializer() {

	}

	public static JavaSerializer getInstance() {
		return JavaSerializerHolder._instance;
	}

	private static class JavaSerializerHolder {
		static JavaSerializer _instance = new JavaSerializer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(ByteBuffer buffer, Class<T> t) {
		try {
			if (buffer == null || buffer.limit() == 0) {
				return null;
			}
			ByteArrayInputStream bin = new ByteArrayInputStream(buffer.array());
			ObjectInputStream inputStream = new ObjectInputStream(bin);
			Object object = inputStream.readObject();
			bin.close();
			return (T) object;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public ByteBuffer serialize(Object obj) {
		try {
			ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
			stream.writeObject(obj);
			stream.close();
			return ByteBuffer.wrap(byteArrayOS.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}
}
