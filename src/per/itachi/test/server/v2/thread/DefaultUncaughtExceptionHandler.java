package per.itachi.test.server.v2.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultUncaughtExceptionHandler implements
		UncaughtExceptionHandler {

	private static final Log log = LogFactory.getLog(DefaultUncaughtExceptionHandler.class);
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error(MessageFormat.format("Uncaught Exception: {0} {1}", t, e.getMessage()), e);
	}
}
