package lempel.old.framework;


import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import lempel.blueprint.log.LogLevel;
import lempel.blueprint.log.Logger;

/**
 * ServerSocket�� ������ �����ϰ�, ���� Resource�� �ʱ�ȭ�� �ϴ� framework
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2005.5.20.
 */
public abstract class Acceptor extends Thread {
	private static final String CONSTANT_1 = "Can't init. Resources"; //$NON-NLS-1$

	/** log ��¿� ��� */
	protected String _header = "Acceptor: ";

	/** ȯ�� ���� */
	protected PropertyManager _prop = null;

	/** XML ȯ�� ���� */
	protected XMLDocument _doc = null;

	/** logger */
	protected Logger _log = null;

	/** Thread�� ���� ���� flag */
	protected boolean _runFlag = false;

	/** Resource Class�� �������� ���� */
	protected ResourceManager _resources = null;

	/** Worker Class�� �������� ���� */
	protected WorkerManager _workers = null;

	/** �۾� queue */
	protected Vector _jobQueue = null;

	/**
	 * @param propFileName
	 *            property ���� �̸�
	 * @throws IOException
	 *             property ������ ���� �� ���� ���
	 */
	public Acceptor(String propFileName) throws IOException {
		this(new PropertyManager(propFileName));
		initialize();
	}

	/**
	 * @param prop
	 *            ȯ�� ����
	 */
	public Acceptor(PropertyManager prop) {
		_prop = prop;
		_log = new Logger(prop);
		_jobQueue = new Vector();
	}

	/**
	 * @param xprop
	 *            XML ȯ�� ����
	 */
	public Acceptor(XMLDocument xprop) {
		_doc = xprop;
		_log = Logger.getInstance(xprop);
		_jobQueue = new Vector();
	}

	/**
	 * Resource���� �ʱ�ȭ�� ����
	 * 
	 * @return �ʱ�ȭ ���� ����
	 */
	protected abstract boolean initResources();

	/**
	 * Worker���� �ʱ�ȭ�� ����
	 * 
	 * @return �ʱ�ȭ ���� ����
	 */
	protected abstract boolean initWorkers();

	/**
	 * ���� ������ �ʱ�ȭ�� ����
	 * 
	 * @return �ʱ�ȭ ���� ����
	 */
	protected abstract boolean initServerSocket();

	/**
	 * ServerSocket���� Accept�ϴ� ������ �ʿ��� �۾��� ����
	 * 
	 * @return accept�� Socket
	 * @throws IOException
	 */
	protected abstract Socket accept() throws IOException;

	/**
	 * Acceptor�� �ʱ�ȭ <br>
	 * initResources() -> initWorkers() -> initServerSocket() ������ �ʱ�ȭ <br>
	 */
	protected void initialize() {
		_runFlag = true;

		if (!initResources()) {
			_log.println(LogLevel.ERR, _header + CONSTANT_1);
			_runFlag = false;
		}

		if (!initWorkers()) {
			_log.println(LogLevel.ERR, _header + "Can't init. Workers");
			_runFlag = false;
		}

		if (!initServerSocket()) {
			_log.println(LogLevel.ERR, _header + "Can't bind ServerSocket");
			_runFlag = false;
		}
	}

	public void run() {
		Socket _sock = null;
		while (_runFlag) {
			try {
				_sock = accept();

				if (_sock != null) {
					// �۾� queue�� �߰�
					_jobQueue.addElement(_sock);
					// worker �ϳ��� �����
					_workers.notifyWorker();
				}
			} catch (Throwable ex) {
				_log.println(LogLevel.WAN, "Can't accept client - " + ex);
				ex.printStackTrace();
			}
		}
	}
}