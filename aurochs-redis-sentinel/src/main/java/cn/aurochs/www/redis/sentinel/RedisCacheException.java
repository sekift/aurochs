package cn.aurochs.www.redis.sentinel;

import cn.aurochs.www.exception.RuntimeAppException;

/**
 * Redis异常
 * 
 * @author  sekift
 * @date 2019-10-16
 */
public class RedisCacheException extends RuntimeAppException {
	private static final long serialVersionUID = 85200268472976742L;

	public RedisCacheException() {
		super();
	}

	public RedisCacheException(String message) {
		super(message);
	}

	public RedisCacheException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedisCacheException(Throwable cause) {
		super(cause);
	}
	
	public static void throwError(String message,Object... args) throws RuntimeException {
		String errorMessage = null;
		if(message!=null && args!=null && args.length>0){
			errorMessage = String.format(message, args);
		}else{
			errorMessage = message;
		}
		throw new RedisCacheException(errorMessage);
	}

	public static void throwError(Throwable cause,String message,Object... args)
			throws RuntimeException {
		String errorMessage = null;
		if(message!=null && args!=null && args.length>0){
			errorMessage = String.format(message, args);
		}else{
			errorMessage = message;
		}
		throw new RedisCacheException(errorMessage, cause);
	}
}
