package lempel.old.framework;


import java.util.*;

import lempel.blueprint.log.*;

/**
 * Client�� ��û�� ó���ϴ� Thread�� framework (Worker Thread Pattern�� ����) <br>
 * J2SE 5�� Executor�� ������ ����̹Ƿ� ��ü ����
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2004.11.2.
 */
public abstract class Worker extends Thread {
	/** log ��¿� ��� */
	protected String _header = "Worker: ";

	/** logger */
	protected Logger _log = null;

	/** Worker���� �����ϴ� Manager */
	protected WorkerManager _manager = null;

	/** �۾� queue */
	protected Vector _jobQueue = null;

	/** ȯ�� ���� */
	protected PropertyManager _prop = null;

	/** Resource��ü���� �����ϴ� Manager */
	protected ResourceManager _resources = null;

	/** Thread ���� ���� flag */
	protected boolean _runFlag = false;

	/** XML ȯ�� ���� */
	protected XMLNode _node = null;

	/**
	 * @param manager
	 *            Worker�� �����ϴ� Manager
	 * @param jobQueue
	 *            �۾� queue
	 * @param prop
	 *            ȯ�� ����
	 * @param resources
	 *            Resource ��ü�� �����ϴ� Manager
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
	 *            Worker�� �����ϴ� Manager
	 * @param jobQueue
	 *            �۾� queue
	 * @param node
	 *            XML ȯ�� ����
	 * @param resources
	 *            Resource ��ü�� �����ϴ� Manager
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
	 * Thread�� �ڿ������� ����ǵ��� ����
	 */
	public void terminate() {
		_runFlag = false;
	}

	public void run() {
		_runFlag = true;

		while (_runFlag) {
			// �۾� queue�� ���� �ִٸ� �����´�
			Object aJob = null;
			synchronized (_jobQueue) {
				if (_jobQueue.size() > 0) {
					aJob = _jobQueue.remove(0);
				}
			}

			if (aJob == null) {
				// �۾� queue�� ������� ���
				synchronized (this) {
					_manager.addWaitingWorker(this);

					try {
						wait();
					} catch (Exception ignored) {
					}
				}
			} else
				// ���� �ִٸ� ó�� ����
				exec(aJob);
		}
	}

	/**
	 * job �ϳ��� ó��
	 * 
	 * @param aJob
	 *            ó���� job
	 */
	public abstract void exec(Object aJob);
}