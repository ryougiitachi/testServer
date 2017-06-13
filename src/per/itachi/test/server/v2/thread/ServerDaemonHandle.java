package per.itachi.test.server.v2.thread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import per.itachi.test.server.v2.config.TotalConfig;

public class ServerDaemonHandle implements ServerRunnable {
	
	private static final Log log = LogFactory.getLog(ServerDaemonHandle.class);
	
	private String name;
	private TotalConfig config;
	private ServerSocket serverSocket;
	private Map<SocketAddress, ClientRunnable> clients;
	private BlockingQueue<SocketAddress> commandQueue;
	
	private boolean running;
	
	public ServerDaemonHandle(TotalConfig config, Map<SocketAddress, ClientRunnable> clients) {
		this.config = config;
		this.clients = clients;
		commandQueue = new LinkedBlockingQueue<>();
	}

	@Override
	public void run() {
		log.info("Starting to initialise server thread...");
		
		InetAddress ip = config.getBasic().getServerIP();
		int port = config.getBasic().getServerPort();
		int maxConn = config.getBasic().getServerMaxConn();
		boolean isFull = false;
		//initialise server socket
		running = true;
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ip, port));
		} 
		catch (IOException e) {
			log.error(MessageFormat.format("Failed to initialise server socket using port {0}.", port), e);
			terminate();
			return;
		}
		//initialise management thread. 
		ServerRunnable management = new ManageClientDaemonHandle(clients, commandQueue);
		Thread thrManagement = new Thread(management, management.toString());
		thrManagement.start();
		
		log.info(MessageFormat.format("Complete initialising server thread, listening {0,number,#}. ", config.getBasic().getServerPort()));
		
		//start
		Socket socket = null;
		ClientRunnable client = null;
		Thread thrClient = null;
		try {
			while (running) {
				socket = serverSocket.accept();//blocking server thread
				log.info(MessageFormat.format("{0} comes in. ", socket.getRemoteSocketAddress()));
				//check whether to exceed the maximum number of connections.
				if (clients.size() >= maxConn) {
					isFull = true;
					log.info(MessageFormat.format("The connection pool is full, and {0} should be disconnected.", 
							socket.getRemoteSocketAddress()));
				}
				else {
					isFull = false;
				}
				client = new ClientDaemonHandle(config, socket, isFull, commandQueue);
				clients.put(socket.getRemoteSocketAddress(), client);
				thrClient = new Thread(client, client.toString());
				thrClient.start();
			}
		} 
		catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			terminate();
		}
	}

	@Override
	public ServerSocket getsServerSocket() {
		return serverSocket;
	}

	/**
	 * This method will close all client sockets at first. </br>
	 * And then server socket will be closed. </br>
	 * Generally, this method will be invoked by main thread instead of server thread.
	 * */
	@Override
	public void terminate() {
		running = false;
		//close clients
		//A synchronized block should be added here? 
		Set<SocketAddress> setKeysAddresses = clients.keySet();
		for (Iterator<SocketAddress> iterator = setKeysAddresses.iterator(); iterator
				.hasNext();) {
			SocketAddress socketAddress = iterator.next();
			clients.get(socketAddress).terminate();
			clients.remove(socketAddress);
		}
		//close server
		if (!serverSocket.isClosed()) {
			try {
				serverSocket.close();
				log.info(MessageFormat.format("{0} is terminated successfully.", toString()));
			} 
			catch (IOException e) {
				log.error(MessageFormat.format("Error occurs when terminating {0}.", toString()), e);
			}
		}
		else {
			log.info(MessageFormat.format("{0} has been closed with no need to terminate it.", toString()));
		}
	}

	@Override
	public String toString() {
		if (name == null) {
			name = MessageFormat.format("Server {0}:{1,number,#} Daemon Thread", 
					config.getBasic().getServerIP().getHostAddress(), config.getBasic().getServerPort());
		}
		return name;
	}
}
