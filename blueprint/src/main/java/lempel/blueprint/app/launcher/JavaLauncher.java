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
package lempel.blueprint.app.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import lempel.blueprint.base.config.XmlConfig;
import lempel.blueprint.base.log.Logger;
import lempel.blueprint.base.util.CharsetUtil;
import lempel.blueprint.base.util.StringUtil;
import lempel.blueprint.base.util.Validator;

import org.xml.sax.SAXException;

/**
 * A Java application launcher for jar hell.<br>
 * Provides easier way to configure classpath.<br>
 * <br>
 * waitFor option:<br>
 * If on, launcher will redirect target's standard output/error streams to
 * console and wait for target to destroy. You don't have to kill separately
 * when waitFor option is on. Just kill Launcher.<br>
 * <br>
 * trace option:<br>
 * To turn on trace function, you need a trace.jar (<a
 * href="http://x-15.org/tracing.htm">http://x-15.org/tracing.htm</a>) file and<br>
 * create a 'trace.properties' file on working directory<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 12. 12
 * @last $Date$
 */
public class JavaLauncher {
	private static final Logger logger = Logger.getInstance();

	/** is this running on M$ Windows? */
	private static boolean isWindows = false;

	private static String pathSeparator = ":";
	private static String traceJarName = "trace.jar";
	private static String traceJarLocation = null;

	static {
		// check OS
		if (System.getProperty("os.name").startsWith("Windows")) {
			isWindows = true;
		}

		if (isWindows) {
			pathSeparator = ";";
		}
	}

	/**
	 * Entry Point
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java " + JavaLauncher.class.getName() // NOPMD
					// by
					// Simon
					// Lee
					// on
					// 09.
					// 3.
					// 9
					// ����
					// 1:46
					+ " <config file name>");
			System.exit(1);
		}

		// load configuration
		XmlConfig config = new XmlConfig();
		try {
			config.load(args[0]);
		} catch (IOException e) {
			logger.error(e.toString());
			return;
		} catch (ParserConfigurationException e) {
			logger.error(e.toString());
			return;
		} catch (SAXException e) {
			logger.error(e.toString());
			return;
		}

		String invoke = config.getString("/javaLauncher/invoke");
		boolean waitFor = config.getBoolean("/javaLauncher/invoke/@waitFor");
		boolean trace = config.getBoolean("/javaLauncher/invoke/@trace");
		// String[] classDirs = config.getStringArray("/javaLauncher/classDir");
		String[] jarFiles = config.getStringArray("/javaLauncher/jarFile");
		String[] jarDirs = config.getStringArray("/javaLauncher/jarDir");

		// for classpath
		StringBuffer buffer = new StringBuffer(10240);

		String[] classDirs = config.getStringArray("/javaLauncher/classDir");

		// add directories first
		for (String dir : classDirs) {
			buffer.append(dir).append(pathSeparator);
		}

		// add jar file
		for (String file : jarFiles) {
			buffer.append(file).append(pathSeparator);
			logger.info("jar file - " + file);

			// check 'trace.jar'
			if (file.endsWith(traceJarName)) {
				traceJarLocation = file;
			}
		}

		// search sub directories and add jar file
		for (String dir : jarDirs) {
			buffer.append(searchJarFiles(dir));
		}

		try {
			Runtime rtime = Runtime.getRuntime();

			// fork target(child) process
			Process proc = null;
			if (trace && Validator.isNotEmpty(traceJarLocation)) {
				proc = rtime.exec(StringUtil.concatString("java -Xbootclasspath/a:", traceJarLocation, " -javaagent:",
						traceJarLocation, " -cp ", buffer.toString(), " ", invoke));
			} else {
				proc = rtime.exec(StringUtil.concatString("java -cp ", buffer.toString(), " ", invoke));
			}

			if (waitFor) {
				// stream tracker thread for target process
				class StreamTracker extends Thread {
					private InputStream input = null;

					private boolean runFlag = true;

					public StreamTracker(final InputStream input) {
						super();
						this.input = input;
					}

					public void terminate() {
						runFlag = false;
					}

					public void run() {
						while (runFlag) {
							try {
								sleep(1000);
							} catch (InterruptedException ignored) {
							}

							try {
								byte[] buffer = new byte[input.available()];
								if (buffer.length > 0) {
									input.read(buffer);
									System.out.print(new String(buffer, // NOPMD
											// by
											// Simon
											// Lee
											// on
											// 09.
											// 3. 9
											// ����
											// 1:46
											CharsetUtil.getDefaultEncoding()));
								}
							} catch (IOException e) {
								logger.error(e.toString());
							}
						}
					}
				}

				// redirect target process's standard output/error
				StreamTracker trk1 = new StreamTracker(proc.getInputStream());
				StreamTracker trk2 = new StreamTracker(proc.getErrorStream());
				trk1.start();
				trk2.start();

				// Shutdown Hook for synchronized termination
				new ShutdownHook(proc);

				try {
					proc.waitFor();
				} catch (InterruptedException e) {
					logger.error(e.toString());
				}

				trk1.terminate();
				trk2.terminate();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * find jar/zip files
	 * 
	 * @param path
	 * @return
	 */
	private static String searchJarFiles(final String path) {
		File targetDir = new File(path);

		if (targetDir.isDirectory()) {
			StringBuffer buffer = new StringBuffer(1024);

			File[] targetFiles = targetDir.listFiles();
			for (File target : targetFiles) {
				if (target.isDirectory()) {
					// skip '.' and '..'
					if (!".".equals(target.getName()) && !"..".equals(target.getName())) {
						buffer.append(searchJarFiles(target.getAbsolutePath()));
					}
				} else if (target.getName().toLowerCase().endsWith("jar")
						|| target.getName().toLowerCase().endsWith("zip")) {

					// add all jar/zip files
					buffer.append(target.getAbsolutePath()).append(pathSeparator);
					logger.info("jar file - " + target.getAbsolutePath());

					// check trace.jar
					if (target.getName().equals(traceJarName)) {
						traceJarLocation = target.getAbsolutePath();
					}
				}
			}

			return buffer.toString();
		} else {
			// if it's not a directory, return empty string
			return "";
		}
	}
}

/**
 * Shutdown Hook to destroy child process
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 12. 12
 * @last $Date$
 */
class ShutdownHook extends Thread {
	private transient Process child;

	/**
	 * Constructor
	 * 
	 * @param child
	 */
	public ShutdownHook(final Process child) {
		super();
		Runtime.getRuntime().addShutdownHook(this);
		this.child = child;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		child.destroy();
	}
}