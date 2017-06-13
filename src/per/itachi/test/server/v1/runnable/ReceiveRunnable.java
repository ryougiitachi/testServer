package per.itachi.test.server.v1.runnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import per.itachi.test.server.v1.data.SimpleTransactionData;
import per.itachi.test.server.v1.data.TransactionData;
import per.itachi.test.server.v1.util.CommonUtility;

public class ReceiveRunnable implements ClosableRunnable {

	private Socket socket;
	private InputStream stream;
	private List<TransactionData> queueReceive;
	private boolean running;
	
	/**
	 * @param queue	receive queue, maybe stack ?
	 * @param s		connecting client socket
	 * */
	public ReceiveRunnable(List<TransactionData> queue, Socket s) {
		queueReceive = queue;
		socket = s;
	}
	
	public ReceiveRunnable(InputStream sis, List<TransactionData> queue) {
		stream = sis;
		queueReceive = queue;
	}

	@Override
	public void run() {
		byte[] bytesData = null;
		byte[] bytesExecCode = new byte[4];
		byte[] bytesTotalLength = new byte[4];
		TransactionData data = null;
		int iExecCode = 0;
		int iTotalLength = 0;
		
		try {
			stream = socket.getInputStream();
		} 
		catch (IOException e) {//error, exits thread.
			e.printStackTrace();
			return;
		}
		
		bytesData = new byte[128];
		running = true;
		try {
			while(running) {
				data = new SimpleTransactionData();
				stream.read(bytesExecCode);
				stream.read(bytesTotalLength);
				iExecCode = CommonUtility.convertBytesToInt(bytesExecCode);
				iTotalLength = CommonUtility.convertBytesToInt(bytesTotalLength);
				if (iTotalLength - 8 >= bytesData.length) {
					bytesData = new byte[iTotalLength];
				}
				stream.read(bytesData, 0, iTotalLength - 8);
				data.setExecCode(iExecCode);
				data.setTotalLength(iTotalLength);
				data.setTransactionData(bytesData);
				queueReceive.add(data);
				data = null;
				Arrays.fill(bytesData, (byte)0);
				Thread.sleep(1l);
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
	
}
