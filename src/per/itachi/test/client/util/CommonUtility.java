package per.itachi.test.client.util;

public class CommonUtility {
	
	public static int convertBytesToInt(byte[] data) {
		return convertBytesToInt(data, 0, data.length);
	}
	
	public static int convertBytesToInt(byte[] data, int offset, int length) {
		if (data == null || data.length == 0
				|| offset <= 0 || length <= 0) {
			return 0;
		}
		if (data.length >= 5) {
			return Integer.MAX_VALUE;
		}
		int value = 0;
		int i = 0;
		for(i=0; i < data.length; ++i) {
			value |= data[i] & 0xff;//what about no & 0xff?
			value <<= 8;
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


}
