package lempel.old.framework;

import java.util.*;

/**
 * �ִ� ó������ ������ �� �ִ� Worker
 * 
 * @author Sang-min Lee
 * @since 2004.11.2.
 * @version 2004.11.2.
 */
public abstract class PeakPointControlWorker extends Worker {
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
	public PeakPointControlWorker(WorkerManager manager, Vector jobQueue,
			PropertyManager prop, ResourceManager resources) {
		super(manager, jobQueue, prop, resources);

		_header = "PeakPointControlWorker: ";
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
	public PeakPointControlWorker(WorkerManager manager, Vector jobQueue,
			XMLNode node, ResourceManager resources) {
		super(manager, jobQueue, node, resources);

		_header = "PeakPointControlWorker: ";
	}

	public void run() {
		_runFlag = true;

		while (_runFlag) {
			Object aJob = null;
			synchronized (_jobQueue) {
				if (_jobQueue.size() > 0) {
					aJob = _jobQueue.remove(0);
				}
			}

			// �۾� queue�� ������� ���
			if (aJob == null) {
				synchronized (this) {
					_manager.addWaitingWorker(this);

					try {
						wait();
					} catch (Exception ignored) {
					}
				}
			} else {
				// �ӵ���� �ʿ��� ��� ���
				try {
					if (_manager.get_delay() > 100)
						sleep(_manager.get_delay());
				} catch (Exception ex) {
				}

				exec(aJob);

				// ó���� ī��Ʈ
				_manager.add_currentLoad();
			}
		}
	}
}