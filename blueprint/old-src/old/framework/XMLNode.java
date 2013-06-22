package lempel.old.framework;

import java.io.*;
import java.util.*;

/**
 * XMLDocument���� ����ϴ� XML Element�� ǥ���ϴ� node
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
	 *            �θ���
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
	 *            �ش� element�� attribute��
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
	 *            child element��
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
	 * �ش� node�� attribute�� �߰�
	 * 
	 * @param name
	 * @param value
	 */
	public synchronized void setAttribute(String name, String value) {
		getAttributes().put(name, value);
	}

	/**
	 * Ư�� attribute�� ���� ���´�
	 * 
	 * @param name
	 * @return attribute ��
	 */
	public synchronized String getAttribute(String name) {
		return getAttributes().get(name).toString();
	}

	/**
	 * Ư�� attribute�� �����ϴ°� �˻�
	 * 
	 * @param name
	 * @return true : ����
	 */
	public synchronized boolean hasAttribute(String name) {
		return getAttributes().containsKey(name);
	}

	/**
	 * child node�� �߰�
	 * 
	 * @param node
	 */
	public synchronized void addChild(XMLNode node) {
		node.setParent(this);
		getChildren().add(node);
	}

	/**
	 * �ش� child node�� �����ϰ� �ִ��� �˻�
	 * 
	 * @param name
	 * @return true : ����
	 */
	public boolean hasChild(String name) {
		boolean result = false;
		if (getChildren(name).size() > 0)
			result = true;
		return result;
	}

	/**
	 * �ش� �̸��� ������ child node���� ��ȯ
	 * 
	 * @param name
	 *            child node ����Ʈ
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
	 * ù��° child node�� ��ȯ <br>
	 * .getFirstChild(String).getFirstChild(String)....... <br>
	 * ���·� ����ϸ� �׳��� ���ݴ� ���� ���ϴ� node�� ������ �� �ֵ��� �ϱ� ���� ������
	 * 
	 * @param name
	 * @return ù��° child node
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
	 * ���� node�� root�� �����ϰ� ����ȭ
	 */
	public String toString() {
		StringBuffer sbuff = new StringBuffer();
		sbuff.append("<?xml version=\"1.0\" encoding=\"euc-kr\" ?>\n").append(
				toString(""));
		return sbuff.toString();
	}

	/**
	 * root node�� �ƴ� node���� indent���ָ鼭 ����ȭ
	 * 
	 * @param indent
	 *            indent�� ����� ���ڿ�
	 * @return ����ȭ�� XML
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