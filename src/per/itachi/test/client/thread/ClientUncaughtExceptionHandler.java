package per.itachi.test.client.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientUncaughtExceptionHandler implements UncaughtExceptionHandler {
	
	private static final Log log = LogFactory.getLog(ClientUncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error(MessageFormat.format("Error occurs in {0}.", t.getName()), e);
	}

}
