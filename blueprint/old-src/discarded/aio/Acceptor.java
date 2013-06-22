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
 * Accept�� SocketChannel���� Proactor�� �Ѱ��ִ� Server
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 18
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Acceptor implements Terminatable {
	/** String ��� : "connection closed by peer" */
	private static final String LOG_MSG_0 = "connection closed by peer";

	/** Logger */
	private final Logger logger = Logger.getInstance();

	/** Accept ���� ����� Proactor */
	private Proactor proactor = null;

	/** Accept�� Selector */
	private Selector selector = null;

	/** Thread�� ���� ���� */
	private boolean running = false;

	/**
	 * Constructor
	 * 
	 * @param proactor
	 *            Accept ���� ����� Proactor
	 * @throws IOException
	 *             Selector ���� ����
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

		// Shutdown Hook�� �߰�
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
							// ServerSocketChannel�̶�� accept()�� ȣ���ؼ�
							// ���� ��û�� �ؿ� ���� ���ϰ� ���� �� �� �ִ�
							// SocketChannel�� ��´�
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
										// Proactor�� ���
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