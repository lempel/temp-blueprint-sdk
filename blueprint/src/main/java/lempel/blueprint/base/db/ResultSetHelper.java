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
package lempel.blueprint.base.db;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Helper for java.sql.ResultSet
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2009. 2. 26.
 * @last $Date$
 */
public class ResultSetHelper {
	private transient ResultSet rset;

	public ResultSetHelper(final ResultSet rset) {
		this.rset = rset;
	}

	public boolean next() throws SQLException {
		return rset.next();
	}

	public void close() throws SQLException {
		rset.close();
	}

	public void wasNull() throws SQLException {
		rset.wasNull();
	}

	public String getString(final int index) throws SQLException {
		String result = rset.getString(index);
		if (result == null) {
			result = "";
		}
		return result.trim();
	}

	public boolean getBoolean(final int index) throws SQLException {
		return rset.getBoolean(index);
	}

	public byte getByte(final int index) throws SQLException {
		return rset.getByte(index);
	}

	public int getInt(final int index) throws SQLException {
		return rset.getInt(index);
	}

	public long getLong(final int index) throws SQLException {
		return rset.getLong(index);
	}

	public float getFloat(final int index) throws SQLException {
		return rset.getFloat(index);
	}

	public double getDouble(final int index) throws SQLException {
		return rset.getDouble(index);
	}

	public BigDecimal getBigDecimal(final int index) throws SQLException {
		return rset.getBigDecimal(index);
	}

	public byte[] getBytes(final int index) throws SQLException {
		return rset.getBytes(index);
	}

	public Date getDate(final int index) throws SQLException {
		return rset.getDate(index);
	}

	public Time getTime(final int index) throws SQLException {
		return rset.getTime(index);
	}

	public Timestamp getTimestamp(final int index) throws SQLException {
		return rset.getTimestamp(index);
	}

	public InputStream getAsciiStream(final int index) throws SQLException {
		return rset.getAsciiStream(index);
	}

	public InputStream getBinaryStream(final int index) throws SQLException {
		return rset.getBinaryStream(index);
	}

	public String getString(final String index) throws SQLException {
		String result = rset.getString(index);
		if (result == null) {
			result = "";
		}
		return result.trim();
	}

	public boolean getBoolean(final String index) throws SQLException {
		return rset.getBoolean(index);
	}

	public byte getByte(final String index) throws SQLException {
		return rset.getByte(index);
	}

	public int getInt(final String index) throws SQLException {
		return rset.getInt(index);
	}

	public long getLong(final String index) throws SQLException {
		return rset.getLong(index);
	}

	public float getFloat(final String index) throws SQLException {
		return rset.getFloat(index);
	}

	public double getDouble(final String index) throws SQLException {
		return rset.getDouble(index);
	}

	public BigDecimal getBigDecimal(final String index) throws SQLException {
		return rset.getBigDecimal(index);
	}

	public byte[] getBytes(final String index) throws SQLException {
		return rset.getBytes(index);
	}

	public Date getDate(final String index) throws SQLException {
		return rset.getDate(index);
	}

	public Time getTime(final String index) throws SQLException {
		return rset.getTime(index);
	}

	public Timestamp getTimestamp(final String index) throws SQLException {
		return rset.getTimestamp(index);
	}

	public InputStream getAsciiStream(final String index) throws SQLException {
		return rset.getAsciiStream(index);
	}

	public InputStream getBinaryStream(final String index) throws SQLException {
		return rset.getBinaryStream(index);
	}

	public SQLWarning getWarnings() throws SQLException {
		return rset.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		rset.clearWarnings();
	}

	public String getCursorName() throws SQLException {
		return rset.getCursorName();
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return rset.getMetaData();
	}

	public Object getObject(final int index) throws SQLException {
		return rset.getObject(index);
	}

	public Object getObject(final String index) throws SQLException {
		return rset.getObject(index);
	}

	public int findColumn(final String index) throws SQLException {
		return rset.findColumn(index);
	}

	public Reader getCharacterStream(final int index) throws SQLException {
		return rset.getCharacterStream(index);
	}

	public Reader getCharacterStream(final String index) throws SQLException {
		return rset.getCharacterStream(index);
	}

	public boolean first() throws SQLException {
		return rset.first();
	}

	public boolean last() throws SQLException {
		return rset.last();
	}

	public int getRow() throws SQLException {
		return rset.getRow();
	}

	public boolean previous() throws SQLException {
		return rset.previous();
	}

	public void setFetchDirection(final int dir) throws SQLException {
		rset.setFetchDirection(dir);
	}

	public int getFetchDirection() throws SQLException {
		return rset.getFetchDirection();
	}

	public void setFetchSize(final int dir) throws SQLException {
		rset.setFetchSize(dir);
	}

	public int getFetchSize() throws SQLException {
		return rset.getFetchSize();
	}

	public Statement getStatement() throws SQLException {
		return rset.getStatement();
	}

	public Blob getBlob(final int index) throws SQLException {
		return rset.getBlob(index);
	}

	public Clob getClob(final int index) throws SQLException {
		return rset.getClob(index);
	}

	public Blob getBlob(final String index) throws SQLException {
		return rset.getBlob(index);
	}

	public Clob getClob(final String index) throws SQLException {
		return rset.getClob(index);
	}

	public ResultSet getRset() {
		return rset;
	}

	public void setRset(final ResultSet rset) {
		this.rset = rset;
	}
}