package per.itachi.test.server.v1.util;

public class CommonUtility {
	
	public static int convertBytesToInt(byte[] data) {
		if (data == null || data.length == 0) {
			return 0;
		}
		if (data.length >= 5) {
			return Integer.MAX_VALUE;
		}
		int value = 0;
		int i = 0;
		for(i=0; i < data.length; ++i) {
			value |= data[i];
			value <<= 8 << i;
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
