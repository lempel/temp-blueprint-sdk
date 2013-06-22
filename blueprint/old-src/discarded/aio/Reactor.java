package lempel.blueprint.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;

import lempel.blueprint.log.Logger;
import lempel.blueprint.util.StringUtil;

/**
 * Client�� Server���� ��� ��� ������ Session (Reactor Pattern)<br>
 * <br>
 * Header (10bytes) + Data�� �̷���� Protocol�� ����Ѵ�<br>
 * Header�� Data�� ���̷ν� ASC-II code�� �� 10���� integer�̸� alignment�� padding�� ������� �ʴ´�<br>
 * <br>
 * isRunnable()�� isRunning()�� ��� true���߸� Thread�� ������ �����ϴ�<br>
 * <b>i.e.</b> �ܺ� Selector�� ����ϸ� Thread�� ���� ��ų �� ����<br>
 * <br>
 * ���� -> <b>setBlocking(boolean)</b> -> connect(String, int) -> start() ������ ȣ���Ͽ�
 * ����Ѵ�<br>
 * <br>
 * Client ���� ����ϴ� ��� setWriter(Writer)�� ���� ������ Writer�� ������ �ָ� ���� ����� ����� �� �ִ�<br>
 * Server ����� ���� Proactor�� ������ Writer�� �����Ѵ�<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 18
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Reactor implements TerminatableConnection {
	/** String ��� : ", stopped" */
	private static final String TO_STRING_4 = ", stopped";
	/** String ��� : ", running" */
	private static final String TO_STRING_3 = ", running";
	/** String ��� : ", disconnected" */
	private static final String TO_STRING_2 = ", disconnected";
	/** String ��� : ", connected" */
	private static final String TO_STRING_1 = ", connected";
	/** String ��� : " : receiveQ = " */
	private static final String TO_STRING_0 = " : receiveQ = ";
	/** String ��� : "invalid header received" */
	private static final String LOG_MSG_1 = "invalid header received";
	/** String ��� : "connect failed - " */
	private static final String LOG_MSG_0 = "connect failed - ";

	/** Header�� ���� */
	public static final int HEADER_LENGTH = 10;

	/** ������ data�� ���� */
	protected int incomingLength = 0;
	/** ������ data�� ���� */
	protected int receivedLength = 0;
	/** ���� buffer�� ���� (�⺻ 10kbytes) */
	private int receiveBufferLength = 10240;

	/** �������� data�� ���� (true: Header, false: Data) */
	protected boolean receivingHeader = true;

	/** ���� buffer */
	protected ByteBuffer receiveBuffer = ByteBuffer
			.allocate(getReceiveBufferLength());

	/** ������� ���ŵ� data */
	protected byte[] receivedData = null;

	/** ���� buffer ����� lock */
	protected Object receiveLock = new Object();

	/** ���� queue */
	protected Vector<byte[]> receiveQ = new Vector<byte[]>(100, 10);

	/** Logger */
	private Logger logger = Logger.getInstance();

	/** ��ſ� SocketChannel */
	protected SocketChannel socketChannel = null;

	/** channel�� Selector */
	protected Selector selector = null;

	/** Timeout Handler */
	protected TimeoutHandler timeoutHandler = null;

	/** Writer Thread */
	protected Writer writer = null;

	/**
	 * receive() �޼ҵ��� ���� ��� (true: blocking, false: non-blocking, default:
	 * false)
	 */
	private boolean blocking = false;
	/** Thread�� ������ �� �ִ��� ���� (true: ����, false: �Ұ���, default: false) */
	private boolean runnable = false;
	/** Thread�� ���� ���� (true: ����, false: ����, default: false) */
	private boolean running = false;

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 *             non-blocking ��� ���� ����
	 */
	public Reactor(SocketChannel socketChannel) throws IOException {
		this.socketChannel = socketChannel;
	}

	/**
	 * �ش� address/port�� ����<br>
	 * �ڽŸ��� Selector�� ����ϹǷ� Thread�� ���� ����<br>
	 * 
	 * @param address
	 * @param port
	 */
	public boolean connect(String address, int port) {
		boolean result = false;
		try {
			// Selector ����
			selector = Selector.open();
			result = connect(address, port, selector);
		} catch (IOException e) {
			logger.warning(LOG_MSG_0 + e);
			result = false;
		}

		// ���� Selector�� ����ϹǷ� Runnable = true
		setRunnable(true);
		return result;
	}

	// $ANALYSIS-IGNORE
	/**
	 * �ش� address/port�� ����<br>
	 * �ܺ� Selector�� ����ϹǷ� Thread�� ���� �Ұ���<br>
	 * 
	 * @param address
	 * @param port
	 * @param selector
	 * @return
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

			if (socketChannel.isConnectionPending()) {
				socketChannel.finishConnect();
			}

			// Timeout Handler�� ����Ѵٸ� Timestamp�� update
			if (timeoutHandler != null) {
				timeoutHandler.updateTimestamp(this);
			}
		} catch (IOException e) {
			logger.warning(LOG_MSG_0 + e);
			return false;
		}

		// �ܺ� Selector�� ����ϹǷ� Runnable = false
		setRunnable(false);
		return true;
	}

	/**
	 * Controller���� ������ ����
	 */
	public void disconnect() {
		try {
			if (socketChannel != null) {
				socketChannel.close();
			}
		} catch (IOException e) {
		}
	}

	// $ANALYSIS-IGNORE
	/**
	 * data�� �����Ѵ� isBlocking()�� ����� ���� blocking/non-blocking���� �����Ѵ�
	 * 
	 * @return ������ byte[] Ȥ�� null (non-blocking �϶��� �߻� ����)
	 */
	public byte[] receive() {
		byte[] result = null;

		// ����� ���¿����� ���� ����
		if (socketChannel.isConnected()) {
			if (isBlocking()) {
				result = blockingReceive();
			} else {
				result = nonblockingReceive();
			}
		}

		return result;
	}

	/**
	 * data�� �����Ѵ�
	 * 
	 * @return ���ŵ� byte[] Ȥ�� null
	 */
	protected byte[] nonblockingReceive() {
		byte[] result = null;

		try {
			if (socketChannel.isConnected()) {
				result = (byte[]) receiveQ.remove(0);
			}
		} catch (RuntimeException e) {
			// non-blocking ��忡���� ����
			// (data ������ �Ϸ���� ���� ���¿��� ����ϰ� �߻�)
			result = null;
		}
		return result;
	}

	/**
	 * data�� �����Ѵ� (������ �Ϸ�ɶ����� block�ȴ�)
	 * 
	 * @return
	 */
	protected byte[] blockingReceive() {
		byte[] result = null;

		// ���� queue�� ������� ���
		synchronized (receiveLock) {
			while (receiveQ.size() <= 0) {
				// FIXME ���⼭ deadlock �߻��� ����. Socket�� �������� ���� �����ؼ� notify�ϵ��� �ؾ�
				// �ϴµ�...
				try {
					receiveLock.wait();
				} catch (InterruptedException ignored) {
				}
			}

			try {
				result = (byte[]) receiveQ.remove(0);
			} catch (RuntimeException ignored) {
				// terminate()���� notify()�� ȣ���Ҷ��� �߻��ϹǷ� �����Ѵ�
			}
		}

		return result;
	}

	/**
	 * data�� �۽��Ѵ�
	 * 
	 * @param data
	 */
	public void send(byte[] data) throws IOException {
		byte[] fileSize = new byte[HEADER_LENGTH];
		for (int i = 0; i < fileSize.length; i++) {
			fileSize[i] = ' ';
		}
		byte[] lengthBytes = Integer.toString(data.length).getBytes();
		System.arraycopy(lengthBytes, 0, fileSize, 0, lengthBytes.length);

		ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH + data.length);
		buffer.put(fileSize);
		buffer.put(data);
		buffer.flip();

		send(buffer);
	}

	/**
	 * Writer Thread�� ���� ��� ���� ByteBuffer�� SocketChannel�� write�Ѵ�
	 * 
	 * @param data
	 */
	protected void send(ByteBuffer data) throws IOException {
		// Timeout Handler�� ����Ѵٸ� Timestamp�� update
		if (timeoutHandler != null) {
			timeoutHandler.updateTimestamp(this);
		}

		if (getWriter() != null) {
			writer.write(getSocketChannel(), data);
		} else {
			// writer�� ������ ���� ����
			int written = 0;
			while (written < data.capacity()) {
				written += socketChannel.write(data);
			}
		}
	}

	/**
	 * <b>����!!</b> ������ ���������� ������� �ʴ� Raw data�� �۽��Ѵ�
	 * 
	 * @param data
	 */
	public void sendRawData(byte[] data) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data);

		send(buffer);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see common.Terminatable#terminate()
	 */
	public void terminate() {
		setRunning(false);
		disconnect();

		if (receiveQ != null) {
			// queue���� û��
			receiveQ.clear();
		}

		// lock�� ����
		synchronized (receiveLock) {
			receiveLock.notifyAll();
		}

		if (socketChannel != null) {
			// Selector���� �ڽ��� Key�� ����
			SelectionKey key = socketChannel.keyFor(selector);
			if (key != null) {
				key.cancel();
				key.attach(null);
			}
		}

		// Timeout Handler�� ���� �ִٸ� Handler���� ����
		if (timeoutHandler != null) {
			timeoutHandler.remove(this);
		}

		// ���� Selector�� ����ϴ� ��� Selector�� ����
		if (isRunnable() && selector != null) {
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
		// Runnable == true �� ��쿡�� start ����
		if (isRunnable()) {
			setRunning(true);

			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		SelectionKey key = null;

		while (isRunnable() && isRunning()) {
			try {
				if (selector.select() > 0) {
					Iterator<SelectionKey> iter = selector.selectedKeys()
							.iterator();
					while (iter.hasNext()) {
						key = (SelectionKey) iter.next();
						iter.remove();

						if (key.isReadable()) {
							read();
						}
					}
				}

				// *IMPORTANT* CPU Overload protection
				try {
					// $ANALYSIS-IGNORE
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			} catch (CancelledKeyException ignored) {
				terminate();
			} catch (IOException e) {
				terminate();
			} catch (RuntimeException e) {
				// Terminate�Ǹ鼭 loop�� ���ÿ� ����Ǵ� ��쿡 �ַ� �߻��ϹǷ� �׳� �����Ѵ�
				terminate();
			}
		}
	}

	/**
	 * SocketChannel���� data�� �д´�<br>
	 * data�� ������ �Ϸ�Ǹ� receive()�� block�� �����Ѵ�<br>
	 * 
	 * @throws IOException
	 */
	public void read() throws IOException {
		// *WARNING*
		// Very Sophisticated Logic! DO NOT modify!
		// �߸� �ǵ帮�� ����� �ٺ��ȴ�!

		// TODO ByteBuffer handling�� ������ Class�� ����

		// Timeout Handler�� ����Ѵٸ� Timestamp�� update
		if (timeoutHandler != null) {
			timeoutHandler.updateTimestamp(this);
		}

		// ������ ���̸� �˼� ���� == ���� ������ �������� �ʾҴ�
		if (incomingLength <= 0) {
			// ������ ���̸� header�� ���̷� set
			incomingLength = HEADER_LENGTH;
			// header ���� ���·� set
			receivingHeader = true;
			// buffer�� HEADER_LENGTH��ŭ�� ����� �� �ֵ��� ���� == Overrun ����
			receiveBuffer.position(0);
			receiveBuffer.limit(HEADER_LENGTH);
		}

		// SocketChannel���� read
		int readLength = getSocketChannel().read(receiveBuffer);

		// ������ ������ ������쿡�� ó��
		if (readLength > 0) {
			// ��ü ���� ���̿� �ջ�
			receivedLength += readLength;

			boolean more = true;
			// buffer�� �ִ� ��� data�� �Һ��Ҷ� ���� �ݺ�
			while (more) {
				// ���� ��忡 ����...
				if (receivingHeader) {
					// header ����
					more = readHeader();
				} else {
					// data ����
					more = readData();
					// data ���� �Ϸ� �Ŀ��� buffer�� data�� �����ִٸ�
					if (more) {
						// header���� �ٽ� ó���Ѵ�
						incomingLength = HEADER_LENGTH;
						receivingHeader = true;
					}
				}
			}
		} else if (readLength == -1) {
			// EOF�̹Ƿ� ����
			terminate();
		}
	}

	/**
	 * ���� buffer���� data�� �д´�
	 * 
	 * @return true: buffer�� ���� data�� �ִ�
	 */
	protected boolean readData() {
		boolean result = false;

		receiveBuffer.flip();
		int length = 0;
		if (receivedLength > incomingLength) {
			// Overrun�Ǿ��ٸ� ���� ���̸� ���
			length = receiveBuffer.remaining()
					- (receivedLength - incomingLength);
			result = true;
		} else {
			// ������ ��ŭ ó��
			length = receiveBuffer.limit();
		}

		int offset = 0;
		if (receivedData == null) {
			receivedData = new byte[length];
		} else {
			// offset�� ���� data�� ���̷� set
			offset = receivedData.length;
			byte[] tempData = receivedData;
			// ���� ������ ���̸� �ݿ��Ͽ� �� byte[]�� ����
			receivedData = new byte[tempData.length + length];
			// ������ ������ ������ �� byte[]�� ����
			System.arraycopy(tempData, 0, receivedData, 0, tempData.length);
		}

		// ���� ��ġ mark
		receiveBuffer.mark();
		// ���� buffer���� length��ŭ�� ���� data�� ����
		receiveBuffer.get(receivedData, offset, length);

		// ������ �Ϸ�Ǿ��°� Ȯ��
		if (receivedLength >= incomingLength) {
			if (receivedLength > incomingLength) {
				// Overrun�Ǿ��ٸ� �Ҹ��� �κи� buffer���� ����
				receiveBuffer.compact();
			} else {
				// ���� buffer �ʱ�ȭ
				receiveBuffer.position(0);
				receiveBuffer.limit(receiveBuffer.capacity());
			}

			// �������� �ʱ�ȭ
			receivedLength -= incomingLength;
			incomingLength = 0;

			// queue�� �߰��ϰ�
			receiveQ.add(receivedData);
			// ������ data�� reset
			receivedData = null;

			// data������ �Ϸ�Ǿ����Ƿ� receive()�� block�� ����
			synchronized (receiveLock) {
				receiveLock.notifyAll();
			}
		} else {
			// ���� �� �����ؾ� �ϹǷ� reset
			receiveBuffer.reset();
		}

		return result;
	}

	/**
	 * ���� buffer���� header�� �д´�
	 * 
	 * @return true: buffer�� ���� data�� �ִ�
	 */
	protected boolean readHeader() {
		boolean result = false;

		// header�� ������ �Ϸ�� ��쿡�� ó��
		if (receivedLength >= HEADER_LENGTH) {
			receiveBuffer.flip();

			// ���� buffer���� header�� ����
			byte[] header = new byte[HEADER_LENGTH];
			receiveBuffer.mark();
			receiveBuffer.get(header, 0, header.length);

			if (receivedLength > incomingLength) {
				// Overrun�Ǿ��ٸ� �Ҹ��� �κи� buffer���� ����
				receiveBuffer.compact();
				result = true;
			} else {
				// ���� �� �����ؾ� �ϹǷ� reset
				receiveBuffer.reset();
				// ���� buffer �ʱ�ȭ
				receiveBuffer.position(0);
				receiveBuffer.limit(receiveBuffer.capacity());
			}

			receivedLength -= incomingLength;
			// header�� ������ ������ data ���̷� set
			try {
				incomingLength = Integer.parseInt(new String(header).trim());
			} catch (RuntimeException ex) {
				logger.error(LOG_MSG_1);
				StringUtil.hexDump(header);
				terminate();
			}

			receivingHeader = false;
		}

		return result;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.terminator.TerminatableConnection#isConnected()
	 */
	public boolean isConnected() {
		if (getSocketChannel() != null) {
			return getSocketChannel().isConnected();
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getClass().getName()
				+ TO_STRING_0
				+ receiveQ
				+ ((getSocketChannel().isConnected() ? TO_STRING_1
						: TO_STRING_2) + ((isRunning() ? TO_STRING_3
						: TO_STRING_4)));
	}

	/**
	 * Returned receiveBufferLength to Requester
	 * 
	 * @return receiveBufferLength
	 */
	public int getReceiveBufferLength() {
		return receiveBufferLength;
	}

	/**
	 * Set receiveBufferLength
	 * 
	 * @param receiveBufferLength
	 *            receiveBufferLength.
	 */
	public void setReceiveBufferLength(int receiveBufferLength) {
		this.receiveBufferLength = receiveBufferLength;
	}

	/**
	 * Returned socketChannel to Requester<br>
	 * 
	 * @return socketChannel
	 */
	public SocketChannel getSocketChannel() {
		return socketChannel;
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
	 * Set selector
	 * 
	 * @param selector
	 *            selector.
	 */
	public void setSelector(Selector selector) {
		this.selector = selector;
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
	 * Set writer
	 * 
	 * @param writer
	 *            writer.
	 */
	public void setWriter(Writer writer) {
		this.writer = writer;
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
	 * Returned runnable to Requester
	 * 
	 * @return runnable
	 */
	public boolean isRunnable() {
		return runnable;
	}

	/**
	 * Set runnable<br>
	 * ���� class�鸸 ������ �� �ִ�<br>
	 * 
	 * @param runnable
	 *            runnable.
	 */
	protected void setRunnable(boolean runnable) {
		this.runnable = runnable;
	}

	/**
	 * Returned running to Requester
	 * 
	 * @return running
	 */
	public final boolean isRunning() {
		return running;
	}

	/**
	 * Set running
	 * 
	 * @param running
	 *            running.
	 */
	public final void setRunning(boolean running) {
		this.running = running;
	}
}