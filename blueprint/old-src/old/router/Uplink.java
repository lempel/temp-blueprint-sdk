package lempel.old.router;

import java.io.*;
import java.net.*;

import lempel.blueprint.log.*;
import lempel.old.framework.*;


/**
 * Uplink Router와의 연결을 담당
 * 
 * @author Administrator
 * @since 2005.5.20.
 * @version 2005.5.20.
 */
public class Uplink extends Linker implements Runnable {

	protected Logger _log = null;

	protected String _header = "Uplink: ";

	/** target ip */
	protected String _ip = null;

	/** target port */
	protected int _port = 0;

	/** connection timeout */
	protected int _timeout = 0;

	protected ObjectOutputStream _oout = null;

	protected ObjectInputStream _oin = null;

	/** socket i/o 제어용 monitor */
	protected Object _busyMonitor = new Object();

	public Uplink() {
		_log = Logger.getInstance();
	}

	public synchronized void connect(String ip, int port, int timeout)
			throws IOException, UnknownHostException {
		_ip = ip;
		_port = port;
		_timeout = timeout;
		connect();
	}

	protected void connect() throws IOException, UnknownHostException {
		_sock = new Socket(_ip, _port);
		_sock.setSoTimeout(_timeout);
		// set SO_LINGER to false
		_sock.setSoLinger(false, 0);
		// RFC 1349 TOS - IPTOS_THROUGHPUT (0x08)
		_sock.setTrafficClass(0x08);

		// Linker에서는 반드시 InputStream 먼저!!
		_oin = new ObjectInputStream(_sock.getInputStream());
		_oout = new ObjectOutputStream(_sock.getOutputStream());
	}

	public void disconnect() {
		try {
			_oout.close();
		} catch (Exception ignored) {
		}

		try {
			_oin.close();
		} catch (Exception ignored) {
		}

		try {
			_sock.close();
		} catch (Exception ignored) {
		}

		_sock = null;
	}

	public Object doJob(Object data) {
		Object result = null;

		synchronized (_busyMonitor) {
			try {
				if (_sock != null) {
					_oout.writeObject(data);
					result = _oin.readObject();
				} else
					_log.println(LogLevel.INF, _header + "not connected");
			} catch (Exception ex) {
				if (ex instanceof SocketTimeoutException)
					_log
							.println(LogLevel.INF, _header
									+ "Connection timed out");
				else if (ex instanceof EOFException)
					_log.println(LogLevel.INF, _header
							+ "Connection closed by peer");
				else if (ex instanceof IOException)
					_log.println(LogLevel.INF, _header
							+ "Connection reset by peer");
				else {
					_log.println(LogLevel.WAN, _header
							+ "Unexpected exception - " + ex);
					ex.printStackTrace();
				}

				disconnect();
			}
		}

		return result;
	}

	public void start() {
		Thread thr = new Thread(this);
		thr.start();
	}

	/**
	 * 30초 간격으로 Keep alive 전송
	 */
	public void run() {
		while (true) {
			// keep alive 전송
			synchronized (_busyMonitor) {
				try {
					_oout.writeObject("KEEP ALIVE");
				} catch (Exception ex) {
					disconnect();

					try {
						connect();
					} catch (Exception exConnect) {
						_log.println(LogLevel.WAN, _header
								+ "Uplink connection failed - " + exConnect);
					}
				}
			}

			// 30초 대기
			try {
				wait(30000);
			} catch (Exception ignored) {
			}
		}
	}
}