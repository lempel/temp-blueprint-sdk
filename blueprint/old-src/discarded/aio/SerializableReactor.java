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
 * Object �ۼ����� ������ Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 23
 * @since 1.5
 * @last $Date$
 * @see
 */
public class SerializableReactor extends Reactor {
	/** String ��� : "instantiation failed - " */
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
	 * ��ü�� �����Ѵ�<br>
	 * (Ping�� Pong�� �ڵ����� ó��)<br>
	 * non-blocking ��忡�� data ������ �Ϸ�Ǳ� ���� ���ῡ ������ ����� null�� ��ȯ�Ѵ�<br>
	 * 
	 * @return Serializable �Ǵ� null
	 * @throws ClassNotFoundException
	 * @throws RuntimeException
	 *             non-blocking ��忡�� data�� ������ �Ϸ���� �������
	 */
	public Serializable receiveObject() throws ClassNotFoundException,
			RuntimeException {
		Serializable result = null;

		// Timeout Handler�� ����Ѵٸ� Timestamp�� update
		if (getTimeoutHandler() != null) {
			getTimeoutHandler().updateTimestamp(this);
		}

		while (true) {
			// ����� ���¿����� ���� ����
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
				// NullPointerException ���� ����� �߻� ����
				if (isBlocking()) {
					// blocking�� ���� ��õ�
					// $ANALYSIS-IGNORE
					continue;
				} else {
					break;
				}
			}

			if (result instanceof Ping) {
				try {
					// Ping�� ���� �������� Pong�� ����
					sendObject(new Pong());
				} catch (IOException e) {
					// ��ſ��� -> ����
					terminate();
				}
			} else if (result instanceof Pong) {
				// Pong�� ����
				// $ANALYSIS-IGNORE
				continue;
			} else {
				// �ǹ� �ִ� ��ü�� ���ŵǾ��ٸ� ������ Ż��
				break;
			}
		}

		return result;
	}

	/**
	 * ��ü�� �����Ѵ�
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void sendObject(Serializable data) throws IOException {
		// ����� ���¿����� �۽� ����
		if (getSocketChannel().isConnected()) {
			send(handler.serialize(data));
		}
	}
}