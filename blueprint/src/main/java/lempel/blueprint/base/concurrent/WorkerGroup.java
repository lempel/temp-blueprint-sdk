/*
 License:

 blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
 (http://www.eclipse.org/legal/epl-v10.html)


 Distribution:

 International - http://code.google.com/p/blueprint-sdk
 South Korea - http://lempel.egloos.com


 Background:

 blueprint-sdk is a java software development kit to protect other open source
 softwares' licenses. It's intended to provide light weight APIs for blueprints.
 Well... at least trying to.

 There are so many great open source projects now. Back in year 2000, there
 were not much to use. Even JDBC drivers were rare back then. Naturally, I have
 to implement many things by myself. Especially dynamic class loading, networking,
 scripting, logging and database interactions. It was time consuming. Now I can
 take my picks from open source projects.

 But I still need my own APIs. Most of my clients just don't understand open
 source licenses. They always want to have their own versions of open source
 projects but don't want to publish derivative works. They shouldn't use open
 source projects in the first place. So I need to have my own open source project
 to be free from derivation terms and also as a mediator between other open
 source projects and my client's requirements.

 Primary purpose of blueprint-sdk is not to violate other open source project's
 license terms.


 To commiters:

 License terms of the other software used by your source code should not be
 violated by using your source code. That's why blueprint-sdk is made for.
 Without that, all your contributions are welcomed and appreciated.
 */
package lempel.blueprint.base.concurrent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lempel.blueprint.base.log.Logger;

/**
 * A Group of Workers<br>
 * Maintains JobQueue and Workers<br>
 * <br>
 * If you want to use automatic thread spanning feature, call start() method.<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2008. 11. 25.
 * @last $Date$
 */
public class WorkerGroup implements Terminatable, Runnable {
	/** check interval (msec) */
	private static final int INTERVAL = 10000;
	/** worker thread increase ratio */
	private static final float THREAD_INC_RATIO = 0.2f;

	private static final Logger LOGGER = Logger.getInstance();

	private final Class<? extends Worker> workerClass;
	private transient final JobQueue jobQueue;
	private transient final List<Worker> workers;

	private transient boolean running = false;

	/**
	 * Constructor<br>
	 * Creates Workers and JobQueue<br>
	 * 
	 * @param workerClass
	 * @param workerCount
	 *            Initial number of workers
	 * @throws InvocationTargetException
	 *             Worker instantiation failure
	 * @throws IllegalAccessException
	 *             Can't access Worker's constructor (it should not happen)
	 * @throws InstantiationException
	 *             Worker instantiation failure
	 * @throws IllegalArgumentException
	 *             Wrong argument for Worker's constructor (it should not
	 *             happen)
	 * @throws NoSuchMethodException
	 *             workerClass is not an Worker or has no visible constructor
	 * @throws SecurityException
	 *             Can't retrieve Worker's constructor
	 */
	public WorkerGroup(final Class<? extends Worker> workerClass, final int workerCount)
			throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		LOGGER.info(this, "creating worker group - class: " + workerClass + ", count: " + workerCount);

		// register to shutdown hook (Terminator)
		Terminator.getInstance().register(this);

		this.jobQueue = new JobQueue();
		this.workers = new ArrayList<Worker>(workerCount);
		this.workerClass = workerClass;

		// instantiate & start workers
		for (int i = 0; i < workerCount; i++) {
			newWorker();
		}

		LOGGER.info(this, "worker group created - class: " + workerClass + ", count: " + workerCount);
	}

	/**
	 * create & add a new Worker
	 * 
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void newWorker() throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Worker aWorker;
		Constructor<? extends Worker> cons = workerClass.getConstructor(JobQueue.class);
		aWorker = cons.newInstance(jobQueue);
		workers.add(aWorker);
		aWorker.start();
	}

	/**
	 * add more workers
	 * 
	 * @param newThreads
	 */
	private void addWorkers(final int newThreads) {
		int failure = 0;
		for (int i = 0; i < newThreads; i++) {
			try {
				newWorker();
			} catch (Exception e) {
				LOGGER.error("worker creation failed - " + e);
				failure++;
			}
		}
		LOGGER.info(this, "worker added - class: " + workerClass + ", count: " + (newThreads - failure));
	}

	public boolean isValid() {
		return running;
	}

	public void terminate() {
		running = false;

		if (workers != null) {
			Iterator<Worker> iter = workers.iterator();
			while (iter.hasNext()) {
				iter.next().terminate();
			}
		}
	}

	public void addJob(final Object job) throws InterruptedException {
		jobQueue.push(job);
	}

	public void start() {
		// if initial number of workers are too small, increase it first
		if (workers.size() * THREAD_INC_RATIO < 1) {
			int newThreads = (int) (1.0f / THREAD_INC_RATIO) - workers.size();
			addWorkers(newThreads);
		}

		Thread thr = new Thread(this);
		thr.setName(this.getClass().getName());
		thr.setDaemon(true);
		thr.start();
	}

	public void run() {
		running = true;
		boolean interrupted = false;
		long start = 0L;

		long maxThroughput = 0;

		while (running) {
			try {
				if (interrupted) {
					// reset interrupted flag & keep start time
					interrupted = false;
				} else {
					// new period, update start time
					start = System.currentTimeMillis();
				}
				Thread.sleep(INTERVAL);
			} catch (InterruptedException ignored) {
				interrupted = true;
			}

			if (!interrupted) {
				// calculate elapsed time
				long elapsed = System.currentTimeMillis() - start;
				// convert msec to sec with rounding
				elapsed = (elapsed + 500) / 1000;

				// calculate throughput & reset counter
				long throughtput = jobQueue.getProcessedJobs() / elapsed;
				jobQueue.resetProcessedJobs();

				// is all busy situation occured?
				if (jobQueue.isAllBusyTrapped()) {
					jobQueue.resetAllBusyTrap();

					// new record?
					if (throughtput >= maxThroughput) {
						maxThroughput = throughtput;

						// increase the number of threads
						int newThreads = (int) (THREAD_INC_RATIO * workers.size());
						addWorkers(newThreads);
					}
				}
			}
		}
	}
}
