package per.itachi.test.server.v2.data;

import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import per.itachi.test.server.v2.constant.TransactionDataConst;
import per.itachi.test.server.v2.util.CommonUtility;

/**
 * |1536bytes|<br/>
 * |head|data|<br/>
 * |TRX_ID|Length(excluding head)|sequence|business_id|data|<br/>
 * |4bytes|4bytes|4bytes|4bytes|1520bytes|<br/>
 * <br/>
 * Convenient for arranging transaction data. 
 * */
public class TransactionDataIn {
	
	private static final Log log = LogFactory.getLog(TransactionDataIn.class);
	
	private int trxID;
	private int length;		//length of data excluding head, may be 0?
	private int businessID;
	private int num;		//the number of segments which should be 
	private int count;		//count the current number of data segments
	private byte[][] rawData;
	private boolean[] filled;//validate whether the current data segment has been filled.

	public TransactionDataIn(byte[] segment) {
		trxID = -1;
		length = -1;
		businessID = -1;
		num = -1;
		count = 0;
		addDataSegment(segment);
	}
	
	/**
	 * Add data segment to transaction data-in object.
	 * */
	public boolean addDataSegment(byte[] segment) {
		int iTrxID = CommonUtility.convertBytesToInt(segment, TransactionDataConst.OFFSET_BYTES_TRX_ID, TransactionDataConst.LENGTH_BYTES_TRX_ID);
		int iLength = CommonUtility.convertBytesToInt(segment, TransactionDataConst.OFFSET_BYTES_LENGTH, TransactionDataConst.LENGTH_BYTES_LENGTH);
		int iSeq = CommonUtility.convertBytesToInt(segment, TransactionDataConst.OFFSET_BYTES_SEQUENCE, TransactionDataConst.LENGTH_BYTES_SEQUENCE);
		int iBusinessID = CommonUtility.convertBytesToInt(segment, TransactionDataConst.OFFSET_BYTES_BUSINESS_ID, TransactionDataConst.LENGTH_BYTES_BUSINESS_ID);
		int iNum;
		//validate head
		if (iLength <= TransactionDataConst.LENGTH_BYTES_DATA) {
			iNum = 1;
		}
		else {
			iNum = iLength / TransactionDataConst.LENGTH_BYTES_DATA;
			if (iLength % TransactionDataConst.LENGTH_BYTES_DATA > 0) {
				++iNum;
			}
		}
		if (trxID <= 0) {
			trxID = iTrxID;
		}
		if (length <= 0) {
			length = iLength;
		}
		if (businessID <= 0) {
			businessID = iBusinessID;
		}
		if (num <= 0) {
			num = iNum;
		}
		if (trxID != iTrxID || length != iLength || businessID != iBusinessID || num != iNum) {
			log.warn(MessageFormat.format("Transaction {0} is illegal about length/businessID/num.", trxID));
			return false;
		}
		if (iSeq < 0 || iSeq > iNum) {
			log.warn(MessageFormat.format("Transaction {0} has illegal sequence.", trxID));
			return false;
		}
		if (filled == null) {
			filled = new boolean[iNum];
			Arrays.fill(filled, false);
		}
		if (rawData == null) {
			rawData = new byte[iNum][TransactionDataConst.LENGTH_BYTES_DATA];
		}
		if (filled[iSeq-1]) {
			log.warn(MessageFormat.format("Transaction {0} sequence {1} has been filled.", trxID, iSeq));
			return false;
		}
		System.arraycopy(segment, TransactionDataConst.OFFSET_BYTES_DATA, rawData[iSeq-1], 0, TransactionDataConst.LENGTH_BYTES_DATA);
		filled[iSeq-1] = true;
		++count;
		return true;
	}
	
	/**
	 * return whether to complete assembling data.
	 * */
	public boolean isComplete() {
		return num == count;
	}
	
	/**
	 * get real data
	 * */
	public boolean loadData(byte[] dataIn) {
		if (!isComplete()) {
			return false;
		}
		if (dataIn == null || dataIn.length < length) {
			return false;
		}
		for (int i = 0; i < num; i++) {
			System.arraycopy(rawData[i], 0, dataIn, TransactionDataConst.LENGTH_BYTES_DATA * i, TransactionDataConst.LENGTH_BYTES_DATA);
		}
		return true;
	}

	public int getTrxID() {
		return trxID;
	}

	public int getLength() {
		return length;
	}

	public int getBusinessID() {
		return businessID;
	}

	public int getNum() {
		return num;
	}
}
