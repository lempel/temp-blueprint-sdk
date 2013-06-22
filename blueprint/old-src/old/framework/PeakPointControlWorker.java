package lempel.old.framework;

import java.util.*;

/**
 * 최대 처리량을 제한할 수 있는 Worker
 * 
 * @author Sang-min Lee
 * @since 2004.11.2.
 * @version 2004.11.2.
 */
public abstract class PeakPointControlWorker extends Worker {
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
	public PeakPointControlWorker(WorkerManager manager, Vector jobQueue,
			PropertyManager prop, ResourceManager resources) {
		super(manager, jobQueue, prop, resources);

		_header = "PeakPointControlWorker: ";
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

			// 작업 queue가 비었으면 대기
			if (aJob == null) {
				synchronized (this) {
					_manager.addWaitingWorker(this);

					try {
						wait();
					} catch (Exception ignored) {
					}
				}
			} else {
				// 속도제어가 필요한 경우 대기
				try {
					if (_manager.get_delay() > 100)
						sleep(_manager.get_delay());
				} catch (Exception ex) {
				}

				exec(aJob);

				// 처리량 카운트
				_manager.add_currentLoad();
			}
		}
	}
}