/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on May 28, 2005
 */
package bp.common.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import bp.common.fp.Generator;

/**
 * From web.. "Gary Olsen's Interpretation of the "Newick's 8:45" Tree Format
 * Standard
 * <p>
 * 
 * Conventions: Items in { } may appear zero or more times. Items in [ ] are
 * optional, they may appear once or not at all. All other punctuation marks
 * (colon, semicolon, parentheses, comma and single quote) are required parts of
 * the format.
 * 
 * <ul>
 * <li>tree ==> descendant_list [ root_label ] [ : branch_length ] ;
 * 
 * <li>descendant_list ==> ( subtree { , subtree } )
 * 
 * <li>subtree
 * <ul>
 * <li>==> descendant_list [internal_node_label] [: branch_length]
 * <li>==> leaf_label [: branch_length]
 * </ul>
 * <li>root_label==> label
 * <li>internal_node_label ==> label
 * <li>leaf_label ==> label
 * <li>label
 * <ul>
 * <li>==> unquoted_label
 * <li>==> quoted_label
 * </ul>
 * 
 * <li>unquoted_label==> string_of_printing_characters
 * <li>quoted_label ==> ' string_of_printing_characters '
 * 
 * 
 * <li>branch_length
 * <ul>
 * <li>==> signed_number
 * <li>==> unsigned_number
 * </ul>
 * </ul>
 * 
 * Notes: Unquoted labels may not contain blanks, parentheses, square brackets,
 * single_quotes, colons, semicolons, or commas. Underscore characters in
 * unquoted labels are converted to blanks. (Note This does not work!....)
 * Single quote characters in a quoted label are represented by two single
 * quotes. Blanks or tabs may appear anywhere except within unquoted labels or
 * branch_lengths. Newlines may appear anywhere except within labels or
 * branch_lengths. Comments are enclosed in square brackets and may appear
 * anywhere newlines are permitted.
 * <p>
 * 
 * Other notes: PAUP (David Swofford) allows nesting of comments. TreeAlign
 * (Jotun Hein) writes a root node branch length (with a value of 0.0). PHYLIP
 * (Joseph Felsenstein) requires that an unrooted tree begin with a
 * trifurcation; it will not "uproot" a rooted tree.
 * <p>
 * 
 * Example:
 * <p>
 * (((One:0.2,Two:0.3):0.3,(Three:0.5,Four:0.3):0.2):0.3,Five:0.7):0.0;
 * 
 * @author benedictpaten
 */
public class NewickTreeParser {

    public static class Node {
        private static final Pattern noQuotesNeeded = Pattern
                .compile("[a-zA-Z0-9]*");

        public double edgeLength = Double.MIN_VALUE;

        private Node parent;

        private final LinkedList<Object> nodes = new LinkedList<Object>();

        public Object o = null;

        /**
         * @param parent2
         */
        public Node(final Node parent) {
            parent.addNode(this);
        }

        public Node() {
            //default
        }

        @Override
		public String toString() {
            final StringBuffer sB = new StringBuffer();
            if (this.nodes.size() != 0) {
                sB.append("(");
                int i = 0;
                sB.append(this.nodes.get(i).toString());
                while (++i < this.nodes.size()) {
                    sB.append(",");
                    sB.append(this.nodes.get(i).toString());
                }
                sB.append(")");
            }
            if (this.o != null) {
                sB.append(Node.noQuotesNeeded.matcher(this.o.toString())
                        .matches() ? this.o.toString() : "'"
                        + this.o.toString() + "'");
            }
            if (this.edgeLength != Double.MIN_VALUE) {
				sB.append(":" + this.edgeLength);
			}
            if (this.parent == null) {
                sB.append(";");
            }
            return sB.toString();
        }

        public NewickTreeParser.Node getParent() {
            return this.parent;
        }

        public List<Object> getNodes() {
            return (List<Object>) this.nodes.clone();
        }

        public void addNode(final Node n) {
            this.nodes.add(n);
            if (Debug.DEBUGCODE && (n.parent != null)
                    && (n.parent != this)) {
				throw new IllegalStateException();
			}
            n.parent = this;
        }

        public void removeNode(final int i) {
            final Node n = (Node) this.nodes.remove(i);
            n.parent = null;
        }

        public Node setNode(final int i, final Node n) {
            final Node m = (Node) this.nodes.set(i, n);
            m.parent = null;
            if (Debug.DEBUGCODE && (n.parent != null)
                    && (n.parent != this)) {
				throw new IllegalStateException();
			}
            n.parent = this;
            return m;
        }
    }

    /**
     * Translate reader into stream of token strings. Does not remove quote
     * strings from quoted words.
     * 
     * @param r
     * @return
     * @throws IOException
     */
    public static Generator tokenise(final Reader r) {
        return new Generator() {
            StreamTokenizer sT = new StreamTokenizer(r);
            {
                sT.resetSyntax();
                //sT.eolIsSignificant(true);
                this.sT.whitespaceChars(0, 32);
                this.sT.wordChars(33, 255);
                this.sT.ordinaryChar('(');
                this.sT.ordinaryChar(')');
                this.sT.ordinaryChar('[');
                this.sT.ordinaryChar(']');
                this.sT.ordinaryChar('{');
                this.sT.ordinaryChar('}');
                this.sT.ordinaryChar(',');
                this.sT.ordinaryChar(':');
                this.sT.ordinaryChar(';');
                this.sT.quoteChar('\'');
                this.sT.quoteChar('\"');
            }

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                int currentToken;
                try {
                    currentToken = this.sT.nextToken();
                } catch (final IOException e) {
                    throw new IllegalStateException();
                }
                //System.out.println("print word " + this.sT.sval + " " + this.sT.nval);
                switch (currentToken) {
                case StreamTokenizer.TT_EOF:
                    return null;
                case StreamTokenizer.TT_WORD:
                	//System.out.println("print word " + this.sT.sval);
                    return this.sT.sval;
                case '\'':
                case '\"':
                	 return this.sT.sval;
                //case StreamTokenizer.TT_NUMBER:
                //	 System.out.println("print value " + this.sT.nval);
                //    return "" + this.sT.nval;
                default:
                    return (char) currentToken + "";
                }
            }
        };
    }

    /**
     * Removes comments from the stram of the form [comment].
     * 
     * @param feeder
     * @return
     */
    public static Generator commentEater(final Generator feeder) {
        return new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                String s = (String) feeder.gen();
                if (s != null) {
                    if (s.equals("[")) {
                        while (!(s = (String) feeder.gen())
                                .equals("]")) {
							if (s == null) {
								throw new IllegalArgumentException();
							}
						}
                        return this.gen();
                    }
                    return s;
                }
                return s;
            }
        };
    }

    Pattern illegalLabel = Pattern.compile("[\\(\\)\\:\\,\\;]+");
   
    String token;

    Generator tokenGen;

    public final Node tree;

    /**
     * Takes a token stream (see {@link NewickTreeParser#tokenise(Reader)}) and
     * parses it to create an {@link NewickTreeParser.Node}placed in the public
     * field {@link NewickTreeParser#tree}.
     * 
     * @param tokenGen
     */
    public NewickTreeParser(final Generator tokenGen) {
        this.tokenGen = tokenGen;
        this.token = (String) tokenGen.gen();
        this.tree = this.parseTree();
    }

    void nextToken() {
        this.token = (String) this.tokenGen.gen();
    }

    void parseBranchLength(final Node n) {
        if (this.token.equals(":")) {
            this.nextToken();
            n.edgeLength = Double.parseDouble(this.token);
            this.nextToken();
        }
    }

    Node parseDescendant_List() {
        final Node n = new Node();
        if (!this.token.equals("(")) {
			throw new IllegalArgumentException();
		}
        this.nextToken();
        while (true) {
            n.addNode(this.parseSubTree());
            if (!this.token.equals(",")) {
				break;
			}
            this.nextToken();
        }
        if (!this.token.equals(")")) {
			throw new IllegalArgumentException(this.token);
		}
        this.nextToken();
        return n;
    }

    void parseRootLabel(final Node n) {
        this.parseLabel(n);
    }

    void parseInternalNodeLabel(final Node n) {
        this.parseLabel(n);
    }

    void parseLabel(final Node n) {
        if (!this.illegalLabel.matcher(this.token).find()) {
            n.o = this.token;
            this.nextToken();
        } else {
			n.o = "";
		}
    }

    Node parseLeafLabel() {
        final Node n = new Node();
        this.parseLabel(n);
        return n;
    }

    Node parseSubTree() {
        if (this.token.equals("(")) {
            final Node n = this.parseDescendant_List();
            this.parseInternalNodeLabel(n);
            this.parseBranchLength(n);
            return n;
        }
        final Node n = this.parseLeafLabel();
        this.parseBranchLength(n);
        return n;
    }

    Node parseTree() {
        final Node n = this.parseDescendant_List();
        this.parseInternalNodeLabel(n);
        this.parseBranchLength(n);
        if (!this.token.equals(";")) {
			throw new IllegalArgumentException();
		}
        return n;
    }

}