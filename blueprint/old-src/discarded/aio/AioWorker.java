package lempel.blueprint.aio;

import java.io.IOException;
import java.util.Vector;

import lempel.blueprint.concurrent.Worker;

/**
 * AIO용 Worker
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
				// 대기열에 자신을 추가하고
				sleepingWorkers.add(this);

				// 대기
				try {
					wait();
				} catch (InterruptedException ignored) {
				}
			}

			// 작업 queue에서 Session 객체를 꺼내려고 시도
			Reactor client = null;
			while (jobQueue.size() > 0) {
				try {
					client = (Reactor) jobQueue.remove(0);
				} catch (RuntimeException ignored) {
					// race로 인해 다른 Worker에게 빼앗겨서
					// ArrayIndexOutOfBoundsException이 발생 가능
				}

				// 꺼낸게 있다면 command 별로 처리
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
	 * receive()/receiveObject()등의 수신 method를 호출하여 data를 얻은 후 적절한 처리를 하고<br>
	 * 다시 send(byte[])/sendObject(Serializable)등의 method로 응답을 처리하도록 구현한다<br>
	 * <b>주의!!</b> 연결을 종료해야 하는 경우 반드시 명시적으로 client.terminate()를 호출해야 한다<br>
	 * 
	 * @param client
	 */
	protected abstract void process(Object client);
}