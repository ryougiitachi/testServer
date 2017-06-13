package per.itachi.test.server.v2.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * digester3 requires commons-logging/commons-beanutils/commons-collections
 * */
public class TotalConfig {
	
	private static final Log log = LogFactory.getLog(TotalConfig.class);
	
	private BasicConfig basic;
	private ErrorMessageConfig errorMsg;
	
	public static TotalConfig load() {
		TotalConfig config = new TotalConfig();
		log.info("Starting to load all configuration. ");
		config.basic = BasicConfig.load("etc/BasicServer.xml");
		config.errorMsg = ErrorMessageConfig.load("etc/ErrorMessage.xml");
		if (config.basic == null) {
			log.info("Failed to load server basic configuration. ");
		}
		else if (config.errorMsg == null) {
			log.info("Failed to load error message configuration. ");
		}
		else {
			log.info("Complete loading all configuration. ");
		}
		return config;
	}
	
	protected TotalConfig() {}

	public BasicConfig getBasic() {
		return basic;
	}

	public ErrorMessageConfig getErrorMsg() {
		return errorMsg;
	}
}
