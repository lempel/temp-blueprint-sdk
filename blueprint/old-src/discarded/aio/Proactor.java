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
 * Worker thread의 queue를 포함하는 Proactor (Proactor Pattern)<br>
 * start()를 호출하면 Thread로 실행되어 getSelector()로 얻어지는 Selector에 등록된 모든
 * SocketChannel들을 Reactor로 Wrapping하여 스스로 read/write를 처리한다<br>
 * <br>
 * getSelector()로 얻어지는 Selector에 등록된 SocketChannel들의 SelectionKey의
 * attach(Object) 메소드로 Reactor를 직접 설정하면 read(), write(byte[]) 메소드를 통해 편리하게 사용할 수
 * 있다<br>
 * <br>
 * Proactor as = new Proactor();<br>
 * SocketChannel sc = SocketChannel.open();<br>
 * sc.register(as.getSelector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);<br>
 * <b>Reactor ac = new Reactor(sc);<br>
 * sc.keyFor(as.getSelector()).attach(ac);</b><br>
 * <br>
 * 또는 다음처럼 짧게 사용할 수 있다<br>
 * Proactor as = new Proactor();<br>
 * SocketChannel sc = SocketChannel.open();<br>
 * <b>sc.register(as.getSelector(), SelectionKey.OP_READ |
 * SelectionKey.OP_WRITE, new Reactor(sc));</b><br>
 * <br>
 * 마지막으로 Proactor.register(SocketChannel)을 호출하면 Reactor가 반환되므로 이것을 이용할 수 있다<br>
 * <br>
 * 만일 SOCKS와 같은 경우 처럼 Reactor 안에서 모든 처리가 끝나는 경우는 worker count를 0으로 설정하면 worker를
 * 생성하지 않고 처리가 가능하다<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 18
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Proactor implements Terminatable {
	/** String 상수 : "there's no workers to start proactor" */
	private static final String LOG_MSG_11 = "no workers. please check";
	/** String 상수 : "can't invoke constructor of reactor class" */
	private static final String LOG_MSG_10 = "can't invoke constructor of reactor class";
	/** String 상수 : "illegal access to reactor class" */
	private static final String LOG_MSG_9 = "illegal access to reactor class";
	/** String 상수 : "can't instantiate reactor class" */
	private static final String LOG_MSG_8 = "can't instantiate reactor class";
	/** String 상수 : "wrong argument for constructor of reactor class" */
	private static final String LOG_MSG_7 = "wrong argument for constructor of reactor class";
	/** String 상수 : "can't invoke constructor of worker class" */
	private static final String LOG_MSG_5 = "can't invoke constructor of worker class";
	/** String 상수 : "illegal access to worker class" */
	private static final String LOG_MSG_4 = "illegal access to worker class";
	/** String 상수 : "can't instantiate worker class" */
	private static final String LOG_MSG_3 = "can't instantiate worker class";
	/** String 상수 : "wrong argument for constructor of worker class" */
	private static final String LOG_MSG_2 = "wrong argument for constructor of worker class";
	/** String 상수 : "can't find constructor of worker class" */
	private static final String LOG_MSG_1 = "can't find constructor of worker class";
	/** String 상수 : "workers init failed" */
	private static final String LOG_MSG_0 = "workers init failed";

	/** Logger */
	private final Logger logger = Logger.getInstance();

	/** Worker Thread들 */
	protected Worker[] workers = null;

	/** 대기상태의 Worker Thread들 */
	protected Vector<Worker> sleepingWorkers = new Vector<Worker>(10, 10);
	/** Proactor들의 작업 queue */
	protected Vector<Object> jobQueue = new Vector<Object>(1000, 100);

	/** Timeout 처리를 위한 Handler */
	private TimeoutHandler timeoutHandler = null;

	/** Writer Thread */
	protected Writer writer = null;

	/** Channel Selector */
	private Selector selector = null;

	/** Reactor Class의 constructor */
	private Constructor<?> cons = null;

	/**
	 * receive() 메소드의 동작 방식 (true: blocking, false: non-blocking, default:
	 * false)
	 */
	private boolean blocking = false;
	/** Thread의 지속 여부 */
	private boolean running = false;

	/**
	 * Constructor
	 * 
	 * @param count
	 *            Worker의 수
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker를 상속한 Class
	 * @throws NoSuchMethodException
	 *             client class의 생성자가 없음
	 * @throws SecurityException
	 *             client class의 생성자를 가져올 수 없음
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
	 *            Worker의 수
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker를 상속한 Class
	 * @param timeoutHandler
	 * @throws NoSuchMethodException
	 *             client class의 생성자가 없음
	 * @throws SecurityException
	 *             client class의 생성자를 가져올 수 없음
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
	 *            Worker의 수
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker를 상속한 Class
	 * @throws NoSuchMethodException
	 *             client class의 생성자가 없음
	 * @throws SecurityException
	 *             client class의 생성자를 가져올 수 없음
	 * @throws IOException
	 */
	public Proactor(Selector selector, int count, Class<?> reactorClass,
			Class<?> workerClass) throws SecurityException, NoSuchMethodException,
			IOException {
		this.selector = selector;

		// writer thread 생성
		// TODO 이 자리에 Writer Thread Pool을 추가
		// TODO Writer의 개수를 자동으로 조절하는 방식과 지정하는 방식 둘 다 구현
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
	 *            Worker의 수
	 * @param reactorClass
	 *            Reactor Class(client session)
	 * @param workerClass
	 *            Worker를 상속한 Class
	 * @param timeoutHandler
	 * @throws NoSuchMethodException
	 *             client class의 생성자가 없음
	 * @throws SecurityException
	 *             client class의 생성자를 가져올 수 없음
	 * @throws IOException
	 */
	public Proactor(Selector selector, int count, Class<?> reactorClass,
			Class<?> workerClass, TimeoutHandler timeoutHandler)
			throws SecurityException, NoSuchMethodException, IOException {
		this(selector, count, reactorClass, workerClass);
		this.timeoutHandler = timeoutHandler;
	}

	/**
	 * Worker들의 초기화 한다<br>
	 * 초기화는 한번만 가능하다<br>
	 * 
	 * @param count
	 *            Worker의 수
	 * @param workerClass
	 *            Worker를 상속한 Class
	 * @return true: 초기화 성공, false: 초기화 실패
	 */
	public boolean initWorkers(int count, Class<?> workerClass) {
		// 이미 생성되어 있으면 pass
		if (workers != null) {
			return false;
		}

		// worker가 필요없는 경우라면 true로 반환
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
			// reflection 관련 오류
			logger.error(LOG_MSG_2);
		} catch (InstantiationException e) {
			// reflection 관련 오류
			logger.error(LOG_MSG_3);
		} catch (IllegalAccessException e) {
			// reflection 관련 오류
			logger.error(LOG_MSG_4);
		} catch (InvocationTargetException e) {
			// reflection 관련 오류
			logger.error(LOG_MSG_5);
		}

		return result;
	}

	// $ANALYSIS-IGNORE
	/**
	 * SocketChannel을 내부의 Selector에 등록.<br>
	 * TimeoutHandler가 있는경우 함께 등록.<br>
	 * 
	 * @param socketChannel
	 * @return
	 * @throws IOException
	 */
	public Reactor register(SocketChannel socketChannel) throws IOException {
		Reactor client = null;

		try {
			// Reflection으로 Client Session을 생성하고
			client = (Reactor) cons.newInstance(new Object[] { socketChannel });
			// selector 설정
			client.setSelector(selector);
			// blocking mode 설정
			client.setBlocking(isBlocking());
			// writer thread 설정
			client.setWriter(writer);
			// SocketChannel은 무조건 non-blocking으로 설정
			socketChannel.configureBlocking(false);
			// SocketChannel을 Selector에 register하고
			// Client Session을 key에 attach
			socketChannel.register(selector, SelectionKey.OP_READ, client);
			// Timeout Handler가 있으면 client에 set
			if (timeoutHandler != null) {
				timeoutHandler.updateTimestamp(client);
				client.setTimeoutHandler(timeoutHandler);
			}
		} catch (IllegalArgumentException e) {
			// reflection 관련 오류
			logger.error(LOG_MSG_7);
		} catch (InstantiationException e) {
			// reflection 관련 오류
			logger.error(LOG_MSG_8);
		} catch (IllegalAccessException e) {
			// reflection 관련 오류
			logger.error(LOG_MSG_9);
		} catch (InvocationTargetException e) {
			// reflection 관련 오류
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

						// SocketChannel이 연결되어 있는가 확인
						if (client.getSocketChannel().isConnected()) {
							if (key.isReadable()) {
								// TODO 이 자리에 Reader Thread Pool을 추가
								// TODO Reader의 개수를 지정하는 방식 구현
								// TODO Reader들의 latency를 계산해서 자동으로 개수를 조절하는 방식 구현
								doRead(client);
							}
						} else {
							// 연결되어 있지 않다면 종료
							// XXX 필요한가?
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
	 * Worker Thread의 queue에 read event가 발생한 Reactor들을 넣는다
	 * 
	 * @param client
	 * @throws IOException
	 */
	protected void doRead(Reactor client) throws IOException {
		// worker가 없다면 직접 read하고 끝
		if (workers != null) {
			// 직접 read하는 것이 아니고 Proactor의 작업 queue에 넣는 것이기 때문에
			// 중복되서 들어가지 않도록 해야 한다
			// contains를 확인하지 않으면 queue가 폭주한다
			if (!jobQueue.contains(client)) {
				// FIXME read를 먼저 하던 이유가 있었던가? 실험후 확인하고 삭제
				// client.read();
				jobQueue.add(client);
				notifySleepingWorker();
			}
		} else {
			client.read();
		}
	}

	/**
	 * 대기상태의 Worker 하나를 활성화 시킨다
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
	 * 해당 Key에 attach된 Reactor를 terminate
	 * 
	 * @param key
	 */
	protected void terminateClient(SelectionKey key) {
		// client session 종료
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
	 * receive() 메소드의 동작 방식을 반환
	 * 
	 * @return blocking true: blocking, false: non-blocking
	 */
	public boolean isBlocking() {
		return blocking;
	}

	/**
	 * receive() 메소드의 동작 방식을 설정
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