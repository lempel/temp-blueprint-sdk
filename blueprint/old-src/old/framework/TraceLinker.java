package lempel.old.framework;

import java.io.*;
import java.net.*;

/**
 * ��/���� ������ trace�� ����ϴ� Linker
 * 
 * @author Sang-min Lee
 * @since 2005.1.11.
 * @version 2005.1.11.
 */
public class TraceLinker extends Linker {
	protected ObjectInputStream _oin = null;

	protected ObjectOutputStream _oout = null;

	public static void main(String[] args) throws Throwable {
		if (args.length != 2) {
			System.out
					.println("Usage: com.bluePrint.framework.TraceLinker <ip> <port>");
			System.exit(1);
		}

		TraceLinker linker = new TraceLinker();
		linker.connect(args[0], Integer.parseInt(args[1]), 0);
		linker.doJob(null);
	}

	public void connect(String ip, int port, int timeout) throws IOException,
			UnknownHostException {
		_sock = new Socket(InetAddress.getByName(ip), port);
		_sock.setSoTimeout(timeout);
		// set SO_LINGER to false
		_sock.setSoLinger(false, 0);
		// RFC 1349 TOS - IPTOS_THROUGHPUT (0x08)
		_sock.setTrafficClass(0x08);

		// Linker������ �ݵ�� InputStream ����!!
		_oin = new ObjectInputStream(_sock.getInputStream());
		_oout = new ObjectOutputStream(_sock.getOutputStream());
	}

	public void disconnect() {
		try {
			_oin.close();
		} catch (Exception ignored) {
		}
		try {
			_oout.close();
		} catch (Exception ignored) {
		}
		try {
			_sock.close();
		} catch (Exception ignored) {
		}
	}

	public Object doJob(Object input) {
		try {
			Object data = null;
			while (true) {
				// ���� ������ ���� ��ȣ
				_oout.writeObject("PING");
				_oout.flush();

				while (true) {
					data = _oin.readObject();
					// ���� ���� ��ȣ�� ���� �����̸� ������� �ʰ� Ż��
					if (data.toString().equals("PONG"))
						break;
					System.out.println(data);
				}
			}
		} catch (SocketTimeoutException ex) {
			System.out.println("Connection timed out");
		} catch (EOFException ex) {
			System.out.println("Connection closed by peer");
		} catch (IOException ex) {
			System.out.println("Connection reset by peer");
		} catch (Exception ex) {
			System.out.println("TraceLinker terminated - " + ex);
			ex.printStackTrace(System.out);
		}

		disconnect();

		return null;
	}
}