package per.itachi.test.server.v2.data;

import java.util.Arrays;

import per.itachi.test.server.v2.constant.TransactionDataConst;
import per.itachi.test.server.v2.util.CommonUtility;

public class TransactionDataOut {
	
//	private static final Log log = LogFactory.getLog(TransactionDataOut.class);
	
	private TransactionDataIn dataIn;
	private byte[] dataOut;
	private byte[][] sendData;
	private int num;
	
	public TransactionDataOut(TransactionDataIn dataIn, byte[] dataOut) {
		this.dataIn = dataIn;
		this.dataOut = dataOut;
		init();
	}
	
	private void init() {
		int iNum = 0;
		if (dataOut.length <= TransactionDataConst.LENGTH_BYTES_DATA) {
			iNum = 1;
		}
		else {
			iNum = dataOut.length / TransactionDataConst.LENGTH_BYTES_DATA;
			if (dataOut.length % TransactionDataConst.LENGTH_BYTES_DATA > 0) {
				++iNum;
			}
		}
		this.num = iNum;
		sendData = new byte[iNum][TransactionDataConst.LENGTH_SEGMENT];
		byte[] bytesHead = new byte[TransactionDataConst.LENGTH_BYTES_TRX_ID 
									+ TransactionDataConst.LENGTH_BYTES_LENGTH 
									+ TransactionDataConst.LENGTH_BYTES_SEQUENCE 
									+ TransactionDataConst.LENGTH_BYTES_BUSINESS_ID];
		//maybe, the field sequence should be after the field business id.
		System.arraycopy(CommonUtility.convertIntToBytes(dataIn.getTrxID()), 0, 
				bytesHead, TransactionDataConst.OFFSET_BYTES_TRX_ID, TransactionDataConst.LENGTH_BYTES_TRX_ID);
		System.arraycopy(CommonUtility.convertIntToBytes(dataIn.getLength()), 0, 
				bytesHead, TransactionDataConst.OFFSET_BYTES_LENGTH, TransactionDataConst.LENGTH_BYTES_LENGTH);
		System.arraycopy(CommonUtility.convertIntToBytes(dataIn.getBusinessID()), 0, 
				bytesHead, TransactionDataConst.OFFSET_BYTES_BUSINESS_ID, TransactionDataConst.LENGTH_BYTES_BUSINESS_ID);
		for (int i = 0; i < iNum - 1; i++) {
			Arrays.fill(sendData[i], (byte)0);
			System.arraycopy(CommonUtility.convertIntToBytes(i + 1), 0, 
					bytesHead, TransactionDataConst.OFFSET_BYTES_SEQUENCE, TransactionDataConst.LENGTH_BYTES_SEQUENCE);
			System.arraycopy(bytesHead, 0, sendData[i], 0, 16);//copy data head.
			System.arraycopy(this.dataOut, TransactionDataConst.LENGTH_BYTES_DATA * i, 
					sendData[i], TransactionDataConst.OFFSET_BYTES_DATA, TransactionDataConst.LENGTH_BYTES_DATA);
		}
		Arrays.fill(sendData[iNum - 1], (byte)0);
		System.arraycopy(CommonUtility.convertIntToBytes(iNum), 0, 
				bytesHead, TransactionDataConst.OFFSET_BYTES_SEQUENCE, TransactionDataConst.LENGTH_BYTES_SEQUENCE);
		System.arraycopy(bytesHead, 0, sendData[iNum - 1], 0, 16);//copy data head.
		System.arraycopy(this.dataOut, TransactionDataConst.LENGTH_BYTES_DATA * (iNum - 1), 
				sendData[iNum - 1], TransactionDataConst.OFFSET_BYTES_DATA, 
				dataOut.length - TransactionDataConst.LENGTH_BYTES_DATA * (iNum - 1));
	}

	public void loadDataSegment(byte[] segment, int index) {
		System.arraycopy(sendData[index], 0, segment, 0, TransactionDataConst.LENGTH_SEGMENT);
	}
	
	public int getNum() {
		return num;
	}
}
