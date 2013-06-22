package lempel.blueprint.service.socks;

/**
 * Client Request Packet<br>
 * 특성상 ClientRequest는 2번에 나누어 읽는다<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 29
 * @since 1.5
 * @last $Date$
 * @see
 */
public class ServerResponse {
	private byte ver = 0x05;
	/** connect only (0x01) */
	private byte rep = 0x01;
	/** reserved */
	private byte rsv = 0x00;
	/** IP v6 not supported (0x04) */
	private byte atyp = 0x01;
	private byte[] addr = new byte[4];
	private byte[] port = new byte[2];

	/**
	 * 내부 변수를 serialize하여 byte[]로 반환
	 * 
	 * @return serizlize된 byte[]
	 */
	public byte[] serialize() {
		byte[] result = new byte[6 + addr.length];
		result[0] = ver;
		result[1] = rep;
		result[2] = rsv;
		result[3] = atyp;
		System.arraycopy(addr, 0, result, 4, addr.length);
		System.arraycopy(port, 0, result, result.length - 2, port.length);

		return result;
	}

	/**
	 * Returned ver to Requester
	 * 
	 * @return ver
	 */
	public byte getVer() {
		return ver;
	}

	/**
	 * Set ver
	 * 
	 * @param ver
	 *            ver.
	 */
	public void setVer(byte ver) {
		this.ver = ver;
	}

	/**
	 * Returned rep to Requester
	 * 
	 * @return rep
	 */
	public byte getRep() {
		return rep;
	}

	/**
	 * Set rep
	 * 
	 * @param rep
	 *            rep.
	 */
	public void setRep(byte rep) {
		this.rep = rep;
	}

	/**
	 * Returned rsv to Requester
	 * 
	 * @return rsv
	 */
	public byte getRsv() {
		return rsv;
	}

	/**
	 * Set rsv
	 * 
	 * @param rsv
	 *            rsv.
	 */
	public void setRsv(byte rsv) {
		this.rsv = rsv;
	}

	/**
	 * Returned atyp to Requester
	 * 
	 * @return atyp
	 */
	public byte getAtyp() {
		return atyp;
	}

	/**
	 * Set atyp
	 * 
	 * @param atyp
	 *            atyp.
	 */
	public void setAtyp(byte atyp) {
		this.atyp = atyp;
	}

	/**
	 * Returned addr to Requester
	 * 
	 * @return addr
	 */
	public byte[] getAddr() {
		return addr;
	}

	/**
	 * Set addr
	 * 
	 * @param addr
	 *            addr.
	 */
	public void setAddr(byte[] addr) {
		this.addr = addr;
	}

	/**
	 * Returned port to Requester
	 * 
	 * @return port
	 */
	public byte[] getPort() {
		return port;
	}

	/**
	 * Set port
	 * 
	 * @param port
	 *            port.
	 */
	public void setPort(byte[] port) {
		this.port = port;
	}
}
