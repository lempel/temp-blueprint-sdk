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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lempel.blueprint.base.util.Validator;

/**
 * Creates java.sql.Connection instance for MDB file(MS Access)
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 08. 07
 * @last $Date$
 */
public class MdbConnector {
	private static final String JDBC_DRIVER_CLASS = "sun.jdbc.odbc.JdbcOdbcDriver";
	private static final String EMPTY_STRING = "";
	private static final String URL_PREFIX = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";

	private transient Connection connection = null;

	public boolean connect(final String mdbPath) throws ClassNotFoundException {
		// Initialize the JdbcOdbc Bridge Driver
		Class.forName(JDBC_DRIVER_CLASS);

		boolean result = false;
		try {
			// DSN & Property set
			connection = getAccessDBConnection(mdbPath);
			result = true;
		} catch (SQLException se) {
			close();
		}

		return result;
	}

	private Connection getAccessDBConnection(final String strDBPath) throws SQLException {
		String path = strDBPath.replace('\\', '/').trim();
		String databaseURL = URL_PREFIX + path;

		return DriverManager.getConnection(databaseURL, EMPTY_STRING, EMPTY_STRING);
	}

	public Connection getConnection() {
		return connection;
	}

	public void close() {
		try {
			if (Validator.isNotNull(connection)) {
				connection.close();
			}
		} catch (SQLException ignored) {
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();

		super.finalize();
	}
}