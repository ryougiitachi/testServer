package per.itachi.test.client.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;

import org.apache.commons.digester3.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

public class BasicConfig {
	
	private static final Log log = LogFactory.getLog(BasicConfig.class);
	
	private InetAddress serverIP;
	private int serverPort;
	private int numConn;
	private Charset charset;
	private long interval;
	
	private boolean enableConsole;
	private int consolePort;
	
	public static BasicConfig load(String path) {
		File file = new File(path);
		if (!file.exists()) {
			log.error(MessageFormat.format("{0} doesn't exist, and failed to initialise basic configuration.", path));
			return null;
		}
		
		//Step: define xml rules.
		Digester digester = new Digester();
		digester.setValidating(false);//set false if no dtd file.
		//solution 1
		digester.addObjectCreate("clients", BasicClients.class);
		digester.addObjectCreate("clients/client", BasicClient.class);
		digester.addBeanPropertySetter("clients/client/serverIP", "serverIP");
		digester.addBeanPropertySetter("clients/client/serverPort", "serverPort");
		digester.addBeanPropertySetter("clients/client/numConn", "numConn");
		digester.addBeanPropertySetter("clients/client/charset", "charset");
		digester.addBeanPropertySetter("clients/client/interval", "interval");
		digester.addSetNext("clients/client", "setClient");
		digester.addObjectCreate("clients/console", BasicConsole.class);
		digester.addBeanPropertySetter("clients/console/enable", "enable");
		digester.addBeanPropertySetter("clients/console/port", "port");
		digester.addSetNext("clients/console", "setConsole");

		//Step: read configuration file.
		InputStream fis = null;
		BasicClients basic = null;
		try {
			fis = new FileInputStream(file);
			basic = digester.<BasicClients>parse(fis);
		} 
		catch (FileNotFoundException e) {
			log.error(MessageFormat.format("{0} doesn't exist, and failed to initialise basic configuration.", path), e);
			return null;
		} 
		catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		} 
		catch (SAXException e) {
			log.error(e.getMessage(), e);
			return null;
		}
		finally {
			if (fis!=null) {
				try {
					fis.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		//Step: validate and put config value
		BasicConfig config = new BasicConfig();
		//server ip
		try {
			config.serverIP = InetAddress.getByName(basic.getClient().getServerIP());
		} 
		catch (UnknownHostException e) {
			log.error(MessageFormat.format("{0} is invalid server ip, and failed to initialise basic configuration.", basic.getClient().getServerIP()), e);
			return null;
		}
		//server port
		try {
			config.serverPort = Integer.parseInt(basic.getClient().getServerPort());
		} 
		catch (NumberFormatException e) {
			log.error(MessageFormat.format("{0} is invalid server port, and failed to initialise basic configuration.", basic.getClient().getServerPort()), e);
			return null;
		}
		if (config.serverPort < 0 || config.serverPort > 65535) {
			log.error(MessageFormat.format("{0} is invalid server port, and failed to initialise basic configuration.", basic.getClient().getServerPort()));
			return null;
		}
		//server max connection
		try {
			config.numConn = Integer.parseInt(basic.getClient().getNumConn());
		} 
		catch (NumberFormatException e) {
			log.error(MessageFormat.format("{0} is invalid connection number, and failed to initialise basic configuration.", basic.getClient().getNumConn()), e);
			return null;
		}
		if (config.serverPort < 0 || config.serverPort > 65535) {
			log.error(MessageFormat.format("{0} is invalid connection number, and failed to initialise basic configuration.", basic.getClient().getNumConn()));
			return null;
		}
		//server charset
		try {
			config.charset = Charset.forName(basic.getClient().getCharset());
		} 
		catch (UnsupportedCharsetException e) {
			log.error(MessageFormat.format("{0} is invalid charset name, and failed to initialise basic configuration.", basic.getClient().getCharset()));
			return null;
		}
		//thread sleep interval
		try {
			config.interval = Long.valueOf(basic.getClient().getInterval());
		} 
		catch (NumberFormatException e) {
			log.error(MessageFormat.format("{0} is invalid millisecond, and failed to initialise basic configuration.", basic.getClient().getInterval()), e);
		}
		if (config.interval < 1000l) {
			log.error(MessageFormat.format("{0} is invalid millisecond, and failed to initialise basic configuration.", basic.getClient().getInterval()));
			return null;
		}
		//console enable 
		config.enableConsole = Boolean.parseBoolean(basic.getConsole().getEnable());
		//console port
		try {
			config.consolePort = Integer.parseInt(basic.getConsole().getPort());
		} 
		catch (NumberFormatException e) {
			log.error(MessageFormat.format("{0} is invalid console port, and failed to initialise basic configuration.", basic.getConsole().getPort()), e);
			return null;
		}
		if (config.consolePort < 0 || config.consolePort > 65535 || config.consolePort == config.serverPort) {
			log.error(MessageFormat.format("{0} is invalid console port, and failed to initialise basic configuration.", basic.getConsole().getPort()));
			return null;
		}
		return config;
	}
	
	protected BasicConfig() {}

	public InetAddress getServerIP() {
		return serverIP;
	}

	public int getServerPort() {
		return serverPort;
	}

	public int getNumConn() {
		return numConn;
	}

	public Charset getCharset() {
		return charset;
	}

	public boolean isEnableConsole() {
		return enableConsole;
	}

	public int getConsolePort() {
		return consolePort;
	}

	public long getInterval() {
		return interval;
	}
}
