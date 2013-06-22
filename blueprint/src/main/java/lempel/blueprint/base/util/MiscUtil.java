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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Miscellaneous methods
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2002. 07. 30
 * @last $Date$
 */
public class MiscUtil {
	protected static String fileSeparator = System.getProperty("file.separator");

	public static String getClassName(final Object obj) {
		return obj.getClass().getName();
	}

	/**
	 * generate a random but not existing file name for given path
	 * 
	 * @param filepath
	 * @param ext
	 * @return full path
	 */
	public static String generateRandomFileName(final String filepath, final String ext) {
		String result = null;

		String path = filepath;
		if (!path.endsWith(fileSeparator)) {
			path = path + fileSeparator;
		}

		String newExt = ext;
		if (newExt.charAt(0) != '.') {
			newExt = "." + newExt;
		}

		StringBuffer buff = new StringBuffer(255);
		fileNameLoop: while (true) {
			long objHash = new Object().hashCode();

			buff.append(path).append(TimeStamper.getDateStamp()).append(TimeStamper.getTimeStamp6()).append(
					Long.toString(objHash)).append(newExt);

			result = buff.toString();

			File aFile = new File(result);
			if (!aFile.exists()) {
				break fileNameLoop;
			}

			buff.delete(0, buff.length());
		}

		return result;
	}

	public static String extractFileName(final String filePath) {
		String result;
		int pos = filePath.lastIndexOf(fileSeparator);
		if (pos < 0) {
			result = filePath;
		} else {
			result = filePath.substring(pos + 1, filePath.length());
		}
		return result;
	}

	public static byte[] concatByteArray(final byte[] first, final byte[] second) {
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static String getFileSeparator() {
		return fileSeparator;
	}

	public static String hostname() {
		String result = null;
		try {
			Process proc = Runtime.getRuntime().exec("hostname");
			try {
				proc.waitFor();
			} catch (InterruptedException ignored) {
			}

			InputStream input = proc.getInputStream();
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			if (buffer.length > 0) {
				result = new String(buffer, CharsetUtil.getDefaultEncoding()).trim();
			}
		} catch (IOException ignored) {
		}

		return result;
	}
}