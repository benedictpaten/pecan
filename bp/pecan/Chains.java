/*
 * Created on Nov 10, 2005
 */
package bp.pecan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import bp.common.ds.Array;
import bp.common.fp.Function_2Args;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Functions_Int_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.GeneratorTools;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_Int;
import bp.common.io.CigarParser_Generator;
import bp.common.io.Debug;
import bp.common.io.ExternalExecution;
import bp.common.io.FastaOutput_Procedure_Int;
import bp.common.io.MultiFastaParser_Generator;
import bp.common.maths.Maths;
import bp.pecan.Chains.PrimeConstraints.ConstraintNode;

/**
 * @author benedictpaten
 */
public final class Chains {

    static final Logger logger = Logger.getLogger(Chains.class
            .getName());

    public static Iterator filterCutPoints(final List cutPoints,
            final List periods, final int diagonalGap) {
        final int[] iA = new int[cutPoints.size()];
        {
            int j = 0;
            int p = Integer.MIN_VALUE;
            for (final Iterator it = cutPoints.iterator(); it.hasNext();) {
                final CutPoint cP = (CutPoint) it.next();
                final int k = cP.x + cP.y;
                iA[j++] = k;
                if (Debug.DEBUGCODE) {
                    if (k <= p) {
						throw new IllegalStateException(p + " " + k);
					}
                    p = k;
                }
            }
        }
        final Set<Integer> s = new HashSet<Integer>();
        for (final Iterator it = periods.iterator(); it.hasNext();) {
            final int[] iA2 = (int[]) it.next();
            final int j = iA2[0] + iA2[1];
            final int k = iA2[2] + iA2[3];
            if (Debug.DEBUGCODE && (k < j)) {
				throw new IllegalStateException(k + " " + j);
			}
            int l = Array.binarySearch(j, iA);
            while ((l < iA.length) && (iA[l] - diagonalGap <= k)) {
                s.add(new Integer(iA[l++]));
            }
        }
        return Iterators.filter(cutPoints.iterator(),
                new Predicate() {
                    public boolean test(final Object o) {
                        final CutPoint cP = (CutPoint) o;
                        return !s.contains(new Integer(cP.x + cP.y));
                    }
                });
    }

    public static class CutPoint {

        public CutPoint(final int s1, final int s2, final int x, final int y, final int tB) {
            this.s1 = s1;
            this.s2 = s2;
            this.x = x;
            this.y = y;
            this.tB = tB;
        }

        int x;

        int y;

        int s1;

        int s2;

        int tB;

        @Override
		public String toString() {
            return " ( " + this.x + " " + this.y + " " + this.s1 + " " + this.s2 + " "
                    + this.tB + " ) ";
        }
    }

    public static final class CutPointReordering {
        private static final class CPO {
            long i;

            long j;

            CutPoint cP;
        }

        public static final List[][] splitCutPointsBySequence(final List l,
                final int sequenceNumber, final int startingPoint) {
            final List[][] lAA = new List[sequenceNumber][sequenceNumber];
            for (int i = 0; i < sequenceNumber; i++) {
                for (int j = i + 1; j < sequenceNumber; j++) {
                    lAA[i][j] = new LinkedList();
                    lAA[j][i] = lAA[i][j];
                }
            }
            long index = 0;
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final CutPoint cP = (CutPoint) it.next();
                final List<CPO> l2 = lAA[cP.s1][cP.s2];
                int k;
                if (l2.size() == 0) {
					k = startingPoint;
				} else {
                    final CutPoint cP2 = l2.get(l2.size() - 1).cP;
                    k = cP2.x + cP2.y;
                }
                final int m = cP.x + cP.y - k;
                if (Debug.DEBUGCODE && (m < 0)) {
					throw new IllegalStateException(cP
                            + " "
                            + m
                            + " "
                            + (l2.size() != 0 ? l2.get(l2
                                    .size() - 1).cP : null) + " "
                            + k);
				}
                index += m;
                final CPO cPO = new CPO();
                cPO.cP = cP;
                cPO.i = index;
                cPO.j = index;
                l2.add(cPO);
            }
            return lAA;
        }

        public static final List zipCutPointsBySequences(
                final List[][] lAA, final int sequenceNumber) {
            final List l = new LinkedList();
            for (int i = 0; i < sequenceNumber; i++) {
                for (int j = i + 1; j < sequenceNumber; j++) {
                    l.addAll(lAA[i][j]);
                }
            }
            Collections.sort(l, new Comparator() {
                public int compare(final Object arg0, final Object arg1) {
                    final CPO cPO = (CPO) arg0;
                    final CPO cPO2 = (CPO) arg1;
                    // stable sort
                    return cPO.j < cPO2.j ? -1 : cPO.j > cPO2.j ? 1
                            : cPO.i < cPO2.i ? -1
                                    : cPO.i > cPO2.i ? 1 : 0;
                }
            });
            for (final ListIterator it = l.listIterator(); it.hasNext();) {
                final CPO cPO = (CPO) it.next();
                it.set(cPO.cP);
            }
            return l;
        }

        public static final void reorder(final List points, final List outGroup,
                final int maxIndexShift) {
            final Iterator it = points.iterator();
            final Iterator it2 = outGroup.iterator();
            CPO cPO2;
            if (it2.hasNext()) {
				cPO2 = (CPO) it2.next();
			} else {
				return;
			}
            while (it.hasNext()) {
                final CPO cPO = (CPO) it.next();
                while (cPO2.i - maxIndexShift <= cPO.i) {
                    if (cPO2.j >= cPO.j) {
						cPO2.j = cPO.j - 1;
					}
                    if (it2.hasNext()) {
						cPO2 = (CPO) it2.next();
					} else {
						return;
					}
                }
            }
        }

        public static final List reorder(final List cutPoints,
                final int sequenceNumber, final int[][][] outGroups,
                final int[][] pairOrdering, final int maxIndexShift,
                final int startingPoint) {
            final List[][] lAA = CutPointReordering.splitCutPointsBySequence(cutPoints,
                    sequenceNumber, startingPoint);
            for (int i = pairOrdering.length - 1; i >= 0; i--) {
                final int[] iA = pairOrdering[i];
                final int j = iA[0];
                final int k = iA[1];
                final int[] iA2 = outGroups[j][k];
                for (final int element : iA2) {
                    CutPointReordering.reorder(lAA[j][k], lAA[j][element], maxIndexShift);
                    CutPointReordering.reorder(lAA[j][k], lAA[k][element], maxIndexShift);
                }
            }
            return CutPointReordering.zipCutPointsBySequences(lAA, sequenceNumber);
        }
    }

    public static final class CutPointOrdering {

        private static final class CPO {
            int x;

            int y;

            CutPoint cP;
        }

        static final int[][] getSequencePositions(final int sequenceNumber,
                final List[][] points) {
            final int[][] iAA = new int[sequenceNumber][];
            for (int i = 0; i < sequenceNumber; i++) {
                int total = 0;
                for (int j = 0; j < i; j++) {
                    total += points[j][i].size();
                }
                for (int j = i + 1; j < sequenceNumber; j++) {
                    total += points[i][j].size();
                }
                final int[] iA = new int[total];
                int k = 0;
                for (int j = 0; j < i; j++) {
                    final List l = points[j][i];
                    for (final Iterator it = l.iterator(); it.hasNext();) {
                        final CutPoint cP = (CutPoint) it.next();
                        iA[k++] = cP.y;
                    }
                }
                for (int j = i + 1; j < sequenceNumber; j++) {
                    final List l = points[i][j];
                    for (final Iterator it = l.iterator(); it.hasNext();) {
                        final CutPoint cP = (CutPoint) it.next();
                        iA[k++] = cP.x;
                    }
                }
                Arrays.sort(iA);
                k = Array.uniq(iA, iA);
                final int[] iA2 = new int[k];
                System.arraycopy(iA, 0, iA2, 0, k);
                iAA[i] = iA2;
            }
            return iAA;
        }

        public static final List orderCutPoints(final int sequenceNumber,
                final int[][] iAA, final int[][] iAA2, final List[][] points) {
            int total = 0;
            for (int i = 0; i < points.length; i++) {
				for (int j = i + 1; j < points.length; j++) {
					total += points[i][j].size();
				}
			}
            final CPO[] cPOA = new CPO[total];
            int k = 0;
            for (int i = 0; i < sequenceNumber; i++) {
                for (int j = i + 1; j < sequenceNumber; j++) {
                    final List l = points[i][j];
                    for (final Iterator it = l.iterator(); it.hasNext();) {
                        final CPO cPO = new CPO();
                        final CutPoint cP = (CutPoint) it.next();
                        cPO.cP = cP;
                        final int m = iAA2[i][Arrays.binarySearch(iAA[i],
                                cP.x)];
                        final int n = iAA2[j][Arrays.binarySearch(iAA[j],
                                cP.y)];
                        if (m < n) {
                            cPO.x = n;
                            cPO.y = m;
                        } else {
                            cPO.x = m;
                            cPO.y = n;
                        }
                        cPOA[k++] = cPO;
                    }
                }
            }
            Arrays.sort(cPOA, new Comparator() {
                public int compare(final Object arg0, final Object arg1) {
                    final CPO cPO = (CPO) arg0;
                    final CPO cPO2 = (CPO) arg1;
                    return cPO.x > cPO2.x ? -1 : cPO.x < cPO2.x ? 1
                            : cPO.y > cPO2.y ? -1
                                    : cPO.y < cPO2.y ? 1 : 0;
                }
            });
            {
                final List l = new LinkedList();
                for (final CPO element : cPOA) {
                    // Debug.pl(" cPO " + cPOA[i].x + " " + cPOA[i].y + " " +
                    // cPOA[i].cP + " ");
                    l.add(element.cP);
                }
                return l;
            }
        }

        public static final List orderCutPoints(final PrimeConstraints pC,
                final List[][] points) {
            // for each sequence order cut points
            final int[][] iAA = CutPointOrdering.getSequencePositions(pC.sequenceNumber,
                    points);
            final int[][] iAA2 = CutPointOrdering.dfs(pC, iAA);
            return CutPointOrdering.orderCutPoints(pC.sequenceNumber, iAA, iAA2,
                    points);
        }

        public static int[][] dfs(final PrimeConstraints pC, final int[][] iAA) {
            final int[][] iAA2 = new int[pC.sequenceNumber][];
            final boolean[][] bAA = new boolean[pC.sequenceNumber][];
            int[] stack;
            int sP = 0;
            {
                int i = 0;
                for (int j = 0; j < pC.sequenceNumber; j++) {
                    final int[] iA = iAA[j];
                    iAA2[j] = new int[iA.length];
                    bAA[j] = new boolean[iA.length];
                    i += iA.length;
                }
                stack = new int[i * 3];
            }
            int t = 1;
            for (int i = 0; i < pC.sequenceNumber; i++) {
                final int[] iA = iAA[i];
                for (int j = 0; j < iA.length; j++) {
                    if (iAA2[i][j] == 0) {
                        int k = (stack[0] = i);
                        int l = (stack[1] = j);
                        int m = (stack[2] = 0);
                        bAA[k][l] = true;
                        sP = 3;
                        source: while (true) {
                            final int n = iAA[k][l];
                            for (int o = m; o < pC.sequenceNumber; o++) {
                                int p;
                                if (o != k) {
                                    final ConstraintNode cN = PrimeConstraints
                                            .getPrimeConstraint(
                                                    pC.primeConstraints[k][o],
                                                    n);
                                    p = cN.maxOffset == Integer.MIN_VALUE ? cN.y
                                            : (cN.x <= n ? (cN.y
                                                    - cN.x + n + 1)
                                                    : cN.y);
                                } else {
                                    p = n + 1;
                                }
                                p = Arrays.binarySearch(iAA[o], p);
                                if (p < 0) {
									p = Math.abs(p + 1);
								}
                                if ((p < iAA[o].length) && !bAA[o][p]) {
                                    stack[sP - 1] = o + 1;
                                    k = (stack[sP++] = o);
                                    l = (stack[sP++] = p);
                                    m = (stack[sP++] = 0);
                                    bAA[k][l] = true;
                                    continue source;
                                }
                            }
                            iAA2[k][l] = t++;
                            sP -= 3;
                            if (sP == 0) {
								break;
							}
                            k = stack[sP - 3];
                            l = stack[sP - 2];
                            m = stack[sP - 1];
                        }
                    }
                }
            }
            return iAA2;
        }
    }

    public static final List breaks(final PrimeConstraints pC, final int i, final int j) {
        final List l = Chains.breaksP(pC, i, j);
        final List l2 = Chains.breaksP(pC, j, i);
        for (final Iterator it = l2.iterator(); it.hasNext();) {
            final int[] iA = (int[]) it.next();
            Array.mingle(iA, iA.length);
        }
        l.addAll(l2);
        Comparator c = new Comparator() {
            public int compare(Object arg0, Object arg1) {
                int[] iA = (int[]) arg0;
                int[] iA2 = (int[]) arg1;
                int i = iA[0] + iA[1];
                int j = iA2[0] + iA2[1];
                if (i < j) {
					return -1;
				}
                if (i > j) {
					return 1;
				}
                return 0;
            }
        };
        Collections.sort(l, c);
        c = new Comparator() {
            public int compare(final Object arg0, final Object arg1) {
                final int[] iA = (int[]) arg0;
                final int[] iA2 = (int[]) arg1;
                final int i = iA[0] + iA[1];
                final int j = iA2[0] + iA2[1];
                if (i == j) {
					return 0;
				}
                return -1;
            }
        };
        final Comparator c2 = new Comparator() {
            public int compare(Object arg0, Object arg1) {
                int[] iA = (int[]) arg0;
                int[] iA2 = (int[]) arg1;
                int i = iA[2] + iA[3];
                int j = iA2[2] + iA2[3];
                if (i == j) {
					return 0;
				}
                return -1;
            }
        };
        final Function_2Args fn = new Function_2Args() {
            public Object fn(Object o, Object o2) {
                return o2;
            }
        };
        final Function_2Args fn2 = new Function_2Args() {
            public Object fn(Object o, Object o2) {
                return o;
            }
        };
        return (List) IterationTools.append(Iterators.uniq(Iterators
                .uniq(l.iterator(), c, fn), c2, fn2),
                new LinkedList());
    }

    static final List breaksP(final PrimeConstraints pC, final int i, final int j) {
        final List l = new LinkedList();
        final SortedSet<ConstraintNode> sS = pC.primeConstraints[j][i];
        SortedSet sS2 = pC.primeConstraints[i][j];
        sS2 = sS2.headSet(sS2.last());
        for (final Iterator it = sS2.iterator(); it.hasNext();) {
            final ConstraintNode cN = (ConstraintNode) it.next();
            int x;
            int y;
            if (cN.maxOffset == Integer.MIN_VALUE) {
                x = cN.x;
                y = cN.y - 1;
            } else {
                x = cN.x + cN.maxOffset;
                y = cN.y + cN.maxOffset;
            }
            final ConstraintNode cN2 = PrimeConstraints
                    .getRightMostPointAffectedByConstraint(sS, x + 1);
            if (cN2.maxOffset == Integer.MIN_VALUE) {
                if ((cN2.y == x + 1) && (cN2.x >= y)) {
                    l.add(new int[] { x, y, x, cN2.x });
                }
            } else {
                if (cN.maxOffset == Integer.MIN_VALUE) {
                    if (cN2.y == x + 1) {
                        if (cN2.x > y) {
							l.add(new int[] { x, y, x, cN2.x - 1 });
						}
                    } else {
                        if ((cN2.y + cN2.maxOffset >= x)
                                && (cN2.y - cN2.x == x - y)) {
                            l.add(new int[] { x, y, x, y });
                        }
                    }
                }
            }
        }
        return l;
    }

    public static void transformConstraints(final int diagonalGap,
            final PrimeConstraints pC) {
        for (int i = 0; i < pC.sequenceNumber; i++) {
            for (int j = 0; j < pC.sequenceNumber; j++) {
                if (i != j) {
                    final SortedSet sS = pC.primeConstraints[i][j];
                    PrimeConstraints.ConstraintNode.compareX = true;
                    final Iterator it = sS.iterator();
                    it.next();
                    while (true) {
                        final PrimeConstraints.ConstraintNode cN = (PrimeConstraints.ConstraintNode) it
                                .next();
                        if (cN.x == Integer.MAX_VALUE) {
							break;
						}
                        cN.x -= diagonalGap;
                        cN.y += diagonalGap;
                    }
                }
            }
        }
    }

    public static void makeMultipleAlignment(final Procedure_Int[] outputs,
            final Generator[][] cAA, final byte[][] bAA, final int[] seqSizes,
            final int sequenceNumber, final int[] sA, final byte gap) {
        // for first sequence :
        // if min positions from other chains are aligned to position(s)
        // that proceed their current points then call those positions:
        final int[] positions = new int[sequenceNumber];
        final Generator[][] nextPairs = new Generator[sequenceNumber][sequenceNumber];
        final int[][] nexts = new int[sequenceNumber][sequenceNumber];
        for (int i = 0; i < sequenceNumber; i++) {
            for (int j = i + 1; j < sequenceNumber; j++) {
                final Generator gen = Chains.nextPairs(cAA[i][j], seqSizes[i],
                        seqSizes[j]);
                nextPairs[i][j] = gen;
                final int[] iA = (int[]) gen.gen();
                nexts[i][j] = iA[0];
                nexts[j][i] = iA[1];
            }
        }
        int i = 0;
        while (i < sequenceNumber) {
            final int j = seqSizes[i];
            while (positions[i] < j) {
				Chains.makeMultipleAlignment(outputs, nextPairs, nexts, bAA,
                        positions, i, sA, 0, gap);
			}
            i++;
        }
    }

    public static Generator nextPairs(final Generator gen,
            final int s1End, final int s2End) {
        return new Generator() {
            List<int[]> l = new LinkedList<int[]>();

            public Object gen() {
                if (this.l.size() != 0) {
                    return this.l.remove(0);
                }
                final PolygonFiller.Node l2 = (PolygonFiller.Node) gen
                        .gen();
                if (l2 == null) {
					return new int[] { s1End, s2End };
				}
                int y = l2.y;
                int x = l2.x;
                while (y <= l2.yMax) {
					this.l.add(new int[] { x++, y++ });
				}
                return this.l.remove(0);
            }
        };
    }

    public static void makeMultipleAlignment(final Procedure_Int[] outputs,
            final Generator[][] nextPairs, final int[][] nexts, final byte[][] bAA,
            final int[] positions, int i, final int[] sA, final int sIndex, final byte gap) {
        int j = Chains.getMins(nexts[i], sA, sIndex, i);
        {
            // insert gaps
            final int k = nexts[i][sA[sIndex]];
            if (positions[i] < k) {
                do {
                    for (int l = 0; l < outputs.length; l++) {
						if (l != i) {
							outputs[l].pro(gap);
						}
					}
                    outputs[i].pro(bAA[i][positions[i]++]);
                } while (positions[i] < k);
                return;
            }
        }
        final int sIndex2 = sIndex + outputs.length;
        Arrays.fill(sA, sIndex2, sIndex2 + outputs.length, 0);
        while (true) {
            for (int k = sIndex; k < j; k++) {
                final int l = sA[k];
                while (positions[l] < nexts[l][i]) {
                    Chains.makeMultipleAlignment(outputs, nextPairs, nexts,
                            bAA, positions, l, sA, sIndex2
                                    + outputs.length, gap);
                }
            }
            sA[sIndex2 + i] |= 2;
            for (int k = sIndex; k < j; k++) {
				sA[sIndex2 + sA[k]] |= 1;
			}
            source: {
                for (int k = 0; k < outputs.length; k++) {
					if (sA[sIndex2 + k] == 1) {
                        i = k;
                        break source;
                    }
				}
                j = sIndex;
                for (int k = 0; k < outputs.length; k++) {
					if (sA[sIndex2 + k] == 3) {
                        outputs[k].pro(bAA[k][positions[k]++]);
                        sA[j++] = k;
                    } else {
                        outputs[k].pro(gap);
                    }
				}
                for (int k = sIndex; k < j; k++) {
                    final int l = sA[k];
                    for (int m = k + 1; m < j; m++) {
                        final int n = sA[m];
                        if (nexts[l][n] == positions[l] - 1) {
                            final int[] iA = (int[]) nextPairs[l][n].gen();
                            nexts[l][n] = iA[0];
                            nexts[n][l] = iA[1];
                        }
                    }
                }
                return;
            }
            j = Chains.getMins(nexts[i], sA, sIndex, i);
        }
    }
    
    public static Chains.PrimeConstraints loadConstraintsFromAlignment(final String inputFile, 
    		final int seqNo) throws IOException {
    	//This function is used by Pecan-AMAP.
    	InputStream iS = new BufferedInputStream(new FileInputStream(inputFile));
    	LineNumberReader lNR = new LineNumberReader(new InputStreamReader(iS));
    	PrimeConstraints pC = new PrimeConstraints(seqNo);
    	String s;
  
    	s = lNR.readLine();
    	while(s != null) {
    		String[] sA = s.split("\\s+");
    		assert(sA.length % 2 == 1);
    		assert(sA.length >= 3);
    		int blockLength = Integer.parseInt(sA[0]);
    		int seq1 = Integer.parseInt(sA[1]);
    		int pos1 = Integer.parseInt(sA[2]);
    		for(int i=3; i<sA.length; i+=2) {
    			int seq2 = Integer.parseInt(sA[i+0]);
        		int pos2 = Integer.parseInt(sA[i+1]);
        		Chains.logger.info(blockLength + " " + seq1 + " " + pos1 + " " + seq2 + " " + pos2 + "\n");
        		pC.updatePrimeConstraints(seq1, seq2, pos1, pos2, blockLength);
        		pC.updatePrimeConstraints(seq2, seq1, pos2, pos1, blockLength);
    		}
    		s = lNR.readLine();
    	}
    	//Cleanup
    	iS.close();
    	return pC;
    }

    static int getMins(final int[] iA, final int[] sA, final int sIndex, final int i) {
        int j = Integer.MAX_VALUE;
        int k = sIndex;
        for (int l = 0; l < iA.length; l++) {
			if (l != i) {
                final int m = iA[l];
                if (m < j) {
                    j = m;
                    k = sIndex + 1;
                    sA[sIndex] = l;
                } else {
                    if (m == j) {
                        sA[k++] = l;
                    }
                }
            }
		}
        return k;
    }

    public static class Node {
        int xS;

        int xE;

        int score;

        Node n;

        Node p;

        Node() {
            // do nothing
        }

        Node(final int xS, final int xE, final int score, final Node n, final Node p) {
            this.xS = xS;
            this.xE = xE;
            this.score = score;
            this.n = n;
            this.p = p;
        }

        @Override
		public String toString() {
            return " ( " + this.xS + " : " + this.xE + " : " + this.score + " , " + this.n.xS + " : "
                    + this.n.xE + " : " + this.n.score + " ) " + (this.p != null ? this.p.toString() : "");
        }
    }

    public static final class PrimeConstraints {

        public final SortedSet[][] primeConstraints;

        final int sequenceNumber;

        final static ConstraintNode cN = new ConstraintNode(0, 0,
                Integer.MIN_VALUE);

        final private static int COORDINATE_MIN_VALUE = -100000;

        public PrimeConstraints(final int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            this.primeConstraints = new SortedSet[sequenceNumber][sequenceNumber];
            final ConstraintNode cN2 = new ConstraintNode(
                    PrimeConstraints.COORDINATE_MIN_VALUE, PrimeConstraints.COORDINATE_MIN_VALUE,// -1, -1,
                    Integer.MIN_VALUE);
            final ConstraintNode cN3 = new ConstraintNode(
                    Integer.MAX_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE);
            for (int i = 0; i < sequenceNumber; i++) {
                for (int j = 0; j < sequenceNumber; j++) {
                    if (i != j) {
                        final SortedSet<ConstraintNode> sS = new TreeSet<ConstraintNode>();
                        sS.add(cN2);
                        sS.add(cN3);
                        this.primeConstraints[i][j] = sS;
                    }
                }
            }
        }

        public static final class ConstraintNode implements
                Comparable, Cloneable {
            int x;

            int y;

            int maxOffset;

            public static boolean compareX = true;

            public static final boolean LESS_THAN_OR_EQUAL = true;

            public static final boolean LESS_THAN = false;

            public ConstraintNode(final int x, final int y, final int maxOffset) {
                this.x = x;
                this.y = y;
                this.maxOffset = maxOffset;
            }

            public final int compareTo(final Object arg0) {
                final ConstraintNode cN2 = (ConstraintNode) arg0;
                if (ConstraintNode.compareX) {
                    return (this.maxOffset != Integer.MIN_VALUE ? this.x
                            + this.maxOffset : this.x) < cN2.x ? -1
                            : this.x > (cN2.maxOffset != Integer.MIN_VALUE ? cN2.x
                                    + cN2.maxOffset
                                    : cN2.x) ? 1 : 0;
                }
                return this.y < cN2.y ? -1 : this.y > cN2.y ? 1 : 0;
            }

            @Override
			public final boolean equals(final Object arg0) {
                return super.equals(arg0);
                // throw new IllegalStateException();
            }

            @Override
			public final String toString() {
                return " ( " + this.x + " " + this.y + " " + this.maxOffset + " ) ";
            }

            @Override
			public final Object clone() {
                try {
                    return super.clone();
                } catch (final CloneNotSupportedException e) {
                    throw new IllegalStateException();
                }
            }
        }

        final public Node filterByConstraints(final int s1, final int s2, Node n) {
            ConstraintNode.compareX = true;
            n = PrimeConstraints.filterByConstraints(n, (List) IterationTools.append(
                    this.primeConstraints[s1][s2].iterator(),
                    new LinkedList()));
            return n;
        }

        final static Node filterByConstraints(Node n, final List constraints) {
            final ListIterator it = constraints.listIterator();
            if (!it.hasNext()) {
				return n;
			}
            ConstraintNode cN2 = (ConstraintNode) it.next();
            Node m = new Node();
            m.p = n;
            final Node p = m;
            while (n != null) {
                while (n.xS > (cN2.maxOffset != Integer.MIN_VALUE ? cN2.x
                        + cN2.maxOffset
                        : cN2.x)) {
                    if (it.hasNext()) {
                        cN2 = (ConstraintNode) it.next();
                    } else {
						return p.p;
					}
                }
                int i = 0;
                while (true) {
                    if (n.xE >= cN2.x) {
                        if (cN2.maxOffset == Integer.MIN_VALUE) {
                            if (n.xS - n.n.xS <= cN2.x - cN2.y) {
                                PrimeConstraints.split(n, cN2.x);
                                m = PrimeConstraints.clip(m, n, cN2.y);
                                n = m.p;
                                break;
                            }
                        } else {
                            if (n.xS - n.n.xS < cN2.x - cN2.y) {
                                PrimeConstraints.split(n, cN2.x + cN2.maxOffset);
                                m = PrimeConstraints.clip(m, n, cN2.y);
                                n = m.p;
                                break;
                            }
                        }
                        if (it.hasNext()) {
                            cN2 = (ConstraintNode) it.next();
                            i++;
                        } else {
                            m = n;
                            n = n.p;
                            break;
                        }
                    } else {
                        if (n.n.xE >= cN2.y) {
                            m = PrimeConstraints.clip(m, n, cN2.y);
                            n = m.p;
                            break;
                        }
                        m = n;
                        n = n.p;
                        break;
                    }
                }
                if (i > 0) {
                    while (i-- >= 0) {
						it.previous();
					}
                    cN2 = (ConstraintNode) it.next();
                    // reset back cN2
                }
            }
            return p.p;
        }
        
        static int noOfConstraintsClipped = 0;

        static final Node clip(final Node parent, final Node m, final int y) {
            PrimeConstraints.noOfConstraintsClipped++;
            final Node n = m.n;
            if (n.xS >= y) {
                parent.p = m.p;
                return parent;
            }
            n.xE = y - 1;
            m.xE = m.xS + (n.xE - n.xS);
            return m;
        }

        static final void split(final Node m, final int x) {
            if (m.xE > x) {
                final Node p = new Node(x + 1, m.xE, m.score, null, null);
                final int i = m.n.xE - (m.xE - x);
                final Node q = new Node(i + 1, m.n.xE, m.n.score, p, null);
                p.n = q;

                m.xE = x;
                m.n.xE = i;
                Chains.insert(p, m);
            }
        }

        public final Generator convertPrimeConstraintsToEdgeList(
                final int i, final int j, final boolean excludeLessThans) {
            SortedSet<ConstraintNode> sS = this.primeConstraints[i][j];
            ConstraintNode.compareX = false;
            PrimeConstraints.cN.y = PrimeConstraints.COORDINATE_MIN_VALUE + 1;
            sS = sS.tailSet(PrimeConstraints.cN);
            final ConstraintNode cN2 = new ConstraintNode(0,
                    Integer.MAX_VALUE, 0);
            sS = sS.headSet(cN2);
            return excludeLessThans ? PrimeConstraints.convertPrimeConstraintsToEdgeList_ExcludeLessThans(sS
                    .iterator())
                    : PrimeConstraints.convertPrimeConstraintsToEdgeList(sS.iterator());
        }

        private static final Generator convertPrimeConstraintsToEdgeList(
                final Iterator<ConstraintNode> it) {
            return new Generator() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Generator#gen()
                 */
                public final Object gen() {
                    if (it.hasNext()) {
                        final ConstraintNode cN = it
                                .next();
                        if (cN.maxOffset != Integer.MIN_VALUE) {
							return new PolygonFiller.Node(cN.x, cN.y,
                                    cN.y + cN.maxOffset, 1);
						}
                        return new PolygonFiller.Node(cN.x, cN.y,
                                cN.y, 0);
                    }
                    return null;
                }
            };
        }

        private static final Generator convertPrimeConstraintsToEdgeList_ExcludeLessThans(
                final Iterator<ConstraintNode> it) {
            return new Generator() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Generator#gen()
                 */
                public final Object gen() {
                    if (it.hasNext()) {
                        final ConstraintNode cN = it
                                .next();
                        if (cN.maxOffset != Integer.MIN_VALUE) {
							return new PolygonFiller.Node(cN.x, cN.y,
                                    cN.y + cN.maxOffset, 1);
						}
                        return this.gen();
                    }
                    return null;
                }
            };
        }

        final public void updatePrimeConstraints(final int s1, final int s2,
                Node n) {
            while (n != null) {
                this.updatePrimeConstraints(s1, s2, n.xS, n.n.xS, n.xE
                        - n.xS);
                n = n.p;
            }
        }

        final public void updatePrimeConstraints(final int s1,
                final int s2, final int x, final int y, int maxOffset) {
            boolean lessThan;
            if (maxOffset == Integer.MIN_VALUE) {
                lessThan = true;
                maxOffset = 0;
            } else {
				lessThan = false;
			}
            for (int i = 0; i < this.sequenceNumber; i++) {
                if (i != s1) {
                    int k = 0;
                    int k2 = 0;
                    while (k <= maxOffset) {
                        int pCs2iY;
                        boolean pCs2iType;
                        if (i != s2) {
                            final ConstraintNode cN2 = PrimeConstraints.getPrimeConstraint(
                                    this.primeConstraints[s2][i], y + k);
                            if ((cN2.maxOffset != Integer.MIN_VALUE)
                                    && (cN2.x <= y + maxOffset)) {
                                pCs2iType = ConstraintNode.LESS_THAN_OR_EQUAL;
                                if (cN2.x > y + k) {
                                    k = cN2.x - y;
                                    pCs2iY = cN2.y;
                                } else {
									pCs2iY = cN2.y - cN2.x + y + k;
								}
                                if (cN2.x + cN2.maxOffset < y
                                        + maxOffset) {
									k2 = cN2.x + cN2.maxOffset - y;
								} else {
									k2 = maxOffset;
								}
                            } else {
                                pCs2iType = ConstraintNode.LESS_THAN;
                                pCs2iY = cN2.y;
                                if (cN2.x < y + maxOffset) {
									k2 = cN2.x - y;
								} else {
									k2 = maxOffset;
								}
                                k = k2;
                            }
                        } else {
                            pCs2iY = y + k;
                            pCs2iType = ConstraintNode.LESS_THAN_OR_EQUAL;
                            k2 = maxOffset;
                        }
                        ConstraintNode pCs1i = PrimeConstraints.getPrimeConstraint(
                                this.primeConstraints[s1][i], x + k);
                        if (lessThan) {
							pCs2iType = ConstraintNode.LESS_THAN;
						}
                        while (true) {
                            int pCs1iY;
                            if (pCs1i.x < x + k) {
								pCs1iY = pCs1i.y - pCs1i.x + x + k;
							} else {
								pCs1iY = pCs1i.y;
							}
                            if ((pCs1iY > pCs2iY)
                                    || ((pCs1iY == pCs2iY)
                                            && (pCs2iType == ConstraintNode.LESS_THAN)
                                            && (pCs1i.maxOffset != Integer.MIN_VALUE) && (pCs1i.x
                                            - pCs1i.y == (x + k)
                                            - pCs2iY))) {
                                int k3;
                                while (true) {
                                    if (pCs1i.maxOffset != Integer.MIN_VALUE) {
                                        if (pCs1i.x + pCs1i.maxOffset >= x
                                                + k2) {
                                            k3 = k2;
                                            break;
                                        }
                                        k3 = pCs1i.x
                                                + pCs1i.maxOffset - x;
                                    } else {
                                        if (pCs1i.x >= x + k2) {
                                            k3 = k2;
                                            break;
                                        }
                                        k3 = pCs1i.x - x;
                                    }
                                    final int j = x + k3 + 1;
                                    pCs1i = PrimeConstraints.getPrimeConstraint(
                                            this.primeConstraints[s1][i],
                                            j);
                                    if (pCs1i.x < j) {
										pCs1iY = pCs1i.y - pCs1i.x
                                                + j;
									} else {
										pCs1iY = pCs1i.y;
									}
                                    if (pCs1iY < pCs2iY + k3 + 1 - k) {
										break;
									}
                                }
                                // int k3 = k2;
                                for (int j = 0; j < this.sequenceNumber; j++) {
                                    if ((j != s2) && (j != i)) {
                                        int k4 = k3;
                                        while (k4 >= k) {
                                            int k5;
                                            int pCjs1X;
                                            boolean pCjs1Type;
                                            if (j != s1) {
                                                final ConstraintNode cN2 = PrimeConstraints.getRightMostPointAffectedByConstraint(
                                                        this.primeConstraints[j][s1],
                                                        x + k4);
                                                if (cN2.maxOffset != Integer.MIN_VALUE) {
                                                    if (cN2.y
                                                            + cN2.maxOffset >= x
                                                            + k) {
                                                        pCjs1Type = ConstraintNode.LESS_THAN_OR_EQUAL;
                                                        if (cN2.y
                                                                + cN2.maxOffset < x
                                                                + k4) {
                                                            k4 = cN2.y
                                                                    + cN2.maxOffset
                                                                    - x;
                                                            pCjs1X = cN2.x
                                                                    + cN2.maxOffset;
                                                        } else {
															pCjs1X = cN2.x
                                                                    - cN2.y
                                                                    + x
                                                                    + k4;
														}
                                                        if (cN2.y > x
                                                                + k) {
															k5 = cN2.y
                                                                    - x;
														} else {
															k5 = k;
														}
                                                        pCjs1X += k5
                                                                - k4;
                                                    } else {
                                                        pCjs1Type = ConstraintNode.LESS_THAN;
                                                        pCjs1X = cN2.x
                                                                + cN2.maxOffset;
                                                        if (cN2.y
                                                                + cN2.maxOffset >= x
                                                                + k) {
															k4 = cN2.y
                                                                    + cN2.maxOffset
                                                                    - x;
														} else {
															k4 = k;
														}
                                                        k5 = k4;
                                                    }
                                                } else {
                                                    pCjs1Type = ConstraintNode.LESS_THAN;
                                                    pCjs1X = cN2.x;
                                                    if (cN2.y >= x
                                                            + k) {
														k4 = cN2.y
                                                                - x;
													} else {
														k4 = k;
													}
                                                    k5 = k4;
                                                }
                                            } else {
                                                pCjs1X = x + k;
                                                pCjs1Type = ConstraintNode.LESS_THAN_OR_EQUAL;
                                                k5 = k;
                                            }
                                            ConstraintNode pCji = PrimeConstraints.getPrimeConstraint(
                                                    this.primeConstraints[j][i],
                                                    pCjs1X);
                                            int k6 = k5;
                                            final boolean pCjiType = ((pCjs1Type == ConstraintNode.LESS_THAN_OR_EQUAL) && (pCs2iType == ConstraintNode.LESS_THAN_OR_EQUAL)) ? ConstraintNode.LESS_THAN_OR_EQUAL
                                                    : ConstraintNode.LESS_THAN;
                                            source: while (true) {
                                                int pCjiY;
                                                if (pCji.x < pCjs1X) {
													pCjiY = pCji.y
                                                            - pCji.x
                                                            + pCjs1X;
												} else {
													pCjiY = pCji.y;
												}
                                                {
                                                    final int m = pCs2iY
                                                            + k6 - k;
                                                    if (pCjiType == ConstraintNode.LESS_THAN) {
                                                        if ((pCjiY > m)
                                                                || ((pCjiY == m)
                                                                        && (pCji.maxOffset != Integer.MIN_VALUE) && (pCjs1X
                                                                        - m == pCji.x
                                                                        - pCji.y))) {
                                                            this.updatePrimeConstraints_LessThan(
                                                                    this.primeConstraints[j][i],
                                                                    new ConstraintNode(
                                                                            pCjs1X,
                                                                            m,
                                                                            Integer.MIN_VALUE));
                                                        }
                                                        break;
                                                    }
                                                    if (pCjiY > m) {
                                                        final int n = pCs2iY
                                                                + k4
                                                                - k;
                                                        while (true) {
                                                            if (pCjiY > n) {
                                                                this.updatePrimeConstraints_LessThanOrEqual(
                                                                        this.primeConstraints[j][i],
                                                                        new ConstraintNode(
                                                                                pCjs1X,
                                                                                m,
                                                                                k4
                                                                                        - k6));
                                                                break source;
                                                            }
                                                            if (pCji.x
                                                                    - pCji.y < pCjs1X
                                                                    - m) {
                                                                if (pCji.maxOffset == Integer.MIN_VALUE) {
                                                                    pCji = PrimeConstraints.getPrimeConstraint(
                                                                            this.primeConstraints[j][i],
                                                                            pCji.x + 1);
                                                                } else {
                                                                    pCji = PrimeConstraints.getPrimeConstraint(
                                                                            this.primeConstraints[j][i],
                                                                            pCji.x
                                                                                    + pCji.maxOffset
                                                                                    + 1);
                                                                }
                                                                pCjiY = pCji.y;
                                                                continue;
                                                            }
                                                            this.updatePrimeConstraints_LessThanOrEqual(
                                                                    this.primeConstraints[j][i],
                                                                    new ConstraintNode(
                                                                            pCjs1X,
                                                                            m,
                                                                            pCjiY
                                                                                    - m
                                                                                    - 1));
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (pCji.maxOffset != Integer.MIN_VALUE) {
                                                    if (pCji.x
                                                            + pCji.maxOffset >= pCjs1X
                                                            + (k4 - k6)) {
														break;
													}
                                                    final int m = pCji.x
                                                            + pCji.maxOffset
                                                            - pCjs1X
                                                            + 1;
                                                    k6 += m;
                                                    pCjs1X += m;
                                                } else {
                                                    if (pCji.x >= pCjs1X
                                                            + (k4 - k6)) {
														break;
													}
                                                    final int m = pCji.x
                                                            - pCjs1X
                                                            + 1;
                                                    k6 += m;
                                                    pCjs1X += m;
                                                }
                                                pCji = PrimeConstraints.getPrimeConstraint(
                                                        this.primeConstraints[j][i],
                                                        pCjs1X);

                                            }
                                            k4 = k5 - 1;
                                        }
                                    }
                                }
                            }
                            if (pCs1i.maxOffset != Integer.MIN_VALUE) {
                                if (pCs1i.x + pCs1i.maxOffset >= x
                                        + k2) {
									break;
								}
                                final int m = pCs1i.x + pCs1i.maxOffset - x
                                        + 1;
                                pCs2iY += m - k;
                                k = m;
                            } else {
                                if (pCs1i.x >= x + k2) {
									break;
								}
                                final int m = pCs1i.x - x + 1;
                                pCs2iY += m - k;
                                k = m;
                            }
                            pCs1i = PrimeConstraints.getPrimeConstraint(
                                    this.primeConstraints[s1][i], x + k);
                        }
                        k = k2 + 1;
                    }
                }
            }
        }

        final void updatePrimeConstraints_LessThan(
                final SortedSet<ConstraintNode> primeConstraints, final ConstraintNode constraint) {
            ConstraintNode.compareX = false;
            final SortedSet<ConstraintNode> sS = primeConstraints.headSet(constraint);
            final ConstraintNode cN2 = sS.last();
            final int i = constraint.x;
            if (cN2.maxOffset != Integer.MIN_VALUE) {
                if (cN2.y + cN2.maxOffset >= constraint.y) {
                    if (cN2.x + cN2.maxOffset > i) {
                        final int j = i + 1;
                        final int k = j - cN2.x;
                        final ConstraintNode cN3 = new ConstraintNode(j,
                                cN2.y + k, cN2.maxOffset - k);
                        cN2.maxOffset = constraint.y - cN2.y - 1;
                        primeConstraints.add(constraint);
                        primeConstraints.add(cN3);
                        return;
                    }
                    cN2.maxOffset = constraint.y - cN2.y - 1;
                }
            }
            this.updatePrimeConstraints_LessThan(primeConstraints,
                    constraint, i);
        }

        final void updatePrimeConstraints_LessThanOrEqual(
                final SortedSet<ConstraintNode> primeConstraints, final ConstraintNode constraint) {
            // Debug.pl(" Updating constraints " + constraint);
            ConstraintNode.compareX = false;
            final SortedSet<ConstraintNode> sS = primeConstraints.headSet(constraint);
            final ConstraintNode cN2 = sS.last();
            int i = constraint.x;
            i += constraint.maxOffset;
            final int diag = constraint.x - constraint.y;
            if (cN2.maxOffset != Integer.MIN_VALUE) {
                final int j = cN2.y + cN2.maxOffset;
                if (j >= constraint.y) {
                    if (cN2.x + cN2.maxOffset > i) {
                        if (cN2.x - cN2.y == diag) {
							return;
						}
                        final int k = i + 1;
                        final int l = k - cN2.x;
                        final ConstraintNode cN3 = new ConstraintNode(k,
                                cN2.y + l, cN2.maxOffset - l);
                        cN2.maxOffset = constraint.y - cN2.y - 1;
                        primeConstraints.add(constraint);
                        primeConstraints.add(cN3);
                        return;
                    }
                    if (cN2.x - cN2.y == diag) {
                        this.updatePrimeConstraints_LessThanOrEqual2(
                                primeConstraints, cN2, i, diag);
                        return;
                    }
                    cN2.maxOffset = constraint.y - cN2.y - 1;
                    this.updatePrimeConstraints_LessThanOrEqual(
                            primeConstraints, constraint, i, diag);
                    return;
                }
                if (j + 1 == constraint.y) {
                    if (cN2.x - cN2.y == diag) {
                        this.updatePrimeConstraints_LessThanOrEqual2(
                                primeConstraints, cN2, i, diag);
                        return;
                    }
                }
            }
            this.updatePrimeConstraints_LessThanOrEqual(primeConstraints,
                    constraint, i, diag);
        }

        final void updatePrimeConstraints_LessThan(
                final SortedSet<ConstraintNode> primeConstraints,
                final ConstraintNode constraint, final int i) {
            ConstraintNode.compareX = false;
            SortedSet<ConstraintNode> sS = primeConstraints.tailSet(constraint);
            final ConstraintNode cN2 = sS.first();
            if (cN2.x <= i) {
                int j = cN2.x;
                if (cN2.maxOffset != Integer.MIN_VALUE) {
					j += cN2.maxOffset;
				}
                if (j > i) {
                    final int k = i + 1;
                    cN2.y = cN2.y - cN2.x + k;
                    cN2.x = k;
                    cN2.maxOffset = j - k;
                    primeConstraints.add(constraint);
                    return;
                }
                PrimeConstraints.cN.y = cN2.y + 1;
                sS = sS.tailSet(PrimeConstraints.cN);
                ConstraintNode cN3 = sS.first();
                while (cN3.x <= i) {
                    j = cN3.x;
                    if (cN3.maxOffset != Integer.MIN_VALUE) {
						j += cN3.maxOffset;
					}
                    if (j > i) {
                        final int k = i + 1;
                        cN3.y = cN3.y - cN3.x + k;
                        cN3.x = k;
                        cN3.maxOffset = j - k;
                        break;
                    }
                    sS.remove(cN3);
                    cN3 = sS.first();
                }
                cN2.x = constraint.x;
                cN2.y = constraint.y;
                cN2.maxOffset = constraint.maxOffset;
                return;
            }
            primeConstraints.add(constraint);
        }

        final void updatePrimeConstraints_LessThanOrEqual(
                final SortedSet<ConstraintNode> primeConstraints,
                final ConstraintNode constraint, final int i, final int diag) {
            ConstraintNode.compareX = false;
            SortedSet<ConstraintNode> sS = primeConstraints.tailSet(constraint);
            final ConstraintNode cN2 = sS.first();
            if (cN2.x <= i) {
                int j = cN2.x;
                if (cN2.maxOffset != Integer.MIN_VALUE) {
					j += cN2.maxOffset;
				}
                if (j > i) {
                    if (cN2.x - cN2.y == diag) {
                        cN2.maxOffset += cN2.x - constraint.x;
                        cN2.x = constraint.x;
                        cN2.y = constraint.y;
                        return;
                    }
                    final int k = i + 1;
                    cN2.y = cN2.y - cN2.x + k;
                    cN2.x = k;
                    cN2.maxOffset = j - k;
                    primeConstraints.add(constraint);
                    return;
                }
                PrimeConstraints.cN.y = cN2.y + 1;
                sS = sS.tailSet(PrimeConstraints.cN);
                ConstraintNode cN3 = sS.first();
                while (cN3.x <= i) {
                    j = cN3.x;
                    if (cN3.maxOffset != Integer.MIN_VALUE) {
						j += cN3.maxOffset;
					}
                    if (j > i) {
                        if (cN3.x - cN3.y == diag) {
                            sS.remove(cN2);
                            cN3.maxOffset += cN3.x - constraint.x;
                            cN3.x = constraint.x;
                            cN3.y = constraint.y;
                            return;
                        }
                        final int k = i + 1;
                        cN3.y = cN3.y - cN3.x + k;
                        cN3.x = k;
                        cN3.maxOffset = j - k;
                        break;
                    }
                    sS.remove(cN3);
                    cN3 = sS.first();
                }
                if ((cN3.x - 1 == i) && (cN3.x - cN3.y == diag)
                        && (cN3.maxOffset != Integer.MIN_VALUE)) {
                    sS.remove(cN3);
                    cN2.x = constraint.x;
                    cN2.y = constraint.y;
                    cN2.maxOffset = cN3.x + cN3.maxOffset
                            - constraint.x;
                    return;
                }
                cN2.x = constraint.x;
                cN2.y = constraint.y;
                cN2.maxOffset = constraint.maxOffset;
                return;
            }
            if ((cN2.x - 1 == i) && (cN2.x - cN2.y == diag)
                    && (cN2.maxOffset != Integer.MIN_VALUE)) {
                cN2.maxOffset += cN2.x - constraint.x;
                cN2.x = constraint.x;
                cN2.y = constraint.y;
                return;
            }
            primeConstraints.add(constraint);
        }

        final void updatePrimeConstraints_LessThanOrEqual2(
                final SortedSet<ConstraintNode> primeConstraints, final ConstraintNode cN2,
                final int i, final int diag) {
            PrimeConstraints.cN.y = cN2.y + 1;
            ConstraintNode.compareX = false;
            final SortedSet<ConstraintNode> sS = primeConstraints.tailSet(PrimeConstraints.cN);
            ConstraintNode cN3 = sS.first();
            while (cN3.x <= i) {
                int j = cN3.x;
                if (cN3.maxOffset != Integer.MIN_VALUE) {
					j += cN3.maxOffset;
				}
                if (j > i) {
                    if (cN3.x - cN3.y == diag) {
                        sS.remove(cN3);
                        cN2.maxOffset = j - cN2.x;
                        return;
                    }
                    final int k = i + 1;
                    cN3.y = cN3.y - cN3.x + k;
                    cN3.x = k;
                    cN3.maxOffset = j - k;
                    break;
                }
                sS.remove(cN3);
                cN3 = sS.first();
            }
            if ((cN3.x - 1 == i) && (cN3.x - cN3.y == diag)
                    && (cN3.maxOffset != Integer.MIN_VALUE)) {
                sS.remove(cN3);
                cN2.maxOffset = cN3.x + cN3.maxOffset - cN2.x;
                return;
            }
            cN2.maxOffset = i - cN2.x;
        }

        final static ConstraintNode getRightMostPointAffectedByConstraint(
                final SortedSet<ConstraintNode> primeConstraints, final int i) {
            ConstraintNode.compareX = false;
            PrimeConstraints.cN.y = i + 1;
            final SortedSet<ConstraintNode> sS = primeConstraints.headSet(PrimeConstraints.cN);
            return (ConstraintNode) sS.last()
                    .clone();
        }

        final static ConstraintNode getPrimeConstraint(
                final SortedSet<ConstraintNode> primeConstraints, final int i) {
            ConstraintNode.compareX = true;
            PrimeConstraints.cN.x = i;
            final SortedSet<ConstraintNode> sS = primeConstraints.tailSet(PrimeConstraints.cN);
            return (ConstraintNode) sS.first()
                    .clone();
        }
    }

    static final List overlap(final List l, final Function_Int_2Args sum) {
        final List l2 = new LinkedList();
        if (l.size() == 0) {
			return l2;
		}
        final Comparator c = new Comparator() {
            public int compare(Object arg0, Object arg1) {
                Node m = (Node) arg0;
                Node n = (Node) arg1;
                int i = m.xS - m.n.xS;
                int j = n.xS - n.n.xS;
                return i < j ? -1 : i > j ? 1 : m.xS < n.xS ? -1
                        : m.xS > n.xS ? 1 : 0;
            }
        };
        Collections.sort(l, c);
        Node m = (Node) l.remove(0);
        int i = m.xS - m.n.xS;
        while (l.size() != 0) {
            final Node n = (Node) l.remove(0);
            final int j = n.xS - n.n.xS;
            if ((i == j) && (m.xE >= n.xS)) {
                if (m.xS < n.xS) {
                    final Node o = new Node(m.xS, n.xS - 1, m.score, null,
                            null);
                    final Node p = new Node(m.n.xS, n.n.xS - 1, m.score, o,
                            null);
                    o.n = p;
                    l2.add(o);
                    m.xS = n.xS;
                    m.n.xS = n.n.xS;
                }
                if (m.xE > n.xE) {
                    n.score = sum.fn(m.score, n.score);
                    n.n.score = n.score;
                    m.xS = n.xE + 1;
                    m.n.xS = n.n.xE + 1;
                    Chains.insert(l, m, c);
                    m = n;
                    continue;
                }
                if (n.xE > m.xE) {
                    m.score = sum.fn(m.score, n.score);
                    m.n.score = m.score;
                    n.xS = m.xE + 1;
                    n.n.xS = m.n.xE + 1;
                    Chains.insert(l, n, c);
                    continue;
                }
                m.score = sum.fn(m.score, n.score);
                m.n.score = m.score;
            } else {
                l2.add(m);
                i = j;
                m = n;
            }
        }
        l2.add(m);
        Collections.sort(l2, Chains.nodeComparator());
        return l2;
    }

    static void insert(final List l, final Node m, final Comparator c) {
        for (final ListIterator it = l.listIterator(); it.hasNext();) {
            final Node n = (Node) it.next();
            if (c.compare(m, n) <= 0) {
                it.previous();
                it.add(m);
                return;
            }
        }
        l.add(m);
    }

    static Node getNext(final List l, final List l2, final Comparator<Object> c) {
        if (l.size() != 0) {
            final Object o = l.get(0);
            if (l2.size() != 0) {
                final Object o2 = l2.get(0);
                return (Node) (c.compare(o, o2) == 1 ? l2.remove(0)
                        : l.remove(0));
            }
            return (Node) l.remove(0);
        }
        if (l2.size() != 0) {
            return (Node) l2.remove(0);
        }
        return null;
    }

    static final Node overlapAndMerge(Node m, Node n,
            final Function_Int_2Args addScores) {
        final List l = new LinkedList();
        while (m != null) {
            l.add(m);
            m = m.p;
        }
        while (n != null) {
            l.add(n);
            n = n.p;
        }
        return Chains.joinUpNodes(Chains.overlap(l, addScores));
    }

    static final Node merge(Node m, Node n) {
        if (m == null) {
			return n;
		}
        if (n == null) {
			return m;
		}
        if ((m.xS < n.xS) || ((m.xS == n.xS) && (m.n.xS < n.n.xS))) {
            final Node o = m;
            m = n;
            n = o;
        }
        final Node p = n;
        while (m != null) {
            final Node o = m.p;
            Chains.insert(m, n);
            n = m;
            m = o;
        }
        return p;
    }

    static final void insert(final Node m, Node n) {
        while ((n.p != null)
                && ((n.p.xS < m.xS) || ((n.p.xS == m.xS) && (n.p.n.xS < m.n.xS)))) {
            n = n.p;
        }
        m.p = n.p;
        n.p = m;
    }

    static void checkOrderingOfNet(Chains.Node m,
            final boolean canOverlapStartingPoints) {
        int pX = Integer.MIN_VALUE;
        int pY = Integer.MIN_VALUE;
        while (m != null) {
            if (m.xE - m.xS != m.n.xE - m.n.xS) {
				throw new IllegalStateException();
			}
            if (m.score != m.n.score) {
				throw new IllegalStateException();
			}
            if (m != m.n.n) {
				throw new IllegalStateException();
			}
            if (pX > m.xS) {
				throw new IllegalStateException();
			}
            if (canOverlapStartingPoints) {
				if (pY > m.n.xS) {
					throw new IllegalStateException();
				} else if (pY >= m.n.xS) {
					throw new IllegalStateException();
				}
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

    static final Node consistencyChains(final Node m, final Node n,
            final Function_Int_2Args scoreFn, final Function_Int_2Args consistencyWeight) {
        final List l = new LinkedList();
        Chains.consistencyChains(m, n, new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#fn(java.lang.Object,
             *      java.lang.Object)
             */
            public void pro(final Object o) {
                l.add(o);
            }
        }, consistencyWeight);
        final List l2 = Chains.overlap(l, scoreFn);
        return Chains.joinUpNodes(l2);
    }

    static final Node joinUpNodes(final List l) {
        final Iterator it = l.iterator();
        if (it.hasNext()) {
            Node m = (Node) it.next();
            final Node o = m;
            while (it.hasNext()) {
                final Node n = (Node) it.next();
                m.p = n;
                m = n;
            }
            m.p = null;
            return o;
        }
        return null;
    }

    static final Comparator nodeComparator() {
        return new Comparator() {
            /*
             * (non-Javadoc)
             * 
             * @see java.util.Comparator#compare(java.lang.Object,
             *      java.lang.Object)
             */
            public int compare(final Object arg0, final Object arg1) {
                final Node q = (Node) arg0;
                final Node r = (Node) arg1;
                return q.xS < r.xS ? -1 : q.xS > r.xS ? 1
                        : q.n.xS < r.n.xS ? -1 : q.n.xS > r.n.xS ? 1
                                : 0;
            }
        };
    }

    public final static Node sortOppositeChain(Node m) {
        final List l = new LinkedList();
        while (m != null) {
            l.add(m.n);
            m = m.p;
        }
        Collections.sort(l, Chains.nodeComparator());
        Node q = new Node();
        final Node r = q;
        for (final Iterator it = l.iterator(); it.hasNext();) {
            q.p = (Node) it.next();
            q = q.p;
        }
        q.p = null;
        return r.p;
    }

    final static void consistencyChains(Node m, Node n, final Procedure pro, final Function_Int_2Args consistencyWeight) {
        if (n == null) {
			return;
		}
        while ((m != null) && (n != null)) {
            while (m.xS > n.xE) {
                n = n.p;
                if (n == null) {
					return;
				}
            }
            Node p = n;
            source: while (m.xE >= p.xS) {
                // calculate overlap
                int i, j, k, l;
                if (m.xS < p.xS) {
                    final int a = p.xS - m.xS;
                    i = m.n.xS + a;
                    j = p.n.xS;
                } else {
                    final int a = m.xS - p.xS;
                    i = m.n.xS;
                    j = p.n.xS + a;
                }
                if (m.xE > p.xE) {
                    final int a = m.xE - p.xE;
                    k = m.n.xE - a;
                    l = p.n.xE;
                } else {
                    final int a = p.xE - m.xE;
                    k = m.n.xE;
                    l = p.n.xE - a;
                }
                final int d = (m.score + p.score) / 2;
                final Node q = new Node(i, k, d, null, null);
                final Node r = new Node(j, l, d, q, null);
                q.n = r;
                pro.pro(q);
                p = p.p;
                if (p == null) {
					break;
				}
                while (m.xS > p.xE) {
                    p = p.p;
                    if (p == null) {
						break source;
					}
                }
            }
            m = m.p;
        }
    }

    public final static Node convertEdgeListToNodeChain(final Generator gen) {
        Node m = new Node();
        final Node n = m;
        // List l;
        PolygonFiller.Node l;
        while ((l = (PolygonFiller.Node) gen.gen()) != null) {
            final Node p = new Node(l.x, l.x + l.yMax - l.y, l.z, null,
                    null);
            final Node q = new Node(l.y, l.yMax, l.z, p, null);
            p.n = q;
            m.p = p;
            m = p;
        }
        return n.p;
    }

    final static List localAlignmentsInGaps(final Generator chain,
            final Aligner aligner, final int minGap, final int maxGap, int startX,
            int startY, int lengthX, int lengthY, final byte[] bA,
            final byte[] bA2) {
        final List l2 = new LinkedList();
        int[] iA;
        final Function_Int_2Args splitWeights = new Function_Int_2Args() {
            public int fn(int i, int j) {
                return (i + j) / 2;
            }
        };
        for (final Generator gen = PolygonFiller.nERPoints(chain, minGap*2,
                startX - 1, startY - 1, startX + lengthX, startY
                        + lengthY); (iA = (int[]) gen.gen()) != null;) {
            startX = iA[0] + 1;
            startY = iA[1] + 1;
            lengthX = iA[2] - iA[0] - 1;
            lengthY = iA[3] - iA[1] - 1;
            if ((lengthX >= minGap) && (lengthY >= minGap)) {
                if (maxGap > lengthX + lengthY) {
                    IterationTools.append(aligner.align(bA, bA2,
                            startX, lengthX, startY, lengthY), l2);
                } else {
                    final int lX = lengthX > maxGap / 2 ? maxGap / 2
                            : lengthX;
                    final int lY = lengthY > maxGap / 2 ? maxGap / 2
                            : lengthY;
                    Node m = Chains.convertEdgeListToNodeChain(Generators
                            .iteratorGenerator(aligner.align(bA, bA2,
                                    startX, lX, startY, lY)));
                    startX = startX + lengthX - lX;
                    startY = startY + lengthY - lY;
                    final Node n = Chains.convertEdgeListToNodeChain(Generators
                            .iteratorGenerator(aligner.align(bA, bA2,
                                    startX, lX, startY, lY)));
                    m = Chains.overlapAndMerge(m, n, splitWeights);
                    GeneratorTools.append(
                            Chains.convertNodeChainToEdgeList(m), l2);
                }
            }
        }
        return l2;
    }

    /**
     * Does not connect complementary chain
     * 
     * @param m
     * @return
     */
    final static Node copyOfChain(Node m) {
        if (m != null) {
            Node n = new Node(m.xS, m.xE, m.score, null, null);
            final Node o = new Node(m.n.xS, m.n.xE, m.n.score, n, null);
            n.n = o;
            final Node p = n;
            m = m.p;
            while (m != null) {
                final Node q = new Node(m.xS, m.xE, m.score, null, null);
                final Node r = new Node(m.n.xS, m.n.xE, m.n.score, q, null);
                q.n = r;

                n.p = q;
                n = q;
                m = m.p;
            }
            return p;
        }
        return null;
    }

    // for progressively more lenient local alignment parameters...
    // for every pair call local aligner to get significant local alignments
    // between gaps in preexisting alignment
    // store on disc or memory, need to provide resetable generator functions
    final static Node[][] makeConsistentChains(final Node[][] chains,
            final int[][] pairOrdering, final PrimeConstraints pC,
            final int sequenceNumber, final boolean consistencyTransform) {
        // given outgroups and ordering
        // for each pair, in order of ascending distance
        final Node[][] chains2 = new Node[sequenceNumber][sequenceNumber];
        final Function_Int_2Args sum = Functions_Int_2Args.sum();
        final Function_Int_2Args max = Functions_Int_2Args.sum(); //max();
        final Function_Int_2Args min = Functions_Int_2Args.min();
        for (int i = 0; i < pairOrdering.length; i++) {
            final int[] iA = pairOrdering[i];
            final int s1 = iA[0];
            final int s2 = iA[1];
            Chains.logger.info(" pair " + i + " " + s1 + " " + s2);
            // get projected alignments and filter for constraints
            Node hC1 = Chains.copyOfChain(chains[s1][s2]);
            if (consistencyTransform) {
                for (int j = 0; j < sequenceNumber; j++) {
                    if ((j != s1) && (j != s2)) {
                        final Node m = Chains.consistencyChains(chains[j][s1],
                                chains[j][s2], max, min);
                        hC1 = Chains.overlapAndMerge(m, hC1, sum);
                    }
                }
            }
            // get highest scoring chains and store
            hC1 = pC.filterByConstraints(s1, s2, hC1);
            hC1 = Chains.sortOppositeChain(hC1);
            hC1 = pC.filterByConstraints(s2, s1, hC1);
            Chains.multiplyScoresByLengths(hC1);
            hC1 = Chains.getHighestChain(hC1);
            Chains.divideScoresByLengths(hC1);
            //hC1 = trim(hC1, edgeTrim);
            chains2[s2][s1] = hC1;
            pC.updatePrimeConstraints(s2, s1, hC1);
            hC1 = Chains.sortOppositeChain(hC1);
            chains2[s1][s2] = hC1;
            pC.updatePrimeConstraints(s1, s2, hC1);
        }
        return chains2;
    }

    public static Node trim(Node n, final int i) {
        final int j = i * 2;
        while (true) {
            if (n == null) {
				return null;
			}
            if (n.xE - n.xS >= j) {
                n.xS += i;
                n.xE -= i;
                n.n.xS += i;
                n.n.xE -= i;
                break;
            }
            n = n.p;
        }
        final Node begin = n;
        Node m = n;
        n = n.p;
        while (n != null) {
            if (n.xE - n.xS >= j) {
                n.xS += i;
                n.xE -= i;
                n.n.xS += i;
                n.n.xE -= i;
                m = n;
                n = n.p;
            } else {
                m.p = n.p;
                n = n.p;
            }
        }
        return begin;
    }

    public final static void multiplyScoresByLengths(Node m) {
        while (m != null) {
            m.score = m.score * (m.xE - m.xS + 1);
            m.n.score = m.score;
            m = m.p;
        }
    }

    public final static void transformChains(Node m, final int x, final int y) {
        while (m != null) {
            m.xE += x;
            m.xS += x;
            m.n.xE += y;
            m.n.xS += y;
            m = m.p;
        }
    }

    public final static void makeConsistentChains(
            final PrimeConstraints pC, final int[][] pairOrdering,
            final int sequenceNumber, final Aligner[] aligners, final int alignerIndex,
            final int minGap, final int[] maxGaps, final byte[][] strings,
            final int[] seqSizes, final boolean[] consistencyTransform, final int edgeTrim) {
        for (int i = alignerIndex; i >= 0; i--) {
            Chains.makeConsistentChains(pC, pairOrdering, sequenceNumber,
                    aligners[i], minGap, maxGaps[i], strings,
                    seqSizes, consistencyTransform[i], edgeTrim);
        }
    }

    public final static void makeConsistentChains(
            final PrimeConstraints pC, final int[][] pairOrdering,
            final int sequenceNumber, final Aligner aligner, final int minGap,
            final int maxGap, final byte[][] strings, final int[] seqSizes,
            final boolean consistencyTransform, final int edgeTrim) {
        final Node[][] nAA = new Node[sequenceNumber][sequenceNumber];
        for (int i = 0; i < sequenceNumber; i++) {
            final int lengthX = seqSizes[i];
            final byte[] bA = strings[i];
            for (int j = i + 1; j < sequenceNumber; j++) {
                final int lengthY = seqSizes[j];
                // the leading sequence is y
                final List l = Chains.localAlignmentsInGaps(
                        pC.convertPrimeConstraintsToEdgeList(i, j,
                                true), aligner, minGap, maxGap, 0, 0,
                        lengthX, lengthY, bA, strings[j]);
                Node m = Chains.convertEdgeListToNodeChain(PolygonFiller
                        .flipEdgeXYDiagonalsCoordinates(Generators
                                .iteratorGenerator(l.iterator())));
                m = Chains.trim(m, edgeTrim);
                nAA[j][i] = m;
                nAA[i][j] = Chains.sortOppositeChain(m);
            }
        }
        Chains.makeConsistentChains(nAA, pairOrdering, pC,
                sequenceNumber, consistencyTransform);
    }

    final static class EdgeNode {
        int xMin, yMin, yMax;

        long score;

        EdgeNode eN;

        EdgeNode(final int xMin, final int yMin, final int yMax, final long score, final EdgeNode eN) {
            this.xMin = xMin;
            this.yMin = yMin;
            this.yMax = yMax;
            this.score = score;
            this.eN = eN;
        }
    }

    final static Node getHighestChain(final Node n) {
        return Chains.convertBackwardsCumulativeEdgeNodeToNode(Chains.getHighestChainP(n));
    }

    final static Node convertBackwardsCumulativeEdgeNodeToNode(
            EdgeNode eN) {
        if (eN != null) {
            Node m = null;
            EdgeNode eN2 = eN.eN;
            while (eN2 != null) {
                m = new Node(eN.yMin, eN.yMax,
                        (int) (eN.score - eN2.score), null, m);
                final Node n = new Node(eN.xMin, eN.xMin - eN.yMin
                        + eN.yMax, m.score, m, null);
                m.n = n;
                eN = eN2;
                eN2 = eN2.eN;
            }
            m = new Node(eN.yMin, eN.yMax, (int) eN.score, null, m);
            final Node n = new Node(eN.xMin, eN.xMin - eN.yMin + eN.yMax,
                    m.score, m, null);
            m.n = n;
            return m;
        }
        return null;
    }

    final static EdgeNode getHighestChainP(Node n) {
        final SortedMap<Integer, Object> sM1 = new TreeMap<Integer, Object>(), sM2 = new TreeMap<Integer, Object>();
        if (n != null) {
            source: while (true) {
                final int y = n.xS;
                Chains.addInChains(sM1, sM2, y);
                do {
                    final SortedMap<Integer, Object> sM3 = sM1.headMap(new Integer(n.n.xS));
                    EdgeNode eN;
                    if (!sM3.isEmpty()) {
                        final EdgeNode eN2 = (EdgeNode) sM3.get(sM3
                                .lastKey());
                        eN = new EdgeNode(n.n.xS, y, n.xE, eN2.score
                                + n.score, eN2);
                    } else {
                        eN = new EdgeNode(n.n.xS, y, n.xE, n.score,
                                null);
                    }
                    if (sM2.containsKey(new Integer(eN.yMax))) {
						List<EdgeNode> list = (List<EdgeNode>) sM2.get(new Integer(eN.yMax));
						list.add(eN);
					} else {
                        final List<EdgeNode> l2 = new LinkedList<EdgeNode>();
                        l2.add(eN);
                        sM2.put(new Integer(eN.yMax), l2);
                    }
                    n = n.p;
                    if (n == null) {
						break source;
					}
                } while (n.xS == y);
            }
        }
        Chains.addInChains(sM1, sM2, Integer.MAX_VALUE);
        return !sM1.isEmpty() ? (EdgeNode) sM1.get(sM1.lastKey())
                : null;
    }

    final static void addInChains(final SortedMap<Integer, Object> sM1, final SortedMap<Integer, Object> sM2, final int y) {
        while (!sM2.isEmpty()
                && (sM2.firstKey().intValue() < y)) {
            for (final Iterator it2 = ((List) sM2.remove(sM2.firstKey()))
                    .iterator(); it2.hasNext();) {
                final EdgeNode eN = (EdgeNode) it2.next();
                final Integer iN = new Integer(eN.xMin + eN.yMax - eN.yMin
                        + 1);
                SortedMap<Integer, Object> sM3 = sM1.headMap(iN);
                if (!sM3.isEmpty()) {
                    if (((EdgeNode) sM3.get(sM3.lastKey())).score >= eN.score) {
						continue;
					}
                } else {
                    if (eN.score <= 0) {
						continue;
					}
                }
                sM3 = sM1.tailMap(iN);
                while (!sM3.isEmpty()) {
                    final EdgeNode eN2 = (EdgeNode) sM3.get(sM3.firstKey());
                    if (eN2.score <= eN.score) {
						sM3.remove(sM3.firstKey());
					} else {
						break;
					}
                }
                sM1.put(new Integer(iN.intValue() - 1), eN);
            }
        }
    }

    final static String[] turnOffBSDP(String[] command) {
        command = command.clone();
        final int i = Array.indexOf(command, "--gappedextension");
        command[i + 1] = "false";
        return command;
    }

    final static boolean isGappedExtensionOn(final String[] command) {
        final int i = Array.indexOf(command, "--gappedextension");
        return (i != Integer.MAX_VALUE)
                && command[i + 1].equals("true");
    }

    final static Generator exonerateCigarGenerator(
            final String[] command) {
        final List l = new LinkedList();
        try {
            ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
            if (ExternalExecution.runSaveOutput(command, bAOS, null) != 0) {
                if (!Chains.isGappedExtensionOn(command)) {
					throw new IllegalStateException();
				}
                Chains.logger
                        .info("Exonerate failed, gapped extension is on, trying BSDP instead");
                bAOS.close();
                bAOS = new ByteArrayOutputStream();
                final String[] command2 = Chains.turnOffBSDP(command);
                if (ExternalExecution.runSaveOutput(command2, bAOS,
                        null) != 0) {
					throw new IllegalStateException();
				}
            }
            bAOS.flush();
            final LineNumberReader lNR = new LineNumberReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            bAOS.toByteArray())));
            Object o;
            while ((o = CigarParser_Generator.parseCigar(lNR)) != null) {
				l.add(o);
			}
            lNR.close();
            bAOS.close();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
        return Generators.iteratorGenerator(l.iterator());
    }

    final static List<List> exonerateAlignment(final String[] command) {
        final Generator gen = Chains.exonerateCigarGenerator(command);
        final List<List> l = new LinkedList<List>();
        CigarParser_Generator.Cigar c;
        while ((c = (CigarParser_Generator.Cigar) gen.gen()) != null) {
            if ((c.queryStrand == CigarParser_Generator.Cigar.PLUS)
                    && (c.targetStrand == CigarParser_Generator.Cigar.PLUS)) {
                final List l2 = (List) IterationTools.append(
                        new GeneratorIterator(CigarParser_Generator
                                .convertToEdgeList(c)),
                        new LinkedList());
                int totalMatches = 0;
                for (final Iterator it = l2.iterator(); it.hasNext();) {
                    final PolygonFiller.Node l3 = (PolygonFiller.Node) it
                            .next();
                    totalMatches += l3.yMax - l3.y + 1;
                }
                for (final Iterator it = l2.iterator(); it.hasNext();) {
                    final PolygonFiller.Node l3 = (PolygonFiller.Node) it
                            .next();
                    l3.z = 100 * c.score / totalMatches;
                    if (Debug.DEBUGCODE && (l3.z <= 0)) {
						throw new IllegalStateException(c.score + " "
                                + totalMatches);
					}
                }
                l.add(l2);
            }
        }
        return l;
    }

    public final static int[] countAlignments(final byte[] bA, final byte[] bA2,
            final List diagonals) {
        int a = 0;
        int c = 0;
        int g = 0;
        int t = 0;
        for (final Iterator it = diagonals.iterator(); it.hasNext();) {
            final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
            int i = n.y;
            final int j = n.yMax;
            int k = n.x;
            while (i <= j) {
                if (bA2[i] == bA[k]) {
                    switch (bA2[i]) {
                    case 'A':
                    case 'a':
                        a++;
                        break;
                    case 'C':
                    case 'c':
                        c++;
                        break;
                    case 'G':
                    case 'g':
                        g++;
                        break;
                    case 'T':
                    case 't':
                        t++;
                        break;
                    }
                }
                i++;
                k++;
            }
        }
        return new int[] { a, c, g, t };
    }

    // expects log(0) to be set to zero to avoid NaN
    public static float relativeEntropy(final int a, final int c, final int g, final int t,
            final float[] logs) {
        final int total = a + c + g + t;
        if (total == 0) {
			return 1;
		}
        if (total < logs.length) {
            return -((logs[a] * a + logs[c] * c + logs[g] * g + logs[t]
                    * t)
                    / total - logs[total])
                    / logs[4]; // logs(4) not strictly needed
        }
        return -((Chains.getTimesLogExcludingZero(a)
                + Chains.getTimesLogExcludingZero(c)
                + Chains.getTimesLogExcludingZero(g) + Chains.getTimesLogExcludingZero(t))
                / total - Maths.log(total))
                / logs[4];
    }

    public static final float getTimesLogExcludingZero(final int a) {
        return a == 0 ? 0 : Maths.log(a) * a;
    }

    public final static LocalAligner rescoreAlignments(
            final LocalAligner aligner,
            final float relativeEntropyThreshold) {
        return new LocalAligner() {
            float[] logs = new float[300];
            {
                for (int i = 1; i < this.logs.length; i++) {
                    this.logs[i] = Maths.log(i);
                }
            }

            public List<List> align(final byte[] bA, final byte[] bA2, final int startX,
                    final int lengthX, final int startY, final int lengthY) {
                final List<List> l = aligner.align(bA, bA2, startX, lengthX,
                        startY, lengthY);
                for (final ListIterator<List> it = l.listIterator(); it.hasNext();) {
                    final List l2 = it.next();
                    final int[] iA = Chains.countAlignments(bA, bA2, l2);
                    final float rE = Chains.relativeEntropy(iA[0], iA[1], iA[2],
                            iA[3], this.logs);
                    if (rE < relativeEntropyThreshold) {
                        it.remove();
                        continue;
                    }
                    for (final Iterator it2 = l2.iterator(); it2.hasNext();) {
                        final PolygonFiller.Node n = (PolygonFiller.Node) it2
                                .next();
                        n.z *= rE;
                    }
                }
                return l;
            }
        };
    }

    public final static Aligner alignerConvertor(
            final LocalAligner aligner) {
        return new Aligner() {
            public Iterator align(final byte[] bA, final byte[] bA2, final int startX,
                    final int lengthX, final int startY, final int lengthY) {
                final List<List> l = aligner.align(bA, bA2, startX, lengthX,
                        startY, lengthY);
                final List l2 = new LinkedList();
                for (final Iterator<List> it = l.iterator(); it.hasNext();) {
					l2.addAll(it.next());
				}
                Collections.sort(l2);
                return l2.iterator();
            }
        };
    }

    /**
     * Takes two string arguments which are fasta file names and runs exonerate
     * on them, returning a edgeList generator to the alignment.
     */
    public final static LocalAligner makeExonerateAlignment(
            final String[] commands) {
        return new LocalAligner() {
            String[] commandsPlusFiles = new String[commands.length + 2];

            File f;

            File f2;
            {
                try {
                    this.f = File.createTempFile("temp", ".fa");
                    this.f2 = File.createTempFile("temp", ".fa");
                    this.f.deleteOnExit();
                    this.f2.deleteOnExit();
                } catch (final IOException e) {
                    throw new IllegalStateException();
                }
                System.arraycopy(commands, 0, this.commandsPlusFiles, 0,
                        commands.length);
            }

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#fn(java.lang.Object,
             *      java.lang.Object)
             */
            public final List<List> align(final byte[] bA, final byte[] bA2,
                    final int startX, final int lengthX, final int startY, final int lengthY) {
                Chains.fastaSubSeq(bA, startX, lengthX, "one", this.f);
                Chains.fastaSubSeq(bA2, startY, lengthY, "two", this.f2);
                this.commandsPlusFiles[commands.length] = this.f.toString();
                this.commandsPlusFiles[commands.length + 1] = this.f2
                        .toString();
                final List<List> l = Chains.exonerateAlignment(this.commandsPlusFiles);
                for (final Iterator<List> it = l.iterator(); it.hasNext();) {
                    PolygonFiller.transformEdgeList(it.next(),
                            startX, startY);
                }
                if (!this.f.delete() || !this.f2.delete()) {
					throw new IllegalStateException();
				}
                return l;
            }
        };
    }

    public interface Aligner {
        Iterator align(byte[] bA, byte[] bA2, int startX,
                int lengthX, int startY, int lengthY);
    }

    public interface LocalAligner {
        // produces sublists lists of edges
        List<List> align(byte[] bA, byte[] bA2, int startX, int lengthX,
                int startY, int lengthY);
    }

    public static final void fastaSubSeq(final byte[] bA, final int start,
            final int length, final String title, final File f) {
        try {
            final OutputStream oS = new BufferedOutputStream(
                    new FileOutputStream(f));
            FastaOutput_Procedure_Int.writeFile(oS, title, bA, start,
                    length);
            oS.close();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    public static final void divideScoresByLengths(Node m) {
        while (m != null) {
            m.score = m.score / (m.xE - m.xS + 1);
            m.n.score = m.score;
            m = m.p;
        }
    }

    public static final Generator convertNodeChainToEdgeList(
            final Node m) {
        return new Generator() {
            Node n = m;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (this.n != null) {
                    final PolygonFiller.Node l = new PolygonFiller.Node(
                            this.n.xS, this.n.n.xS, this.n.n.xE, this.n.score);
                    this.n = this.n.p;
                    return l;
                }
                return null;
            }
        };
    }

    public interface CloseSpan {
        void pro(PrimeConstraints pC, int i, int j, int pX, int pY,
                int x, int y);
    }

    public static void closeGapsEliminateSpan(final Iterator it,
            final PrimeConstraints pC, final int i, final int j, final int maxGap,
            final CloseSpan closeSpan) {
        if (!it.hasNext()) {
			return;
		}
        PolygonFiller.Node n = (PolygonFiller.Node) it.next();
        int pX = n.x + n.yMax - n.y;
        int pY = n.yMax;
        while (it.hasNext()) {
            n = (PolygonFiller.Node) it.next();
            Chains.doGap(pC, i, j, pX, pY, n.x, n.y, maxGap, closeSpan);
            pX = n.x + n.yMax - n.y;
            pY = n.yMax;
        }
    }

    static void doGap(final PrimeConstraints pC, final int i, final int j, final int pX,
            final int pY, final int x, final int y, final int maxGap, final CloseSpan closeSpan) {
        if (x - pX - 1 > maxGap) {
			closeSpan.pro(pC, i, j, pX, pY, x, y);
		} else {
            if (y - pY - 1 > maxGap) {
				closeSpan.pro(pC, j, i, pY, pX, y, x);
			}
        }
    }
    
    static int noOfLongGapOptimisations = 0;

    static CloseSpan getMinimumPath(final List<List<Object>> paths,
            final int borderGap, final Aligner aligner,
            final byte[] bA, final byte[] bA2) {
        return new CloseSpan() {
            public void pro(final PrimeConstraints pC, final int i, final int j,
                    final int pX, final int pY, final int x, final int y) {
                Chains.noOfLongGapOptimisations++;
                if (i < j) {
                    paths.add(Chains.getMinimumPath(pC, i, j, pX, pY, x, y,
                            borderGap, aligner, bA, bA2));
                } else {
                    paths.add(Chains.getMinimumPath(pC, i, j, pX, pY, x, y,
                            borderGap, aligner, bA2, bA));
                }
            }
        };
    }

    static CloseSpan fillInPath(final Iterator paths) {
        return new CloseSpan() {
            public void pro(final PrimeConstraints pC, final int i, final int j,
                    final int pX, final int pY, final int x, final int y) {
                Chains.fillInPath(pC, (List) paths.next());
            }
        };
    }

    /**
     * Inclusive coordinates for starts and ends.
     * 
     * @param pC
     * @param i
     * @param j
     * @param xMin
     * @param yMin
     * @param xMax
     * @param yMax
     * @param aligner
     * @param maxBorderGap
     * @param bA
     * @param bA2
     * @return
     */
    static int partitionSequences(final PrimeConstraints pC, final int i, final int j,
            final int xMin, final int yMin, final int xMax, final int yMax, final Aligner aligner,
            final int maxBorderGap, final byte[] bA, final byte[] bA2) {
        if (Debug.DEBUGCODE && (maxBorderGap * 2 >= yMax - yMin)) {
			throw new IllegalStateException(xMin + " " + xMax + " "
                    + yMin + " " + yMax + " " + maxBorderGap);
		}
        final List l = Chains.localAlignmentsInGaps(Generators.constant(null),
                aligner, 1, maxBorderGap * 2, xMin + 1, yMin + 1,
                xMax - xMin, yMax - yMin, bA, bA2);
        Node m = Chains.convertEdgeListToNodeChain(Generators
                .iteratorGenerator(l.iterator()));
        m = pC.filterByConstraints(i, j, m);
        m = Chains.sortOppositeChain(m);
        m = pC.filterByConstraints(j, i, m);
        Chains.multiplyScoresByLengths(m);
        m = Chains.getHighestChain(m);
        // partition on the y axis
        final int k = (yMax - yMin) / 2 + yMin;
        int p = xMin;
        int q = xMax;
        while (m != null) {
            if (m.xE >= k) {
                if (m.xS <= k) {
                    p = m.n.xS + k - m.xS;
                    q = p;
                } else {
                    q = m.n.xS;
                }
                break;
            }
            p = m.n.xE;
            m = m.p;
        }
        return p + ((q - p) / 2) - xMin;
    }

    static int[] drawInPoints(final PrimeConstraints pC, final int i, final int j,
            int xMin, int yMin, int xMax, int yMax, final int borderGap,
            final Aligner aligner, final byte[] bA, final byte[] bA2) {
        final int xG = xMax - xMin;
        int lXG;
        int rXG;
        if (xG >= borderGap * 2) {
            lXG = borderGap;
            rXG = borderGap;
        } else {
            lXG = Chains.partitionSequences(pC, i, j, xMin, yMin, xMax,
                    yMax, aligner, borderGap, bA, bA2);
            rXG = xG - lXG;
        }
        final int yG = yMax - yMin;
        int lYG;
        int rYG;
        if (yG >= borderGap * 2) {
            lYG = borderGap;
            rYG = borderGap;
        } else {
            lYG = Chains.partitionSequences(pC, j, i, yMin, xMin, yMax,
                    xMax, aligner, borderGap, bA2, bA);
            rYG = yG - lYG;
        }
        ConstraintNode cN = PrimeConstraints.getPrimeConstraint(
                pC.primeConstraints[i][j], xMin + lXG);
        ConstraintNode cN2 = PrimeConstraints.getPrimeConstraint(
                pC.primeConstraints[j][i], yMin + lYG);
        int cNY = cN.y
                + (cN.maxOffset != Integer.MIN_VALUE ? cN.maxOffset
                        : -1);
        int cN2X = cN2.y
                + (cN2.maxOffset != Integer.MIN_VALUE ? cN2.maxOffset
                        : -1);
        if (xMin + lXG + cNY < yMin + lYG + cN2X) {
            xMin += lXG;
            yMin = cNY <= yMin + lYG ? cNY : (yMin + lYG);
        } else {
            yMin += lYG;
            xMin = cN2X <= xMin + lXG ? cN2X : (xMin + lXG);
        }

        cN = PrimeConstraints.getRightMostPointAffectedByConstraint(
                pC.primeConstraints[j][i], xMax - rXG);
        cN2 = PrimeConstraints.getRightMostPointAffectedByConstraint(
                pC.primeConstraints[i][j], yMax - rYG);
        cNY = cN.x
                + (cN.maxOffset != Integer.MIN_VALUE ? cN.maxOffset
                        : 0);
        cN2X = cN2.x
                + (cN2.maxOffset != Integer.MIN_VALUE ? cN2.maxOffset
                        : 0);
        if (xMax - rXG + cNY >= yMax - rYG + cN2X) {
            xMax -= rXG;
            yMax = (cNY >= yMax - rYG) ? cNY : (yMax - rYG);
        } else {
            yMax -= rYG;
            xMax = (cN2X >= xMax - rXG) ? cN2X : (xMax - rXG);
        }
        return new int[] { xMin, yMin, xMax, yMax };
    }

    static List<Object> getMinimumPath(final PrimeConstraints pC, final int i, final int j,
            int xMin, int yMin, int xMax, int yMax, final int borderGap,
            final Aligner aligner, final byte[] bA, final byte[] bA2) {
        // minus ones because ignore top edge
        final int[] iA = Chains.drawInPoints(pC, i, j, xMin, yMin, xMax - 1,
                yMax - 1, borderGap, aligner, bA, bA2);
        xMin = iA[0];
        yMin = iA[1];
        xMax = iA[2];
        yMax = iA[3];
        final List<Object> l = new LinkedList<Object>();
        Chains.getMinimumPath(pC, i, j, xMin + 1, xMax, yMax, l);
        final List<Object> l2 = new LinkedList<Object>();
        Chains.getMinimumPath(pC, j, i, yMin + 1, yMax, xMax, l2);
        if (l2.size() < l.size()) {
            l2.add(0, new int[] { j, i, yMin, xMin });
            return l2;
        }
        l.add(0, new int[] { i, j, xMin, yMin });
        return l;
    }

    static void getMinimumPath(final PrimeConstraints pC, final int i, final int j,
            final int xMin, final int xMax, final int yMax, final List<Object> l) {
        // get point to the left and
        final ConstraintNode cN = PrimeConstraints.getPrimeConstraint(
                pC.primeConstraints[i][j], xMin);
        if (cN.y > yMax) {
            l.add(new Integer(yMax));
            if (xMin > xMax) {
				return;
			}
            Chains.getMinimumPath(pC, j, i, yMax + 1, yMax, xMax, l);
        } else {
            l.add(new Integer(cN.y - 1));
            Chains.getMinimumPath(pC, j, i, cN.y, yMax, xMax, l);
        }
    }

    static void fillInPath(final PrimeConstraints pC, final List l) {
        final Iterator it = l.iterator();
        final int[] iA = (int[]) it.next();
        final int i = iA[0];
        final int j = iA[1];
        final int x = iA[2];
        final int y = iA[3];
        pC.updatePrimeConstraints(i, j, x, y + 1, Integer.MIN_VALUE);
        if (it.hasNext()) {
			Chains.fillInPath(pC, it, i, j, x + 1);
		}
    }

    static void fillInPath(final PrimeConstraints pC, final Iterator it, final int i,
            final int j, final int x) {
        final int y = ((Integer) it.next()).intValue();
        pC.updatePrimeConstraints(j, i, y, x, Integer.MIN_VALUE);
        if (it.hasNext()) {
			Chains.fillInPath(pC, it, j, i, y + 1);
		}
    }

    public static void transformConstraintsAndCloseLargeGaps(
            final PrimeConstraints pC, final List[][] chains, final int diagonalGap,
            final int maxGap, final int borderGap, final Aligner aligner,
            final byte[][] sequences, final int[][] pairOrdering) {
        if (Debug.DEBUGCODE && (borderGap * 2 > maxGap)) {
			throw new IllegalStateException();
		}
        final List[][] lAA2 = new List[pC.sequenceNumber][pC.sequenceNumber];
        for (final int[] iA : pairOrdering) {
            final int i = iA[0];
            final int j = iA[1];
            lAA2[i][j] = new LinkedList();
            CloseSpan closeSpan = Chains.getMinimumPath(lAA2[i][j],
                    borderGap, aligner, sequences[i], sequences[j]);
            Chains.closeGapsEliminateSpan(chains[i][j].iterator(),
                    pC, i, j, maxGap, closeSpan);
            closeSpan = Chains.fillInPath(lAA2[i][j].iterator());
            Chains.closeGapsEliminateSpan(chains[i][j].iterator(),
                    pC, i, j, maxGap, closeSpan);
        }
        Chains.transformConstraints(diagonalGap, pC);
        for (int i = 0; i < pC.sequenceNumber; i++) {
			for (int j = i + 1; j < pC.sequenceNumber; j++) {
                final CloseSpan closeSpan = Chains.fillInPath(lAA2[i][j]
                        .iterator());
                Chains.closeGapsEliminateSpan(
                        chains[i][j].iterator(), pC, i, j, maxGap,
                        closeSpan);
            }
		}
    }
}