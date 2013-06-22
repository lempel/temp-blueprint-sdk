package lempel.blueprint.service.socks;

import java.nio.channels.Selector;

/**
 * Connector���� target�� ������ �����ϴµ� ����ϴ� bean
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 12. 12
 * @since 1.5
 * @last $Date$
 * @see
 */
public class ConnectionInfo {
	/** Relay�� Reactor */
	private Socks5RelayReactor relayReactor = null;

	/** ������ �ּ� */
	private String address = null;

	/** ������ port */
	private int port = 0;

	/** ���ῡ ����� Selector */
	private Selector selector = null;

	/**
	 * Constructor
	 * 
	 * @param relayReactor
	 * @param address
	 * @param port
	 * @param selector
	 */
	public ConnectionInfo(Socks5RelayReactor relayReactor, String address,
			int port, Selector selector) {
		this.relayReactor = relayReactor;
		this.address = address;
		this.port = port;
		this.selector = selector;
	}

	/**
	 * Returned address to Requester
	 * 
	 * @return address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Returned port to Requester
	 * 
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returned relayReactor to Requester
	 * 
	 * @return relayReactor
	 */
	public Socks5RelayReactor getRelayReactor() {
		return relayReactor;
	}

	/**
	 * Returned selector to Requester
	 * 
	 * @return selector
	 */
	public Selector getSelector() {
		return selector;
	}
}