package lempel.blueprint.aio.example.httpserver;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import lempel.blueprint.aio.SerializableReactor;

/**
 * Simple HTTP Protocol Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 30
 * @since 1.5
 * @last $Date$
 * @see
 */
public class SimpleHttpReactor extends SerializableReactor {
	/** String ��� : "not implemented" */
	private static final String ERR_MSG_0 = "not implemented";

	/** HTTP GET command */
	private byte[] command = new byte[0];

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public SimpleHttpReactor(SocketChannel socketChannel) throws IOException {
		super(socketChannel);

		// $ANALYSIS-IGNORE
		setReceiveBufferLength(256);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see gatas.core.SerializableReactor#receiveObject()
	 */
	public Serializable receiveObject() throws ClassNotFoundException,
			RuntimeException {
		throw new IllegalStateException(ERR_MSG_0);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see gatas.core.SerializableReactor#sendObject(java.io.Serializable)
	 */
	public void sendObject(Serializable data) throws IOException {
		throw new IllegalStateException(ERR_MSG_0);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.aio.Reactor#read()
	 */
	// $ANALYSIS-IGNORE
	public void read() throws IOException {
		// ����� GET �� �����Ѵ�

		try {
			// ���� buffer�� null�̸� ���� ����
			if (receiveBuffer == null) {
				receiveBuffer = ByteBuffer.allocate(getReceiveBufferLength());
			}

			// SocketChannel���� read
			int readLength = getSocketChannel().read(receiveBuffer);

			// ������ ������ ������쿡�� ó��
			if (readLength > 0) {
				receiveBuffer.flip();
				byte[] data = new byte[readLength];

				// ���� buffer���� length��ŭ�� ���� data�� ����
				receiveBuffer.get(data, 0, readLength);

				// ���� buffer �ʱ�ȭ
				receiveBuffer.position(0);
				receiveBuffer.limit(receiveBuffer.capacity());

				byte[] newCommand = new byte[command.length + data.length];
				System.arraycopy(command, 0, newCommand, 0, command.length);
				System.arraycopy(data, 0, newCommand, command.length,
						data.length);

				if (isEndOfRequest(newCommand)) {
					command = null;
					receiveQ.add(newCommand);
				} else {
					command = newCommand;
				}
			} else if (readLength == -1) {
				// EOF�̹Ƿ� ����
				terminate();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			terminate();
		}
	}

	/**
	 * HTTP request�� �����°� Ȯ��
	 * 
	 * @param req
	 * @return
	 */
	private boolean isEndOfRequest(byte[] req) {
		// 4byte�� �ȵǸ� ������ false
		if (req.length < 4) {
			return false;
		}

		int lastIdx = req.length - 1;

		if (req[lastIdx - 3] == 0x0d && req[lastIdx - 2] == 0x0a
				&& req[lastIdx - 1] == 0x0d && req[lastIdx] == 0x0a) {
			// win32 �迭
			return true;
		} else if (req[lastIdx - 3] == 0x0a && req[lastIdx - 3] == 0x0a) {
			// ��Ÿ
			return true;
		}

		return false;
	}
}