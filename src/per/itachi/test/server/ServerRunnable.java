package per.itachi.test.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import per.itachi.test.server.v1.client.ClientWapper;
import per.itachi.test.server.v1.runnable.ClosableRunnable;

public class ServerRunnable implements ClosableRunnable, ClientManager {
	
	private ServerSocket serversocket;
	private Map<SocketAddress, ClientWapper> mapClientSockets;
	private boolean running;
	
	public ServerRunnable(int port) {
		try {
			serversocket = new ServerSocket(port);
			mapClientSockets = new HashMap<SocketAddress, ClientWapper>();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		running = true;
		try {
			while(running) {
				Socket socket = serversocket.accept();
				ClientWapper client = new ClientWapper(socket, this);
				client.start();
				synchronized (mapClientSockets) {
					mapClientSockets.put(socket.getRemoteSocketAddress(), client);
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try to stop server thread.
	 * */
	@Override
	public void close() {
		running = false;
		SocketAddress address = null;
		Iterator<SocketAddress> iterator = null;
		synchronized (mapClientSockets) {
			Set<SocketAddress> keys = mapClientSockets.keySet();
			for (iterator = keys.iterator(); iterator.hasNext(); ) {
				address = iterator.next();
				mapClientSockets.remove(address);
				address = null;
			}
		}
	}

	@Override
	public ClientWapper addClient(SocketAddress address, ClientWapper client) {
		synchronized (mapClientSockets) {
			mapClientSockets.put(address, client);
		}
		return client;
	}

	@Override
	public ClientWapper removeClient(ClientWapper client) {
		synchronized (mapClientSockets) {
			mapClientSockets.remove(client);
		}
		return client;
	}

	@Override
	public int countOfClients() {
		return mapClientSockets.size();
	}
}
