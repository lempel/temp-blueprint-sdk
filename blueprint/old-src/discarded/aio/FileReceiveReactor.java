package lempel.blueprint.aio;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import lempel.blueprint.util.GlobalContext;

/**
 * File ������ ����� �߰��� Serializable Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 30
 * @since 1.5
 * @last $Date$
 * @see
 */
public class FileReceiveReactor extends SerializableReactor {
	/** String ��� : "can't read file during packet mode" */
	private static final String ERR_MSG_2 = "can't read file during packet mode";
	/** String ��� : "can't send object during file mode" */
	private static final String ERR_MSG_1 = "can't send object during file mode";
	/** String ��� : "can't receive object during file mode" */
	private static final String ERR_MSG_0 = "can't receive object during file mode";
	/** String ��� : ".zip" */
	private static final String FILE_NAME_0 = ".zip";
	/** String ��� : "LOG_PATH" */
	private static final String FIELD_NAME_0 = "LOG_PATH";
	/** String ��� : "file.separator" */
	private static final String FILE_SEPARATOR = "file.separator";

	/** ������ file ũ�� */
	private long incomingFileLength = 0;
	/** ������ file ũ�� */
	private long receivedFileLength = 0;

	/** ���� mode (true: ���� ����, false: Packet ����, deault: false) */
	private boolean receiveMode = false;
	/** file ���� ������ ���� (true: file ���� ������) */
	private boolean receivingFileLength = true;
	/** ���� ���� �Ϸ� ���� (true: �Ϸ�) */
	private boolean receiveFileFinished = false;

	/** ���ŵǴ� file�� ������ �̸� (path�� ����) */
	private String fileName = null;

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public FileReceiveReactor(SocketChannel socketChannel) throws IOException {
		super(socketChannel);

		// ���� buffer�� 100kb�� set
		// $ANALYSIS-IGNORE
		setReceiveBufferLength(102400);

		GlobalContext context = GlobalContext.getInstance();
		// context���� LOG_PATH�� �����´�
		String sep = System.getProperty(FILE_SEPARATOR);
		String filePath = (String) context.get(FIELD_NAME_0);
		if (!filePath.endsWith(sep)) {
			filePath += sep;
		}
		// file �̸��� agent�� address�� ����
		fileName = filePath
				+ socketChannel.socket().getInetAddress().getHostAddress()
				+ FILE_NAME_0;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see gatas.core.SerializableReactor#receiveObject()
	 */
	public Serializable receiveObject() throws ClassNotFoundException,
			RuntimeException {
		if (isReceiveMode()) {
			throw new IllegalStateException(ERR_MSG_0);
		}

		return super.receiveObject();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see gatas.core.SerializableReactor#sendObject(java.io.Serializable)
	 */
	public void sendObject(Serializable data) throws IOException {
		if (isReceiveMode()) {
			throw new IllegalStateException(ERR_MSG_1);
		}

		super.sendObject(data);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see gatas.core.AsyncClient#terminate()
	 */
	public void terminate() {
		super.terminate();

		// ���ſ� ������ ������ ����
		if (isReceiveMode() && !isReceiveFileFinished()) {
			deleteFile();
		}
	}

	/**
	 * ���� �������� ������ ����
	 * 
	 * @return
	 */
	public boolean deleteFile() {
		File target = new File(fileName);
		return target.delete();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.aio.Reactor#read()
	 */
	// $ANALYSIS-IGNORE
	public void read() throws IOException {
		if (receiveMode) {
			if (readFile()) {
				setReceiveFileFinished(true);
			}
		} else {
			super.read();
		}
	}

	/**
	 * file�� ����<br>
	 * fd ������ ���� �׶��׶� file�� ������ �����ϰ� �ݴ´�<br>
	 * 
	 * @return true: ���� �Ϸ�
	 * @throws FileNotFoundException
	 *             ������ file���� �������� �ʾҰų� �߸��� ��� �߻�
	 * @throws IOException
	 *             ��� ���� �Ǵ� file ���� ����
	 * @throws IllegalStateException
	 *             Packet ���Ÿ�� �϶� �߻�
	 */
	protected boolean readFile() throws FileNotFoundException, IOException,
			IllegalStateException {
		boolean result = false;

		// Timeout Handler�� ����Ѵٸ� Timestamp�� update
		if (getTimeoutHandler() != null) {
			getTimeoutHandler().updateTimestamp(this);
		}

		if (!isReceiveMode()) {
			throw new IllegalStateException(ERR_MSG_2);
		}

		BufferedOutputStream bout = null;
		try {
			bout = new BufferedOutputStream(
					new FileOutputStream(fileName, true));

			// ���� buffer�� null�̸� ���� ����
			if (receiveBuffer == null) {
				receiveBuffer = ByteBuffer.allocate(getReceiveBufferLength());
			}

			// ������ ���̸� �˼� ���� == ���� ������ �������� �ʾҴ�
			if (incomingFileLength <= 0) {
				// ������ ������ �����Ѵٸ� ���� (����� �����ϰ��̴�)
				File prevFile = new File(fileName);
				if (prevFile.exists()) {
					// Stream�� �켱 �ݰ�
					bout.close();
					// ������ ����
					prevFile.delete();
					// Stream�� �ٽ� ����
					bout = new BufferedOutputStream(new FileOutputStream(
							fileName, true));
				}

				// ������ ���̸� header�� ���̷� set
				incomingFileLength = HEADER_LENGTH;
				receivingFileLength = true;

				// buffer�� HEADER_LENGTH��ŭ�� ����� �� �ֵ��� ���� == Overrun ����
				receiveBuffer.position(0);
				receiveBuffer.limit(HEADER_LENGTH);
			}

			// SocketChannel���� read
			int readLength = getSocketChannel().read(receiveBuffer);

			// ������ ������ ������쿡�� ó��
			if (readLength > 0) {
				// Timeout Handler�� ����Ѵٸ� Timestamp�� update
				if (getTimeoutHandler() != null) {
					getTimeoutHandler().updateTimestamp(this);
				}

				// ���� ��忡 ����...
				if (receivingFileLength) {
					// file ���� ����
					readFileLength();
				} else {
					// ������ ���̸� �ջ�
					receivedFileLength += readLength;
					// file ���� & ����
					// fout.write(readFileData());

					receiveBuffer.flip();
					byte[] data = new byte[readLength];
					receiveBuffer.get(data);
					// buffer�� reset�Ѵ�
					receiveBuffer.position(0);
					receiveBuffer.limit(receiveBuffer.capacity());
					bout.write(data);

					// �� ���������� ��ȯ���� true�� set
					// �� ū ����� �޾Ƽ��� �ȵ�����, �������� ���� == ��� >= �� ���
					if (receivedFileLength >= incomingFileLength) {
						result = true;
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (bout != null) {
				try {
					bout.flush();
				} catch (IOException ignored) {
				}
				try {
					bout.close();
				} catch (IOException ignored) {
				}
			}
		}

		return result;
	}

	/**
	 * file data�� ����
	 * 
	 * @return ������ byte[]
	 */
	protected byte[] readFileData() {
		receiveBuffer.flip();
		byte[] result = new byte[receiveBuffer.limit()];
		receiveBuffer.get(result);

		// buffer�� reset�Ѵ�
		receiveBuffer.position(0);
		receiveBuffer.limit(receiveBuffer.capacity());

		return result;
	}

	/**
	 * file ���̸� ����
	 */
	protected void readFileLength() {
		// ���� ������ �Ϸ�Ǹ�
		if (receiveBuffer.remaining() == 0) {
			receiveBuffer.flip();
			byte[] tempBytes = new byte[HEADER_LENGTH];
			receiveBuffer.get(tempBytes);
			// ������ file ���̸� set�ϰ�
			incomingFileLength = Long.parseLong(new String(tempBytes).trim());
			receivedFileLength = 0;
			// ���� ���� flag�� reset�ϰ�
			receivingFileLength = false;
			// buffer�� reset�Ѵ�
			receiveBuffer.position(0);
			receiveBuffer.limit(receiveBuffer.capacity());
		}
	}

	/**
	 * Returned receiveMode to Requester
	 * 
	 * @return receiveMode
	 */
	public boolean isReceiveMode() {
		return receiveMode;
	}

	/**
	 * Set receiveMode
	 * 
	 * @param receiveMode
	 *            receiveMode.
	 */
	public void setReceiveMode(boolean receiveMode) {
		this.receiveMode = receiveMode;
	}

	/**
	 * Returned incomingFileLength to Requester
	 * 
	 * @return incomingFileLength
	 */
	public long getIncomingFileLength() {
		return incomingFileLength;
	}

	/**
	 * Set incomingFileLength
	 * 
	 * @param incomingFileLength
	 *            incomingFileLength.
	 */
	public void setIncomingFileLength(long incomingFileLength) {
		this.incomingFileLength = incomingFileLength;
	}

	/**
	 * Returned receivedFileLength to Requester
	 * 
	 * @return receivedFileLength
	 */
	public long getReceivedFileLength() {
		return receivedFileLength;
	}

	/**
	 * Set receivedFileLength
	 * 
	 * @param receivedFileLength
	 *            receivedFileLength.
	 */
	public void setReceivedFileLength(long receivedFileLength) {
		this.receivedFileLength = receivedFileLength;
	}

	/**
	 * Returned receiveFileFinished to Requester
	 * 
	 * @return receiveFileFinished
	 */
	public boolean isReceiveFileFinished() {
		return receiveFileFinished;
	}

	/**
	 * Set receiveFileFinished
	 * 
	 * @param receiveFileFinished
	 *            receiveFileFinished.
	 */
	public void setReceiveFileFinished(boolean receiveFileFinished) {
		this.receiveFileFinished = receiveFileFinished;
	}

	/**
	 * Returned fileName to Requester
	 * 
	 * @return fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Set fileName
	 * 
	 * @param fileName
	 *            fileName.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}