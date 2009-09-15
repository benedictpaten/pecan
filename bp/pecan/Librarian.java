/*
 * Created on Oct 1, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.logging.Logger;

import bp.common.ds.Array;
import bp.common.ds.IntCrapHash;
import bp.common.ds.IntStack;
import bp.common.ds.ScrollingQueue_Int;
import bp.common.ds.wrappers.ObjectNode;
import bp.common.fp.Function;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Function_Int_3Args;
import bp.common.fp.Generator;
import bp.common.fp.Generator_Int;
import bp.common.fp.IterationTools;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_Int_2Args;
import bp.common.fp.Procedure_Int_5Args;
import bp.common.fp.Procedure_NoArgs;
import bp.common.io.Debug;
import bp.common.maths.Maths;

/**
 * @author benedictpaten
 */
public final class Librarian {
    static final Logger logger = Logger.getLogger(Pecan.class
            .getName());

    public static final int sum(final int i, final int j) {
        return Float.floatToRawIntBits(Float.intBitsToFloat(i)
                + Float.intBitsToFloat(j));
    }
    
    public static final boolean greaterThanOrEqual(final int i, final int j) {
        return Float.intBitsToFloat(i) >= Float.intBitsToFloat(j);
    }

    public static final int multiply(final int i, final int j) {
        return Float.floatToRawIntBits(Float.intBitsToFloat(i)
                * Float.intBitsToFloat(j));
    }

    public static final int convertWeight(final int i) {
        return Float.floatToRawIntBits(Maths.exp(Float
                .intBitsToFloat(i)));
    }

    public interface WeightsGetter {
        int fn(int i, int iSeq, int jSeq, int[] iA, int startsIndex);
    }
    
    static final int sumWeights(int[] iA, int start, int end) {
    	if(start < end) {
    		int j = iA[start+1];
    		for(int i=start+3; i<end; i+=2) {
    			j = sum(iA[i], j);
    		}
    		return j;
    	}
    	return Integer.MIN_VALUE;
    }
    
    public static final WeightsGetter weightsGetter_FilterByGapThreshold(
            final PairsHeap[][] sQA, final float gamma) {
        return new WeightsGetter() {
        	int iGamma = Float.floatToRawIntBits(gamma);
        	
        	int convertToThreshold(int i) {
        		return multiply(Float.floatToRawIntBits(1.0f - Float.intBitsToFloat(i)), iGamma);
        	}
        	
            public final int fn(final int i, final int iSeq, final int jSeq, final int[] iA,
                    final int startsIndex) {
                int end = sQA[iSeq][jSeq].get(i, startsIndex, iA);
                int k = startsIndex;
                int threshold = convertToThreshold(sumWeights(iA, startsIndex, end));
                PairsHeap sQ = sQA[jSeq][iSeq];
                for(int j=startsIndex; j<end; j+=2) {
                	if(greaterThanOrEqual(iA[j+1], threshold)) { 
                		int end2 = sQ.get(iA[j], end, iA);
                		int threshold2 = convertToThreshold(sumWeights(iA, end, end2));
                		if(greaterThanOrEqual(iA[j+1], threshold2)) {
                			iA[k++] = iA[j];
                			iA[k++] = iA[j+1];
                		}
                	}
                }
                return k;
            }
        };
    }

    public static final WeightsGetter weightsGetter(
            final PairsHeap[][] sQA) {
        return new WeightsGetter() {
            public final int fn(final int i, final int iSeq, final int jSeq, final int[] iA,
                    final int startsIndex) {
                return sQA[iSeq][jSeq].get(i, startsIndex, iA);
            }
        };
    }

    /*
     * public static final WeightsGetter weightsGetter_Consistency_DivideBySum(
     * final PairsHeap[][] sQA, final WeightsGetter weightsGetter) { return new
     * WeightsGetter() { public int fn(int i, int iSeq, int jSeq, int[] iA, int
     * startsIndex) { int j = weightsGetter.fn(i, iSeq, jSeq, iA, startsIndex);
     * for(int k=startsIndex; k<j; k+=2) { int total =
     * Float.floatToIntBits(14); for(int k1=0; k1<sQA[iSeq].length; k1++) if(k1 !=
     * iSeq && k1 != jSeq) total = sum(total, sQA[iSeq][k1].sumTotal(i,
     * Float.floatToIntBits(0.0f))); for(int k1=0; k1<sQA[jSeq].length; k1++)
     * if(k1 != jSeq && k1 != iSeq) total = sum(total,
     * sQA[jSeq][k1].sumTotal(iA[k], Float.floatToIntBits(0.0f))); iA[k+1] =
     * Float.floatToRawIntBits(Float.intBitsToFloat(iA[k+1])/Float.intBitsToFloat(total/2)); }
     * return j; } }; }
     */

    public static final WeightsGetter weightsGetter_Consistency(
            final PairsHeap[][] sQA) {
        return new WeightsGetter() {
            int[] iA2 = new int[300];

            IntCrapHash iH = new IntCrapHash(500);

            Function_Int_2Args fn = new Function_Int_2Args() {
                public int fn(int i, int j) {
                    return Librarian.sum(i, j);
                }
            };

            final int multiple = Float
                    .floatToIntBits(sQA.length > 2 ? sQA.length - 2
                            : 1);

            public final int fn(final int i, final int iSeq, final int jSeq, final int[] iA,
                    final int startsIndex) {
                final PairsHeap[] sQ = sQA[iSeq];
                int j = sQ[jSeq].get(i, 0, iA);
                for (int k = 0; k < j; k += 2) {
					this.iH
                            .put(iA[k],
                                    Librarian.multiply(iA[k + 1], this.multiple),
                                    null);
				}
                for (int k = 0; k < sQ.length; k++) {
                    if ((k != iSeq) && (k != jSeq)) {
                        j = sQ[k].get(i, 0, iA);
                        for (int l = 0; l < j; l += 2) {
                            final int m = iA[l];
                            final int n = iA[l + 1];
                            final int o = sQA[k][jSeq].get(m, 0, this.iA2);
                            for (int p = 0; p < o; p += 2) {
                                final int q = Librarian.multiply(n, this.iA2[p + 1]);
                                this.iH.put(this.iA2[p], q, this.fn);
                            }
                        }
                    }
                }
                j = this.iH.getEntries(iA);
                this.iH.clear();
                // int j = sQ[jSeq].get(i, iA);
                return j;
            }
        };
    }

    /***************************************************************************
     * public static final WeightsGetter weightsGetter( final PairsHeap[][]
     * sQAI, final PairsHeap[][] sQA_Secret) { return new WeightsGetter() {
     * public final int fn(int i, int iSeq, int jSeq, int[] iA) { int tot =
     * Float.floatToIntBits(0.0f); for (int k = 0; k < sQA_Secret[iSeq].length;
     * k++) { if (k != iSeq && k != jSeq) { int l =
     * sQA_Secret[iSeq][k].sumTotal(i, Float .floatToIntBits(0.0f)); tot =
     * Librarian.sum(l, tot); } } float f = Float.intBitsToFloat(tot); i =
     * sQAI[iSeq][jSeq].get(i, iA); for (int k = 0; k < i; k += 2) { int tot2 =
     * Float.floatToIntBits(0.0f); for (int l = 0; l < sQA_Secret[jSeq].length;
     * l++) { if (l != iSeq && l != jSeq) tot2 =
     * Librarian.sum(sQA_Secret[jSeq][l] .sumTotal(iA[k], Float
     * .floatToIntBits(0.0f)), tot2); } float f2 = 8 + (f +
     * Float.intBitsToFloat(tot2)) / 2; f2 = Float.intBitsToFloat(iA[k + 1]) /
     * f2; if (f2 > 1.0) throw new IllegalStateException(); iA[k + 1] =
     * Float.floatToIntBits(f2); } return i; } }; }
     **************************************************************************/

    /**
     * Same argument format as {@link WeightsGetter}.
     * 
     * @param sQAI
     * @param nullValue
     * @return
     */
    public static final Function_Int_3Args weightsTotal(
            final PairsHeap[][] sQA, final int nullValue) {
        return new Function_Int_3Args() {
            public final int fn(final int i, final int iSeq, final int jSeq) {
                return sQA[iSeq][jSeq].sumTotal(i, nullValue);
            }
        };
    }

    public static final Function_Int_3Args getMaxResidue(
            final ScrollingQueue_Int[][] queues) {
        return new Function_Int_3Args() {
            public final int fn(final int i, final int j, final int k) {
                return queues[j][k].get(i);
            }
        };
    }

    /*
     * public static final Function_Int_3Args getMaxResidue( final PairsHeap[][]
     * heaps, final ScrollingQueue_Int[][] queues) { return new
     * Function_Int_3Args() { WeightsGetter wG =
     * weightsGetter_Consistency(heaps);
     * 
     * int[] iA = new int[10000];
     * 
     * public final int fn(int i, int j, int k) { int y = wG.fn(i, j, k, iA, 0);
     * int z = Integer.MIN_VALUE; for (int x = 0; x < y; x += 2) { if (iA[x] >
     * z) z = iA[x]; } z = z == Integer.MIN_VALUE ? Integer.MAX_VALUE : z; int
     * z2 = queues[j][k].get(i); if (z != z2) throw new IllegalStateException(z + " " +
     * z2); return z; } }; }
     */

    static public void coordinateAlignment(
            final Function[][] alignmentPairs, final Generator cutPoints,
            final Procedure_Int[] finishedOffsets,
            final PairsHeap[][] sQAA, final int sequenceNumber,
            final IntStack iS, final int diagonalGapBetweenRelations,
            final Procedure_Int_2Args[][] maxOffsetsPro) {
        final int[][][] iAAA = Librarian.iAAAMake(sequenceNumber);
        final TransitiveDependencies tD = new TransitiveDependencies(
                sequenceNumber, iAAA, Librarian.completedOffsets(
                        sequenceNumber, iAAA,
                        new Procedure_Int_2Args() {
                            public final void pro(int i, int j) {
                                finishedOffsets[i].pro(j);
                            }
                        }));
        for (int i = 0; i < sequenceNumber; i++) {
			for (int j = i + 1; j < sequenceNumber; j++) {
                tD.updateTransitiveDependencies(i, j, sQAA[i][j]
                        .firstIndex() - 1);
                tD.updateTransitiveDependencies(j, i, sQAA[j][i]
                        .firstIndex() - 1);
            }
		}
        final Procedure[][] pAA = Librarian.coordinateAlignment(sQAA, sequenceNumber,
                tD, diagonalGapBetweenRelations, iS);
        Librarian.coordinatePairs(cutPoints, alignmentPairs, pAA, tD, iS,
                maxOffsetsPro);
        if (Debug.DEBUGCODE) {
			tD.checkEmpty();
		}
    }

    static void coordinatePairs(final Generator cutPoints,
            final Function[][] alignmentGens,
            final Procedure[][] libraryFunctions,
            final Librarian.TransitiveDependencies tD, final IntStack iS,
            final Procedure_Int_2Args[][] maxOffsetsPro) {
        final int[][][] iAAA = tD.getSequenceOffsets();
        Chains.CutPoint cutPoint;
        while ((cutPoint = (Chains.CutPoint) cutPoints.gen()) != null) {
            final boolean lTP = cutPoint.tB == Integer.MAX_VALUE;
            if (lTP) {
				cutPoint.tB = 0;
			}
            final int[] dC = (int[]) alignmentGens[cutPoint.s1][cutPoint.s2]
                    .fn(cutPoint);
            final int xFinishedUpto = (lTP || (iAAA[cutPoint.s1][cutPoint.s2][cutPoint.s2] == dC[6])) ? dC[6]
                    : dC[6] - 1;
            final int yFinishedUpto = (lTP || (iAAA[cutPoint.s2][cutPoint.s1][cutPoint.s1] == dC[5])) ? dC[5]
                    : dC[5] - 1;
            libraryFunctions[cutPoint.s1][cutPoint.s2].pro(new int[] {
                    xFinishedUpto, yFinishedUpto });
            if (Debug.DEBUGCODE && !iS.empty()) {
				throw new IllegalArgumentException();
			}
            maxOffsetsPro[cutPoint.s1][cutPoint.s2].pro(dC[4],
                    xFinishedUpto);
            maxOffsetsPro[cutPoint.s2][cutPoint.s1].pro(dC[7],
                    yFinishedUpto);
            tD.updateTransitiveDependencies(cutPoint.s1, cutPoint.s2,
                    xFinishedUpto);
            tD.updateTransitiveDependencies(cutPoint.s2, cutPoint.s1,
                    yFinishedUpto);
        }
    }

    static public Procedure[][] coordinateAlignment(
            final PairsHeap[][] sQAA, final int sequenceNumber,
            final TransitiveDependencies tD,
            final int diagonalGapBetweenRelations, final IntStack iS) {
        final Procedure[][] pAA = new Procedure[sequenceNumber][sequenceNumber];
        for (int i = 0; i < sequenceNumber; i++) {
			for (int j = i + 1; j < sequenceNumber; j++) {
                final Procedure_NoArgs pro = Librarian.processAlignment_NoConsistency(
                        sQAA[i], i, j, iS);
                pAA[i][j] = Librarian.coordinateAlignment(i, j, sQAA[i],
                        sQAA[j], tD, diagonalGapBetweenRelations, pro);
            }
		}
        return pAA;
    }

    static Procedure coordinateAlignment(final int i, final int j,
            final PairsHeap[] sQ1, final PairsHeap[] sQ3,
            final TransitiveDependencies tD,
            final int diagonalGapBetweenRelations,
            final Procedure_NoArgs processAlignment) {
        return new Procedure() {
            int kI = sQ1[j].firstIndex() - 1;

            int lI = sQ3[i].firstIndex() - 1;

            int kJ = this.lI;

            int lJ = this.kI;

            public void pro(final Object o) {
                final int[] finishedUpto = (int[]) o;
                final int xFinishedUpto = finishedUpto[0];
                final int yFinishedUpto = finishedUpto[1];
                // actual alignment process
                processAlignment.pro();
                {
                    final int[] iA5 = Librarian.createRelationships(tD, sQ1[j], this.kI,
                            xFinishedUpto, i, j, this.lI, this.kI + this.lI - 1,
                            diagonalGapBetweenRelations);
                    this.kI = xFinishedUpto + 1;
                    this.lI = iA5[0];
                    tD.insertRelationship(j, i, this.lI, xFinishedUpto);
                }
                {
                    final int[] iA5 = Librarian.createRelationships(tD, sQ3[i], this.kJ,
                            yFinishedUpto, j, i, this.lJ, this.kJ + this.lJ - 1,
                            diagonalGapBetweenRelations);
                    this.kJ = yFinishedUpto + 1;
                    this.lJ = iA5[0];
                    tD.insertRelationship(i, j, this.lJ, yFinishedUpto);
                }
            }
        };
    }

    static Procedure_Int_2Args updateMaxOffsets(final int i,
            final int j, final ScrollingQueue_Int[][] sQAA,
            final Procedure_Int[][] gens,
            final WeightsGetter weightsGetter, final int[] scratchA,
            final int sequenceNumber) {
        return new Procedure_Int_2Args() {
            final ScrollingQueue_Int sQIJ = sQAA[i][j];

            final ScrollingQueue_Int[] sQAJ = sQAA[j];

            final Procedure_Int genIJ = gens[i][j];

            final Procedure_Int[] gensJ = gens[j];

            int k = this.sQIJ.firstIndex();

            public void pro(final int l, final int l2) {
                this.genIJ.pro(l + 1);
                while (this.k <= l) {
                    final int offset = this.k;
                    final int n = this.getWeights(offset, i, j, scratchA, 0);
                    if (n != 0) {
                        final int o = scratchA[0];
                        int p = this.sQIJ.get(this.k);
                        if ((p == Integer.MAX_VALUE) || (o > p)) {
							this.sQIJ.set(this.k, o);
						}
                        for (int q = 0; q < sequenceNumber; q++) {
                            if ((q != i) && (q != j)) {
                                final int u = this.getWeights(offset, i, q,
                                        scratchA, n);
                                if (u != n) {
                                    final int r = scratchA[n];
                                    ScrollingQueue_Int sQ = this.sQAJ[q];
                                    this.gensJ[q].pro(o + 1);
                                    for (int s = 0; s < n; s += 2) {
                                        final int t = scratchA[s];
                                        p = sQ.get(t);
                                        if ((p == Integer.MAX_VALUE)
                                                || (r > p)) {
											sQ.set(t, r);
										}
                                    }
                                    gens[q][j].pro(r + 1);
                                    sQ = sQAA[q][j];
                                    for (int s = n; s < u; s += 2) {
                                        final int t = scratchA[s];
                                        p = sQ.get(t);
                                        if ((p == Integer.MAX_VALUE)
                                                || (o > p)) {
											sQ.set(t, o);
										}
                                    }
                                }
                            }
                        }
                    }
                    this.k++;
                }
                this.k = l2 + 1;
            }

            int getWeights(final int i, final int iSeq, final int jSeq, final int[] scratchA,
                    final int offset) {
                final int j = weightsGetter.fn(i, iSeq, jSeq, scratchA,
                        offset);
                return j;
            }
        };
    }

    static int[] createRelationships(final TransitiveDependencies tD,
            final PairsHeap sQ, int i, final int j, final int iSeq, final int jSeq, int k,
            int l, final int diagonalGapBetweenRelations) {
        if (sQ.firstIndex() > i) {
			i = sQ.firstIndex();
		}
        while (i <= j) {
            final int m = sQ.getRightmostPoint(i);
            if ((m != Integer.MAX_VALUE) && (m > k)) {
				k = m;
			}
            if (i + k >= l + diagonalGapBetweenRelations) {
                // place relationship
                tD.insertRelationship(jSeq, iSeq, k, i);
                l = i + k;
            }
            i++;
        }
        return new int[] { k, l };
    }

    static final class FreeMemory implements Procedure_Int_2Args,
            Procedure_Int {
        int[][] iAA;

        int[] iA;

        PairsHeap[][] sQA;

        int seqNo;

        public FreeMemory(final PairsHeap[][] sQA,
                final int[][] seqStartsAndEnds, final int seqNo) {
            this.iAA = new int[seqNo][seqNo];
            this.iA = new int[seqNo];
            for (int i = 0; i < seqNo; i++) {
                final int j = seqStartsAndEnds[i][0] - 1;
                this.iA[i] = j;
                Arrays.fill(this.iAA[i], j);
            }
            this.sQA = sQA;
            this.seqNo = seqNo;
        }

        public final void pro(final int i, final int j) {
            if (Debug.DEBUGCODE && (this.iAA[i][i] > j)) {
				throw new IllegalStateException(this.iAA[i][i] + " " + j);
			}
            this.iAA[i][i] = j;
        }

        public final void pro(final int i) {
            final int[] iA2 = this.iAA[i];
            final int j = iA2[i];
            final PairsHeap[] sQ = this.sQA[i];
            for (int k = 0; k < iA2.length; k++) {
                if (k != i) {
                    int l = iA2[k] + 1;
                    final PairsHeap pH = sQ[k];
                    final int m = this.iAA[k][k];
                    while (l <= j) {
                        final int n = pH.getRightmostPoint(l);
                        if ((n != Integer.MAX_VALUE) && (n > m)) {
							break;
						}
                        l++;
                    }
                    iA2[k] = l - 1;
                }
            }
            final int l = Array.getMin(iA2);
            if (l > this.iA[i]) {
                this.iA[i] = l;
                for (int n = 0; n < i; n++) {
					sQ[n].tryToRemoveUpto(l + 1);
				}
                for (int n = i + 1; n < this.seqNo; n++) {
					sQ[n].tryToRemoveUpto(l + 1);
				}
            }
        }
    }

    static Procedure_NoArgs processAlignment_NoConsistency(
            final PairsHeap[] sQ1, final int iSeq, final int jSeq,
            final Generator_Int gen2) {
        return new Procedure_NoArgs() {
            public void pro() {
                final Generator_Int gen = gen2;// SparseAlign.sparseAlignBoth(gen2,
                                            // Pecan.seqGets12[iSeq],
                                            // Pecan.seqGets12[jSeq],
                                            // SparseAlign.logGapFn(),//doubleAffineGapFn(),
                // Pecan.seqStartsAndEnds12[iSeq][0]-1,
                // Pecan.seqStartsAndEnds12[jSeq][0]-1,
                // Pecan.seqStartsAndEnds12[iSeq][1],
                // Pecan.seqStartsAndEnds12[jSeq][1]);
                int i;
                while ((i = gen.gen()) != Integer.MAX_VALUE) {
                    final int j = gen.gen();
                    final int k = Librarian.convertWeight(gen.gen());
                    sQ1[jSeq].add(i, j, k);
                }
            }
        };
    }

    static int[][] iAAMake(final int seqNo) {
        final int[][] iAA = new int[seqNo][seqNo];
        for (int i = 0; i < iAA.length; i++) {
            final int[] iA2 = iAA[i];
            Arrays.fill(iA2, Integer.MIN_VALUE);
            iA2[i] = Integer.MAX_VALUE;
        }
        return iAA;
    }

    static int[] iAMake(final int seqNo) {
        final int[] iA = new int[seqNo];
        Arrays.fill(iA, Integer.MIN_VALUE);
        return iA;
    }

    static Procedure_Int_5Args completedOffsets(final int seqNo,
            final int[][][] iAAA, final Procedure_Int_2Args pro) {
        return new Procedure_Int_5Args() {
            int[][] iAA = Librarian.iAAMake(seqNo);

            int[] iA = Librarian.iAMake(seqNo);

            public void pro(final int i, int j, final int k, final int l, final int m) {
                if (this.iAA[i][j] == l) {
                    final int n = Array.getMin(iAAA[i][j]);
                    if (this.iAA[i][j] == this.iA[i]) {
                        this.iAA[i][j] = n;
                        j = this.iA[i];
                        this.iA[i] = Array.getMin(this.iAA[i]);
                        if (this.iA[i] > j) {
							pro.pro(i, this.iA[i]);
						}
                    } else {
						this.iAA[i][j] = n;
					}
                }
            }
        };
    }

    static int[][][] iAAAMake(final int seqNo) {
        final int[][][] iAAA = new int[seqNo][seqNo][];
        for (int i = 0; i < seqNo; i++) {
            for (int j = i + 1; j < seqNo; j++) {
                final int[] iA = (iAAA[i][j] = new int[seqNo]);
                final int[] iA2 = (iAAA[j][i] = new int[seqNo]);
                Arrays.fill(iA, Integer.MIN_VALUE);
                Arrays.fill(iA2, Integer.MIN_VALUE);
                iA[i] = Integer.MAX_VALUE;
                iA2[j] = Integer.MAX_VALUE;
            }
        }
        return iAAA;
    }

    static class TransitiveDependencies {
        private static final int COORDINATE = 0;

        private static final int OTHERSEQCOORDINATE = 1;

        private final int seqNo;

        private final int[][][] iAAA;

        private final ObjectNode[][] oNAA;

        private final ObjectNode[][][] oNAAA;

        final Procedure_Int_5Args pro;

        public TransitiveDependencies(final int seqNo, final int[][][] iAAA,
                final Procedure_Int_5Args pro) {
            this.seqNo = seqNo;
            this.iAAA = iAAA;
            this.pro = pro;
            this.oNAA = new ObjectNode[seqNo][seqNo];
            for (int i = 0; i < this.oNAA.length; i++) {
                final ObjectNode[] oNA = this.oNAA[i];
                for (int j = 0; j < oNA.length; j++) {
					if (i != j) {
						oNA[j] = new ObjectNode();
					}
				}
            }
            this.oNAAA = new ObjectNode[seqNo][seqNo][seqNo];
            for (int i = 0; i < seqNo; i++) {
                for (int j = 0; j < seqNo; j++) {
                    if (i != j) {
                        final ObjectNode[] oNA = this.oNAAA[i][j];
                        for (int k = 0; k < seqNo; k++) {
							if (k != j) {
								oNA[k] = this.oNAA[i][k];
							}
						}
                    }
                }
            }
        }

        public final void checkEmpty() {
            for (int i = 0; i < this.oNAAA.length; i++) {
                for (int j = 0; j < this.oNAAA[i].length; j++) {
                    for (int k = 0; k < this.oNAAA[i][j].length; k++) {
                        if ((this.oNAAA[i][j][k] != null)
                                && (this.oNAAA[i][j][k].pointer != null)) {
                            throw new IllegalStateException(
                                    i
                                            + " "
                                            + j
                                            + " "
                                            + k
                                            + " "
                                            + this.oNAAA[i][j][k]
                                            + " "
                                            + IterationTools
                                                    .join(
                                                            (int[]) this.oNAAA[i][j][k].pointer.o,
                                                            " "));
                        }
                    }
                }
            }
            for (final ObjectNode[] element : this.oNAA) {
                for (ObjectNode element0 : element) {
                    if ((element0 != null)
                            && (element0.pointer != null)) {
						throw new IllegalStateException();
					}
                }
            }
        }

        public final void updateTransitiveDependencies(final int i, final int j,
                final int k) {
            final int m = this.iAAA[i][j][j];
            this.iAAA[i][j][j] = k;
            this.pro.pro(i, j, j, m, k);
            TransitiveDependencies.updateTransitiveDependencies1(this.seqNo, this.iAAA, this.pro, this.oNAAA, i,
                    j, k);
            TransitiveDependencies.updateTransitiveDependencies2(this.seqNo, this.pro, this.iAAA, this.oNAAA, i,
                    j);
        }

        public void insertRelationship(final int i, final int j, final int k, final int l) {
            final ObjectNode oN = new ObjectNode(new int[] { k, l }, null);
            final ObjectNode[] oNA = this.oNAA[i];
            oNA[j].pointer = oN;
            oNA[j] = oN;
        }

        private static final void updateTransitiveDependencies2(
                final int seqNo, final Procedure_Int_5Args pro, final int[][][] iAAA,
                final ObjectNode[][][] oNAAA, final int i, final int j) {
            final int[][] iAA2 = iAAA[i];
            final int[][] iAA3 = iAAA[j];
            final ObjectNode[][] oNAA = oNAAA[i];
            for (int m = 0; m < seqNo; m++) {
                if ((m != i) && (m != j)) {
                    final ObjectNode[] oNA = oNAA[m];
                    ObjectNode oN = oNA[j];
                    final int n = iAA2[m][m];
                    while (oN.pointer != null) {
                        final int[] iA = (int[]) oN.pointer.o;
                        if (n >= iA[TransitiveDependencies.COORDINATE]) {
                            final int o = iAA3[m][i];
                            iAA3[m][i] = iA[TransitiveDependencies.OTHERSEQCOORDINATE];
                            pro.pro(j, m, i, o,
                                    iA[TransitiveDependencies.OTHERSEQCOORDINATE]);
                            oN = oN.pointer;
                            oNA[j] = oN;
                        } else {
							break;
						}
                    }
                }
            }
        }

        private static final void updateTransitiveDependencies1(
                final int seqNo, final int[][][] iAAA, final Procedure_Int_5Args pro,
                final ObjectNode[][][] oNAAA, final int i, final int j, final int k) {
            final ObjectNode[] oNA = oNAAA[i][j];
            // track through other sequences
            for (int l = 0; l < seqNo; l++) {
                if ((l != i) && (l != j)) {
                    ObjectNode oN = oNA[l];
                    while (oN.pointer != null) {
                        final int[] relationship = (int[]) oN.pointer.o;
                        if (relationship[TransitiveDependencies.COORDINATE] <= k) {
                            final int m = iAAA[l][j][i];
                            iAAA[l][j][i] = relationship[TransitiveDependencies.OTHERSEQCOORDINATE];
                            pro.pro(l, j, i, m,
                                    relationship[TransitiveDependencies.OTHERSEQCOORDINATE]);
                            oN = oN.pointer;
                            oNA[l] = oN;
                        } else {
							break;
						}
                    }
                }
            }
        }

        public int[][][] getSequenceOffsets() {
            return this.iAAA;
        }
    }
}