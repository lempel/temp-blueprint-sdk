package lempel.old.framework;

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * XML������ �а�/���� �ϴ� Document class <br>
 * SAX�� �̿��Ͽ� �ۼ��Ǿ����Ƿ� ?�� ���� �±׵��� �ν����� ���Ѵ� <br>
 * ������ ������ ���� ������ ��쿡�� �� class�� �̿��ؼ� ���Ϸ� �����ϴ� �� ���ٴ� ǥ�� DOM ��ü�� ����ϴ� ���� ���Ѵ�.
 * 
 * @author Sang-min Lee
 * @since 2004.10.27.
 * @version 2004.11.1.
 */
public class XMLDocument implements Serializable {
	/** */
	private static final long serialVersionUID = 5901918947695108605L;

	private ContentHandler contentHandler = null;

	private ErrorHandler errorHandler = null;

	private XMLReader parser = null;

	private XMLNode rootNode = null;

	public XMLDocument() {
	}

	/**
	 * ������ URI�� �ִ� ������ load
	 * 
	 * @param uri
	 * @throws IOException
	 * @throws SAXException
	 */
	public void load(String uri) throws IOException, SAXException {
		contentHandler = new XMLParserContentHandler();
		errorHandler = new XMLParserErrorHandler();

		parser = XMLReaderFactory
				.createXMLReader("org.apache.xerces.parsers.SAXParser");
		parser.setContentHandler(contentHandler);
		parser.setErrorHandler(errorHandler);

		parser.parse(uri);

		rootNode = ((XMLParserContentHandler) contentHandler).getRootNode();
	}

	/**
	 * ������ InputStream�� ������ load
	 * 
	 * @param in
	 * @throws IOException
	 * @throws SAXException
	 */
	public void load(InputStream in) throws IOException, SAXException {
		contentHandler = new XMLParserContentHandler();
		errorHandler = new XMLParserErrorHandler();

		parser = XMLReaderFactory
				.createXMLReader("org.apache.xerces.parsers.SAXParser");
		parser.setContentHandler(contentHandler);
		parser.setErrorHandler(errorHandler);

		parser.parse(new InputSource(in));

		rootNode = ((XMLParserContentHandler) contentHandler).getRootNode();
	}

	/**
	 * ������ ���Ϸ� XML�� ����
	 * 
	 * @param fileName
	 */
	public void write(String fileName) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(fileName));
		pw.println(rootNode);
		pw.flush();
		pw.close();
	}

	/**
	 * @return Returns the rootNode.
	 */
	public XMLNode getRootNode() {
		return rootNode;
	}
}