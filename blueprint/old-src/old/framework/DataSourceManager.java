package lempel.old.framework;

import org.apache.commons.dbcp.*;

import java.io.*;
import java.util.*;
import javax.sql.DataSource;

import lempel.blueprint.log.*;
import lempel.old.framework.PropertyManager;
import lempel.old.framework.XMLNode;

/**
 * DataSource���� ����<br>
 * <br>
 * Jakarta commons - DBCP �� ����Ͽ����Ƿ� ���� 3���� jar������ classpath�� �߰��ؾ� ����� �� �ִ� <br>
 * <br>
 * commons-collections.jar <br>
 * commons-dbcp-1.1.jar <br>
 * commons-pool-1.1.jar <br>
 * <br>
 * ȯ�� ������ �Ϲ� property file�� XML ������ �����ϸ� �ʿ��� �������� ������ ���� <br>
 * <br>- �Ϲ� property file -<br>
 * ������ ���� ������ Property file�� DB_INFO �ʵ忡 �����ϴ� PropertyManager class��
 * Constructor�� argument�� �Ѱ� �ްų�, JDBC ǥ���� DataSource��ü�� addDataSource(String,
 * DataSource) �޼ҵ�� �߰��ؼ� ����Ѵ�. <br>
 * <br>
 * JDBC_ID = db1 <br>
 * JDBC_CLASS = org.postgresql.Driver <br>
 * JDBC_URI = jdbc:postgresql://127.0.0.1/db1 <br>
 * JDBC_USER = db1 <br>
 * JDBC_PASSWORD = password1 <br>
 * JDBC_MAX_ACTIVE = 5 <br>
 * JDBC_MAX_IDLE = 2 <br>
 * JDBC_VALIDATION_QUERY = select count(*) from dummy_table <br>
 * JDBC_EVICTION_INTERVAL = 600000 <br>
 * <br>
 * Property file�� DB_INFO �ʵ�� ������ ���� ���·� ���� �Ѵ� <br>
 * <br>
 * DB_INFO = db1.properties db2.properties .... <br>
 * <br>
 * ����Ҷ��� getDataSource(String)���� ������ ����Ѵ� <br>
 * ��ȯ�Ǵ� ��ü�� JDBC ǥ�� DataSource ��ü�� <br>
 * DataSource.createConnection()���� Connection��ü�� ������ �����ϸ� <br>
 * �׷��� ������ Connection��ü���� ������ Statement���� ������ close()�� ������ �ʾƵ� <br>
 * Connection.close()�� ȣ���ϴ� ���� �ڵ����� close�ȴ� <br>
 * <br>- XML file -<br>
 * ������ ���� ������ XMLNode ��ü�� Constructor�� argument�� �Ѱ� �޾ƾ��Ѵ�. <br>
 * maxActive, maxIdle, validationQuery, evictionInterval�� ���� ����������, ��������
 * connection ������ ���� ����� ���� �����Ѵ� <br>
 * <br>
 * &lt;dataSourceInfo&gt; <br>
 * &lt;dataSource id="test1" class="org.postgresql.Driver" <br>
 * url="jdbc:postgresql://127.0.0.1/test1" user="scott" password="tiger"&gt;
 * <br>
 * &lt;maxActive&gt;5&lt;/maxActive&gt; <br>
 * &lt;maxIdle&gt;2&lt;/maxIdle&gt; <br>
 * &lt;validationQuery&gt;select sysdate from dual&lt;/validationQuery&gt; <br>
 * &lt;evictionInterval&gt;600000&lt;/evictionInterval&gt; <br>
 * &lt;/dataSource&gt; <br>
 * &lt;dataSource id="test2" class="org.postgresql.Driver" <br>
 * url="jdbc:postgresql://127.0.0.1/test2" user="scott" password="tiger"&gt;
 * <br>
 * ... <br>
 * &lt;/dataSource&gt; <br>
 * &lt;/dataSourceInfo&gt; <br>
 * <br>
 * 
 * @author �̻��
 * @version $Revision$
 * @create 2004. 05. 21
 * @since 1.5
 * @last $Date$
 * @see
 */
public class DataSourceManager {
	/** log ��¿� ��� */
	protected String logheader = "DataSourceManager: ";

	/** logger */
	protected Logger logger = null;

	/** DataSource���� �����ϴ� map */
	protected HashMap dataSources = null;

	/**
	 * Constructor
	 * 
	 * @author �̻��
	 */
	public DataSourceManager() {
		dataSources = new HashMap();
		logger = Logger.getInstance();
	}

	/**
	 * DataSource���� �ʱ�ȭ
	 * 
	 * @param prop
	 *            ȯ�� ���� (DB_INFO �ʵ带 �����ؾ� �Ѵ�)
	 * @return �ʱ�ȭ ���� ����
	 * @author �̻��
	 */
	public boolean init(PropertyManager prop) {
		boolean result = false;

		StringTokenizer st = new StringTokenizer(prop.getString("DB_INFO"));
		while (st.hasMoreTokens()) {
			String propertyFileName = st.nextToken();
			try {
				// ������Ƽ ������ �о�´�
				PropertyManager tempProp = new PropertyManager(propertyFileName);

				addDataSource(tempProp.getString("JDBC_ID"),
						createDataSource(tempProp));
			} catch (IOException exIO) {
				logger.println(LogLevel.WAN, logheader
						+ "Can't read db property file [" + propertyFileName
						+ "]");
			}
		}

		return result;
	}

	/**
	 * DataSource���� �ʱ�ȭ
	 * 
	 * @param node
	 *            dataSourceInfo node
	 * @return �ʱ�ȭ ���� ����
	 * @author �̻��
	 */
	public boolean init(XMLNode node) {
		if (node.getName().equals("dataSourceInfo") == false)
			return false;

		try {
			Vector children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				XMLNode childNode = (XMLNode) children.get(i);
				addDataSource(childNode.getAttribute("id"),
						createDataSource(childNode));
			}
		} catch (Exception ex) {
			logger.println(LogLevel.WAN, logheader
					+ "Can't read dataSourceInfoNode - <" + node.getName()
					+ ">");
		}

		return true;
	}

	/**
	 * ȯ�溯���� �����ؼ� DataSource�� �����Ѵ�
	 * 
	 * @param prop
	 *            ȯ�溯��
	 * @return ������ DataSource
	 * @author �̻��
	 */
	protected DataSource createDataSource(PropertyManager prop) {
		BasicDataSource dataSource = new BasicDataSource();

		dataSource.setDriverClassName(prop.getString("JDBC_CLASS"));
		dataSource.setUrl(prop.getString("JDBC_URI"));
		dataSource.setUsername(prop.getString("JDBC_USER"));
		dataSource.setPassword(prop.getString("JDBC_PASSWORD"));
		dataSource.setMaxActive(prop.getInt("JDBC_MAX_ACTIVE"));
		dataSource.setMaxIdle(prop.getInt("JDBC_MAX_IDLE"));
		if (prop.containsKey("JDBC_VALIDATION_QUERY")) {
			dataSource.setValidationQuery(prop
					.getString("JDBC_VALIDATION_QUERY"));
			dataSource.setTimeBetweenEvictionRunsMillis(prop
					.getLong("JDBC_EVICTION_INTERVAL"));
		}

		logger.println(LogLevel.INF, logheader + "DataSource ["
				+ prop.getString("JDBC_ID") + "] created");

		return dataSource;
	}

	/**
	 * XMLNode�� �����ؼ� DataSource�� �����Ѵ�
	 * 
	 * @param node
	 *            dataSource node
	 * @return ������ DataSource
	 * @throws IllegalArgumentException
	 *             node�� �ʼ���Ұ� ������� ���� ���
	 * @author �̻��
	 */
	protected DataSource createDataSource(XMLNode node)
			throws IllegalArgumentException {
		BasicDataSource dataSource = new BasicDataSource();

		try {
			dataSource.setDriverClassName(node.getAttribute("class"));
			dataSource.setUrl(node.getAttribute("url"));
			dataSource.setUsername(node.getAttribute("user"));
			dataSource.setPassword(node.getAttribute("password"));

			if (node.hasChild("maxActive"))
				dataSource.setMaxActive(Integer.parseInt(node.getFirstChild(
						"maxActive").getValue()));
			if (node.hasChild("maxIdle"))
				dataSource.setMaxIdle(Integer.parseInt(node.getFirstChild(
						"maxIdle").getValue()));
			if (node.hasChild("validationQuery"))
				dataSource.setValidationQuery(node.getFirstChild(
						"validationQuery").getValue());
			if (node.hasChild("evictionInterval"))
				dataSource.setTimeBetweenEvictionRunsMillis(Integer
						.parseInt(node.getFirstChild("evictionInterval")
								.getValue()));
		} catch (Exception ex) {
			throw new IllegalArgumentException(
					"wrong argument for createDataSource(XMLNode) - " + ex);
		}

		return dataSource;
	}

	/**
	 * ���ϴ� DataSource�� �����´�
	 * 
	 * @param key
	 *            DataSource�� key
	 * @return ���ϴ� DataSource. ���� ��� null
	 * @author �̻��
	 */
	public DataSource getDataSource(String key) {
		DataSource result = null;
		if (containsKey(key))
			result = (DataSource) dataSources.get(key);
		return result;
	}

	/**
	 * �ش� key�� DataSource�� �ִ°� Ȯ��
	 * 
	 * @param key
	 *            DataSource�� key
	 * @return ���� ����
	 * @author �̻��
	 */
	public boolean containsKey(String key) {
		return dataSources.containsKey(key);
	}

	/**
	 * DataSource�� �߰�
	 * 
	 * @param key
	 *            DataSource�� key
	 * @param source
	 *            �߰��� DataSource
	 * @author �̻��
	 */
	public void addDataSource(String key, DataSource source) {
		synchronized (dataSources) {
			dataSources.put(key, source);
		}
	}

	/**
	 * ��ϵ� ��ü DataSource�� ���� ��ȯ
	 * 
	 * @return ��ϵ� ��ü DataSource�� ��
	 * @author �̻��
	 */
	public int size() {
		synchronized (dataSources) {
			return dataSources.size();
		}
	}
}