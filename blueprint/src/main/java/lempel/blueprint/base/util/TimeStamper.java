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

import java.util.Calendar;

/**
 * @author Simon Lee
 * @version $Revision$
 * @since 2002. 07. 30
 * @last $Date$
 */
public class TimeStamper {
	protected static Calendar now = null;

	/**
	 * @return yyyymmdd
	 */
	public static String getDateStamp() {
		now = Calendar.getInstance();
		byte[] tempArray;
		byte[] stamp = new byte[8];
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.YEAR)), 4).getBytes();
		System.arraycopy(tempArray, 0, stamp, 0, 4);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.MONTH) + 1), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 4, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.DAY_OF_MONTH)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 6, 2);

		return new String(stamp);
	}

	/**
	 * @param days
	 *            +, - days
	 * @return yyyymmdd
	 */
	public static String getDateStampAfter(final int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, days);
		byte[] tempArray;
		byte[] stamp = new byte[8];
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.YEAR)), 4).getBytes();
		System.arraycopy(tempArray, 0, stamp, 0, 4);
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.MONTH) + 1), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 4, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 6, 2);

		return new String(stamp);
	}

	/**
	 * @return yyyymmdd
	 */
	public static String tomorrow() {
		return getDateStampAfter(1);
	}

	/**
	 * @return hhMMss
	 */
	public static String getTimeStamp() {
		return getTimeStamp6();
	}

	/**
	 * @return hh
	 */
	public static String getTimeStamp2() {
		now = Calendar.getInstance();
		byte[] tempArray;
		byte[] stamp = new byte[2];
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.HOUR_OF_DAY)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 0, 2);

		return new String(stamp);
	}

	/**
	 * @return hhMM
	 */
	public static String getTimeStamp4() {
		now = Calendar.getInstance();
		byte[] tempArray;
		byte[] stamp = new byte[4];
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.HOUR_OF_DAY)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 0, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.MINUTE)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 2, 2);

		return new String(stamp);
	}

	/**
	 * @return hhMMss
	 */
	public static String getTimeStamp6() {
		now = Calendar.getInstance();
		byte[] tempArray;
		byte[] stamp = new byte[6];
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.HOUR_OF_DAY)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 0, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.MINUTE)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 2, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.SECOND)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 4, 2);

		return new String(stamp);
	}

	/**
	 * @return hh:MM:ss
	 */
	public static String getTimeStamp8() {
		now = Calendar.getInstance();
		byte[] tempArray;
		byte[] stamp = new byte[8];
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.HOUR_OF_DAY)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 0, 2);
		stamp[2] = ':';
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.MINUTE)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 3, 2);
		stamp[5] = ':';
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.SECOND)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 6, 2);

		return new String(stamp);
	}

	/**
	 * @return hhMMssSSS
	 */
	public static String getTimeStamp9() {
		now = Calendar.getInstance();
		byte[] tempArray;
		byte[] stamp = new byte[9];
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.HOUR_OF_DAY)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 0, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.MINUTE)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 2, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.SECOND)), 2).getBytes();
		System.arraycopy(tempArray, 0, stamp, 4, 2);
		tempArray = StringUtil.lpadZero(Integer.toString(now.get(Calendar.MILLISECOND)), 3).getBytes();
		System.arraycopy(tempArray, 0, stamp, 6, 3);

		return new String(stamp);
	}
}