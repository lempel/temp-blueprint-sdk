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

import java.util.Enumeration;
import java.util.Hashtable;

import blueprint.sdk.logger.Logger;
import bluerpint.sdk.util.jvm.shutdown.Terminatable;

/**
 * Checks registered Terminatables and terminates timed-out or invalid ones
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 07. 20
 * @last $Date$
 */
public final class TimeoutHandler implements Terminatable, Runnable {
	private static final Logger LOGGER = Logger.getInstance();

	private transient Hashtable<Terminatable, Long> map = new Hashtable<Terminatable, Long>();
	private transient boolean running = false;
	private transient boolean terminated = false;

	/** timeout (msec) */
	private final int timeout;
	/** check interval (msec) */
	private final int interval;

	private TimeoutHandler(final int timeout, final int interval) {
		LOGGER.info(this, "creating timeout handler - timeout: " + timeout + "s, interval: " + timeout + "s");

		this.timeout = timeout * 1000;
		this.interval = interval * 1000;

		LOGGER.info(this, "timeout handler created - timeout: " + timeout + "s, _timeout: " + interval + "s");
	}

	public static TimeoutHandler newTimeoutHandler(final int timeout, final int interval) {
		TimeoutHandler result = new TimeoutHandler(timeout, interval);
		result.start();
		return result;
	}

	/**
	 * Updates given Terminatable's timestamp<br>
	 * Call right after every SUCCESSFUL read/write<br>
	 * 
	 * @param con
	 */
	public void updateTimestamp(Terminatable con) {
		map.put(con, System.currentTimeMillis());

		LOGGER.debug(this, "updateTimestamp - <" + con + ", " + map.get(con) + ">");
	}

	/**
	 * removes given Terminatable from handler
	 * 
	 * @param con
	 */
	public void remove(Terminatable con) {
		map.remove(con);
	}

	public void start() {
		Thread thr = new Thread(this);
		thr.setName(this.getClass().getName());
		thr.setDaemon(true);
		thr.start();
	}

	public boolean isValid() {
		return running;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void terminate() {
		running = false;

		Enumeration<Terminatable> enu = map.keys();
		while (enu.hasMoreElements()) {
			enu.nextElement().terminate();
		}
		map.clear();
	}

	public void run() {
		running = true;

		LOGGER.info(this, "timeout handler started - timeout: " + timeout + "ms, interval: " + interval + "ms");
		while (running) {
			try {
				Thread.sleep(getInterval());
			} catch (InterruptedException ignored) {
				LOGGER.error("oh my fuckin got!");
			}

			long currentTime = System.currentTimeMillis();

			Enumeration<Terminatable> enu = map.keys();
			long count = 0;
			while (enu.hasMoreElements()) {
				Terminatable key = (Terminatable) enu.nextElement();
				if (key.isValid()) {
					long timestamp = map.get(key);

					// close & cancel timed-out keys
					if (currentTime - timestamp >= getTimeout()) {
						key.terminate();
						count++;
					}
				} else {
					key.terminate();
					map.remove(key);
				}
			}

			if (count > 0) {
				LOGGER.debug(this, count + " connections are timed out (" + map.size() + " connections)");
			}
		}

		terminated = true;
				
		LOGGER.info(this, "timeout handler stopped - timeout: " + timeout + " interval: " + interval);
	}

	public boolean isRunning() {
		return running;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getInterval() {
		return interval;
	}

	@Override
	protected void finalize() throws Throwable {
		map.clear();
		map = null;

		super.finalize();
	}
}