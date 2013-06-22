package lempel.old.framework;


import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

import lempel.blueprint.log.LogLevel;
import lempel.blueprint.util.StringUtil;
import lempel.blueprint.util.TimeStamper;

/**
 * trace정보를 전송해주는 worker
 * 
 * @author Sang-min Lee
 * @since 2005.1.12.
 * @version 2005.5.20.
 */
public class TraceWorker extends Worker {

	protected Vector _messages = null;

	protected String _header = "TraceWorker: ";

	/**
	 * 사용 안함
	 * 
	 * @param manager
	 *            Worker를 관리하는 Manager
	 * @param jobQueue
	 *            작업 queue
	 * @param prop
	 *            환경 변수
	 * @param resources
	 *            Resource 객체를 관리하는 Manager
	 */
	public TraceWorker(WorkerManager manager, Vector jobQueue,
			PropertyManager prop, ResourceManager resources) {
		super(manager, jobQueue, prop, resources);
		throw new RuntimeException("not implemented yet");
	}

	/**
	 * @param manager
	 *            Worker를 관리하는 Manager
	 * @param jobQueue
	 *            작업 queue
	 * @param node
	 *            XML 환경 변수
	 * @param resources
	 *            Resource 객체를 관리하는 Manager
	 */
	public TraceWorker(WorkerManager manager, Vector jobQueue, XMLNode node,
			ResourceManager resources) {
		super(manager, jobQueue, node, resources);

		_messages = new Vector();
	}

	public void exec(Object input) {
		Socket sock = null;
		ObjectOutputStream oout = null;
		ObjectInputStream oin = null;

		try {
			sock = (Socket) input;

			sock.setSoTimeout(0);
			// set SO_LINGER to false
			sock.setSoLinger(false, 0);
			// RFC 1349 TOS - IPTOS_THROUGHPUT (0x08)
			sock.setTrafficClass(0x08);

			// Woker에서는 반드시 OutputStream 먼저!!
			oout = new ObjectOutputStream(sock.getOutputStream());
			oin = new ObjectInputStream(sock.getInputStream());

			// 자신을 tracer로 등록
			TraceableLinker.setTracer(this);

			while (_runFlag) {
				oin.readObject();

				// 전송할 데이터가 있다면 모두 전송
				while (_messages.size() > 0) {
					oout.writeObject(_messages.remove(0));
					oout.flush();
				}

				// 접속유지 신호 전송
				oout.writeObject("PONG");
				oout.flush();

				// 과부하 방지를 위해 1초간 대기
				synchronized (this) {
					try {
						sleep(1000);
					} catch (Exception ignored) {
					}
				}
			}
		} catch (SocketTimeoutException ex) {
			_log.println(LogLevel.INF, _header + "Connection timed out");
		} catch (EOFException ex) {
			_log
					.println(LogLevel.INF, _header
							+ "Connection closed by peer");
		} catch (IOException ex) {
			_log.println(LogLevel.INF, _header + "Connection reset by peer");
		} catch (Exception ex) {
			_log.println(LogLevel.ERR, _header + "Client connection failed");
			ex.printStackTrace();
		} finally {
			TraceableLinker.setTracer(null);
			_messages.clear();

			try {
				oout.close();
			} catch (Exception ignored) {
			}

			try {
				oin.close();
			} catch (Exception ignored) {
			}

			try {
				sock.close();
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * client로 전송할 메시지를 구성
	 * 
	 * @param header
	 *            데이터의 앞에 붙일 헤더
	 * @param value
	 *            전송할 데이터
	 */
	public synchronized void write(String header, Object value) {
		// timestamp와 header 재구성
		_messages.add(StringUtil.concatString("[", TimeStamper.getDateStamp(), " ",
				TimeStamper.getTimeStamp9(), "] ", header));

		if (value instanceof String[]) {
			// String array는 array의 내용만 재구성
			String[] arr = (String[]) value;
			for (int i = 0; i < arr.length; i++)
				_messages.add(StringUtil.sanitize(arr[i]));
		} else if (value instanceof String) {
			// String이면 해당 String과 hexa값을 같이 구성
			_messages.add(StringUtil.sanitize(value.toString()));
			_messages.add(lempel.blueprint.log.Logger.toHex(value.toString()
					.getBytes()));
		} else
			// 기타 객체이면 해당 객체만 그대로 전송
			_messages.add(value);

		// End of Message
		_messages.add("----------------------------------------");
	}
}