package org.pegdown;

import static org.parboiled.common.Preconditions.checkArgNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.mson.MSONNode;
import org.parboiled.common.StringUtils;
import org.pegdown.DefaultVerbatimSerializer;
import org.pegdown.LinkRenderer;
import org.pegdown.Printer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AnchorLinkNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrikeNode;
import org.pegdown.ast.StrongEmphSuperNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TaskListNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class ToJsonSerializer implements Visitor {

//    private static final String COLON = ": ";
	private static final char QUOTE = '"';
	private static final String CLOSE_BRACKET = "}";
	private static final String OPEN_BRACKET = "{";
	private static final char SQUARE_CLOSE = ']';
	private static final char SQUARE_OPEN = '[';
	private static final String COLON = ": ";
	private static final String COMMA = ",";
	protected Printer printer = new Printer();
    protected final Map<String, ReferenceNode> references = new HashMap<String, ReferenceNode>();
    protected final Map<String, String> abbreviations = new HashMap<String, String>();
    protected final LinkRenderer linkRenderer;
    protected final List<ToHtmlSerializerPlugin> plugins;

    protected TableNode currentTableNode;
    protected int currentTableColumn;
    protected boolean inTableHeader;

    protected Map<String, VerbatimSerializer> verbatimSerializers;

    public ToJsonSerializer(LinkRenderer linkRenderer) {
        this(linkRenderer, Collections.<ToHtmlSerializerPlugin>emptyList());
    }

    public ToJsonSerializer(LinkRenderer linkRenderer, List<ToHtmlSerializerPlugin> plugins) {
        this(linkRenderer, Collections.<String, VerbatimSerializer>emptyMap(), plugins);
    }

    public ToJsonSerializer(final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers) {
        this(linkRenderer, verbatimSerializers, Collections.<ToHtmlSerializerPlugin>emptyList());
    }

    public ToJsonSerializer(final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers, final List<ToHtmlSerializerPlugin> plugins) {
        this.linkRenderer = linkRenderer;
        this.verbatimSerializers = new HashMap<String, VerbatimSerializer>(verbatimSerializers);
        if (!this.verbatimSerializers.containsKey(VerbatimSerializer.DEFAULT)) {
            this.verbatimSerializers.put(VerbatimSerializer.DEFAULT, DefaultVerbatimSerializer.INSTANCE);
        }
        this.plugins = plugins;
    }

    public ToJsonSerializer() {
        this(new LinkRenderer());
	}

	public String toJson(RootNode astRoot) {
        checkArgNotNull(astRoot, "astRoot");
        astRoot.accept(this);
        return this.printer.getString();
    }

    public void visit(RootNode node) {
        for (ReferenceNode refNode : node.getReferences()) {
            visitChildren(refNode);
            this.references.put(normalize(this.printer.getString()), refNode);
            this.printer.clear();
        }
        for (AbbreviationNode abbrNode : node.getAbbreviations()) {
            visitChildren(abbrNode);
            String abbr = this.printer.getString();
            this.printer.clear();
            abbrNode.getExpansion().accept(this);
            String expansion = this.printer.getString();
            this.abbreviations.put(abbr, expansion);
            this.printer.clear();
        }
        visitChildren(node);
    }

    public void visit(AbbreviationNode node) {
    	//TODO do something
    }

    public void visit(AnchorLinkNode node) {
        printLink(this.linkRenderer.render(node));
    }

    public void visit(AutoLinkNode node) {
        printLink(this.linkRenderer.render(node));
    }

    public void visit(BlockQuoteNode node) {
    	//TODO
    }

    public void visit(BulletListNode node) {
//        printArray(node);
        visitChildren(node);
    }

    public void visit(CodeNode node) {
        this.printer.print(OPEN_BRACKET).indent(+2).printchkln();
        printTag(node, "code");
        this.printer.indent(-2).printchkln().print(CLOSE_BRACKET);
    }

    public void visit(DefinitionListNode node) {
    	//TODO
    }

    public void visit(DefinitionNode node) {
        printConditionallyIndentedTag(node, "dd");
    }

    public void visit(DefinitionTermNode node) {
        printConditionallyIndentedTag(node, "dt");
    }

    public void visit(ExpImageNode node) {
        String text = printChildrenToString(node);
        printImageTag(this.linkRenderer.render(node, text));
    }

    public void visit(ExpLinkNode node) {
        String text = printChildrenToString(node);
        printLink(this.linkRenderer.render(node, text));
    }

    public void visit(HeaderNode node) {
        printBreakBeforeTag(node, "h" + node.getLevel());
    }

    public void visit(HtmlBlockNode node) {
        String text = node.getText();
        if (text.length() > 0) this.printer.println();
        this.printer.print(text);
    }

    public void visit(InlineHtmlNode node) {
        this.printer.print(node.getText());
    }

    public void visit(ListItemNode node) {
        if (node instanceof TaskListNode) {
            // vsch: #185 handle GitHub style task list items, these are a bit messy because the <input> checkbox needs to be
            // included inside the optional <p></p> first grand-child of the list item, first child is always RootNode
            // because the list item text is recursively parsed.
            Node firstChild = node.getChildren().get(0).getChildren().get(0);
            boolean firstIsPara = firstChild instanceof ParaNode;
            int indent = node.getChildren().size() > 1 ? 2 : 0;
            boolean startWasNewLine = this.printer.endsWithNewLine();

            this.printer.println().print("<li class=\"task-list-item\">").indent(indent);
            if (firstIsPara) {
                this.printer.println().print("<p>");
                this.printer.print("<input type=\"checkbox\" class=\"task-list-item-checkbox\"" + (((TaskListNode) node).isDone() ? " checked=\"checked\"" : "") + " disabled=\"disabled\"></input>");
                visitChildren((SuperNode) firstChild);

                // render the other children, the p tag is taken care of here
                visitChildrenSkipFirst(node);
                this.printer.print("</p>");
            } else {
                this.printer.print("<input type=\"checkbox\" class=\"task-list-item-checkbox\"" + (((TaskListNode) node).isDone() ? " checked=\"checked\"" : "") + " disabled=\"disabled\"></input>");
                visitChildren(node);
            }
            this.printer.indent(-indent).printchkln(indent != 0).print("</li>")
                    .printchkln(startWasNewLine);
        } else {
            printConditionallyIndentedTag(node, "li");
        }
    }

    public void visit(MailLinkNode node) {
        printLink(this.linkRenderer.render(node));
    }

    public void visit(OrderedListNode node) {
    	//TODO
    }

    public void visit(ParaNode node) {
        printBreakBeforeTag(node, "p");
    }

    public void visit(QuotedNode node) {
        switch (node.getType()) {
            case DoubleAngle:
                this.printer.print("&laquo;");
                visitChildren(node);
                this.printer.print("&raquo;");
                break;
            case Double:
                this.printer.print("&ldquo;");
                visitChildren(node);
                this.printer.print("&rdquo;");
                break;
            case Single:
                this.printer.print("&lsquo;");
                visitChildren(node);
                this.printer.print("&rsquo;");
                break;
        }
    }

    public void visit(ReferenceNode node) {
        // reference nodes are not printed
    }

    public void visit(RefImageNode node) {
        String text = printChildrenToString(node);
        String key = node.referenceKey != null ? printChildrenToString(node.referenceKey) : text;
        ReferenceNode refNode = this.references.get(normalize(key));
        if (refNode == null) { // "fake" reference image link
            this.printer.print('!').print(SQUARE_OPEN).print(text).print(SQUARE_CLOSE);
            if (node.separatorSpace != null) {
                this.printer.print(node.separatorSpace).print(SQUARE_OPEN);
                if (node.referenceKey != null) this.printer.print(key);
                this.printer.print(SQUARE_CLOSE);
            }
        } else printImageTag(this.linkRenderer.render(node, refNode.getUrl(), refNode.getTitle(), text));
    }

    public void visit(RefLinkNode node) {
        String text = printChildrenToString(node);
        String key = node.referenceKey != null ? printChildrenToString(node.referenceKey) : text;
        ReferenceNode refNode = this.references.get(normalize(key));
        if (refNode == null) { // "fake" reference link
            this.printer.print(SQUARE_OPEN).print(text).print(SQUARE_CLOSE);
            if (node.separatorSpace != null) {
                this.printer.print(node.separatorSpace).print(SQUARE_OPEN);
                if (node.referenceKey != null) this.printer.print(key);
                this.printer.print(SQUARE_CLOSE);
            }
        } else printLink(this.linkRenderer.render(node, refNode.getUrl(), refNode.getTitle(), text));
    }

    public void visit(SimpleNode node) {
        switch (node.getType()) {
            case Apostrophe:
                this.printer.print("&rsquo;");
                break;
            case Ellipsis:
                this.printer.print("&hellip;");
                break;
            case Emdash:
                this.printer.print("&mdash;");
                break;
            case Endash:
                this.printer.print("&ndash;");
                break;
            case HRule:
                this.printer.println().print("<hr/>");
                break;
            case Linebreak:
                this.printer.print("<br/>");
                break;
            case Nbsp:
                this.printer.print("&nbsp;");
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void visit(StrongEmphSuperNode node) {
        if (node.isClosed()) {
            if (node.isStrong())
                printTag(node, "strong");
            else
                printTag(node, "em");
        } else {
            //sequence was not closed, treat open chars as ordinary chars
            this.printer.print(node.getChars());
            visitChildren(node);
        }
    }

    public void visit(StrikeNode node) {
        printTag(node, "del");
    }

    public void visit(TableBodyNode node) {
    	//TODO
    }

    public void visit(TableCaptionNode node) {
        this.printer.println().print("<caption>");
        visitChildren(node);
        this.printer.print("</caption>");
    }

    public void visit(TableCellNode node) {
        String tag = this.inTableHeader ? "th" : "td";
        List<TableColumnNode> columns = this.currentTableNode.getColumns();
        TableColumnNode column = columns.get(Math.min(this.currentTableColumn, columns.size() - 1));

        this.printer.println().print('<').print(QUOTE).print(tag).print(QUOTE);
        column.accept(this);
        if (node.getColSpan() > 1) this.printer.print(" colspan=\"").print(Integer.toString(node.getColSpan())).print(QUOTE);
        this.printer.print('>');
        visitChildren(node);
        this.printer.print('<').print('/').print(QUOTE).print(tag).print(QUOTE).print('>');

        this.currentTableColumn += node.getColSpan();
    }

    public void visit(TableColumnNode node) {
        switch (node.getAlignment()) {
            case None:
                break;
            case Left:
                this.printer.print(" align=\"left\"");
                break;
            case Right:
                this.printer.print(" align=\"right\"");
                break;
            case Center:
                this.printer.print(" align=\"center\"");
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void visit(TableHeaderNode node) {
        this.inTableHeader = true;
//        printIndentedTag(node, "thead");
        this.inTableHeader = false;
    }

    public void visit(TableNode node) {
        this.currentTableNode = node;
//        printIndentedTag(node, "table");
        this.currentTableNode = null;
    }

    public void visit(TableRowNode node) {
        this.currentTableColumn = 0;
//        printIndentedTag(node, "tr");
    }

    public void visit(VerbatimNode node) {
        VerbatimSerializer serializer = lookupSerializer(node.getType());
        serializer.serialize(node, this.printer);
    }

    protected VerbatimSerializer lookupSerializer(final String type) {
        if (type != null && this.verbatimSerializers.containsKey(type)) {
            return this.verbatimSerializers.get(type);
        }
		return this.verbatimSerializers.get(VerbatimSerializer.DEFAULT);
    }

    public void visit(WikiLinkNode node) {
        printLink(this.linkRenderer.render(node));
    }

    public void visit(TextNode node) {
        if (this.abbreviations.isEmpty()) {
        	if(!"".equals(node.getText().trim())){
        		this.printer.print(QUOTE).print(node.getText().trim()).print(QUOTE);
        	}
        } else {
            printWithAbbreviations(node.getText());
        }
    }

    public void visit(SpecialTextNode node) {
        this.printer.printEncoded(node.getText());
    }

    public void visit(SuperNode node) {
    	if (node instanceof MSONNode) {
    		MSONNode cNode = (MSONNode) node;
            for (ToHtmlSerializerPlugin plugin : this.plugins) {
	            if (plugin.visit(cNode, this, this.printer)) {
	                return;
	            }
            }
    	} else printArray(node);
    }
    
    public void visit(Node node) {
        for (ToHtmlSerializerPlugin plugin : this.plugins) {
        	if (node instanceof MSONNode) {
        		MSONNode cNode = (MSONNode) node;
	            if (plugin.visit(cNode, this, this.printer)) {
	                return;
	            }
        	}
        }
        // override this method for processing custom Node implementations
        throw new RuntimeException("Don't know how to handle node " + node);
    }

    // helpers
    protected void visitChildren(SuperNode node) {
		for (Iterator<Node> iterator = node.getChildren().iterator(); iterator.hasNext();) {
			Node child = iterator.next();
			child.accept(this);
			if(iterator.hasNext()) this.printer.print(',');
		}
    }

    // helpers
    protected void visitChildrenSkipFirst(SuperNode node) {
        boolean first = true;
        for (Node child : node.getChildren()) {
            if (!first) child.accept(this);
            first = false;
        }
    }

    protected void printTag(TextNode node, String tag) {
        printTagValue(tag);
        this.printer.printEncoded(node.getText());
    }

    protected void printTag(SuperNode node, String tag) {
        this.printer.print(OPEN_BRACKET).indent(+2).printchkln();
        printTagValue(tag);
        visitChildren(node);
        this.printer.indent(-2).printchkln().print(CLOSE_BRACKET);
    }

    protected void printBreakBeforeTag(SuperNode node, String tag) {
        boolean startWasNewLine = this.printer.endsWithNewLine();
        this.printer.println();
        printTag(node, tag);
        if (startWasNewLine) this.printer.println();
    }

    protected void printArray(SuperNode node) {
		this.printer.print(SQUARE_OPEN).indent(+2).printchkln();
		visitChildren(node);
		this.printer.indent(-2).printchkln(true).print(SQUARE_CLOSE);
    }

    protected void printConditionallyIndentedTag(SuperNode node, String tag) {
        if (node.getChildren().size() > 1) {
            this.printer.print(OPEN_BRACKET).indent(+2).printchkln();            
            printTagValue(tag);            
            this.printer.print(OPEN_BRACKET).indent(+2).printchkln();
            visitChildren(node);
            this.printer.indent(-2).printchkln().print(CLOSE_BRACKET);
            
            this.printer.indent(-2).printchkln(true).print(CLOSE_BRACKET);
        } else {
            boolean startWasNewLine = this.printer.endsWithNewLine();
//            this.printer.println().print(QUOTE);
            visitChildren(node);
            this.printer.printchkln(startWasNewLine);
        }
    }

	private void printTagValue(String tag) {
		this.printer.print(QUOTE).print("tag").print(QUOTE).print(COLON).print("\"").print(tag).print("\"").print(COMMA).println();
		this.printer.print(QUOTE).print("value").print(QUOTE).print(COLON);
	}

    protected void printImageTag(LinkRenderer.Rendering rendering) {
        this.printer.print("<img");
        printAttribute("src", rendering.href);
        // shouldn't include the alt attribute if its empty
        if(!rendering.text.equals("")){
            printAttribute("alt", rendering.text);
        }
        for (LinkRenderer.Attribute attr : rendering.attributes) {
            printAttribute(attr.name, attr.value);
        }
        this.printer.print(" />");
    }

    protected void printLink(LinkRenderer.Rendering rendering) {
        this.printer.print('<').print('a');
        printAttribute("href", rendering.href);
        for (LinkRenderer.Attribute attr : rendering.attributes) {
            printAttribute(attr.name, attr.value);
        }
        this.printer.print('>').print(rendering.text).print("</a>");
    }

    protected void printAttribute(String name, String value) {
        this.printer.print(' ').print(name).print(':').print(QUOTE).print(value).print(QUOTE);
    }

    protected String printChildrenToString(SuperNode node) {
        Printer priorPrinter = this.printer;
        this.printer = new Printer();
        visitChildren(node);
        String result = this.printer.getString();
        this.printer = priorPrinter;
        return result;
    }

    protected String normalize(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case ' ':
                case '\n':
                case '\t':
                    continue;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    protected void printWithAbbreviations(String string) {
        Map<Integer, Map.Entry<String, String>> expansions = null;

        for (Map.Entry<String, String> entry : this.abbreviations.entrySet()) {
            // first check, whether we have a legal match
            String abbr = entry.getKey();

            int ix = 0;
            while (true) {
                int sx = string.indexOf(abbr, ix);
                if (sx == -1) break;

                // only allow whole word matches
                ix = sx + abbr.length();

                if (sx > 0 && Character.isLetterOrDigit(string.charAt(sx - 1))) continue;
                if (ix < string.length() && Character.isLetterOrDigit(string.charAt(ix))) {
                    continue;
                }

                // ok, legal match so save an expansions "task" for all matches
                if (expansions == null) {
                    expansions = new TreeMap<Integer, Map.Entry<String, String>>();
                }
                expansions.put(sx, entry);
            }
        }

        if (expansions != null) {
            int ix = 0;
            for (Map.Entry<Integer, Map.Entry<String, String>> entry : expansions.entrySet()) {
                int sx = entry.getKey();
                String abbr = entry.getValue().getKey();
                String expansion = entry.getValue().getValue();

                this.printer.printEncoded(string.substring(ix, sx));
                this.printer.print("<abbr");
                if (StringUtils.isNotEmpty(expansion)) {
                    this.printer.print(" title=\"");
                    this.printer.printEncoded(expansion);
                    this.printer.print(QUOTE);
                }
                this.printer.print('>');
                this.printer.printEncoded(abbr);
                this.printer.print("</abbr>");
                ix = sx + abbr.length();
            }
            this.printer.print(string.substring(ix));
        } else {
            this.printer.print(string);
        }
    }

}
