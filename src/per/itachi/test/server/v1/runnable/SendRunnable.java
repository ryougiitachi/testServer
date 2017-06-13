package per.itachi.test.server.v1.runnable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import per.itachi.test.server.v1.constant.BusinessConstant;
import per.itachi.test.server.v1.data.TransactionData;

public class SendRunnable implements ClosableRunnable {
	
	private Socket socket;
	private OutputStream stream;
	private List<TransactionData> queueSend;
	private boolean running;

	public SendRunnable(List<TransactionData> queue, Socket s) {
		queueSend = queue;
		socket = s;
	}

	public SendRunnable(List<TransactionData> queue, OutputStream sos) {
		queueSend = queue;
		stream = sos;
	}
	
	@Override
	public void run() {
		TransactionData data = null;
		boolean isExit = false;
		
		try {
			stream = socket.getOutputStream();
		} 
		catch (IOException e) {//error, exits thread.
			e.printStackTrace();
			return;
		}
		
		running = true;
		try {
			while (running && !isExit) {
				if (queueSend.size() > 0) {
					data = queueSend.remove(queueSend.size() - 1);
//					stream.write(data.convertToBytes());
					stream.write(convertIntToBytes(data.getExecCode()));
					stream.write(convertIntToBytes(data.getTotalLength()));
					stream.write(data.getTransactionData());
					switch (convertBytesToInt(data.getTransactionData())) {
					case BusinessConstant.RETURN_CODE_FULL_CONNECTION:
					case BusinessConstant.RETURN_CODE_INVALID_USERNAME:
					case BusinessConstant.RETURN_CODE_INVALID_PASSWORD:
						isExit = true;
						break;
					default:
						break;
					}
					data = null;
				} 
				else {
//					Thread.sleep(100l);
					queueSend.wait();
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (stream != null) {
			try {
				stream.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				stream = null;
			}
		}
	}

	@Override
	public void close() {
		this.running = false;
	}
	
	private int convertBytesToInt(byte[] data) {
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
	
	/**
	 * @param value		integer number to be converted
	 * */
	private byte[] convertIntToBytes(int value) {
		byte[] data = null;
		int i = 0;
		data = new byte[4];
		for(i=0; i < 4; ++i) {
			data[4- 1 - i] = (byte) ((value >>> (i << 3)) & 0xff);
		}
		return data;
	}
}
