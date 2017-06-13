package per.itachi.test.server.v2.config;

public class BasicServer {
	
	private String ip;
	private String port;
	private String maxConn;
	private String filter;
	private String charset;
	
	public BasicServer() {}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getPort() {
		return port;
	}
	
	public void setPort(String port) {
		this.port = port;
	}
	
	public String getMaxConn() {
		return maxConn;
	}
	
	public void setMaxConn(String maxConn) {
		this.maxConn = maxConn;
	}
	
	public String getFilter() {
		return filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Original clone method is a protected method with Object return type.
	 * */
	@Override
	public BasicServer clone() throws CloneNotSupportedException {
		BasicServer clone = (BasicServer)super.clone();
		clone.ip = ip;
		clone.port = port;
		clone.maxConn = maxConn;
		clone.filter = filter;
		return clone;
	}

}
