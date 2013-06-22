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
package lempel.blueprint.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A StringTokenizer for shell command.<br>
 * Shell arguments can contain double quotes to preserve arguments with spaces.<br>
 * But StringTokenizer separates such arguments.<br>
 * CommandTokenizer prevents that.<br>
 * 
 * @author Simon Lee
 * @since 2012. 11. 29.
 */
public class CommandTokenizer {
	/**
	 * Tokenize current command and returns String[]
	 * 
	 * @param command
	 *            shell command to tokenize
	 * @return array of tokens
	 * @throws IllegalArgumentException
	 *             double quote not closed
	 */
	public static String[] tokenize(final String command) throws IllegalArgumentException {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(command);
		while (tokenizer.hasMoreTokens()) {
			String temp = tokenizer.nextToken();
			if (temp.indexOf('"') == 0) {
				byte[] bytes = temp.getBytes();
				while (bytes[bytes.length - 1] != '"') {
					if (!tokenizer.hasMoreTokens()) {
						throw new IllegalArgumentException("double quote not closed - " + command);
					}
					temp += " " + tokenizer.nextToken();
					bytes = temp.getBytes();
				}
				tokens.add(temp);
			} else {
				tokens.add(temp);
			}
		}

		String[] result = tokens.toArray(new String[tokens.size()]);
		return result;
	}
}
