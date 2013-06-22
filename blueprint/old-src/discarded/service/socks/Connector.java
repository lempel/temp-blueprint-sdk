package lempel.blueprint.service.socks;

import java.nio.channels.Selector;
import java.util.Vector;

import lempel.blueprint.concurrent.Worker;

/**
 * Target으로의 연결을 처리<br>
 * Reactor에서 직접 target으로 연결을 시도하면 Selector가 그만큼 blocking되므로 별도의 class로 분리해야 한다.<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 12. 12
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Connector {
	/** Worker Thread들 */
	protected Worker[] workers = null;

	/** 연결해야 할 Reactor들의 queue */
	private Vector<Object> jobQueue = new Vector<Object>(100, 10);
	/** 대기상태의 Worker Thread들 */
	private Vector<Worker> sleepingWorkers = new Vector<Worker>(10, 1);

	/**
	 * Constructor
	 * 
	 * @param workerCount
	 */
	public Connector(int workerCount) {
		workers = new Worker[workerCount];
		for (int i = 0; i < workerCount; i++) {
			workers[i] = new ConnectorWorker(jobQueue, sleepingWorkers);
			workers[i].start();
		}
	}

	/**
	 * Target으로 연결해야 할 Reactor의 연결을 요청
	 * 
	 * @param relayReactor
	 * @param address
	 * @param port
	 * @param sel
	 */
	public void register(Socks5RelayReactor relayReactor, String address,
			int port, Selector sel) {
		jobQueue.add(new ConnectionInfo(relayReactor, address, port, sel));

		synchronized (sleepingWorkers) {
			if (sleepingWorkers.size() > 0) {
				Object sleepingWorker = sleepingWorkers.remove(0);
				synchronized (sleepingWorker) {
					sleepingWorker.notify();
				}
			}
		}
	}
}