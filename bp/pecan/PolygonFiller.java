/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 22, 2005
 */
package bp.pecan;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import bp.common.ds.Array;
import bp.common.fp.Function;
import bp.common.fp.Function_Index;
import bp.common.fp.Function_Index_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.Generators;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate;
import bp.common.fp.Procedure_Int_2Args;
import bp.common.io.Debug;

public final class PolygonFiller {

    public static class Node implements Cloneable, Comparable {
        public int x;

        public int y;

        public int yMax;

        public int z;

        public Node(final int x, final int y, final int yMax, final int z) {
            this.x = x;
            this.y = y;
            this.yMax = yMax;
            this.z = z;
        }

        public int compareTo(final Object arg0) {
            final Node m = (Node) arg0;
            return this.y < m.y ? -1 : this.y > m.y ? 1 : this.x < m.x ? -1
                    : this.x > m.x ? 1 : 0;
        }

        @Override
		public Object clone() {
            try {
                return super.clone();
            } catch (final CloneNotSupportedException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        }

        @Override
		public String toString() {
            return " ( " + this.x + " " + this.y + " " + this.yMax + " " + this.z + " ) ";
        }

    }

    static final Logger logger = Logger.getLogger(Pecan.class
            .getName());

    public static final void scanPolygon(final Iterator itL, final Iterator itR,
            final Procedure_Int_2Args spanX) {
        if (itL.hasNext()) {
            Node m = (Node) itL.next();
            int y = m.y;
            int xL = m.x;
            Node n = (Node) itR.next();
            int xR = n.x;
            do {
                spanX.pro(xL, xR);
                xL += m.z;
                xR += n.z;
                if (y == m.yMax) {
                    if (itL.hasNext()) {
                        m = (Node) itL.next();
                        xL = m.x;
                    } else {
						break;
					}
                }
                if (y == n.yMax) {
                    n = (Node) itR.next();
                    xR = n.x;
                }
                y++;
            } while (true);
        }
    }

    /**
     * Reverses (mirrors) an edge tables coordinates in both x and y axes.
     * Consequently iteration through the polygon is exactly the reverse of
     * iteration in the forward direction. Coordinates however, will be reversed
     * also so must be further flipped to reflect their original values.
     * 
     * @param eT
     *            edge table to reverse
     * @param x0
     *            the size of a row (zero x coordinate will end up as equal to
     *            x0 and vice versa)
     * @param y0
     *            the size of a column (zero y coordinate will end up as equal
     *            to (y0) and vice versa)
     */
    static List reverseCoordinates(final List eT, final int x0, final int y0) {
        final List l = new LinkedList();
        for (final Iterator it = eT.iterator(); it.hasNext();) {
            final Node m = (Node) it.next();
            int x = m.x;
            int y = m.y;
            int yMax = m.yMax;
            final int z = m.z;
            if (z != 0) {
                if (z == 1) {
					x = x0 - (x + yMax - y);
				} else {
                    if (Debug.DEBUGCODE && (z != -1)) {
						throw new IllegalStateException();
					}
                    x = x0 - (x - yMax + y);
                }
            } else {
				x = x0 - x;
			}
            final int i = yMax;
            yMax = y0 - y;
            y = y0 - i;
            l.add(0, new Node(x, y, yMax, z));
        }
        return l;
    }

    public static void transformEdgeList(final List l, final int x, final int y) {
        for (final Iterator it = l.iterator(); it.hasNext();) {
            final Node m = (Node) it.next();
            m.y += y;
            m.yMax += y;
            m.x += x;
        }
    }

    /**
     * Iterator adaptor for
     * {@link PolygonFiller#transformEdges(Generator, int, int)}.
     * 
     * @param it
     * @param xTransfrom
     * @param yTransform
     * @return
     */
    public static Iterator transformEdges(final Iterator it,
            final int xTransform, final int yTransform) {
        return new GeneratorIterator(PolygonFiller.transformEdges(Generators
                .iteratorGenerator(it), xTransform, yTransform));
    }

    /**
     * Takes an generator that produces edges and transforms those edges by a
     * constant transformation in the x and y axes.
     * 
     * @param gen
     * @param xTransfrom
     * @param yTransform
     * @return
     */
    public static Generator transformEdges(final Generator gen,
            final int xTransform, final int yTransform) {
        return new Generator() {

            public Object gen() {
                final Node m = (Node) gen.gen();
                if (m != null) {
                    PolygonFiller.transformEdge(xTransform, yTransform, m);
                    return m;
                }
                return null;
            }
        };
    }

    public static Function transformEdge(final int xTransform,
            final int yTransform) {
        return new Function() {
            public Object fn(final Object o) {
                PolygonFiller.transformEdge(xTransform, yTransform, (Node) o);
                return o;
            }
        };
    }

    public static final void transformEdge(final int xTransform,
            final int yTransform, final Node m) {
        m.y += yTransform;
        m.yMax += yTransform;
        m.x += xTransform;
    }

    /**
     * Take two edge lists and merges them together. If two edges have equal x
     * coordinates then they will appear in the merged edge list in stable order
     * from the original lists, with edges from the first iterator proceding
     * those from the second.
     * 
     * @param it
     *            first edge list
     * @param it2
     *            second edge list
     * @return merged edge list
     */
    public static Iterator combineEdgeLists(final Iterator it,
            final Iterator it2) {
        return Iterators.merge(it, it2, new Comparator() {
            public int compare(final Object arg0, final Object arg1) {
                return ((Comparable<Object>) arg0).compareTo(arg1);
            }
        });
    }

    /**
     * Takes an iterator to an edge list where no edge overlaps any other and is
     * sorted in increasing order in both dimensions and flips the x and y
     * coordinate. This operations destroys the previous edge list.
     * 
     * @param edgeList
     */
    public static Generator flipEdgeXYDiagonalsCoordinates(
            final Generator gen) {
        return new Generator() {

            public Object gen() {
                final Node m = (Node) gen.gen();
                if (m != null) {
                    final int y = m.y;
                    m.yMax = m.x + m.yMax - y;
                    m.y = m.x;
                    m.x = y;
                    return m;
                }
                return null;
            }
        };
    }

    /**
     * Takes single enclosed polygon and breaks into chunks along given diagonal
     * coordinates. The polygon returned will lie between 1 + the diagonal of
     * the last call and the the current diagonal (inclusive). The boundary
     * coordinates run clock wise from left and represent the four points on the
     * vertices of the clipped diagonal. { XTOPLEFT, YTOPLEFT, XTOPRIGHT,
     * YTOPRIGHT, XBOTTOMRIGHT, YBOTTOMRIGHT, XBOTTOMLEFT, YBOTTOMLEFT }.
     * Calling the function with a diagonal which precedes the coordinates of
     * the next clippable diagonal will cause an {@link IllegalStateException}
     * to be thrown. Calling the function with a diagonal which is greater than
     * the greatest x+y diagonal of the polygon will cause an
     * {@link IllegalStateException}to be thrown.
     * 
     * @param it
     * @return { edgeList, int[] containing boundary coordinates }
     * @param xMax
     *            maximum x coordinate (exclusive)
     * @param yMax
     *            maximum y coordinate (exclusive)
     */
    public static Function_Index clipPolygons(final Generator left,
            final Generator right, final int xMax, final int yMax) {
        return new Function_Index() {

            Node leftE = (Node) left.gen();

            Node rightE = (Node) right.gen();

            List<Node> lET = new LinkedList<Node>(), rET = new LinkedList<Node>();

            int[] boundaries = new int[8];

            {
                if (this.leftE == null) {
					this.leftE = new Node(xMax, yMax, yMax, 1);
				}
                if (this.rightE == null) {
					this.rightE = new Node(xMax, yMax, yMax, 1);
				}
                this.fillInValues(0, this.boundaries, this.leftE.x, this.leftE.y,
                        this.leftE.x, this.leftE.y);
            }

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Index#polygonClipper(int)
             */
            public final Object fn(final int diagonal) {
                if ((Debug.DEBUGCODE
                        && (this.boundaries[0] + this.boundaries[1] > diagonal))
                        || (diagonal > (yMax + xMax - 2))) {
					throw new IllegalStateException(this.boundaries[0]
                            + this.boundaries[1] + " " + diagonal + " "
                            + (yMax + xMax - 2));
				}
                final int[] cBoundaries = this.boundaries;
                this.boundaries = new int[8];
                this.leftE = this.getEdgesLessThanDiagonal(this.leftE, left,
                        diagonal + 1, this.lET);
                this.rightE = this.getEdgesLessThanDiagonal(this.rightE, right,
                        diagonal, this.rET);
                if (this.rightE.y != this.leftE.y) {
                    final Node line = this.createDiagonalLine(diagonal);
                    {
                        this.fillInValues(4, cBoundaries, line.x, line.y,
                                line.x - (line.yMax - line.y),
                                line.yMax);
                    }
                    {
                        if (this.rightE.y + this.rightE.x == diagonal) {
							this.rightE = this.shiftLineUpOne(this.rightE, right);
						}
                    }
                    this.rET.add(line);
                } else {
                    final Node line = this.rET.get(this.rET.size() - 1);
                    final int j = line.x + line.z * (line.yMax - line.y);
                    this.fillInValues(4, cBoundaries, j, line.yMax, j,
                            line.yMax);
                }
                final List<Node> lETR = this.lET;
                final List<Node> rETR = this.rET;
                this.lET = new LinkedList<Node>();
                this.rET = new LinkedList<Node>();
                if (this.rightE.y != this.leftE.y) {
                    final Node line = this.createDiagonalLine(diagonal + 1);
                    this.lET.add(line);
                    {
                        final int j = line.yMax
                                + (this.leftE.y + this.leftE.x == diagonal + 1 ? 1
                                        : 0);
                        this.fillInValues(0, this.boundaries, line.x
                                - (j - line.y), j, line.x, line.y);
                    }
                } else {
                    this.fillInValues(0, this.boundaries, this.leftE.x, this.leftE.y,
                            this.leftE.x, this.leftE.y);
                }
                return new Object[] { lETR, rETR, cBoundaries };
            }

            void fillInValues(final int offset, final int[] array, final int i, final int j,
                    final int k, final int l) {
                array[offset] = i;
                array[offset + 1] = j;
                array[offset + 2] = k;
                array[offset + 3] = l;
            }

            Node createDiagonalLine(final int diagonal) {
                return new Node(diagonal - this.rightE.y, this.rightE.y,
                        this.leftE.y - 1, -1);
            }

            Node shiftLineUpOne(Node line, final Generator it) {
                if (line.y == line.yMax) {
                    line = (Node) it.gen();
                    if (line == null) {
						line = new Node(xMax, yMax, yMax, 1);
					}
                } else {
                    line.y++;
                    line.x += line.z;
                }
                return line;
            }

            Node getEdgesLessThanDiagonal(Node currentEdge,
                    final Generator edgeIterator, final int diagonal,
                    final List<Node> precedingEdges) {
                while (true) {
                    final int maxDiag = currentEdge.x + currentEdge.z
                            * (currentEdge.yMax - currentEdge.y)
                            + currentEdge.yMax;
                    if (maxDiag < diagonal) {
                        precedingEdges.add(currentEdge);
                        currentEdge = (Node) edgeIterator.gen();
                        if (currentEdge == null) {
							currentEdge = new Node(xMax, yMax, yMax,
                                    1);
						}
                    } else {
                        final int diff = diagonal - currentEdge.y
                                - currentEdge.x;
                        if (diff > 0) {
                            precedingEdges.add(currentEdge);
                            if (currentEdge.z == 1) {
                                final int yMax = currentEdge.yMax;
                                currentEdge.yMax = currentEdge.y
                                        + (diff - 1) / 2;
                                currentEdge = new Node(currentEdge.x
                                        + (diff + 1) / 2,
                                        currentEdge.y + (diff + 1)
                                                / 2, yMax, 1);
                            } else {
                                final int yMax = currentEdge.yMax;
                                currentEdge.yMax = currentEdge.y
                                        + diff - 1;
                                currentEdge = new Node(currentEdge.x,
                                        currentEdge.y + diff, yMax, 0);
                            }
                        }
                        return currentEdge;
                    }
                }
            }
        };
    }

    /**
     * Adds the leftmost edges to a list of diagonal for creating an enclosed
     * polygon. Is symmetric with
     * {@link PolygonFiller#addVerticalEdgesRight(Iterator, int)}.
     * 
     * @param edgeIt
     *            non overlapping diagonals edge list
     * @param xMin
     *            min xCoordinate (inclusive)
     * @param yMin
     *            min yCoordinate (inclusive)
     * @param yMax
     *            max yCoordinate (exclusive)
     * @return
     */
    public static Generator addVerticalEdgesLeft(
            final Generator edgeIt, final int xMin, final int yMin,
            final int yMax) {
        return new Generator() {
            // List cL;
            Node cL;

            int x = xMin, y = yMin, yS;
            {
                if ((this.cL = (Node) edgeIt.gen()) != null) {
                    this.yS = this.cL.y;
                } else {
					this.yS = yMax;
				}
            }

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (this.y < this.yS) {
                    // return a horizontal edge
                    final Node l = new Node(this.x, this.y, this.yS - 1, 0);
                    this.y = Integer.MAX_VALUE;
                    return l;
                }
                if (this.cL != null) {
                    this.y = this.cL.yMax + 1;
                    this.x = this.cL.x + this.cL.yMax - this.cL.y;
                    // set the current edge to be returned
                    final Node rL = this.cL;
                    if ((this.cL = (Node) edgeIt.gen()) != null) {
                        this.yS = this.cL.y;
                    } else {
                        this.yS = yMax;
                    }
                    return rL;
                }
                return null;
            }
        };
    }

    /**
     * Adds the rightmost edges to a list of diagonal for creating an enclosed
     * polygon. Is symmetric with
     * {@link PolygonFiller#addVerticalEdgesLeft(Iterator, int)}.
     * 
     * @param edgeIt
     *            non overlapping diagonals edge list
     * @param xMax
     *            max xCoordinate (exclusive)
     * @param yMin
     *            min yCoordinate (inclusive)
     * @param yMax
     *            max yCoordinate (exclusive)
     * 
     * @return
     */
    public static Generator addVerticalEdgesRight(
            final Generator edgeIt, final int xMax, final int yMin,
            final int yMax) {
        return new Generator() {

            Node cL;

            int y = yMin, yS;
            {
                if ((this.cL = (Node) edgeIt.gen()) != null) {
                    this.yS = this.cL.y;
                } else {
                    this.yS = yMin;
                }
            }

            public Object gen() {
                if (this.y < this.yS) {
                    final Node l = new Node(this.cL.x - 1, this.y, this.yS - 1, 0);
                    this.y = this.cL.yMax;
                    return l;
                }
                Object o;
                if ((o = edgeIt.gen()) != null) {
                    final Node l = this.cL;
                    this.cL = (Node) o;
                    if (this.yS == l.yMax) { // is of
                        this.yS = this.cL.y;
                        return this.gen(); // length 1
                    }
                    this.yS = this.cL.y;
                    this.y = l.yMax;
                    l.yMax -= 1;
                    return l;
                }
                if ((this.cL != null) && (this.yS != this.cL.yMax)) {
                    this.y = this.cL.yMax;
                    this.cL.yMax--;
                    final Node l = this.cL;
                    this.cL = null;
                    return l;
                }
                if (this.y < yMax) {
                    final Node l = new Node(xMax - 1, this.y, yMax - 1, 0);
                    this.y = Integer.MAX_VALUE;
                    return l;
                }
                return null;
            }
        };
    }

    /**
     * Turns an edge list into a polygon complete alignment polygon. Returns a
     * function from
     * {@link PolygonFiller#clipPolygons(Iterator, Iterator, int, int)}.
     * Including clipping the boundaries of the polygon, defining the perimeters
     * and ensuring the polygon is runs from xmin-1, ymin-1 to xmax, ymax.
     * 
     * @param eT
     *            the edge table
     * @param rET
     *            the reverse edge table
     * @param xMin
     *            the minimum x coordinate (inclusive)
     * @param xMax
     *            the maxmimum x coordinate (exclusive)
     * @param yMin
     *            the minimum y coordinate (inclusive)
     * @param yMax
     *            the maximum y coordinate (exclusive)
     * @return
     */
    public static Function_Index polygonIterator(final Generator eT,
            final Generator rET, final int xMin, final int xMax,
            final int yMin, final int yMax) {
        return PolygonFiller.clipPolygons(PolygonFiller.addVerticalEdgesLeft(
                PolygonFiller.flipEdgeXYDiagonalsCoordinates(rET), xMin - 1,
                yMin - 1, yMax), PolygonFiller.addVerticalEdgesRight(eT, xMax,
                yMin - 1, yMax), xMax, yMax);
    }

    public static Generator cutPointGenerator(final Generator gen,
            final int minLength, final int minLengthDivision) {
        return new Generator() {
            int i;

            int j;

            int k;

            int m;

            int n = Integer.MAX_VALUE;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                if (this.n == Integer.MAX_VALUE) {
                    final Node l = (Node) gen.gen();
                    if (l != null) {
                        this.j = l.y - 1;
                        this.k = l.yMax - this.j;
                        if (this.k >= minLength) {
                            this.i = l.x - 1;
                            this.n = 2 + this.k / minLengthDivision;
                            this.m = this.k / this.n;
                            if (this.m == 0) {
								this.m = 1; // prevent loop
							}
                            this.k = this.m * (this.n - 1);
                            this.n = this.m;
                            return this.gen();
                        }
                        return this.gen();
                    }
                    return null;
                }
                if (this.n <= this.k) {
                    // return division
                    final int[] iA = new int[] { this.i + this.n, this.j + this.n };
                    this.n += this.m;
                    return iA;
                }
                this.n = Integer.MAX_VALUE;
                return this.gen();
            }
        };
    }

    /**
     * Creates a generator which produces non-equivalent regions from a stream
     * of edges. See
     * {@link PolygonFiller#nERPoints(Iterator, int, int, int, int, int)}for
     * more. Gaps are inserted if the distance between two points is greater
     * than maximumDiagonals apart in diagonal (x+y) coordinates. NER gaps are
     * in the format { x, y, x, y }, coordinates are inclusive for the diagonal
     * points surrounding the gap.
     * 
     * @param it
     * @param maximumDiagonals
     *            number of x+y diagonals between diagonals (exclusive) to
     *            generate NER
     * @param overhangingDiagonals
     * 
     * @return
     */
    public static Generator nERPoints(final Generator it,
            final int maximumDiagonals, final int startX,
            final int startY, final int endX, final int endY) {
        return new Generator() {
            int pD = startX + startY;

            int pX = startX;

            public Object gen() {
                Node l;
                while ((l = (Node) it.gen()) != null) {
                    if (l.x + l.y > this.pD + maximumDiagonals) {
                        final int[] block = new int[] { this.pX, this.pD - this.pX, l.x,
                                l.y };
                        this.pD = 2 * l.yMax + l.x - l.y;
                        this.pX = l.x - l.y + l.yMax;
                        return block;
                    }
                    this.pD = 2 * l.yMax + l.x - l.y;
                    this.pX = l.x - l.y + l.yMax;
                }
                if (endX + endY > this.pD + maximumDiagonals) {
                    final int[] block = new int[] { this.pX, this.pD - this.pX, endX, endY };
                    this.pD = endX + endY;
                    this.pX = endX;
                    return block;
                }
                return null;
            }
        };
    }

    public static final Predicate filterEdgeListByLessThanConstraints(
            final Generator lessThans, final int border) {
        return new Predicate() {
            int lTX;

            int lTY;
            {
                this.getLT();
            }

            public final boolean test(final Object o) {
                final PolygonFiller.Node n = (PolygonFiller.Node) o;
                if (n.y >= this.lTY) {
                    while (n.x > this.lTX) {
						this.getLT();
					}
                    return n.y < this.lTY;
                }
                return true;
            }

            final void getLT() {
                final int[] iA = (int[]) lessThans.gen();
                if (iA != null) {
                    this.lTX = iA[1] + border;
                    this.lTY = iA[0] - border;
                } else {
                    this.lTX = Integer.MAX_VALUE;
                    this.lTY = Integer.MAX_VALUE;
                }
            }
        };
    }

    public static final Predicate filterEdgeListByLessThanOrEqualConstraints(
            final Generator lessThans, final int border) {
        return new Predicate() {
            int lTX;

            int lTXMax = Integer.MIN_VALUE;

            int lTY = Integer.MIN_VALUE;

            public final boolean test(final Object o) {
                final PolygonFiller.Node n = (PolygonFiller.Node) o;
                if (n.y >= this.lTY) {
                    this.getLT(n.x);
                    return n.y < this.lTY;
                }
                return true;
            }

            final void getLT(final int i) {
                if (i <= this.lTXMax) {
                    if (i > this.lTX) {
                        this.lTY += i - this.lTX;
                        this.lTX = i;
                    }
                    return;
                }
                while (true) {
                    final PolygonFiller.Node m = (PolygonFiller.Node) lessThans
                            .gen();
                    if (m != null) {
                        this.lTXMax = m.yMax + border;
                        if (i <= this.lTXMax) {
                            this.lTX = m.y + border;
                            this.lTY = m.x - border;
                            if (i > this.lTX) {
                                this.lTY += i - this.lTX;
                                this.lTX = i;
                            }
                            return;
                        }
                    } else {
                        this.lTY = Integer.MAX_VALUE;
                        this.lTX = Integer.MAX_VALUE;
                        this.lTXMax = Integer.MAX_VALUE;
                        return;
                    }
                }
            }
        };
    }

    public static final Generator mergeLessThansEdgeList(
            final Generator edgeList, final Generator lessThans) {
        return new Generator() {
            Node m = null;

            Node n = (Node) lessThans.gen();

            int pY = Integer.MIN_VALUE;

            public final Object gen() {
                if (this.m == null) {
                    this.m = (Node) edgeList.gen();
                    if (this.m == null) {
                        return this.n == null ? null : this.returnN();
                    }
                }
                if (this.n == null) {
                    return this.returnM();
                }
                while (true) {
                    if (this.m.x < this.n.x) {
                        if (this.m.y > this.pY) {
                            this.pY = this.m.y;
                            final Object o = this.m;
                            this.m = null;
                            return o;
                        }
                        this.m = (Node) edgeList.gen();
                        if (this.m == null) {
							return this.returnN();
						}
                        continue;
                    }
                    if (this.m.x == this.n.x) {
                        if (this.m.y > this.n.y) {
                            this.n = (Node) lessThans.gen();
                            this.pY = this.m.y;
                            final Object o = this.m;
                            this.m = null;
                            return o;
                        }
                        this.m = (Node) edgeList.gen();
                        if (this.m == null) {
							return this.returnN();
						}
                        continue;
                    }
                    if (this.n.y > this.pY) {
                        this.pY = this.n.y;
                        final Object o = this.n;
                        this.n = (Node) lessThans.gen();
                        return o;
                    }
                    this.n = (Node) lessThans.gen();
                    if (this.n == null) {
                        return this.returnM();
                    }
                }
            }

            final Node returnM() {
                while (true) {
                    if (this.m.y > this.pY) {
                        final Node o = this.m;
                        this.m = null;
                        return o;
                    }
                    this.m = (Node) edgeList.gen();
                    if (this.m == null) {
						return null;
					}
                }
            }

            final Node returnN() {
                while (true) {
                    if (this.n.y > this.pY) {
                        this.pY = this.n.y;
                        final Node o = this.n;
                        this.n = (Node) lessThans.gen();
                        return o;
                    }
                    this.n = (Node) lessThans.gen();
                    if (this.n == null) {
						return null;
					}
                }
            }
        };
    }

    public static final Generator mergeEdgeListWithTransitiveAnchors(
            final Generator gen,
            final Function_Index_2Args blockingGen,
            final Predicate bP, final Predicate bP2) {
        return new Generator() {
            int pX = Integer.MIN_VALUE;

            int pY = Integer.MIN_VALUE;

            int x;

            int y;

            Node m;

            Node n;
            {
                this.getM();
            }

            public final Object gen() {
                if (this.n == null) {
                    this.n = (Node) blockingGen.fn(this.pY + 1, this.y);
                    if (this.n == null) {
						return this.returnM();
					}
                }
                // if (n.y < y) {
                while (/* n.y <= pY || */(this.n.x <= this.pX) || (this.n.x >= this.x)
                        || !bP.test(this.n) || !bP2.test(this.n)) {
                    this.n = (Node) blockingGen.fn(this.pY + 1, this.y);
                    if ((this.n == null) || (this.n.y >= this.y)) {
						return this.returnM();
					}
                }
                final Object rV = this.n;
                this.n = (Node) blockingGen.fn(this.pY + 1, this.y);
                return rV;
                // }
                // return returnM();
            };

            final Object returnM() {
                if (this.m == null) {
					return null;
				}
                final Object rV = this.m;
                this.pX = this.m.x + this.m.yMax - this.m.y;
                this.pY = this.m.yMax;
                this.getM();
                return rV;
            }

            final void getM() {
                this.m = (Node) gen.gen();
                if (this.m != null) {
                    this.x = this.m.x;
                    this.y = this.m.y;
                } else {
                    this.x = Integer.MAX_VALUE;
                    this.y = Integer.MAX_VALUE;
                }
            }
        };
    }

    /**
     * Deep copies an edge table
     * 
     * @param eT
     *            edge table to deep copy
     * @return
     */
    public static List cloneEdgeList(final List eT) {
        final List eT2 = new LinkedList();
        for (final Iterator it = eT.iterator(); it.hasNext();) {
            final Node m = (Node) it.next();
            eT2.add(m.clone());
        }
        return eT2;
    }
    
    public static Function cloneEdge() {
        return new Function() {
            public Object fn(final Object o) {
                return ((Node)o).clone();
            }
        };
    }

    public static Predicate isLessThan() {
        return new Predicate() {
            public boolean test(final Object o) {
                return ((Node) o).z == 0;
            }
        };
    }

    public static Predicate isLessThanOrEqual() {
        return new Predicate() {
            public boolean test(final Object o) {
                return ((Node) o).z != 0;
            }
        };
    }

    public static Function_Index polygonIteratorWithLessThans(
            final Function_Index fn, final Generator lTRows,
            final Generator lTColumns, final int[] scratch) {
        return new Function_Index() {
            int[] iA1 = null;

            int[] iA2 = null;

            int upto;

            public Object fn(final int i) {
                final Object[] oA = (Object[]) fn.fn(i);
                this.iA1 = this.lessThanPoints(lTRows, this.iA1, scratch, i);
                int j = this.upto;
                final int[] iA = new int[j];
                System.arraycopy(scratch, 0, iA, 0, j);
                this.iA2 = this.lessThanPoints(lTColumns, this.iA2, scratch, i);
                j = this.upto;
                // flip the x-y points
                Array.mingle(scratch, j - 2);
                final int[] iA2 = new int[j];
                System.arraycopy(scratch, 0, iA2, 0, j);
                final Object[] oA2 = new Object[oA.length + 2];
                System.arraycopy(oA, 0, oA2, 0, oA.length);
                oA2[oA2.length - 1] = iA;
                oA2[oA2.length - 2] = iA2;
                return oA2;
            }

            final int[] lessThanPoints(final Generator lT, int[] iA,
                    final int[] scratch, final int diag) {
                int i = 0;
                if (iA == null) {
                    iA = (int[]) lT.gen();
                }
                while (iA != null) {
                    final int j = iA[0];
                    final int k = iA[1];
                    final int l = j + k;
                    if (l <= diag + 2) {
                        scratch[i++] = j;
                        scratch[i++] = k;
                        if (l <= diag) {
                            iA = (int[]) lT.gen();
                        } else {
							break;
						}
                    } else {
						break;
					}
                }
                scratch[i++] = Integer.MAX_VALUE;
                scratch[i++] = Integer.MAX_VALUE;
                this.upto = i;
                return iA;
            }
        };
    }

    public static void reverseLessThanCoordinates(final int[] lTA,
            final int[] iA, final int x0, final int y0) {
        int i = 0;
        while (lTA[i] != Integer.MAX_VALUE) {
			i += 2;
		}
        int k = i - 1;
        for (int j = 0; j < i; j += 2) {
            iA[k - 1] = x0 - lTA[j] + 1;
            iA[k] = y0 - lTA[j + 1] + 1;
            k -= 2;
        }
        iA[i] = Integer.MAX_VALUE;
        iA[i + 1] = Integer.MAX_VALUE;
    }

    public static void transformCoordinates(final int[] lTA, final int[] iA,
            final int x0, final int y0) {
        int i = 0;
        while (lTA[i] != Integer.MAX_VALUE) {
            iA[i] = lTA[i] + x0;
            iA[i + 1] = lTA[i + 1] + y0;
            i += 2;
        }
        iA[i] = Integer.MAX_VALUE;
        iA[i + 1] = Integer.MAX_VALUE;
    }

    /**
     * Clips boundaries of polygons within the bounds.
     * 
     * @param eT
     *            polygon
     * @param xMin
     *            minimal x coordinate (inclusive)
     * @param xMax
     *            minimal y coordinate (inclusive)
     * @param yMin
     *            maximal x coordinate (exclusive)
     * @param yMax
     *            maximal y coordinate (exclusive)
     * @return
     */
    public static Generator clipBoundariesOfDiagonalList(
            final Generator eT, final int xMin, final int xMax,
            final int yMin, final int yMax) {
        return new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                Node l;
                if ((l = (Node) eT.gen()) != null) {
                    if (l.y < yMin) {
                        // adjust y coordinates if possible
                        if (l.yMax < yMin) {
							return this.gen();
						}
                        l.x += yMin - l.y;
                        l.y = yMin;
                    }
                    if (l.yMax >= yMax) {
                        if (l.y >= yMax) {
							return this.gen();
						}
                        l.yMax = yMax - 1;
                    }
                    final int xM = l.x + l.yMax - l.y;
                    if (l.x < xMin) {
                        // adjust x coordinates if possible
                        if (xM < xMin) {
							return this.gen();
						}
                        l.y += xMin - l.x;
                        l.x = xMin;
                    }
                    if (xM >= xMax) {
                        if (l.x >= xMax) {
							return this.gen();
						}
                        l.yMax -= xM - xMax + 1;
                    }
                    return l;
                }
                return null;
            }
        };
    }
}