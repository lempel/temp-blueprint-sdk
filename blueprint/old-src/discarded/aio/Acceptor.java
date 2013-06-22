package lempel.blueprint.aio;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import lempel.blueprint.concurrent.Terminatable;
import lempel.blueprint.concurrent.Terminator;
import lempel.blueprint.log.Logger;

/**
 * Accept된 SocketChannel들을 Proactor로 넘겨주는 Server
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 18
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Acceptor implements Terminatable {
	/** String 상수 : "connection closed by peer" */
	private static final String LOG_MSG_0 = "connection closed by peer";

	/** Logger */
	private final Logger logger = Logger.getInstance();

	/** Accept 이후 사용할 Proactor */
	private Proactor proactor = null;

	/** Accept용 Selector */
	private Selector selector = null;

	/** Thread의 지속 여부 */
	private boolean running = false;

	/**
	 * Constructor
	 * 
	 * @param proactor
	 *            Accept 이후 사용할 Proactor
	 * @throws IOException
	 *             Selector 생성 실패
	 */
	public Acceptor(Proactor proactor) throws IOException {
		selector = Selector.open();
		this.proactor = proactor;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see common.Terminatable#terminate()
	 */
	public void terminate() {
		running = false;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see common.Terminatable#start()
	 */
	public void start() {
		setRunning(true);

		// Shutdown Hook에 추가
		Terminator terminator = Terminator.getInstance();
		terminator.register(this);

		Thread t = new Thread(this);
		t.setName(this.getClass().getName());
		t.setDaemon(false);
		t.start();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		SelectionKey key = null;

		while (isRunning()) {
			try {
				if (selector.select() > 0) {
					Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
					while (iter.hasNext()) {
						key = (SelectionKey) iter.next();
						iter.remove();

						if (key.isAcceptable()) {
							// ServerSocketChannel이라면 accept()를 호출해서
							// 접속 요청을 해온 상대방 소켓과 연결 될 수 있는
							// SocketChannel을 얻는다
							if (key.channel() instanceof ServerSocketChannel) {
								ServerSocketChannel serverChannel = (ServerSocketChannel) key
										.channel();
								SocketChannel socketChannel = serverChannel
										.accept();
								if (socketChannel != null) {
									if (socketChannel.isConnectionPending()) {
										socketChannel.finishConnect();
									}

									if (socketChannel.isConnected()) {
										// Proactor로 등록
										proactor.register(socketChannel);
									}
								}
							}
						}
					}
				}
			} catch (CancelledKeyException ignored) {
			} catch (IOException e) {
				logger.info(LOG_MSG_0);
			}
		}
	}

	/**
	 * Returned proactor to Requester
	 * 
	 * @return proactor
	 */
	public Proactor getProactor() {
		return proactor;
	}

	/**
	 * Returned selector to Requester
	 * 
	 * @return selector
	 */
	public Selector getSelector() {
		return selector;
	}

	/**
	 * Returned running to Requester
	 * 
	 * @return running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Set running
	 * 
	 * @param running
	 *            running.
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}
}