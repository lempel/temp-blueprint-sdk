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

	/** Singleton */
	protected transient static Logger log;

	/** Stream sync. lock */
	protected transient static ReentrantLock lock = new ReentrantLock();

	/** log level */
	protected String logLevel = DEFAULT_LOG_LEVEL;

	/** whether trace caller's source code or not */
	private boolean traceFlag = false;

	/** set if out/err stream is different */
	private boolean separateStream = false;

	// Stream for logging
	private transient LogStream outStream;
	private transient LogStream errStream;

	public Logger() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param replaceSystem
	 *            set to replace System Streams (System.out, System.err)
	 */
	public Logger(final boolean replaceSystem) {
		synchronized (this) {
			if (log == null) {
				log = this;
				ConsoleAppender appender = new ConsoleAppender(replaceSystem);
				outStream = appender.outStream;
				errStream = appender.errStream;
				separateStream = false;
			} else {
				throw new IllegalStateException("You can't create Logger instance more than 1.");
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 *            file name to store System.out's output
	 */
	public Logger(final String outFileName) {
		this(outFileName, true);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 *            file name to store System.out's output
	 * @param append
	 *            true: append
	 */
	public Logger(final String outFileName, final boolean append) {
		this(outFileName, append, false);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 *            file name to store System.out's output
	 * @param append
	 *            true: append
	 * @param replaceSystem
	 *            set to replace System Streams (System.out, System.err)
	 */
	public Logger(final String outFileName, final boolean append, final boolean replaceSystem) {
		synchronized (this) {
			if (log == null) {
				try {
					FileAppender appender = new FileAppender(outFileName, append, replaceSystem);
					outStream = appender.outStream;
					errStream = appender.errStream;
					separateStream = false;
					appender.start();

					log = this;
				} catch (FileNotFoundException ex) {
					outStream.println("Log: ====== can't create log file       =====");
					outStream.println("Log: ====== log is redirected to stdout =====");
				}
			} else {
				throw new IllegalStateException("You can't create Logger instance more than 1");
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 *            file name to store System.out's output
	 * @param errFileName
	 *            file name to store System.err's output
	 */
	public Logger(final String outFileName, final String errFileName) {
		this(outFileName, errFileName, true);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 *            file name to store System.out's output
	 * @param errFileName
	 *            file name to store System.err's output
	 * @param append
	 *            true: append
	 */
	public Logger(final String outFileName, final String errFileName, final boolean append) {
		this(outFileName, errFileName, append, false);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 *            file name to store System.out's output
	 * @param errFileName
	 *            file name to store System.err's output
	 * @param append
	 *            true: append
	 * @param replaceSystem
	 *            set to replace System Streams (System.out, System.err)
	 */
	public Logger(final String outFileName, final String errFileName, final boolean append, final boolean replaceSystem) {
		synchronized (this) {
			if (log == null) {
				try {
					FileAppender appender = new FileAppender(outFileName, errFileName, append, replaceSystem);
					outStream = appender.outStream;
					errStream = appender.errStream;
					separateStream = !outFileName.equals(errFileName);
					appender.start();

					log = this;
				} catch (FileNotFoundException ex) {
					outStream.println("Log: ====== can't create log file       =====");
					outStream.println("Log: ====== log is redirected to stdout =====");
				}
			} else {
				throw new IllegalStateException("You can't create Logger instance more than 1");
			}

		}
	}

	public static synchronized Logger getInstance() {
		if (log == null) {
			log = new Logger();
		}

		return log;
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
	 * @param logLevel
	 */
	public void setLogLevel(final String logLevel) {
		this.logLevel = logLevel;
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

	public void println(final Object msg) {
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
		if (level < LogLevel.SYS) {
			lock.lock();
			try {
				String printMsg;
				if (traceFlag) {
					printMsg = LogLevel.strLevel[0] + getTraceInfo() + msg;
				} else {
					printMsg = LogLevel.strLevel[0] + msg;
				}

				outStream.println(printMsg);
				if (separateStream) {
					errStream.println(printMsg);
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
					if (level == LogLevel.WAN && separateStream) {
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
		println(nLevel, getCallerInfo(caller) + msg);
	}

	public void println(final int nLevel, final Object... msgs) {
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
	}

	public void println(final Object caller, final int nLevel, final Object... msgs) {
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
	}

	/**
	 * print hexa decimal values
	 * 
	 * @param msg
	 */
	public void hexDump(final byte[] msg) {
		println(StringUtil.toHex(msg));
	}

	public void error(final Object msg) {
		println(LogLevel.ERR, msg);
	}

	public void error(final Object caller, final Object msg) {
		error(getCallerInfo(caller) + msg);
	}

	public void system(final Object msg) {
		println(LogLevel.SYS, msg);
	}

	public void system(final Object caller, final Object msg) {
		system(getCallerInfo(caller) + msg);
	}

	public void warning(final Object msg) {
		println(LogLevel.WAN, msg);
	}

	public void warning(final Object caller, final Object msg) {
		warning(getCallerInfo(caller) + msg);
	}

	public void info(final Object msg) {
		println(LogLevel.INF, msg);
	}

	public void info(final Object caller, final Object msg) {
		info(getCallerInfo(caller) + msg);
	}

	public void debug(final Object msg) {
		println(LogLevel.DBG, msg);
	}

	public void debug(final Object caller, final Object msg) {
		debug(getCallerInfo(caller) + msg);
	}

	public void trace(final Throwable thr) {
		thr.printStackTrace();
	}

	public boolean isTracing() {
		return traceFlag;
	}

	public void setTracing(final boolean trace_) {
		this.traceFlag = trace_;
	}

	private String getCallerInfo(final Object caller) {
		String result = "";
		if (Validator.isNotNull(caller)) {
			result = "[" + caller.getClass().getSimpleName() + "#" + caller.hashCode() + "] ";
		}
		return result;
	}

	private String getTraceInfo() {
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
