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
 * SOCKS version 5 용 Relay Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 29
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Socks5RelayReactor extends Reactor {
	/** String 상수 : " " */
	private static final String SPACE = " ";
	/** String 상수 : ":" */
	private static final String COLON = ":";
	/** String 상수 : "]" */
	private static final String RIGHT_BRAKET = "]";
	/** String 상수 : "[" */
	private static final String LEFT_BRAKET = "[";
	/** String 상수 : "connect failed - " */
	private static final String LOG_MSG_0 = "connect failed - ";

	/** 연결이 완료된 상태에서의 수신 버퍼 크기 */
	private static final int BUFFER_LENGTH = 2048;

	/** Logger */
	private final Logger logger = Logger.getInstance();

	/** 반대쪽 reactor (Socks5 connection은 쌍으로 이루어진다) */
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

			// Timeout Handler를 사용한다면 Timestamp를 update
			if (timeoutHandler != null) {
				timeoutHandler.updateTimestamp(this);
			}
		} catch (IOException e) {
			logger.warning(StringUtil.concatString(LOG_MSG_0, LEFT_BRAKET,
					address, COLON, Integer.toString(port), RIGHT_BRAKET,
					SPACE, e.toString()));
			return false;
		}

		// 외부 Selector를 사용하므로 Runnable = false
		setRunnable(false);
		return true;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.aio.Reactor#read()
	 */
	public void read() throws IOException {
		// 수신 buffer가 null이면 새로 생성
		if (receiveBuffer == null) {
			receiveBuffer = ByteBuffer.allocate(BUFFER_LENGTH);
		}

		// SocketChannel에서 read
		int readLength = getSocketChannel().read(receiveBuffer);

		// 실제로 읽은게 있을경우에만 처리
		if (readLength > 0) {
			// 연결 완료 상태와 negotiation 상태는 처리가 다르다
			receiveBuffer.flip();
			int length = receiveBuffer.limit();
			byte[] readData = new byte[length];
			// 수신 buffer에서 length만큼을 수신 data로 복사
			receiveBuffer.get(readData, 0, length);

			// partner로 전송
			partner.sendRawData(readData);

			// 수신 buffer 초기화
			receiveBuffer.position(0);
			receiveBuffer.limit(receiveBuffer.capacity());
		} else if (readLength == -1) {
			// EOF이므로 종료
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
	 * partner가 호출하는 terminate method
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
