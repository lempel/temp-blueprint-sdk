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
package lempel.blueprint.base.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import lempel.blueprint.base.concurrent.Terminatable;
import lempel.blueprint.base.log.Logger;

/**
 * Monitors memory usage<br>
 * For better result, please set -Xmx argument.<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2009. 3. 2.
 * @last $Date$
 */
public class MemoryMonitor implements Terminatable, Runnable {
	private static final Logger LOGGER = Logger.getInstance();

	/** check interval - 10sec */
	private static final int INTERVAL = 10000;
	/** memory usage limit to warn - 80% */
	private static final int WARNING_USAGE = 80;
	/** maximum tolerable warnings - 6times */
	private static final int MAX_WARNINGS = 6;

	private boolean running = false;
	private boolean trace = false;

	public MemoryMonitor() {
		super();
	}

	public MemoryMonitor(boolean trace) {
		super();

		this.trace = trace;
	}

	public boolean isValid() {
		return running;
	}

	public void terminate() {
		running = false;
	}

	public static int getMemoryUsage() {
		Runtime rtime = Runtime.getRuntime();
		long total = rtime.maxMemory();
		long used = rtime.totalMemory() - rtime.freeMemory();
		double ratio = (double) used / (double) total;
		return (int) (ratio * 100d);
	}

	public static boolean isXmxSet() {
		boolean result = false;

		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = RuntimemxBean.getInputArguments();
		for (String arg : arguments) {
			if (arg.toLowerCase().startsWith("-xmx")) {
				result = true;
				break;
			}
		}

		return result;
	}

	public void start() {
		Thread thr = new Thread(this);
		thr.setName(this.getClass().getName());
		thr.setDaemon(true);
		thr.start();
	}

	public void run() {
		running = true;

		boolean interrupted = false;
		int warnCount = 0;
		boolean xmx = isXmxSet();

		while (running) {
			if (!interrupted) {
				Runtime rtime = Runtime.getRuntime();
				long total = rtime.maxMemory();
				long used = rtime.totalMemory() - rtime.freeMemory();
				double ratio = (double) used / (double) total;
				int percent = (int) (ratio * 100d);

				if (percent >= WARNING_USAGE) {
					warnCount++;
					if (warnCount >= MAX_WARNINGS) {
						LOGGER.error(this, "LOW FREE MEMORY!! Memory usage is Critical. Over " + WARNING_USAGE
								+ "% for long time.");
						if (xmx) {
							LOGGER.error(this, "RECOMMEND: 1. Increase -Xmx value");
						} else {
							LOGGER.error(this, "RECOMMEND: 1. Set -Xmx value");
						}
						LOGGER.error(this, "RECOMMEND: 2. Check for memory leak");
						LOGGER.error(this, "RECOMMEND: 3. Increase performance (tune or get better system)");
						warnCount = 0;
					} else {
						LOGGER.warning(this, "Memory usage: " + percent + "% - " + (used / 1024 / 1024) + "M");
					}
				} else {
					warnCount = 0;
				}

				if (trace) {
					LOGGER.info(this, "Memory usage: " + percent + "% - " + (used / 1024 / 1024) + "M");
				}
			}

			try {
				Thread.sleep(INTERVAL);
				interrupted = false;
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
	}
}
