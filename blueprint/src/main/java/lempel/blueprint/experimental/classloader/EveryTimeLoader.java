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

package lempel.blueprint.experimental.classloader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import blueprint.sdk.logger.Logger;


/**
 * loads class every time (always hot!)
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @since 2009. 3. 26.
 * @last $Date$
 */
public class EveryTimeLoader extends ClassLoader {
	private static final Logger LOGGER = Logger.getInstance();
	private ClassLoader parent;
	private String classDir;

	private EveryTimeLoader(ClassLoader parent, String classDir) {
		super();

		this.parent = parent;
		this.classDir = classDir;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	public static synchronized EveryTimeLoader newInstance(String classDir) {
		Thread currentThread = Thread.currentThread();
		ClassLoader ctxClassLoader = currentThread.getContextClassLoader();

		EveryTimeLoader newClassLoader;
		if (!(ctxClassLoader instanceof EveryTimeLoader)) {
			newClassLoader = new EveryTimeLoader(ctxClassLoader, classDir);
			currentThread.setContextClassLoader(newClassLoader);
		} else {
			newClassLoader = (EveryTimeLoader) ctxClassLoader;
		}

		return newClassLoader;
	}

	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> result = null;

		if (name.startsWith("com.kbstar.iqm.pms")) {
			try {
				FileInputStream fis = new FileInputStream(classDir + EveryTimeLoader.reaplace(name, '.', '/')
						+ ".class");
				byte[] buffer = new byte[fis.available()];
				fis.read(buffer);
				fis.close();

				result = defineClass(name, buffer, 0, buffer.length);

				LOGGER.debug(this, "class loaded  - " + classDir + EveryTimeLoader.reaplace(name, '.', '/') + ".class");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new ClassNotFoundException();
			} catch (IOException e) {
				e.printStackTrace();
				throw new ClassNotFoundException();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			result = parent.loadClass(name);
		}
		return result;
	}

	private static String reaplace(String str, char srcChar, char targetChar) {
		char[] temp = str.toCharArray();
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] == srcChar) {
				temp[i] = targetChar;
			}
		}
		return new String(temp);
	}
}
