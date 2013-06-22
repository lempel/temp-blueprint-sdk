package lempel.old.framework;

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * XML파일을 읽고/쓰고 하는 Document class <br>
 * SAX를 이용하여 작성되었으므로 ?가 들어가는 태그들을 인식하지 못한다 <br>
 * 복잡한 정보를 가진 문서의 경우에는 이 class를 이용해서 파일로 저장하는 것 보다는 표준 DOM 객체를 사용하는 것을 권한다.
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
	 * 지정한 URI에 있는 파일을 load
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
	 * 지정한 InputStream의 내용을 load
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
	 * 지정된 파일로 XML을 저장
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