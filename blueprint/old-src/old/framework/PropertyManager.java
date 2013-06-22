package lempel.old.framework;

import java.io.*;
import java.util.*;

/**
 * ���� ȯ�� ������ ���� <br>
 * return value�� ���� ���� Ÿ���� get method�� �����ϸ� <br>
 * get method���� NullPointerException�� �߻��� ��� <br>
 * � key���� �߻��ߴ°��� �˷��ش�.
 * 
 * @author Sang-min Lee
 * @since 2001.1.13.
 * @version 2005.1.12.
 */
public class PropertyManager extends Properties {
	/** */
	private static final long serialVersionUID = -1315364274477982026L;
	
	/** log ��¿� ��� */
	protected String _header = "PropertyManager: ";

	public PropertyManager() {
		super();
	}

	/**
	 * @param fileName
	 *            property file �̸�
	 * @throws IOException
	 *             file read�� ���� �߻�
	 * @throws FileNotFoundException
	 *             �ش� ������ ���� ���
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
	 * key�� �ش��ϴ� String Object�� ��ȯ
	 * 
	 * @param key
	 *            hashtable key
	 * @return key�� �ش��ϴ� String Object
	 * @throws NullPointerException
	 *             key�� null�̰ų� value�� null
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
	 * key�� �ش��ϴ� int value�� ��ȯ
	 * 
	 * @param key
	 *            hashtable key
	 * @return key�� �ش��ϴ� int value
	 * @throws NullPointerException
	 *             key�� null�̰ų� value�� null
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
	 * key�� �ش��ϴ� long value�� ��ȯ
	 * 
	 * @param key
	 *            hashtable key
	 * @return key�� �ش��ϴ� long value
	 * @throws NullPointerException
	 *             key�� null�̰ų� value�� null
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
	 * key�� �ش��ϴ� boolean value�� ��ȯ
	 * 
	 * @param key
	 *            hashtable key
	 * @return key�� �ش��ϴ� boolean value
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
	 * key�� �ش��ϴ� Map Object�� ��ȯ
	 * 
	 * @param key
	 *            hashtable key
	 * @return key�� �ش��ϴ� Map Object
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
	 * key�� �ش��ϴ� List Object�� ��ȯ
	 * 
	 * @param key
	 *            hashtable key
	 * @return key�� �ش��ϴ� List Object
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