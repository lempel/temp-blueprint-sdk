package lempel.old.framework;


import java.util.*;

import lempel.blueprint.log.*;

/**
 * Client의 요청을 처리하는 Thread의 framework (Worker Thread Pattern과 유사) <br>
 * J2SE 5의 Executor와 동일한 기능이므로 교체 가능
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2004.11.2.
 */
public abstract class Worker extends Thread {
	/** log 출력용 헤더 */
	protected String _header = "Worker: ";

	/** logger */
	protected Logger _log = null;

	/** Worker들을 관리하는 Manager */
	protected WorkerManager _manager = null;

	/** 작업 queue */
	protected Vector _jobQueue = null;

	/** 환경 변수 */
	protected PropertyManager _prop = null;

	/** Resource객체들을 관리하는 Manager */
	protected ResourceManager _resources = null;

	/** Thread 지속 여부 flag */
	protected boolean _runFlag = false;

	/** XML 환경 변수 */
	protected XMLNode _node = null;

	/**
	 * @param manager
	 *            Worker를 관리하는 Manager
	 * @param jobQueue
	 *            작업 queue
	 * @param prop
	 *            환경 변수
	 * @param resources
	 *            Resource 객체를 관리하는 Manager
	 */
	public Worker(WorkerManager manager, Vector jobQueue, PropertyManager prop,
			ResourceManager resources) {
		_log = Logger.getInstance();
		_manager = manager;
		_jobQueue = jobQueue;
		_prop = prop;
		_resources = resources;
	}

	/**
	 * @param manager
	 *            Worker를 관리하는 Manager
	 * @param jobQueue
	 *            작업 queue
	 * @param node
	 *            XML 환경 변수
	 * @param resources
	 *            Resource 객체를 관리하는 Manager
	 */
	public Worker(WorkerManager manager, Vector jobQueue, XMLNode node,
			ResourceManager resources) {
		_log = Logger.getInstance();
		_manager = manager;
		_jobQueue = jobQueue;
		_node = node;
		_resources = resources;
	}

	/**
	 * Thread가 자연스럽게 종료되도록 유도
	 */
	public void terminate() {
		_runFlag = false;
	}

	public void run() {
		_runFlag = true;

		while (_runFlag) {
			// 작업 queue에 뭔가 있다면 꺼내온다
			Object aJob = null;
			synchronized (_jobQueue) {
				if (_jobQueue.size() > 0) {
					aJob = _jobQueue.remove(0);
				}
			}

			if (aJob == null) {
				// 작업 queue가 비었으면 대기
				synchronized (this) {
					_manager.addWaitingWorker(this);

					try {
						wait();
					} catch (Exception ignored) {
					}
				}
			} else
				// 뭔가 있다면 처리 시작
				exec(aJob);
		}
	}

	/**
	 * job 하나를 처리
	 * 
	 * @param aJob
	 *            처리할 job
	 */
	public abstract void exec(Object aJob);
}