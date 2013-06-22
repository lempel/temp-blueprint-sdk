package lempel.old.framework;

import java.util.*;

/**
 * Resource 객체들을 관리
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
	 * 원하는 Resource를 가져온다
	 * 
	 * @param key
	 *            Resource의 key
	 * @return Resource 객체. 찾을수 없는경우 null.
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
	 * 해당 Resource가 존재하는가 확인
	 * 
	 * @param key
	 *            Resource의 key
	 * @return 존재 여부
	 */
	public boolean containsKey(Object key) {
		return _resources.containsKey(key);
	}

	/**
	 * Resource 객체를 추가
	 * 
	 * @param key
	 *            Resource의 key
	 * @param resource
	 *            추가할 Resource 객체
	 */
	public void addResource(Object key, Object resource) {
		_resources.put(key, resource);
	}

	/**
	 * 등록된 전체 Resource의 수를 반환
	 * 
	 * @return 등록된 전체 Resource의 수
	 */
	public int size() {
		return _resources.size();
	}
}