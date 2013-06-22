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

import java.util.List;
import java.util.Vector;

import lempel.blueprint.base.util.Counter;

/**
 * JobQueue for Workers.<br>
 * A Worker Group shares a JobQueue.<br>
 * <b>Thread Safe</b><br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2008. 11. 25.
 * @last $Date$
 */
public class JobQueue<T> {
	/** mutex for exclusion */
	private final Mutex mtx = new Mutex();
	/** mutex for lock */
	private final Mutex lock = new Mutex();
	/** actual job queue */
	private final List<T> queue = new Vector<T>();
	/** counts how many jobs are processed */
	private final Counter processedJobs = new Counter();

	/** is all threads busy situation occured? */
	private boolean allBusyTrap = false;
	/** lock for allBusyTrap flag */
	private final Object allBusyTrapLock = new Object();
	/** start/stop count */
	private boolean count = false;;

	/**
	 * push a job Object to queue<br>
	 * 
	 * @param aJob
	 * @throws InterruptedException
	 *             mutex exception
	 */
	public void push(final T aJob) throws InterruptedException {
		mtx.acquire();

		queue.add(aJob);

		// if lock is not in use (i.e. all threads are busy)
		if (!lock.isInuse()) {
			synchronized (allBusyTrapLock) {
				allBusyTrap = true;
			}
		}

		// release lock mutex
		lock.release();

		mtx.release();
	}

	/**
	 * pops a job Object from queue
	 * 
	 * @return a job Object
	 * @throws InterruptedException
	 *             mutex exception
	 */
	public T pop() throws InterruptedException {
		mtx.acquire();

		T aJob;
		while (queue.size() <= 0) {
			// release exclusion mutex
			mtx.release();

			// acquire lock mutex
			lock.acquire();

			// re-acquire exclusion mutex to pop
			mtx.acquire();
		}
		aJob = queue.remove(0);

		mtx.release();

		return aJob;
	}

	/**
	 * returns size of queue
	 * 
	 * @return
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * is all threads busy situation occured?
	 * 
	 * @return true: yes
	 */
	public boolean isAllBusyTrapped() {
		synchronized (allBusyTrapLock) {
			return allBusyTrap;
		}
	}

	/**
	 * reset allBusyTrap flag
	 */
	protected void resetAllBusyTrap() {
		synchronized (allBusyTrapLock) {
			allBusyTrap = false;
		}
	}

	/**
	 * start/stop count.<br>
	 * this method is called by WorkerGroup<br>
	 * 
	 * @param flag
	 */
	protected void setCount(boolean flag) {
		this.count = flag;
	}

	/**
	 * counts a processed job<br>
	 * this method is called by Worker<br>
	 * 
	 */
	protected void increaseProcessedJobCounter() {
		if (count) {
			processedJobs.increase();
		}
	}

	/**
	 * returns processed jobs count
	 * 
	 * @return
	 */
	protected long getProcessedJobs() {
		return processedJobs.count();
	}

	/**
	 * reset processed jobs count
	 */
	protected void resetProcessedJobs() {
		processedJobs.reset();
	}

	/**
	 * clears queue
	 * 
	 * @throws InterruptedException
	 *             mutex exception
	 * 
	 */
	public void clear() throws InterruptedException {
		mtx.acquire();

		queue.clear();
		resetProcessedJobs();
		allBusyTrap = false;

		mtx.release();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		queue.clear();

		super.finalize();
	}
}
