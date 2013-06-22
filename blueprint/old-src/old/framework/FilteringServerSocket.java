package lempel.old.framework;

import java.io.*;
import java.nio.channels.*;
import java.net.*;

import lempel.blueprint.util.AddressMap;

/**
 * IP���� ���������� ������ �� �ִ� ServerSocket <br>
 * <br>
 * Allow��å�� Deny��å �� �Ѱ����� ����ؾ� �ϸ� ���ÿ� ����� ��� Deny��å�� ����ȴ� <br>
 * setAllowList(AddressMap), setDenyList(AddresMap)�� ����Ͽ� Allow/Deny �� ip���� �����Ѵ�
 * <br>
 * 
 * @author Sang-min Lee
 * @since 2004.5.17.
 * @version 2005.5.20.
 */
public class FilteringServerSocket extends ServerSocket {
	/** ������ ���� IP list */
	protected AddressMap _allowList = null;

	/** ������ ������ IP list */
	protected AddressMap _denyList = null;

	/**
	 * ��� ����!!
	 * 
	 * @throws IOException
	 *             ������ �߻�
	 */
	public FilteringServerSocket() throws IOException {
		throw new IOException("don't use this method");
	}

	/**
	 * XMLNode�� ��� ������ ���������� �����Ѵ� <br>
	 * denyList�� allowList�� �ϳ��� ���Ǹ� denyList�� �켱���� ���´� <br>
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
	 *             node�� ��������� �������� ����
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

		// deny/allow list ����
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
	 *            Bind�� port
	 * @throws IOException
	 *             Bind ����
	 */
	public FilteringServerSocket(int port) throws IOException {
		this(port, 50, (InetAddress) null);
	}

	/**
	 * @param port
	 *            Bind�� port
	 * @param backlog
	 *            Backlog
	 * @throws IOException
	 *             Bind ����
	 */
	public FilteringServerSocket(int port, int backlog) throws IOException {
		this(port, backlog, (InetAddress) null);
	}

	/**
	 * @param port
	 *            Bind�� port
	 * @param backlog
	 *            Backlog
	 * @param bindAddr
	 *            Bind�� address
	 * @throws IOException
	 *             Bind ����
	 */
	public FilteringServerSocket(int port, int backlog, InetAddress bindAddr)
			throws IOException {
		super(port, backlog, bindAddr);

		_allowList = new AddressMap();
		_denyList = new AddressMap();
	}

	/**
	 * @param port
	 *            Bind�� port
	 * @param backlog
	 *            Backlog
	 * @param bindAddr
	 *            Bind�� address
	 * @throws IOException
	 *             Bind ����
	 */
	public FilteringServerSocket(int port, int backlog, String bindAddr)
			throws IOException {
		this(port, backlog, InetAddress.getByName(bindAddr));
	}

	/**
	 * ��� ����!! ���� �ȵ�
	 * 
	 * @throws NullPointerException
	 *             �׻�
	 */
	public ServerSocketChannel getChannel() {
		throw new NullPointerException("getChannel() is not implemented yet");
	}

	/**
	 * ������ ����ϴ� IP����� ����
	 * 
	 * @param allowList
	 *            ���� IP�� ���
	 */
	public synchronized void setAllowList(AddressMap allowList) {
		_allowList = allowList;
	}

	/**
	 * ������ �����ϴ� IP����� ����
	 * 
	 * @param denyList
	 *            ������ IP�� ���
	 */
	public synchronized void setDenyList(AddressMap denyList) {
		_denyList = denyList;
	}

	/**
	 * ������ ����ϴ� IP����� ����
	 * 
	 * @return ���� IP�� ���
	 */
	public AddressMap getAllowList() {
		return _allowList;
	}

	/**
	 * ������ �����ϴ� IP����� ����
	 * 
	 * @return ������ IP�� ���
	 */
	public AddressMap getDenyList() {
		return _denyList;
	}

	/**
	 * Client�� ������ �����Ѵ�
	 * 
	 * @return ����� socket
	 * @throws IOException
	 *             Accept ����
	 */
	public Socket accept() throws IOException {
		Socket socket = super.accept();

		String hostAddr = socket.getInetAddress().getHostAddress();

		// deny ��å ����
		if (!_denyList.isEmpty()) {
			if (_denyList.containsKey(hostAddr)) {
				try {
					socket.close();
				} catch (Exception ignored) {
				}

				socket = null;
			}
		}
		// allow ��å ����
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