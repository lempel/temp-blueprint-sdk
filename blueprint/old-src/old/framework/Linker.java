package lempel.old.framework;

import java.io.*;
import java.net.*;

/**
 * 다른 Process로 연결하는데 사용하는 Client의 framework
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2004.5.21.
 */
public abstract class Linker {
	/** target에 연결할 Socket */
	protected Socket _sock = null;

	/**
	 * target으로 연결
	 * 
	 * @param ip
	 *            target의 ip
	 * @param port
	 *            target의 port
	 * @param timeout
	 *            Socket의 timeout (msec)
	 * @throws IOException
	 *             연결 실패
	 * @throws UnknownHostException
	 *             target을 찾을수 없는 경우
	 */
	public abstract void connect(String ip, int port, int timeout)
			throws IOException, UnknownHostException;

	/**
	 * Target과의 연결을 끊는다
	 */
	public abstract void disconnect();

	/**
	 * data를 target으로 전송하고 처리 결과를 반환
	 * 
	 * @param data
	 *            전송할 data
	 */
	public abstract Object doJob(Object data);
}