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
package lempel.blueprint.base.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lempel.blueprint.base.io.ByteArrayHandler;

import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import bluerpint.sdk.util.CharsetUtil;

/**
 * eXtream XML Parser (XP) - A VERY SIMPLE DOM parser.<br>
 * Just builds DOM tree only. No validations. No DTDs.<br>
 * Use setSimultaneousXPs(int) method to choke.<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2008. 02. 05
 * @last $Date$
 */
public class XP {
	/** number of XPs - currently running */
	private static int xps = 0;

	/** number of XPs - maximum */
	private static int maxXps = Integer.MAX_VALUE;

	/** monitor object */
	private static Object monitor = new Object();

	private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
	private transient DocumentBuilder builder;

	private String encoding;
	private Document result;
	private Node rootNode;
	private Node currentNode;

	public XP() throws ParserConfigurationException {
		builder = FACTORY.newDocumentBuilder();
	}

	/**
	 * Don't change the numbers while XPs are busy. It may cause some XPs to
	 * sleep forever.
	 * 
	 * @param count
	 */
	public static void setSimultaneousXPs(final int count) {
		synchronized (monitor) {
			maxXps = count;
			monitor.notifyAll();
		}
	}

	public Document parse(final String fileName) throws IOException {
		return parse(new FileInputStream(fileName));
	}

	/**
	 * Blocking streams can't be use<br>
	 * ex) Socket's InputStream<br>
	 * 
	 * @param inStream
	 * @return org.w3c.dom.Document
	 * @throws IOException
	 */
	public Document parse(final InputStream inStream) throws IOException {
		final byte[] buffer = new byte[inStream.available()];
		inStream.read(buffer);
		return parse(buffer);
	}

	public Document parse(final byte[] input) throws IOException {
		return parse(new ByteArrayHandler(input));
	}

	private Document parse(final ByteArrayHandler handler) throws IOException {
		setResult(builder.newDocument());
		setRootNode(null);
		setCurrentNode(null);
		setEncoding(null);

		// choking point
		synchronized (monitor) {
			xps++;
			if (maxXps < xps) {
				try {
					monitor.wait();
				} catch (InterruptedException ignored) {
				}
			}
		}

		while (handler.getOffset() < handler.getLength()) {
			parseATag(handler, handler.find(handler.getOffset(), (byte) '<'));
		}

		synchronized (monitor) {
			maxXps--;
			monitor.notifyAll();
		}

		return getResult();
	}

	private void parseATag(final ByteArrayHandler handler, final int startPosition) throws DOMException,
			UnsupportedEncodingException {
		int startPos = startPosition;
		int endPos;
		boolean isEntity = false;

		if (handler.getOffset() < (startPos) && getCurrentNode() != null) {
			getCurrentNode().appendChild(
					getResult().createTextNode(new String(handler.getBytes(handler.getOffset(), startPos), encoding)));
		}

		if (handler.getByte(startPos + 1) == '?') {
			// PI tag
			endPos = handler.findAll(startPos + 2, new byte[] { '?', '>' }) + 2;

			final int encStartPos = handler.findAll(startPos, endPos, new byte[] { 'e', 'n', 'c', 'o', 'd', 'i', 'n',
					'g', '=' }) + 10;
			final int encEndPos = handler.find(encStartPos, endPos, new byte[] { '\'', '"' });
			setEncoding(new String(handler.getBytes(encStartPos, encEndPos), CharsetUtil.getDefaultEncoding()));
		} else if (handler.getByte(startPos + 1) == '!') {
			if (handler.getByte(startPos + 2) == '[') {
				// CDATA tag
				endPos = handler.findAll(startPos + 3, new byte[] { ']', ']', '>' }) + 3;

				if (getCurrentNode() != null) {
					getCurrentNode().appendChild(
							getResult().createCDATASection(
									new String(handler.getBytes(startPos + 9, endPos - 3), encoding)));
				}
			} else if (handler.getByte(startPos + 2) == '-' && handler.getByte(startPos + 3) == '-') {
				// comment tag (skip)
				endPos = handler.findAll(startPos + 2, new byte[] { '-', '-', '>' }) + 3;
			} else {
				// DOCTYPE tag (skip)
				endPos = handler.findAll(startPos + 2, new byte[] { ']', '>' }) + 2;
			}
		} else {
			// entity

			isEntity = true;
			endPos = handler.find((byte) '>');
		}

		// no tags? EOF?
		if (endPos < 0 || endPos > handler.getLength()) {
			return;
		}

		// closing tag? (0: opening, 1: closing, 2: self closing)
		int isClosing = 0;
		if (handler.getByte(startPos + 1) == '/') {
			// closing tag
			isClosing = 1;
			startPos++;
		} else if (handler.getByte(endPos - 1) == '/') {
			// single tag
			isClosing = 2;
		}

		if (isEntity) {
			// find the end of tag name (space, tab, return, line-feed)
			int namePos = handler.find(startPos + 1, handler.find(startPos + 1, (byte) '>'), new byte[] { 0x09, 0x20,
					0x0d, 0x0a });

			// correct the position when it's self closing
			if (isClosing == 2 && handler.getByte(namePos - 1) == '/') {
				namePos--;
			}

			// extract node name, namespace
			byte[] nsBytes = null;
			final byte[] nameBytes = handler.getBytes(startPos + 1, namePos);
			final int nsPos = handler.find(startPos + 1, namePos, new byte[] { ':' });
			if (nsPos < namePos) {
				nsBytes = handler.getBytes(startPos + 1, nsPos);
			}

			if (isClosing != 1) {
				// opening/single tag

				Element newElement;
				if (nsBytes == null) {
					newElement = getResult().createElement(new String(nameBytes, encoding));
				} else {
					newElement = getResult().createElementNS("", new String(nameBytes, encoding));
				}

				if (getCurrentNode() == null) {
					setRootNode(newElement);
					getResult().appendChild(newElement);
				} else {
					getCurrentNode().appendChild(newElement);
				}
				setCurrentNode(newElement);

				parseAttributes(handler, namePos + 1, endPos);
			}

			// closing/single tag
			// FIXME Can't use getter/setter. i dunno why but it goes wrong.)
			if (isClosing > 0 && currentNode != rootNode) {
				currentNode = currentNode.getParentNode();
			}
		}

		handler.setOffset(endPos + 1);
	}

	private void parseAttributes(final ByteArrayHandler handler, final int startIndex, final int endIndex)
			throws DOMException, UnsupportedEncodingException {
		int startPos = startIndex;
		int tagSepPos;

		// while there's any separator
		final byte[] lineDelimeters = new byte[] { 0x09, 0x20, 0x0d, 0x0a };
		final byte[] delimeters = new byte[] { '=' };
		while (startPos < (tagSepPos = handler.find(startPos, endIndex, lineDelimeters))) {
			int sepPos = handler.find(startPos, tagSepPos, delimeters);
			// no more separator?
			if (sepPos == tagSepPos) {
				return;
			}
			final byte[] nameBytes = handler.getBytes(startPos, sepPos);
			final byte[] valueBytes = handler.getBytes(sepPos + 2, tagSepPos - 1);
			((Element) currentNode).setAttribute(new String(nameBytes, encoding), new String(valueBytes, encoding));
			startPos = tagSepPos + 1;
		}
	}

	private void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	private void setResult(final Document doc) {
		this.result = doc;
	}

	private Document getResult() {
		return result;
	}

	public Node getRootNode() {
		return rootNode;
	}

	private void setRootNode(final Node root) {
		rootNode = root;
	}

	private Node getCurrentNode() {
		return currentNode;
	}

	private void setCurrentNode(final Node current) {
		currentNode = current;
	}

	@Override
	protected void finalize() throws Throwable {
		builder = null;
		currentNode = null;
		result = null;
		rootNode = null;

		super.finalize();
	}

	/**
	 * print XML document to target
	 * 
	 * @param result
	 * @throws IOException
	 */
	public static void print(Document result, PrintStream target) throws IOException {
		org.dom4j.Document dom4jDoc = new DOMReader().read(result);
		print(dom4jDoc, target);
	}

	/**
	 * print dom4j document to target
	 * 
	 * @param dom4jDoc
	 * @throws IOException
	 */
	public static void print(org.dom4j.Document dom4jDoc, PrintStream target) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("EUC-KR");
		XMLWriter writer = new XMLWriter(target, format);
		writer.write(dom4jDoc);
		writer.close();
	}
}
