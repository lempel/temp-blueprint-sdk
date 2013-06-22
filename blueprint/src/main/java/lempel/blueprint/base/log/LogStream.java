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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import lempel.blueprint.base.util.StringUtil;

/**
 * Extended PrintStream for logger.<br>
 * Prints timestamp always.<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2002. 07. 12
 * @last $Date$
 */
public class LogStream extends PrintStream {
	protected transient Calendar cal;

	/** Time Stamp */
	protected transient byte[] timeStamp = new byte[21];

	public LogStream(final OutputStream stream) {
		super(stream);
	}

	public LogStream(final String fileName) throws FileNotFoundException {
		super(new FileOutputStream(fileName));
	}

	public void println(final boolean val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final char val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final int val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final long val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final float val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final double val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final char[] val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final String val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void println(final Object val) {
		synchronized (this) {
			cal = Calendar.getInstance();
			printTimeStamp();
			super.println(val);
		}
	}

	public void printTimeStamp() {
		timeStamp[0] = '[';
		timeStamp[3] = '/';
		timeStamp[6] = ' ';
		timeStamp[9] = ':';
		timeStamp[12] = ':';
		timeStamp[15] = '.';
		timeStamp[19] = ']';
		timeStamp[20] = ' ';

		byte[] tempArray;
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.MONTH) + 1), 2).getBytes();
		System.arraycopy(tempArray, 0, timeStamp, 1, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2).getBytes();
		System.arraycopy(tempArray, 0, timeStamp, 4, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)), 2).getBytes();
		System.arraycopy(tempArray, 0, timeStamp, 7, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.MINUTE)), 2).getBytes();
		System.arraycopy(tempArray, 0, timeStamp, 10, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.SECOND)), 2).getBytes();
		System.arraycopy(tempArray, 0, timeStamp, 13, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.MILLISECOND)), 3).getBytes();
		System.arraycopy(tempArray, 0, timeStamp, 16, 3);

		super.print(new String(timeStamp));
	}

	@Override
	protected void finalize() throws Throwable {
		cal = null;
		timeStamp = null;

		super.finalize();
	}
}