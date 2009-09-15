/*
 * Created on Oct 21, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.Iterator;

import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Function;
import bp.common.fp.Function_2Args;
import bp.common.fp.Function_Int;
import bp.common.fp.Generator;
import bp.common.fp.Generator_Int;
import bp.common.io.NewickTreeParser;

/**
 * @author benedictpaten
 */
public class Ancestor {

    public static final Generator_Int convertToAncestor(
            final Generator alignment,
            final NewickTreeParser.Node tree, final int reservedValue) {
        return new Generator_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final int gen() {
                final int[] iA = (int[]) alignment.gen();
                if (iA == null) {
					return reservedValue;
				}
                return Ancestor.getMostProbableBase(iA, tree);
            }
        };
    }

    static int getMostProbableBase(final int[] column,
            final NewickTreeParser.Node tree) {
        return column[0];
    }

    static float[][] convertArray(final double[][] dAA) {
        final float[][] fAA = new float[dAA.length][];
        for (int i = 0; i < dAA.length; i++) {
            fAA[i] = Ancestor.convertArray(dAA[i]);
        }
        return fAA;
    }

    static float[] convertArray(final double[] dA) {
        final float[] fA = new float[dA.length];
        for (int i = 0; i < dA.length; i++) {
			fA[i] = (float) dA[i];
		}
        return fA;
    }

    public static class FelsensteinsMostProbBases implements Function {
        private final MutableInteger mI;

        private final float[][] fAA;

        private final float[] fAScratch;

        private float[][][] branchSubMatrices;

        private final NewickTreeParser.Node tree;

        /**
         *  
         */
        public FelsensteinsMostProbBases(final NewickTreeParser.Node tree,
                final SubstitutionMatrixGenerator sMG, final int alphabetSize) {
            final int i = FelsensteinsMostProbBases.countNodes(tree);
            this.mI = new MutableInteger();
            this.fAA = new float[i][alphabetSize];
            this.branchSubMatrices = new float[i][][];
            FelsensteinsMostProbBases.getSubMatrices(0, tree, sMG, this.branchSubMatrices);
            this.tree = tree;
            this.fAScratch = new float[alphabetSize];
        }

        static int getSubMatrices(int i, final NewickTreeParser.Node n,
                final SubstitutionMatrixGenerator sMG,
                final float[][][] branchSubMatrices) {
            branchSubMatrices[i++] = Ancestor.convertArray(sMG
                    .getMatrix(n.edgeLength));
            for (final Iterator<Object> it = n.getNodes().iterator(); it.hasNext();) {
				i = FelsensteinsMostProbBases.getSubMatrices(i, (NewickTreeParser.Node) it
                        .next(), sMG, branchSubMatrices);
			}
            return i;
        }

        static int countNodes(final NewickTreeParser.Node n) {
            int i = 0;
            for (final Iterator<Object> it = n.getNodes().iterator(); it.hasNext();) {
                i += FelsensteinsMostProbBases.countNodes((NewickTreeParser.Node) it.next());
            }
            return i + 1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bp.common.fp.Function#fn(java.lang.Object)
         */
        public Object fn(final Object o) {
            this.mI.i = 0;
            Ancestor.felsensteinsForwardPass((float[][]) o, 0, this.mI, this.fAA,
                    this.fAScratch, this.branchSubMatrices, this.tree);
            return this.fAA[0].clone();
        }
    }

    static int felsensteinsForwardPass(final float[][] column, int index,
            final MutableInteger mI, final float[][] fAA, final float[] fAScratch,
            final float[][][] branchSubMatrices, final NewickTreeParser.Node tree) {
        final float[] fA = fAA[index++];
        final Iterator<Object> it = tree.getNodes().iterator();
        if (it.hasNext()) {
            float[] fA2 = fAA[index];
            float[][] branchSubMatrix = branchSubMatrices[index];
            index = Ancestor.felsensteinsForwardPass(column, index, mI, fAA,
                    fAScratch, branchSubMatrices,
                    (NewickTreeParser.Node) it.next());
            Ancestor.scaleByEdgeLength(fA2, fAScratch, branchSubMatrix);
            System.arraycopy(fAScratch, 0, fA, 0, fAScratch.length);
            //scale probabilities
            //branch
            while (it.hasNext()) {
                fA2 = fAA[index];
                branchSubMatrix = branchSubMatrices[index];
                index = Ancestor.felsensteinsForwardPass(column, index, mI,
                        fAA, fAScratch, branchSubMatrices,
                        (NewickTreeParser.Node) it.next());
                Ancestor.scaleByEdgeLength(fA2, fAScratch, branchSubMatrix);
                for (int j = 0; j < fAScratch.length; j++) {
                    fA[j] *= fAScratch[j];
                }
            }
        } else {
            System.arraycopy(column[mI.i++], 0, fA, 0, fA.length);
        }
        return index;
    }

    static void scaleByEdgeLength(final float[] fA, final float[] fA2,
            final float[][] fAA) {
        Arrays.fill(fA2, 0);
        for (int j = 0; j < fA.length; j++) {
            final float f = fA[j];
            final float[] fA3 = fAA[j];
            for (int k = 0; k < fA3.length; k++) {
                fA2[k] += fA3[k] * f;
            }
        }
    }

    public static interface SubstitutionMatrixGenerator {
        double[][] getMatrix(double time);
    }

    public static final int A = 0, C = 1, G = 2, T = 3, GAP = 4,
            N = 4;

    /**
     * Felsenstein F84 model with gaps added.
     * 
     * transition rate equal See.. 'Models of Sequence Evolution for DNA
     * Sequences Containing Gaps Grainne McGuire, David Balding'
     * 
     * @param aP
     * @return
     */
    static double[][] f84WithGaps(final double aPi, final double cPi, final double gPi,
            final double tPi, final double gapPi, double alpha, double gamma,
            final double time) {
        final double[][] dAA = new double[5][5];
        final double eGT = Math.exp(-gamma * time);
        final double m1EGT = 1 - eGT;
        final double eGTm1EAT = eGT * (1 - Math.exp(-alpha * time));
        final double eAGT = Math.exp(-(alpha + gamma) * time);
        final double rPi = aPi + gPi;
        final double yPi = cPi + tPi;
        final double[] dA = new double[] { m1EGT * aPi, m1EGT * cPi,
                m1EGT * gPi, m1EGT * tPi, m1EGT * gapPi };
        for (int i = 0; i < 5; i++) {
            final double[] dA2 = dAA[i];
            System.arraycopy(dA, 0, dA2, 0, 5);
        }
        dAA[Ancestor.A][Ancestor.A] += eAGT + eGTm1EAT * aPi / rPi;
        dAA[Ancestor.C][Ancestor.C] += eAGT + eGTm1EAT * cPi / yPi;
        dAA[Ancestor.G][Ancestor.G] += eAGT + eGTm1EAT * gPi / rPi;
        dAA[Ancestor.T][Ancestor.T] += eAGT + eGTm1EAT * tPi / yPi;

        dAA[Ancestor.GAP][Ancestor.GAP] += eGT;

        dAA[Ancestor.A][Ancestor.G] += eGTm1EAT * gPi / rPi;
        dAA[Ancestor.G][Ancestor.A] += eGTm1EAT * aPi / rPi;

        dAA[Ancestor.C][Ancestor.T] += eGTm1EAT * tPi / yPi;
        dAA[Ancestor.T][Ancestor.C] += eGTm1EAT * cPi / yPi;

        return dAA;
    }

    /**
     * Felsenstein F84 model
     * 
     * See.. 'Models of Sequence Ebolution for DNA
     * Sequences Containing Gaps Grainne McGuire, David Balding'
     * 
     */
    static double[][] f84(final double aPi, final double cPi, final double gPi,
            final double tPi, double alpha, double gamma, final double time) {
        final double[][] dAA = new double[4][4];
        final double eGT = Math.exp(-gamma * time);
        final double m1EGT = 1 - eGT;
        final double eGTm1EAT = eGT * (1 - Math.exp(-alpha * time));
        final double eAGT = Math.exp(-(alpha + gamma) * time);
        final double rPi = aPi + gPi;
        final double yPi = cPi + tPi;
        final double[] dA = new double[] { m1EGT * aPi, m1EGT * cPi,
                m1EGT * gPi, m1EGT * tPi };
        for (int i = 0; i < 4; i++) {
            final double[] dA2 = dAA[i];
            System.arraycopy(dA, 0, dA2, 0, 4);
        }
        dAA[Ancestor.A][Ancestor.A] += eAGT + eGTm1EAT * aPi / rPi;
        dAA[Ancestor.C][Ancestor.C] += eAGT + eGTm1EAT * cPi / yPi;
        dAA[Ancestor.G][Ancestor.G] += eAGT + eGTm1EAT * gPi / rPi;
        dAA[Ancestor.T][Ancestor.T] += eAGT + eGTm1EAT * tPi / yPi;

        dAA[Ancestor.A][Ancestor.G] += eGTm1EAT * gPi / rPi;
        dAA[Ancestor.G][Ancestor.A] += eGTm1EAT * aPi / rPi;

        dAA[Ancestor.C][Ancestor.T] += eGTm1EAT * tPi / yPi;
        dAA[Ancestor.T][Ancestor.C] += eGTm1EAT * cPi / yPi;

        return dAA;
    }

    public static SubstitutionMatrixGenerator f84WithGaps(
            final double aPi, final double cPi, final double gPi,
            final double tPi, final double gapPi, final double alpha,
            final double gamma) {
        return new SubstitutionMatrixGenerator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.Ancestor.SubstitutionMatrixGenerator#getMatrix(double)
             */
            public double[][] getMatrix(final double time) {
                return Ancestor.f84WithGaps(aPi, cPi, gPi, tPi, gapPi, alpha,
                        gamma, time);
            }
        };
    }

    public static SubstitutionMatrixGenerator f84(final double aPi,
            final double cPi, final double gPi, final double tPi,
            final double alpha, final double gamma) {
        return new SubstitutionMatrixGenerator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.Ancestor.SubstitutionMatrixGenerator#getMatrix(double)
             */
            public double[][] getMatrix(final double time) {
                return Ancestor.f84(aPi, cPi, gPi, tPi, alpha, gamma, time);
            }
        };
    }

    public static SubstitutionMatrixGenerator f81WithGaps(final double aPi,
            final double cPi, final double gPi, final double tPi, final double gapPi,
            final double gamma) {
        return Ancestor.f84WithGaps(aPi, cPi, gPi, tPi, gapPi, 0, gamma);
    }

    public static SubstitutionMatrixGenerator jukesCantorWithGaps(
            final double lamda) {
        return Ancestor.f81WithGaps(0.2, 0.2, 0.2, 0.2, 0.2, lamda * 5);
    }

    public static SubstitutionMatrixGenerator k2PWithGaps(
            final double transitionRate, final double tranversionRate) {
        final double gamma = tranversionRate * 5.0;
        final double alpha = (transitionRate - tranversionRate) * 2.0;
        return Ancestor.f84WithGaps(0.2, 0.2, 0.2, 0.2, 0.2, alpha, gamma);
    }

    public static SubstitutionMatrixGenerator f81(final double aPi,
            final double cPi, final double gPi, final double tPi, final double gamma) {
        return Ancestor.f84(aPi, cPi, gPi, tPi, 0, gamma);
    }

    public static SubstitutionMatrixGenerator jukesCantor(final double lamda) {
        return Ancestor.f81(0.25, 0.25, 0.25, 0.25, lamda * 4);
    }

    public static SubstitutionMatrixGenerator k2P(
            final double transitionRate, final double tranversionRate) {
        final double gamma = tranversionRate * 5.0;
        final double alpha = (transitionRate - tranversionRate) * 2.0;
        return Ancestor.f84(0.25, 0.25, 0.25, 0.25, alpha, gamma);
    }

    /**
     * Two state jukes cantor
     * @param gamma
     * @return
     */
    public static SubstitutionMatrixGenerator repeat(final double gamma) {
        return new SubstitutionMatrixGenerator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.Ancestor.SubstitutionMatrixGenerator#getMatrix(double)
             */
            public double[][] getMatrix(final double time) {
                final double d = 0.5*(1 + Math.exp(-2*gamma*time));
                final double d2 = 0.5*(1 - Math.exp(-2*gamma*time));
                return new double[][] { { d, d2 },
                        { d2, d } };
            }
        };
    }

    public static Function isAncestorRepeat(
            final NewickTreeParser.Node tree, final int seqNo) {
        final Ancestor.FelsensteinsMostProbBases fMPB2 = new Ancestor.FelsensteinsMostProbBases(
                tree, Ancestor.repeat(1), 2);
        final float[][] fAA2 = new float[seqNo][2];
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object fn(final Object o) {
                final int[] iA = (int[]) o;
                for (int i = 0; i < iA.length; i++) {
                    final float[] fA2 = fAA2[i];
                    final char j = (char) iA[i];
                    if (j == '-') {
                        fA2[0] = (float) 0.5;
                        fA2[1] = (float) 0.5;
                    } else if (Character.isLowerCase(j) || (j == 'N')) {
                        fA2[0] = 0;
                        fA2[1] = 1;
                    } else {
                        fA2[0] = 1;
                        fA2[1] = 0;
                    }
                }
                return fMPB2.fn(fAA2);
            }
        };
    }

    public static Function felsensteinsMostProbBases_DNAMSAColumn(
            final Ancestor.FelsensteinsMostProbBases fMPB, final int seqNo,
            final Function_Int translateChars) {
        final float[][] fAA = new float[seqNo][5];
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object fn(final Object o) {
                final int[] iA = (int[]) o;
                for (int i = 0; i < iA.length; i++) {
                    final float[] fA = fAA[i];
                    switch (translateChars.fn(iA[i])) {
                    case 0:
                        fA[0] = 1;
                        fA[1] = 0;
                        fA[2] = 0;
                        fA[3] = 0;
                        fA[4] = 0;
                        break;
                    case 1:
                        fA[0] = 0;
                        fA[1] = 1;
                        fA[2] = 0;
                        fA[3] = 0;
                        fA[4] = 0;
                        break;
                    case 2:
                        fA[0] = 0;
                        fA[1] = 0;
                        fA[2] = 1;
                        fA[3] = 0;
                        fA[4] = 0;
                        break;
                    case 3:
                        fA[0] = 0;
                        fA[1] = 0;
                        fA[2] = 0;
                        fA[3] = 1;
                        fA[4] = 0;
                        break;
                    case 4:
                        fA[0] = (float) 0.25;
                        fA[1] = (float) 0.25;
                        fA[2] = (float) 0.25;
                        fA[3] = (float) 0.25;
                        fA[4] = 0;
                        break;
                    default:
                        if (iA[i] != '-') {
							throw new IllegalStateException();
						}
                        fA[0] = 0;
                        fA[1] = 0;
                        fA[2] = 0;
                        fA[3] = 0;
                        fA[4] = 1;
                    }
                }
                return fMPB.fn(fAA);
            }
        };
    }

    public static int convertToMostProbableResidue(final float[] fA,
            final int fALength, final byte[] bA) {
        float f = fA[0];
        int j = 0;
        int k = 1;
        for (int i = 1; i < fALength; i++) {
			if (fA[i] > f) {
                f = fA[i];
                j = i;
            } else {
                if (fA[i] == f) {
					k++;
				}
            }
		}
        if ((j == 0) && (k == fALength)) {
			return 'N';
		}
        return bA[j];
    }

    public static Function_2Args convertToMostProbableDNAResidue_WithRepeats(
            final boolean keepAncestorInserts) {
        return new Function_2Args() {
            byte[] bA = new byte[] { 'A', 'C', 'G', 'T', '-' };

            byte[] bARepeat = new byte[] { 'a', 'c', 'g', 't', '-' };

            int fASize = keepAncestorInserts ? 4 : 5;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator_Int#gen()
             */
            public Object fn(final Object o, final Object o2) {
                final float[] fA = (float[]) o;
                final float[] fA2 = (float[]) o2;
                if (fA2[0] < fA2[1]) {
					//is repeat
                    return new Integer(Ancestor.convertToMostProbableResidue(
                            fA, this.fASize, this.bARepeat));
				}
                return new Integer(Ancestor.convertToMostProbableResidue(fA,
                        this.fASize, this.bA));
            }
        };
    }
}