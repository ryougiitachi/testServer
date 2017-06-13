package per.itachi.test.server.v2.thread;

import java.net.ServerSocket;

public interface ServerRunnable extends Runnable {
	
	ServerSocket getsServerSocket();
	
	void terminate();

}
