/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Apr 6, 2005
 */
package bp.pecan;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.Array;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Function_Int_3Args;
import bp.common.fp.Functions_Int_2Args;
import bp.common.fp.IterationTools;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Predicate_Double_2Args;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_Int_3Args;
import bp.common.io.NewickTreeParser;

/**
 * @author benedictpaten
 */
public class AlignmentPumpTest
                              extends TestCase {
    // Function_Index getWeightsForResidues;
    Librarian.WeightsGetter getWeightsForResidues;

    int[][] seqStartAndEnds;

    List[] seqs;

    Procedure output;

    final List lOutput = new ArrayList();

    Function_Int_2Args adder;

    DripAligner.Add dripAdder;

    Predicate_Double_2Args greaterThan;

    int defaultNodeValue;

    public AlignmentPumpTest(final String s) {
        super(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        this.seqStartAndEnds = new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 },
                { 4, 5 }, { 5, 6 } };
        this.seqs = new List[] {
                Arrays.asList(new Object[] { new int[] { 1 } }),
                Arrays.asList(new Object[] { new int[] { 2 } }),
                Arrays.asList(new Object[] { new int[] { 3 } }),
                Arrays.asList(new Object[] { new int[] { 4 } }),
                Arrays.asList(new Object[] { new int[] { 5 } }) };
        this.getWeightsForResidues = new Librarian.WeightsGetter() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.Librarian.WeightsGetter#fn(int, int,
             *      int)
             */
            public int fn(final int i, final int iSeq, final int jSeq, final int[] iA, final int startsIndex) {
                iA[0] = jSeq + 1;
                iA[1] = 1;
                return 2;
            }
        };
        /*
         * getWeightsForResidues = new Function_Index() {
         * 
         * public Object fn(int i) { int[] iA = new int[8]; int k = 0; for (int
         * j = 1; j < 6; j++) { if (j != i) { iA[k++] = j; iA[k++] = 1; } }
         * return iA; } };
         */
        this.lOutput.clear();
        this.output = new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public void pro(final Object o) {
                IterationTools.append(((List) o).iterator(), AlignmentPumpTest.this.lOutput);
            }
        };
        this.adder = Functions_Int_2Args.sum();
        this.dripAdder = new DripAligner.Add() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.DripAligner.Add#fn(double, int)
             */
            public double fn(final double d, final int i) {
                return Double.longBitsToDouble(Double
                        .doubleToLongBits(d)
                        + i);
            }
        };
        this.greaterThan = new Predicate_Double_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_Int_2Args#test(int, int)
             */
            public boolean test(final double i, final double j) {
                return Double.doubleToLongBits(i) > Double
                        .doubleToLongBits(j);
            }
        };
        this.defaultNodeValue = 0;
        super.setUp();
    }

    public void testGetAlignmentPump() {
        // create sequence containing all the positions
        this.lOutput.clear();
        final Procedure[] proA = AlignmentPump.getAlignmentPump(
                new int[] { 0 },
                new int[] { 1 },
                this.seqStartAndEnds[0],
                this.seqStartAndEnds[1],
                this.output,
                new Librarian.WeightsGetter() {
                    public int fn(int i, int iSeq, int jSeq, int[] iA, int startsIndex) {
                        int j = AlignmentPumpTest.this.getWeightsForResidues.fn(i, iSeq,
                                jSeq, iA, 0);
                        Array.reverseArray(iA, j);
                        for (int k = 0; k < j; k += 2) {
                            int l = iA[k];
                            iA[k] = iA[k + 1];
                            iA[k + 1] = l;
                        }
                        return j;
                    };
                },
                new Function_Int_3Args() {
                    public int fn(int i, int j, int k) {
                        int[] iA = new int[100];
                        int l = AlignmentPumpTest.this.getWeightsForResidues.fn(i, j, k, iA, 0);
                        if (l > 0) {
							return iA[l - 2];
						}
                        return Integer.MAX_VALUE;
                    }
                }, (int) (Math.random() * 100), this.adder, this.dripAdder,
                this.greaterThan, Double
                        .longBitsToDouble(this.defaultNodeValue),
                new Procedure_Int_3Args() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Procedure_Int_3Args#pro(int, int, int)
                     */
                    public void pro(int i, int j, int k) {
                        // do nothing
                    }
                }, new int[10000]);
        proA[0].pro(this.seqs[0]);
        proA[1].pro(this.seqs[1]);
        Assert.assertTrue(this.lOutput.size() == 1);
        final Object[] oA = (Object[])this.lOutput.get(0);
        Assert.assertTrue(Arrays.equals((int[]) oA[0], new int[] { 1 }));
        Assert.assertTrue(Arrays.equals((int[]) oA[1], new int[] { 2 }));
    }

    public void testGetAlignmentPumps() {
        final String[] trees = new String[] { "(1, 2, 3, 4, 5);",
                "((1, 2), 3, (4:1, 5:1));",
                "((1, (2, 3)), (4:1, 5:1));",
                "(((1, 2), (3, 4:1):1), 5:1);",
                "((((1, 2), 3), 4:1), 5:1);" };
        final Object[] outputs = new Object[] {
                new Object[] {
                        new int[] { 1 },
                        new Object[] {
                                new int[] { 2 },
                                new Object[] {
                                        new int[] { 3 },
                                        new Object[] {
                                                new int[] { 4 },
                                                new int[] { 5 } }, }, }, },
                new Object[] {
                        new Object[] { new int[] { 1 },
                                new int[] { 2 } },
                        new Object[] {
                                new int[] { 3 },
                                new Object[] { new int[] { 4 },
                                        new int[] { 5 } } } },
                new Object[] {
                        new Object[] {
                                new int[] { 1 },
                                new Object[] { new int[] { 2 },
                                        new int[] { 3 } } },
                        new Object[] { new int[] { 4 },
                                new int[] { 5 } } },
                new Object[] {
                        new Object[] {
                                new Object[] { new int[] { 1 },
                                        new int[] { 2 } },
                                new Object[] { new int[] { 3 },
                                        new int[] { 4 } } },
                        new int[] { 5 } },
                new Object[] {
                        new Object[] {
                                new Object[] {
                                        new Object[] {
                                                new int[] { 1 },
                                                new int[] { 2 } },
                                        new int[] { 3 } },
                                new int[] { 4 } }, new int[] { 5 } } };

        // Map seqIdsToStartEndsMap;
        for (int i = 0; i < trees.length; i++) {
            final NewickTreeParser nTP = new NewickTreeParser(
                    NewickTreeParser.tokenise(new StringReader(
                            trees[i])));
            final Procedure[] proA = new Procedure[5];

            AlignmentPump.getAlignmentPumps(this.output,
                    new Librarian.WeightsGetter() {
                        public int fn(final int i, final int iSeq, final int jSeq,
                                final int[] iA, final int startsIndex) {
                            final int j = AlignmentPumpTest.this.getWeightsForResidues.fn(i, iSeq,
                                    jSeq, iA, 0);
                            Array.reverseArray(iA, j);
                            for (int k = 0; k < j; k += 2) {
                                final int l = iA[k];
                                iA[k] = iA[k + 1];
                                iA[k + 1] = l;
                            }
                            return j;
                        };
                    }, new Function_Int_3Args() {
                        public int fn(final int i, final int j, final int k) {
                            final int[] iA = new int[100];
                            final int l = AlignmentPumpTest.this.getWeightsForResidues.fn(i, j, k,
                                    iA, 0);
                            if (l > 0) {
								return iA[l - 2];
							}
                            return Integer.MAX_VALUE;
                        }
                    }, this.seqStartAndEnds, proA, 0, nTP.tree, 10, this.adder,
                    this.dripAdder, this.greaterThan, Double
                            .longBitsToDouble(this.defaultNodeValue),
                    new Procedure_Int_3Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Procedure_Int_3Args#pro(int, int,
                         *      int)
                         */
                        public void pro(final int i, final int j, final int k) {
                            // do nothings
                        }
                    }, new int[10000]);

            proA[0].pro(this.seqs[0]);
            proA[1].pro(this.seqs[1]);
            proA[2].pro(this.seqs[2]);
            proA[3].pro(this.seqs[3]);
            proA[4].pro(this.seqs[4]);

            Assert.assertEquals(this.lOutput.size(), 1);
            
            Assert.assertTrue(this.lOutput.size() == 1);
            
            Assert.assertTrue(Array.arraysEqual().test(AlignmentPumpTest.convertToOldFormat(this.lOutput.get(0)),
                    outputs[i], new Predicate_2Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
                         *      java.lang.Object)
                         */
                        public boolean test(final Object o, final Object o2) {
                            return Arrays.equals((int[]) o,
                                    (int[]) o2);
                        }
                    }));
            this.lOutput.clear();
        }
    }
    
    static Object convertToOldFormat(final Object o) {
        if (o instanceof Object[]) {
            final Object[] oA = (Object[]) o;
            return new Object[] { AlignmentPumpTest.convertToOldFormat(oA[0]), AlignmentPumpTest.convertToOldFormat(oA[1]) };
        }
        return o;
    }

    public void testGetSubSequenceRanges() {
        final int[][] seqStartAndEnds = new int[][] { { 0, 1 }, { 2, 3 },
                { 4, 5 } };

        NewickTreeParser nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader(
                        "('1':1.0, '2':1.0, '3':1.0);")));
        int[] iA = AlignmentPump.getStartAndEnds(AlignmentPump
                .getNodeNumber(nTP.tree), seqStartAndEnds, 0);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 1, 2, 3, 4, 5 }));

        nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader(
                        "('1':1.0,('2':1.5,'3':2.0):1.0);")));
        iA = AlignmentPump.getStartAndEnds(AlignmentPump
                .getNodeNumber(nTP.tree), seqStartAndEnds, 0);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 1, 2, 3, 4, 5 }));

        nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader(
                        "(('1':1.0, '2':1.5):1.0,'3':2.0);")));
        iA = AlignmentPump.getStartAndEnds(AlignmentPump
                .getNodeNumber(nTP.tree), seqStartAndEnds, 0);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 1, 2, 3, 4, 5 }));

        nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader("('2':1.0);")));
        iA = AlignmentPump.getStartAndEnds(AlignmentPump
                .getNodeNumber(nTP.tree), seqStartAndEnds, 0);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 1 }));

        nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader("('2':1.0);")));
        iA = AlignmentPump.getStartAndEnds(AlignmentPump
                .getNodeNumber(nTP.tree), seqStartAndEnds, 1);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 2, 3 }));

        nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader("('2':1.0);")));
        iA = AlignmentPump.getStartAndEnds(AlignmentPump
                .getNodeNumber(nTP.tree), seqStartAndEnds, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 4, 5 }));

        nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader("(('1':1.0):1.0);")));
        iA = AlignmentPump.getStartAndEnds(AlignmentPump
                .getNodeNumber(nTP.tree), seqStartAndEnds, 2);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 4, 5 }));
    }

}