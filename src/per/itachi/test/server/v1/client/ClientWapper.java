package per.itachi.test.server.v1.client;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

import per.itachi.test.server.ClientManager;
import per.itachi.test.server.v1.constant.BusinessConstant;
import per.itachi.test.server.v1.data.SimpleTransactionData;
import per.itachi.test.server.v1.data.TransactionData;
import per.itachi.test.server.v1.runnable.ClosableRunnable;
import per.itachi.test.server.v1.runnable.HandleRunnable;
import per.itachi.test.server.v1.runnable.ReceiveRunnable;
import per.itachi.test.server.v1.runnable.SendRunnable;
import per.itachi.test.server.v1.util.CommonUtility;

public class ClientWapper {
	
	private Socket socket;
	private ClientManager clients;
	
//	private InputStream sis;
//	private OutputStream sos;
	
	private List<TransactionData> queueReceive;
	private List<TransactionData> queueSend;
	private ClosableRunnable runnableReceive;
	private ClosableRunnable runnableSend;
	private ClosableRunnable runnableHandle;
	private Thread threadReceive;
	private Thread threadSend;
	private Thread threadHandle;
	
	public ClientWapper(Socket socket, ClientManager clients) {
		this.socket = socket;
		this.clients = clients;
	}
	
	public void start() {
		if (clients.countOfClients() >= BusinessConstant.TEST_MAX_CLIENTS) {
			queueSend = new Vector<>(8);
			runnableSend = new SendRunnable(queueSend, socket);
			threadSend = new Thread(runnableSend, "Send Thread for " + socket.getRemoteSocketAddress().toString());
			TransactionData data = new SimpleTransactionData();
			data.setExecCode(0);
			data.setTotalLength(12);
			data.setTransactionData(CommonUtility.convertIntToBytes(BusinessConstant.RETURN_CODE_FULL_CONNECTION));
			queueSend.add(data);
			data = null;
			queueSend = null;
			runnableSend = null;
			threadSend = null;
			return;
		}

//		try {
//			sis = socket.getInputStream();
//			sos = socket.getOutputStream();
//		} 
//		catch (IOException e) {
//			e.printStackTrace();
//		}
		queueReceive = new Vector<>(128, 16);
		queueSend = new Vector<>(128, 16);
		runnableReceive = new ReceiveRunnable(queueReceive, socket);
		runnableSend = new SendRunnable(queueSend, socket);
		runnableHandle = new HandleRunnable(queueReceive, queueSend);
		threadReceive = new Thread(runnableReceive, "Receive Thread for " + socket.getRemoteSocketAddress().toString());
		threadSend = new Thread(runnableSend, "Send Thread for " + socket.getRemoteSocketAddress().toString());
		threadHandle = new Thread(runnableHandle, "Handle Thread for " + socket.getRemoteSocketAddress().toString());
		
		threadReceive.start();
		threadSend.start();
		threadHandle.start();
		
		clients.addClient(socket.getRemoteSocketAddress(), this);
	}
	
	public void close() {
		try {
			runnableReceive.close();
			runnableSend.close();
			runnableHandle.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		threadReceive = null;
		threadSend = null;
		threadHandle = null;
		runnableReceive = null;
		runnableSend = null;
		runnableHandle = null;
		queueReceive.clear();
		queueSend.clear();
		queueReceive = null;
		queueSend = null;
	}
}
