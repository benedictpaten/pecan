/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Mar 19, 2005
 */
package bp.pecan;

import java.util.LinkedList;
import java.util.List;

import bp.common.ds.SkipList;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Generator;
import bp.common.fp.Generator_Int;
import bp.common.fp.Predicate_Double_2Args;

/**
 * This class progressively aligns together two sequences using the weights from
 * a supplied library through successive calls to the generator function.
 * Alignment is limited by the available input.
 * 
 * @author benedictpaten
 */
public class DripAligner implements Generator {

    static interface Add {
        public double fn(double d, int i);
    }

    /**
     * Provides function for generating a node where the x and y coordinates can
     * be reversed.
     * 
     * @author benedictpaten
     */
    static interface GenerateNode {
        Node gen(int i, int j, double score, Node pN);
    }

    /**
     * Provides a function for getting a chosen coordinate, such the either x or
     * y might be reported.
     * 
     * @author benedictpaten
     */
    static interface GetFocusCoordinate {
        int get(Node n);
    }

    static class Node {

        Node n;

        int x, y, z;

        double score;

        Node(final int x, final int y, final double score, final Node n) {
            this.x = x;
            this.y = y;
            this.score = score;
            this.n = n;
        }
    }

    static final GenerateNode generateNode_Column = new GenerateNode() {
        /*
         * (non-Javadoc)
         * 
         * @see bp.pecan.consistency.DripAligner.GenerateNode#gen(int, int, int,
         *      bp.pecan.consistency.DripAligner.Node)
         */
        public Node gen(int i, int j, double score, Node pN) {
            return new Node(j, i, score, pN);
        }
    };

    static final GenerateNode generateNode_Row = new GenerateNode() {
        /*
         * (non-Javadoc)
         * 
         * @see bp.pecan.consistency.DripAligner.GenerateNode#gen(int, int, int,
         *      bp.pecan.consistency.DripAligner.Node)
         */
        public Node gen(int i, int j, double score, Node pN) {
            return new Node(i, j, score, pN);
        }
    };

    static final GetFocusCoordinate getFocusCoordinate_Column = new GetFocusCoordinate() {
        /*
         * (non-Javadoc)
         * 
         * @see bp.pecan.consistency.DripAligner.GetFocusCoordinate#get(bp.pecan.consistency.DripAligner.Node)
         */
        public final int get(Node n) {
            return n.y;
        }
    };

    static final GetFocusCoordinate getFocusCoordinate_Row = new GetFocusCoordinate() {
        /*
         * (non-Javadoc)
         * 
         * @see bp.pecan.consistency.DripAligner.GetFocusCoordinate#get(bp.pecan.consistency.DripAligner.Node)
         */
        public final int get(Node n) {
            return n.x;
        }
    };

    static final int ROWBIT = 1, COLUMNBIT = 2, SILENTBIT = 4;

    /**
     * Procedure to update the column varible with the weights for the new
     * column.
     * 
     * @param line
     *            the active live containing nodes available for chaining
     * @param weights
     *            the list of weights to insert in the next alignment
     * @param lineIndex
     *            the index of the focus coordinate
     * @param generateNode
     *            a function to generate a node
     * @param getFocusCoordinate
     *            a function to retrieve the correct focus coordinate (either x
     *            or y)
     */
    static final void doLine(final SkipList line, final int[] weights, final int lineIndex,
            final int oppositeLineIndex, final GenerateNode generateNode,
            final GetFocusCoordinate getFocusCoordinate, final Add adder,
            final Predicate_Double_2Args greaterThan) {
        //find parent points
        final List<Node> newNodes = new LinkedList<Node>();
        for (int i = 0; i < weights.length; i += 2) {
            if (weights[i] >= oppositeLineIndex) {
                final Node n = (Node) line.searchLessThan(weights[i]);
                newNodes.add(generateNode.gen(weights[i], lineIndex,
                        adder.fn(n.score, weights[i + 1]), n));
            }
        }
        //insert the points, starting from the beginning, do not insert any
        // point
        //whose score is less than or equal to a point on the column which
        // is
        // ordered before
        //or equal to it
        for (int i = 0; i < newNodes.size(); i++) {
            final Node n = newNodes.get(i);
            Node m = (Node) line
                    .searchLessThan(getFocusCoordinate.get(n)+1);
            if (greaterThan.test(n.score, m.score)) {
                while (((m = (Node) line
                        .searchGreaterThanOrEqual(getFocusCoordinate.get(n)+1)) != null)
                        && !greaterThan.test(m.score, n.score)) {
					// l.score <= n.score)
                    line.delete(getFocusCoordinate.get(m));
				}
                line.insert(getFocusCoordinate.get(n), n);
            }
        }
    }

    /**
     * Detects if all the nodes on one line are within the bound of the opposite
     * line and vice versa. If so we can place a silent node at point
     * (rowNumber, columnNumber).
     * 
     * @param row
     * @param rowNumber
     *            the current completed row number
     * @param column
     * @param columnNumber
     *            the current completed column line number
     */
    static final void findAnchor(final SkipList row, final int rowNumber,
            final SkipList column, final int columnNumber) {
        //if all positions in line are less than or equal to opposite line
        // number and vice versa
        if ((row.searchGreaterThanOrEqual(columnNumber+1) == null)
                && (column.searchGreaterThanOrEqual(rowNumber+1) == null)) {
            // create a new node and place it at position (lineNumber,
            // oppositeLineNumber) and connect it to the
            // the previous best position (which will be the top point in either
            // line)
            Node n = (Node) row.searchLessThan(columnNumber+1);
            if ((n.x != columnNumber) || (n.y != rowNumber)) {
                final Node m = new Node(columnNumber, rowNumber, n.score, n);
                m.z = DripAligner.SILENTBIT;
                //delete the previous nodes as this is a certain anchor point
                while ((n = (Node) row
                        .searchLessThan(columnNumber + 1)) != null) {
					row.delete(n.x);
				}
                while ((n = (Node) column
                        .searchLessThan(rowNumber + 1)) != null) {
					column.delete(n.y);
				}
                //row.delete(n.x);
                //column.delete(n.y);
                //insert the new position into both lines
                row.insert(columnNumber, m);
                column.insert(rowNumber, m);
            }
        }
    }

    /**
     * Efficient function for detecting consensus path of nodes from a current
     * completed row and column. Returned node will be the next node in the
     * chain after the one previously returned. To allow this to work correctly
     * the lines must contain a dummy node at point -1, -1 to act as the
     * original previous node.
     * 
     * @param row
     *            current row
     * @param column
     *            current column
     * @return
     */
    static final Node getAlignment(final SkipList row, final SkipList column) {
        //trace back from farthest (rightmost and lowest points) in column
        // and row
        // the point node list. points should be labelled
        //as this is done and stopped when already labelled encountered.
        // Labels
        // created from the point on the column should be labelled
        //with column tags and vice versa for rows. For node with greatest
        // x, y
        // coordinate
        //labelled with both row and column report as aligned
        Node n = (Node) row.searchLessThan(Integer.MAX_VALUE);
        n = DripAligner.traceBackAndLabel(n, DripAligner.ROWBIT, DripAligner.COLUMNBIT);
        if (n != null) {
            n = DripAligner.switchBackPointers(n);
            Node m = (Node) column.searchLessThan(Integer.MAX_VALUE);
            m = DripAligner.traceBackAndLabel(m, DripAligner.COLUMNBIT, DripAligner.ROWBIT);
            if (m != null) {
				DripAligner.switchBackPointers(m);
			}
            return n.n;
        }
        n = (Node) column.searchLessThan(Integer.MAX_VALUE);
        n = DripAligner.traceBackAndLabel(n, DripAligner.COLUMNBIT, DripAligner.ROWBIT);
        return n != null ? DripAligner.switchBackPointers(n).n : null;
    }

    /**
     * Encapsulation for
     * {@link DripAligner#doLine(SkipList, SkipList, int, int[], GenerateNode, GetFocusCoordinate)}.
     * At a call to the generator causes successive lines to be computed until
     * the weight generator returns null.
     * 
     * @param column
     * @param row
     * @param weightGenerator
     * @param nodeGen
     * @param getFocusCoordinate
     * @return the next line number (0 >)
     */
    static final Generator_Int inputLineProcedure(final SkipList line,
            final MutableInteger lineIndex,
            final SkipList oppositeLine,
            final MutableInteger oppositeLineIndex,
            final Generator weightGenerator,
            final GenerateNode nodeGen,
            final GetFocusCoordinate getFocusCoordinate,
            final Add adder, final Predicate_Double_2Args greaterThan) {
        return new Generator_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public int gen() {
                int[] weights;
                while ((weights = (int[]) weightGenerator.gen()) != null) {
                    DripAligner.doLine(line, weights, lineIndex.i,
                            oppositeLineIndex.i, nodeGen,
                            getFocusCoordinate, adder, greaterThan);
                    DripAligner.syncWithOtherLine(line, oppositeLine,
                            lineIndex.i++, getFocusCoordinate,
                            greaterThan);
                }
                return lineIndex.i;
            }
        };
    }

    /**
     * Switches the direction of pointers in a node chain.
     * 
     * @param n
     *            node from which to start switching back (head node)
     * @return first node on the chain (new head node)
     */
    static final Node switchBackPointers(Node n) {
        Node forward = null;
        do {
            final Node previous = n.n;
            n.n = forward;
            forward = n;
            n = previous;
        } while (n != null);
        return forward;
    }

    /**
     * Function to ensure the region contained within the intersection of the
     * two scanlines contain only shared points. This involves ensuring any
     * points in the scanline less than a point on the oppositeline are removed
     * and that the point on the opposite line is inserted onto the line.
     * 
     * @param line
     *            scan line
     * @param oppositeLine
     *            other scan line
     * @param lineIndex
     *            focus coordinate of the scan line
     * @param getFocusCoordinate
     *            retrieves the appropriate coordinate focus from a node (either
     *            x or y)
     */
    static final void syncWithOtherLine(final SkipList line,
            final SkipList oppositeLine, final int lineIndex,
            final GetFocusCoordinate getFocusCoordinate,
            final Predicate_Double_2Args greaterThan) {
        //find first insert any point from the opposite column that lies
        // at
        // intersection, put at front of new points list
        final Node n = (Node) oppositeLine.search(lineIndex);
        if (n != null) {
            //delete any points which preceed n in the chain, as they can not
            // be in future linked
            //on this line
            Node m;
            while ((m = (Node) line.searchLessThan(getFocusCoordinate
                    .get(n))) != null) {
                line.delete(getFocusCoordinate.get(m));
            }
            //insert point from other line
            while (((m = (Node) line
                    .searchGreaterThanOrEqual(getFocusCoordinate.get(n)+1)) != null)
                    && !greaterThan.test(m.score, n.score)) {
				//l.score <= n.score)
                line.delete(getFocusCoordinate.get(m));
			}
            line.insert(getFocusCoordinate.get(n), n);
        }
    }

    /**
     * Labels a chain of nodes with a given label, stopping when it encounters a
     * node that is already labelled or a null pointer. If the the first node it
     * encounters is labelled with the 'otherlabel' then it is returned, else in
     * all other case null is returned.
     * 
     * @param n
     *            head node
     * @param label
     *            to set {@link Node#z}
     * @param otherLabel
     * @return node whose {@link Node#z}== otherLabel
     */
    static final Node traceBackAndLabel(Node n, final int label, final int otherLabel) {
        while (n != null) {
            if ((n.z & label) != 0) {
				break;
			}
            if ((n.z & otherLabel) != 0) {
				return n;
			}
            n.z |= label;
            n = n.n;
        }
        return null;
    }

    final SkipList row, column;

    Generator_Int rowLineProcedure, columnLineProcedure;

    int rowOffset = 0, columnOffset = 0;
    
    double pScore;

    /**
     * 
     * @param inputYWeightGenerator
     *            function containing the rows weights
     * @param inputXWeightGenerator
     *            function containing the column weights
     * @param adder
     *            adding together weights
     * @param greaterThan
     *            is first argument greaterthan second
     * @param startingValue
     *            score of first node
     */
    public DripAligner(final Generator inputXWeightGenerator,
            final Generator inputYWeightGenerator, final Add adder,
            final Predicate_Double_2Args greaterThan, final double startingValue) {
        this.row = new SkipList();
        this.column = new SkipList();
        //start with golden first node, so node lines will never be empty
        final Node n = new Node(-1, -1, startingValue, null);
        this.row.insert(-1, n);
        this.column.insert(-1, n);
        final MutableInteger rowLineIndex = new MutableInteger(), columnLineIndex = new MutableInteger();
        this.rowLineProcedure = DripAligner.inputLineProcedure(this.row, rowLineIndex,
                this.column, columnLineIndex, inputYWeightGenerator,
                DripAligner.generateNode_Row, DripAligner.getFocusCoordinate_Row, adder,
                greaterThan);
        this.columnLineProcedure = DripAligner.inputLineProcedure(this.column,
                columnLineIndex, this.row, rowLineIndex,
                inputXWeightGenerator, DripAligner.generateNode_Column,
                DripAligner.getFocusCoordinate_Column, adder, greaterThan);
        this.pScore = startingValue;
    }

    final Node align() {
        final int rowlineIndex = this.rowLineProcedure.gen();
        final int columnLineIndex = this.columnLineProcedure.gen();
        DripAligner.findAnchor(this.row, rowlineIndex - 1, this.column, columnLineIndex - 1);
        return DripAligner.getAlignment(this.row, this.column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.fp.Generator#gen()
     */
    public final Object gen() {
        final Node n = this.align();
        return n != null ? this.translateNodes(n) : null;
    }
    
    /**
     * Converts the given node path into a generator suitable for the creation
     * of an alignment.
     * 
     * @param n
     *            alignment
     * @return generator for creating an alignment
     */
    final Generator translateNodes(final Node n) {
        return new Generator() {

            LinkedList<Float> l = new LinkedList<Float>();

            Node m = n;
            
           // final boolean[] match = { true, true }, rowGap = { false,
           //         true }, columnGap = { true, false };
            
            final Float rowGap = new Float(Float.NEGATIVE_INFINITY);
            final Float columnGap = new Float(Float.POSITIVE_INFINITY);

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                if (this.l.size() != 0) {
                    return this.l.removeFirst();
                }
                if (this.m == null) {
                    return null;
                }
                if ((this.m.z & DripAligner.SILENTBIT) == 0) { //non silent match
                    while (DripAligner.this.rowOffset++ < this.m.x) {
                        this.l.add(this.columnGap);
                    }
                    while (DripAligner.this.columnOffset++ < this.m.y) {
                        this.l.add(this.rowGap);
                    }
                    final double d = this.m.score - DripAligner.this.pScore;
                    this.l.add(new Float(d));
                    DripAligner.this.pScore = this.m.score;
                } //silent match
                while (DripAligner.this.rowOffset <= this.m.x) {
                    DripAligner.this.rowOffset++;
                    this.l.add(this.columnGap);
                }
                while (DripAligner.this.columnOffset <= this.m.y) {
                    DripAligner.this.columnOffset++;
                    this.l.add(this.rowGap);
                }
                this.m = this.m.n;
                return this.gen();
            }
        };
    }
}