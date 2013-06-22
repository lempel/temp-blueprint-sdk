package lempel.blueprint.app.tracer;

import java.util.*;
import java.io.*;

/**
 * ------------------------------ Method Tracing Instrumentation
 * ------------------------------
 * 
 * Implements logging of every method call that happens in a running program.
 * Calls to this class will have been injected into the byte-code at class-load
 * time by the TracingXformer class. So this class and TracingXformer must work
 * together at runtime to achieve method tracing.
 * 
 * A simplified version of this class would be one that simply calls
 * System.out.println("Entering "+method) inside methodEnter and did nothing
 * more than that. This would log every method call.
 * 
 * However this class goes a step further and monitors each thread stack so that
 * logging method calls can be indented correctly. The indenting adds a HUGE
 * amount of readability to the logging. Without indenting we loose the
 * knowledge of what is calling what - and the real call stack at any given time
 * is not decipherable.
 * 
 */

// TODO: Currently threadMap will maintain all threads that ever ran, so I need
// to add a
// clean up method that I call intermittently that will check everything in
// theadMap, and
// if isAlive is false then I can go ahead and remove the thread from threadMap.
// Since ThreadInfo
// is only a few bytes of overhead, I am putting off this minor detail for
// later. Only if many thousands of
// threads get created will this currently be a problem.
public class MethodLogger {
	// master switch. do tracing at all?
	// static private boolean trace = true;

	private static final Object logLk = new Object();
	static private PrintStream out = null;

	// Fast tracing uses a single StringBuilder (ftBuf) so that, while there is
	// overhead
	// involved in synchronizing threads for accessing it, there is NOT overhead
	// on the
	// garbage collector. So we allow a slight performance hit in order to keep
	// from putting
	// a massive load on memory allocation.
	// static private boolean fastTrace = true;
	static private StringBuilder ftBuf = new StringBuilder(4096);

	// This thread map is currently only used to keep track of certain stack
	// info for each
	// executing thread. Java has no way of doing this.
	// static HashMap<Thread, ThreadInfo> threadMap = new HashMap<Thread,
	// ThreadInfo>();
	static Hashtable<Thread, ThreadInfo> threadMap = new Hashtable<Thread, ThreadInfo>();

	private static TracingProperties props = TracingProperties.getInstance();

	/*
	 * This method will be called by the instrumentation added to the byte code
	 * whenever a non-static method gets called. It maintains some stack info
	 * and then logs the method call
	 */
	public static void instanceMethodEnter(Object obj, String method) {
		if (!props.isTrace())
			return;

		Thread thread = Thread.currentThread();
		if (props.isFastTrace()) {
			synchronized (ftBuf) {
				ftBuf.setLength(0);
				ftBuf.append(thread.getName()).append("#").append(
						thread.getId()).append(": ");
				ftBuf.append(obj.getClass().getName());
				ftBuf.append(".");
				ftBuf.append(method);
				log(ftBuf.toString());
			}
			return;
		}

		synchronized (threadMap) {
			ThreadInfo tinfo = (ThreadInfo) threadMap.get(thread);

			// UPDATE THEAD MAP (stack size)
			if (tinfo == null) {
				tinfo = new ThreadInfo(1);
				threadMap.put(thread, tinfo);
			} else {
				tinfo.stackSize++;
			}
			tinfo.pushTiming();

			synchronized (ftBuf) {
				ftBuf.setLength(0);
				ftBuf.append(thread.getName()).append("#").append(
						thread.getId()).append(": ");

				// TODO: for performance I could use a switch here and hard code
				// a string
				// to indent for each stack size up to about 10 (same for
				// similar blocks of code)

				// indent based on stack size.
				int stackSize = (tinfo != null ? tinfo.stackSize : 0);
				int stackLeft = stackSize;

				// start going 3 at a time (for performance)
				while (stackLeft >= 3) {
					ftBuf.append("   ");
					stackLeft -= 3;
				}

				while (stackLeft >= 1) {
					ftBuf.append(" ");
					stackLeft--;
				}

				// output method and class name
				ftBuf.append("> ");
				ftBuf.append(obj.getClass().getName());
				ftBuf.append(".");
				ftBuf.append(method);

				log(ftBuf.toString());
			}
		}
	}

	/*
	 * This method will be called by the instrumentation added to the byte code
	 * whenever a static method gets called. It maintains some stack info and
	 * then loggs the method call
	 */
	public static void staticMethodEnter(String className, String method) {
		if (!props.isTrace())
			return;
		
		if (props.isFastTrace()) {
			synchronized (ftBuf) {
				Thread thread = Thread.currentThread();
				ftBuf.setLength(0);
				ftBuf.append(thread.getName()).append("#").append(
						thread.getId()).append(": ");
				ftBuf.append(className);
				ftBuf.append(".");
				ftBuf.append(method);
				log(ftBuf.toString());
			}
			return;
		}

		synchronized (threadMap) {
			Thread thread = Thread.currentThread();
			ThreadInfo tinfo = (ThreadInfo) threadMap.get(thread);

			// UPDATE THEAD MAP (stack size)
			if (tinfo == null) {
				tinfo = new ThreadInfo(1);
				threadMap.put(thread, tinfo);
			} else {
				tinfo.stackSize++;
			}
			tinfo.pushTiming();

			synchronized (ftBuf) {
				ftBuf.setLength(0);
				ftBuf.append(thread.getName()).append("#").append(
						thread.getId()).append(": ");

				// indent based on stack size.
				int stackSize = (tinfo != null ? tinfo.stackSize : 0);
				int stackLeft = stackSize;

				// start going 3 at a time (for performance)
				while (stackLeft >= 3) {
					ftBuf.append("   ");
					stackLeft -= 3;
				}

				while (stackLeft >= 1) {
					ftBuf.append(" ");
					stackLeft--;
				}

				// add method and maybe class name
				ftBuf.append("> ");
				if (className != null) {
					ftBuf.append(className);
					ftBuf.append(".");
				}
				ftBuf.append(method);

				log(ftBuf.toString());
			}
		}
	}

	/*
	 * This method will be called by the instrumentation added to the byte code
	 * whenever a any method exits. It maintains some stack info and then logs
	 * the method exit info
	 */
	public static void methodExit(String method) {
		if (!props.isTrace())
			return;
		if (props.isFastTrace())
			return;

		synchronized (threadMap) {
			Thread thread = Thread.currentThread();
			ThreadInfo tinfo = (ThreadInfo) threadMap.get(thread);
			long time = tinfo.popTiming();
			int delta = tinfo.lastPushPos - tinfo.tos;

			synchronized (ftBuf) {
				ftBuf.setLength(0);
				ftBuf.append(thread.getName()).append("#").append(
						thread.getId()).append(": ");

				// indent based on stack size.
				int stackSize = (tinfo != null ? tinfo.stackSize : 0);
				int stackLeft = stackSize;

				// start going 3 at a time (for performance)
				while (stackLeft >= 3) {
					ftBuf.append("   ");
					stackLeft -= 3;
				}

				while (stackLeft >= 1) {
					ftBuf.append(" ");
					stackLeft--;
				}

				// add method name
				ftBuf.append("< ");
				ftBuf.append(delta == 0 ? "*" : method);
				ftBuf.append(" t = ");

				if (time > 999999999) {
					time /= 999999999;
					ftBuf.append(time);
					ftBuf.append("s");
				} else if (time > 999999) {
					time /= 999999;
					ftBuf.append(time);
					ftBuf.append("ms");
				} else if (time > 999) {
					time /= 999;
					ftBuf.append(time);
					ftBuf.append("mc");
				} else {
					ftBuf.append(time);
					ftBuf.append("ns");
				}

				log(ftBuf.toString());
			}

			// UPDATE THEAD MAP (stack size)
			if (tinfo == null) {
				// I could throw an exception here, because this should never
				// happen. However since
				// I must trust the instrumentation to be perfect, and since
				// adding exception handling around
				// every method enter and exit would be too much impact on
				// performance, I opt not to catch
				// this error case.
			} else {
				// ditto for error case if stackSize ever goes negative.
				tinfo.stackSize--;
			}		
		}
	}

	public static void log(String msg) {
		synchronized (logLk) {
			try {
				if (out == null) {
					String logFileName = props.getLogFileName();

					// TODO: how delayed is this output if PrintStream autoflush
					// param is false instead?
					out = new PrintStream(new BufferedOutputStream(
							new FileOutputStream(logFileName)), true);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// System.out.println(msg);
			// TODO: make this optional (duplicating to console window)
			out.println(msg);
		}
	}

	// NOTE: DO NOT DELETE:
	/*
	 * I experimented with trying some way to get the logging to appear in the
	 * log file of the application being monitored but then I decided this is
	 * not what I want to do anyway...and it never did work anyway.
	 * 
	 * public static void injectLog(String msg) {
	 * 	if (injectionTestDone) return;
	 * 
	 * 	Class clazz = Class.forName("org.apache.log4j.Logger");
	 * 	Class clazz2 = Class.forName("com.ca.harvest.core.log");
	 * 	Method mainMethod = clazz.getMethod("getLogger", new Class[]{Class.class});
	 * 	Object[] args = new Object[1]; args[0] = clazz2;
	 * 	Object retVal = mainMethod.invoke(null, args);
	 * 	System.out.println("retVal className = " + retVal.getClass().getName());
	 * 
	 * 	try {
	 * 		Thread.sleep(7000);
	 * 	} catch (InterruptedException e) {
	 * 		// TODO Auto-generated catch block e.printStackTrace();
	 * 	}
	 * }
	 */
}
