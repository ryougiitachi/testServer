package per.itachi.test.server.v2.constant;

public interface ErrorCode {
	
	public static final int ERROR_CODE_SUCCESS 			= 0000;
	public static final int ERROR_CODE_RECEIVED 		= 0001;
	
	public static final int ERROR_CODE_FULL_CONN 		= -9001;
	public static final int ERROR_CODE_NOT_FOUND_SN 	= -9002;
	public static final int ERROR_CODE_NO_MATCHED_SN 	= -9003;
	public static final int ERROR_CODE_NO_SUCH_BUZI 	= -9004;
	public static final int ERROR_CODE_INCORRECT_TRX 	= -9005;
	
}
