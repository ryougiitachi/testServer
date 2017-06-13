package per.itachi.test.server.v2.thread;

import java.net.ServerSocket;
import java.net.SocketAddress;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManageClientDaemonHandle implements ServerRunnable {
	
	private static final Log log = LogFactory.getLog(ManageClientDaemonHandle.class);
	
	private Map<SocketAddress, ClientRunnable> clients;
	private BlockingQueue<SocketAddress> commandQueue;
	
	private boolean running;
	
	/**
	 * Currently, the only function is to remove idle client from map. 
	 * */
	public ManageClientDaemonHandle(Map<SocketAddress, ClientRunnable> clients, BlockingQueue<SocketAddress> commandQueue) {
		this.clients = clients;
		this.commandQueue = commandQueue;
	}

	@Override
	public void run() {
		Map<SocketAddress, ClientRunnable> mapClients;
		BlockingQueue<SocketAddress> queue;
		SocketAddress item;
		
		mapClients = clients;
		queue = commandQueue;
		running = true;
		try {
			while (running) {
//				item = queue.poll();
//				if (item == null) {
//					log.info("The command queue is empty, and waiting. ");
//					queue.wait();
//					log.info("Waking up. ");
//				}
//				else {
//					if (mapClients.get(item) != null) {
//						mapClients.remove(item);//a suspected recursive call?
//						log.info(MessageFormat.format("{0} has been removed. ", item));
//					}
//				}
				item = queue.take();//it is better to use this method with blocking
				if (mapClients.get(item) != null) {
					mapClients.remove(item);//a suspected recursive call?
					log.info(MessageFormat.format("{0} is removed. ", item));
				}
				else {
					log.info(MessageFormat.format("{0} doesn't exist in current map. ", item));
				}
			}
		} 
		catch (InterruptedException e) {
			log.error("Management has been interrupted. ", e);
		}
		finally {
			log.info("Thread exits. ");
		}
	}

	/**
	 * It is not necessary for this thread.
	 * */
	@Override
	public ServerSocket getsServerSocket() {
		return null;
	}

	@Override
	public void terminate() {
		running = false;
		Thread.currentThread().interrupt();
	}

	@Override
	public String toString() {
		return "Management about Clients Daemon Thread";
	}
}
