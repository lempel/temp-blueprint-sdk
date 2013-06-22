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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import lempel.blueprint.base.concurrent.Terminatable;

/**
 * Log appender for file
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2000. 12. 04
 * @last $Date$
 */
public class FileAppender implements Terminatable, Runnable {
	/** System.out */
	protected transient LogStream outStream = null;
	/** System.err */
	protected transient LogStream errStream = null;

	protected String outFileName = "";
	protected String errFileName = "";

	private transient boolean runFlag = false;

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 * @throws FileNotFoundException
	 */
	public FileAppender(final String outFileName) throws FileNotFoundException {
		this(outFileName, false);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 * @param append
	 * @throws FileNotFoundException
	 */
	public FileAppender(final String outFileName, final boolean append) throws FileNotFoundException {
		this(outFileName, append, false);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 * @param append
	 * @param replaceSystem
	 *            set to replace System Streams (System.out, System.err)
	 * @throws FileNotFoundException
	 */
	public FileAppender(final String outFileName, final boolean append, final boolean replaceSystem)
			throws FileNotFoundException {
		if (outStream == null) {
			outStream = new LogStream(new FileOutputStream(outFileName, append));
			errStream = outStream;
			this.outFileName = outFileName;

			runFlag = true;
		}

		if (replaceSystem) {
			System.setOut(outStream);
			System.setErr(outStream);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 * @param errFileName
	 * @throws FileNotFoundException
	 */
	public FileAppender(final String outFileName, final String errFileName) throws FileNotFoundException {
		this(outFileName, errFileName, false, false);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 * @param errFileName
	 * @param append
	 * @throws FileNotFoundException
	 */
	public FileAppender(final String outFileName, final String errFileName, final boolean append)
			throws FileNotFoundException {
		this(outFileName, errFileName, append, false);
	}

	/**
	 * Constructor
	 * 
	 * @param outFileName
	 * @param errFileName
	 * @param append
	 * @param replaceSystem
	 *            set to replace System Streams (System.out, System.err)
	 * @throws FileNotFoundException
	 */
	public FileAppender(final String outFileName, final String errFileName, final boolean append,
			final boolean replaceSystem) throws FileNotFoundException {
		this(outFileName, append);

		if (errStream == null) {
			errStream = new LogStream(new FileOutputStream(errFileName, append));
			this.errFileName = errFileName;
		}

		if (replaceSystem) {
			System.setErr(errStream);
		}
	}

	public void start() {
		Thread thr = new Thread(this);
		thr.setDaemon(true);
		thr.start();
	}

	public boolean isValid() {
		return runFlag;
	}

	public void terminate() {
		runFlag = false;
	}

	public void run() {
		while (runFlag) {
			if (outFileName.length() > 0) {
				File outFile = new File(outFileName);

				if (!outFile.exists()) {
					outStream.close();
					try {
						outStream = new LogStream(outFileName);
					} catch (FileNotFoundException e) {
						System.out.println("Can't create log file"); // NOPMD by
																		// Simon
																		// Lee
																		// on
																		// 09.
																		// 3. 9
																		// ����
																		// 1:39
					}
				}
			}

			if (errFileName.length() > 0 && !outFileName.equals(errFileName)) {
				File errFile = new File(errFileName);

				if (!errFile.exists()) {
					errStream.close();
					try {
						errStream = new LogStream(errFileName);
					} catch (FileNotFoundException e) {
						System.out.println("Can't create log file"); // NOPMD by
																		// Simon
																		// Lee
																		// on
																		// 09.
																		// 3. 9
																		// ����
																		// 1:39
					}
				}
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException ignored) {
			}
		}
	}
}
