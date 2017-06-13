package per.itachi.test.server.v2.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import per.itachi.test.server.v2.config.TotalConfig;
import per.itachi.test.server.v2.constant.ErrorCode;
import per.itachi.test.server.v2.constant.TransactionDataConst;
import per.itachi.test.server.v2.data.TransactionDataIn;
import per.itachi.test.server.v2.data.TransactionDataOut;
import per.itachi.test.server.v2.util.CommonUtility;

public class ClientDaemonHandle implements ClientRunnable {

	private static final Log log = LogFactory.getLog(ClientDaemonHandle.class);
	
	private String name;
	private TotalConfig config;
	private Socket socket;
	private boolean full;
	private BlockingQueue<SocketAddress> queue;
	private Map<Integer, TransactionDataIn> dataIn;
	
	private boolean running;
	
	/**
	 * @param full	determine whether the number of clients reaches the maximum value.
	 * 				If true, the current client thread should be terminated. 
	 * */
	public ClientDaemonHandle(TotalConfig config, Socket socket, boolean full, BlockingQueue<SocketAddress> queue) {
		this.config = config;
		this.socket = socket;
		this.full = full;
		this.queue = queue;
		this.dataIn = new HashMap<Integer, TransactionDataIn>(16);
	}
	
	@Override
	public void run() {
		//check whether the number of clients reaches the maximum value. 
		if (full) {
			log.error(MessageFormat.format("Excced the max limit, and {0} should be terminated. ", socket.getRemoteSocketAddress()));
			terminate();
			return;
		}
		//initialise socket I/O.
		InputStream sis = null;
		OutputStream sos = null;
		try {
			sis = socket.getInputStream();
			sos = socket.getOutputStream();
		} 
		catch (IOException e) {
			log.error("Failed to initialise socket I/O, and terminate the current client thread.", e);
			terminate();
			return;
		}
		
		//start
		byte[] bytesIn = new byte[TransactionDataConst.LENGTH_SEGMENT];
		byte[] bytesOut = new byte[TransactionDataConst.LENGTH_SEGMENT];
		int iTrxID;
		TransactionDataIn in;
		TransactionDataOut out;
		
		int iLenInDataBuf = TransactionDataConst.LENGTH_BYTES_DATA;
//		int iLenOutDataBuf = TransactionDataConst.LENGTH_BYTES_DATA;
		byte[] bytesInDataBuf = new byte[iLenInDataBuf];
//		byte[] bytesOutDataBuf = new byte[iLenOutDataBuf];
		String strInData;
		String strOutData;
		
		Map<String, String> mapParamsIn = new HashMap<>();
		Map<String, String> mapParamsOut = new HashMap<>();
		
		try {
			running = true;
			while (running) {
				sis.read(bytesIn);//receive data segment
				iTrxID = CommonUtility.convertBytesToInt(bytesIn, 0, 4);
				log.debug(MessageFormat.format("The content of transaction {0} is as follows:{1}{2}", 
						iTrxID, System.lineSeparator(), CommonUtility.convertBytesToHexStr(bytesIn)));
				in = dataIn.get(iTrxID);
				if (in == null) {
					in = new TransactionDataIn(bytesIn);
				}
				else {
					if (!in.addDataSegment(bytesIn)) {
						log.warn(MessageFormat.format("There is something wrong is {0}.", iTrxID));
					}
				}//if (in == null)
				//check whether detail data is completed. 
				if (in.isComplete()) {
					dataIn.remove(iTrxID);
					if (in.getLength() > iLenInDataBuf) {
						iLenInDataBuf = TransactionDataConst.LENGTH_BYTES_DATA * in.getNum();
						bytesInDataBuf = new byte[iLenInDataBuf];
					}
					in.loadData(bytesInDataBuf);
					strInData = new String(bytesInDataBuf, config.getBasic().getCharset());
					parseFormatParams(mapParamsIn, strInData);
					switch (in.getBusinessID()) {
					case 8001://add an item to blacklist.
						handleBusiness8001(in, mapParamsIn, mapParamsOut);
						break;
					case 8002://remove an item from blacklist
						handleBusiness8002(in, mapParamsIn, mapParamsOut);
						break;
					case 8003://pulse package
						handleBusiness8003(in, mapParamsIn, mapParamsOut);
						break;
					default:
						handleBusinessDefault(in, mapParamsIn, mapParamsOut);
						break;
					}
					strOutData = getParamsString(mapParamsOut);
					out = new TransactionDataOut(in, strOutData.getBytes(config.getBasic().getCharset()));
					//when data segment is not complete, is it necessary to notify client the number of left segments?
					//If yes, this invocation should be out of if block.
					for (int i = 0; i < out.getNum(); i++) {
						out.loadDataSegment(bytesOut, i);
						sos.write(bytesOut); //send data segment
					}
					//clear 
					mapParamsIn.clear();
					mapParamsOut.clear();
				}//if (in.isComplete())
			}//while (running)
		} 
		catch (IOException e) {
			//Usually, e is SocketException?
			log.error(e.getMessage(), e);
		} 
		finally {
			if (sis != null) {
				try {
					sis.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (sos != null) {
				try {
					sos.close();
				} 
				catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			terminate();
			//clear 
			mapParamsIn.clear();
			mapParamsOut.clear();
		}
	}
	
	private void parseFormatParams(Map<String, String> map, String params) {
		//map.clear();
		String[] arrayParams = params.split("&");
		String[] arrayPair;
		for (int i = 0; i < arrayParams.length; i++) {
			arrayPair = arrayParams[i].split("=");
			map.put(arrayPair[0], arrayPair[1]);
		}
	}
	
	private String getParamsString(Map<String, String> params) {
		StringBuilder builder = new StringBuilder(512);
		Set<String> setKey = params.keySet();
		Iterator<String> iterator = setKey.iterator();
		for (iterator = setKey.iterator(); iterator.hasNext();) {
			String strKey = iterator.next();
			builder.append(strKey).append("=").append(params.get(strKey)).append("&");
		}
		if (builder.length() > 0) {
			return builder.substring(0, builder.length() - 1);
		}
		else {
			return builder.toString();
		}
	}
	
	/**
	 * simulate an action of adding an item to blacklist
	 * */
	private void handleBusiness8001(TransactionDataIn dataIn, Map<String, String> paramsIn, Map<String, String> paramsOut) {
		String strSerialNumber = paramsIn.get("serialNumber");
		if (strSerialNumber == null || strSerialNumber.length() == 0) {
			paramsOut.put("returnCode", String.valueOf(ErrorCode.ERROR_CODE_NOT_FOUND_SN));
			paramsOut.put("returnMsg", config.getErrorMsg().getMessage(ErrorCode.ERROR_CODE_NOT_FOUND_SN));
			return;
		}
		log.debug(MessageFormat.format("{0} is into blacklist.", strSerialNumber));
		paramsOut.put("returnCode", String.valueOf(ErrorCode.ERROR_CODE_SUCCESS));
		paramsOut.put("returnMsg", config.getErrorMsg().getMessage(ErrorCode.ERROR_CODE_SUCCESS));
	}
	
	/**
	 * simulate an action of removing an item from blacklist
	 * */
	private void handleBusiness8002(TransactionDataIn dataIn, Map<String, String> paramsIn, Map<String, String> paramsOut) {
		String strSerialNumber = paramsIn.get("serialNumber");
		if (strSerialNumber == null || strSerialNumber.length() == 0) {
			paramsOut.put("returnCode", String.valueOf(ErrorCode.ERROR_CODE_NOT_FOUND_SN));
			paramsOut.put("returnMsg", config.getErrorMsg().getMessage(ErrorCode.ERROR_CODE_NOT_FOUND_SN));
			return;
		}
		log.debug(MessageFormat.format("{0} is out of blacklist.", strSerialNumber));
		paramsOut.put("returnCode", String.valueOf(ErrorCode.ERROR_CODE_SUCCESS));
		paramsOut.put("returnMsg", config.getErrorMsg().getMessage(ErrorCode.ERROR_CODE_SUCCESS));
	}
	
	/**
	 * pulse
	 * */
	private void handleBusiness8003(TransactionDataIn dataIn, Map<String, String> paramsIn, Map<String, String> paramsOut) {
		paramsOut.put("returnCode", String.valueOf(ErrorCode.ERROR_CODE_SUCCESS));
		paramsOut.put("returnMsg", config.getErrorMsg().getMessage(ErrorCode.ERROR_CODE_SUCCESS));
	}
	
	/**
	 * If business id is unknown, use this process. 
	 * */
	private void handleBusinessDefault(TransactionDataIn dataIn, Map<String, String> paramsIn, Map<String, String> paramsOut) {
		paramsOut.put("returnCode", String.valueOf(ErrorCode.ERROR_CODE_NO_SUCH_BUZI));
		paramsOut.put("returnMsg", config.getErrorMsg().getMessage(ErrorCode.ERROR_CODE_NO_SUCH_BUZI));
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Currently, terminate socket directly. 
	 * */
	@Override
	public void terminate() {
		running = false;
		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} 
			catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		//add current thread to remove queue, actually a socket address.
//		queue.add(socket.getRemoteSocketAddress());
		try {
			queue.put(socket.getRemoteSocketAddress());//it is better to use this method because of its blocking
		} 
		catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}//
	}

	/**
	 * just funny to override
	 * */
	@Override
	public int hashCode() {
		return socket.getRemoteSocketAddress().hashCode();
	}

	/**
	 * just funny to override
	 * */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClientDaemonHandle) {
			ClientDaemonHandle client = (ClientDaemonHandle)obj;
			return this.socket.getRemoteSocketAddress().equals(client.socket.getRemoteSocketAddress());
		}
		return false;
	}

	@Override
	public String toString() {
		if (name == null) {
			name = MessageFormat.format("Client {0} Daemon Thread", socket.getRemoteSocketAddress());
		}
		return name;
	}
}
