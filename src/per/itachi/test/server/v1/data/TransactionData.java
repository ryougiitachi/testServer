package per.itachi.test.server.v1.data;

public interface TransactionData {
	
	void setExecCode(int code);
	
	int getExecCode();
	
	void setTotalLength(int length);
	
	int getTotalLength();
	
	void setTransactionData(byte[] data);
	
	byte[] getTransactionData();
	/**
	 * Just test
	 * */
	byte[] convertToBytes();
}
