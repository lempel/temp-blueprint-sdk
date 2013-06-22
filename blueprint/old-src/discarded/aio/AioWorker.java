package lempel.blueprint.aio;

import java.io.IOException;
import java.util.Vector;

import lempel.blueprint.concurrent.Worker;

/**
 * AIO�� Worker
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 08. 22
 * @since 1.5
 * @last $Date$
 * @see
 */
public abstract class AioWorker extends Worker {
	/**
	 * Constructor
	 * 
	 * @param jobQueue
	 * @param sleepingWorkers
	 */
	public AioWorker(Vector<Object> jobQueue, Vector<Worker> sleepingWorkers) {
		super(jobQueue, sleepingWorkers);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (isRunning()) {
			// $ANALYSIS-IGNORE
			synchronized (this) {
				// ��⿭�� �ڽ��� �߰��ϰ�
				sleepingWorkers.add(this);

				// ���
				try {
					wait();
				} catch (InterruptedException ignored) {
				}
			}

			// �۾� queue���� Session ��ü�� �������� �õ�
			Reactor client = null;
			while (jobQueue.size() > 0) {
				try {
					client = (Reactor) jobQueue.remove(0);
				} catch (RuntimeException ignored) {
					// race�� ���� �ٸ� Worker���� ���Ѱܼ�
					// ArrayIndexOutOfBoundsException�� �߻� ����
				}

				// ������ �ִٸ� command ���� ó��
				if (client != null) {
					synchronized (client) {
						try {
							client.read();
							process(client);
						} catch (IOException e) {
							client.terminate();
						} catch (RuntimeException e) {
							client.terminate();
						}
					}
				}
			}
		}
	}

	/**
	 * receive()/receiveObject()���� ���� method�� ȣ���Ͽ� data�� ���� �� ������ ó���� �ϰ�<br>
	 * �ٽ� send(byte[])/sendObject(Serializable)���� method�� ������ ó���ϵ��� �����Ѵ�<br>
	 * <b>����!!</b> ������ �����ؾ� �ϴ� ��� �ݵ�� ��������� client.terminate()�� ȣ���ؾ� �Ѵ�<br>
	 * 
	 * @param client
	 */
	protected abstract void process(Object client);
}