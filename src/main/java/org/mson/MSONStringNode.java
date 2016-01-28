package org.mson;

import java.util.List;

import org.parboiled.common.ImmutableList;
import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

public class MSONStringNode extends AbstractNode implements MSONNode {
	
	private Node value; //can be string or emphasized node etc.

	MSONStringNode(Node text) {
		this.value = text;
	}

	public Node getValue() {
		return this.value;
	}

    public List<Node> getChildren() {
        return ImmutableList.of();
    }
    
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
