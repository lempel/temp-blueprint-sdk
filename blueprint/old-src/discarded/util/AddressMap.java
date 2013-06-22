package lempel.blueprint.util;

import java.util.*;

/**
 * IP�ּҸ� �����ϴ� Map <br>
 * <br>
 * put(String)�� putIp(String)�� ����Ͽ� IP�� �߰� �Ѵ� <br>
 * put�� ��� ","�� �����Ͽ� �������� IP�� ������ �� ������ <br>
 * putIp�� ���� �Ѱ��� IP�� �߰� �Ҷ� ����Ѵ� <br>
 * IP�� *�� ����ŷ�� �� �� �ִ� <br>
 * ex) 192.168.*.*, 127.*.*.*, 203.248.46.* <br>
 * <br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2004. 05. 17
 * @since 1.5
 * @last $Date$
 * @see
 * @deprecated
 */
//FIXME ���ۼ�
public class AddressMap {
	/** String ��� : "," */
	private static final String COMMA = ",";
	/** String ��� : "." */
	private static final String DOT = ".";
	/** String ��� : "*" */
	private static final String ASTERISK = "*";
	/** String ��� : "" */
	private static final String EMPTY_STRING = "";

	/** IP���� ���� */
	@SuppressWarnings("unchecked")
	protected HashMap _map = null;

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public AddressMap() {
		_map = new HashMap();
	}

	/**
	 * Constructor
	 * 
	 * @param addr
	 *            IP�ּ� ('*' ��뵵 ����. �������� IP�� ','�� �и����ش�)
	 */
	public AddressMap(String addr) {
		this();

		// $ANALYSIS-IGNORE
		put(addr);
	}

	/**
	 * �ٸ� Map�� �ִ� ������ ��� �����´�
	 * 
	 * @param t
	 *            ������ Map
	 */
	@SuppressWarnings("unchecked")
	public void putAll(Map t) {
		_map.putAll(t);
	}

	/**
	 * map�� ����ִ°� Ȯ��
	 * 
	 * @return map�� ����°��� ����
	 */
	public boolean isEmpty() {
		return (_map.size() > 0) ? true : false;
	}

	/**
	 * �ش� IP�� ���ԵǴ°� �˻�
	 * 
	 * @param key
	 *            �˻��� IP
	 * @return ���� ����
	 */
	@SuppressWarnings("unchecked")
	public boolean containsKey(String key) {
		StringTokenizer st = new StringTokenizer(key, DOT);
		String token = null;
		HashMap parentMap = _map;
		HashMap tempMap = null;

		while (st.hasMoreTokens()) {
			token = st.nextToken();
			if (parentMap.containsKey(ASTERISK)) {
				// * �� ��������� �� �̻� �˻��� �ʿ䰡 ����
				return true;
			} else if (parentMap.containsKey(token)) {
				// ���� class�� �߰ߵȴٸ� ��� ����
				tempMap = (HashMap) parentMap.get(token);
			} else {
				// �ش� key�� ������� �ʴ�
				return false;
			}

			parentMap = tempMap;
		}

		return true;
	}

	/**
	 * �ش� IP�� �߰� (* ��� ����)
	 * 
	 * @param key
	 *            �߰��� IP (',' �� �����ؼ� ������ ��� ����)
	 */
	public void put(String key) {
		StringTokenizer st = null;

		if (key.indexOf(COMMA) > -1) {
			// ','�� ������ ip���� �и��ؼ� ó��
			st = new StringTokenizer(key, COMMA);
			while (st.hasMoreTokens()) {
				putIp(st.nextToken().trim());
			}
		} else {
			putIp(key);
		}
	}

	/**
	 * �ش� IP�� �߰� (* ��� ����)
	 * 
	 * @param key
	 *            �߰��� IP �ϳ�
	 */
	@SuppressWarnings("unchecked")
	private void putIp(String key) {
		StringTokenizer st = new StringTokenizer(key, DOT);
		String token = null;
		HashMap parentMap = _map;
		HashMap tempMap = null;
		boolean breakFlag = true;

		while (st.hasMoreTokens() && breakFlag) {
			token = st.nextToken();
			if (parentMap.containsKey(ASTERISK)) {
				// '*'�� �̹� ������ �߰��� �ʿ䰡 ����
				breakFlag = false;
			} else if (parentMap.containsKey(token)) {
				// �̹� ���� class�� ����� �Ǿ������� �����´�
				tempMap = (HashMap) parentMap.get(token);
			} else {
				if (!st.hasMoreTokens() || ASTERISK.equals(token)) {
					// D-class�̰ų� �߰��� class������ '*'�̶�� ���⼭ ��
					parentMap.put(token, EMPTY_STRING);
					breakFlag = false;
				} else {
					// ���� class�� �߰��ϰ� ��� ����
					tempMap = new HashMap();
					parentMap.put(token, tempMap);
				}
			}

			parentMap = tempMap;
		}
	}
}