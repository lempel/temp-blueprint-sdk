package lempel.blueprint.util;

import java.util.*;

/**
 * IP주소를 관리하는 Map <br>
 * <br>
 * put(String)과 putIp(String)을 사용하여 IP를 추가 한다 <br>
 * put의 경우 ","로 구분하여 여러개의 IP를 열거할 수 있으며 <br>
 * putIp의 경우는 한개의 IP만 추가 할때 사용한다 <br>
 * IP는 *로 마스킹을 할 수 있다 <br>
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
//FIXME 재작성
public class AddressMap {
	/** String 상수 : "," */
	private static final String COMMA = ",";
	/** String 상수 : "." */
	private static final String DOT = ".";
	/** String 상수 : "*" */
	private static final String ASTERISK = "*";
	/** String 상수 : "" */
	private static final String EMPTY_STRING = "";

	/** IP들을 저장 */
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
	 *            IP주소 ('*' 사용도 가능. 여러개의 IP는 ','로 분리해준다)
	 */
	public AddressMap(String addr) {
		this();

		// $ANALYSIS-IGNORE
		put(addr);
	}

	/**
	 * 다른 Map에 있는 내용을 모두 가져온다
	 * 
	 * @param t
	 *            가져올 Map
	 */
	@SuppressWarnings("unchecked")
	public void putAll(Map t) {
		_map.putAll(t);
	}

	/**
	 * map이 비어있는가 확인
	 * 
	 * @return map이 비었는가의 여부
	 */
	public boolean isEmpty() {
		return (_map.size() > 0) ? true : false;
	}

	/**
	 * 해당 IP가 포함되는가 검색
	 * 
	 * @param key
	 *            검색할 IP
	 * @return 포함 여부
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
				// * 가 들어있으면 더 이상 검색할 필요가 없다
				return true;
			} else if (parentMap.containsKey(token)) {
				// 다음 class가 발견된다면 계속 진행
				tempMap = (HashMap) parentMap.get(token);
			} else {
				// 해당 key가 들어있지 않다
				return false;
			}

			parentMap = tempMap;
		}

		return true;
	}

	/**
	 * 해당 IP를 추가 (* 사용 가능)
	 * 
	 * @param key
	 *            추가할 IP (',' 로 구별해서 여러개 사용 가능)
	 */
	public void put(String key) {
		StringTokenizer st = null;

		if (key.indexOf(COMMA) > -1) {
			// ','가 있으면 ip별로 분리해서 처리
			st = new StringTokenizer(key, COMMA);
			while (st.hasMoreTokens()) {
				putIp(st.nextToken().trim());
			}
		} else {
			putIp(key);
		}
	}

	/**
	 * 해당 IP를 추가 (* 사용 가능)
	 * 
	 * @param key
	 *            추가할 IP 하나
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
				// '*'가 이미 있으면 추가할 필요가 없다
				breakFlag = false;
			} else if (parentMap.containsKey(token)) {
				// 이미 현재 class가 등록이 되어있으면 꺼내온다
				tempMap = (HashMap) parentMap.get(token);
			} else {
				if (!st.hasMoreTokens() || ASTERISK.equals(token)) {
					// D-class이거나 추가할 class정보가 '*'이라면 여기서 끝
					parentMap.put(token, EMPTY_STRING);
					breakFlag = false;
				} else {
					// 현재 class를 추가하고 계속 진행
					tempMap = new HashMap();
					parentMap.put(token, tempMap);
				}
			}

			parentMap = tempMap;
		}
	}
}