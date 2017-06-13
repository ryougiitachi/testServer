package per.itachi.test.server.v2.config;

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
	private int serverMaxConn;
	private String serverFilterIP;
	private Charset charset;
	
	private boolean enableConsole;
	private int consolePort;
	
	/**
	 * @param path	The path of configuration file about error codes.
	 * */
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
		digester.addObjectCreate("servers", BasicServers.class);
		digester.addObjectCreate("servers/server", BasicServer.class);
		digester.addBeanPropertySetter("servers/server/ip", "ip");
		digester.addBeanPropertySetter("servers/server/port", "port");
		digester.addBeanPropertySetter("servers/server/maxConn", "maxConn");
		digester.addBeanPropertySetter("servers/server/filter", "filter");
		digester.addBeanPropertySetter("servers/server/charset", "charset");
		digester.addSetNext("servers/server", "setServer");
		digester.addObjectCreate("servers/console", BasicConsole.class);
		digester.addBeanPropertySetter("servers/console/enable", "enable");
		digester.addBeanPropertySetter("servers/console/port", "port");
		digester.addSetNext("servers/console", "setConsole");
		//solution 2 not available
//		digester.addObjectCreate("servers", "per.itachi.test.server.v2.config.BasicServers");
//		digester.addSetProperties("servers");
//		digester.addObjectCreate("servers/server", "per.itachi.test.server.v2.config.BasicServer");
//		digester.addSetProperties("servers/server");
//		digester.addBeanPropertySetter("servers/server/ip", "ip");
//		digester.addBeanPropertySetter("servers/server/port", "port");
//		digester.addBeanPropertySetter("servers/server/maxConn", "maxConn");
//		digester.addBeanPropertySetter("servers/server/filter", "filter");
		
		//Step: read configuration file.
		InputStream fis = null;
		BasicServers basic = null;
		try {
			fis = new FileInputStream(file);
			basic = digester.<BasicServers>parse(fis);
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
			config.serverIP = InetAddress.getByName(basic.getServer().getIp());
		} 
		catch (UnknownHostException e) {
			log.error(MessageFormat.format("{0} is invalid server ip, and failed to initialise basic configuration.", basic.getServer().getIp()), e);
			return null;
		}
		//server port
		try {
			config.serverPort = Integer.parseInt(basic.getServer().getPort());
		} 
		catch (NumberFormatException e) {
			log.error(MessageFormat.format("{0} is invalid server port, and failed to initialise basic configuration.", basic.getServer().getPort()), e);
			return null;
		}
		if (config.serverPort < 0 || config.serverPort > 65535) {
			log.error(MessageFormat.format("{0} is invalid server port, and failed to initialise basic configuration.", basic.getServer().getPort()));
			return null;
		}
		//server max connection
		try {
			config.serverMaxConn = Integer.parseInt(basic.getServer().getMaxConn());
		} 
		catch (NumberFormatException e) {
			log.error(MessageFormat.format("{0} is invalid connection number, and failed to initialise basic configuration.", basic.getServer().getMaxConn()), e);
			return null;
		}
		if (config.serverPort < 0 || config.serverPort > 65535) {
			log.error(MessageFormat.format("{0} is invalid connection number, and failed to initialise basic configuration.", basic.getServer().getMaxConn()));
			return null;
		}
		//server filter ip
		config.serverFilterIP = basic.getServer().getFilter();
		//server charset
		try {
			config.charset = Charset.forName(basic.getServer().getCharset());
		} 
		catch (UnsupportedCharsetException e) {
			log.error(MessageFormat.format("{0} is invalid charset name, and failed to initialise basic configuration.", basic.getServer().getCharset()));
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
	
	protected BasicConfig() {
	}

	public InetAddress getServerIP() {
		return serverIP;
	}

	public int getServerPort() {
		return serverPort;
	}

	public int getServerMaxConn() {
		return serverMaxConn;
	}

	public String getServerFilterIP() {
		return serverFilterIP;
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
}
