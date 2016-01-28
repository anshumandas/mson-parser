package org.mson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.pegdown.ast.Node;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;

public class MSONObjectNode extends SuperNode implements MSONNode {
	// name_value (param(,s))
	// value(,s) (param(,s)) //where key is anything other than name

	private Map<String, MSONNode> fields = new TreeMap<String, MSONNode>();
	boolean isArray;
	boolean isEnum;

	MSONObjectNode(MSONNode child) {
		super();
		// add a property
		addProperty(child);
	}

	MSONObjectNode() {
		super();
	}

	public void addField(String key, MSONNode property) {
		if (property != null) {
			this.fields.put(key, property);
		}
	}

	public void addType(MSONStringNode type) {
		this.isArray = (((TextNode) type.getValue()).getText().startsWith("array"));
		this.isEnum = (((TextNode) type.getValue()).getText().startsWith("enum"));
		addField("type", type);
	}

	public void addName(MSONNode name) {
		addField("name", name);
	}

	public void addTitle(MSONNode title) {
		addField("title", title);
	}

	public void addDescription(MSONNode desc) {
		addField("description", desc);
	}

	public void addRequired(MSONNode node) {
		if (!this.fields.containsKey("required")) {
			addField("required", new MSONArrayNode());
		}
		((MSONArrayNode) this.fields.get("required")).addItem(node);
	}

	public void addSample(Node node) {
		addSomething("samples", node);
	}

	private void addSomething(String to, Node node) {
		if (node instanceof MSONArrayNode) {
			if (!this.fields.containsKey(to)) {
				addField(to, (MSONArrayNode) node);
			} else {
				((MSONArrayNode) this.fields.get(to)).getChildren().addAll(node.getChildren());
			}
			return;
		} else if (node instanceof MSONObjectNode) {
			MSONObjectNode m = (MSONObjectNode) node;
			if (m.getProperties().size() == 1) {
				if (!this.fields.containsKey(to)) {
					addField(to, (MSONNode) m.getChildren().get(0));
				} else {
					((MSONArrayNode) this.fields.get(to)).getChildren()
							.addAll(m.getChildren().get(0).getChildren());
				}
				return;
			}
		} 
		
		if (!this.fields.containsKey(to)) {
			addField(to, new MSONArrayNode());
		}
		((MSONArrayNode) this.fields.get(to)).addItem(node);
	}

	public void addProperty(Node node) {
		if (this.isArray) {
			addSample(node);
		} else {
			addSomething(this.isEnum? "values" : "properties", node);
		}
	}

	@Override
	public List<Node> getChildren() {
		return new ArrayList<Node>(this.fields.values());
	}

	public Map<String, MSONNode> getProperties() {
		return this.fields;
	}

	public void setProperties(Map<String, MSONNode> properties) {
		this.fields = properties;
	}

	public void addAttribute(MSONNode node) {
		// TODO This needs work

	}
}
