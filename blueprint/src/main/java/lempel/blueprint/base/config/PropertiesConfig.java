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
package lempel.blueprint.base.config;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;


import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import blueprint.sdk.logger.Logger;
import blueprint.sdk.util.Validator;

/**
 * Manages Properties file
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 10. 22
 * @last $Date$
 */
public class PropertiesConfig implements Config {
	private static final Logger LOGGER = Logger.getInstance();
	private Properties config = new Properties();

	public PropertiesConfig(final String fileName) throws IOException, DocumentException {
		this(null, fileName);
	}

	public PropertiesConfig(final String templateFileName, final String fileName) throws IOException, DocumentException {
		if (Validator.isNotEmpty(templateFileName)) {
			PropertiesConfig.load(config, templateFileName);
		}

		if (Validator.isNotEmpty(fileName)) {
			PropertiesConfig.load(config, fileName);
		}
	}

	public boolean getBoolean(final String path) {
		boolean result = false;
		if (config.containsKey(path)) {
			result = Boolean.getBoolean(config.getProperty(path));
		} else {
			LOGGER.error("configuration path \"" + path + "\" not exists");
		}
		return result;
	}

	public double getDouble(final String path) {
		double result = Double.MIN_VALUE;
		if (config.containsKey(path)) {
			try {
				result = NumberFormat.getNumberInstance().parse(config.getProperty(path)).doubleValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" not a double");
			}
		} else {
			LOGGER.error("configuration path \"" + path + "\" not exists");
		}
		return result;
	}

	public float getFloat(final String path) {
		float result = Float.MIN_VALUE;
		if (config.containsKey(path)) {
			try {
				result = NumberFormat.getNumberInstance().parse(config.getProperty(path)).floatValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" not a float");
			}
		} else {
			LOGGER.error("configuration path \"" + path + "\" not exists");
		}
		return result;
	}

	public int getInt(final String path) {
		int result = Integer.MIN_VALUE;
		if (config.containsKey(path)) {
			try {
				result = NumberFormat.getNumberInstance().parse(config.getProperty(path)).intValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" not an int");
			}
		} else {
			LOGGER.error("configuration path \"" + path + "\" not exists");
		}
		return result;
	}

	public long getLong(final String path) {
		long result = Long.MIN_VALUE;
		if (config.containsKey(path)) {
			try {
				result = NumberFormat.getNumberInstance().parse(config.getProperty(path)).longValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" not a long");
			}
		} else {
			LOGGER.error("configuration path \"" + path + "\" not exists");
		}
		return result;
	}

	public String getString(final String path) {
		String result = null;
		if (config.containsKey(path)) {
			result = config.getProperty(path);
		} else {
			LOGGER.error("configuration path \"" + path + "\" not exists");
		}
		return result;
	}

	/**
	 * load a file to given Properties
	 * 
	 * @param config
	 * @param fileName
	 * @throws IOException
	 */
	public static void load(final Properties config, final String fileName) throws IOException {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(fileName);
			config.load(fin);
		} finally {
			if (Validator.isNotNull(fin)) {
				try {
					fin.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	public void load(final String fileName) throws IOException, ParserConfigurationException, SAXException {
		PropertiesConfig.load(config, fileName);
	}

	public void save(final String fileName) throws IOException {
		BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(fileName));
		config.store(bout, null);
		bout.flush();
	}

	public String toString() {
		return config.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		config.clear();
		config = null;

		super.finalize();
	}

	public Properties getConfig() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}
}
