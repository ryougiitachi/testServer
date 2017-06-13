package per.itachi.test.client.config;

public class BasicClient {
	
	private String serverIP;
	private String serverPort;
	private String numConn;
	private String charset;
	private String interval;
	
	public BasicClient() {}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getNumConn() {
		return numConn;
	}

	public void setNumConn(String numConn) {
		this.numConn = numConn;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}
}