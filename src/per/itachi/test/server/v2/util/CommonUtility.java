package per.itachi.test.server.v2.util;

public class CommonUtility {
	
	public static int convertBytesToInt(byte[] data) {
		return convertBytesToInt(data, 0, data.length);
	}
	
	public static int convertBytesToInt(byte[] data, int offset, int length) {
		if (data == null || data.length == 0
				|| offset < 0 || length <= 0) {
			return 0;
		}
		if (length >= 5) {
			return Integer.MAX_VALUE;
		}
		int value = 0;
		int i = 0;
		for(i=offset; i < offset + length; ++i) {
			value <<= 8;
			value |= data[i] & 0xff;//what about no & 0xff?
		}
		return value;
	}
	
	public static byte[] convertIntToBytes(int value) {
		byte[] data = null;
		int i = 0;
		data = new byte[4];
		for(i=0; i < 4; ++i) {
			data[4- 1 - i] = (byte) ((value >>> (i << 3)) & 0xff);
		}
		return data;
	}

	public static String convertBytesToHexStr(byte[] data) {
		int i, j;
		int colNum = 32;
		int shiftNum = 5;
		int loop = data.length >> shiftNum;// length / 32
//		int reminder = data.length & 31;// length % 32
		StringBuilder builder = new StringBuilder(4800);//4608 + 96 * 2
		for (i = 0; i < loop; ++i) {
			for(j = 0; j < colNum; ++j) {
				builder.append(String.format("%02X ", data[i * colNum + j]));
			}
			builder.append(System.lineSeparator());
		}
		for (i = loop << shiftNum; i < data.length; ++i) {
			builder.append(String.format("%02X ", data[i]));
		}
		return builder.toString();
	}
}
