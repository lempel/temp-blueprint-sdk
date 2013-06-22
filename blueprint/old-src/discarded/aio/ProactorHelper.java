package lempel.blueprint.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * Proactor�� ����� ���� ���ִ� Helper
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 22
 * @since 1.5
 * @last $Date$
 * @see
 */
public class ProactorHelper {
	private Proactor proactor;
	private Acceptor acceptor;

	/**
	 * Constructor
	 * 
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @param workerCount
	 *            Worker�� ��
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param port
	 *            ServerSocket�� bind�� port
	 * @throws NoSuchMethodException
	 *             client class�� �����ڰ� ����
	 * @throws SecurityException
	 *             client class�� �����ڸ� ������ �� ����
	 * @throws IOException
	 */
	public ProactorHelper(Class<?> workerClass, int workerCount,
			Class<?> reactorClass, int port) throws SecurityException,
			NoSuchMethodException, IOException {
		this(workerClass, workerCount, reactorClass,
				new InetSocketAddress(port));
	}

	/**
	 * Constructor
	 * 
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @param workerCount
	 *            Worker�� ��
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param host
	 *            ServerSocket�� bind�� �ּ�
	 * @param port
	 *            ServerSocket�� bind�� port
	 * @throws NoSuchMethodException
	 *             client class�� �����ڰ� ����
	 * @throws SecurityException
	 *             client class�� �����ڸ� ������ �� ����
	 * @throws IOException
	 */
	public ProactorHelper(Class<?> workerClass, int workerCount,
			Class<?> reactorClass, String host, int port)
			throws SecurityException, NoSuchMethodException, IOException {
		this(workerClass, workerCount, reactorClass, new InetSocketAddress(
				host, 8080));
	}

	/**
	 * Constructor
	 * 
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @param workerCount
	 *            Worker�� ��
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param address
	 *            ServerSocket�� bind�� �ּ�
	 * @throws NoSuchMethodException
	 *             client class�� �����ڰ� ����
	 * @throws SecurityException
	 *             client class�� �����ڸ� ������ �� ����
	 * @throws IOException
	 */
	public ProactorHelper(Class<?> workerClass, int workerCount,
			Class<?> reactorClass, InetSocketAddress address)
			throws SecurityException, NoSuchMethodException, IOException {
		proactor = new Proactor(Selector.open(), workerCount, reactorClass,
				workerClass);

		acceptor = new Acceptor(proactor);
		ServerSocketChannel ss = ServerSocketChannel.open();
		ss.configureBlocking(false);
		ss.register(acceptor.getSelector(), SelectionKey.OP_ACCEPT);
		ss.socket().bind(address);
	}

	/**
	 * Proactor/Acceptor�� �����Ͽ� Socket������ �ޱ� ����
	 */
	public void start() {
		proactor.start();
		acceptor.start();
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
	 * Set proactor
	 * 
	 * @param proactor
	 *            proactor.
	 */
	public void setProactor(Proactor proactor) {
		this.proactor = proactor;
	}

	/**
	 * Returned acceptor to Requester
	 * 
	 * @return acceptor
	 */
	public Acceptor getAcceptor() {
		return acceptor;
	}

	/**
	 * Set acceptor
	 * 
	 * @param acceptor
	 *            acceptor.
	 */
	public void setAcceptor(Acceptor acceptor) {
		this.acceptor = acceptor;
	}
}
