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
 * Client와 Server에서 모두 사용 가능한 Session (Reactor Pattern)<br>
 * <br>
 * Header (10bytes) + Data로 이루어진 Protocol을 사용한다<br>
 * Header는 Data의 길이로써 ASC-II code로 된 10진수 integer이며 alignment와 padding은 상관하지 않는다<br>
 * <br>
 * isRunnable()과 isRunning()이 모두 true여야만 Thread로 동작이 가능하다<br>
 * <b>i.e.</b> 외부 Selector를 사용하면 Thread로 동작 시킬 수 없다<br>
 * <br>
 * 생성 -> <b>setBlocking(boolean)</b> -> connect(String, int) -> start() 순으로 호출하여
 * 사용한다<br>
 * <br>
 * Client 모드로 사용하는 경우 setWriter(Writer)를 통해 별도의 Writer를 설정해 주면 성능 향상을 기대할 수 있다<br>
 * Server 모드인 경우는 Proactor가 스스로 Writer를 제공한다<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 18
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Reactor implements TerminatableConnection {
	/** String 상수 : ", stopped" */
	private static final String TO_STRING_4 = ", stopped";
	/** String 상수 : ", running" */
	private static final String TO_STRING_3 = ", running";
	/** String 상수 : ", disconnected" */
	private static final String TO_STRING_2 = ", disconnected";
	/** String 상수 : ", connected" */
	private static final String TO_STRING_1 = ", connected";
	/** String 상수 : " : receiveQ = " */
	private static final String TO_STRING_0 = " : receiveQ = ";
	/** String 상수 : "invalid header received" */
	private static final String LOG_MSG_1 = "invalid header received";
	/** String 상수 : "connect failed - " */
	private static final String LOG_MSG_0 = "connect failed - ";

	/** Header의 길이 */
	public static final int HEADER_LENGTH = 10;

	/** 수신할 data의 길이 */
	protected int incomingLength = 0;
	/** 수신한 data의 길이 */
	protected int receivedLength = 0;
	/** 수신 buffer의 길이 (기본 10kbytes) */
	private int receiveBufferLength = 10240;

	/** 수신중인 data의 유형 (true: Header, false: Data) */
	protected boolean receivingHeader = true;

	/** 수신 buffer */
	protected ByteBuffer receiveBuffer = ByteBuffer
			.allocate(getReceiveBufferLength());

	/** 현재까지 수신된 data */
	protected byte[] receivedData = null;

	/** 수신 buffer 제어용 lock */
	protected Object receiveLock = new Object();

	/** 수신 queue */
	protected Vector<byte[]> receiveQ = new Vector<byte[]>(100, 10);

	/** Logger */
	private Logger logger = Logger.getInstance();

	/** 통신용 SocketChannel */
	protected SocketChannel socketChannel = null;

	/** channel용 Selector */
	protected Selector selector = null;

	/** Timeout Handler */
	protected TimeoutHandler timeoutHandler = null;

	/** Writer Thread */
	protected Writer writer = null;

	/**
	 * receive() 메소드의 동작 방식 (true: blocking, false: non-blocking, default:
	 * false)
	 */
	private boolean blocking = false;
	/** Thread로 실행할 수 있는지 여부 (true: 가능, false: 불가능, default: false) */
	private boolean runnable = false;
	/** Thread의 지속 여부 (true: 지속, false: 종료, default: false) */
	private boolean running = false;

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 *             non-blocking 모드 설정 실패
	 */
	public Reactor(SocketChannel socketChannel) throws IOException {
		this.socketChannel = socketChannel;
	}

	/**
	 * 해당 address/port로 연결<br>
	 * 자신만의 Selector를 사용하므로 Thread로 실행 가능<br>
	 * 
	 * @param address
	 * @param port
	 */
	public boolean connect(String address, int port) {
		boolean result = false;
		try {
			// Selector 생성
			selector = Selector.open();
			result = connect(address, port, selector);
		} catch (IOException e) {
			logger.warning(LOG_MSG_0 + e);
			result = false;
		}

		// 내부 Selector를 사용하므로 Runnable = true
		setRunnable(true);
		return result;
	}

	// $ANALYSIS-IGNORE
	/**
	 * 해당 address/port로 연결<br>
	 * 외부 Selector를 사용하므로 Thread로 실행 불가능<br>
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

			// Timeout Handler를 사용한다면 Timestamp를 update
			if (timeoutHandler != null) {
				timeoutHandler.updateTimestamp(this);
			}
		} catch (IOException e) {
			logger.warning(LOG_MSG_0 + e);
			return false;
		}

		// 외부 Selector를 사용하므로 Runnable = false
		setRunnable(false);
		return true;
	}

	/**
	 * Controller와의 연결을 종료
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
	 * data를 수신한다 isBlocking()의 결과에 따라 blocking/non-blocking으로 동작한다
	 * 
	 * @return 수신한 byte[] 혹은 null (non-blocking 일때만 발생 가능)
	 */
	public byte[] receive() {
		byte[] result = null;

		// 연결된 상태에서만 수신 가능
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
	 * data를 수신한다
	 * 
	 * @return 수신된 byte[] 혹은 null
	 */
	protected byte[] nonblockingReceive() {
		byte[] result = null;

		try {
			if (socketChannel.isConnected()) {
				result = (byte[]) receiveQ.remove(0);
			}
		} catch (RuntimeException e) {
			// non-blocking 모드에서는 정상
			// (data 수신이 완료되지 않은 상태에서 빈번하게 발생)
			result = null;
		}
		return result;
	}

	/**
	 * data를 수신한다 (수신이 완료될때까지 block된다)
	 * 
	 * @return
	 */
	protected byte[] blockingReceive() {
		byte[] result = null;

		// 수신 queue가 비었으면 대기
		synchronized (receiveLock) {
			while (receiveQ.size() <= 0) {
				// FIXME 여기서 deadlock 발생이 가능. Socket이 끊어지는 것을 감지해서 notify하도록 해야
				// 하는데...
				try {
					receiveLock.wait();
				} catch (InterruptedException ignored) {
				}
			}

			try {
				result = (byte[]) receiveQ.remove(0);
			} catch (RuntimeException ignored) {
				// terminate()에서 notify()를 호출할때만 발생하므로 무시한다
			}
		}

		return result;
	}

	/**
	 * data를 송신한다
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
	 * Writer Thread가 없는 경우 직접 ByteBuffer를 SocketChannel로 write한다
	 * 
	 * @param data
	 */
	protected void send(ByteBuffer data) throws IOException {
		// Timeout Handler를 사용한다면 Timestamp를 update
		if (timeoutHandler != null) {
			timeoutHandler.updateTimestamp(this);
		}

		if (getWriter() != null) {
			writer.write(getSocketChannel(), data);
		} else {
			// writer가 없으면 직접 전송
			int written = 0;
			while (written < data.capacity()) {
				written += socketChannel.write(data);
			}
		}
	}

	/**
	 * <b>주의!!</b> 별도의 프로토콜을 사용하지 않는 Raw data를 송신한다
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
			// queue들을 청소
			receiveQ.clear();
		}

		// lock을 해제
		synchronized (receiveLock) {
			receiveLock.notifyAll();
		}

		if (socketChannel != null) {
			// Selector에서 자신의 Key를 제거
			SelectionKey key = socketChannel.keyFor(selector);
			if (key != null) {
				key.cancel();
				key.attach(null);
			}
		}

		// Timeout Handler를 쓰고 있다면 Handler에서 제거
		if (timeoutHandler != null) {
			timeoutHandler.remove(this);
		}

		// 내부 Selector를 사용하는 경우 Selector도 종료
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
		// Runnable == true 인 경우에만 start 가능
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
				// Terminate되면서 loop가 동시에 실행되는 경우에 주로 발생하므로 그냥 무시한다
				terminate();
			}
		}
	}

	/**
	 * SocketChannel에서 data를 읽는다<br>
	 * data의 수신이 완료되면 receive()의 block을 해제한다<br>
	 * 
	 * @throws IOException
	 */
	public void read() throws IOException {
		// *WARNING*
		// Very Sophisticated Logic! DO NOT modify!
		// 잘못 건드리면 제대로 바보된다!

		// TODO ByteBuffer handling을 별도의 Class로 구현

		// Timeout Handler를 사용한다면 Timestamp를 update
		if (timeoutHandler != null) {
			timeoutHandler.updateTimestamp(this);
		}

		// 수신할 길이를 알수 없다 == 길이 정보를 수신하지 않았다
		if (incomingLength <= 0) {
			// 수신할 길이를 header의 길이로 set
			incomingLength = HEADER_LENGTH;
			// header 수신 상태로 set
			receivingHeader = true;
			// buffer를 HEADER_LENGTH만큼만 사용할 수 있도록 설정 == Overrun 방지
			receiveBuffer.position(0);
			receiveBuffer.limit(HEADER_LENGTH);
		}

		// SocketChannel에서 read
		int readLength = getSocketChannel().read(receiveBuffer);

		// 실제로 읽은게 있을경우에만 처리
		if (readLength > 0) {
			// 전체 수신 길이에 합산
			receivedLength += readLength;

			boolean more = true;
			// buffer에 있는 모든 data를 소비할때 까지 반복
			while (more) {
				// 수신 모드에 따라서...
				if (receivingHeader) {
					// header 수신
					more = readHeader();
				} else {
					// data 수신
					more = readData();
					// data 수신 완료 후에도 buffer에 data가 남아있다면
					if (more) {
						// header부터 다시 처리한다
						incomingLength = HEADER_LENGTH;
						receivingHeader = true;
					}
				}
			}
		} else if (readLength == -1) {
			// EOF이므로 종료
			terminate();
		}
	}

	/**
	 * 수신 buffer에서 data를 읽는다
	 * 
	 * @return true: buffer에 남은 data가 있다
	 */
	protected boolean readData() {
		boolean result = false;

		receiveBuffer.flip();
		int length = 0;
		if (receivedLength > incomingLength) {
			// Overrun되었다면 남은 길이를 계산
			length = receiveBuffer.remaining()
					- (receivedLength - incomingLength);
			result = true;
		} else {
			// 수신한 만큼 처리
			length = receiveBuffer.limit();
		}

		int offset = 0;
		if (receivedData == null) {
			receivedData = new byte[length];
		} else {
			// offset은 기존 data의 길이로 set
			offset = receivedData.length;
			byte[] tempData = receivedData;
			// 새로 수신한 길이를 반영하여 새 byte[]를 생성
			receivedData = new byte[tempData.length + length];
			// 기존에 수신한 내용을 새 byte[]로 복사
			System.arraycopy(tempData, 0, receivedData, 0, tempData.length);
		}

		// 현재 위치 mark
		receiveBuffer.mark();
		// 수신 buffer에서 length만큼을 수신 data로 복사
		receiveBuffer.get(receivedData, offset, length);

		// 수신이 완료되었는가 확인
		if (receivedLength >= incomingLength) {
			if (receivedLength > incomingLength) {
				// Overrun되었다면 소모한 부분만 buffer에서 제거
				receiveBuffer.compact();
			} else {
				// 수신 buffer 초기화
				receiveBuffer.position(0);
				receiveBuffer.limit(receiveBuffer.capacity());
			}

			// 길이정보 초기화
			receivedLength -= incomingLength;
			incomingLength = 0;

			// queue에 추가하고
			receiveQ.add(receivedData);
			// 수신한 data를 reset
			receivedData = null;

			// data수신이 완료되었으므로 receive()의 block을 해제
			synchronized (receiveLock) {
				receiveLock.notifyAll();
			}
		} else {
			// 아직 더 수신해야 하므로 reset
			receiveBuffer.reset();
		}

		return result;
	}

	/**
	 * 수신 buffer에서 header를 읽는다
	 * 
	 * @return true: buffer에 남은 data가 있다
	 */
	protected boolean readHeader() {
		boolean result = false;

		// header가 수신이 완료된 경우에만 처리
		if (receivedLength >= HEADER_LENGTH) {
			receiveBuffer.flip();

			// 수신 buffer에서 header를 추출
			byte[] header = new byte[HEADER_LENGTH];
			receiveBuffer.mark();
			receiveBuffer.get(header, 0, header.length);

			if (receivedLength > incomingLength) {
				// Overrun되었다면 소모한 부분만 buffer에서 제거
				receiveBuffer.compact();
				result = true;
			} else {
				// 아직 더 수신해야 하므로 reset
				receiveBuffer.reset();
				// 수신 buffer 초기화
				receiveBuffer.position(0);
				receiveBuffer.limit(receiveBuffer.capacity());
			}

			receivedLength -= incomingLength;
			// header의 내용을 수신할 data 길이로 set
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
	 * Returned runnable to Requester
	 * 
	 * @return runnable
	 */
	public boolean isRunnable() {
		return runnable;
	}

	/**
	 * Set runnable<br>
	 * 하위 class들만 변경할 수 있다<br>
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