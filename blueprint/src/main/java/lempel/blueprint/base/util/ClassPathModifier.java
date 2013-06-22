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
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Adds jar/zip file to classpath during <b>run-time</b>.<br>
 * During compile-time, you still have to add library files.<br>
 * By calling addFile(String) or addURI(URI), classpath can be extended.<br>
 * <br>
 * example:<br>
 * try {<br>
 * addFile("a/b/c/d/e.jar");<br>
 * }<br>
 * catch (IOException e) {<br>
 * e.printStackTrace();<br>
 * }<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2009. 2. 4.
 * @last $Date$
 */
public class ClassPathModifier {
	@SuppressWarnings("unchecked")
	private static final Class[] parameters = new Class[] { URL.class };

	public static void addFile(final String str) throws IOException {
		File file = new File(str);
		addFile(file);
	}

	public static void addFile(final File target) throws IOException {
		addURI(target.toURI());
	}

	public static void addURI(final URI target) throws IOException {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { target });
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
}
