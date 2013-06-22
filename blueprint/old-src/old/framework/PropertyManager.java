package lempel.old.framework;

import java.io.*;
import java.util.*;

/**
 * 각종 환경 변수를 관리 <br>
 * return value에 따른 여러 타입의 get method를 지원하며 <br>
 * get method에서 NullPointerException이 발생할 경우 <br>
 * 어떤 key에서 발생했는가를 알려준다.
 * 
 * @author Sang-min Lee
 * @since 2001.1.13.
 * @version 2005.1.12.
 */
public class PropertyManager extends Properties {
	/** */
	private static final long serialVersionUID = -1315364274477982026L;
	
	/** log 출력용 헤더 */
	protected String _header = "PropertyManager: ";

	public PropertyManager() {
		super();
	}

	/**
	 * @param fileName
	 *            property file 이름
	 * @throws IOException
	 *             file read중 오류 발생
	 * @throws FileNotFoundException
	 *             해당 파일이 없는 경우
	 */
	public PropertyManager(String fileName) throws IOException,
			FileNotFoundException {
		FileInputStream fi = null;
		fi = new FileInputStream(fileName);
		load(fi);
		fi.close();
	}

	/**
	 * Maps the specified key to the specified value in this hashtable.
	 * 
	 * @param key
	 *            the hashtable key.
	 * @param value
	 *            the value.
	 */
	public Object put(Object key, Object value) {
		Object result = null;
		if (value == null)
			result = super.put(key, "null");
		else
			result = super.put(key, value);
		return result;
	}

	/**
	 * key에 해당하는 String Object를 반환
	 * 
	 * @param key
	 *            hashtable key
	 * @return key에 해당하는 String Object
	 * @throws NullPointerException
	 *             key가 null이거나 value가 null
	 */
	public String getString(Object key) throws NullPointerException {
		String temp = "";

		try {
			temp = get(key).toString().trim();
		} catch (NullPointerException ex) {
			System.out.println("!! " + _header + "getString(\"" + key
					+ "\") returns null !!");
			throw ex;
		}

		return temp;
	}

	/**
	 * key에 해당하는 int value를 반환
	 * 
	 * @param key
	 *            hashtable key
	 * @return key에 해당하는 int value
	 * @throws NullPointerException
	 *             key가 null이거나 value가 null
	 */
	public int getInt(Object key) throws NullPointerException {
		String temp = "";

		try {
			temp = get(key).toString().trim();
		} catch (NullPointerException ex) {
			System.out.println("!! " + _header + "getInt(\"" + key
					+ "\") returns null !!");
			throw new NullPointerException();
		}

		return Integer.parseInt(temp.trim());
	}

	/**
	 * key에 해당하는 long value를 반환
	 * 
	 * @param key
	 *            hashtable key
	 * @return key에 해당하는 long value
	 * @throws NullPointerException
	 *             key가 null이거나 value가 null
	 */
	public long getLong(Object key) throws NullPointerException {
		String temp = "";

		try {
			temp = get(key).toString().trim();
		} catch (NullPointerException ex) {
			System.out.println("!! " + _header + "getLong(\"" + key
					+ "\") returns null !!");
			throw new NullPointerException();
		}

		return Long.parseLong(temp.trim());
	}

	/**
	 * key에 해당하는 boolean value를 반환
	 * 
	 * @param key
	 *            hashtable key
	 * @return key에 해당하는 boolean value
	 */
	public boolean getBoolean(Object key) {
		String temp = "";

		try {
			temp = get(key).toString().trim();
		} catch (NullPointerException ex) {
			System.out.println("!! " + _header + "getBoolean(\"" + key
					+ "\") returns null !!");
			throw ex;
		}

		boolean result = false;
		if (temp.toUpperCase().compareTo("TRUE") == 0)
			result = true;
		return result;
	}

	/**
	 * key에 해당하는 Map Object를 반환
	 * 
	 * @param key
	 *            hashtable key
	 * @return key에 해당하는 Map Object
	 */
	public Map getMap(Object key) {
		Object temp = null;
		Map result = null;

		try {
			temp = get(key);
		} catch (NullPointerException ex) {
			System.out.println("!! " + _header + "getMap(\"" + key
					+ "\") returns null !!");
			throw ex;
		}

		if (temp instanceof Map) {
			result = (Map) temp;
		} else {
			System.out.println("!! " + _header + "getMap(\"" + key
					+ "\") returns illegal type !!");
		}

		return result;
	}

	/**
	 * key에 해당하는 List Object를 반환
	 * 
	 * @param key
	 *            hashtable key
	 * @return key에 해당하는 List Object
	 */
	public List getList(Object key) {
		Object temp = null;
		List result = null;

		try {
			temp = get(key);
		} catch (NullPointerException ex) {
			System.out.println("!! " + _header + "getList(\"" + key
					+ "\") returns null !!");
			throw ex;
		}

		if (temp instanceof List) {
			result = (List) temp;
		} else {
			System.out.println("!! " + _header + "getList(\"" + key
					+ "\") returns illegal type !!");
		}

		return result;
	}
}