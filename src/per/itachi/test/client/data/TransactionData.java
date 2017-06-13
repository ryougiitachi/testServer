package per.itachi.test.client.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import per.itachi.test.client.config.TotalConfig;
import per.itachi.test.client.util.IdentifierGenerator;
import per.itachi.test.server.v2.constant.TransactionDataConst;
import per.itachi.test.server.v2.util.CommonUtility;

public class TransactionData {
	
	private TotalConfig config;
	private int businessID;
	
	private byte[][] rawdata;
	private boolean[] filled;
	
	public TransactionData(TotalConfig config, int businessID) {
		this.config = config;
		this.businessID = businessID;
	}
	
	public void parseParamsToBytes(Map<String, String> params) {
		StringBuilder builder = new StringBuilder(512);
		Set<String> setKeys = params.keySet();
		for (String strKey : setKeys) {
			builder.append(strKey).append("=").append(params.get(strKey)).append("&");
		}
		String strParams = builder.substring(0, builder.length() - 1);
		byte[] bytesParams = strParams.getBytes(config.getBasic().getCharset());
		int num;
		if (bytesParams.length <= TransactionDataConst.LENGTH_BYTES_DATA) {
			num = 1;
		}
		else {
			num = bytesParams.length / TransactionDataConst.LENGTH_BYTES_DATA;
			if (bytesParams.length % TransactionDataConst.LENGTH_BYTES_DATA > 0) {
				++num;
			}
		}
		if (rawdata == null) {
			rawdata = new byte[num][TransactionDataConst.LENGTH_BYTES_DATA];
		}
		if (filled == null) {
			filled = new boolean[num];
		}
		byte[] dataHead = new byte[16];
		System.arraycopy(CommonUtility.convertIntToBytes(IdentifierGenerator.getNextTrxID()), 0, dataHead, TransactionDataConst.OFFSET_BYTES_TRX_ID, TransactionDataConst.LENGTH_BYTES_TRX_ID);
		System.arraycopy(CommonUtility.convertIntToBytes(bytesParams.length), 0, dataHead, TransactionDataConst.OFFSET_BYTES_LENGTH, TransactionDataConst.LENGTH_BYTES_LENGTH);
		System.arraycopy(CommonUtility.convertIntToBytes(businessID), 0, dataHead, TransactionDataConst.OFFSET_BYTES_BUSINESS_ID, TransactionDataConst.LENGTH_BYTES_BUSINESS_ID);
		for (int i = 0; i < num; i++) {
			Arrays.fill(rawdata[i], (byte)0);
			System.arraycopy(CommonUtility.convertIntToBytes(i+1), 0, dataHead, TransactionDataConst.OFFSET_BYTES_SEQUENCE, TransactionDataConst.LENGTH_BYTES_SEQUENCE);
			System.arraycopy(dataHead, 0, rawdata[i], 0, 16);
			System.arraycopy(bytesParams, TransactionDataConst.OFFSET_BYTES_DATA * i, rawdata[i], TransactionDataConst.OFFSET_BYTES_DATA, bytesParams.length - TransactionDataConst.LENGTH_BYTES_DATA * i);
		}
		Arrays.fill(filled, true);
	}
	
	public void read(InputStream is) throws IOException {
		is.read();
	}
	
	public void write(OutputStream os) throws IOException {
		if (rawdata == null) {
			return;
		}
		for (int i = 0; i < rawdata.length; i++) {
			os.write(rawdata[i]);
		}
	}
}
