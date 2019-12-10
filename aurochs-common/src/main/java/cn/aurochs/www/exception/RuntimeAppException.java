package cn.aurochs.www.exception;

/**
 * 运行时应用异常
 * 
 * @author sekift
 * @date 2019-10-18
 */
public class RuntimeAppException extends RuntimeException {
	private static final long serialVersionUID = -5177859780228318340L;

	public RuntimeAppException() {
		super();
	}

	public RuntimeAppException(String message) {
		super(message);
	}

	public RuntimeAppException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuntimeAppException(Throwable cause) {
		super(cause);
	}

	public static void throwError(String message,Object... args) throws RuntimeException {
		String errorMessage = null;
		if(message!=null && args!=null && args.length>0){
			errorMessage = String.format(message, args);
		}else{
			errorMessage = message;
		}
		throw new RuntimeAppException(errorMessage);
	}

	public static void throwError(Throwable cause,String message,Object... args)
			throws RuntimeException {
		String errorMessage = null;
		if(message!=null && args!=null && args.length>0){
			errorMessage = String.format(message, args);
		}else{
			errorMessage = message;
		}
		throw new RuntimeAppException(errorMessage, cause);
	}
}
