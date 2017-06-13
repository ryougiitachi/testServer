package per.itachi.test.client.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TotalConfig {
	
	private static final Log log = LogFactory.getLog(TotalConfig.class);
	
	private BasicConfig basic;
	
	public static TotalConfig load() {
		TotalConfig config = new TotalConfig();
		log.info("Starting to load all configuration. ");
		config.basic = BasicConfig.load("etc/BasicClient.xml");
		if (config.basic == null) {
			log.info("Failed to load client basic configuration. ");
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
}
