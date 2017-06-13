package per.itachi.test.server.v2.console;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import per.itachi.test.server.v2.config.TotalConfig;
import per.itachi.test.server.v2.thread.ClientRunnable;
import per.itachi.test.server.v2.thread.ServerRunnable;

/**
 * Client should always send message at first? nextLine only get the sentences ending with "\n\r" and flush
 * */
public class ConsoleDaemonHandle implements ServerRunnable {
	
	private static final Log log = LogFactory.getLog(ConsoleDaemonHandle.class);
	
	private TotalConfig config;
	private Map<SocketAddress, ClientRunnable> clients;
	private ServerSocket serverSocket;
	private boolean running;

	public ConsoleDaemonHandle(TotalConfig config, Map<SocketAddress, ClientRunnable> clients) {
		this.config = config;
		this.clients = clients;
	}
	
	@Override
	public void run() {
		log.info("Starting to initialise console thread...");
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 
					config.getBasic().getConsolePort()));
		} 
		catch (IOException e) {
			log.error(MessageFormat.format("Failed to initialise console socket using port {0}.", config.getBasic().getConsolePort()), e);
			terminate();
			return;
		}

		log.info(MessageFormat.format("Complete initialising console thread, listening {0,number,#}. ", config.getBasic().getConsolePort()));
		
		Socket socket = null;
		Scanner input = null;
		Writer output = null;
		OutputStream sos = null;
		boolean isConnected = false;
		
		running = true;
		try {
			while (running) {
				socket = serverSocket.accept();//it is better to use data block. 
				log.info(MessageFormat.format("{0} comes to console.", socket.getRemoteSocketAddress()));
				try {
					sos = socket.getOutputStream();
					
					input = new Scanner(socket.getInputStream(), config.getBasic().getCharset().name());
					output = new OutputStreamWriter(sos, config.getBasic().getCharset());
					//welcome message
//					log.info(input.nextLine());
//					sos.write(MessageFormat.format("Connect successfully, current connection number is {0,number,#}", clients.size()).getBytes());
					output.write(MessageFormat.format("Welcome to connect! current connection number is {0,number,#}{1}", clients.size(), System.lineSeparator()));
					output.flush();//easy to forget
					isConnected = true;
					while (isConnected) {
						isConnected = handleCommand(input, output);
					}
					output.write("Thanks.");
					output.flush();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
				catch (NoSuchElementException e) {
					log.error("Maybe disconnect?", e);
				}
				finally {
					if (input != null) {
						input.close();
					}
					if (output != null) {
						output.close();
					}
					socket.close();
					input = null;
					output = null;
//					socket = null;
				}
				log.info(MessageFormat.format("{0} has disconnected.", socket.getRemoteSocketAddress()));
			}
			
		} 
		catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				try {
					output.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (!serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		log.info("Console is closed.");
	}
	
	/**
	 * @return	temporarily, return boolean, show whether client wants to disconnect.
	 * */
	private boolean handleCommand(Scanner input, Writer output) throws IOException {
		String strCommondLine = null;
		StringBuilder builder = new StringBuilder(512);
		boolean isConnected = false;
		
//		log.debug("Having next is " + input.hasNext());
//		strCommondLine = input.next();//input.next() returns one word.
//		log.debug("Having next is " + input.hasNextLine());
		strCommondLine = input.nextLine();
		log.debug(MessageFormat.format("Client console says {0}", strCommondLine));
		if (strCommondLine.equals("quit") || strCommondLine.equals("exit")) {
			isConnected = false;
		} 
		else {
			isConnected = true;
			if (strCommondLine.equals("list")) {
				builder.append("All clients is as listed: ").append(System.lineSeparator());
				Set<SocketAddress> setKeys = clients.keySet();
				for (SocketAddress key : setKeys) {
					builder.append(key).append(System.lineSeparator());
				}
				output.write(builder.toString());
			}
			else if (strCommondLine.equals("disconnect")) {
				String strIP = input.next();
				String strPort = input.next();
				SocketAddress address;
				ClientRunnable client;
				try {
					address = new InetSocketAddress(InetAddress.getByName(strIP), Integer.parseInt(strPort));
					client = clients.get(address);
					if (client == null) {
						builder.append(MessageFormat.format("{0} not found", address)).append(System.lineSeparator());
					}
					else {
						client.terminate();
//						clients.remove(address);//maybe not necessary?
						builder.append(MessageFormat.format("{0} has been disconnected.", address)).append(System.lineSeparator());
					}
				} 
				catch (UnknownHostException e) {
					log.error(e.getMessage(), e);
					builder.append(MessageFormat.format("IP {0} is invalid", strIP)).append(System.lineSeparator());
				}
				catch (NumberFormatException e) {
					log.error(e.getMessage(), e);
					builder.append(MessageFormat.format("Port {0} is invalid", strPort)).append(System.lineSeparator());
				}
				finally {
					output.write(builder.toString());
				}
			}
			else if (strCommondLine.equals("help")) {
				builder.append("developing").append(System.lineSeparator());
				output.write(builder.toString());
			}
			else if (strCommondLine.equals("shutdown")) {//unsafe function?
				builder.append("developing").append(System.lineSeparator());
				output.write(builder.toString());
			}
			else {
				builder.append("Invalid command").append(System.lineSeparator());
				output.write(builder.toString());
			}
			output.flush();
		}
		builder.setLength(0);
		return isConnected;
	}

	@Override
	public ServerSocket getsServerSocket() {
		return serverSocket;
	}

	@Override
	public void terminate() {
		running = false;
//		if (serverSocket != null && !serverSocket.isClosed()) {
//			try {
//				serverSocket.close();
//			} 
//			catch (IOException e) {
//				log.error(e.getMessage(), e);
//			}
//		}
	}

	@Override
	public String toString() {
		return "Console Daemon Thread";
	}
}
