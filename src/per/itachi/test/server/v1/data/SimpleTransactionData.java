package per.itachi.test.server.v1.data;

public class SimpleTransactionData implements TransactionData {
	
	private int execCode;
	private int totalLength;
	private byte[] transactionData;

	@Override
	public void setExecCode(int code) {
		execCode = code;
	}

	@Override
	public int getExecCode() {
		return execCode;
	}

	@Override
	public void setTotalLength(int length) {
		totalLength = length;
	}

	@Override
	public int getTotalLength() {
		return totalLength;
	}

	@Override
	public void setTransactionData(byte[] data) {
		if (transactionData == null || transactionData.length < data.length) {
			transactionData = new byte[data.length];
		}
		System.arraycopy(data, 0, transactionData, 0, data.length);
	}

	@Override
	public byte[] getTransactionData() {
		return transactionData;
	}

	@Override
	public byte[] convertToBytes() {
		return null;
	}

}
