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
package lempel.blueprint.base.log;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;

import lempel.blueprint.base.util.CharsetUtil;
import lempel.blueprint.base.util.StringUtil;
import lempel.blueprint.base.util.Validator;

/**
 * Logger
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2000. 12. 04
 * @last $Date$
 */
public class Logger {
	private static final String DEFAULT_LOG_LEVEL = "11111";

	private transient static Logger singleton;

	/** whether trace caller's source code or not */
	private boolean traceFlag = false;

	/** set if out/err stream is different */
	private static boolean separateStream = false;

	// Appender for logging
	private static IAppender appender;
	// lock for appender
	private static Object appenderLock;

	static {
		// do not change order
		appenderLock = new Object();
		singleton = new Logger();
	}

	/** Stream sync. lock */
	protected transient static ReentrantLock lock = new ReentrantLock();

	/** log level */
	protected static String logLevel;

	static {
		logLevel = DEFAULT_LOG_LEVEL;
	}

	public Logger() {
		super();

		// default
		toConsole();
	}

	/**
	 * @return Logger
	 */
	public static Logger getInstance() {
		return singleton;
	}

	/**
	 * Print log message to console from now on
	 * 
	 * @return Logger
	 */
	public static void toConsole() {
		toConsole(true);
	}

	/**
	 * Print log message to console from now on
	 * 
	 * @param replaceSystem
	 *            true: replace System.out/err
	 * @return Logger
	 */
	public static Logger toConsole(final boolean replaceSystem) {
		synchronized (appenderLock) {
			IAppender oldAppender = appender;
			appender = new ConsoleAppender(replaceSystem);
			separateStream = false;

			if (oldAppender != null) {
				oldAppender.close();
			}
		}

		return singleton;
	}

	/**
	 * Print log message to file from now on
	 * 
	 * @param outFile
	 *            log file name
	 * @return Logger
	 */
	public static Logger toFile(final String outFile) {
		return toFile(outFile, true, true);
	}

	/**
	 * Print log message to file from now on
	 * 
	 * @param outFile
	 *            log file name
	 * @param append
	 *            true: append to previous file
	 * @return Logger
	 */
	public static Logger toFile(final String outFile, final boolean append) {
		return toFile(outFile, append, true);
	}

	/**
	 * Print log message to file from now on
	 * 
	 * @param outFile
	 *            log file name
	 * @param append
	 *            true: append to previous file
	 * @param replaceSystem
	 *            true: replace System.out/err
	 * @return Logger
	 */
	public static Logger toFile(final String outFile, final boolean append, final boolean replaceSystem) {
		return toFile(outFile, null, append, replaceSystem);
	}

	/**
	 * Print log message to file from now on
	 * 
	 * @param outFile
	 *            log file name (normal messages)
	 * @param errFile
	 *            log file name (errors)
	 * @param append
	 *            true: append to previous file
	 * @param replaceSystem
	 *            true: replace System.out/err
	 * @return Logger
	 */
	public static Logger toFile(final String outFile, final String errFile, final boolean append,
			final boolean replaceSystem) {
		synchronized (appenderLock) {
			try {
				IAppender oldAppender = appender;
				if (errFile == null) {
					appender = new FileAppender(outFile, append, replaceSystem);
					separateStream = false;
				} else {
					appender = new FileAppender(outFile, errFile, append, replaceSystem);
					separateStream = true;
				}
				((FileAppender) appender).start();

				if (oldAppender != null) {
					oldAppender.close();
				}

			} catch (FileNotFoundException ex) {
				PrintStream outStream = appender.getOutStream();
				outStream.println("Log: ====== can't create log file       =====");
				outStream.println("Log: ====== log is redirected to stdout =====");
			}
		}

		return singleton;
	}

	/**
	 * logLevel is a String of 5 digit number.<br>
	 * Each digit represents each log level - SYS, WAN, INF, DBG, SQL.<br>
	 * Each digit can be 0 or 1.<br>
	 * 0 means off, 1 means on.<br>
	 * ex) "11111" - log everything, "00000" - no log<br>
	 * 
	 * @return
	 */
	public String getLogLevel() {
		return logLevel;
	}

	/**
	 * logLevel is a String of 5 digit number.<br>
	 * Each digit represents each log level - SYS, WAN, INF, DBG, SQL.<br>
	 * Each digit can be 0 or 1.<br>
	 * 0 means off, 1 means on.<br>
	 * ex) "11111" - log everything, "00000" - no log<br>
	 * 
	 * @param level
	 */
	public void setLogLevel(final String level) {
		logLevel = level;
	}

	/**
	 * set log level (LogLevel.ERR ~ LogLevel.SQL)
	 * 
	 * @param logLevel
	 */
	public void setLogLevel(final int logLevel) {
		switch (logLevel) {
		case LogLevel.ERR:
			setLogLevel("00000");
			break;
		case LogLevel.SYS:
			setLogLevel("10000");
			break;
		case LogLevel.WAN:
			setLogLevel("11000");
			break;
		case LogLevel.INF:
			setLogLevel("11100");
			break;
		case LogLevel.DBG:
			setLogLevel("11110");
			break;
		case LogLevel.SQL:
			setLogLevel("11111");
			break;
		default:
			setLogLevel(DEFAULT_LOG_LEVEL);
			break;
		}
	}

	public void print(final Object msg) {
		PrintStream outStream = appender.getOutStream();

		lock.lock();
		try {
			if (traceFlag) {
				outStream.print(getTraceInfo() + msg);
			} else {
				outStream.print(msg);
			}
		} finally {
			lock.unlock();
		}
	}

	public void println(final Object msg) {
		PrintStream outStream = appender.getOutStream();

		lock.lock();
		try {
			if (traceFlag) {
				outStream.println(getTraceInfo() + msg);
			} else {
				outStream.println(msg);
			}
		} finally {
			lock.unlock();
		}
	}

	public void println(final int level, final Object msg) {
		PrintStream outStream = appender.getOutStream();
		PrintStream errStream = appender.getErrStream();

		if (level == LogLevel.ERR) {
			lock.lock();
			try {
				String printMsg;
				if (traceFlag) {
					printMsg = LogLevel.strLevel[0] + getTraceInfo() + msg;
				} else {
					printMsg = LogLevel.strLevel[0] + msg;
				}

				errStream.println(printMsg);
				if (separateStream) {
					outStream.println(printMsg);
				}
			} finally {
				lock.unlock();
			}
			return;
		}

		try {
			if (logLevel == null) {
				if (traceFlag) {
					outStream.println(LogLevel.strLevel[level + 1] + getTraceInfo() + msg);
				} else {
					outStream.println(LogLevel.strLevel[level + 1] + msg);
				}
			} else {
				int targetLevel = Integer.parseInt(String.valueOf(logLevel.charAt(level)));
				lock.lock();
				try {
					String printMsg = "";
					if (traceFlag) {
						printMsg = LogLevel.strLevel[level + 1] + getTraceInfo() + msg;
					} else {
						printMsg = LogLevel.strLevel[level + 1] + msg;
					}

					if (targetLevel == 1) {
						outStream.println(printMsg);
					}
					if ((level == LogLevel.WAN || level == LogLevel.ERR) && separateStream) {
						errStream.println(printMsg);
					}
				} finally {
					lock.unlock();
				}
			}
		} catch (IndexOutOfBoundsException oe) {
			warning(this, "No such error level - " + level);
		}
	}

	public void println(final Object caller, final int nLevel, final Object msg) {
		lock.lock();
		try {
			println(nLevel, getCallerInfo(caller) + msg);
		} finally {
			lock.unlock();
		}
	}

	public void println(final int nLevel, final Object... msgs) {
		lock.lock();
		try {
			int totalLen = 0;
			byte[][] msgsBytes = new byte[msgs.length][];

			String aMsg;
			for (int i = 0; i < msgs.length; i++) {
				aMsg = (msgs[i] == null) ? "null" : msgs[i].toString();
				try {
					msgsBytes[i] = aMsg.getBytes(CharsetUtil.getDefaultEncoding());
				} catch (UnsupportedEncodingException e) {
					msgsBytes[i] = aMsg.getBytes();
				}
				totalLen += msgsBytes[i].length;
			}

			int offset = 0;
			byte[] logMsg = new byte[totalLen];
			for (int i = 0; i < msgs.length; i++) {
				System.arraycopy(msgsBytes[i], 0, logMsg, offset, msgsBytes[i].length);
				offset += msgsBytes[i].length;
			}

			try {
				println(nLevel, new String(logMsg, CharsetUtil.getDefaultEncoding()));
			} catch (UnsupportedEncodingException e) {
				println(nLevel, new String(logMsg));
			}
		} finally {
			lock.unlock();
		}
	}

	public void println(final Object caller, final int nLevel, final Object... msgs) {
		lock.lock();
		try {
			if (caller == null) {
				println(nLevel, msgs);
			} else {
				Object[] newMsgs = new Object[msgs.length + 1];
				newMsgs[0] = getCallerInfo(caller);
				for (int i = 0; i < msgs.length; i++) {
					newMsgs[1 + i] = msgs[i];
				}

				println(nLevel, newMsgs);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * print hexa decimal values
	 * 
	 * @param msg
	 */
	public void hexDump(final byte[] msg) {
		lock.lock();
		try {
			println(StringUtil.toHex(msg));
		} finally {
			lock.unlock();
		}
	}

	public void error(final Object msg) {
		lock.lock();
		try {
			println(LogLevel.ERR, msg);
		} finally {
			lock.unlock();
		}
	}

	public void error(final Object caller, final Object msg) {
		lock.lock();
		try {
			error(getCallerInfo(caller) + msg);
		} finally {
			lock.unlock();
		}
	}

	public void system(final Object msg) {
		lock.lock();
		try {
			println(LogLevel.SYS, msg);
		} finally {
			lock.unlock();
		}
	}

	public void system(final Object caller, final Object msg) {
		lock.lock();
		try {
			system(getCallerInfo(caller) + msg);
		} finally {
			lock.unlock();
		}
	}

	public void warning(final Object msg) {
		lock.lock();
		try {
			println(LogLevel.WAN, msg);
		} finally {
			lock.unlock();
		}
	}

	public void warning(final Object caller, final Object msg) {
		lock.lock();
		try {
			warning(getCallerInfo(caller) + msg);
		} finally {
			lock.unlock();
		}
	}

	public void info(final Object msg) {
		lock.lock();
		try {
			println(LogLevel.INF, msg);
		} finally {
			lock.unlock();
		}
	}

	public void info(final Object caller, final Object msg) {
		lock.lock();
		try {
			info(getCallerInfo(caller) + msg);
		} finally {
			lock.unlock();
		}
	}

	public void debug(final Object msg) {
		lock.lock();
		try {
			println(LogLevel.DBG, msg);
		} finally {
			lock.unlock();
		}
	}

	public void debug(final Object caller, final Object msg) {
		lock.lock();
		try {
			debug(getCallerInfo(caller) + msg);
		} finally {
			lock.unlock();
		}
	}

	public void trace(final Throwable thr) {
		lock.lock();
		try {
			thr.printStackTrace(appender.getErrStream());
			if (separateStream) {
				thr.printStackTrace(appender.getOutStream());
			}
		} finally {
			lock.unlock();
		}
	}

	public String getCallerInfo(final Object caller) {
		String result = "";
		if (Validator.isNotNull(caller)) {
			result = "[" + caller.getClass().getSimpleName() + "#" + caller.hashCode() + "] ";
		}
		return result;
	}

	public String getTraceInfo() {
		String result = "";
		try {
			throw new IllegalStateException();
		} catch (Exception e) {
			StackTraceElement[] ste = e.getStackTrace();
			for (int i = 0; i < ste.length; i++) {
				// exclude Logger itself
				if (!ste[i].getClassName().startsWith(Logger.class.getName())) {
					result = "(" + ste[i].getFileName() + ":" + ste[i].getLineNumber() + ") ";
					break;
				}
			}
		}

		return result;
	}

	public boolean isTraceFlag() {
		return traceFlag;
	}

	public void setTraceFlag(final boolean traceFlag) {
		this.traceFlag = traceFlag;
	}
}
