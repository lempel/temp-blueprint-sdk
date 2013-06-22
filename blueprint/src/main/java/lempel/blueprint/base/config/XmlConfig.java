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

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lempel.blueprint.base.log.Logger;
import lempel.blueprint.base.util.Validator;

import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.function.StringFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Manages XML file
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 10. 22
 * @last $Date$
 */
public class XmlConfig implements Config {
	private static final Logger LOGGER = Logger.getInstance();

	private Document config = null;

	public XmlConfig() {
		super();
	}

	public XmlConfig(final String fileName) throws IOException, ParserConfigurationException, SAXException {
		this(null, fileName);
	}

	public XmlConfig(final String templateFileName, final String fileName) throws IOException,
			ParserConfigurationException, SAXException {
		if (Validator.isNotEmpty(templateFileName)) {
			XmlConfig.loadXmlFile(templateFileName);
		}

		if (fileName != null) {
			XmlConfig.loadXmlFile(fileName);
		}
	}

	public boolean getBoolean(final String path) {
		boolean result = false;
		String value = getString(path);
		if (Validator.isNotEmpty(value)) {
			result = Boolean.valueOf(value);
		}

		return result;
	}

	public double getDouble(final String path) {
		double result = Double.MIN_VALUE;
		String value = getString(path);
		if (Validator.isNotEmpty(value)) {
			try {
				result = NumberFormat.getNumberInstance().parse(value).doubleValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" not a double");
			}
		}

		return result;
	}

	public float getFloat(final String path) {
		float result = Float.MIN_VALUE;
		String value = getString(path);
		if (Validator.isNotEmpty(value)) {
			try {
				result = NumberFormat.getNumberInstance().parse(value).floatValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" not a float");
			}
		}

		return result;
	}

	public int getInt(final String path) {
		int result = Integer.MIN_VALUE;
		String value = getString(path);
		if (Validator.isNotEmpty(value)) {
			try {
				result = NumberFormat.getNumberInstance().parse(value).intValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" is not an int");
			}
		}

		return result;
	}

	public long getLong(final String path) {
		long result = Long.MIN_VALUE;
		String value = getString(path);
		if (Validator.isNotEmpty(value)) {
			try {
				result = NumberFormat.getNumberInstance().parse(value).longValue();
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" is not a long");
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public String getString(final String path) {
		String result = null;
		if (Validator.isNotNull(config)) {
			try {
				XPath expression = new org.jaxen.dom.DOMXPath(path);
				Navigator navigator = expression.getNavigator();

				List results = expression.selectNodes(config);
				if (results.isEmpty()) {
					LOGGER.error("configuration path \"" + path + "\" not exists");
				} else {
					Node resultNode = (Node) results.get(0);
					result = StringFunction.evaluate(resultNode, navigator);
				}
			} catch (JaxenException e) {
				LOGGER.error("configuration path \"" + path + "\" is invalid");
			}
		}

		return result;
	}

	private static Document loadXmlFile(final String fileName) throws IOException, ParserConfigurationException,
			SAXException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(fileName);
	}

	public void load(final String fileName) throws IOException, ParserConfigurationException, SAXException {
		config = loadXmlFile(fileName);
	}

	public void save(final String fileName) throws IOException {
		org.dom4j.Document dom4jDoc = new DOMReader().read(config);
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(fileName), format);
		writer.write(dom4jDoc);
		writer.close();
	}

	@SuppressWarnings("unchecked")
	public boolean[] getBooleanArray(final String path) {
		if (Validator.isNotNull(config)) {
			try {
				XPath expression = new org.jaxen.dom.DOMXPath(path);
				Navigator navigator = expression.getNavigator();

				List results = expression.selectNodes(config);
				int resultSize = results.size();
				boolean[] resultArray = new boolean[resultSize];
				for (int i = 0; i < resultSize; i++) {
					Node result = (Node) results.get(i);
					resultArray[i] = Boolean.getBoolean(StringFunction.evaluate(result, navigator));
				}

				return resultArray;
			} catch (JaxenException e) {
				LOGGER.error("configuration path \"" + path + "\" is invalid");
			}
		}

		return new boolean[] {};
	}

	@SuppressWarnings("unchecked")
	public double[] getDoubleArray(final String path) {
		if (Validator.isNotNull(config)) {
			try {
				XPath expression = new org.jaxen.dom.DOMXPath(path);
				Navigator navigator = expression.getNavigator();

				List results = expression.selectNodes(config);
				int resultSize = results.size();
				double[] resultArray = new double[resultSize];
				for (int i = 0; i < resultSize; i++) {
					Node result = (Node) results.get(i);
					resultArray[i] = NumberFormat.getNumberInstance().parse(StringFunction.evaluate(result, navigator))
							.doubleValue();
				}

				return resultArray;
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" is not a double array");
			} catch (JaxenException e) {
				LOGGER.error("configuration path \"" + path + "\" is invalid");
			}
		}

		return new double[] {};
	}

	@SuppressWarnings("unchecked")
	public float[] getFloatArray(final String path) {
		if (Validator.isNotNull(config)) {
			try {
				XPath expression = new org.jaxen.dom.DOMXPath(path);
				Navigator navigator = expression.getNavigator();

				List results = expression.selectNodes(config);
				int resultSize = results.size();
				float[] resultArray = new float[resultSize];
				for (int i = 0; i < resultSize; i++) {
					Node result = (Node) results.get(i);
					resultArray[i] = NumberFormat.getNumberInstance().parse(StringFunction.evaluate(result, navigator))
							.floatValue();
				}

				return resultArray;
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" is not a float array");
			} catch (JaxenException e) {
				LOGGER.error("configuration path \"" + path + "\" is invalid");
			}
		}

		return new float[] {};
	}

	@SuppressWarnings("unchecked")
	public int[] getIntArray(final String path) {
		if (Validator.isNotNull(config)) {
			try {
				XPath expression = new org.jaxen.dom.DOMXPath(path);
				Navigator navigator = expression.getNavigator();

				List results = expression.selectNodes(config);
				int resultSize = results.size();
				int[] resultArray = new int[resultSize];
				for (int i = 0; i < resultSize; i++) {
					Node result = (Node) results.get(i);
					resultArray[i] = NumberFormat.getNumberInstance().parse(StringFunction.evaluate(result, navigator))
							.intValue();
				}

				return resultArray;
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" is not an int array");
			} catch (JaxenException e) {
				LOGGER.error("configuration path \"" + path + "\" is invalid");
			}
		}

		return new int[] {};
	}

	@SuppressWarnings("unchecked")
	public long[] getLongArray(final String path) {
		if (Validator.isNotNull(config)) {
			try {
				XPath expression = new org.jaxen.dom.DOMXPath(path);
				Navigator navigator = expression.getNavigator();

				List results = expression.selectNodes(config);
				int resultSize = results.size();
				long[] resultArray = new long[resultSize];
				for (int i = 0; i < resultSize; i++) {
					Node result = (Node) results.get(i);
					resultArray[i] = NumberFormat.getNumberInstance().parse(StringFunction.evaluate(result, navigator))
							.longValue();
				}

				return resultArray;
			} catch (ParseException e) {
				LOGGER.error("configuration path \"" + path + "\" is not long array");
			} catch (JaxenException e) {
				LOGGER.error("configuration path \"" + path + "\" is invalid");
			}
		}

		return new long[] {};
	}

	@SuppressWarnings("unchecked")
	public String[] getStringArray(final String path) {
		if (Validator.isNotNull(config)) {
			try {
				XPath expression = new org.jaxen.dom.DOMXPath(path);
				Navigator navigator = expression.getNavigator();

				List results = expression.selectNodes(config);
				int resultSize = results.size();
				String[] resultArray = new String[resultSize];
				for (int i = 0; i < resultSize; i++) {
					Node result = (Node) results.get(i);
					resultArray[i] = StringFunction.evaluate(result, navigator);
				}

				return resultArray;
			} catch (JaxenException e) {
				LOGGER.error("configuration path \"" + path + "\" is invalid");
			}
		}

		return new String[] {};
	}

	public String toString() {
		return config.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		config = null;

		super.finalize();
	}

	public Document getConfig() {
		return config;
	}

	public void setConfig(Document config) {
		this.config = config;
	}
}
