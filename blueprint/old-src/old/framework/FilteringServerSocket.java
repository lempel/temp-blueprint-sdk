package lempel.old.framework;

import java.io.*;
import java.nio.channels.*;
import java.net.*;

import lempel.blueprint.util.AddressMap;

/**
 * IP별로 접근제한을 설정할 수 있는 ServerSocket <br>
 * <br>
 * Allow정책과 Deny정책 중 한가지만 사용해야 하며 동시에 선언된 경우 Deny정책이 적용된다 <br>
 * setAllowList(AddressMap), setDenyList(AddresMap)을 사용하여 Allow/Deny 할 ip들을 설정한다
 * <br>
 * 
 * @author Sang-min Lee
 * @since 2004.5.17.
 * @version 2005.5.20.
 */
public class FilteringServerSocket extends ServerSocket {
	/** 접속이 허용된 IP list */
	protected AddressMap _allowList = null;

	/** 접속이 금지된 IP list */
	protected AddressMap _denyList = null;

	/**
	 * 사용 금지!!
	 * 
	 * @throws IOException
	 *             무조건 발생
	 */
	public FilteringServerSocket() throws IOException {
		throw new IOException("don't use this method");
	}

	/**
	 * XMLNode에 담긴 정보로 서버소켓을 생성한다 <br>
	 * denyList와 allowList중 하나만 사용되며 denyList가 우선권을 갖는다 <br>
	 * <br>
	 * &lt;...&gt; <br>
	 * &lt;bindPort&gt;80&lt;/bindPort&gt; <br>
	 * &lt;backlog&gt;5&lt;/backlog&gt; <br>
	 * &lt;bindIp&gt;127.0.0.1&lt;/bindIp&gt; <br>
	 * &lt;denyList&gt;192.168.1.* 203.*.*.* 203.248.46.1&lt;/denyList&gt; <br>
	 * &lt;allowList&gt;192.168.1.* 203.*.*.* 203.248.46.1&lt;/allowList&gt;
	 * <br>
	 * &lt;/...&gt; <br>
	 * 
	 * @param node
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             node에 담긴정보가 적절하지 못함
	 */
	public static FilteringServerSocket create(XMLNode node)
			throws IOException, IllegalArgumentException {
		FilteringServerSocket sock = null;
		int port = Integer.parseInt(node.getFirstChild("bindPort").getValue());
		int backlog = 5;
		String ip = null;
		if (node.hasChild("backlog"))
			backlog = Integer
					.parseInt(node.getFirstChild("backlog").getValue());
		if (node.hasChild("bindIp"))
			ip = node.getFirstChild("bindIp").getValue();

		if (ip != null)
			sock = new FilteringServerSocket(port, backlog, InetAddress
					.getByName(ip));
		else
			sock = new FilteringServerSocket(port, backlog);

		// deny/allow list 설정
		if (node.hasChild("denyList"))
			sock.setDenyList(new AddressMap(node.getFirstChild("denyList")
					.getValue()));
		if (node.hasChild("allowList"))
			sock.setAllowList(new AddressMap(node.getFirstChild("allowList")
					.getValue()));

		return sock;
	}

	/**
	 * @param port
	 *            Bind할 port
	 * @throws IOException
	 *             Bind 실패
	 */
	public FilteringServerSocket(int port) throws IOException {
		this(port, 50, (InetAddress) null);
	}

	/**
	 * @param port
	 *            Bind할 port
	 * @param backlog
	 *            Backlog
	 * @throws IOException
	 *             Bind 실패
	 */
	public FilteringServerSocket(int port, int backlog) throws IOException {
		this(port, backlog, (InetAddress) null);
	}

	/**
	 * @param port
	 *            Bind할 port
	 * @param backlog
	 *            Backlog
	 * @param bindAddr
	 *            Bind할 address
	 * @throws IOException
	 *             Bind 실패
	 */
	public FilteringServerSocket(int port, int backlog, InetAddress bindAddr)
			throws IOException {
		super(port, backlog, bindAddr);

		_allowList = new AddressMap();
		_denyList = new AddressMap();
	}

	/**
	 * @param port
	 *            Bind할 port
	 * @param backlog
	 *            Backlog
	 * @param bindAddr
	 *            Bind할 address
	 * @throws IOException
	 *             Bind 실패
	 */
	public FilteringServerSocket(int port, int backlog, String bindAddr)
			throws IOException {
		this(port, backlog, InetAddress.getByName(bindAddr));
	}

	/**
	 * 사용 금지!! 구현 안됨
	 * 
	 * @throws NullPointerException
	 *             항상
	 */
	public ServerSocketChannel getChannel() {
		throw new NullPointerException("getChannel() is not implemented yet");
	}

	/**
	 * 접속을 허용하는 IP목록을 설정
	 * 
	 * @param allowList
	 *            허용된 IP의 목록
	 */
	public synchronized void setAllowList(AddressMap allowList) {
		_allowList = allowList;
	}

	/**
	 * 접속을 금지하는 IP목록을 설정
	 * 
	 * @param denyList
	 *            금지된 IP의 목록
	 */
	public synchronized void setDenyList(AddressMap denyList) {
		_denyList = denyList;
	}

	/**
	 * 접속을 허용하는 IP목록을 얻어낸다
	 * 
	 * @return 허용된 IP의 목록
	 */
	public AddressMap getAllowList() {
		return _allowList;
	}

	/**
	 * 접속을 금지하는 IP목록을 얻어낸다
	 * 
	 * @return 금지된 IP의 목록
	 */
	public AddressMap getDenyList() {
		return _denyList;
	}

	/**
	 * Client의 연결을 수락한다
	 * 
	 * @return 연결된 socket
	 * @throws IOException
	 *             Accept 실패
	 */
	public Socket accept() throws IOException {
		Socket socket = super.accept();

		String hostAddr = socket.getInetAddress().getHostAddress();

		// deny 정책 적용
		if (!_denyList.isEmpty()) {
			if (_denyList.containsKey(hostAddr)) {
				try {
					socket.close();
				} catch (Exception ignored) {
				}

				socket = null;
			}
		}
		// allow 정책 적용
		else if (!_allowList.isEmpty()) {
			if (!_allowList.containsKey(hostAddr)) {
				try {
					socket.close();
				} catch (Exception ignored) {
				}

				socket = null;
			}
		}

		return socket;
	}
}