package per.itachi.test.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import per.itachi.test.server.v2.config.TotalConfig;

public class EntryConsole {

	private static final Log log = LogFactory.getLog(EntryConsole.class);
	
	/** 
	 * 〈Simple description of the method〉  
	 * 〈Function of the method〉 
	 * 
	 * @param [parameter type]     [description of the parameter]  
	 * @param [parameter type]     [description of the parameter] 
	 * @return  [return type] 
	 * @exception/throws [exception type] [description of exception]  
	 * @see [relate method or delegation method]
	 */
	public static void main(String[] args) {
		TotalConfig config = TotalConfig.load();
		if (config.getBasic() == null || config.getErrorMsg() == null) {
			log.error("Failed to load configurations, and terminate launch. ");
			return;
		}
		Socket socket;
		socket = new Socket();
		try {
			log.info(MessageFormat.format("Start connecting to {0,number,#}", config.getBasic().getConsolePort()));
			socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), config.getBasic().getConsolePort()));
			log.info("Connected");
		} 
		catch (IOException e) {
			log.error("Failed to connect to console endpoint.", e);
			return;
		}
		
		Scanner command = null;
		Scanner input = null;
		Writer output = null;
		
		String strCommandLine = null;
		
		boolean running;
		try {
			command = new Scanner(System.in, config.getBasic().getCharset().name());
			input = new Scanner(socket.getInputStream(), config.getBasic().getCharset().name());
			output = new OutputStreamWriter(socket.getOutputStream(), config.getBasic().getCharset());
			
//			output.write("Hello! Server" + System.lineSeparator());
			log.info(MessageFormat.format("Console Information: {0}", input.nextLine()));
			running = true;
			while (running) {
				log.info("command>");
				strCommandLine = command.nextLine();
				if (strCommandLine.equals("quit") || strCommandLine.equals("exit")) {
					running = false;
				} 
				output.write(strCommandLine);
				output.write(System.lineSeparator());//must end with enter
				output.flush();
				log.info(input.nextLine());
//				while (input.hasNextLine()) {
//				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (output != null) {
				try {
					output.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (input != null) {
				input.close();
			}
			if (command != null) {
				command.close();
			}
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		log.info("Finished.");
	}
}
