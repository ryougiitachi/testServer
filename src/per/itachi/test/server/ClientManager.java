package per.itachi.test.server;

import java.net.SocketAddress;

import per.itachi.test.server.v1.client.ClientWapper;

public interface ClientManager {
	
	ClientWapper addClient(SocketAddress address, ClientWapper client);
	
	ClientWapper removeClient(ClientWapper client);
	
	int countOfClients();
}
