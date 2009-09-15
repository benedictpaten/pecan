/*
 * Created on Dec 25, 2005
 */
package bp.pecan.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import bp.common.ds.Array;
import bp.common.ds.FloatStack;
import bp.common.fp.Function_Index;
import bp.common.fp.Function_Index_3Args;
import bp.common.fp.Function_Int;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorTools;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Predicate;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_NoArgs;
import bp.common.io.Debug;
import bp.common.io.InputMunger;
import bp.common.io.NewickTreeParser;
import bp.common.maths.Maths;
import bp.pecan.Cell;
import bp.pecan.Chains;
import bp.pecan.ForwardBackwardMatrixIter;
import bp.pecan.MatrixIterator;
import bp.pecan.PolygonFiller;
import bp.pecan.Cell.CellCalculator;

public final class PairEM {
    static final Logger logger = Logger.getLogger(PairEM.class
            .getName());

    static final String HMM_OUTPUT_FILE = "HMM_OUTPUT_FILE";

    static final String HMM_FILE = "HMM_FILE";

    static final String REPETITIONS = "REPETITIONS";

    static final String PRE_CLOSE_GAPS_LARGER_THAN = "PRE_CLOSE_GAPS_LARGER_THAN";

    static final String OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS = "OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS";

    static final String MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT = "MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT";

    String hMMOutputFile = "output.hmm";

    String hMMFile = "bp/pecan/utils/EMTest.hmm";

    int repetitions = 1;

    public int anchorDiagonalWidth = 10;

    public int minimumDiagonalGapBetweenCutPointAndPolygon = 0;

    public int minimumGapBetweenCutPoints = 500;

    public int maximumDiagonalDistanceBetweenRescalings = 100;

    public int matrixIteratorLineLength = 50000;

    public int preCloseGapsLargerThan = 10000;

    public int overhangingDiagonalsIntoPreClosedGaps = 4500;

    public int minimumSizeOfDiagonalForCutPoint = 4; // plus edge trim of

    public InputMunger setCommandLineArguments(final InputMunger inputMunger) {
        inputMunger
                .noInputString("PairEM : \nFor the given pair of sequences and pairwise HMM, create an alignment using the pre-Pecan chaining routines and then perform iterative Baum Welch");
        inputMunger.addWatch(PairEM.HMM_FILE, 1,
                "Hidden markov model to optimise");
        inputMunger.addWatch(PairEM.HMM_OUTPUT_FILE, 1,
                "Hidden markov outout file,  default : "
                        + this.hMMOutputFile);
        inputMunger.addWatch(PairEM.REPETITIONS, 1,
                "Number of repetitions to perform, default : "
                        + this.repetitions);
        inputMunger.addWatch(PairEM.PRE_CLOSE_GAPS_LARGER_THAN, 1,
                "Pre close gaps larger than this length, default : "
                        + this.preCloseGapsLargerThan);
        inputMunger
                .addWatch(
                        PairEM.OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS,
                        1,
                        "Size of overhanging border (per sequence) into pre-closed gaps, default : "
                                + this.overhangingDiagonalsIntoPreClosedGaps);
        inputMunger
                .addWatch(
                        PairEM.MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT,
                        1,
                        "Size of diagonal sufficient to generate a potential cut point, default : "
                                + this.minimumSizeOfDiagonalForCutPoint);
        return inputMunger;
    }

    public void parseArguments(final InputMunger inputMunger) {
        this.hMMFile = inputMunger.parseValue(this.hMMFile, PairEM.HMM_FILE);
        this.hMMOutputFile = inputMunger.parseValue(this.hMMOutputFile,
                PairEM.HMM_OUTPUT_FILE);
        this.repetitions = inputMunger
                .parseValue(this.repetitions, PairEM.REPETITIONS);
        this.preCloseGapsLargerThan = inputMunger.parseValue(
                this.preCloseGapsLargerThan, PairEM.PRE_CLOSE_GAPS_LARGER_THAN);
        this.overhangingDiagonalsIntoPreClosedGaps = inputMunger
                .parseValue(this.overhangingDiagonalsIntoPreClosedGaps,
                        PairEM.OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS);
        this.minimumSizeOfDiagonalForCutPoint = inputMunger.parseValue(
                this.minimumSizeOfDiagonalForCutPoint,
                PairEM.MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT);
    }

    static final byte[] padSeq(final byte[] bA, final int length, final byte nValue,
            final Function_Int translateChars) {
        final byte[] bA2 = new byte[length + 2];
        bA2[0] = nValue;
        for (int j = 0; j < length; j++) {
            bA2[j + 1] = (byte) translateChars.fn(bA[j]);
        }
        bA2[bA2.length - 1] = nValue;
        return bA2;
    }

    static final Function_Int seqGet(final byte[] bA) {
        return new Function_Int() {
            public int fn(final int x) {
                return bA[x + 1];
            }
        };
    }

    static void writeTransitions(final double[] transitions, final int[] from,
            final int[] to, final NewickTreeParser.Node node, final Map<Long, Double> map) {
        for (int i = 0; i < transitions.length; i++) {
            final Long l = new Long((long) from[i] << 32 | to[i]);
            if (map.containsKey(l)) {
				map.put(l, new Double((transitions[i] + map
                        .get(l).doubleValue()) / 2.0));
			} else {
				map.put(l, new Double(transitions[i]));
			}
        }
    }

    static void writeTransitions(final Map<Long, Double> map, final NewickTreeParser.Node node) {
        int k = 0;
        for (final Iterator<Object> it = node.getNodes().listIterator(3); it
                .hasNext();) {
            final NewickTreeParser.Node n = (NewickTreeParser.Node) it
                    .next();
            int i = 0;
            final int j = n.getNodes().size() - 2;
            while (i < j) {
                NewickTreeParser.Node m = (NewickTreeParser.Node) n
                        .getNodes().get(i);
                final NewickTreeParser.Node o = (NewickTreeParser.Node) m
                        .getNodes().get(0);
                final Long l = new Long((long) k << 32
                        | (int) Double.parseDouble((String) o.o));
                final double d = map.get(l).doubleValue();
                m = (NewickTreeParser.Node) m.getNodes().get(1);
                PairEM.logger.info(" Transition, from :  " + k + " to :  "
                        + (int) Double.parseDouble((String) o.o)
                        + " :: " + d);
                m.o = Double.toString(Maths.log(d));
                i++;
            }
            k++;
        }
    }

    public void runPairEM(final List chains, final Generator chainsRows,
            final Generator chainsColumns, final byte[] seq1, final byte[] seq2,
            final int seqLength1, final int seqLength2, final NewickTreeParser nTP,
            final Map<Long, Double> output) {
        final long startTime = System.currentTimeMillis();

        Cell.isLegitimateHMM(nTP.tree);
        final Object[] program = Cell.createProgram(nTP.tree,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        final float[] startStates = (float[]) program[Cell.STARTSTATES];
        final float[] endStates = (float[]) program[Cell.ENDSTATES];
        final int stateNumber = startStates.length;
        PairEM.logger.info("State number : " + stateNumber);
        final int alphabetSize = ((Number) program[Cell.ALPHABETSIZE])
                .intValue();
        final Function_Int translateChars = (Function_Int) program[Cell.TRANSLATEALPHABETCHAR];
        PairEM.logger.info("Alphabet size : " + alphabetSize);
        final float[] emissions = (float[]) program[Cell.EMISSIONS];
        final int[] programForward = (int[]) program[Cell.PROGRAM];
        final float[] transitionsForward = (float[]) program[Cell.TRANSITIONS];
        Object[] oA = Cell.makeRProgram(programForward,
                transitionsForward, stateNumber);
        final int[] programBackward = (int[]) oA[0];
        int[] from;
        int[] to;
        {
            final int[][] iAA = PairEM.getFromToTransitions(programForward,
                    stateNumber);
            from = iAA[0];
            Array.reverseArray(from, from.length);
            to = iAA[1];
            Array.reverseArray(to, to.length);
        }
        final float[] transitionsBackward = (float[]) oA[1];
        final int[] programForwardL = programForward.clone();
        final int[] programForwardR = programForward.clone();
        Cell.transformProgram(programForwardL,
                -(this.matrixIteratorLineLength + 1), startStates.length);
        Cell.transformProgram(programForwardR,
                this.matrixIteratorLineLength + 1, startStates.length);
        oA = Cell.makeRProgram(programForwardL, transitionsForward,
                startStates.length);
        final int[] programBackwardL = (int[]) oA[0];
        oA = Cell.makeRProgram(programForwardR, transitionsForward,
                startStates.length);
        final int[] programBackwardR = (int[]) oA[0];

        final Cell.GetCellCalculator getForwardsLL = Cell
                .getForwardCellCalculator(programForward,
                        transitionsForward, emissions, alphabetSize);
        final Cell.GetCellCalculator getForwardsL = Cell
                .getForwardCellCalculator(programForwardL,
                        transitionsForward, emissions, alphabetSize);
        final Cell.GetCellCalculator getForwardsR = Cell
                .getForwardCellCalculator(programForwardR,
                        transitionsForward, emissions, alphabetSize);
        final Cell.GetCellCalculator getBackwardsLL = Cell
                .getBackwardCellCalculator(programBackward,
                        transitionsBackward, emissions,
                        startStates.length, alphabetSize);
        final byte nValue = 4;
        final Function_Int seqGetI = PairEM.seqGet(PairEM.padSeq(seq1, seqLength1,
                nValue, translateChars));
        final Function_Int seqGetJ = PairEM.seqGet(PairEM.padSeq(seq2, seqLength2,
                nValue, translateChars));
        final Cell.CellCalculator forwardsLL = getForwardsLL
                .getCellCalculator(seqGetI, seqGetJ);
        final Cell.CellCalculator forwardsL = getForwardsL
                .getCellCalculator(seqGetI, seqGetJ);
        final Cell.CellCalculator forwardsR = getForwardsR
                .getCellCalculator(seqGetI, seqGetJ);
        final Cell.CellCalculator backwardsLL = getBackwardsLL
                .getCellCalculator(seqGetI, seqGetJ);
        final FloatStack fS = new FloatStack(1000000);
        final Cell.CellCalculator backwardsL = PairEM.nextSumBackward(
                programBackwardL, transitionsBackward, emissions,
                startStates.length, alphabetSize, seqGetI, seqGetJ,
                fS);
        final Cell.CellCalculator backwardsR = PairEM.nextSumBackward(
                programBackwardR, transitionsBackward, emissions,
                startStates.length, alphabetSize, seqGetI, seqGetJ,
                fS);

        /*anchorDiagonalWidth = 0;
        Generator chainsRows = PolygonFiller
                .clipBoundariesOfDiagonalList(PolygonFiller
                        .transformEdges(Generators
                                .iteratorGenerator(PolygonFiller
                                        .cloneEdgeList(chainsRow)
                                        .iterator()), anchorDiagonalWidth,
                                -anchorDiagonalWidth), 0,
                        seqLength1, 0, seqLength2);
        Generator chainsColumns = PolygonFiller
                .clipBoundariesOfDiagonalList(PolygonFiller
                        .transformEdges(Generators
                                .iteratorGenerator(PolygonFiller
                                        .cloneEdgeList(chainsColumn)
                                        .iterator()), anchorDiagonalWidth,
                        -anchorDiagonalWidth), 0, seqLength2,
                        0, seqLength1);*/
        final Generator cutPointGenerator = Generators.filter(PolygonFiller
                .cutPointGenerator(Generators
                        .iteratorGenerator(chains.iterator()),
                        this.minimumSizeOfDiagonalForCutPoint,
                        this.minimumSizeOfDiagonalForCutPoint * 10),
                new Predicate() {
                    int pDiagonal = 0;

                    public boolean test(Object o) {
                        int[] iA = (int[]) o;
                        if (iA[0] + iA[1] > this.pDiagonal
                                + PairEM.this.minimumGapBetweenCutPoints) {
                            this.pDiagonal = iA[0] + iA[1];
                            return true;
                        }
                        return false;
                    }
                });
        final EM eM = new EM(from, fS);
        final Function_Index fn = PolygonFiller
                .polygonIteratorWithLessThans(PolygonFiller
                        .polygonIterator(chainsRows, 
                                chainsColumns,
                                0, seqLength1, 0, seqLength2),
                        Generators.constant(null), Generators
                                .constant(null), new int[1000]);
        final MatrixIterator mI = new MatrixIterator(stateNumber,
                this.matrixIteratorLineLength);
        final float[] holdingForwardDiagonal = new float[((this.anchorDiagonalWidth * 2 + 1) * 2 + 1)
                * stateNumber];
        Arrays.fill(holdingForwardDiagonal, Float.NEGATIVE_INFINITY);
        System.arraycopy(startStates, 0, holdingForwardDiagonal,
                startStates.length, startStates.length);
        final float[] forwardDiagonal = new float[this.matrixIteratorLineLength
                * 2 * stateNumber];
        final float[] reverseDiagonal = new float[this.matrixIteratorLineLength
                * 2 * stateNumber];
        final ForwardBackwardMatrixIter fBMI = new ForwardBackwardMatrixIter(
                fn, startStates.length, -1, -1, 0,
                holdingForwardDiagonal, forwardDiagonal,
                reverseDiagonal, endStates, forwardsLL, backwardsLL,
                forwardsL, forwardsR, backwardsL, backwardsR,
                eM.forwards, eM.backwards, eM.passRunningTotal, mI,
                new FloatStack(1000000),
                this.maximumDiagonalDistanceBetweenRescalings, eM.reset);

        final Function_Index_3Args fnFB = ForwardBackwardMatrixIter
                .cutPointAlignmentGenerator(fBMI);
        fn.fn(-2);

        /*{
            if (true) {
                int xStart = 0;
                int yStart = 0;
                Object[] oA2 = (Object[]) fn.fn(xStart + yStart
                        + 1500);
                List one = (List) oA2[0];
                List two = (List) oA2[1];
                for (Iterator it = one.iterator(); it.hasNext();) {
                    PolygonFiller.Node n = (PolygonFiller.Node) it
                            .next();
                    Debug.pl(n + " one ");
                }
                for (Iterator it = two.iterator(); it.hasNext();) {
                    PolygonFiller.Node n = (PolygonFiller.Node) it
                            .next();
                    Debug.pl(n + " two ");
                }
                int xOffset = -100;
                int yOffset = -100;
                PolygonFillerTest.checkPolygon(one, two);
                PolygonFiller.transformEdgeList(one, -xStart
                        - xOffset, -yStart - yOffset);
                PolygonFiller.transformEdgeList(two, -xStart
                        - xOffset, -yStart - yOffset);

                int[] iA = PolygonFillerTest.getDisplayMatrix(900,
                        900);
                PolygonFillerTest.addLines(iA, one, 900, 900,
                        PolygonFillerTest.LEFT_COLOUR);
                PolygonFillerTest.addLines(iA, two, 900, 900,
                        PolygonFillerTest.RIGHT_COLOUR);

                int[] lLTA = (int[]) oA2[3];
                int[] rLTA = (int[]) oA2[4];
                PolygonFillerTest.addPoints(iA, 900, 900, lLTA,
                        xStart + xOffset, yStart + yOffset);
                PolygonFillerTest.addPoints(iA, 900, 900, rLTA,
                        xStart + xOffset, yStart + yOffset);
                PolygonFillerTest.displayMatrix(iA, 900, 900);
            }
        }*/

        int[] cutPoint;
        while ((cutPoint = (int[]) cutPointGenerator.gen()) != null) {
            PairEM.logger.info("Cut point " + cutPoint[0] + " "
                    + cutPoint[1] + " "
                    + this.minimumDiagonalGapBetweenCutPointAndPolygon);
            fnFB.fn(cutPoint[0], cutPoint[1],
                    this.minimumDiagonalGapBetweenCutPointAndPolygon);
        }
        PairEM.logger.info("Final cut point " + (seqLength1 - 1) + " "
                + (seqLength2 - 1));

        fnFB.fn(seqLength1 - 1, seqLength2 - 1, 0);
        PairEM.finishOff(eM.transitionEstimates, from, stateNumber);
        PairEM.writeTransitions(eM.transitionEstimates, from, to, nTP.tree,
                output);
        Debug.pl("Total time taken for PairEM rep "
                + (System.currentTimeMillis() - startTime) / 1000.0);
    }

    public static final CellCalculator nextSumBackward(
            final int[] rProgram, final float[] transitions,
            final float[] emissions, final int stateNumber,
            final int alphabetSize, final Function_Int getX,
            final Function_Int getY, final FloatStack fS) {
        return new CellCalculator() {
            private final int jump = alphabetSize * alphabetSize;

            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(int[], int, int)
             */
            public final void calc(final float[] cells, final int offset, final int x,
                    final int y) {
                PairEM.nextSumBackward(cells, rProgram, transitions,
                        emissions, this.jump,
                        (getX.fn(x + 1) * alphabetSize)
                                + getY.fn(y + 1), offset,
                        stateNumber, fS);
            }
        };
    }

    public static final void nextSumBackward(final float[] cells,
            final int[] program, final float[] transitions,
            final float[] emissions, final int jump, int xyz,
            final int offset, final int stateNumber, final FloatStack fS) {
        Arrays.fill(cells, offset, offset + stateNumber,
                Float.NEGATIVE_INFINITY);
        int i = 1;
        float f;
        while (i < program.length) {
            final float s = cells[program[i] + offset] + emissions[xyz];
            i++;
            xyz += jump;
            cells[program[i] + offset] = Maths.logAdd(
                    cells[program[i] + offset], (f = s
                            + transitions[i]));
            fS.stuff(f);
            i++;
            while (program[i] != Integer.MAX_VALUE) {
                cells[program[i] + offset] = Maths.logAdd(
                        cells[program[i] + offset], (f = s
                                + transitions[i]));
                fS.stuff(f);
                i++;
            }
            i++;
        }
    }

    static int[][] getFromToTransitions(final int[] program, final int stateNumber) {
        final int[] iA = new int[1000];
        final int[] iA2 = new int[1000];
        int j = 0;
        int sN = -1;
        for (final int element : program) {
            if (element != Integer.MAX_VALUE) {
                iA[j] = element % stateNumber;
                iA2[j++] = sN;
            } else {
				sN++;
			}
        }
        final int[] from = new int[j];
        final int[] to = new int[j];
        System.arraycopy(iA, 0, from, 0, j);
        System.arraycopy(iA2, 0, to, 0, j);
        return new int[][] { from, to };
    }

    static void finishOff(final double[] summedTransitions,
            final int[] fromTransitions, final int stateNumber) {
        PairEM.logger.info("Summed transitions : "
                + IterationTools.join(summedTransitions, " ") + "  ");
        final double[] dA2 = new double[stateNumber];
        for (int i = 0; i < fromTransitions.length; i++) {
			if (fromTransitions[i] != Integer.MAX_VALUE) {
				dA2[fromTransitions[i]] += summedTransitions[i];
			}
		}
        for (int i = 0; i < fromTransitions.length; i++) {
			if (fromTransitions[i] != Integer.MAX_VALUE) {
				summedTransitions[i] /= dA2[fromTransitions[i]];
			}
		}
        PairEM.logger.info("Transitions divided through : "
                + IterationTools.join(summedTransitions, " ") + "  ");
    }

    public static final class EM {

        float total;

        final double[] transitionEstimates;

        final int[] from;

        final FloatStack fS;

        public EM(final int[] from, final FloatStack fS) {
            this.transitionEstimates = new double[from.length];
            this.from = from;
            this.fS = fS;
        }

        public final Cell.CellCalculator forwards = new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */

            public final void calc(float[] cells, int offset, int x,
                    int y) {
                for (int i = 0; i < EM.this.from.length; i++) {
                    float f1 = EM.this.fS.unstuff();
                    float f2 = cells[EM.this.from[i] + offset];
                    float f3 = (f1 + f2) - EM.this.total;
                    float f4 = Maths.exp(f3);
                    double p = EM.this.transitionEstimates[i];
                    if (f4 > 0) {
						EM.this.transitionEstimates[i] += f4;
					}
                    if (Debug.DEBUGCODE && (f4 > 0)
                            && (EM.this.transitionEstimates[i] <= p)) {
						throw new IllegalStateException();
					}
                    if (Debug.DEBUGCODE && Float.isNaN(f4)) {
						throw new IllegalStateException(
                                " Value is not a number " + x + " "
                                        + y + " " + f4 + " " + EM.this.total);
					}
                    if ((Debug.DEBUGCODE && (f4 > 1.02)) || (f4 < -0.01)) {
						PairEM.logger.info(" Value greater than one " + f1
                                + " " + f2 + " " + f3 + " " + f4
                                + " " + EM.this.total + " " + x + " " + y
                                + " " + i);
					}
                }
            }
        };

        public final Cell.CellCalculator backwards = new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */
            public final void calc(float[] cells, int offset, int x,
                    int y) {
            }
        };

        public final Procedure_Int passRunningTotal = new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public final void pro(int i) {
                EM.this.total = Float.intBitsToFloat(i);
                if (Debug.DEBUGCODE
                        && (Float.isNaN(EM.this.total) || Float
                                .isInfinite(EM.this.total))) {
					throw new IllegalStateException(
                            " Total is unacceptable " + EM.this.total);
				}
            }
        };

        public final Procedure_NoArgs reset = new Procedure_NoArgs() {
            public final void pro() {
                if (!EM.this.fS.empty()) {
					throw new IllegalStateException();
				}
            }
        };
    }

    public static void main(final String[] args) throws IOException {
        final PairEM pairEM = new PairEM();
        final PrePecan prePecan = new PrePecan();
        final InputMunger inputMunger = new InputMunger();
        inputMunger.addStandardWatches();
        prePecan.setCommandLineArguments(inputMunger);
        pairEM.setCommandLineArguments(inputMunger);
        if (!inputMunger.parseInput(args)) {
			System.exit(0);
		}
        inputMunger.processStandardWatches();
        prePecan.parseArguments(inputMunger);
        pairEM.parseArguments(inputMunger);
        final String treeFile = inputMunger.watchStrings(PrePecan.TREE)[0];
        final String[] seqFiles = inputMunger
                .watchStrings(PrePecan.SEQUENCES);
        final NewickTreeParser.Node tree = new NewickTreeParser(
                NewickTreeParser.commentEater(NewickTreeParser
                        .tokenise(new StringReader(treeFile)))).tree;
        if (seqFiles.length % 2 != 0) {
			throw new IllegalStateException();
		}
        final Chains.PrimeConstraints pC = prePecan.runPrePecan(tree,
                seqFiles);
        int leadLength = 10;
        pC.updatePrimeConstraints(0, 1, -leadLength - 1,
                -leadLength - 1, leadLength);
        pC.updatePrimeConstraints(1, 0, -leadLength - 1,
                -leadLength - 1, leadLength);
        pC.updatePrimeConstraints(0, 1, prePecan.seqSizes[0], prePecan.seqSizes[1], 0);
        pC.updatePrimeConstraints(1, 0, prePecan.seqSizes[1], prePecan.seqSizes[0], 0);
        final List[][] chains = new List[][] {
                {
                        null,
                        (List) GeneratorTools.append(pC
                                .convertPrimeConstraintsToEdgeList(0,
                                        1, true), new LinkedList()) },
                {
                        (List) GeneratorTools.append(pC
                                .convertPrimeConstraintsToEdgeList(1,
                                        0, true), new LinkedList()),
                        null } };
        {

            Chains.LocalAligner localAligner = PrePecan
                    .makeExonerateAligner(
                            prePecan.exonerateBasicCommand,
                            prePecan.exonerateString,
                            prePecan.exoneratePartitionAlignerWordLength,
                            prePecan.exoneratePartitionAlignerMinScore,
                            prePecan.exoneratePartitionAlignerGappedExtension,
                            prePecan.exoneratePartitionAlignerModel,
                            prePecan.saturateThreshold,
                            prePecan.exoneratePartitionAlignerSoftMaskSequences,
                            prePecan.exoneratePartitionAlignerSoftMaskSequences);
            if (prePecan.rescoreAlignments) {
				localAligner = Chains.rescoreAlignments(localAligner,
                        prePecan.relativeEntropyThreshold);
			}
            final Chains.Aligner aligner = Chains
                    .alignerConvertor(localAligner);
            Chains.transformConstraintsAndCloseLargeGaps(pC, chains,
                    pairEM.anchorDiagonalWidth,
                    pairEM.preCloseGapsLargerThan,
                    pairEM.overhangingDiagonalsIntoPreClosedGaps,
                    aligner, prePecan.cachedSeqFiles, new int[][] { {
                            0, 1 } });
        }
        pC.updatePrimeConstraints(0, 1, -leadLength - 1,
                -leadLength - 1, leadLength);
        pC.updatePrimeConstraints(1, 0, -leadLength - 1,
                -leadLength - 1, leadLength);
        pC.updatePrimeConstraints(0, 1, prePecan.seqSizes[0], prePecan.seqSizes[1], 0);
        pC.updatePrimeConstraints(1, 0, prePecan.seqSizes[1], prePecan.seqSizes[0], 0);
        final NewickTreeParser nTP = new NewickTreeParser(NewickTreeParser
                .commentEater(NewickTreeParser
                        .tokenise(new BufferedReader(new FileReader(
                                pairEM.hMMFile)))));// */ExternalExecution
        // .getAbsolutePath(pairEM.hMMFile))))));
        for (int i = 0; i < pairEM.repetitions; i++) {
            final Map<Long, Double> m = new HashMap<Long, Double>();
            Generator g = PolygonFiller
                    .flipEdgeXYDiagonalsCoordinates(pC
                            .convertPrimeConstraintsToEdgeList(0, 1,
                                    false));
            Generator g2 = PolygonFiller
                    .flipEdgeXYDiagonalsCoordinates(pC
                            .convertPrimeConstraintsToEdgeList(1, 0,
                                    false));
            pairEM.runPairEM(chains[0][1], g2, g,
                    prePecan.cachedSeqFiles[0],
                    prePecan.cachedSeqFiles[1], prePecan.seqSizes[0],
                    prePecan.seqSizes[1], nTP, m);
            g = PolygonFiller
                    .flipEdgeXYDiagonalsCoordinates(pC
                            .convertPrimeConstraintsToEdgeList(1, 0,
                                    false));
            g2 = PolygonFiller
                    .flipEdgeXYDiagonalsCoordinates(pC
                            .convertPrimeConstraintsToEdgeList(0, 1,
                                    false));
            pairEM.runPairEM(chains[1][0], g2, g,
                    prePecan.cachedSeqFiles[1],
                    prePecan.cachedSeqFiles[0], prePecan.seqSizes[1],
                    prePecan.seqSizes[0], nTP, m);
            PairEM.writeTransitions(m, nTP.tree);
        }
        final OutputStream oS = new BufferedOutputStream(
                new FileOutputStream(pairEM.hMMOutputFile));
        oS.write(nTP.tree.toString().getBytes());
        oS.close();
    }

}
