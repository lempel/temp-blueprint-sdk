package lempel.old.framework;

import java.io.*;
import java.util.*;

/**
 * XMLDocument에서 사용하는 XML Element를 표현하는 node
 * 
 * @author Sang-min Lee
 * @since 2004.10.27.
 * @version 2005.1.12.
 */
public class XMLNode implements Serializable {
	/** serialVersionUID */
	private static final long serialVersionUID = -7889787776700083607L;

	private XMLNode parent = null;

	private String name = null;

	private String value = null;

	private Hashtable attributes = null;

	private Vector children = null;

	/**
	 * 
	 * @param parent
	 *            부모노드
	 * @param name
	 * @param value
	 */
	public XMLNode(XMLNode parent, String name, String value) {
		this(parent, name, value, new Hashtable());
	}

	/**
	 * 
	 * @param parent
	 * @param name
	 * @param value
	 * @param attributes
	 *            해당 element의 attribute들
	 */
	public XMLNode(XMLNode parent, String name, String value,
			Hashtable attributes) {
		this(parent, name, value, attributes, new Vector());
	}

	/**
	 * 
	 * @param parent
	 * @param name
	 * @param value
	 * @param attributes
	 * @param children
	 *            child element들
	 */
	public XMLNode(XMLNode parent, String name, String value,
			Hashtable attributes, Vector children) {
		setParent(parent);
		setName(name);
		setValue(value);
		setAttributes(attributes);
		setChildren(children);
	}

	/**
	 * 해당 node에 attribute를 추가
	 * 
	 * @param name
	 * @param value
	 */
	public synchronized void setAttribute(String name, String value) {
		getAttributes().put(name, value);
	}

	/**
	 * 특정 attribute의 값을 얻어온다
	 * 
	 * @param name
	 * @return attribute 값
	 */
	public synchronized String getAttribute(String name) {
		return getAttributes().get(name).toString();
	}

	/**
	 * 특정 attribute가 존재하는가 검사
	 * 
	 * @param name
	 * @return true : 존재
	 */
	public synchronized boolean hasAttribute(String name) {
		return getAttributes().containsKey(name);
	}

	/**
	 * child node를 추가
	 * 
	 * @param node
	 */
	public synchronized void addChild(XMLNode node) {
		node.setParent(this);
		getChildren().add(node);
	}

	/**
	 * 해당 child node를 포함하고 있는지 검사
	 * 
	 * @param name
	 * @return true : 포함
	 */
	public boolean hasChild(String name) {
		boolean result = false;
		if (getChildren(name).size() > 0)
			result = true;
		return result;
	}

	/**
	 * 해당 이름을 가지는 child node들을 반환
	 * 
	 * @param name
	 *            child node 리스트
	 * @return child nodes
	 */
	public Vector getChildren(String name) {
		Vector result = new Vector();

		synchronized (children) {
			XMLNode temp = null;
			for (int i = 0; i < children.size(); i++) {
				temp = (XMLNode) children.get(i);
				if (temp.getName().equals(name))
					result.add(temp);
			}
		}

		return result;
	}

	/**
	 * 첫번째 child node를 반환 <br>
	 * .getFirstChild(String).getFirstChild(String)....... <br>
	 * 형태로 사용하면 그나마 조금더 쉽게 원하는 node에 접근할 수 있도록 하기 위해 구현함
	 * 
	 * @param name
	 * @return 첫번째 child node
	 */
	public XMLNode getFirstChild(String name) {
		XMLNode result = null;

		synchronized (children) {
			loop1: for (int i = 0; i < children.size(); i++) {
				result = (XMLNode) children.get(i);
				if (result.getName().equals(name))
					break loop1;
			}
		}

		return result;
	}

	/**
	 * 현재 node를 root로 간주하고 직렬화
	 */
	public String toString() {
		StringBuffer sbuff = new StringBuffer();
		sbuff.append("<?xml version=\"1.0\" encoding=\"euc-kr\" ?>\n").append(
				toString(""));
		return sbuff.toString();
	}

	/**
	 * root node가 아닌 node들을 indent해주면서 직렬화
	 * 
	 * @param indent
	 *            indent에 사용할 문자열
	 * @return 직렬화된 XML
	 */
	protected String toString(String indent) {
		StringBuffer sbuff = new StringBuffer();

		sbuff.append(indent).append("<").append(getName());
		synchronized (attributes) {
			if (attributes.size() > 0) {
				Enumeration en = attributes.keys();
				String key = null;
				String value = null;

				while (en.hasMoreElements()) {
					key = en.nextElement().toString();
					value = attributes.get(key).toString();

					sbuff.append(" ").append(key).append("=\"").append(value)
							.append("\"");
				}
			}
		}

		sbuff.append(">");
		if (getValue().trim().length() != 0)
			sbuff.append(getValue().trim());

		if (children.size() > 0) {
			synchronized (children) {
				XMLNode child = null;
				sbuff.append("\n");
				for (int i = 0; i < children.size(); i++) {
					child = (XMLNode) children.get(i);
					sbuff.append(child.toString(indent + "  "));
				}
				sbuff.append(indent);
			}
		}
		sbuff.append("</").append(getName()).append(">\n");

		return sbuff.toString();
	}

	/**
	 * @return Returns the parent.
	 */
	public synchronized XMLNode getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            The parent to set.
	 */
	public synchronized void setParent(XMLNode parent) {
		this.parent = parent;
	}

	/**
	 * @return Returns the name.
	 */
	public synchronized String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public synchronized void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the value.
	 */
	public synchronized String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            The value to set.
	 */
	public synchronized void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return Returns the attributes.
	 */
	public synchronized Hashtable getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            The attributes to set.
	 */
	public synchronized void setAttributes(Hashtable attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return Returns the children.
	 */
	public synchronized Vector getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            The children to set.
	 */
	public synchronized void setChildren(Vector children) {
		this.children = children;
	}
}