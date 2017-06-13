package per.itachi.test.server.v2.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester3.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

public class ErrorMessageConfig {
	
	private static final Log log = LogFactory.getLog(ErrorMessageConfig.class);
	
	private Map<Integer, String> messages;
	
	/**
	 * @param path	The path of configuration file about error codes.
	 * */
	public static ErrorMessageConfig load(String path) {
		File file = new File(path);
		if (!file.exists()) {
			log.error(MessageFormat.format("{0} doesn't exist, and failed to initialise configuration about error message.", path));
			return null;
		}
		
		//Step: define xml rules.
		Digester digester = new Digester();
		digester.setValidating(false);
		digester.addObjectCreate("errors", ErrorMessageErrors.class);
//		digester.addSetProperties("foo");
		digester.addObjectCreate("errors/error", ErrorMessageError.class);
//		digester.addSetProperties("foo/bar");
		digester.addBeanPropertySetter("errors/error/errorCode", "errorCode");
		digester.addBeanPropertySetter("errors/error/errorMsg", "errorMsg"); 
		digester.addSetNext("errors/error", "addError");
		
		//Step: read configuration file.
		InputStream fis = null;
		ErrorMessageErrors errors = null;
		try {
			fis = new FileInputStream(file);
			errors = digester.<ErrorMessageErrors>parse(fis);
		} 
		catch (FileNotFoundException e) {
			log.error(MessageFormat.format("{0} doesn't exist, and failed to initialise configuration about error message.", path), e);
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
		
		//Step: validate and put key value
		ErrorMessageConfig config = new ErrorMessageConfig();
		ErrorMessageError error = null;
		int length = errors.countError();
		try {
			for (int i = 0; i < length; i++) {
				error = errors.getError(i);
				config.messages.put(Integer.valueOf(error.getErrorCode()), error.getErrorMsg());
			}
		} 
		catch (NumberFormatException e) {
			log.error("Failed to initialise configuration about error message due to invalid error code.", e);
			return null;
		}
		finally {
			config.messages.clear();
		}
		return config;
	}
	
	protected ErrorMessageConfig() {
		messages = new HashMap<Integer, String>(32);
	}
	
	public String getMessage(int errorCode) {
		return messages.get(errorCode);
	}
}
