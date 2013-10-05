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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import blueprint.sdk.logger.Logger;


/**
 * Copy table/index/sequence from source to target
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @since 2009. 9. 7.
 * @last $Date$
 */
public class Copier {
	private static final Logger LOGGER = Logger.getInstance();

	private Connection srcCon;
	private Connection targetCon;
	private boolean createTable;
	private boolean dropTable;

	/**
	 * Constructor
	 * 
	 * @param srcCon
	 * @param targetCon
	 * @param create
	 *            set to create new object
	 * @param drop
	 *            set to drop existing object
	 */
	public Copier(final Connection srcCon, final Connection targetCon, boolean create, boolean drop) {
		this.srcCon = srcCon;
		this.targetCon = targetCon;
		this.createTable = create;
		this.dropTable = drop;
	}

	public Connection getSrcCon() {
		return srcCon;
	}

	public Connection getTargetCon() {
		return targetCon;
	}

	public boolean isCreateTable() {
		return createTable;
	}

	public boolean isDropTable() {
		return dropTable;
	}

	/**
	 * copy tables
	 * 
	 * @param databaseType
	 *            (see DatabaseType)
	 * @param srcSchemaName
	 * @param targetSchemaName
	 * @param tableNames
	 * @throws SQLException
	 *             many things can go wrong...
	 * @throws IOException
	 *             can't read LOB
	 */
	public void copyTables(final int databaseType, final String srcSchemaName, final String targetSchemaName,
			final String... tableNames) throws SQLException, IOException {
		MetaDataHelper meta = new MetaDataHelper(getSrcCon());
		TableHandler targetHandler = new TableHandler(getTargetCon());

		// TODO implement Index, Sequence copy

		Statement srcStmt = getSrcCon().createStatement();

		for (String tableName : tableNames) {
			TableInfo tableInfo = meta.getTableInfo(srcSchemaName, tableName);
			ColumnInfo[] columns = tableInfo.getColumns();

			int columnCount = columns.length;
			if (columnCount > 0) {
				StringBuilder builder = new StringBuilder(1024);
				builder.append("insert into \"");
				builder.append(tableName);
				builder.append("\" values (");
				for (int i = 0; i < columnCount - 1; i++) {
					builder.append("?,");
				}
				builder.append("?)");

				PreparedStatement targetStmt = getTargetCon().prepareStatement(builder.toString());

				if (isDropTable()) {
					try {
						targetHandler.dropTable(databaseType, targetSchemaName, tableName);
					} catch (SQLException ignored) {
						// table may not exists
					}
				}
				if (isCreateTable()) {
					targetHandler.createTable(databaseType, tableInfo);
				}

				ResultSet rset;
				if (srcSchemaName != null && srcSchemaName.length() > 0) {
					rset = srcStmt.executeQuery("select * from " + srcSchemaName + ".\"" + tableName + "\"");
				} else {
					rset = srcStmt.executeQuery("select * from \"" + tableName + "\"");
				}

				int rowCount = 0;
				while (rset.next()) {
					for (ColumnInfo column : columns) {
						int index = column.getIndex();
						String name = column.getName();
						int type = column.getType();

						// TODO support more types
						switch (type) {
						case Types.DATE:
							targetStmt.setDate(index, rset.getDate(name));
							break;
						case Types.DECIMAL:
							targetStmt.setBigDecimal(index, rset.getBigDecimal(name));
							break;
						case Types.NUMERIC:
						case Types.INTEGER:
							targetStmt.setInt(index, rset.getInt(name));
							break;
						case Types.FLOAT:
							targetStmt.setFloat(index, rset.getFloat(name));
							break;
						case Types.DOUBLE:
							targetStmt.setDouble(index, rset.getDouble(name));
							break;
						case Types.TIME:
							targetStmt.setTime(index, rset.getTime(name));
							break;
						case Types.TIMESTAMP:
							targetStmt.setTimestamp(index, rset.getTimestamp(name));
							break;
						case Types.BLOB:
							targetStmt.setBlob(index, rset.getBlob(name));
							break;
						case Types.CLOB:
							targetStmt.setClob(index, rset.getClob(name));
							break;
						default:
							targetStmt.setString(index, rset.getString(name));
							break;
						}
					}

					// TODO do more sophisticated exception handling
					targetStmt.executeUpdate();

					rowCount++;
					if (rowCount % 100 == 0) {
						getTargetCon().commit();
					}
				}
			}

			LOGGER.info("table \"" + tableName + "\" copied from \"" + srcSchemaName + "\" to \"" + targetSchemaName
					+ "\"");
			getTargetCon().commit();
		}
	}
}
