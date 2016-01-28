package org.mson;

import java.util.ArrayList;
import java.util.List;

import org.pegdown.ast.Node;
import org.pegdown.ast.SuperNode;

public class MSONArrayNode extends SuperNode implements MSONNode {

	MSONArrayNode(){
		super();
	}
	
	//[item(,s) ]
	List<Node> items = new ArrayList<Node>();
	
	@Override
	public List<Node> getChildren() {
		return this.items;
	}

	public void addItem(Node node) {
		if(node instanceof MSONArrayNode && node.getChildren().size() == 1){
			this.items.add(node.getChildren().get(0));			
		} else {
			if(node instanceof MSONNode){
				this.items.add(node);				
			} else {
				throw new Error("Need MSON. Got "+node);
			}
		}
	}
}
