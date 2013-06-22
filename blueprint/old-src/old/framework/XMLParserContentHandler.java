package lempel.old.framework;

import java.util.*;

import org.xml.sax.*;

/**
 * SAX �ļ� <br>
 * XMLNode�� Ʈ�� ���·� xml������ �м��Ѵ� <br>
 * SAX �ļ� ������ �������� �����̹Ƿ� SAX �ļ��� �����Ѵٸ� comment�� �ʿ���� �׷��� �ʴٸ� �ڸ�Ʈ�� SAX �ļ��� ����
 * �����̾�� �Ѵ� <br>
 * ���� �޼ҵ庰 comment�� �����Ѵ�
 * 
 * @author Sang-min Lee
 * @since 2004.10.27.
 * @version 2005.1.12.
 */
class XMLParserContentHandler implements ContentHandler {
	private XMLNode rootNode = null;

	private XMLNode currentNode = null;

	private Hashtable prefixMap = null;

	public XMLNode getRootNode() {
		return rootNode;
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void startDocument() {
		prefixMap = new Hashtable();
	}

	public void endDocument() {
		prefixMap.clear();
	}

	public void processingInstruction(String target, String data) {
	}

	public void startPrefixMapping(String prefix, String uri) {
		prefixMap.put(uri, prefix);
	}

	public void endPrefixMapping(String prefix) {
		Enumeration en = prefixMap.keys();
		Object uri = null;
		while (en.hasMoreElements()) {
			uri = en.nextElement();
			if (prefixMap.get(uri).toString().equals(prefix))
				prefixMap.remove(uri);
		}
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
		XMLNode node = new XMLNode(currentNode, qName, "");

		if (currentNode != null)
			currentNode.addChild(node);
		else
			rootNode = node;
		currentNode = node;

		if ((namespaceURI != null) && (namespaceURI.trim().length() > 0))
			currentNode.setAttribute("xmlns:" + prefixMap.get(namespaceURI),
					namespaceURI);

		int len = atts.getLength();
		for (int i = 0; i < len; i++)
			currentNode.setAttribute(atts.getQName(i), atts.getValue(i));
	}

	public void endElement(String namespaceURI, String localName, String rawName) {
		currentNode = currentNode.getParent();
	}

	public void characters(char[] ch, int start, int end) {
		currentNode.setValue(new String(ch, start, end));
	}

	public void ignorableWhitespace(char[] ch, int start, int end) {
	}

	public void skippedEntity(String name) {
	}
}