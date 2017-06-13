package per.itachi.test.server.v1.constant;

public interface BusinessConstant {
	
	public static final int TEST_MAX_CLIENTS = 8;
	
	public static final int RETURN_CODE_SUCCESS = 0;
	public static final int RETURN_CODE_FULL_CONNECTION = -1;
	public static final int RETURN_CODE_INVALID_USERNAME = -2;
	public static final int RETURN_CODE_INVALID_PASSWORD = -3;
	public static final int RETURN_CODE_EXIST_NUMBER = -4;
	public static final int RETURN_CODE_NONEXIST_NUMBER = -5;
	
	public static final int EXEC_CODE_LOGIN = 1;
	public static final int EXEC_CODE_ADD_RUBBISH_NUMBER = 1001;
	public static final int EXEC_CODE_REMOVE_RUBBISH_NUMBER = 1002;

}
