package org.mson;

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
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;

public class MSONNodeFactory {

	public MSONNodeFactory() {
	}

	// public MSONNode create(Node node) {
	// MyVisitor mv = new MyVisitor();
	// node.accept(mv);
	// return mv.created;
	// }

	public Node createMSONString(String string) {
		return new MSONStringNode(new TextNode(string));
	}

	public MSONStringNode createMSONString(Node node) {
		MyVisitor mv = new MyVisitor();
		node.accept(mv);
		return new MSONStringNode(mv.searched);
	}

	public MSONObjectNode createMSONObject() {
		return new MSONObjectNode();
	}

	public MSONObjectNode createMSONObject(Node node) {
		MSONObjectNode m;
		MyVisitor mv = new MyVisitor();
		node.accept(mv);
		if (mv.searched instanceof MSONObjectNode) {
			m = (MSONObjectNode) mv.searched;
		} else {
			m = new MSONObjectNode();
			if (mv.searched instanceof MSONNode) {
				m.addProperty(mv.searched);
			} else {
				m.addName(this.createMSONString(mv.searched));
			}
		}
		return m;
	}

	public MSONArrayNode createMSONArray(Node node) {
		// create an MSONArrayNode
		MSONArrayNode m;
		if (node instanceof MSONArrayNode) {
			m = (MSONArrayNode) node;
		} else {
			m = new MSONArrayNode();
			if (node instanceof MSONObjectNode) {
				m.addItem(node);
			} else if (node instanceof SuperNode) {
				// collapse special text and other text nodes
				StringBuilder sb = new StringBuilder();
				for (Node c : node.getChildren()) {
					MyVisitor mv = new MyVisitor();
					c.accept(mv);
//					if (mv.searched instanceof MSONNode) {
//						if (sb.toString().trim().length() > 0) {
//							m.addItem(this.createMSONString(sb.toString().trim()));
//							sb.setLength(0);
//						}
//						m.addItem(mv.searched);
//					} else {
						if(mv.searched instanceof TextNode) {
							sb.append(((TextNode) mv.searched).getText().trim());
						} else {
							if (sb.toString().trim().length() > 0) {
								m.addItem(this.createMSONString(sb.toString().trim()));
								sb.setLength(0);
							}
							m.addItem(this.createMSONString(mv.searched));
						}
//					}
				}
				if (sb.toString().trim().length() > 0) {
					m.addItem(this.createMSONString(sb.toString().trim()));
				}
			} else {
				MyVisitor mv = new MyVisitor();
				node.accept(mv);
				m.addItem((mv.searched instanceof MSONNode) ? mv.searched : this.createMSONString(mv.searched));
			}
		}
		return m;
	}

	class MyVisitor implements Visitor {

		public Node searched;

		// TODO: remove sysout and do proper handling

		public void visit(AbbreviationNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(AnchorLinkNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(AutoLinkNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(BlockQuoteNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(BulletListNode node) {
			System.out.println(node);
//			this.searched = createMSONArray(node);
			visit((SuperNode) node);
		}

		public void visit(CodeNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(DefinitionListNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(DefinitionNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(DefinitionTermNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(ExpImageNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(ExpLinkNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(HeaderNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(HtmlBlockNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(InlineHtmlNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(ListItemNode node) {
			visit(node.getChildren().get(0));
		}

		public void visit(MailLinkNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(OrderedListNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(ParaNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(QuotedNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(ReferenceNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(RefImageNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(RefLinkNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(SimpleNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(SpecialTextNode node) {
			visit((Node) node);
		}

		public void visit(StrikeNode node) {
			visit((Node) node);
		}

		public void visit(StrongEmphSuperNode node) {
			visit((Node) node);
		}

		public void visit(TableBodyNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(TableCaptionNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(TableCellNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(TableColumnNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(TableHeaderNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(TableNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(TableRowNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(VerbatimNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(WikiLinkNode node) {
			System.out.println(node);
			visit((Node) node);
		}

		public void visit(TextNode node) {
			visit((Node) node);
		}

		public void visit(SuperNode node) {
			if (node instanceof MSONNode) {
				visit((MSONNode) node);
			} else {
				if (node.getChildren().size() > 1) {
					// create an MSONObjectNode
					MSONArrayNode a = createMSONArray(node);
					
					this.searched = (a.getChildren().size() == 1) ? a.getChildren().get(0) : a;
					
				} else if (node.getChildren().size() > 0) {
					node.getChildren().get(0).accept(this);
				}
			}
		}

		public void visit(RootNode node) {
			if(node.getChildren().size() == 1) {
				node.getChildren().get(0).accept(this);
			} else {
				this.searched = new MSONStringNode(node);
			}
		}

		public void visit(Node node) {
			this.searched = node;
		}
	}
}