package per.itachi.test.server.v1.runnable;

import java.util.List;

import per.itachi.test.server.v1.data.SimpleTransactionData;
import per.itachi.test.server.v1.data.TransactionData;

public class HandleRunnable implements ClosableRunnable {

	private List<TransactionData> queueReceive;
	private List<TransactionData> queueSend;
	private boolean running;
	
	public HandleRunnable(List<TransactionData> receive, List<TransactionData> send) {
		queueReceive = receive;
		queueSend = send;
	}
	
	@Override
	public void run() {
		TransactionData dataReceive = null;
		TransactionData dataSend = null;
		
		running = true;
		try {
			while(running) {
				if (queueReceive.size() > 0) {
					dataReceive = queueReceive.remove(queueReceive.size() - 1);
					dataReceive.convertToBytes();
					// TODO: something to handle 
					dataSend = new SimpleTransactionData();
					queueSend.add(dataSend);
//					queueSend.notify();//allow to block ? 
					
					dataReceive = null;
					dataSend = null;
				}
				else {
					Thread.sleep(100l);
//					queueReceive.wait();//allow to block ? 
				}
			}
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		this.running = false;
	}
}