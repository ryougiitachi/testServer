package per.itachi.test.server;

import java.net.SocketAddress;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import per.itachi.test.server.v2.config.TotalConfig;
import per.itachi.test.server.v2.console.ConsoleDaemonHandle;
import per.itachi.test.server.v2.thread.ClientRunnable;
import per.itachi.test.server.v2.thread.DefaultUncaughtExceptionHandler;
import per.itachi.test.server.v2.thread.ServerRunnable;
import per.itachi.test.server.v2.thread.ServerDaemonHandle;

public class EntryServer {
	
	private static final Log log = LogFactory.getLog(EntryServer.class);

	/**
	 * v1:
	 * ServerRunnable: HashMap{SocketAddress, ClientWapper}
	 * ClientWapper: ReceiveRunnable =Vector=> HandleRunnable =Vector=> SendRunnable
	 * 
	 * v2:
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("Starting to launch server...");
		log.info("Launching main thread...");
		
		TotalConfig config = TotalConfig.load();
		if (config.getBasic() == null || config.getErrorMsg() == null) {
			log.error("Failed to load configurations, and terminate launch. ");
			return;
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
		Map<SocketAddress, ClientRunnable> mapClients = new Hashtable<>(16);
		ServerRunnable server = new ServerDaemonHandle(config, mapClients);
		Thread thrServer = new Thread(server, server.toString());
		//The main thread seems to exit somehow after invoking Thread.setDaemon().--
		//After invoking Thread.setDaemon(true), log4j won't print completely. 
		//Maybe, it is related to the fact that log4j is running on different thread from main thread. 
//		thrServer.setDaemon(true);
		thrServer.start();
		//setDaemon should be invoked before invoking Thread.start().
		//Otherwise, java.lang.IllegalThreadStateException
//		thrServer.setDaemon(true);
		log.info("The main thread has launched.");
//		System.out.println("The main thread has launched.");
		
		ServerRunnable console = new ConsoleDaemonHandle(config, mapClients);
		Thread thrConsole = new Thread(console, console.toString());
		thrConsole.start();
	}
}
