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
	/** String 상수 : "not implemented" */
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
		// 현재는 GET 만 지원한다

		try {
			// 수신 buffer가 null이면 새로 생성
			if (receiveBuffer == null) {
				receiveBuffer = ByteBuffer.allocate(getReceiveBufferLength());
			}

			// SocketChannel에서 read
			int readLength = getSocketChannel().read(receiveBuffer);

			// 실제로 읽은게 있을경우에만 처리
			if (readLength > 0) {
				receiveBuffer.flip();
				byte[] data = new byte[readLength];

				// 수신 buffer에서 length만큼을 수신 data로 복사
				receiveBuffer.get(data, 0, readLength);

				// 수신 buffer 초기화
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
				// EOF이므로 종료
				terminate();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			terminate();
		}
	}

	/**
	 * HTTP request가 끝났는가 확인
	 * 
	 * @param req
	 * @return
	 */
	private boolean isEndOfRequest(byte[] req) {
		// 4byte도 안되면 무조건 false
		if (req.length < 4) {
			return false;
		}

		int lastIdx = req.length - 1;

		if (req[lastIdx - 3] == 0x0d && req[lastIdx - 2] == 0x0a
				&& req[lastIdx - 1] == 0x0d && req[lastIdx] == 0x0a) {
			// win32 계열
			return true;
		} else if (req[lastIdx - 3] == 0x0a && req[lastIdx - 3] == 0x0a) {
			// 기타
			return true;
		}

		return false;
	}
}