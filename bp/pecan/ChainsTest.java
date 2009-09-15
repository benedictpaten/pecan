/*
 * Created on Nov 15, 2005
 */
package bp.pecan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.Graphics;
import bp.common.ds.Array;
import bp.common.ds.LockedObject;
import bp.common.fp.Function;
import bp.common.fp.Function_Index;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Functions_Int_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.GeneratorTools;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_Int_2Args;
import bp.common.io.Debug;
import bp.common.io.NewickTreeParser;
import bp.common.io.NewickTreeParserTest;
import bp.common.maths.Maths;
import bp.pecan.Chains.CutPoint;
import bp.pecan.Chains.Node;
import bp.pecan.Chains.PrimeConstraints;
import bp.pecan.Chains.PrimeConstraints.ConstraintNode;

/**
 * @author benedictpaten
 */
public class ChainsTest
                       extends TestCase {
    
    public void testRecursiveAlignment() throws Exception {
        for (int trial = 0; trial < 100; trial++) {
            final int length = 1 + (int) (Math.random() * 100);
            final Chains.Aligner[] fA = new Chains.Aligner[length];
            for (int i = 0; i < fA.length; i++) {
				fA[i] = this.sillyAligner(i);
			}
            Debug.pl(" Length " + length);
            final int[] gaps = new int[fA.length];
            int j = length;
            for (int i = 0; i < gaps.length; i++) {
                gaps[i] = j * 2 + 2;// 3;
                j /= 2;
            }
            final byte[] bA = new byte[length];
            final byte[] bA2 = new byte[length];
            for (int i = 0; i < length; i++) {
                bA[i] = new byte[] { 'A', 'B', 'C' }[(int) (Math
                        .random() * 3)];
                bA2[i] = new byte[] { 'A', 'B', 'C' }[(int) (Math
                        .random() * 3)];
            }
            Generator gen = Generators.arrayGenerator(new Object[0]);
            for (int i = 0; i < fA.length; i++) {
                List ll = (List) GeneratorTools.append(gen,
                        new LinkedList());
                final List l2 = Chains.localAlignmentsInGaps(Generators
                        .iteratorGenerator(ll.iterator()), fA[i], 1,
                        gaps[i], 0, 0, length, length, bA, bA2);
                ll = (List) IterationTools.append(PolygonFiller
                        .combineEdgeLists(ll.iterator(), l2
                                .iterator()), new LinkedList());
                gen = Generators.iteratorGenerator(ll.iterator());
            }
            final List l = PolygonFillerTest
                    .convertNewEdgeListToOldEdgeList((List) IterationTools
                            .append(new GeneratorIterator(gen),
                                    new LinkedList()));
            // List l = (List) IterationTools.append(PecanTools
            // .recursiveAlignment(fA, 2, gaps, 0, 0, 0, length,
            // length, bA, bA2), new LinkedList());
            int i = 0;
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final List l2 = (List) it.next();
                Assert.assertEquals(((Integer) l2.get(0)).intValue(), i);
                Assert.assertEquals(l2.size(), 2);
                final int[] iA = (int[]) l2.get(1);
                Assert.assertEquals(iA[PolygonFillerTest.X], i);
                Assert.assertEquals(iA[PolygonFillerTest.YMAX], i);
                i++;
            }
            Assert.assertEquals(i, length);
        }
    }

    public void testCutPointReordering() {
        for (int trial = 0; trial < 25;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if ((seqNo < 3) || (seqNo > 8)) {
				//if (seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            final int[][][] sequenceOutgroups = PecanTools.getOutgroups(
                    new LockedObject(new int[10000]), dAA, seqNo);
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.8, 0.9, 100);
                    Chains.transformChains(cAA[i][j], 20, 20);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo);
            // we don't transform the sequences, so points must be consistent
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            final List[][] cutPoints = new List[seqNo][seqNo];
            final Set s = new HashSet();
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    cutPoints[i][j] = this.cutPoints(i, j, cAA[i][j]);
                    s.addAll(cutPoints[i][j]);
                }
            }
            final int maxIndexShift = (int) (Math.random() * 1000);
            final List l = Chains.CutPointOrdering.orderCutPoints(pC,
                    cutPoints);
            final List l2 = Chains.CutPointReordering.reorder(l, seqNo,
                    sequenceOutgroups, pairOrdering, maxIndexShift, -100);
            final Map<CutPoint, Integer> m = new HashMap<CutPoint, Integer>();
            int index = 0;
            final int[][] iAA = new int[seqNo][seqNo];
            for(int i=0; i<seqNo; i++) {
                for(int j=0; j<seqNo; j++) {
                    iAA[i][j] = -100;
                }
            }
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final CutPoint cP = (CutPoint) it.next();
                final int k = cP.x + cP.y - iAA[cP.s1][cP.s2];
                if(k == 0) {
                    Assert.assertTrue(cP.tB == 1);
                }
                else {
                    Assert.assertTrue(k > 0);
                    Assert.assertTrue(cP.tB == 0);
                }
                index += k;
                m.put(cP, new Integer(index));
                iAA[cP.s1][cP.s2] = cP.x + cP.y;
            }
            final Map<CutPoint, Integer> m2 = new HashMap<CutPoint, Integer>();
            index = 0;
            for (final Iterator it = l2.iterator(); it.hasNext();) {
                final CutPoint cP = (CutPoint) it.next();
                m2.put(cP, new Integer(index++));
            }
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final CutPoint cP = (CutPoint) it.next();
                index = m2.get(cP).intValue();
                final int i = m.get(cP).intValue();
                for (final Iterator it2 = l.iterator(); it2.hasNext();) {
                    final CutPoint cP2 = (CutPoint) it2.next();
                    final int j = m.get(cP2).intValue();
                    if ((j - maxIndexShift <= i)
                            && ((ChainsTest.inArray(
                                    sequenceOutgroups[cP.s1][cP.s2],
                                    cP2.s1) && ((cP2.s2 == cP.s1) || (cP2.s2 == cP.s2))) || (ChainsTest.inArray(
                                    sequenceOutgroups[cP.s1][cP.s2],
                                    cP2.s2) && ((cP2.s1 == cP.s1) || (cP2.s1 == cP.s2))))) {
                        final int index2 = m2.get(cP2)
                                .intValue();
                        if(index < index2) {
							Assert.fail(cP + " " + cP2 + " " + index + " " + index2);
						}
                        Assert.assertTrue(index > index2);
                    }
                }
            }
        }
    }

    static boolean inArray(final int[] iA, final int i) {
        for (final int element : iA) {
			if (element == i) {
				return true;
			}
		}
        return false;
    }

    public void testRelativeEntropy() {
        for (int trial = 0; trial < 1000; trial++) {
            final int a = (int) (Math.random() * 1000);
            final int c = (int) (Math.random() * 1000);
            final int g = (int) (Math.random() * 1000);
            final int t = (int) (Math.random() * 1000);
            final float[] logs = new float[1000];
            for (int i = 1; i < logs.length; i++) {
                logs[i] = (float) Math.log(i);
            }
            final float rE = Chains.relativeEntropy(a, c, g, t, logs);
            final float total = a + c + g + t;
            final float aF = a / total;
            final float cF = c / total;
            final float gF = g / total;
            final float tF = t / total;
            float rE2 = -(ChainsTest.getVal(aF) + ChainsTest.getVal(cF) + ChainsTest.getVal(gF) + ChainsTest.getVal(tF))
                    / Maths.log(4);
            if (total == 0) {
				rE2 = 1;
			}
            Assert.assertEquals(rE, rE2, 0.001);
        }
    }

    public static float getVal(final float aF) {
        return aF * (aF == 0 ? 0 : Maths.log(aF));
    }

    public void testCountAlignments() {
        for (int trial = 0; trial < 1000; trial++) {
            final Chains.Node m = ChainsTest.getRandomChain(0.6, 0.9, 100);
            final List l = (List) GeneratorTools.append(Chains
                    .convertNodeChainToEdgeList(m), new LinkedList());
            final byte[] bA = new byte[2000];
            final byte[] bA2 = new byte[2000];
            for (int i = 0; i < 2000; i++) {
                bA[i] = ChainsTest.getRandomNucleotide();
                bA2[i] = ChainsTest.getRandomNucleotide();
            }
            final int[] iA = Chains.countAlignments(bA, bA2, l);
            int a = 0;
            int c = 0;
            int g = 0;
            int t = 0;
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
                while (n.y <= n.yMax) {
                    if (bA[n.x] == bA2[n.y]) {
                        if ((bA[n.x] == 'A') || (bA[n.x] == 'a')) {
							a++;
						} else if ((bA[n.x] == 'C') || (bA[n.x] == 'c')) {
							c++;
						} else if ((bA[n.x] == 'G') || (bA[n.x] == 'g')) {
							g++;
						} else if ((bA[n.x] == 'T') || (bA[n.x] == 't')) {
							t++;
						}
                    }
                    n.x++;
                    n.y++;
                }
            }
            Assert.assertEquals(iA.length, 4);
            Assert.assertEquals(iA[0], a);
            Assert.assertEquals(iA[1], c);
            Assert.assertEquals(iA[2], g);
            Assert.assertEquals(iA[3], t);
        }
    }

    static byte getRandomNucleotide() {
        return new byte[] { 'A', 'C', 'G', 'T', 'a', 'c', 'g', 't',
                'N', 'x' }[(int) (Math.random() * 10)];
    }

    public void testBreaks() {
        for (int trial = 0; trial < 100;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if ((seqNo < 3) || (seqNo > 12)) {
				// if (seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            final int xStart = 0;
            final int yStart = 0;
            final int max = ChainsTest.getBox().length - 1;
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.8, 0.9, 50);
                    Chains.transformChains(cAA[i][j], xStart + 10,
                            yStart + 10);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo); //
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final int maxGap = (int) (Math.random() * 20);
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    pC.updatePrimeConstraints(i, j, 1, 1, 0);
                    pC.updatePrimeConstraints(j, i, 1, 1, 0);
                    pC.updatePrimeConstraints(i, j, max, max, 0);
                    pC.updatePrimeConstraints(j, i, max, max, 0);
                }
            }

            if (!this.isConsistent(pC, seqNo, max + 10)) {
				continue;
			}
            final List[][] lAA = new List[seqNo][seqNo];
            for (int i = 0; i < seqNo; i++) {
				for (int j = i + 1; j < seqNo; j++) {
                    lAA[i][j] = (List) GeneratorTools.append(pC
                            .convertPrimeConstraintsToEdgeList(i, j,
                                    true), new LinkedList());
                }
			}

            final int diagonalGap = (int) (Math.random() * 5);
            final int borderGap = (int) (Math.random() * (maxGap / 2));
            final Chains.Aligner aligner = new Chains.Aligner() {
                public Iterator align(byte[] bA, byte[] bA2,
                        int startX, int lengthX, int startY,
                        int lengthY) {
                    Assert.assertTrue(startX + lengthX <= bA.length);
                    Assert.assertTrue(startY + lengthY <= bA2.length);
                    Assert.assertTrue(lengthX >= 0);
                    Assert.assertTrue(lengthY >= 0);
                    Assert.assertTrue(startX >= 0);
                    Assert.assertTrue(startY >= 0);
                    if ((lengthX != 0) && (lengthY != 0)) {
                        List l = new LinkedList();
                        while (Math.random() > 0.1) {
                            int x = (int) (Math.random() * lengthX);
                            int y = (int) (Math.random() * lengthY);
                            int length = (int) (lengthX - x > lengthY
                                    - y ? (lengthY - y)
                                    * Math.random() : (lengthX - x)
                                    * Math.random());
                            l.add(new PolygonFiller.Node(startX + x,
                                    startY + y, startY + y + length,
                                    1));
                        }
                        Collections.sort(l);
                        return l.iterator();
                        // return Arrays.asList(new Object[] {
                        // new PolygonFiller.Node(x, y, y, 1)
                        // }).iterator();
                    }
                    return Arrays.asList(new Object[] {}).iterator();
                }
            };
            Chains.transformConstraintsAndCloseLargeGaps(pC, lAA,
                    diagonalGap, maxGap, borderGap, aligner,
                    new byte[seqNo][1000], pairOrdering);
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    pC.updatePrimeConstraints(i, j, 1, 1, 0);
                    pC.updatePrimeConstraints(j, i, 1, 1, 0);
                    pC.updatePrimeConstraints(i, j, max, max, 0);
                    pC.updatePrimeConstraints(j, i, max, max, 0);
                }
            }

            final Generator gen1 = PolygonFiller
                    .flipEdgeXYDiagonalsCoordinates(pC
                            .convertPrimeConstraintsToEdgeList(1, 0,
                                    false));
            final Generator gen2 = PolygonFiller
                    .flipEdgeXYDiagonalsCoordinates(pC
                            .convertPrimeConstraintsToEdgeList(0, 1,
                                    false));
            final Function_Index fn = PolygonFiller.polygonIterator(gen1,
                    gen2, 0, Integer.MAX_VALUE / 2, 0,
                    Integer.MAX_VALUE / 2);
            final Object[] oA = (Object[]) fn.fn(ChainsTest.getBox().length * 2 + 20);
            final int[][] matrix = new int[500][500];
            PolygonFiller.scanPolygon(((List) oA[0]).iterator(),
                    ((List) oA[1]).iterator(),
                    new Procedure_Int_2Args() {
                        int y = -1;

                        public void pro(int i, int j) {
                            ConstraintNode cN = PrimeConstraints
                                    .getPrimeConstraint(
                                            pC.primeConstraints[1][0],
                                            this.y);
                            if (cN.maxOffset == Integer.MIN_VALUE) {
                                if (cN.x == this.y) {
                                    j = cN.y - 1;
                                }
                            } else {
                                if (cN.x + cN.maxOffset == this.y) {
									j = cN.y + cN.maxOffset;
								}
                            }
                            cN = PrimeConstraints
                                    .getRightMostPointAffectedByConstraint(
                                            pC.primeConstraints[0][1],
                                            this.y + 1);
                            if (cN.y == this.y + 1) {
                                if (cN.maxOffset == Integer.MIN_VALUE) {
                                    i = cN.x;
                                } else {
									i = cN.x - 1;
								}
                            }
                            if ((j <= i) && (i < 300) && (j < 300)) {
                                // Debug.pl(i + " " + j + " " + y);
                                matrix[j][this.y] = 1;
                                matrix[i][this.y] = 1;
                            }
                            this.y++;
                        }
                    });
            final List l = Chains.breaks(pC, 0, 1);
            int pX = Integer.MIN_VALUE;
            int pY = Integer.MIN_VALUE;
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final int[] iA = (int[]) it.next();
                if ((iA[0] < 300) && (iA[1] < 300)) {
                    if (matrix[iA[0]][iA[1]] == 0) {
						Assert.fail();
					}
                    if (iA[0] < pX) {
						Assert.fail();
					}
                    if (iA[1] < pY) {
						Assert.fail();
					}
                    pX = iA[0];
                    pY = iA[1];
                    matrix[iA[0]][iA[1]] = 2;

                    if (matrix[iA[2]][iA[3]] == 0) {
						Assert.fail();
					}
                    if (iA[2] < pX) {
						Assert.fail();
					}
                    if (iA[3] < pY) {
						Assert.fail();
					}
                    pX = iA[2];
                    pY = iA[3];
                    matrix[iA[2]][iA[3]] = 2;
                }
            }

            /*
             * for (int i = 0; i < 300; i++) { for (int j = 0; j < 300; j++) {
             * if (matrix[i][j] == 1) if (matrix[i - 1][j] == 0 && matrix[i][j -
             * 1] == 0) fail(); } }
             */

        }
    }

    public void testEliminateSpans() {
        // tests close gap minimiser also
        for (int trial = 0; trial < 100;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if ((seqNo < 3) || (seqNo > 12)) {
				// if (seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            final int xStart = 0;
            final int yStart = 0;
            final int max = ChainsTest.getBox().length - 1;
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.8, 0.9, 50);
                    Chains.transformChains(cAA[i][j], xStart + 10,
                            yStart + 10);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo); //
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final int maxGap = (int) (Math.random() * 20);
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    pC.updatePrimeConstraints(i, j, 0, 0, 0);
                    pC.updatePrimeConstraints(j, i, 0, 0, 0);
                    pC.updatePrimeConstraints(i, j, max, max, 0);
                    pC.updatePrimeConstraints(j, i, max, max, 0);
                }
            }

            if (!this.isConsistent(pC, seqNo, max + 10)) {
				continue;
			}
            final List[][] lAA = new List[seqNo][seqNo];
            for (int i = 0; i < seqNo; i++) {
				for (int j = i + 1; j < seqNo; j++) {
                    lAA[i][j] = (List) GeneratorTools.append(pC
                            .convertPrimeConstraintsToEdgeList(i, j,
                                    true), new LinkedList());
                }
			}

            final int diagonalGap = (int) (Math.random() * 5);
            final int borderGap = (int) (Math.random() * (maxGap / 2));
            final Chains.Aligner aligner = new Chains.Aligner() {
                public Iterator align(byte[] bA, byte[] bA2,
                        int startX, int lengthX, int startY,
                        int lengthY) {
                    Assert.assertTrue(startX + lengthX <= bA.length);
                    Assert.assertTrue(startY + lengthY <= bA2.length);
                    Assert.assertTrue(lengthX >= 0);
                    Assert.assertTrue(lengthY >= 0);
                    Assert.assertTrue(startX >= 0);
                    Assert.assertTrue(startY >= 0);
                    if ((lengthX != 0) && (lengthY != 0)) {
                        List l = new LinkedList();
                        while (Math.random() > 0.1) {
                            int x = (int) (Math.random() * lengthX);
                            int y = (int) (Math.random() * lengthY);
                            int length = (int) (lengthX - x > lengthY
                                    - y ? (lengthY - y)
                                    * Math.random() : (lengthX - x)
                                    * Math.random());
                            l.add(new PolygonFiller.Node(startX + x,
                                    startY + y, startY + y + length,
                                    1));
                        }
                        Collections.sort(l);
                        return l.iterator();
                        // return Arrays.asList(new Object[] {
                        // new PolygonFiller.Node(x, y, y, 1)
                        // }).iterator();
                    }
                    return Arrays.asList(new Object[] {}).iterator();
                }
            };
            Chains.transformConstraintsAndCloseLargeGaps(pC, lAA,
                    diagonalGap, maxGap, borderGap, aligner,
                    new byte[seqNo][1000], pairOrdering);
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    pC.updatePrimeConstraints(i, j, 0, 0, 0);
                    pC.updatePrimeConstraints(j, i, 0, 0, 0);
                    pC.updatePrimeConstraints(i, j, max, max, 0);
                    pC.updatePrimeConstraints(j, i, max, max, 0);
                }
            }
            for (int i = 0; i < seqNo; i++) {
				for (int j = i + 1; j < seqNo; j++) {
                    final List l = lAA[i][j];
                    Chains.Node node = Chains
                            .convertEdgeListToNodeChain(Generators
                                    .iteratorGenerator(l.iterator()));
                    final int[][] iAA = ChainsTest.getBox();
                    ChainsTest.outLineChain(iAA, node, false);
                    node = pC.filterByConstraints(i, j, node);
                    node = Chains.sortOppositeChain(node);
                    node = pC.filterByConstraints(j, i, node);
                    node = Chains.sortOppositeChain(node);
                    final int[][] iAA2 = ChainsTest.getBox();
                    ChainsTest.outLineChain(iAA2, node, false);
                    ChainsTest.arraysEquals(iAA, iAA2);
                }
			}

            final int[] ends = new int[seqNo];
            Arrays.fill(ends, max);
            ChainsTest.removeOnePlaceTransitiveChains(pC, /* maxGap + 1 */80,
                    ends);
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    int[][] iAA = ChainsTest.getBox();
                    this.fillInMatrix(iAA, pC.primeConstraints[0][1]
                            .iterator(), pC.primeConstraints[1][0]
                            .iterator());
                    ChainsTest.checkMatrix(iAA, iAA.length - 1, 0, 1000);
                    iAA = ChainsTest.getBox();
                    this.fillInMatrix(iAA, pC.primeConstraints[1][0]
                            .iterator(), pC.primeConstraints[0][1]
                            .iterator());
                    ChainsTest.checkMatrix(iAA, iAA.length - 1, 0, 1000);
                }
            }
            Assert.assertTrue(this.isConsistent(pC, seqNo, max + 10));
        }
    }

    public void testMakeConsistentChains() {
        for (int trial = 0; trial < 100;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if (seqNo > 3) {
				// if(seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.6, 0.9, 100);
                    Chains.transformChains(cAA[i][j], 20, 20);
                    /*
                     * Chains.Node cN = cAA[i][j]; while (cN != null) { cN.xE =
                     * cN.xS; cN.n.xE = cN.n.xS; cN = cN.p; }
                     */
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo);
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            Chains.transformConstraints((int) (Math.random() * 10),
                    pC);
            Assert.assertTrue(this.isConsistent(pC, seqNo, 300));
        }
    }

    public void testOverlapAndMerge() {
        for (int trial = 0; trial < 1000; trial++) {
            Debug.pl(" trial " + trial);
            final Chains.Node m = ChainsTest.getRandomChain(0.6, 0.9, 100);
            final Chains.Node n = ChainsTest.getRandomChain(0.6, 0.9, 100);
            final int[][] iAA = ChainsTest.overlap(m, n);
            final Chains.Node p = Chains.overlapAndMerge(m, n,
                    Functions_Int_2Args.sum());
            ChainsTest.checkOrderingOfNet(p, false);
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA2, p, false);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public void testConsistencyChains() {
        for (int trial = 0; trial < 1000; trial++) {
            final Chains.Node m = ChainsTest.getRandomChain(0.6, 0.9, 100);
            final Chains.Node n = ChainsTest.getRandomChain(0.6, 0.9, 100);
            ChainsTest.checkOrderingOfNet(m, false);
            ChainsTest.checkOrderingOfNet(n, false);
            final int[][] iAA = ChainsTest.consistencyChains(m, n);
            final Chains.Node p = Chains.consistencyChains(m, n,
                    Functions_Int_2Args.sum(), Functions_Int_2Args.sum());
            ChainsTest.checkOrderingOfNet(p, false);
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA2, p, false);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public void testFilterCutPoints_Big() {
        for (int trial = 0; trial < 100; trial++) {
            Debug.pl(trial + " ");
            final List cutpoints = new LinkedList();
            final List blocks = new LinkedList();
            final int tB = (int) (Math.random() * 20);
            {
                int pX = 50;
                int pY = 50;
                while (Math.random() > 0.01) {
                    pX += 1 + (int) (Math.random() * 10);
                    pY += 1 + (int) (Math.random() * 10);
                    cutpoints.add(new Chains.CutPoint(0, 1, pX, pY,
                            tB));
                }
            }
            {
                int pX = 0;
                int pY = 0;
                while (Math.random() > 0.01) {
                    pX += 1 + (int) (Math.random() * 20);
                    pY += 1 + (int) (Math.random() * 20);
                    final int x = pX + (int) (Math.random() * 10);
                    final int y = pY + (int) (Math.random() * 10);
                    blocks.add(new int[] { pX, pY, x, y });
                    pX = x;
                    pY = y;
                }
            }
            final int[] iA = new int[1000000];
            for (final Iterator it = blocks.iterator(); it.hasNext();) {
                final int[] iA2 = (int[]) it.next();
                int i = iA2[0] + iA2[1];
                final int j = iA2[2] + iA2[3];
                while (i <= j) {
                    iA[i++] = 1;
                }
            }
            final List tCutPoints = (List) IterationTools.append(Iterators
                    .filter(cutpoints.iterator(), new Predicate() {
                        public boolean test(Object o) {
                            Chains.CutPoint cP = (Chains.CutPoint) o;
                            int i = cP.x + cP.y - tB;
                            int j = cP.x + cP.y;
                            while (i <= j) {
                                if (iA[i++] == 1) {
									return false;
								}
                            }
                            return true;
                        }
                    }), new LinkedList());
            final List fCutPoints = (List) IterationTools.append(Chains
                    .filterCutPoints(cutpoints, blocks, tB),
                    new LinkedList());
            Assert.assertTrue(IterationTools.equals(fCutPoints.iterator(),
                    tCutPoints.iterator(), new Predicate_2Args() {
                        public boolean test(final Object o, final Object o2) {
                            boolean b = o.equals(o2);
                            if (!b) {
								Assert.fail();
							}
                            return b;
                        };
                    }));
        }
    }

    public void testFilterByConstraints() {
        for (int trial = 0; trial < 1000; trial++) {
            Debug.pl(" trial " + trial);
            Chains.Node n = ChainsTest.getRandomChain(0.6, 0.9, 100);
            final int[][] iAA = ChainsTest.getBox();
            final int[][] iAA_C = ChainsTest.getBox();
            final List l = new LinkedList();
            int x = 0;
            int y = 0;
            {
                while ((Math.random() > 0.1) && (x < 150) && (y < 100)) {
                    final boolean type = // false;
                    Math.random() > 0.5 ? Chains.PrimeConstraints.ConstraintNode.LESS_THAN
                            : Chains.PrimeConstraints.ConstraintNode.LESS_THAN_OR_EQUAL;
                    if (type == Chains.PrimeConstraints.ConstraintNode.LESS_THAN_OR_EQUAL) {
                        final int length = (int) (Math.random() * 15);
                        final Chains.PrimeConstraints.ConstraintNode cN = new Chains.PrimeConstraints.ConstraintNode(
                                x, y, length);
                        x += length + ((int) (Math.random() * 15));
                        y += length + ((int) (Math.random() * 15));
                        l.add(cN);
                    } else {
                        final Chains.PrimeConstraints.ConstraintNode cN = new Chains.PrimeConstraints.ConstraintNode(
                                x, y, Integer.MIN_VALUE);
                        x += (int) (Math.random() * 15);
                        y += (int) (Math.random() * 15);
                        l.add(cN);
                    }
                    x++;
                    y++;
                }
            }
            ChainsTest.fillInConstraints(iAA_C, l);
            ChainsTest.checkChain(n);
            ChainsTest.filterNodesByConstraints(iAA, iAA_C, n);
            n = Chains.PrimeConstraints.filterByConstraints(n, l);
            ChainsTest.checkChain(n);
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA2, n, false);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public void testFilterCutPoints() {
        final Object[] oA = new Object[] {
                new Chains.CutPoint(0, 1, 0, 0, 5),
                new Chains.CutPoint(0, 1, 0, 4, 5),
                new Chains.CutPoint(0, 1, 5, 9, 5),
                new Chains.CutPoint(0, 1, 5, 10, 5),
                new Chains.CutPoint(0, 1, 10, 15, 5),
                new Chains.CutPoint(0, 1, 11, 16, 5),
                new Chains.CutPoint(0, 1, 11, 19, 5),
                new Chains.CutPoint(0, 1, 12, 19, 5),
                new Chains.CutPoint(0, 1, 30, 24, 5),
                new Chains.CutPoint(0, 1, 30, 25, 5),
                new Chains.CutPoint(0, 1, 32, 27, 5),
                new Chains.CutPoint(0, 1, 32, 28, 5),
                new Chains.CutPoint(0, 1, 33, 28, 5),
                new Chains.CutPoint(0, 1, 40, 40, 5) };
        final Object[] oA2 = new Object[] { new int[] { 5, 10, 10, 15 },
                new int[] { 25, 30, 25, 30 } };
        final Object[] oA3 = ((List) IterationTools.append(Chains
                .filterCutPoints(Arrays.asList(oA), Arrays
                        .asList(oA2), 5), new LinkedList()))
                .toArray();
        Assert.assertTrue(Arrays.equals(oA3, new Object[] { oA[0], oA[1],
                oA[2], oA[7], oA[8], oA[12], oA[13] }));
    }

    public void testPrimeConstraints() {
        for (int trial = 0; trial < 100;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if (seqNo < 3) {
				continue;
			}
            Debug.pl(" done " + trial);
            trial++;
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            final Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = i + 1; j < cAA[i].length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.6, 0.9, 100);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo);
            for (int i = 0; i < pairOrdering.length; i++) {
                final int[] iA = pairOrdering[i];
                cAA[iA[0]][iA[1]] = pC.filterByConstraints(iA[0],
                        iA[1], cAA[iA[0]][iA[1]]);
                cAA[iA[1]][iA[0]] = Chains
                        .sortOppositeChain(cAA[iA[0]][iA[1]]);
                cAA[iA[1]][iA[0]] = pC.filterByConstraints(iA[1],
                        iA[0], cAA[iA[1]][iA[0]]);
                cAA[iA[0]][iA[1]] = Chains
                        .sortOppositeChain(cAA[iA[1]][iA[0]]);
                cAA[iA[0]][iA[1]] = Chains
                        .getHighestChain(cAA[iA[0]][iA[1]]);

                // Chains
                // .convertEdgeListToNodeChain(Generators
                // .iteratorGenerator(Chains
                // .getHighestChain(
                // new GeneratorIterator(
                // Chains
                // .convertNodeChainToEdgeList(cAA[iA[0]][iA[1]])))
                // .iterator()));
                cAA[iA[1]][iA[0]] = Chains
                        .sortOppositeChain(cAA[iA[0]][iA[1]]);
                if (i + 1 < pairOrdering.length) {
                    pC.updatePrimeConstraints(iA[0], iA[1],
                            cAA[iA[0]][iA[1]]);
                    pC.updatePrimeConstraints(iA[1], iA[0],
                            cAA[iA[1]][iA[0]]);
                }
            }
            for (int i = 0; i < dAA.length; i++) {
                for (int j = i + 1; j < dAA.length; j++) {
                    ChainsTest.checkChainIsColinear(cAA[i][j]);
                    ChainsTest.checkChainIsColinear(cAA[j][i]);
                }
            }
            final Chains.Node[][] cAA2 = new Chains.Node[seqNo][seqNo];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA[i].length; j++) {
					cAA2[i][j] = Chains.copyOfChain(cAA[i][j]);
				}
            }
            Assert.assertTrue(ChainsTest.checkConsistency(cAA));
        }
    }

    public void testCutPointDFS() {
        for (int trial = 0; trial < 100;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if ((seqNo < 3) || (seqNo > 8)) {
				// if (seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.8, 0.9, 100);
                    Chains.transformChains(cAA[i][j], 20, 20);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo); //
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            Chains.transformConstraints((int) (Math.random() * 10),
                    pC);
            final List[][] cutPoints = new List[seqNo][seqNo];
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    cutPoints[i][j] = this.cutPoints(i, j, cAA[i][j]);
                }
            }
            {
                final int[][] iAA = Chains.CutPointOrdering
                        .getSequencePositions(pC.sequenceNumber,
                                cutPoints);
                final int[][] iAA2 = Chains.CutPointOrdering.dfs(pC, iAA);
                final SortedMap sS = new TreeMap();
                for (int i = 0; i < iAA2.length; i++) {
                    for (int j = 0; j < iAA2[i].length; j++) {
                        Assert.assertFalse(sS.containsKey(new Integer(
                                iAA2[i][j])));
                        sS.put(new Integer(iAA2[i][j]), new int[] {
                                i, iAA[i][j] });
                    }
                }
                {
                    int j = 0;
                    for (final int[] element : iAA) {
                        j += element.length;
                    }
                    Assert.assertEquals(j, sS.size());
                }
                final List l2 = (List) IterationTools.append(sS.values()
                        .iterator(), new ArrayList());
                Collections.reverse(l2);
                for (int i = 0; i < l2.size(); i++) {
                    final int[] iA = (int[]) l2.get(i);
                    for (int j = i + 1; j < l2.size(); j++) {
                        final int[] iA2 = (int[]) l2.get(j);
                        if (iA[0] == iA2[0]) {
                            if (!(iA[1] <= iA2[1])) {
								Assert.fail();
							}
                        } else {
                            final ConstraintNode cN = PrimeConstraints
                                    .getPrimeConstraint(
                                            pC.primeConstraints[iA2[0]][iA[0]],
                                            iA2[1]);
                            int y = cN.x < iA2[1] ? cN.y - cN.x
                                    + iA2[1] : cN.y;
                            if ((cN.maxOffset == Integer.MIN_VALUE)
                                    || (cN.x > iA2[1])) {
								y--;
							}
                            if (iA[1] > y) {
								Assert.fail();
							}
                        }
                    }
                }
            }
        }
    }

    public void testCutPointOrdering() {
        for (int trial = 0; trial < 100;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if ((seqNo < 3) || (seqNo > 8)) {
				// if (seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.8, 0.9, 100);
                    Chains.transformChains(cAA[i][j], 20, 20);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo);
            // we don't transform the sequences, so points must be consistent
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            final List[][] cutPoints = new List[seqNo][seqNo];
            final Set s = new HashSet();
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    cutPoints[i][j] = this.cutPoints(i, j, cAA[i][j]);
                    s.addAll(cutPoints[i][j]);
                }
            }
            final int[][] iAA = Chains.CutPointOrdering
                    .getSequencePositions(pC.sequenceNumber,
                            cutPoints);
            final int[][] iAA2 = Chains.CutPointOrdering.dfs(pC, iAA);
            final List l = Chains.CutPointOrdering.orderCutPoints(pC,
                    cutPoints);
            Assert.assertEquals(l.size(), s.size());
            final Set s2 = new HashSet();
            s2.addAll(l);
            Assert.assertTrue(s.equals(s2));
            for (int i = 0; i < l.size(); i++) {
                final CutPoint cP = (CutPoint) l.get(i);
                for (int j = i + 1; j < l.size(); j++) {
                    final CutPoint cP2 = (CutPoint) l.get(j);
                    if (cP.s1 == cP2.s1) {
                        if (!(cP.x <= cP2.x)) {
							Assert.fail();
						}
                    } else {
                        final ConstraintNode cN = PrimeConstraints
                                .getPrimeConstraint(
                                        pC.primeConstraints[cP2.s1][cP.s1],
                                        cP2.x);
                        int y = cN.x < cP2.x ? cN.y - cN.x + cP2.x
                                : cN.y;
                        if ((cN.maxOffset == Integer.MIN_VALUE)
                                || (cN.x > cP2.x)) {
							y--;
						}
                        if (cP.x > y) {
							Assert.fail(cP + " " + cN);
						}
                    }
                    if (cP.s2 == cP2.s2) {
                        if (!(cP.y <= cP2.y)) {
							Assert.fail();
						}
                    } else {
                        final ConstraintNode cN = PrimeConstraints
                                .getPrimeConstraint(
                                        pC.primeConstraints[cP2.s2][cP.s2],
                                        cP2.y);
                        int y = cN.x < cP2.y ? cN.y - cN.x + cP2.y
                                : cN.y;
                        if ((cN.maxOffset == Integer.MIN_VALUE)
                                || (cN.x > cP2.y)) {
							y--;
						}
                        if (cP.y > y) {
							Assert.fail();
						}
                    }
                }
            }
        }
    }

    public List<CutPoint> cutPoints(final int s1, final int s2, Chains.Node c) {
        final List<CutPoint> l = new LinkedList<CutPoint>();
        while (c != null) {
            if (Math.random() > 0.2) {
                l.add(new Chains.CutPoint(s1, s2, c.xS, c.n.xS, 0));
                if(Math.random() > 0.8) {
                    l.add(new Chains.CutPoint(s1, s2, c.xS, c.n.xS, 1));
                }
            }
            c = c.p;
        }
        return l;
    }

    public void testCheckConsistency2() {
        final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(3);
        pC.primeConstraints[0][1]
                .add(new PrimeConstraints.ConstraintNode(17, 21, 0));
        pC.primeConstraints[1][0]
                .add(new PrimeConstraints.ConstraintNode(20, 12, 0));
        pC.primeConstraints[0][2]
                .add(new PrimeConstraints.ConstraintNode(11, 3, 0));
        pC.primeConstraints[2][0]
                .add(new PrimeConstraints.ConstraintNode(17, 12, 0));
        pC.primeConstraints[1][2]
                .add(new PrimeConstraints.ConstraintNode(20, 3, 0));
        pC.primeConstraints[2][1]
                .add(new PrimeConstraints.ConstraintNode(18, 21,
                        Integer.MIN_VALUE));
        Assert.assertTrue(this.isConsistent(pC, 3, 300));
    }

    public void testCheckConsistency() {
        final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(3);
        pC.primeConstraints[0][1]
                .add(new PrimeConstraints.ConstraintNode(100, 100, 0));
        Assert.assertTrue(this.isConsistent(pC, 3, 300));
        pC.primeConstraints[1][2]
                .add(new PrimeConstraints.ConstraintNode(150, 150, 0));
        Assert.assertTrue(this.isConsistent(pC, 3, 300));
        pC.primeConstraints[2][0]
                .add(new PrimeConstraints.ConstraintNode(150, 100, 0));
        Assert.assertFalse(this.isConsistent(pC, 3, 300));
    }

    public boolean isConsistent(final PrimeConstraints pC, final int seqNo,
            final int seqLength) {
        final int[][][][] iAAAA = new int[seqNo][seqNo][seqLength][];
        final int[][][] iAAA = new int[seqNo][seqLength][2];
        final List<int[]> l = new LinkedList<int[]>();
        for (int i = 0; i < seqNo; i++) {
            for (int k = 0; k < seqLength; k++) {
                l.add(new int[] { i, k });
            }
            for (int k = 0; k < seqLength - 1; k++) {
                iAAAA[i][i][k] = new int[] { k + 1, Integer.MIN_VALUE };
            }
            for (int j = 0; j < seqNo; j++) {
                if (i != j) {
                    final SortedSet<ConstraintNode> sS = pC.primeConstraints[i][j];
                    for (int k = 0; k < seqLength; k++) {
                        final ConstraintNode cN = PrimeConstraints
                                .getPrimeConstraint(sS, k);
                        if (cN.x != Integer.MAX_VALUE) {
                            if ((cN.maxOffset == Integer.MIN_VALUE)
                                    || (cN.x > k)) {
                                iAAAA[i][j][k] = new int[] { cN.y,
                                        Integer.MIN_VALUE };
                            } else {
                                final int m = cN.y - cN.x + k;
                                iAAAA[i][j][k] = new int[] { m,
                                        Integer.MAX_VALUE };
                            }
                        }
                    }
                }
            }
        }
        // depth first
        int time = 1;
        while (l.size() != 0) {
            time = this.depthFirst(iAAAA, iAAA, seqNo, l,
                    new LinkedList<int[]>(), time);
        }
        final int[][][][] iAAAA2 = new int[seqNo][seqNo][seqLength][];
        final SortedMap<Integer, int[]> sM = new TreeMap<Integer, int[]>();
        for (int i = 0; i < seqNo; i++) {
            for (int k = 0; k < seqLength; k++) {
                sM
                        .put(new Integer(iAAA[i][k][1]), new int[] {
                                i, k });
            }
            for (int j = 0; j < seqNo; j++) {
                for (int k = 0; k < seqLength; k++) {
                    final int[] iA = iAAAA[i][j][k];
                    if (iA != null) {
						iAAAA2[j][i][iA[0]] = new int[] { k };
					}
                }
            }
        }
        l.clear();
        for (final Iterator<int[]> it = sM.values().iterator(); it.hasNext();) {
            l.add(0, it.next());
        }
        final int[][][] iAAA2 = new int[seqNo][seqLength][2];
        time = 1;
        while (l.size() != 0) {
            final List<int[]> l2 = new LinkedList<int[]>();
            time = this.depthFirst(iAAAA2, iAAA2, seqNo, l, l2, time);
            final int[][] iAA = new int[l2.size()][];
            {
                int i = 0;
                for (final Iterator<int[]> it = l2.iterator(); it.hasNext();) {
                    iAA[i++] = it.next();
                }
            }
            for (int i = 0; i < iAA.length; i++) {
                for (int j = 0; j < iAA.length; j++) {
                    if (i != j) {
                        final int[] iA = iAA[i];
                        final int[] iA2 = iAA[j];
                        final int[] iA3 = iAAAA[iA[0]][iA2[0]][iA[1]];
                        if ((iA3 != null)
                                && (((iA3[0] == iA2[1]) && (iA3[1] == Integer.MIN_VALUE)) || (iA3[0] < iA2[1]))) {
							return false;
						}
                    }
                }
            }
        }
        return true;
    }

    public final int depthFirst(final int[][][][] iAAAA, final int[][][] iAAA,
            final int seqNo, final List<int[]> l, final List<int[]> l2, int time) {
        int[] iA = l.remove(0);
        final int i = iA[0];
        final int j = iA[1];
        if (iAAA[i][j][0] != 0) {
			return time;
		}
        l2.add(iA);
        iAAA[i][j][0] = time++;
        for (int k = 0; k < seqNo; k++) {
            iA = iAAAA[i][k][j];
            if (iA != null) {
                final int m = iA[0];
                if (iAAA[k][m][0] == 0) {
                    l.add(0, new int[] { k, m });
                    time = this.depthFirst(iAAAA, iAAA, seqNo, l, l2, time);
                }
            }
        }
        iAAA[i][j][1] = time++;
        return time;
    }

    public void testUpdatingPairwiseConstraints_LessThan() {
        for (int trial = 0; trial < 50;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if (seqNo < 3) {
				// if(seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.6, 0.9, 100);
                    Chains.transformChains(cAA[i][j], 20, 20);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo);
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            int j = 0;
            while (j < 5) {
                // Debug.pl(" j " + j);
                final int[][] iAA = ChainsTest.getBox();
                final List l2 = (List) IterationTools.append(Iterators.map(
                        pC.primeConstraints[0][1].iterator(),
                        new Function() {
                            public Object fn(Object o) {
                                return ((PrimeConstraints.ConstraintNode) o)
                                        .clone();
                            }
                        }), new LinkedList());
                ChainsTest.fillInConstraints(iAA, l2);
                final int x = (int) (Math.random() * 250);
                final int y = (int) (Math.random() * 250);
                Node n1 = new Node(x, x, 0, null, null);
                final Node n2 = new Node(y, y, 0, n1, null);
                n1.n = n2;
                n1 = pC.filterByConstraints(0, 1, n1);
                if (n1 == null) {
					continue;
				}
                // n2 = pC.filterByConstraints(1, 0, n2);
                // if(n2 == null)
                // continue;
                final PrimeConstraints.ConstraintNode cN = new PrimeConstraints.ConstraintNode(
                        x, y, Integer.MIN_VALUE);
                pC.updatePrimeConstraints_LessThan(
                        pC.primeConstraints[0][1],
                        (PrimeConstraints.ConstraintNode) cN.clone());
                final List l = new LinkedList();
                l.add(cN);
                ChainsTest.fillInConstraints(iAA, l);
                final int[][] iAA2 = ChainsTest.getBox();
                ChainsTest.fillInConstraints(iAA2, (List) IterationTools.append(
                        pC.primeConstraints[0][1].iterator(),
                        new LinkedList()));
                ChainsTest.arraysEquals(iAA, iAA2);
                j++;
            }
        }
    }

    public void testUpdatingPairwiseConstraints_LessThanOrEqual() {
        for (int trial = 0; trial < 100;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if (seqNo != 3) {
				// if(seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.6, 0.9, 100);
                    Chains.transformChains(cAA[i][j], 20, 20);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo);
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            int j = 0;
            while (j++ < 20) {
                // Debug.pl(" j " + j);
                final int[][] iAA = ChainsTest.getBox();
                final List l2 = (List) IterationTools.append(Iterators.map(
                        pC.primeConstraints[0][1].iterator(),
                        new Function() {
                            public Object fn(Object o) {
                                return ((PrimeConstraints.ConstraintNode) o)
                                        .clone();
                            }
                        }), new LinkedList());
                ChainsTest.fillInConstraints(iAA, l2);
                final int x = (int) (Math.random() * 250);
                final int y = (int) (Math.random() * 250);
                final int length = (int) (Math.random() * 50);
                pC.updatePrimeConstraints(0, 1, x, y, length);
                final PrimeConstraints.ConstraintNode cN = new PrimeConstraints.ConstraintNode(
                        x, y, length);
                final List l = new LinkedList();
                l.add(cN);
                ChainsTest.fillInConstraints(iAA, l);
                final int[][] iAA2 = ChainsTest.getBox();
                ChainsTest.fillInConstraints(iAA2, (List) IterationTools.append(
                        pC.primeConstraints[0][1].iterator(),
                        new LinkedList()));
                ChainsTest.arraysEquals(iAA, iAA2);
            }
        }
    }

    public void testTransformConstraints() {
        for (int trial = 0; trial < 10;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if (seqNo < 3) {
				// if(seqNo != 3)
                continue;
			}
            trial++;
            Debug.pl(" Trial " + trial);
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());
            Chains.Node[][] cAA = new Chains.Node[dAA.length][dAA.length];
            for (int i = 0; i < cAA.length; i++) {
                for (int j = 0; j < cAA.length; j++) {
                    cAA[i][j] = ChainsTest.getRandomChain(0.6, 0.9, 100);
                    Chains.transformChains(cAA[i][j], 20, 20);
                    cAA[j][i] = Chains.sortOppositeChain(cAA[i][j]);
                }
            }
            final int[][] pairOrdering = PecanTools.getPairOrdering(dAA);
            Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    seqNo);
            cAA = Chains.makeConsistentChains(cAA, pairOrdering, pC,
                    dAA.length, true);
            final Chains.Node[][] cAA2 = new Chains.Node[seqNo][seqNo];
            final int diagGap = (int) (Math.random() * 10);
            Chains.transformConstraints(diagGap, pC);
            for (int i = 0; i < dAA.length; i++) {
                for (int j = i + 1; j < dAA.length; j++) {
                    cAA[i][j] = Chains.convertEdgeListToNodeChain(pC
                            .convertPrimeConstraintsToEdgeList(i, j,
                                    true));
                    cAA[j][i] = Chains.convertEdgeListToNodeChain(pC
                            .convertPrimeConstraintsToEdgeList(j, i,
                                    true));
                    cAA2[i][j] = Chains.convertEdgeListToNodeChain(pC
                            .convertPrimeConstraintsToEdgeList(i, j,
                                    false));
                    cAA2[j][i] = Chains.convertEdgeListToNodeChain(pC
                            .convertPrimeConstraintsToEdgeList(j, i,
                                    false));
                }
            }
            pC = new Chains.PrimeConstraints(seqNo);
            for (final int[] iA : pairOrdering) {
                final int s1 = iA[0];
                final int s2 = iA[1];
                pC.updatePrimeConstraints(s1, s2, cAA2[s1][s2]);
                Node hC1 = Chains.copyOfChain(cAA[s1][s2]);
                hC1 = pC.filterByConstraints(s1, s2, hC1);
                hC1 = Chains.sortOppositeChain(hC1);
                hC1 = pC.filterByConstraints(s2, s1, hC1);
                hC1 = Chains.sortOppositeChain(hC1);
                // Debug.pl(hC1 + " ");
                // Debug.pl(cAA[s1][s2] + " ");
                this.nodesEquals(hC1, cAA[s1][s2]);
                pC.updatePrimeConstraints(s1, s2, hC1);

                pC.updatePrimeConstraints(s2, s1, cAA2[s2][s1]);
                hC1 = Chains.copyOfChain(cAA[s2][s1]);
                hC1 = pC.filterByConstraints(s2, s1, hC1);
                hC1 = Chains.sortOppositeChain(hC1);
                hC1 = pC.filterByConstraints(s1, s2, hC1);
                hC1 = Chains.sortOppositeChain(hC1);
                // Debug.pl(hC1 + " ");
                // Debug.pl(cAA[s2][s1] + " ");
                this.nodesEquals(hC1, cAA[s2][s1]);
                pC.updatePrimeConstraints(s2, s1, hC1);
            }
            final Chains.Node[][] cAA3 = new Chains.Node[seqNo][seqNo];
            for (int i = 0; i < seqNo; i++) {
                for (int j = i + 1; j < seqNo; j++) {
                    cAA3[i][j] = Chains.convertEdgeListToNodeChain(pC
                            .convertPrimeConstraintsToEdgeList(i, j,
                                    true));
                    cAA3[j][i] = Chains.convertEdgeListToNodeChain(pC
                            .convertPrimeConstraintsToEdgeList(j, i,
                                    true));
                }
            }
            Assert.assertTrue(ChainsTest.checkConsistency(cAA3));
            Assert.assertTrue(ChainsTest.checkConsistency(cAA));
        }
    }

    public void nodesEquals(Node one, Node two) {
        while (true) {
            if (one == null) {
                Assert.assertEquals(two, null);
                return;
            }
            Assert.assertEquals(one.xS, two.xS);
            Assert.assertEquals(one.xE, two.xE);
            Assert.assertEquals(one.n.xS, two.n.xS);
            Assert.assertEquals(one.n.xE, two.n.xE);
            Assert.assertEquals(one.n.score, two.n.score);
            Assert.assertEquals(one.score, two.score);
            one = one.p;
            two = two.p;
        }
    }

    public static void checkMatrix(final int[][] matrix, final int upto,
            final int minGap, final int maxGap) {
        for (int i = 0; i <= upto; i++) {
            int k = 0;
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != 1) {
                    k++;
                }
            }
            if (k > maxGap) {
				Assert.fail(i + " " + k + " " + maxGap);
			}
            if (k + 1 < minGap) {
                Debug.pl(i + " " + k + " " + maxGap + " " + minGap);
                final int[] iA = new int[300 * 300];
                int yy = 0;
                for (final int[] element : matrix) {
                    for (int zz = 0; zz < element.length; zz++) {
                        if (element[zz] == 1) {
							element[zz] = ((255 << 24) | 255);
						}
                    }
                    System.arraycopy(element, 0, iA, yy,
                            element.length);
                    yy += element.length;
                }
                Graphics.displayArrayAsImage(iA, 300, 300);
                while (true) {
                    ;
                }
                // fail(i + " " + k + " " + minGap);
            }
            // Debug.pl(i + " " + k + " " + maxGap);
        }
    }

    public void fillInMatrix(final int[][] matrix, final Iterator it, final Iterator it2) {
        int s = 0;
        it.next();
        it2.next();
        for (final int[] element : matrix) {
            element[0] = 1;
            element[element.length - 1] = 1;
        }
        while (it.hasNext()) {
            final Chains.PrimeConstraints.ConstraintNode cN = (Chains.PrimeConstraints.ConstraintNode) it
                    .next();
            if (cN.x == Integer.MAX_VALUE) {
				break;
			}
            final int x = cN.x;
            int y = cN.y;
            for (int i = s; i <= x; i++) {
                final int[] iA = matrix[i];
                Arrays.fill(iA, y, iA.length, 1);
            }
            s = x + 1;
            if (cN.maxOffset != Integer.MIN_VALUE) {
                for (int i = s; i <= x + cN.maxOffset; i++) {
                    final int[] iA = matrix[i];
                    Arrays.fill(iA, ++y, iA.length, 1);
                }
                s += cN.maxOffset;
            }
        }
        final List l = (List) IterationTools.append(it2, new LinkedList());
        final ListIterator it3 = l.listIterator(l.size());
        it3.previous();
        s = matrix.length - 1;
        while (it3.hasPrevious()) {
            final Chains.PrimeConstraints.ConstraintNode cN = (Chains.PrimeConstraints.ConstraintNode) it3
                    .previous();
            if (cN.y < 0) {
				break;
			}
            final int x = cN.maxOffset != Integer.MIN_VALUE ? cN.y
                    + cN.maxOffset : cN.y;
            int y = cN.maxOffset != Integer.MIN_VALUE ? cN.x
                    + cN.maxOffset : cN.x;
            for (int i = s; i >= x; i--) {
                final int[] iA = matrix[i];
                for (int xx = 1; xx < y /* + 1 */; xx++) {
					if (iA[xx] == 1) {
                        throw new IllegalStateException(i + " " + xx);
                    }
				}
                Arrays.fill(iA, 0, y + 1, 1);
            }
            s = x - 1;
            if (cN.maxOffset != Integer.MIN_VALUE) {
                for (int i = s; i >= cN.y; i--) {
                    final int[] iA = matrix[i];
                    for (int xx = 1; xx < y - 1; xx++) {
						if (iA[xx] == 1) {
                            throw new IllegalStateException(i + " "
                                    + xx);
                        }
					}
                    Arrays.fill(iA, 0, y--, 1);
                }
                s -= cN.maxOffset;
            }
        }
    }

    public void testConvertPrimeConstraintsToEdgeList() {
        for (int trial = 0; trial < 1000; trial++) {
            final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                    10);
            final SortedSet<ConstraintNode> sS = pC.primeConstraints[0][1];
            final List<bp.pecan.PolygonFiller.Node> l = new LinkedList<bp.pecan.PolygonFiller.Node>();
            int x = 0;
            int y = 0;
            while (Math.random() > 0.1) {
                x += (int) (Math.random() * 100);
                y += (int) (Math.random() * 100);
                final int length = (int) (Math.random() * 10);
                final Chains.PrimeConstraints.ConstraintNode cN = new Chains.PrimeConstraints.ConstraintNode(
                        x, y, length);
                sS.add(cN);
                l.add(new PolygonFiller.Node(x, y, y + length, 1));
                x += length + 1;
                y += length + 1;
            }
            final Iterator<bp.pecan.PolygonFiller.Node> it = l.iterator();
            for (final Generator gen = pC
                    .convertPrimeConstraintsToEdgeList(0, 1, false); it
                    .hasNext();) {
                final PolygonFiller.Node n = it.next();
                final PolygonFiller.Node m = (PolygonFiller.Node) gen.gen();
                if (n.compareTo(m) != 0) {
					Assert.fail(n + " " + m);
				}
            }
        }
    }

    public void testMakeMultipleAlignment() {
        for (int trial = 0; trial < 1000; trial++) {
            final int sequenceNumber = 2 + (int) (Math.random() * 10);
            final byte[][] bAA = new byte[sequenceNumber][100];
            for (int i = 0; i < 100; i++) {
                int position = 0;
                for (int j = 0; j < sequenceNumber; j++) {
					if (Math.random() > 0.5) {
                        bAA[j][i] = (byte) (1 + Math.random() * 100);
                        position++;
                    } else {
						bAA[j][i] = 0;
					}
				}
            }
            final byte[][] bAA2 = new byte[sequenceNumber][];
            final int[] seqSizes = new int[sequenceNumber];
            for (int i = 0; i < sequenceNumber; i++) {
                int j = 0;
                for (int k = 0; k < bAA[i].length; k++) {
					if (bAA[i][k] != 0) {
						j++;
					}
				}
                seqSizes[i] = j;
                bAA2[i] = new byte[j];
                j = 0;
                for (int k = 0; k < bAA[i].length; k++) {
					if (bAA[i][k] != 0) {
						bAA2[i][j++] = bAA[i][k];
					}
				}
            }
            final Generator[][] gens = new Generator[sequenceNumber][sequenceNumber];
            for (int i = 0; i < sequenceNumber; i++) {
				for (int j = i + 1; j < sequenceNumber; j++) {
                    final byte[] bA = bAA[i];
                    final byte[] bA2 = bAA[j];
                    gens[i][j] = PolygonFiller
                            .flipEdgeXYDiagonalsCoordinates(new Generator() {
                                int i = 0;

                                int bAI = 0;

                                int bA2I = 0;

                                public Object gen() {
                                    while (this.i < bA.length) {
                                        if ((bA[this.i] != 0) && (bA2[this.i] != 0)) {
                                            final int j = this.i;
                                            this.i++;
                                            while ((this.i < bA.length)
                                                    && (bA[this.i] != 0)
                                                    && (bA2[this.i] != 0)) {
												this.i++;
											}
                                            final Object o = new PolygonFiller.Node(
                                                    this.bA2I,
                                                    this.bAI,
                                                    this.bAI + (this.i - j - 1),
                                                    1);
                                            this.bAI += this.i - j;
                                            this.bA2I += this.i - j;
                                            return o;
                                        }
                                        if (bA[this.i] != 0) {
											this.bAI++;
										}
                                        if (bA2[this.i] != 0) {
											this.bA2I++;
										}
                                        this.i++;
                                    }
                                    return null;
                                }
                            });
                }
			}
            final Procedure_Int[] pros = new Procedure_Int[sequenceNumber];
            final byte[][] bAA3 = new byte[sequenceNumber][100];
            for (int i = 0; i < sequenceNumber; i++) {
                final int seqIndex = i;
                pros[i] = new Procedure_Int() {
                    int j = 0;

                    public void pro(final int i) {
                        bAA3[seqIndex][this.j++] = (byte) i;
                    }
                };
            }
            Chains.makeMultipleAlignment(pros, gens, bAA2, seqSizes,
                    sequenceNumber, new int[10000], (byte) 0);
            final int[] positions = new int[sequenceNumber];
            for (int i = 0; i < positions.length; i++) {
				positions[i] = i * 100;
			}
            final int[] positions2 = positions.clone();
            final int[] sA = new int[sequenceNumber];
            final Map<Integer, List<Integer>> m = new HashMap<Integer, List<Integer>>();
            for (int i = 0; i < 100; i++) {
                int k = 0;
                for (int j = 0; j < sequenceNumber; j++) {
                    if (bAA[j][i] != 0) {
                        sA[k++] = j;
                    }
                }
                for (int j = 0; j < k; j++) {
                    final List<Integer> l = new LinkedList<Integer>();
                    for (int n = j + 1; n < k; n++) {
                        l.add(new Integer(positions[sA[n]]));
                    }
                    m.put(new Integer(positions[sA[j]]), l);
                    positions[sA[j]]++;
                }
            }
            for (int i = 0; i < 100; i++) {
                int k = 0;
                for (int j = 0; j < sequenceNumber; j++) {
                    if (bAA3[j][i] != 0) {
                        sA[k++] = j;
                    }
                }
                for (int j = 0; j < k; j++) {
                    final List l = m.get(new Integer(
                            positions2[sA[j]]));
                    for (int n = j + 1; n < k; n++) {
                        Assert.assertTrue(l.contains(new Integer(
                                positions2[sA[n]])));
                    }
                    Assert.assertEquals(l.size(), k - j - 1);
                    m.remove(new Integer(positions2[sA[j]]));
                    positions2[sA[j]]++;
                }
            }
            Assert.assertEquals(m.size(), 0);
        }
    }

    void checkStickiness(Chains.Node n) {
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        final Chains.Node m = n;
        while (n != null) {
            if ((n.xS <= x + 1) && (n.n.xS <= y + 1)) {
                throw new IllegalStateException(m + " ");
            }
            x = n.xE;
            y = n.n.xE;
            n = n.p;
        }
    }

    public static void checkChain(Chains.Node n) {
        while (n != null) {
            Assert.assertTrue(n.xE >= n.xS);
            Assert.assertTrue(n.n.xE >= n.n.xS);
            Assert.assertTrue(n.xE - n.xS == n.n.xE - n.n.xS);
            Assert.assertTrue(n.n.n == n);
            n = n.p;
        }
    }

    public void testMultiplyAndDivideScoresByLength() {
        for (int trial = 0; trial < 10; trial++) {
            final Chains.Node o = ChainsTest.getRandomChain(0.6, 0.9, 100);
            final int[][] iAA = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA, o, true);
            Chains.multiplyScoresByLengths(o);
            Chains.divideScoresByLengths(o);
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA2, o, true);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public void testGetHighestChain() {
        for (int trial = 0; trial < 1000; trial++) {
            final Chains.Node m = ChainsTest.getRandomCoLinearChain();
            {
                Chains.Node n = m;
                while (n != null) {
                    n.score = (int) (Math.random() * (Integer.MAX_VALUE - 100)) + 100;
                    n.n.score = n.score;
                    n = n.p;
                }
            }
            Chains.Node n = Chains.copyOfChain(m);

            while (Math.random() > 0.5) {
                final Chains.Node o = ChainsTest.getRandomChain(0.6, 0.9, 100);
                {
                    Chains.Node p = o;
                    while (p != null) {
                        p.score = -1;
                        p.n.score = p.score;
                        p = p.p;
                    }
                }
                n = Chains.merge(n, o);
            }
            n = Chains.getHighestChain(n);
            ChainsTest.checkChainIsColinear(n);
            /*
             * { Chains.Node p = n; while (p != null) { if(p.score == 0) p.score =
             * 1; p.n.score = p.score; p = p.p; } }
             */
            final int[][] iAA = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA, m, false);
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA2, n, false);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public static boolean checkConsistency(final Chains.Node[][] cAA) {
        // counter = 0
        // finished = 0
        // i = 0
        // while(true) {
        // j = 0;
        // source:
        // {
        // for each seq k != i && min(i, k) != null:
        // min(i, k) < min(i, minSeq(i)) or (min(i, k) == min(i, minSeq(i)) and
        // min(k, minSeq(k)) < min(k, i)): {
        // counter++
        // break;
        // }
        // (i, minSeq(i)) = nextPair(i, minSeq(i))
        // if minSeq(i) == null
        // finished++
        // if finished == seqNo:
        // return true
        // counter = 0
        // }
        // counter++;
        // if(counter >= seqNo)
        // return false
        // }

        int counter = 0;
        int finished = 0;
        final int[] minSeq = new int[cAA.length];
        for (int i = 0; i < minSeq.length; i++) {
			minSeq[i] = ChainsTest.getMin(cAA[i]);
		}
        while (true) {
            for (int i = 0; i < cAA.length; i++) {
                source: if (minSeq[i] != Integer.MAX_VALUE) {
                    for (int j = 0; j < cAA.length; j++) {
                        if ((i != j) && (minSeq[j] != Integer.MAX_VALUE)
                                && (cAA[j][i] != null)) {
                            if ((cAA[j][i].n.xS < cAA[i][minSeq[i]].xS)
                                    || ((cAA[j][i].n.xS == cAA[i][minSeq[i]].xS) && (cAA[j][minSeq[j]].xS < cAA[j][i].xS))) {
                                counter++;
                                break source;
                            }
                        }
                    }
                    cAA[i][minSeq[i]] = cAA[i][minSeq[i]].p;
                    minSeq[i] = ChainsTest.getMin(cAA[i]);
                    counter = 0;
                    if (minSeq[i] == Integer.MAX_VALUE) {
						finished++;
					}
                    if (finished == cAA.length) {
						return true;
					}
                } else {
					counter++;
				}
            }
            if (counter >= cAA.length) {
                for (int xx = 0; xx < cAA.length; xx++) {
					for (int yy = 0; yy < cAA[xx].length; yy++) {
						Debug.pl(xx + " " + yy + " " + minSeq[xx]
                                + " " + cAA[xx][yy] + " ");
					}
				}
                return false;
            }
        }
    }

    static int getMin(final Chains.Node[] cA) {
        int k = Integer.MAX_VALUE;
        int l = Integer.MAX_VALUE;
        for (int j = 0; j < cA.length; j++) {
            if ((cA[j] != null) && (k > cA[j].xS)) {
                k = cA[j].xS;
                l = j;
            }
        }
        return l;
    }

    public void testMakeCopyOfChain() {
        for (int trial = 0; trial < 1000; trial++) {
            Chains.Node m = ChainsTest.getRandomChain(0.6, 0.9, 100);
            ChainsTest.checkOrderingOfNet(m, false);
            final int[][] iAA = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA, m, false);
            final Chains.Node n = Chains.copyOfChain(m);
            while (m != null) {
                m.xS = (int) (Math.random() * 1000);
                m.xE = (int) (Math.random() * 1000);
                m.n.xS = (int) (Math.random() * 1000);
                m.n.xE = (int) (Math.random() * 1000);
                m = m.p;
            }
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA2, n, false);
            ChainsTest.checkOrderingOfNet(n, false);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public static void checkChainIsColinear(Chains.Node n) {
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        while (n != null) {
            if (n.xS <= x) {
				Debug.pl(" boo ");
			}
            Assert.assertTrue(n.xS > x);
            Assert.assertTrue(n.n.xS > y);
            Assert.assertTrue(n.xE >= n.xS);
            Assert.assertTrue(n.n.xE >= n.n.xS);
            Assert.assertTrue(n.xE - n.xS == n.n.xE - n.n.xS);
            Assert.assertTrue(n.n.n == n);
            x = n.xE;
            y = n.n.xE;
            n = n.p;
        }
    }

    static Chains.Node getRandomChain(final double rowProb,
            final double columnProb, final int rowNo) {
        // double rowProb,
        // double columnProb, int columnSize
        Chains.Node n = Chains
                .convertEdgeListToNodeChain(PolygonFiller
                        .flipEdgeXYDiagonalsCoordinates(Generators
                                .iteratorGenerator(PolygonFillerTest
                                        .convertOldEdgeListToNewEdgeList(PolygonFillerTest
                                                .makeRandomEdgeList(
                                                        Math.random()
                                                                * rowProb,
                                                        Math.random()
                                                                * columnProb,
                                                        rowNo)
                                                .iterator()))));
        n = ChainsTest.filterOutOverlaps(n);
        Chains.Node m = n;
        while (m != null) {
            m.score = (int) (Math.random() * 10) + 1;
            m.n.score = m.score;
            m = m.p;
        }
        return n;
    }

    static Chains.Node getRandomCoLinearChain() {
        return Chains
                .convertEdgeListToNodeChain(Generators
                        .iteratorGenerator(PolygonFillerTest
                                .convertOldEdgeListToNewEdgeList(PolygonFillerTest
                                        .clipUpperDiagonalEdgeList(PolygonFillerTest
                                                .makeRandomEdgeList(
                                                        Math.random() * 0.6,
                                                        Math.random() * 0.9,
                                                        100)
                                                .iterator()))));
    }

    static Chains.Node filterOutOverlaps(Chains.Node n) {
        final Set<Integer> s = new HashSet<Integer>();
        final Chains.Node p = new Chains.Node();
        Chains.Node q = new Chains.Node();
        p.p = n;
        q = p;
        while (n != null) {
            int x = n.xS;
            int y = n.n.xS;
            while (x <= n.xE) {
                final Integer i = new Integer((x << 16) + y);
                if (s.contains(i)) {
                    // delete n
                    // n = n.p;
                    q.p = n.p;
                    n = q;
                    break;
                }
                s.add(i);
                x++;
                y++;
            }
            q = n;
            n = n.p;
        }
        ChainsTest.checkOrderingOfNet(p.p, false);
        return p.p;
    }

    static int[][] getBox() {
        return new int[300][300];
    }

    static int[][] overlap(final Chains.Node m, final Chains.Node n) {
        final int[][] iAA = ChainsTest.getBox();
        ChainsTest.outLineChain(iAA, m, false);
        ChainsTest.outLineChain(iAA, n, true);
        return iAA;
    }

    static void outLineChain(final int[][] iAA, Chains.Node n,
            boolean canOverlap) {
        while (n != null) {
            int x = n.xS;
            int y = n.n.xS;
            while (x <= n.xE) {
                if (!canOverlap && (iAA[x][y] != 0)) {
					Assert.fail(x + " " + y);
				}
                iAA[x++][y++] += n.score;
            }
            n = n.p;
        }
    }

    static void outLineChain(final int[][] iAA, Chains.Node n, final int[] labels) {
        while (n != null) {
            int x = n.xS;
            int y = n.n.xS;
            while (x <= n.xE) {
                int i = 0;
                while ((iAA[x][y] & labels[i]) != 0) {
                    i++;
                }
                iAA[x++][y++] |= labels[i];
            }
            n = n.p;
        }
    }

    static void checkOrderingOfNet(Chains.Node m,
            final boolean canOverlapStartingPoints) {
        int pX = Integer.MIN_VALUE;
        int pY = Integer.MIN_VALUE;
        while (m != null) {
            Assert.assertEquals(m.xE - m.xS, m.n.xE - m.n.xS);
            Assert.assertTrue(m.xE >= m.xS);
            Assert.assertTrue(m.n.xE >= m.n.xS);
            Assert.assertEquals(m.score, m.n.score);
            Assert.assertEquals(m, m.n.n);
            Assert.assertFalse(pX > m.xS);
            if (canOverlapStartingPoints) {
				Assert.assertFalse(pY > m.n.xS);
			} else {
				Assert.assertFalse(pY >= m.n.xS);
			}
            pX = m.xS;
            if ((m.p == null) || (m.p.xS > m.xS)) {
				pY = Integer.MIN_VALUE;
			} else {
				pY = m.n.xS;
			}
            m = m.p;
        }
    }

    public void testMerge() {
        for (int trial = 0; trial < 1000; trial++) {
            final Chains.Node m = ChainsTest.getRandomChain(0.6, 0.9, 100);
            ChainsTest.checkOrderingOfNet(m, false);
            final Chains.Node n = ChainsTest.getRandomChain(0.6, 0.9, 100);
            ChainsTest.checkOrderingOfNet(n, false);
            final int[][] iAA = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA, m, false);
            ChainsTest.outLineChain(iAA, n, true);
            final Chains.Node p = Chains.merge(m, n);
            ChainsTest.checkOrderingOfNet(p, true);
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA2, p, true);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public static void arraysEquals(final int[][] iAA,
            final int[][] iAA2) {
        Assert.assertTrue(Array.arraysEqual().test(iAA, iAA2,
                new Predicate_2Args() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
                     *      java.lang.Object)
                     */
                    public boolean test(final Object o, final Object o2) {
                        boolean b = Arrays.equals((int[]) o,
                                (int[]) o2);
                        if (!b) {
                            final int[] iA = (int[]) o;
                            final int[] iA2 = (int[]) o2;
                            Assert.assertEquals(iA.length, iA2.length);
                            int j = 0;
                            for (int i = 0; i < iAA.length; i++) {
								if (iAA[i] == iA) {
									j = i;
								}
							}
                            for (int i = 0; i < iA.length; i++) {
                                if (iA[i] != iA2[i]) {
                                    Debug.pl(iA[i] + " " + iA2[i]
                                            + " " + i + " " + j);
                                }
                            }
                        }
                        return b;
                    }
                }));
    }

    public void testSortOppositeChain() {
        for (int trial = 0; trial < 1000; trial++) {
            Chains.Node m = ChainsTest.getRandomChain(0.6, 0.9, 100);
            ChainsTest.checkOrderingOfNet(m, false);
            boolean jumbled = Math.random() > 0.5;
            if (jumbled) {
                m = ChainsTest.jumble(m);
            }
            final Chains.Node n = Chains.sortOppositeChain(m);
            if (!jumbled) {
				ChainsTest.checkOrderingOfNet(m, false);
			}
            ChainsTest.checkOrderingOfNet(n, false);
            final int[][] iAA = ChainsTest.getBox();
            int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA, m, false);
            ChainsTest.outLineChain(iAA2, n, false);
            iAA2 = (int[][]) Array.transpose2DMatrix().fn(iAA2);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public static Chains.Node jumble(Chains.Node n) {
        final List<Node> l = new LinkedList<Node>();
        while (n != null) {
            l.add(n);
            n = n.p;
        }
        Collections.shuffle(l);
        final Chains.Node p = new Chains.Node();
        Chains.Node q = p;
        for (final Iterator<Node> it = l.iterator(); it.hasNext();) {
            final Chains.Node r = it.next();
            q.p = r;
            q = r;
        }
        q.p = null;
        return p.p;
    }

    public static int[][] consistencyChains(Chains.Node m,
            Chains.Node n) {
        final int[][] iAA = ChainsTest.getBox();
        final Map<Integer, Object> map = new HashMap<Integer, Object>();
        while (n != null) {
            int x = n.xS;
            int y = n.n.xS;
            while (x <= n.xE) {
                if (map.containsKey(new Integer(x))) {
                    map.put(new Integer(x), Functions_2Args
                            .concatenateIntArrays().fn(
                                    map.get(new Integer(x)),
                                    new int[] { y, n.score }));
                } else {
					map.put(new Integer(x), new int[] { y, n.score });
				}
                x++;
                y++;
            }
            n = n.p;
        }
        while (m != null) {
            int x = m.xS;
            int y = m.n.xS;
            while (x <= m.xE) {
                if (map.containsKey(new Integer(x))) {
                    final int[] iA = (int[]) map.get(new Integer(x));
                    for (int i = 0; i < iA.length; i += 2) {
						iAA[y][iA[i]] += (iA[i + 1] + m.score) / 2;
					}
                }
                x++;
                y++;
            }
            m = m.p;
        }
        return iAA;
    }

    public void testConvertEdgeListToNodeChainAndViceVersa() {
        for (int trial = 0; trial < 1000; trial++) {
            final List l = PolygonFillerTest
                    .convertOldEdgeListToNewEdgeList(PolygonFillerTest
                            .makeRandomEdgeList(Math.random() * 0.95,
                                    Math.random() * 0.95, 200));
            final int i = l.size();
            final Chains.Node m = Chains
                    .convertEdgeListToNodeChain(Generators
                            .iteratorGenerator(l.iterator()));
            Assert.assertEquals(l.size(), i);
            final List l2 = (List) IterationTools.append(
                    new GeneratorIterator(Chains
                            .convertNodeChainToEdgeList(m)),
                    new LinkedList());
            PolygonFillerTest.listsEquals(PolygonFillerTest
                    .convertNewEdgeListToOldEdgeList(l2.iterator()),
                    PolygonFillerTest
                            .convertNewEdgeListToOldEdgeList(l
                                    .iterator()), false);
        }
    }

    public void testConvertNodeChainToEdgeListAndViceVersa() {
        for (int trial = 0; trial < 1000; trial++) {
            final Chains.Node m = ChainsTest.getRandomChain(0.6, 0.9, 100);
            final int[][] iAA = ChainsTest.getBox();
            final int[][] iAA2 = ChainsTest.getBox();
            ChainsTest.outLineChain(iAA, m, true);
            final Chains.Node n = Chains.convertEdgeListToNodeChain(Chains
                    .convertNodeChainToEdgeList(m));
            ChainsTest.outLineChain(iAA2, n, true);
            ChainsTest.arraysEquals(iAA, iAA2);
        }
    }

    public static void checkChain(final List l) {
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        for (final Iterator it = l.iterator(); it.hasNext();) {
            final int[] iA = (int[]) it.next();
            Assert.assertFalse(iA[0] <= x);
            x = iA[0];
            Assert.assertFalse(iA[1] <= y);
            y = iA[1];
        }
    }

    public Chains.Aligner sillyAligner(final int sillyLevel) {
        return new Chains.Aligner() {
            public Iterator align(final byte[] bA, final byte[] bA2, final int startX,
                    final int lengthX, final int startY, final int lengthY) {
                Assert.assertFalse((startX >= bA.length) || (startX < 0)
                        || (lengthX < 0)
                        || (startX + lengthX > bA.length));
                final int i = lengthX, j = lengthY;
                final List l = new LinkedList();
                l.add(new PolygonFiller.Node(startX + i / 2, startY
                        + j / 2, startY + j / 2, sillyLevel));
                return l.iterator();
            }
        };
    }

    public static void isConsistent(final PrimeConstraints pC, final int i,
            final int j, final int x, final int y, final int length) {
        // int k = j;
        // j = i;
        // i = k;
        Node m = new Node(x, x + length, 0, null, null);
        Node n = new Node(y, y + length, 0, m, null);
        m.n = n;
        m = pC.filterByConstraints(i, j, m);
        if (m == null) {
            Assert.fail();
            Debug.pl(" is null ");
            return;
        }
        n = Chains.sortOppositeChain(m);
        if (n == null) {
			Assert.fail();
		}
        n = pC.filterByConstraints(j, i, n);
        if (n == null) {
            Assert.fail();
            Debug.pl(" is null ");
        }
        m = Chains.sortOppositeChain(n);
        if ((m.xS != x) || (m.xE != x + length) || (m.n.xS != y)
                || (m.n.xE != y + length)) {
            Assert.fail();
            Debug.pl(" changed " + m);
        }
    }

    public static void removeOnePlaceTransitiveChains(
            final Chains.PrimeConstraints pC,
            final int maxWindowLength, final int[] ends) {
        // p denotes sequence position
        // for i in sequences
        // for j in sequences
        // for k primeConstraint in ordered list i.p < j.p || i.p <= j.p
        // for l in sequences
        // get m primeConstraint i.p < l.p < j.p etc
        // if k.j.p + maxWindowLength < m.j.p
        // place constraint i <= first position in k within k.j.p +
        // maxWindowLength
        for (int i = 0; i < pC.sequenceNumber; i++) {
            for (int j = 0; j < pC.sequenceNumber; j++) {
                if (i != j) {
                    final SortedSet sS = pC.primeConstraints[i][j];
                    final Iterator it = sS.iterator();
                    it.next();
                    while (it.hasNext()) {
                        Chains.PrimeConstraints.ConstraintNode.compareX = true;
                        final Chains.PrimeConstraints.ConstraintNode cN = (ConstraintNode) it
                                .next();
                        if (cN.x == Integer.MAX_VALUE) {
							continue;
						}
                        for (int k = 0; k < pC.sequenceNumber; k++) {
                            if ((k != i) && (k != j)) {
                                int x = cN.x;
                                int y = cN.y;
                                final int xMax = cN.maxOffset != Integer.MIN_VALUE ? cN.x
                                        + cN.maxOffset
                                        : cN.x;
                                while (x <= xMax) {
                                    final ConstraintNode cN2 = Chains.PrimeConstraints
                                            .getPrimeConstraint(
                                                    pC.primeConstraints[i][k],
                                                    x);
                                    int y2;
                                    if ((cN2.maxOffset == Integer.MIN_VALUE)
                                            || (cN2.x > x)) {
                                        if (cN2.x == Integer.MAX_VALUE) {
                                            y2 = ends[k] - 1;
                                        } else {
                                            y2 = cN2.y - 1;
                                        }
                                    } else {
                                        y2 = cN2.y - cN2.x + x;
                                    }
                                    final ConstraintNode cN3 = Chains.PrimeConstraints
                                            .getPrimeConstraint(
                                                    pC.primeConstraints[k][j],
                                                    y2);
                                    // if (cN3.y == Integer.MAX_VALUE)
                                    // break;
                                    int y3;
                                    if (cN3.y == Integer.MAX_VALUE) {
										y3 = ends[j];
									} else {
										y3 = cN3.y;
									}
                                    if (y + maxWindowLength < y3) {
                                        // cN2 = Chains.PrimeConstraints
                                        // .getRightMostPointAffectedByConstraint(
                                        // pC.primeConstraints[k][j],
                                        // cN.y
                                        // + maxWindowLength);
                                        // if (cN.maxOffset > 5)
                                        // Debug.pl("tran " + y + " "
                                        Assert.fail("tran " + y + " "
                                                + maxWindowLength
                                                + " " + y3 + " i "
                                                + i + " j " + j
                                                + " k " + k + " x "
                                                + cN.x + " y " + cN.y
                                                + " z "
                                                + cN.maxOffset);
                                        // Debug.pl(" ohh no " + i + " " + k + "
                                        // " +
                                        // cN.x + " " + (cN2.x-1) + " ");
                                        // pC.updatePrimeConstraints(i, k,
                                        // cN.x, cN2.x - 1, 0);
                                    }
                                    x++;
                                    y++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void checkConsistency(final SortedSet sS) {
        ConstraintNode.compareX = false;
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        for (final Iterator it = sS.iterator(); it.hasNext();) {
            final ConstraintNode cN2 = (ConstraintNode) it.next();
            if (cN2.x <= x) {
				throw new IllegalStateException(sS + " ");
			}
            if (cN2.y <= y) {
				throw new IllegalStateException(sS + " ");
			}
            if (cN2.maxOffset != Integer.MIN_VALUE) {
                x = cN2.maxOffset + cN2.x;
                y = cN2.maxOffset + cN2.y;
            } else {
                x = cN2.x;
                y = cN2.y;
            }
        }
    }

    public static void filterNodesByConstraints(final int[][] iAA,
            final int[][] iAA_C, Chains.Node m) {
        while (m != null) {
            source: {
                int x = m.xS;
                int y = m.n.xS;
                while (x <= m.xE) {
                    if (iAA_C[x][y] == 0) {
                        iAA[x][y] = m.score;
                        // break source;
                    }
                    // iAA[x][y] = m.score;
                    x++;
                    y++;
                }
                /*
                 * int x = m.xS; int y = m.n.xS; while (x <= m.xE) { iAA[x][y] =
                 * m.score; x++; y++; }
                 */
            }
            m = m.p;
        }
    }

    public static void fillInConstraints(final int[][] iAA, final List l) {
        for (final Iterator it = l.iterator(); it.hasNext();) {
            final Chains.PrimeConstraints.ConstraintNode cN = (Chains.PrimeConstraints.ConstraintNode) it
                    .next();
            final int end = cN.maxOffset == Integer.MIN_VALUE ? cN.x + 1
                    : cN.x;
            for (int i = 0; i < end; i++) {
                if (iAA[i][cN.y] == 1) {
					continue;
				}
                for (int j = cN.y; j < iAA[i].length; j++) {
                    iAA[i][j] = 1;
                }
            }
            if (cN.maxOffset != Integer.MIN_VALUE) {
                int y = cN.y + 1;
                for (int i = cN.x; i <= cN.x + cN.maxOffset; i++) {
                    if (iAA[i][y] == 1) {
                        y++;
                        continue;
                    }
                    for (int j = y; j < iAA[i].length; j++) {
                        iAA[i][j] = 1;
                    }
                    y++;
                }
            }
        }
    }
}