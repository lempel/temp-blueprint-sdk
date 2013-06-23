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

import blueprint.sdk.logger.Logger;
import bluerpint.sdk.util.jvm.shutdown.Terminatable;

/**
 * Worker Thread
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 07. 25
 * @last $Date$
 */
public abstract class Worker<T> implements Terminatable, Runnable {
	private static final Logger LOGGER = Logger.getInstance();

	protected transient JobQueue<T> jobQueue = null;

	private transient boolean running = false;
	
	private transient boolean terminated = false;

	private transient boolean active = false;

	public Worker(final JobQueue<T> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public void start() {
		Thread thr = new Thread(this);
		thr.setName(this.getClass().getName());
		thr.setDaemon(true);
		thr.start();
	}

	public void run() {
		running = true;

		while (running) {
			try {
				// blocks until queue have something to pop
				T job = jobQueue.pop();
				active = true;
				process(job);
			} catch (InterruptedException e) {
				LOGGER.error(this, e.toString());
			} finally {
				jobQueue.increaseProcessedJobCounter();
				active = false;
			}
		}
		
		terminated = true;
	}

	/**
	 * handles a job or client object
	 * 
	 * @param clientObject
	 */
	protected abstract void process(T clientObject);

	public boolean isRunning() {
		return running;
	}

	public boolean isValid() {
		return isRunning();
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void terminate() {
		running = false;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	protected void finalize() throws Throwable {
		jobQueue.clear();
		jobQueue = null;

		super.finalize();
	}
}