package per.itachi.test.client.util;

public class IdentifierGenerator {
	
	private static final Object lockTrxID = new Object();
	private static int trxID = 0;
	
	/**
	 * Perhaps, it is better to store this id into file.
	 * */
	public static int getNextTrxID() {
		synchronized (lockTrxID) {
			++trxID;
			if (trxID < 0) {
				trxID = 1;
			}
			return trxID;
		}
	}
}
