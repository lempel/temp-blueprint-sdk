package lempel.blueprint.service.socks;

import java.nio.channels.Selector;
import java.util.Vector;

import lempel.blueprint.concurrent.Worker;

/**
 * Target������ ������ ó��<br>
 * Reactor���� ���� target���� ������ �õ��ϸ� Selector�� �׸�ŭ blocking�ǹǷ� ������ class�� �и��ؾ� �Ѵ�.<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 12. 12
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Connector {
	/** Worker Thread�� */
	protected Worker[] workers = null;

	/** �����ؾ� �� Reactor���� queue */
	private Vector<Object> jobQueue = new Vector<Object>(100, 10);
	/** �������� Worker Thread�� */
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
	 * Target���� �����ؾ� �� Reactor�� ������ ��û
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