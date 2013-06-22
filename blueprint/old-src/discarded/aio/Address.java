package lempel.blueprint.aio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * 주소 정보 (IP/Port)
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 07. 26
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Address implements Serializable {
	private static final long serialVersionUID = -3741969404373228243L;

	/** ip */
	private String ip = null;

	/** port */
	private int port = 0;

	/**
	 * Constructor
	 * 
	 * @param ip
	 * @param port
	 */
	public Address(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/**
	 * Returned ip to Requester
	 * 
	 * @return ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Set ip
	 * 
	 * @param ip
	 *            ip.
	 */
	public void setIp(String ip) {
		this.ip = ip;
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
	 * Set port
	 * 
	 * @param port
	 *            port.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[IP=" + ip + ", PORT=" + port + "]";
	}

	/**
	 * 연이어 표기한 IP/Port 정보를 분석
	 * 
	 * @param data
	 * @return
	 */
	public static Address[] parse(String data) {
		Address[] result = null;

		StringTokenizer st = new StringTokenizer(data, ";");
		int tokenCount = st.countTokens();
		if (tokenCount == 0) {
			// token이 0개면 종료
			return result;
		}

		ArrayList<Address> addresses = new ArrayList<Address>(tokenCount);
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(token, ":");
			String ip = st2.nextToken();
			int port = Integer.parseInt(st2.nextToken());
			Address address = new Address(ip, port);
			addresses.add(address);
		}

		result = new Address[addresses.size()];
		addresses.toArray(result);

		return result;
	}
}