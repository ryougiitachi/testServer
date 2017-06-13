package per.itachi.test.server.v2.constant;

public interface TransactionDataConst {
	
	public static final int LENGTH_SEGMENT = 1536;
	
	public static final int LENGTH_BYTES_TRX_ID = 4;
	public static final int LENGTH_BYTES_LENGTH = 4;
	public static final int LENGTH_BYTES_SEQUENCE = 4;
	public static final int LENGTH_BYTES_BUSINESS_ID = 4;
	public static final int LENGTH_BYTES_DATA = 1520;
	
	public static final int OFFSET_BYTES_TRX_ID = 0;
	public static final int OFFSET_BYTES_LENGTH = 4;
	public static final int OFFSET_BYTES_SEQUENCE = 8;
	public static final int OFFSET_BYTES_BUSINESS_ID = 12;
	public static final int OFFSET_BYTES_DATA = 16;

}
