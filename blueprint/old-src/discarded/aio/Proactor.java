package lempel.blueprint.aio;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;

import lempel.blueprint.concurrent.Terminatable;
import lempel.blueprint.concurrent.Terminator;
import lempel.blueprint.concurrent.Worker;
import lempel.blueprint.log.Logger;

/**
 * Worker thread�� queue�� �����ϴ� Proactor (Proactor Pattern)<br>
 * start()�� ȣ���ϸ� Thread�� ����Ǿ� getSelector()�� ������� Selector�� ��ϵ� ���
 * SocketChannel���� Reactor�� Wrapping�Ͽ� ������ read/write�� ó���Ѵ�<br>
 * <br>
 * getSelector()�� ������� Selector�� ��ϵ� SocketChannel���� SelectionKey��
 * attach(Object) �޼ҵ�� Reactor�� ���� �����ϸ� read(), write(byte[]) �޼ҵ带 ���� ���ϰ� ����� ��
 * �ִ�<br>
 * <br>
 * Proactor as = new Proactor();<br>
 * SocketChannel sc = SocketChannel.open();<br>
 * sc.register(as.getSelector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);<br>
 * <b>Reactor ac = new Reactor(sc);<br>
 * sc.keyFor(as.getSelector()).attach(ac);</b><br>
 * <br>
 * �Ǵ� ����ó�� ª�� ����� �� �ִ�<br>
 * Proactor as = new Proactor();<br>
 * SocketChannel sc = SocketChannel.open();<br>
 * <b>sc.register(as.getSelector(), SelectionKey.OP_READ |
 * SelectionKey.OP_WRITE, new Reactor(sc));</b><br>
 * <br>
 * ���������� Proactor.register(SocketChannel)�� ȣ���ϸ� Reactor�� ��ȯ�ǹǷ� �̰��� �̿��� �� �ִ�<br>
 * <br>
 * ���� SOCKS�� ���� ��� ó�� Reactor �ȿ��� ��� ó���� ������ ���� worker count�� 0���� �����ϸ� worker��
 * �������� �ʰ� ó���� �����ϴ�<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 18
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Proactor implements Terminatable {
	/** String ��� : "there's no workers to start proactor" */
	private static final String LOG_MSG_11 = "no workers. please check";
	/** String ��� : "can't invoke constructor of reactor class" */
	private static final String LOG_MSG_10 = "can't invoke constructor of reactor class";
	/** String ��� : "illegal access to reactor class" */
	private static final String LOG_MSG_9 = "illegal access to reactor class";
	/** String ��� : "can't instantiate reactor class" */
	private static final String LOG_MSG_8 = "can't instantiate reactor class";
	/** String ��� : "wrong argument for constructor of reactor class" */
	private static final String LOG_MSG_7 = "wrong argument for constructor of reactor class";
	/** String ��� : "can't invoke constructor of worker class" */
	private static final String LOG_MSG_5 = "can't invoke constructor of worker class";
	/** String ��� : "illegal access to worker class" */
	private static final String LOG_MSG_4 = "illegal access to worker class";
	/** String ��� : "can't instantiate worker class" */
	private static final String LOG_MSG_3 = "can't instantiate worker class";
	/** String ��� : "wrong argument for constructor of worker class" */
	private static final String LOG_MSG_2 = "wrong argument for constructor of worker class";
	/** String ��� : "can't find constructor of worker class" */
	private static final String LOG_MSG_1 = "can't find constructor of worker class";
	/** String ��� : "workers init failed" */
	private static final String LOG_MSG_0 = "workers init failed";

	/** Logger */
	private final Logger logger = Logger.getInstance();

	/** Worker Thread�� */
	protected Worker[] workers = null;

	/** �������� Worker Thread�� */
	protected Vector<Worker> sleepingWorkers = new Vector<Worker>(10, 10);
	/** Proactor���� �۾� queue */
	protected Vector<Object> jobQueue = new Vector<Object>(1000, 100);

	/** Timeout ó���� ���� Handler */
	private TimeoutHandler timeoutHandler = null;

	/** Writer Thread */
	protected Writer writer = null;

	/** Channel Selector */
	private Selector selector = null;

	/** Reactor Class�� constructor */
	private Constructor<?> cons = null;

	/**
	 * receive() �޼ҵ��� ���� ��� (true: blocking, false: non-blocking, default:
	 * false)
	 */
	private boolean blocking = false;
	/** Thread�� ���� ���� */
	private boolean running = false;

	/**
	 * Constructor
	 * 
	 * @param count
	 *            Worker�� ��
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @throws NoSuchMethodException
	 *             client class�� �����ڰ� ����
	 * @throws SecurityException
	 *             client class�� �����ڸ� ������ �� ����
	 * @throws IOException
	 */
	public Proactor(int count, Class<?> reactorClass, Class<?> workerClass)
			throws SecurityException, NoSuchMethodException, IOException {
		this(Selector.open(), count, reactorClass, workerClass);
	}

	/**
	 * Constructor
	 * 
	 * @param count
	 *            Worker�� ��
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @param timeoutHandler
	 * @throws NoSuchMethodException
	 *             client class�� �����ڰ� ����
	 * @throws SecurityException
	 *             client class�� �����ڸ� ������ �� ����
	 * @throws IOException
	 */
	public Proactor(int count, Class<?> reactorClass, Class<?> workerClass,
			TimeoutHandler timeoutHandler) throws SecurityException,
			NoSuchMethodException, IOException {
		this(Selector.open(), count, reactorClass, workerClass, timeoutHandler);
	}

	/**
	 * Constructor
	 * 
	 * @param selector
	 * @param count
	 *            Worker�� ��
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @throws NoSuchMethodException
	 *             client class�� �����ڰ� ����
	 * @throws SecurityException
	 *             client class�� �����ڸ� ������ �� ����
	 * @throws IOException
	 */
	public Proactor(Selector selector, int count, Class<?> reactorClass,
			Class<?> workerClass) throws SecurityException, NoSuchMethodException,
			IOException {
		this.selector = selector;

		// writer thread ����
		// TODO �� �ڸ��� Writer Thread Pool�� �߰�
		// TODO Writer�� ������ �ڵ����� �����ϴ� ��İ� �����ϴ� ��� �� �� ����
		writer = new Writer();

		// $ANALYSIS-IGNORE
		if (!initWorkers(count, workerClass)) {
			logger.error(LOG_MSG_0);
			workers = null;
		}

		cons = reactorClass.getConstructor(new Class[] { SocketChannel.class });
	}

	/**
	 * Constructor
	 * 
	 * @param selector
	 * @param count
	 *            Worker�� ��
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @param timeoutHandler
	 * @throws NoSuchMethodException
	 *             client class�� �����ڰ� ����
	 * @throws SecurityException
	 *             client class�� �����ڸ� ������ �� ����
	 * @throws IOException
	 */
	public Proactor(Selector selector, int count, Class<?> reactorClass,
			Class<?> workerClass, TimeoutHandler timeoutHandler)
			throws SecurityException, NoSuchMethodException, IOException {
		this(selector, count, reactorClass, workerClass);
		this.timeoutHandler = timeoutHandler;
	}

	/**
	 * Worker���� �ʱ�ȭ �Ѵ�<br>
	 * �ʱ�ȭ�� �ѹ��� �����ϴ�<br>
	 * 
	 * @param count
	 *            Worker�� ��
	 * @param workerClass
	 *            Worker�� ����� Class
	 * @return true: �ʱ�ȭ ����, false: �ʱ�ȭ ����
	 */
	public boolean initWorkers(int count, Class<?> workerClass) {
		// �̹� �����Ǿ� ������ pass
		if (workers != null) {
			return false;
		}

		// worker�� �ʿ���� ����� true�� ��ȯ
		if (count == 0) {
			return true;
		}

		boolean result = false;
		Constructor<?> cons = null;
		try {
			cons = workerClass.getConstructor(new Class[] { Vector.class,
					Vector.class });
			workers = new Worker[count];
			for (int i = 0; i < count; i++) {
				workers[i] = (Worker) cons.newInstance(new Object[] { jobQueue,
						sleepingWorkers });
				workers[i].start();
			}

			result = true;
		} catch (NoSuchMethodException e) {
			logger.error(LOG_MSG_1);
		} catch (IllegalArgumentException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_2);
		} catch (InstantiationException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_3);
		} catch (IllegalAccessException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_4);
		} catch (InvocationTargetException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_5);
		}

		return result;
	}

	// $ANALYSIS-IGNORE
	/**
	 * SocketChannel�� ������ Selector�� ���.<br>
	 * TimeoutHandler�� �ִ°�� �Բ� ���.<br>
	 * 
	 * @param socketChannel
	 * @return
	 * @throws IOException
	 */
	public Reactor register(SocketChannel socketChannel) throws IOException {
		Reactor client = null;

		try {
			// Reflection���� Client Session�� �����ϰ�
			client = (Reactor) cons.newInstance(new Object[] { socketChannel });
			// selector ����
			client.setSelector(selector);
			// blocking mode ����
			client.setBlocking(isBlocking());
			// writer thread ����
			client.setWriter(writer);
			// SocketChannel�� ������ non-blocking���� ����
			socketChannel.configureBlocking(false);
			// SocketChannel�� Selector�� register�ϰ�
			// Client Session�� key�� attach
			socketChannel.register(selector, SelectionKey.OP_READ, client);
			// Timeout Handler�� ������ client�� set
			if (timeoutHandler != null) {
				timeoutHandler.updateTimestamp(client);
				client.setTimeoutHandler(timeoutHandler);
			}
		} catch (IllegalArgumentException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_7);
		} catch (InstantiationException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_8);
		} catch (IllegalAccessException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_9);
		} catch (InvocationTargetException e) {
			// reflection ���� ����
			logger.error(LOG_MSG_10);
		}

		return client;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see common.Terminatable#terminate()
	 */
	public void terminate() {
		setRunning(false);

		if (selector != null) {
			try {
				selector.close();
			} catch (IOException ignored) {
			}
		}
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see common.Terminatable#start()
	 */
	public void start() {
		if (workers == null) {
			logger.info(LOG_MSG_11);
		}

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

		while (running) {
			try {
				// select gains lock of selector.
				// to register channels, need to release lock periodically
				if (selector.select(10) > 0) {
					Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
					while (iter.hasNext()) {
						key = (SelectionKey) iter.next();
						iter.remove();

						Reactor client = null;
						if (key.attachment() != null) {
							client = (Reactor) key.attachment();
						}

						// SocketChannel�� ����Ǿ� �ִ°� Ȯ��
						if (client.getSocketChannel().isConnected()) {
							if (key.isReadable()) {
								// TODO �� �ڸ��� Reader Thread Pool�� �߰�
								// TODO Reader�� ������ �����ϴ� ��� ����
								// TODO Reader���� latency�� ����ؼ� �ڵ����� ������ �����ϴ� ��� ����
								doRead(client);
							}
						} else {
							// ����Ǿ� ���� �ʴٸ� ����
							// XXX �ʿ��Ѱ�?
							terminateClient(key);
						}
					}
				}
			} catch (CancelledKeyException ignored) {
				terminateClient(key);
			} catch (IOException e) {
				terminateClient(key);
			} catch (RuntimeException e) {
				terminateClient(key);
			}

			// give little time to register
			try {
				// $ANALYSIS-IGNORE
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Worker Thread�� queue�� read event�� �߻��� Reactor���� �ִ´�
	 * 
	 * @param client
	 * @throws IOException
	 */
	protected void doRead(Reactor client) throws IOException {
		// worker�� ���ٸ� ���� read�ϰ� ��
		if (workers != null) {
			// ���� read�ϴ� ���� �ƴϰ� Proactor�� �۾� queue�� �ִ� ���̱� ������
			// �ߺ��Ǽ� ���� �ʵ��� �ؾ� �Ѵ�
			// contains�� Ȯ������ ������ queue�� �����Ѵ�
			if (!jobQueue.contains(client)) {
				// FIXME read�� ���� �ϴ� ������ �־�����? ������ Ȯ���ϰ� ����
				// client.read();
				jobQueue.add(client);
				notifySleepingWorker();
			}
		} else {
			client.read();
		}
	}

	/**
	 * �������� Worker �ϳ��� Ȱ��ȭ ��Ų��
	 */
	protected void notifySleepingWorker() {
		synchronized (sleepingWorkers) {
			if (sleepingWorkers.size() > 0) {
				Worker target = (Worker) sleepingWorkers.remove(0);
				synchronized (target) {
					target.notifyAll();
				}
			}
		}
	}

	/**
	 * �ش� Key�� attach�� Reactor�� terminate
	 * 
	 * @param key
	 */
	protected void terminateClient(SelectionKey key) {
		// client session ����
		Reactor client = (Reactor) key.attachment();
		if (client != null) {
			if (timeoutHandler != null) {
				timeoutHandler.remove(client);
			}
			client.terminate();
		}
	}

	/**
	 * Returned timeoutHandler to Requester
	 * 
	 * @return timeoutHandler
	 */
	public TimeoutHandler getTimeoutHandler() {
		return timeoutHandler;
	}

	/**
	 * Set timeoutHandler
	 * 
	 * @param timeoutHandler
	 *            timeoutHandler.
	 */
	public void setTimeoutHandler(TimeoutHandler timeoutHandler) {
		this.timeoutHandler = timeoutHandler;
	}

	/**
	 * Returned writer to Requester
	 * 
	 * @return writer
	 */
	public Writer getWriter() {
		return writer;
	}

	/**
	 * Returned selector to Requester<br>
	 * 
	 * @return selector
	 */
	public Selector getSelector() {
		return selector;
	}

	/**
	 * receive() �޼ҵ��� ���� ����� ��ȯ
	 * 
	 * @return blocking true: blocking, false: non-blocking
	 */
	public boolean isBlocking() {
		return blocking;
	}

	/**
	 * receive() �޼ҵ��� ���� ����� ����
	 * 
	 * @param blocking
	 *            true: blocking, false: non-blocking
	 */
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
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