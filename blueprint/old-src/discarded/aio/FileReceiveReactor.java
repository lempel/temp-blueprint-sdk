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
 * File 수신이 기능이 추가된 Serializable Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 30
 * @since 1.5
 * @last $Date$
 * @see
 */
public class FileReceiveReactor extends SerializableReactor {
	/** String 상수 : "can't read file during packet mode" */
	private static final String ERR_MSG_2 = "can't read file during packet mode";
	/** String 상수 : "can't send object during file mode" */
	private static final String ERR_MSG_1 = "can't send object during file mode";
	/** String 상수 : "can't receive object during file mode" */
	private static final String ERR_MSG_0 = "can't receive object during file mode";
	/** String 상수 : ".zip" */
	private static final String FILE_NAME_0 = ".zip";
	/** String 상수 : "LOG_PATH" */
	private static final String FIELD_NAME_0 = "LOG_PATH";
	/** String 상수 : "file.separator" */
	private static final String FILE_SEPARATOR = "file.separator";

	/** 수신할 file 크기 */
	private long incomingFileLength = 0;
	/** 수신한 file 크기 */
	private long receivedFileLength = 0;

	/** 수신 mode (true: 파일 수신, false: Packet 수신, deault: false) */
	private boolean receiveMode = false;
	/** file 길이 수신중 여부 (true: file 길이 수신중) */
	private boolean receivingFileLength = true;
	/** 파일 수신 완료 여부 (true: 완료) */
	private boolean receiveFileFinished = false;

	/** 수신되는 file을 저장할 이름 (path를 포함) */
	private String fileName = null;

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public FileReceiveReactor(SocketChannel socketChannel) throws IOException {
		super(socketChannel);

		// 수신 buffer를 100kb로 set
		// $ANALYSIS-IGNORE
		setReceiveBufferLength(102400);

		GlobalContext context = GlobalContext.getInstance();
		// context에서 LOG_PATH를 가져온다
		String sep = System.getProperty(FILE_SEPARATOR);
		String filePath = (String) context.get(FIELD_NAME_0);
		if (!filePath.endsWith(sep)) {
			filePath += sep;
		}
		// file 이름은 agent의 address로 설정
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

		// 수신에 실패한 파일은 삭제
		if (isReceiveMode() && !isReceiveFileFinished()) {
			deleteFile();
		}
	}

	/**
	 * 현재 저장중인 파일을 삭제
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
	 * file을 수신<br>
	 * fd 절약을 위해 그때그때 file을 열었다 저장하고 닫는다<br>
	 * 
	 * @return true: 수신 완료
	 * @throws FileNotFoundException
	 *             저장할 file명이 지정되지 않았거나 잘못된 경우 발생
	 * @throws IOException
	 *             통신 오류 또는 file 저장 오류
	 * @throws IllegalStateException
	 *             Packet 수신모드 일때 발생
	 */
	protected boolean readFile() throws FileNotFoundException, IOException,
			IllegalStateException {
		boolean result = false;

		// Timeout Handler를 사용한다면 Timestamp를 update
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

			// 수신 buffer가 null이면 새로 생성
			if (receiveBuffer == null) {
				receiveBuffer = ByteBuffer.allocate(getReceiveBufferLength());
			}

			// 수신할 길이를 알수 없다 == 길이 정보를 수신하지 않았다
			if (incomingFileLength <= 0) {
				// 기존에 파일이 존재한다면 삭제 (재수신 상태일것이다)
				File prevFile = new File(fileName);
				if (prevFile.exists()) {
					// Stream을 우선 닫고
					bout.close();
					// 삭제한 다음
					prevFile.delete();
					// Stream을 다시 생성
					bout = new BufferedOutputStream(new FileOutputStream(
							fileName, true));
				}

				// 수신할 길이를 header의 길이로 set
				incomingFileLength = HEADER_LENGTH;
				receivingFileLength = true;

				// buffer를 HEADER_LENGTH만큼만 사용할 수 있도록 설정 == Overrun 방지
				receiveBuffer.position(0);
				receiveBuffer.limit(HEADER_LENGTH);
			}

			// SocketChannel에서 read
			int readLength = getSocketChannel().read(receiveBuffer);

			// 실제로 읽은게 있을경우에만 처리
			if (readLength > 0) {
				// Timeout Handler를 사용한다면 Timestamp를 update
				if (getTimeoutHandler() != null) {
					getTimeoutHandler().updateTimestamp(this);
				}

				// 수신 모드에 따라서...
				if (receivingFileLength) {
					// file 길이 수신
					readFileLength();
				} else {
					// 수신한 길이를 합산
					receivedFileLength += readLength;
					// file 수신 & 저장
					// fout.write(readFileData());

					receiveBuffer.flip();
					byte[] data = new byte[readLength];
					receiveBuffer.get(data);
					// buffer를 reset한다
					receiveBuffer.position(0);
					receiveBuffer.limit(receiveBuffer.capacity());
					bout.write(data);

					// 다 수신했으면 반환값을 true로 set
					// 더 큰 사이즈를 받아서는 안돼지만, 안정성을 위해 == 대신 >= 를 사용
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
	 * file data를 수신
	 * 
	 * @return 수신한 byte[]
	 */
	protected byte[] readFileData() {
		receiveBuffer.flip();
		byte[] result = new byte[receiveBuffer.limit()];
		receiveBuffer.get(result);

		// buffer를 reset한다
		receiveBuffer.position(0);
		receiveBuffer.limit(receiveBuffer.capacity());

		return result;
	}

	/**
	 * file 길이를 수신
	 */
	protected void readFileLength() {
		// 길이 수신이 완료되면
		if (receiveBuffer.remaining() == 0) {
			receiveBuffer.flip();
			byte[] tempBytes = new byte[HEADER_LENGTH];
			receiveBuffer.get(tempBytes);
			// 수신할 file 길이를 set하고
			incomingFileLength = Long.parseLong(new String(tempBytes).trim());
			receivedFileLength = 0;
			// 길이 수신 flag를 reset하고
			receivingFileLength = false;
			// buffer를 reset한다
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