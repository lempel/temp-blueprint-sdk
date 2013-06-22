package lempel.old.framework;

import org.xml.sax.*;

/**
 * SAXÆÄ¼­ÀÇ ErrorHandler
 * 
 * @author Sang-min Lee
 * @since 2004.10.27.
 * @version 2004.10.27.
 */
class XMLParserErrorHandler implements ErrorHandler {
	public void warning(SAXParseException exception) throws SAXException {
		System.out.println(">> Parsing warning**\n" + "  Line: "
				+ exception.getLineNumber() + "\n" + "  URI: "
				+ exception.getSystemId() + "\n" + "  Message: "
				+ exception.getMessage());

		throw new SAXException(">> Warning encountered");
	}

	public void error(SAXParseException exception) throws SAXException {
		System.out.println(">> Parsing error**\n" + "  Line: "
				+ exception.getLineNumber() + "\n" + "  URI: "
				+ exception.getSystemId() + "\n" + "  Message: "
				+ exception.getMessage());

		throw new SAXException(">> Error encountered");
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		System.out.println(">> Fatal Parsing Error**\n" + "  Line: "
				+ exception.getLineNumber() + "\n" + "  URI: "
				+ exception.getSystemId() + "\n" + "  Message: "
				+ exception.getMessage());

		throw new SAXException(">> Fatal Error encountered");
	}
}