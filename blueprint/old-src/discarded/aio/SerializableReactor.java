package lempel.blueprint.aio;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

import lempel.blueprint.aio.packet.Ping;
import lempel.blueprint.aio.packet.Pong;
import lempel.blueprint.log.Logger;
import lempel.blueprint.util.Serializer;
import lempel.blueprint.util.StringUtil;

/**
 * Object 송수신이 가능한 Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 23
 * @since 1.5
 * @last $Date$
 * @see
 */
public class SerializableReactor extends Reactor {
	/** String 상수 : "instantiation failed - " */
	private static final String LOG_MSG_0 = "instantiation failed - ";

	/** Logger */
	private Logger logger = Logger.getInstance();

	/** handler */
	private Serializer handler = null;

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public SerializableReactor(SocketChannel socketChannel) throws IOException {
		super(socketChannel);

		handler = new Serializer();
	}

	/**
	 * 객체를 수신한다<br>
	 * (Ping과 Pong은 자동으로 처리)<br>
	 * non-blocking 모드에서 data 수신이 완료되기 전에 연결에 문제가 생기면 null을 반환한다<br>
	 * 
	 * @return Serializable 또는 null
	 * @throws ClassNotFoundException
	 * @throws RuntimeException
	 *             non-blocking 모드에서 data의 수신이 완료되지 않은경우
	 */
	public Serializable receiveObject() throws ClassNotFoundException,
			RuntimeException {
		Serializable result = null;

		// Timeout Handler를 사용한다면 Timestamp를 update
		if (getTimeoutHandler() != null) {
			getTimeoutHandler().updateTimestamp(this);
		}

		while (true) {
			// 연결된 상태에서만 수신 가능
			if (!getSocketChannel().isOpen()) {
				break;
			}

			try {
				result = handler.instantiate(receive());
			} catch (IOException e) {
				logger
						.error(StringUtil.concatString(LOG_MSG_0, e
								.getMessage()));
			} catch (RuntimeException e) {
				// NullPointerException 등이 빈번히 발생 가능
				if (isBlocking()) {
					// blocking일 때는 재시도
					// $ANALYSIS-IGNORE
					continue;
				} else {
					break;
				}
			}

			if (result instanceof Ping) {
				try {
					// Ping이 오면 응답으로 Pong을 전송
					sendObject(new Pong());
				} catch (IOException e) {
					// 통신오류 -> 종료
					terminate();
				}
			} else if (result instanceof Pong) {
				// Pong은 무시
				// $ANALYSIS-IGNORE
				continue;
			} else {
				// 의미 있는 객체가 수신되었다면 루프를 탈출
				break;
			}
		}

		return result;
	}

	/**
	 * 객체를 전송한다
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void sendObject(Serializable data) throws IOException {
		// 연결된 상태에서만 송신 가능
		if (getSocketChannel().isConnected()) {
			send(handler.serialize(data));
		}
	}
}