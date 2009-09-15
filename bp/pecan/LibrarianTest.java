/*
 * Created on Oct 1, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.Array;
import bp.common.ds.LockedObject;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Procedure_Int_5Args;
import bp.common.io.Debug;
import bp.common.io.NewickTreeParser;
import bp.common.io.NewickTreeParserTest;

/**
 * @author benedictpaten
 */
public class LibrarianTest
                          extends TestCase {

    public void testCreateRelationships() {
        PairsHeap.initialise(12 + (int)(Math.random()*20), 1.5f, true);
        final PairsHeap[] lib = PairsHeap.getPair(0, 0,
                1000);
        final PairsHeap sQ = lib[0];
        // Librarian.PairsHeap.
        // Librarian.PairsHeap sQ = new Librarian.PairsHeap(0, 1000, 1);
        // ScrollingQueue sQ = new ScrollingQueue(1000,0);
        for (int i = 0; i < 1000; i++) {
            sQ.add(i, i, 1);
            // sQ.add(Librarian.makePosition(2));
            // Librarian.addToLibrary(i, 1, 1, (int[][]) sQ.get(i),
            // Maths.sum());
        }
        final int[][][] iAAA = Librarian.iAAAMake(2);
        final Librarian.TransitiveDependencies tD = new Librarian.TransitiveDependencies(
                2, iAAA, Librarian
                        .completedOffsets(2, iAAA, /*iA,*/ null));
        final int[] iA2 = Librarian.createRelationships(tD, sQ, 0, 999, 0,
                1, 0, 0, 400);
        Assert.assertTrue(Arrays.equals(iA2, new int[] { 999, 1600 }));
    }

    public void testTransitiveDependencies() {
        for (int trial = 0; trial < 1000;) {
            final int sequenceNumber = 2 + (int) (Math.random() * 20);
            final int[][] previousWidths = new int[sequenceNumber][sequenceNumber];
            final int[][][] iAAA = new int[sequenceNumber][sequenceNumber][];
            for (int i = 0; i < sequenceNumber; i++) {
                for (int j = 0; j < sequenceNumber; j++) {
                    iAAA[i][j] = new int[sequenceNumber];
                    Arrays.fill(iAAA[i][j], Integer.MIN_VALUE);
                    iAAA[i][j][i] = Integer.MAX_VALUE;
                }
                iAAA[i][i] = null;
            }
            final int[][][] iAAA5 = Librarian
                    .iAAAMake(sequenceNumber);
            final Librarian.TransitiveDependencies tD = new Librarian.TransitiveDependencies(
                    sequenceNumber, iAAA5, new Procedure_Int_5Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Procedure_Int_4Args#pro(int, int,
                         *      int, int)
                         */
                        public void pro(int i, int j, int k, int l,
                                int m) {
                            Assert.assertEquals(iAAA5[i][j][k], m);
                            Assert.assertEquals(iAAA[i][j][k], l);
                            iAAA[i][j][k] = m;
                        }
                    });
            final int[][] iAA = new int[sequenceNumber][sequenceNumber];
            for (final int[] element : iAA) {
				Arrays.fill(element, Integer.MIN_VALUE);
			}
            final List<int[]> stackOfRelationships = new LinkedList<int[]>();
            int seq1 = (int) (Math.random() * sequenceNumber);
            int seq2 = (int) (Math.random() * sequenceNumber);
            for (int i = 0; i < 100; i++) {
                if (seq1 == seq2) {
					continue;
				}
                if (iAA[seq1][seq2] == Integer.MIN_VALUE) {
					iAA[seq1][seq2] = 0;
				}
                if (iAA[seq2][seq1] == Integer.MIN_VALUE) {
					iAA[seq2][seq1] = 0;
				}
                final int i6 = iAA[seq1][seq2]
                        + (int) (Math.random() * 100);
                final int i5 = iAA[seq2][seq1]
                        + (int) (Math.random() * 100);
                final int pRandomWidth = previousWidths[seq1][seq2];
                int randomWidth = (int) (Math.random() * 20);
                if ((i6 + randomWidth < iAA[seq1][seq2] + pRandomWidth)
                        || (i5 + randomWidth < iAA[seq2][seq1]
                                + pRandomWidth)) {
					randomWidth = pRandomWidth;
				}
                final int i4 = i6 + randomWidth;
                final int i7 = i5 + randomWidth;
                previousWidths[seq1][seq2] = randomWidth;
                previousWidths[seq2][seq1] = randomWidth;
                iAA[seq1][seq2] = i6;
                iAA[seq2][seq1] = i5;
                stackOfRelationships.add(new int[] { seq1, seq2, i6,
                        i7 });
                stackOfRelationships.add(new int[] { seq2, seq1, i5,
                        i4 });
                tD.insertRelationship(seq1, seq2, i4, i5);
                tD.insertRelationship(seq2, seq1, i7, i6);
                if (Math.random() < 0.5) {
					continue;
				}
                tD.updateTransitiveDependencies(seq1, seq2, i6);
                tD.updateTransitiveDependencies(seq2, seq1, i5);
                final int[][][] iAAA2 = this.computeOffsets(iAA,
                        stackOfRelationships);
                final int[][][] iAAA3 = tD.getSequenceOffsets();
                Assert.assertTrue(Array.arraysEqual().test(iAAA2, iAAA3,
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
									Debug.pl(" ahhh ");
								}
                                return b;
                            }
                        }));
                Assert.assertTrue(Array.arraysEqual().test(iAAA, iAAA3,
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
									Debug.pl(" ahhh ");
								}
                                return b;
                            }
                        }));
                seq1 = (int) (Math.random() * sequenceNumber);
                seq2 = (int) (Math.random() * sequenceNumber);
            }
            trial++;
        }
    }

    int[][][] computeOffsets(final int[][] iAA, final List<int[]> l) {
        final int[][] iAA2 = new int[iAA.length][iAA.length];
        for (final int[] element : iAA2) {
			Arrays.fill(element, Integer.MAX_VALUE);
		}
        final int[][][] iAAA2 = new int[iAA.length][iAA.length][iAA.length];
        for (int i = 0; i < iAAA2.length; i++) {
            for (int j = 0; j < iAAA2[i].length; j++) {
                Arrays.fill(iAAA2[i][j], Integer.MIN_VALUE);
                iAAA2[i][j][i] = Integer.MAX_VALUE;
                iAAA2[i][j][j] = iAA[i][j];
            }
        }
        for (final Iterator<int[]> it = l.iterator(); it.hasNext();) {
            final int[] iA = it.next();
            final int seq1 = iA[0], seq2 = iA[1], i1 = iA[2], i2 = iA[3];
            for (int i = 0; i < iAA.length; i++) {
                if ((iAA[seq2][i] >= i2) && (i1 > iAAA2[seq1][i][seq2])) {
					iAAA2[seq1][i][seq2] = i1;
				}
            }
        }
        for (int i = 0; i < iAAA2.length; i++) {
            iAAA2[i][i] = null;
        }
        return iAAA2;
    }

    int[][] computeOffsets(final int[][] iAA, final List l, final int[][][] iAAA) {
        final int[][] iAA2 = new int[iAA.length][iAA.length];
        for (final int[] element : iAA2) {
			Arrays.fill(element, Integer.MAX_VALUE);
		}
        final int[][][] iAAA2 = new int[iAA.length][iAA.length][iAA.length];
        for (final int[][] element : iAAA2) {
            for (int[] element0 : element) {
				Arrays.fill(element0, Integer.MIN_VALUE);
			}
        }
        for (final Iterator it = l.iterator(); it.hasNext();) {
            final int[] iA = (int[]) it.next();
            final int seq1 = iA[0], seq2 = iA[1], i1 = iA[2], i2 = iA[3];
            for (int i = 0; i < iAA.length; i++) {
                if ((iAA[seq2][i] >= i2) && (i1 > iAAA2[seq1][i][seq2])) {
					iAAA2[seq1][i][seq2] = i1;
				}
            }
        }
        for (int i = 0; i < iAA.length; i++) {
            for (int j = i + 1; j < iAA.length; j++) {
                final int[] iA = iAAA[i][j], iA2 = iAAA2[i][j], iA3 = iAAA2[j][i];
                int k = Integer.MAX_VALUE;
                for (final int element : iA) {
					if (iA2[element] < k) {
						k = iA2[element];
					}
				}
                iAA2[i][j] = k;
                k = Integer.MAX_VALUE;
                for (final int element : iA) {
					if (iA3[element] < k) {
						k = iA3[element];
					}
				}
                iAA2[j][i] = k;
            }
        }
        return iAA2;
    }

    public void testDependencyChain() {
        for (int trial = 0; trial < 1000;) {
            final NewickTreeParser.Node n = NewickTreeParserTest
                    .getRandomTreeRoot();
            final int seqNo = LibrarianTest.subTreeSize(n);
            if (seqNo < 3) {
				continue;
			}
            final double[][] dAA = PecanTools.getDistances(n, seqNo,
                    Functions_2Args.sum());// , Double.MIN_VALUE);
            final int[][][] iAAA = PecanTools.getOutgroups(
                    new LockedObject(new int[1000]), dAA, seqNo);
            double max = Double.MIN_VALUE;
            int i = 0, j = 0;
            for (int k = 0; k < dAA.length; k++) {
                for (int l = k + 1; l < dAA[k].length; l++) {
                    if (dAA[k][l] >= max) {
                        max = dAA[k][l];
                        i = k;
                        j = l;
                    }
                }
            }
            final int chainSize = LibrarianTest.maxChain(iAAA, i, j);
            Debug.pl(chainSize + " " + seqNo);
            if (chainSize != seqNo - 1) {
				Debug.pl(" ahhh ");
			}
            Assert.assertTrue(chainSize == seqNo - 1);
            trial++;
        }
    }

    public static int maxChain(final int[][][] iAAA, final int i, final int j) {
        final int[] iA = iAAA[i][j];
        int maxChain = 0;
        for (final int element : iA) {
            int l = i < element ? LibrarianTest.maxChain(iAAA, i, element) : LibrarianTest.maxChain(
                    iAAA, element, i);
            if (l > maxChain) {
				maxChain = l;
			}
            l = j < element ? LibrarianTest.maxChain(iAAA, j, element) : LibrarianTest.maxChain(iAAA,
                    element, j);
            if (l > maxChain) {
				maxChain = l;
			}
        }
        return maxChain + 1;
    }

    NewickTreeParser.Node getRandomTree() {
        final NewickTreeParser.Node n = new NewickTreeParser.Node();
        n.edgeLength = (int) (Math.random() * 100);
        while (Math.random() > 0.6) {
			n.addNode(this.getRandomTree());
		}
        return n;
    }

    int countNodes(final NewickTreeParser.Node tree) {
        if (tree.getNodes().size() == 0) {
			return 1;
		}
        int i = 0;
        for (final Iterator<Object> it = tree.getNodes().iterator(); it.hasNext();) {
            i += this.countNodes((NewickTreeParser.Node) it.next());
        }
        return i;
    }

    public static int subTreeSize(final NewickTreeParser.Node n) {
        if (n.getNodes().size() == 0) {
			return 1;
		}
        int i = 0;
        for (final Iterator<Object> it = n.getNodes().iterator(); it.hasNext();) {
			i += LibrarianTest.subTreeSize((NewickTreeParser.Node) it.next());
		}
        return i;
    }
}