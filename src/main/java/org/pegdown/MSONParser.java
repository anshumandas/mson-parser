package org.pegdown;

import static org.parboiled.errors.ErrorUtils.printParseErrors;

import java.util.List;

import org.mson.MSONArrayNode;
import org.mson.MSONNode;
import org.mson.MSONNodeFactory;
import org.mson.MSONObjectNode;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.StringBuilderVar;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;
import org.pegdown.plugins.BlockPluginParser;
import org.pegdown.plugins.InlinePluginParser;

/**
 * Parses MSON
 *
 * key: value (param(,s)) 
 * name_value (param(,s))
 * name: property(,s) (param(,s)) //where key is anything other than string 'name'
 */

public class MSONParser extends Parser implements InlinePluginParser, BlockPluginParser {
	
	private Parser internalParser;	
	MSONNodeFactory msonFactory = new MSONNodeFactory();
    
	public MSONParser() {
		super(ALL, 1000000l, DefaultParseRunnerProvider);
	}
	
	public Rule[] blockPluginRules() {
		return new Rule[] { MSONListBlock() };
	}

	public Rule BackQuoteBlock() {
		// `anything here is converted to a CodeNoed `

		return Code(Ticks(1));
	}

	public Rule[] inlinePluginRules() {
		return new Rule[] { BackQuoteBlock(), MSONInLine() }; //TODO how to get code block back in parsing the overall
	}

	// parses :
	// name: comma separated list items (comma separated params)
	public Rule MSONInLine() {
		return NodeSequence(
			Test(TestMSON()), 
			MSON(), ZeroOrMore(Newline()), EOI 
		);
	}

	public Rule whitespace() {
		return ZeroOrMore(AnyOf(" \t\f"));
	}

	public Rule items() {
		StringBuilderVar item = new StringBuilderVar();
		return Sequence(
			whitespace(), 
			OneOrMore(
			   TestNot(AnyOf(",[]()\n")),
               BaseParser.ANY,
               item.append(matchedChar())
            ), 
			Optional(Ch('['),
		        item.append(" of "),
				OneOrMore(
				   TestNot(Ch(']')),
	               BaseParser.ANY,
	               item.append(matchedChar())
			    ),
				Ch(']')
			),
			push(item), 
			//debugMsg("item ", item.getString()),
			whitespace(),
			Optional(Ch(','))
		);
	}

	public Rule TestMSON() {
		return FirstOf(
			// key? :? value? (params)? - description	
			Sequence(
				OneOrMore(TestNot(Ch('-')), TestNot(Newline()), ANY), 
				Ch('-'), OneOrMore(TestNot(Newline()), ANY),
//				debugMsg("hi", "1"),
				Optional(Newline())
			),
			// key? :? value? (params) -? description?	
			Sequence(
				ZeroOrMore(TestNot(Ch('(')), TestNot(Newline()), ANY), 
				Ch('('), OneOrMore(TestNot(Ch(')')), ANY), Ch(')'),
				ZeroOrMore(TestNot(Newline()), ANY),
//				debugMsg("hi", "2"),
				Optional(Newline())
			),
			// key : value* (params)? -? description?	
			Sequence(
				OneOrMore(TestNot(Ch(':')), TestNot(Newline()), ANY), 
				Ch(':'), 
				OneOrMore(TestNot(Newline()), ANY),
//				debugMsg("hi", "3"),
				Optional(Newline())
			),
			// key? :? value,* value? (params)? -? description?	
			Sequence(
				OneOrMore(TestNot(AnyOf(" ,")), TestNot(Newline()), ANY), 
//				debugMsg("hi", getContext().getInputBuffer().extractLine(1)),
				whitespace(), Ch(','), ZeroOrMore(OneOrMore(TestNot(AnyOf(" ,")), TestNot(Newline()), ANY), whitespace(), Optional(Ch(',')) ),
				Optional(Newline())
			)
		);
	}
	
	public Rule MSON() {
		StringBuilderVar name = new StringBuilderVar();

		return Sequence(
			Test(TestMSON()), 
//			debugMsg("hi", "5"),	
			push(this.msonFactory.createMSONObject()),
			FirstOf(
			Sequence(
					// name: property(,s) (params)? - description?
					name.clearContents(),
					Test(FirstOf(OneOrMore(TestNot(AnyOf(",:(\n")), TestNot(Newline()), ANY), isCode()), Ch(':'), OneOrMore(OneOrMore(TestNot(AnyOf(",()\n")), ANY), whitespace(), Optional(Ch(',')), whitespace()), ZeroOrMore(TestNot(Newline()), ANY), Newline()), 	
					whitespace(), Optional(FirstOf(OneOrMore(TestNot(AnyOf(", :(\n")), BaseParser.ANY, name.append(matchedChar())), Sequence(isCode(), name.append(getCode()))), push(name), addName(), name.clearContents()),
					whitespace(), Ch(':'), 
					OneOrMore(TestNot(AnyOf(",()\n")), items(), addSample())
				),
//				Sequence(
//					// key:value (params)? - description?	
//					name.clearContents(),
//					Test(OneOrMore(TestNot(AnyOf(",:(\n")), TestNot(Newline()), ANY), Ch(':'), OneOrMore(TestNot(AnyOf(",()\n")), ANY), ZeroOrMore(TestNot(Newline()), ANY), Newline()),	
//					whitespace(), OneOrMore(TestNot(AnyOf(", :(\n")), BaseParser.ANY, name.append(matchedChar())), 
//					whitespace(), Ch(':'), items(), addProperty(name.getString()), name.clearContents()
//				),
				Sequence(
					// name_value? (params) - description	
					name.clearContents(),
					Test(ZeroOrMore(TestNot(AnyOf(",()\n")), ANY), whitespace(), FirstOf(Ch('('), Ch('-')), OneOrMore(TestNot(Newline()), ANY), Newline()), 
					Optional(whitespace(), OneOrMore(TestNot(AnyOf(", :(\n")), BaseParser.ANY, name.append(matchedChar())), push(name), addName(), name.clearContents())
				),
				Sequence(
					// property(,s) (params) - description
					name.clearContents(),
					Test(OneOrMore(OneOrMore(TestNot(AnyOf(",()\n")), ANY), whitespace(), Optional(Ch(','))), FirstOf(Ch('('), Ch('-')), OneOrMore(TestNot(Newline()), ANY), Newline()),	
					OneOrMore(TestNot(AnyOf(",()\n")), items(), addSample())
				),
				Sequence(
					// property, property(,s)
					name.clearContents(),
					Test(whitespace(), OneOrMore(TestNot(AnyOf(" ,()\n")), ANY), whitespace(), Ch(','), OneOrMore(whitespace(), OneOrMore(TestNot(AnyOf(" ,()\n")), ANY), whitespace(), Optional(Ch(','))), ANY, Newline()), 	
					OneOrMore(TestNot(AnyOf(",()\n")), items(), addSample())
				)
			), 
			//(params)
			Optional(whitespace(), Ch('('), whitespace(), items(), addType(), ZeroOrMore(whitespace(), items(), addAttribute()), whitespace(), Ch(')')),
			DescriptionRule()
		);
	}

	public String getCode() {
		Object peek = ((Node) peek(2)).getChildren().get(0);
		return ((CodeNode) peek).getText();
	}

	public boolean isCode() {
		Object peek = null;
		try {
			peek = ((Node) peek(2)).getChildren().get(0);
		} catch (Exception e) {
			//
		}
		return peek instanceof CodeNode;
	}

	public Rule DescriptionRule() {
		StringBuilderVar desc = new StringBuilderVar();
		return Optional(
				whitespace(), Ch('-'), whitespace(), 
				OneOrMore(TestNot(EOI), BaseParser.ANY, desc.append(matchedChar())), 
				push(desc), addProperty("description"), desc.clearContents()
		);
	}

	public boolean addName() {
		MSONObjectNode parent = (MSONObjectNode) peek(1);
        StringBuilderVar var = (StringBuilderVar) pop();
		RootNode child = parseInternal(var);
        parent.addName(this.msonFactory.createMSONString(child));
		var.clearContents();
		return true;
	}
	
	public boolean addSample() {
		MSONObjectNode parent = (MSONObjectNode) peek(1);
        StringBuilderVar var = (StringBuilderVar) pop();
		RootNode child = parseInternal(var);
        parent.addSample(this.msonFactory.createMSONString(child));
		var.clearContents();
		return true;
	}
	
	public boolean addType() {
		MSONObjectNode parent = (MSONObjectNode) peek(1);
        StringBuilderVar var = (StringBuilderVar) pop();
		RootNode child = parseInternal(var);
        parent.addType(this.msonFactory.createMSONString(child));
		var.clearContents();
		return true;
	}
	
	public boolean addAttribute() {
		MSONObjectNode parent = (MSONObjectNode) peek(1);
        StringBuilderVar var = (StringBuilderVar) pop();
		RootNode child = parseInternal(var);
        parent.addAttribute(this.msonFactory.createMSONString(child));
		var.clearContents();
		return true;
	}
	
	public boolean addProperty(java.lang.String key) {
		MSONObjectNode parent = (MSONObjectNode) peek(1);
        StringBuilderVar var = (StringBuilderVar) pop();
		RootNode child = parseInternal(var);
        parent.addField(key, this.msonFactory.createMSONString(child));
		var.clearContents();
		return true;
	}

	public Rule MSONListBlock() {
		return NodeSequence(
			Test(
				whitespace(), Bullet(), 
				TestMSON()
			), 
			whitespace(), MSONList(),
			ZeroOrMore(whitespace(), Newline()), EOI 
		);
	}

    public Rule MSONList() {
        SuperNodeCreator itemNodeCreator = new SuperNodeCreator() {
            public SuperNode create(Node child) {
            	SuperNode n = MSONParser.this.msonFactory.createMSONObject(child);
                return n;
            }
        };
        return NodeSequence(
            ListItem(Bullet(), itemNodeCreator), push(this.msonFactory.createMSONArray(this.msonFactory.createMSONObject(popAsNode()))),
            ZeroOrMore(
                //vsch: this one will absorb all blank lines but the last one preceding a list item otherwise two blank lines end a list and start a new one
                ZeroOrMore(Test(BlankLine(), BlankLine()), BlankLine()),
                ListItem(Bullet(), itemNodeCreator), addAsChild()
            )
        );
    }

    @Override
	public boolean addAsChild() {
        SuperNode parent = (SuperNode) peek(1);
        if(parent instanceof MSONObjectNode) {
        	MSONObjectNode m = (MSONObjectNode) parent;
        	m.addProperty(this.msonFactory.createMSONObject(popAsNode()));
        } else {
	        List<Node> children = parent.getChildren();
	        Node child = popAsNode();
	        if (child.getClass() == TextNode.class && !children.isEmpty()) {
	            Node lastChild = children.get(children.size() - 1);
	            if (lastChild.getClass() == TextNode.class) {
	                // collapse peer TextNodes
	                TextNode last = (TextNode) lastChild;
	                TextNode current = (TextNode) child;
	                last.append(current.getText());
	                last.setEndIndex(current.getEndIndex());
	                return true;
	            }
	        }
	        if(parent instanceof MSONArrayNode) {
	        	MSONArrayNode m = (MSONArrayNode) parent;
	        	m.addItem((child instanceof MSONNode) ? child : this.msonFactory.createMSONString(child));
	        } else {
	        	children.add(child);
	        }
        }
        
        return true;
    }
    
    @Override
	public RootNode parseInternal(char[] source) {
        ParsingResult<Node> result = this.internalParser.parseToParsingResult(source);
        if (result.hasErrors()) {
            throw new RuntimeException("Internal error during markdown parsing:\n--- ParseErrors ---\n" +
                    printParseErrors(result)/* +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(result)*/
            );
        }
        return (RootNode) result.resultValue;
    }
    
	public void setInternalParser(Parser parser) {
		this.internalParser = parser;		
	}
	
	boolean debug(@SuppressWarnings("rawtypes") Context context) {
		System.out.println(context);
	    return true; // set permanent breakpoint here
	}

}
