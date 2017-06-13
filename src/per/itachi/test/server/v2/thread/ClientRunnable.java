package per.itachi.test.server.v2.thread;

import java.net.Socket;

public interface ClientRunnable extends Runnable {
	
	Socket getSocket();
	
	void terminate();

}
