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

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;

import bluerpint.sdk.util.StringUtil;

/**
 * Collection of methods for date & time handling
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2004. 10. 25
 * @last $Date$
 */
public class DateTime implements Serializable {
	private static final long serialVersionUID = 2356507345448836939L;

	private int year = 0;
	private int month = 0;
	private int day = 0;
	private int hour = 0;
	private int min = 0;
	private int sec = 0;

	/**
	 * Constructor
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param min
	 */
	public DateTime(final int year, final int month, final int day, final int hour, final int min) {
		this(year, month, day, hour, min, 0);
	}

	/**
	 * Constructor
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param min
	 * @param sec
	 */
	public DateTime(final int year, final int month, final int day, final int hour, final int min, final int sec) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.min = min;
		this.sec = sec;
	}

	/**
	 * @param dateStr
	 *            Formatted String - yyyymmddhhMM
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	public DateTime(final String dateStr) throws NullPointerException, ParseException, NumberFormatException {
		if (dateStr == null) {
			throw new NumberFormatException("Null argument for DateTime(String)");
		}
		if ((dateStr.length() != 12) && (dateStr.length() != 14)) {
			throw new NumberFormatException("Argument must be 'yyyymmddhhMM' or 'yyyymmddhhMMss'");
		}

		year = NumberFormat.getNumberInstance().parse(dateStr.substring(0, 4)).intValue();
		month = NumberFormat.getNumberInstance().parse(dateStr.substring(4, 6)).intValue();
		day = NumberFormat.getNumberInstance().parse(dateStr.substring(6, 8)).intValue();
		hour = NumberFormat.getNumberInstance().parse(dateStr.substring(8, 10)).intValue();
		min = NumberFormat.getNumberInstance().parse(dateStr.substring(10, 12)).intValue();
		if (dateStr.length() == 14) {
			sec = NumberFormat.getNumberInstance().parse(dateStr.substring(12, 14)).intValue();
		}

		if (month == 0 || month > 12) {
			throw new ParseException("month must be 1~12", 2);
		}
		if (day == 0 || day > 31) {
			throw new ParseException("day must be 1~31", 2);
		}
		if (hour > 23) {
			throw new ParseException("hour must be 0~23", 2);
		}
		if (min > 59) {
			throw new ParseException("min must be 0~59", 2);
		}
		if (sec > 59) {
			throw new ParseException("sec must be 0~59", 2);
		}
	}

	public DateTime(final Calendar cal) {
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH) + 1;
		day = cal.get(Calendar.DAY_OF_MONTH);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		min = cal.get(Calendar.MINUTE);
		sec = cal.get(Calendar.SECOND);
	}

	/**
	 * Compares to target with 1 minute precision
	 * 
	 * @param target
	 * @return 1: target is future, -1: target is past, 0: equal
	 */
	public int compareTo(final DateTime target) {
		int result = 0;
		// messy, but fast
		if (getYear() < target.getYear()) {
			result = 1;
		} else if (getYear() > target.getYear()) {
			result = -1;
		} else if (getMonth() < target.getMonth()) {
			result = 1;
		} else if (getMonth() > target.getMonth()) {
			result = -1;
		} else if (getDay() < target.getDay()) {
			result = 1;
		} else if (getDay() > target.getDay()) {
			result = -1;
		} else if (getHour() < target.getHour()) {
			result = 1;
		} else if (getHour() > target.getHour()) {
			result = -1;
		} else if (getMin() < target.getMin()) {
			result = 1;
		} else if (getMin() > target.getMin()) {
			result = -1;
		} else if (getSec() < target.getSec()) {
			result = 1;
		} else if (getSec() > target.getSec()) {
			result = -1;
		}

		return result;
	}

	public Calendar toCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, getYear());
		cal.set(Calendar.MONTH, getMonth() - 1);
		cal.set(Calendar.DAY_OF_MONTH, getDay());
		cal.set(Calendar.HOUR_OF_DAY, getHour());
		cal.set(Calendar.MINUTE, getMin());
		cal.set(Calendar.SECOND, getSec());

		return cal;
	}

	public String toString() {
		StringBuffer result = new StringBuffer(14);
		result.append(StringUtil.lpadZero(Integer.toString(getYear()), 4)).append(
				StringUtil.lpadZero(Integer.toString(getMonth()), 2)).append(
				StringUtil.lpadZero(Integer.toString(getDay()), 2)).append(
				StringUtil.lpadZero(Integer.toString(getHour()), 2)).append(
				StringUtil.lpadZero(Integer.toString(getMin()), 2)).append(
				StringUtil.lpadZero(Integer.toString(getSec()), 2));
		return result.toString();
	}

	public static boolean isValid(final DateTime start, final DateTime end) {
		DateTime now = new DateTime(Calendar.getInstance());
		boolean result = true;
		if (start.compareTo(now) < 0) {
			result = false;
		} else if (end.compareTo(now) > 0) {
			result = false;
		}

		return result;
	}

	public int getYear() {
		return year;
	}

	public void setYear(final int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(final int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(final int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(final int hour) {
		this.hour = hour;
	}

	public int getMin() {
		return min;
	}

	public void setMin(final int min) {
		this.min = min;
	}

	public int getSec() {
		return sec;
	}

	public void setSec(final int sec) {
		this.sec = sec;
	}
}