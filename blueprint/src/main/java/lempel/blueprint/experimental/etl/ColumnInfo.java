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

package lempel.blueprint.experimental.etl;

import java.sql.Types;

/**
 * Table column info
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2009. 9. 7.
 * @last $Date$
 */
public class ColumnInfo {
	private int index = -1;
	private String name = null;
	private int type = -1;
	private int length = -1;
	private int digits = -1;
	private int radix = 10;
	private boolean nullable = true;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getDigits() {
		return digits;
	}

	public void setDigits(int digits) {
		this.digits = digits;
	}

	public int getRadix() {
		return radix;
	}

	public void setRadix(int radix) {
		this.radix = radix;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	/**
	 * returns actual type name (for DDL)
	 * 
	 * @param databaseType
	 * @param type
	 * @return
	 */
	public static String getTypeName(int databaseType, int type) {
		String result = "unknown";

		// TODO expand type support
		switch (type) {
		case Types.BIGINT:
			result = "BIGINT";
			break;
		case Types.BINARY:
			result = "BINARY";
			break;
		case Types.BOOLEAN:
			result = "BOOLEAN";
			break;
		case Types.CHAR:
			result = "CHARACTER";
			break;
		case Types.CLOB:
			result = "CLOB";
			break;
		case Types.DATE:
			result = "DATE";
			break;
		case Types.DECIMAL:
			result = "DECIMAL";
			break;
		case Types.DOUBLE:
			result = "DOUBLE";
			break;
		case Types.FLOAT:
			result = "FLOAT";
			break;
		case Types.INTEGER:
			switch (databaseType) {
			case DatabaseType.DB2:
				result = "INTEGER";
				break;
			case DatabaseType.MSSQL:
			default:
				result = "INT";
				break;
			}
			break;
		case Types.NUMERIC:
			result = "NUMERIC";
			break;
		case Types.TIME:
			result = "TIME";
			break;
		case Types.TIMESTAMP:
			result = "TIMESTAMP";
			break;
		case Types.VARCHAR:
			result = "VARCHAR";
			break;
		}

		return result;
	}
}
