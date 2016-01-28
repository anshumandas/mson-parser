package org.mson;

import java.util.Iterator;
import java.util.Map.Entry;

import org.pegdown.Printer;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class MSONSerializer implements ToHtmlSerializerPlugin {

	private static final char SQUARE_CLOSE = ']';
	private static final char SQUARE_OPEN = '[';
	private static final String COLON = ": ";
	private static final char QUOTE = '"';
	private static final char CLOSE_BRACKET = '}';
	private static final char OPEN_BRACKET = '{';
	private static final char COMMA = ',';

	public boolean visit(Node node, Visitor visitor, Printer printer) {
		if(!(node instanceof MSONNode)) 
			return false;
		
		if (node instanceof MSONObjectNode) {
			return visit((MSONObjectNode) node, visitor, printer);
		}
		if (node instanceof MSONArrayNode) {
			return visit((MSONArrayNode) node, visitor, printer);
		}
		if (node instanceof MSONStringNode) {
			return visit((MSONStringNode) node, visitor, printer);
		}
		
		return false;
	}
	
	public boolean visit(MSONArrayNode cNode, Visitor visitor, Printer printer) {
		printer.print(SQUARE_OPEN).indent(+2).printchkln();
		for (Iterator<Node> iterator = cNode.getChildren().iterator(); iterator.hasNext();) {
			Node value = iterator.next(); 
			value.accept(visitor);
			if(iterator.hasNext()) printer.print(COMMA).println();
		}
		printer.indent(-2).printchkln(true).print(SQUARE_CLOSE);
		return true;
	}
	
	public boolean visit(MSONStringNode cNode, Visitor visitor, Printer printer) {

		if(cNode.getValue() != null) {
			cNode.getValue().accept(visitor);
		} 
		return true;
	}

	public boolean visit(MSONObjectNode cNode, Visitor visitor, Printer printer) {
		
		printer.print(OPEN_BRACKET).indent(+2).printchkln();

		for (Iterator<Entry<String, MSONNode>> iterator = cNode.getProperties().entrySet().iterator(); iterator.hasNext();) {
			Entry<String, MSONNode> entry = iterator.next(); 
			String key = entry.getKey();
			MSONNode value = entry.getValue();
			
			printer.print(QUOTE).print(key).print(QUOTE).print(COLON);
			value.accept(visitor);
			if(iterator.hasNext()) printer.print(COMMA).println();
		}
		
		printer.indent(-2).printchkln(true).print(CLOSE_BRACKET);
		return true;
	}

}
