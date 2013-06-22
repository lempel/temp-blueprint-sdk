package lempel.blueprint.service.socks;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import lempel.blueprint.aio.Reactor;
import lempel.blueprint.log.Logger;
import lempel.blueprint.util.StringUtil;

/**
 * SOCKS version 5 �� Relay Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 29
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Socks5RelayReactor extends Reactor {
	/** String ��� : " " */
	private static final String SPACE = " ";
	/** String ��� : ":" */
	private static final String COLON = ":";
	/** String ��� : "]" */
	private static final String RIGHT_BRAKET = "]";
	/** String ��� : "[" */
	private static final String LEFT_BRAKET = "[";
	/** String ��� : "connect failed - " */
	private static final String LOG_MSG_0 = "connect failed - ";

	/** ������ �Ϸ�� ���¿����� ���� ���� ũ�� */
	private static final int BUFFER_LENGTH = 2048;

	/** Logger */
	private final Logger logger = Logger.getInstance();

	/** �ݴ��� reactor (Socks5 connection�� ������ �̷������) */
	private Socks5Reactor partner = null;

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public Socks5RelayReactor(SocketChannel socketChannel) throws IOException {
		super(socketChannel);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.aio.Reactor#connect(java.lang.String, int,
	 *      java.nio.channels.Selector)
	 */
	public boolean connect(String address, int port, Selector selector) {
		this.selector = selector;
		try {
			InetSocketAddress remote = new InetSocketAddress(address, port);
			if (!socketChannel.isOpen()) {
				socketChannel = SocketChannel.open();
			}
			socketChannel.connect(remote);
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ, this);

			// Timeout Handler�� ����Ѵٸ� Timestamp�� update
			if (timeoutHandler != null) {
				timeoutHandler.updateTimestamp(this);
			}
		} catch (IOException e) {
			logger.warning(StringUtil.concatString(LOG_MSG_0, LEFT_BRAKET,
					address, COLON, Integer.toString(port), RIGHT_BRAKET,
					SPACE, e.toString()));
			return false;
		}

		// �ܺ� Selector�� ����ϹǷ� Runnable = false
		setRunnable(false);
		return true;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.aio.Reactor#read()
	 */
	public void read() throws IOException {
		// ���� buffer�� null�̸� ���� ����
		if (receiveBuffer == null) {
			receiveBuffer = ByteBuffer.allocate(BUFFER_LENGTH);
		}

		// SocketChannel���� read
		int readLength = getSocketChannel().read(receiveBuffer);

		// ������ ������ ������쿡�� ó��
		if (readLength > 0) {
			// ���� �Ϸ� ���¿� negotiation ���´� ó���� �ٸ���
			receiveBuffer.flip();
			int length = receiveBuffer.limit();
			byte[] readData = new byte[length];
			// ���� buffer���� length��ŭ�� ���� data�� ����
			receiveBuffer.get(readData, 0, length);

			// partner�� ����
			partner.sendRawData(readData);

			// ���� buffer �ʱ�ȭ
			receiveBuffer.position(0);
			receiveBuffer.limit(receiveBuffer.capacity());
		} else if (readLength == -1) {
			// EOF�̹Ƿ� ����
			terminate();
		}
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.aio.Reactor#terminate()
	 */
	public void terminate() {
		super.terminate();
		if (partner != null && partner.getSocketChannel().isConnected()) {
			try {
				partner.selfTerminate();
			} catch (Exception ignored) {
			}
			partner = null;
		}
	}

	/**
	 * partner�� ȣ���ϴ� terminate method
	 */
	public void selfTerminate() {
		super.terminate();
	}

	/**
	 * Returned partner to Requester
	 * 
	 * @return partner
	 */
	public Socks5Reactor getPartner() {
		return partner;
	}

	/**
	 * Set partner
	 * 
	 * @param partner
	 *            partner.
	 */
	public void setPartner(Socks5Reactor partner) {
		this.partner = partner;
	}
}
