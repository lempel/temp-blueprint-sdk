package lempel.old.router;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Vector;

import lempel.blueprint.log.LogLevel;
import lempel.blueprint.util.StringUtil;
import lempel.old.framework.ResourceManager;
import lempel.old.framework.Worker;
import lempel.old.framework.WorkerManager;
import lempel.old.framework.XMLNode;

/**
 * Process들과의 연결을 담당
 * 
 * @author Administrator
 * @since 2005.5.20.
 * @version 2005.5.20.
 */
public class Downlink extends Worker {

	public Downlink(WorkerManager manager, Vector jobQueue, XMLNode node,
			ResourceManager resources) {
		super(manager, jobQueue, node, resources);
	}

	public void exec(Object input) {
		Socket sock = null;
		ObjectOutputStream oout = null;
		ObjectInputStream oin = null;

		try {
			sock = (Socket) input;

			sock.setSoTimeout(60000);
			// set SO_LINGER to false
			sock.setSoLinger(false, 0);
			// RFC 1349 TOS - IPTOS_THROUGHPUT (0x08)
			sock.setTrafficClass(0x08);

			// Woker에서는 반드시 OutputStream 먼저!!
			oout = new ObjectOutputStream(sock.getOutputStream());
			oin = new ObjectInputStream(sock.getInputStream());

			Hashtable job = null;
			while (_runFlag) {
				job = (Hashtable) oin.readObject();
				oout.writeObject(doJob(job));
				oout.flush();
			}
		} catch (SocketTimeoutException ex) {
			_log.println(LogLevel.INF, _header + "Connection timed out");
		} catch (EOFException ex) {
			_log.println(LogLevel.INF, _header + "Connection closed by peer");
		} catch (IOException ex) {
			_log.println(LogLevel.INF, _header + "Connection reset by peer");
		} catch (Exception ex) {
			_log.println(LogLevel.ERR, StringUtil.concatString(_header,
					"Broken connection - ", ex.toString()));
			ex.printStackTrace();
		} finally {
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

	protected Object doJob(Object input) {
		Object result = null;

		// client의 요청대로 적절한 곳으로 전송

		return result;
	}
}