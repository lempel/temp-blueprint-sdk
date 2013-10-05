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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import blueprint.sdk.logger.LogLevel;
import blueprint.sdk.logger.Logger;


/**
 * table handler
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @since 2009. 9. 7.
 * @last $Date$
 */
public class TableHandler {
	protected static final Logger LOGGER = Logger.getInstance();

	protected Statement stmt;

	public TableHandler(final Connection con) throws SQLException {
		stmt = con.createStatement();
	}

	/**
	 * @param databaseType
	 *            (see DatabaseType)
	 * @param schemaName
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public boolean dropTable(final int databaseType, final String schemaName, final String tableName)
			throws SQLException {
		String sql = "";

		// TODO support more database types
		switch (databaseType) {
		default:
			if (schemaName != null && schemaName.length() > 0) {
				sql = "drop table " + schemaName + ".\"" + tableName + '\"';
			} else {
				sql = "drop table \"" + tableName + '\"';
			}
			break;
		}

		return stmt.execute(sql);
	}

	/**
	 * @param databaseType
	 *            (see DatabaseType)
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public boolean createTable(final int databaseType, final TableInfo table) throws SQLException {
		StringBuilder builder = new StringBuilder(1024);

		// TODO support more database types
		switch (databaseType) {
		default:
			builder.append("create table \"");
			if (table.getSchemaName() != null && table.getSchemaName().length() > 0) {
				builder.append(table.getSchemaName()).append('.');
			}
			builder.append(table.getTableName()).append('\"');

			builder.append(" (\n");
			for (int c = 0; c < table.getColumns().length; c++) {
				ColumnInfo column = table.getColumns()[c];
				if (c > 0) {
					builder.append(",\n");
				}
				builder.append('\"');
				builder.append(column.getName());
				builder.append("\" ");
				builder.append(ColumnInfo.getTypeName(DatabaseType.DB2, column.getType()));
				if (column.getType() == Types.CHAR || column.getType() == Types.VARCHAR) {
					builder.append('(');
					builder.append(column.getLength());
					builder.append(')');
				} else if (column.getType() == Types.DECIMAL) {
					builder.append('(');
					builder.append(column.getLength());
					builder.append(',');
					builder.append(column.getDigits());
					builder.append(')');
				} else if (column.getType() == Types.FLOAT || column.getType() == Types.DOUBLE) {
					builder.append('(');
					builder.append(column.getDigits());
					builder.append(',');
					builder.append(column.getRadix());
					builder.append(')');
				}
				if (!column.isNullable()) {
					builder.append(" NOT NULL");
				}
			}

			if (table.getKeys() != null && table.getKeys().length > 0) {
				builder.append("primary key (");

				String[] keys = table.getKeys();
				for (int i = 0; i < keys.length - 1; i++) {
					builder.append('\"');
					builder.append(keys[i]).append("\", ");
				}
				builder.append('\"');
				builder.append(keys[keys.length - 1]);
				builder.append('\"');

				builder.append(")\n");
			}

			builder.append("\n)");
			break;
		}

		boolean result = false;
		try {
			result = stmt.execute(builder.toString());
		} catch (SQLException e) {
			LOGGER.println(LogLevel.SQL, "Can't create table - " + table.getTableName());
			LOGGER.println(LogLevel.SQL, builder.toString());
			throw e;
		}
		return result;
	}
}
