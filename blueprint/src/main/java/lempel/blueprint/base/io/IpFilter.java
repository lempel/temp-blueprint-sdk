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
package lempel.blueprint.base.io;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import lempel.blueprint.base.util.Validator;

/**
 * IP Filter (IPv4)
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2008. 12. 9.
 * @last $Date$
 */
public class IpFilter {
	@SuppressWarnings("unchecked")
	private final Map allowed = new HashMap();
	@SuppressWarnings("unchecked")
	private final Map banned = new HashMap();

	/**
	 * @param ip
	 *            an ip. '*' can be used.
	 */
	public void allow(final String ip) {
		addIp(ip, allowed);
	}

	/**
	 * @param ip
	 *            an ip. '*' can be used.
	 */
	public void ban(final String ip) {
		addIp(ip, banned);
	}

	@SuppressWarnings("unchecked")
	public void putAll(final IpFilter parent) {
		allowed.putAll(parent.allowed);
		banned.putAll(parent.allowed);
	}

	// if allowed.size() is 0 or not banned, returns true
	public boolean isAllowed(final String ip) {
		boolean result = true;
		if (isBanned(ip)) {
			result = false;
		} else if (!allowed.isEmpty()) {
			result = containsIp(ip, allowed);
		}

		return result;
	}

	// if banned.size() is 0, returns false
	public boolean isBanned(final String ip) {
		boolean result = false;
		if (!banned.isEmpty()) {
			result = containsIp(ip, banned);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void addIp(final String ip, final Map target) {
		Map map = target;
		StringTokenizer tokenizer = new StringTokenizer(ip, ".");
		for (int i = 0; i < 4 && tokenizer.hasMoreTokens(); i++) {
			String token = tokenizer.nextToken();
			if (token != null && !map.containsKey(token)) {
				map.put(token, new HashMap());
			}

			if ("*".equals(token)) {
				return;
			}

			map = (HashMap) map.get(token);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean containsIp(final String ip, final Map target) {
		boolean result = false;
		Map map = target;

		StringTokenizer st = new StringTokenizer(ip, ".");
		for (int i = 0; i < 4 && st.hasMoreTokens(); i++) {
			if (map.containsKey("*")) {
				result = true;
				break;
			}

			String token = st.nextToken();
			if (Validator.isNotEmpty(token)) {
				if (map.containsKey(token)) {
					result = true;
					map = (HashMap) map.get(token);
				} else {
					result = false;
				}
			}
		}

		return result;
	}
}
