package per.itachi.test.server.v1.runnable;

/**
 * Maybe, Closable is better. 
 * */
public interface ControllabilityRunnable extends Runnable {

	boolean isRunning();
	
	void setRunning(boolean running);
}
