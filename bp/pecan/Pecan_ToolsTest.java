/*
 * Created on Jun 27, 2005
 */
package bp.pecan;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.Array;
import bp.common.ds.LockedObject;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Predicates_2Args;
import bp.common.io.NewickTreeParser;
import bp.common.io.NewickTreeParserTest;
import bp.common.io.NewickTreeParser.Node;

/**
 * @author benedictpaten
 */
public class Pecan_ToolsTest
                            extends TestCase {

    public void testGetPairOrdering() {
        for (int trial = 0; trial < 1000;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if (seqNo < 3) {
				continue;
			}
            trial++;
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            for (int i = 0; i < pairOrdering.length; i++) {
                final int[] iA = pairOrdering[i];
                final double d = dAA[iA[0]][iA[1]];
                for (int j = 0; j < i; j++) {
                    final int[] iA2 = pairOrdering[j];
                    final double d2 = dAA[iA2[0]][iA2[1]];
                    Assert.assertTrue((d2 < d)
                            || ((d == d2) && ((iA2[0] < iA[0]) || ((iA2[0] == iA[0]) && (iA2[1] < iA[1])))));
                }
            }
        }
    }

    public static void testAveragePathLengthToChildren() {
        for (int trial = 0; trial < 1000; trial++) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            PecanTools.replaceEdgeLengths(n, Double.MIN_VALUE, 0);
            final double d1 = PecanTools.averagePathLengthToChildren(n);
            final double d2 = Pecan_ToolsTest.averagePathLengthToChildren(n);
            Assert.assertEquals(d1, d2, 0.000001);
        }
    }

    public static double averagePathLengthToChildren(
            final NewickTreeParser.Node n) {
        NewickTreeParser.Node m;
        double d = 0;
        int i = 0;
        for (final Generator gen = PecanTools.leafGenerator(n); (m = (NewickTreeParser.Node) gen
                .gen()) != null;) {
            i++;
            while (m != null) {
                d += m.edgeLength;
                m = m.getParent();
            }
        }
        return d / i;
    }

    public void testGetCommonAncestor() {
        for (int trial = 0; trial < 1000; trial++) {
            NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            NewickTreeParser.Node o = n;
            while ((Math.random() > 0.5) && (o.getNodes().size() != 0)) {
                o = (NewickTreeParser.Node) o.getNodes().get(
                        (int) (o.getNodes().size() * Math.random()));
            }
            n = o;
            NewickTreeParser.Node m;
            NewickTreeParser.Node p;
            o = n;
            while ((Math.random() > 0.5) && (o.getNodes().size() > 0)) {
                o = (NewickTreeParser.Node) o.getNodes().get(0);
            }
            m = o;
            o = n;
            while ((Math.random() > 0.5) && (o.getNodes().size() > 1)) {
                o = (NewickTreeParser.Node) o.getNodes().get(
                        1 + (int) ((o.getNodes().size() - 1) * Math
                                .random()));
            }
            p = o;
            Assert.assertEquals(n, PecanTools.getCommonAncestor(m, p));
        }
    }

    public void testLeafGenerator() {
        for (int trial = 0; trial < 10000; trial++) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final List l = new LinkedList();
            Pecan_ToolsTest.getLeaves(l, n);
            Assert.assertTrue(IterationTools
                    .equals(l.iterator(), new GeneratorIterator(
                            PecanTools.leafGenerator(n)),
                            Predicates_2Args.equal()));
        }
    }

    public static void getLeaves(final List l, final NewickTreeParser.Node n) {
        if (n.getNodes().size() == 0) {
            l.add(n);
        } else {
            for (final Iterator<Object> it = n.getNodes().iterator(); it.hasNext();) {
                Pecan_ToolsTest.getLeaves(l, (Node) it.next());
            }
        }
    }

    public void testGetOutgroups() {
        // one less than
        double[] dA1 = new double[] { Integer.MIN_VALUE, 8, 5, 6 }, dA2 = new double[] {
                8, Integer.MIN_VALUE, 1, 12 };
        int[] iA = PecanTools.getOutgroups(new LockedObject(
                new int[1000]), dA1, dA2, dA1.length, 0, 1);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 2 }));
        // two less than
        dA1 = new double[] { 5, Integer.MIN_VALUE, 8, 7, 6 };
        dA2 = new double[] { 5, 8, Integer.MIN_VALUE, 5, 12 };
        iA = PecanTools.getOutgroups(new LockedObject(new int[1000]),
                dA1, dA2, dA1.length, 1, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 3 }));
        // two less than, reverse order
        dA1 = new double[] { 5, Integer.MIN_VALUE, 8, 4, 6 };
        dA2 = new double[] { 5, 8, Integer.MIN_VALUE, 4, 12 };
        iA = PecanTools.getOutgroups(new LockedObject(new int[1000]),
                dA1, dA2, dA1.length, 1, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 3 }));
        // equal order for number less than
        dA1 = new double[] { 8, Integer.MIN_VALUE, 8, 7, 6 };
        dA2 = new double[] { 8, 8, Integer.MIN_VALUE, 1, 12 };
        iA = PecanTools.getOutgroups(new LockedObject(new int[1000]),
                dA1, dA2, dA1.length, 1, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 3 }));
        // value less than excluded
        dA1 = new double[] { 9, Integer.MIN_VALUE, 8, 7, 6 };
        dA2 = new double[] { 6, 8, Integer.MIN_VALUE, 1, 12 };
        iA = PecanTools.getOutgroups(new LockedObject(new int[1000]),
                dA1, dA2, dA1.length, 1, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 3 }));
        // value greater than excluded
        dA1 = new double[] { 6, Integer.MIN_VALUE, 8, 8, 6 };
        dA2 = new double[] { 5, 8, Integer.MIN_VALUE, 7, 12 };
        iA = PecanTools.getOutgroups(new LockedObject(new int[1000]),
                dA1, dA2, dA1.length, 1, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0 }));
        // three values sorted
        dA1 = new double[] { 6, Integer.MIN_VALUE, 8, 6, 6 };
        dA2 = new double[] { 3, 8, Integer.MIN_VALUE, 5, 2 };
        iA = PecanTools.getOutgroups(new LockedObject(new int[1000]),
                dA1, dA2, dA1.length, 1, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 3, 4 }));
        // three values sorted, earlier value equal to later value
        dA1 = new double[] { 6, Integer.MIN_VALUE, 8, 6, 6 };
        dA2 = new double[] { 2, 8, Integer.MIN_VALUE, 5, 2 };
        iA = PecanTools.getOutgroups(new LockedObject(new int[1000]),
                dA1, dA2, dA1.length, 1, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 3, 4 }));
    }

    public void testCutUpDiagonals() throws Exception {
        for (int trial = 0; trial < 100; trial++) {
            final List l = (List) IterationTools.append(PolygonFillerTest
                    .clipUpperDiagonalEdgeList(PolygonFillerTest
                            .makeRandomEdgeList(Math.random() * 0.95,
                                    Math.random() * 0.95,
                                    (int) (Math.random() * 500))
                            .iterator()), new LinkedList());
            final PecanTools.CutUpDiagonals cUD = new PecanTools.CutUpDiagonals(
                    Generators
                            .iteratorGenerator(PolygonFiller
                                    .cloneEdgeList(
                                            PolygonFillerTest
                                                    .convertOldEdgeListToNewEdgeList(l))
                                    .iterator()));
            // Generator gen2 =
            // PolygonFillerTest.convertNewEdgeListToOldEdgeList(cUD);
            final Iterator it = l.iterator();
            int cutPoint = (int) (Math.random() * 50);
            cUD.setCutUpPoint(cutPoint);
            for (; it.hasNext();) {
                final List l2 = (List) it.next();
                int yI = ((Integer) l2.get(0)).intValue();
                int[] iA = (int[]) l2.get(1);
                int xI = iA[PolygonFillerTest.X];
				final int yMaxI = iA[PolygonFillerTest.YMAX], xMaxI = xI
                        + yMaxI - yI;
                List<Object> l3 = PolygonFillerTest
                        .convertNewEdgeToOldEdge((PolygonFiller.Node) cUD
                                .gen()); // cUD.gen();
                iA = (int[]) l3.get(1);
                int yJ = ((Integer) l3.get(0)).intValue();
                int xJ = iA[PolygonFillerTest.X], yMaxJ = iA[PolygonFillerTest.YMAX], xMaxJ = xJ
                        + yMaxJ - yJ;
                if (xMaxI + yMaxI < cutPoint) {
                    Assert.assertEquals(xI, xJ);
                    Assert.assertEquals(yI, yJ);
                    Assert.assertEquals(xMaxI, xMaxJ);
                    Assert.assertEquals(yMaxI, yMaxJ);
                } else {
                    if (xI + yI <= cutPoint) {
                        Assert.assertEquals(xI, xJ);
                        Assert.assertEquals(yI, yJ);
                        final int i = (cutPoint - xI - yI) / 2;
                        Assert.assertEquals(xI + i, xMaxJ);
                        Assert.assertEquals(yI + i, yMaxJ);
                        xI += i + 1;
                        yI += i + 1;
                    } else {
                        Assert.assertEquals(xI, xJ);
                        Assert.assertEquals(yI, yJ);
                        Assert.assertEquals(xI++, xMaxJ);
                        Assert.assertEquals(yI++, yMaxJ);
                    }
                    while (yI <= yMaxI) {
                        l3 = PolygonFillerTest
                                .convertNewEdgeToOldEdge((PolygonFiller.Node) cUD
                                        .gen());
                        iA = (int[]) l3.get(1);
                        yJ = ((Integer) l3.get(0)).intValue();
                        xJ = iA[PolygonFillerTest.X];
                        yMaxJ = iA[PolygonFillerTest.YMAX];
                        xMaxJ = xJ + yMaxJ - yJ;
                        Assert.assertEquals(xI, xJ);
                        Assert.assertEquals(yI, yJ);
                        Assert.assertEquals(xI++, xMaxJ);
                        Assert.assertEquals(yI++, yMaxJ);
                    }
                }
                if ((xMaxI + yMaxI >= cutPoint) && (Math.random() > 0.5)) {
                    cUD.setCutUpPoint((cutPoint = (int) (Math
                            .random() * 100)));
                }
            }
            Assert.assertEquals(null, cUD.gen());
            Assert.assertEquals(null, cUD.gen());
        }
    }

    public void testReplaceEdgeLengths() {
        final String[] sA = new String[] { "();", "((a, b), c);" };
        final String[] sA2 = new String[] { "(:1.0):1.0;",
                "((a:1.0,b:1.0):1.0,c:1.0):1.0;" };
        for (int i = 0; i < sA.length; i++) {
            final NewickTreeParser nTP = new NewickTreeParser(
                    NewickTreeParser
                            .tokenise(new StringReader(sA[i])));
            PecanTools.replaceEdgeLengths(nTP.tree, Double.MIN_VALUE,
                    1.0);
            Assert.assertEquals(nTP.tree.toString(), sA2[i]);
        }
    }

    public void testGetDistances2() {
        final String tree = "(((((((((((((human:0.00669,chimp:0.007571):0.024272,((baboon:0.008258,macaque:0.028617):0.008519):0.02212):0.02396,((marmoset:0.029549):0.008236):0.027158):0.066101,(galago:0.121375):0.032386):0.017073,((rat:0.081728,mouse:0.077017):0.229273,rabbit:0.206767):0.02334):0.023026,(((cow:0.159182,dog:0.147731):0.004946,rfbat:0.138877):0.01015,(shrew:0.261724):0.054246):0.024354):0.028505,armadillo:0.149862):0.015994,(elephant:0.104891,tenrec:0.259797):0.040371):0.2184):0.065268):0.123856):0.123297):0.156067):1.0;";
        final NewickTreeParser nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader(tree)));
        final double[][] dA = PecanTools.getDistances(nTP.tree,
                16, Functions_2Args.sum());
    }

    public void testGetDistances() {
        final double[][][] dAAA = new double[][][] {
                { { Double.NaN, 15 }, { 15, Double.NaN } },
                { { Double.NaN, 5, 40, 12 },
                        { 5, Double.NaN, 41, 13 },
                        { 40, 41, Double.NaN, 32 },
                        { 12, 13, 32, Double.NaN } },
                { { Double.NaN, 2, 3 }, { 2, Double.NaN, 3 },
                        { 3, 3, Double.NaN } } };
        final String[] sA = new String[] { "(a:5, b:10);",
                "((a:2, b:3):7, ((c:10):20, d:2):1);", "((a, b), c);" };
        for (int i = 0; i < dAAA.length; i++) {
            final NewickTreeParser nTP = new NewickTreeParser(
                    NewickTreeParser
                            .tokenise(new StringReader(sA[i])));
            PecanTools.replaceEdgeLengths(nTP.tree, Double.MIN_VALUE,
                    1.0);
            final double[][] dA = PecanTools.getDistances(nTP.tree,
                    dAAA[i].length, Functions_2Args.sum());
            Assert.assertTrue(Array.arraysEqual().test(dA, dAAA[i],
                    new Predicate_2Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
                         *      java.lang.Object)
                         */
                        public boolean test(final Object o, final Object o2) {
                            return Arrays.equals((double[]) o,
                                    (double[]) o2);
                        }
                    }));
        }
    }
}