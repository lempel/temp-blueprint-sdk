package lempel.old.framework;


import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import lempel.blueprint.log.LogLevel;
import lempel.blueprint.log.Logger;

/**
 * ServerSocket의 연결을 수락하고, 각종 Resource의 초기화를 하는 framework
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2005.5.20.
 */
public abstract class Acceptor extends Thread {
	private static final String CONSTANT_1 = "Can't init. Resources"; //$NON-NLS-1$

	/** log 출력용 헤더 */
	protected String _header = "Acceptor: ";

	/** 환경 변수 */
	protected PropertyManager _prop = null;

	/** XML 환경 변수 */
	protected XMLDocument _doc = null;

	/** logger */
	protected Logger _log = null;

	/** Thread의 지속 여부 flag */
	protected boolean _runFlag = false;

	/** Resource Class의 구현들을 관리 */
	protected ResourceManager _resources = null;

	/** Worker Class의 구현들을 관리 */
	protected WorkerManager _workers = null;

	/** 작업 queue */
	protected Vector _jobQueue = null;

	/**
	 * @param propFileName
	 *            property 파일 이름
	 * @throws IOException
	 *             property 파일을 읽을 수 없는 경우
	 */
	public Acceptor(String propFileName) throws IOException {
		this(new PropertyManager(propFileName));
		initialize();
	}

	/**
	 * @param prop
	 *            환경 변수
	 */
	public Acceptor(PropertyManager prop) {
		_prop = prop;
		_log = new Logger(prop);
		_jobQueue = new Vector();
	}

	/**
	 * @param xprop
	 *            XML 환경 변수
	 */
	public Acceptor(XMLDocument xprop) {
		_doc = xprop;
		_log = Logger.getInstance(xprop);
		_jobQueue = new Vector();
	}

	/**
	 * Resource들의 초기화를 구현
	 * 
	 * @return 초기화 성공 여부
	 */
	protected abstract boolean initResources();

	/**
	 * Worker들의 초기화를 구현
	 * 
	 * @return 초기화 성공 여부
	 */
	protected abstract boolean initWorkers();

	/**
	 * 서버 소켓의 초기화를 구현
	 * 
	 * @return 초기화 성공 여부
	 */
	protected abstract boolean initServerSocket();

	/**
	 * ServerSocket에서 Accept하는 과정에 필요한 작업을 구현
	 * 
	 * @return accept된 Socket
	 * @throws IOException
	 */
	protected abstract Socket accept() throws IOException;

	/**
	 * Acceptor를 초기화 <br>
	 * initResources() -> initWorkers() -> initServerSocket() 순으로 초기화 <br>
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
					// 작업 queue에 추가
					_jobQueue.addElement(_sock);
					// worker 하나를 깨운다
					_workers.notifyWorker();
				}
			} catch (Throwable ex) {
				_log.println(LogLevel.WAN, "Can't accept client - " + ex);
				ex.printStackTrace();
			}
		}
	}
}