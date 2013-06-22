package lempel.old.framework;

import org.apache.commons.dbcp.*;

import java.io.*;
import java.util.*;
import javax.sql.DataSource;

import lempel.blueprint.log.*;
import lempel.old.framework.PropertyManager;
import lempel.old.framework.XMLNode;

/**
 * DataSource들을 관리<br>
 * <br>
 * Jakarta commons - DBCP 를 사용하였으므로 다음 3개의 jar파일을 classpath에 추가해야 사용할 수 있다 <br>
 * <br>
 * commons-collections.jar <br>
 * commons-dbcp-1.1.jar <br>
 * commons-pool-1.1.jar <br>
 * <br>
 * 환경 설정은 일반 property file과 XML 파일을 지원하며 필요한 변수들은 다음과 같다 <br>
 * <br>- 일반 property file -<br>
 * 다음과 같은 형식의 Property file을 DB_INFO 필드에 포함하는 PropertyManager class를
 * Constructor의 argument로 넘겨 받거나, JDBC 표준의 DataSource객체를 addDataSource(String,
 * DataSource) 메소드로 추가해서 사용한다. <br>
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
 * Property file에 DB_INFO 필드는 다음과 같은 형태로 정의 한다 <br>
 * <br>
 * DB_INFO = db1.properties db2.properties .... <br>
 * <br>
 * 사용할때는 getDataSource(String)으로 꺼내서 사용한다 <br>
 * 반환되는 객체는 JDBC 표준 DataSource 객체다 <br>
 * DataSource.createConnection()으로 Connection객체의 생성이 가능하며 <br>
 * 그렇게 생성한 Connection객체에서 생성한 Statement들은 일일이 close()를 해주지 않아도 <br>
 * Connection.close()를 호출하는 순간 자동으로 close된다 <br>
 * <br>- XML file -<br>
 * 다음과 같은 형식의 XMLNode 객체를 Constructor의 argument로 넘겨 받아야한다. <br>
 * maxActive, maxIdle, validationQuery, evictionInterval은 생략 가능하지만, 안정적인
 * connection 관리를 위해 사용할 것을 권장한다 <br>
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
 * @author 이상민
 * @version $Revision$
 * @create 2004. 05. 21
 * @since 1.5
 * @last $Date$
 * @see
 */
public class DataSourceManager {
	/** log 출력용 헤더 */
	protected String logheader = "DataSourceManager: ";

	/** logger */
	protected Logger logger = null;

	/** DataSource들을 보관하는 map */
	protected HashMap dataSources = null;

	/**
	 * Constructor
	 * 
	 * @author 이상민
	 */
	public DataSourceManager() {
		dataSources = new HashMap();
		logger = Logger.getInstance();
	}

	/**
	 * DataSource들을 초기화
	 * 
	 * @param prop
	 *            환경 변수 (DB_INFO 필드를 포함해야 한다)
	 * @return 초기화 성공 여부
	 * @author 이상민
	 */
	public boolean init(PropertyManager prop) {
		boolean result = false;

		StringTokenizer st = new StringTokenizer(prop.getString("DB_INFO"));
		while (st.hasMoreTokens()) {
			String propertyFileName = st.nextToken();
			try {
				// 프로퍼티 파일을 읽어온다
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
	 * DataSource들을 초기화
	 * 
	 * @param node
	 *            dataSourceInfo node
	 * @return 초기화 성공 여부
	 * @author 이상민
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
	 * 환경변수를 참조해서 DataSource를 생성한다
	 * 
	 * @param prop
	 *            환경변수
	 * @return 생성된 DataSource
	 * @author 이상민
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
	 * XMLNode를 참조해서 DataSource를 생성한다
	 * 
	 * @param node
	 *            dataSource node
	 * @return 생성된 DataSource
	 * @throws IllegalArgumentException
	 *             node에 필수요소가 들어있지 않은 경우
	 * @author 이상민
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
	 * 원하는 DataSource를 가져온다
	 * 
	 * @param key
	 *            DataSource의 key
	 * @return 원하는 DataSource. 없을 경우 null
	 * @author 이상민
	 */
	public DataSource getDataSource(String key) {
		DataSource result = null;
		if (containsKey(key))
			result = (DataSource) dataSources.get(key);
		return result;
	}

	/**
	 * 해당 key의 DataSource가 있는가 확인
	 * 
	 * @param key
	 *            DataSource의 key
	 * @return 존재 여부
	 * @author 이상민
	 */
	public boolean containsKey(String key) {
		return dataSources.containsKey(key);
	}

	/**
	 * DataSource를 추가
	 * 
	 * @param key
	 *            DataSource의 key
	 * @param source
	 *            추가할 DataSource
	 * @author 이상민
	 */
	public void addDataSource(String key, DataSource source) {
		synchronized (dataSources) {
			dataSources.put(key, source);
		}
	}

	/**
	 * 등록된 전체 DataSource의 수를 반환
	 * 
	 * @return 등록된 전체 DataSource의 수
	 * @author 이상민
	 */
	public int size() {
		synchronized (dataSources) {
			return dataSources.size();
		}
	}
}