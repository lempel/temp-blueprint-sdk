package lempel.old.framework;

import java.io.*;
import java.net.*;

/**
 * �ٸ� Process�� �����ϴµ� ����ϴ� Client�� framework
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2004.5.21.
 */
public abstract class Linker {
	/** target�� ������ Socket */
	protected Socket _sock = null;

	/**
	 * target���� ����
	 * 
	 * @param ip
	 *            target�� ip
	 * @param port
	 *            target�� port
	 * @param timeout
	 *            Socket�� timeout (msec)
	 * @throws IOException
	 *             ���� ����
	 * @throws UnknownHostException
	 *             target�� ã���� ���� ���
	 */
	public abstract void connect(String ip, int port, int timeout)
			throws IOException, UnknownHostException;

	/**
	 * Target���� ������ ���´�
	 */
	public abstract void disconnect();

	/**
	 * data�� target���� �����ϰ� ó�� ����� ��ȯ
	 * 
	 * @param data
	 *            ������ data
	 */
	public abstract Object doJob(Object data);
}