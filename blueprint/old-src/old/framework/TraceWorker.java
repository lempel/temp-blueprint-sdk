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
 * trace������ �������ִ� worker
 * 
 * @author Sang-min Lee
 * @since 2005.1.12.
 * @version 2005.5.20.
 */
public class TraceWorker extends Worker {

	protected Vector _messages = null;

	protected String _header = "TraceWorker: ";

	/**
	 * ��� ����
	 * 
	 * @param manager
	 *            Worker�� �����ϴ� Manager
	 * @param jobQueue
	 *            �۾� queue
	 * @param prop
	 *            ȯ�� ����
	 * @param resources
	 *            Resource ��ü�� �����ϴ� Manager
	 */
	public TraceWorker(WorkerManager manager, Vector jobQueue,
			PropertyManager prop, ResourceManager resources) {
		super(manager, jobQueue, prop, resources);
		throw new RuntimeException("not implemented yet");
	}

	/**
	 * @param manager
	 *            Worker�� �����ϴ� Manager
	 * @param jobQueue
	 *            �۾� queue
	 * @param node
	 *            XML ȯ�� ����
	 * @param resources
	 *            Resource ��ü�� �����ϴ� Manager
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

			// Woker������ �ݵ�� OutputStream ����!!
			oout = new ObjectOutputStream(sock.getOutputStream());
			oin = new ObjectInputStream(sock.getInputStream());

			// �ڽ��� tracer�� ���
			TraceableLinker.setTracer(this);

			while (_runFlag) {
				oin.readObject();

				// ������ �����Ͱ� �ִٸ� ��� ����
				while (_messages.size() > 0) {
					oout.writeObject(_messages.remove(0));
					oout.flush();
				}

				// �������� ��ȣ ����
				oout.writeObject("PONG");
				oout.flush();

				// ������ ������ ���� 1�ʰ� ���
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
	 * client�� ������ �޽����� ����
	 * 
	 * @param header
	 *            �������� �տ� ���� ���
	 * @param value
	 *            ������ ������
	 */
	public synchronized void write(String header, Object value) {
		// timestamp�� header �籸��
		_messages.add(StringUtil.concatString("[", TimeStamper.getDateStamp(), " ",
				TimeStamper.getTimeStamp9(), "] ", header));

		if (value instanceof String[]) {
			// String array�� array�� ���븸 �籸��
			String[] arr = (String[]) value;
			for (int i = 0; i < arr.length; i++)
				_messages.add(StringUtil.sanitize(arr[i]));
		} else if (value instanceof String) {
			// String�̸� �ش� String�� hexa���� ���� ����
			_messages.add(StringUtil.sanitize(value.toString()));
			_messages.add(lempel.blueprint.log.Logger.toHex(value.toString()
					.getBytes()));
		} else
			// ��Ÿ ��ü�̸� �ش� ��ü�� �״�� ����
			_messages.add(value);

		// End of Message
		_messages.add("----------------------------------------");
	}
}