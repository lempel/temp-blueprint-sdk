package lempel.old.framework;

import java.util.*;

/**
 * Resource ��ü���� ����
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2005.5.20.
 */
public class ResourceManager {
	protected Hashtable _resources = null;

	public ResourceManager() {
		_resources = new Hashtable();
	}

	/**
	 * ���ϴ� Resource�� �����´�
	 * 
	 * @param key
	 *            Resource�� key
	 * @return Resource ��ü. ã���� ���°�� null.
	 */
	public Object getResource(Object key) {
		Object result = null;

		if (containsKey(key))
			result = _resources.get(key);
		else
			result = null;

		return result;
	}

	/**
	 * �ش� Resource�� �����ϴ°� Ȯ��
	 * 
	 * @param key
	 *            Resource�� key
	 * @return ���� ����
	 */
	public boolean containsKey(Object key) {
		return _resources.containsKey(key);
	}

	/**
	 * Resource ��ü�� �߰�
	 * 
	 * @param key
	 *            Resource�� key
	 * @param resource
	 *            �߰��� Resource ��ü
	 */
	public void addResource(Object key, Object resource) {
		_resources.put(key, resource);
	}

	/**
	 * ��ϵ� ��ü Resource�� ���� ��ȯ
	 * 
	 * @return ��ϵ� ��ü Resource�� ��
	 */
	public int size() {
		return _resources.size();
	}
}