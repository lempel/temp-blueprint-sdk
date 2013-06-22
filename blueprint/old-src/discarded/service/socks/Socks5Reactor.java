package lempel.blueprint.service.socks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import lempel.blueprint.aio.Reactor;
import lempel.blueprint.config.XmlConfig;
import lempel.blueprint.util.GlobalContext;

/**
 * SOCKS version 5 용 Reactor
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 29
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Socks5Reactor extends Reactor {
	/** 연결이 완료된 상태에서의 수신 버퍼 크기 */
	private static final int BUFFER_LENGTH = 2048;

	/** partner의 연결을 처리하는 connector */
	private Connector connector = null;

	/** 반대쪽 reactor (Socks5 connection은 쌍으로 이루어진다) */
	private Socks5RelayReactor partner = null;

	/** Configuration (인증 정보를 사용하기 위해) */
	private XmlConfig config = null;

	/**
	 * negotiation 상태<br>
	 * 0: 연결 완료<br>
	 * 1: waiting for negotiation<br>
	 * 2: reading negotiation<br>
	 * 3: waiting for subnegotiation<br>
	 * 4: reading subnegotiatoin uname<br>
	 * 5: reading subnegotiation passwd<br>
	 * 6: waiting for request<br>
	 * 7: reading request<br>
	 */
	private int negotiation = 1;
	/** client의 요청 version */
	private byte clientVersion = 0x00;
	/** client의 인증 요청 uname */
	private byte[] clientUname = null;
	/** client가 요청한 연결의 type */
	private byte clientRequestType = 0x00;
	/** client의 요청 type에 따라 나중에 다르게 사용되는 1byte를 보관 */
	private byte clientRequestTemp = 0x00;
	/** server의 마지막 응답 packet */
	private ServerResponse serverResPacket = new ServerResponse();

	/**
	 * Constructor
	 * 
	 * @param socketChannel
	 * @throws IOException
	 */
	public Socks5Reactor(SocketChannel socketChannel) throws IOException {
		super(socketChannel);
		GlobalContext gctx = GlobalContext.getInstance();
		config = (XmlConfig) gctx.get(XmlConfig.class.getName());
		connector = (Connector) gctx.get(Connector.class.getName());
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

			if (negotiation == 0) {
				receiveBuffer.limit(receiveBuffer.capacity());
			} else if (negotiation == 1) {
				receiveBuffer.limit(2);
			}
		}

		// SocketChannel에서 read
		int readLength = getSocketChannel().read(receiveBuffer);

		// 실제로 읽은게 있을경우에만 처리
		if (readLength > 0) {
			// 연결 완료 상태와 negotiation 상태는 처리가 다르다
			if (negotiation == 0) {
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
			} else if (receiveBuffer.position() == receiveBuffer.limit()) {
				receiveBuffer.flip();
				byte[] readData = new byte[receiveBuffer.limit()];
				receiveBuffer.get(readData);

				if (negotiation == 1) {
					clientVersion = readData[0];
					incomingLength = readData[1];

					negotiation = 2;

					// 수신 buffer 초기화
					receiveBuffer.position(0);
					receiveBuffer.limit(incomingLength);
				} else if (negotiation == 2) {
					boolean doAuth = config.getBoolean("/socks/auth/@doAuth");
					byte check = doAuth ? (byte) 0x02 : (byte) 0x00;

					boolean supported = false;
					if (clientVersion == 0x05) {
						int methodCount = readData.length;
						for (int i = 0; i < methodCount; i++) {
							if (readData[i] == check) {
								supported = true;
							}
						}
					}

					if (supported) {
						sendRawData(new byte[] { 0x05, (byte) check });
					} else {
						sendRawData(new byte[] { 0x05, (byte) 0xFF });
						// 지원되지 않는 client는 종료
						terminate();
					}

					if (doAuth) {
						negotiation = 3;

						// 수신 buffer 초기화
						receiveBuffer.position(0);
						receiveBuffer.limit(2);
					} else {
						negotiation = 6;

						// 수신 buffer 초기화
						receiveBuffer.position(0);
						receiveBuffer.limit(5);
					}
				} else if (negotiation == 3) {
					// uname + plen
					incomingLength = readData[1] + 1;

					negotiation = 4;

					// 수신 buffer 초기화
					receiveBuffer.position(0);
					receiveBuffer.limit(incomingLength);
				} else if (negotiation == 4) {
					clientUname = new byte[readData.length - 1];
					System.arraycopy(readData, 0, clientUname, 0,
							clientUname.length);

					incomingLength = readData[readData.length - 1];

					negotiation = 5;

					// 수신 buffer 초기화
					receiveBuffer.position(0);
					receiveBuffer.limit(incomingLength);
				} else if (negotiation == 5) {
					String clientPasswd = new String(readData);
					String passwd = config
							.getString("/socks/auth/user[@uname='"
									+ clientUname + "']/@passwd");
					if (passwd != null && passwd.equals(clientPasswd)) {
						sendRawData(new byte[] { 0x05, 0x00 });
					} else {
						sendRawData(new byte[] { 0x05, (byte) 0xFF });
						// 인증에 실패한 client는 종료
						terminate();
					}

					negotiation = 6;

					// 수신 buffer 초기화
					receiveBuffer.position(0);
					receiveBuffer.limit(5);
				} else if (negotiation == 6) {
					clientRequestType = readData[3];
					clientRequestTemp = readData[4];
					if (clientRequestType == 0x01) {
						// IP v4
						incomingLength = 5;
					} else if (clientRequestType == 0x03) {
						// domain name
						incomingLength = clientRequestTemp + 2;
					} else {
						// IP v6는 현재 지원하지 않음
						terminate();
					}

					negotiation = 7;

					// 수신 buffer 초기화
					receiveBuffer.position(0);
					receiveBuffer.limit(incomingLength);
				} else if (negotiation == 7) {
					byte[] addr = null;
					String address = null;
					if (clientRequestType == 0x01) {
						addr = new byte[4];
						addr[0] = clientRequestTemp;
						System.arraycopy(readData, 0, addr, 1, 3);
						address = Integer.toString(0x00ff & addr[0]) + "."
								+ Integer.toString(0x00ff & addr[1]) + "."
								+ Integer.toString(0x00ff & addr[2]) + "."
								+ Integer.toString(0x00ff & addr[3]);
					} else if (clientRequestType == 0x03) {
						addr = new byte[clientRequestTemp + 1];
						addr[0] = clientRequestTemp;
						System.arraycopy(readData, 0, addr, 1, addr.length);
						address = new String(addr, 1, addr.length);
					}
					byte[] port = new byte[2];
					port[0] = readData[readData.length - 2];
					port[1] = readData[readData.length - 1];

					serverResPacket.setAtyp(clientRequestType);
					serverResPacket.setAddr(addr);
					serverResPacket.setPort(port);

					SocketChannel channel = SocketChannel.open();
					partner = new Socks5RelayReactor(channel);
					partner.setBlocking(false);
					partner.setPartner(this);
					connector.register(partner, address, port[0] * 256
							+ port[1], getSelector());
					// connector에서 접속 성공 여부에 따라 응답 전송

					negotiation = 0;

					// 수신 buffer 초기화
					receiveBuffer.position(0);
					receiveBuffer.limit(receiveBuffer.capacity());
				}
			}
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
	public Socks5RelayReactor getPartner() {
		return partner;
	}

	/**
	 * Set partner
	 * 
	 * @param partner
	 *            partner.
	 */
	public void setPartner(Socks5RelayReactor partner) {
		this.partner = partner;
	}

	/**
	 * Set negotiation
	 * 
	 * @param negotiation
	 *            negotiation.
	 */
	public void setNegotiation(int negotiation) {
		this.negotiation = negotiation;
	}

	/**
	 * Returned serverResPacket to Requester
	 * 
	 * @return serverResPacket
	 */
	public ServerResponse getServerResponsePacket() {
		return serverResPacket;
	}
}
