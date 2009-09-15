/*
 * Created on Feb 28, 2005
 */
package bp.pecan;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function_Index;
import bp.common.fp.Function_Int;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Procedure_Int_2Args;
import bp.common.io.Debug;
import bp.common.io.NewickTreeParser;
import bp.common.maths.Maths;
import bp.pecan.dimensions.Dimension;
import bp.pecan.dimensions.Sequence;

/**
 * @author benedictpaten
 */
public class MatrixIteratorTest
                               extends TestCase {

    public void testMatrixIterator_BoundaryConditions() {
        final Procedure_Int_2Args p = new Procedure_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int_2Args#pro(int, int)
             */
            public void pro(int i, int j) {
            }
        };
        final MatrixIterator mI = new MatrixIterator(3, 1000);
        final List lET = new ArrayList();
        final List rET = new ArrayList();
        final int[] lTA = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
        final Cell.CellCalculator doNothing = new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */
            public void calc(float[] cells, int offset, int x, int y) {
                // TODO Auto-generated method stub

            }
        };
        lET.add(PolygonFillerTest.createLine(0, 0, 2, 0));
        rET.add(PolygonFillerTest.createLine(1, 0, 2, 0));
        // ((List) eT.get(0)).add(new int[] { 2, 1, 0 });
        mI.scanPolygon(PolygonFiller.cloneEdgeList(PolygonFillerTest
                .convertOldEdgeListToNewEdgeList(lET)), PolygonFiller
                .cloneEdgeList(PolygonFillerTest
                        .convertOldEdgeListToNewEdgeList(rET)), 2, 3,
                new float[9], 0, doNothing, doNothing, lTA, lTA);
        try { // illegal initial diagonal
            mI.scanPolygon(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(lET)),
                    PolygonFiller.cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(rET)),
                    2, 3, new float[21], 3, null, null, lTA, lTA);
            Assert.fail();
        } catch (final IllegalStateException e) {
            ;
        }
        try { // illegal initial diagonal
            mI.scanPolygon(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(lET)),
                    PolygonFiller.cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(rET)),
                    2, 3, new float[9], -1, null, null, lTA, lTA);
            Assert.fail();
        } catch (final IllegalStateException e) {
            ;
        }
        try { // illegal initial polygon offset
            ((List<Integer>) lET.get(0)).set(0, new Integer(2));
            ((List<Integer>) rET.get(0)).set(0, new Integer(2));
            mI.scanPolygon(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(lET)),
                    PolygonFiller.cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(rET)),
                    2, 3, new float[15], 2, null, null, lTA, lTA);
            Assert.fail();
        } catch (final IllegalStateException e) {
            ;
        }
        try { // illegal final height
            mI.scanPolygon(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(lET)),
                    PolygonFiller.cloneEdgeList(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(rET)),
                    2, 4, new float[9], 0, null, null, lTA, lTA);
            Assert.fail();
        } catch (final IllegalStateException e) {
            ;
        }
        try { // illegal empty edge list
            mI.scanPolygon(new ArrayList(), new ArrayList(), 1, 1,
                    new float[9], 0, null, null, lTA, lTA);
            Assert.fail();
        } catch (final RuntimeException e) {
            ;
        }
    }

    static Dimension getRandomSeq(final int length, final int alphabetSize) {
        final int[] seq1 = new int[length];
        for (int i = 0; i < seq1.length; i++) {
			seq1[i] = (int) (Math.random() * alphabetSize);
		}
        return new Sequence(seq1);
    }

    static Object[] getStateMachine(final int bitPrecision, final int lineLength,
            final Function_Int seq1, final Function_Int seq2) {
        final float p0 = Maths.log(0), p1 = Maths
                .log((float) 0.1), p2 = Maths.log((float) 0.2), p3 = Maths
                .log((float) 0.3), p4 = Maths.log((float) 0.4), p5 = Maths
                .log((float) 0.5), p6 = Maths.log((float) 0.6), p7 = Maths
                .log((float) 0.7), p8 = Maths.log((float) 0.8), p9 = Maths
                .log((float) 0.9), p10 = Maths.log((float) 1.0);

        final String randomHMM = "(('A', 'T'), (" + p5 + ", " + p2
                + ", " + p3 + "), (" + p7 + ", " + p1 + ", " + p2
                + " )," + "( (0, " + p1 + "), (1, " + p7 + "), (2, "
                + p2 + "), 2, (" + p2 + ", " + p2 + ", " + p2 + ", "
                + p4 + "))," + "((0, " + p5 + "), (1, " + p5
                + "), 1, (" + p6 + ", " + p4 + " ))," + "((2, " + p8
                + "), (0, " + p2 + ")," + " 3, (" + p9 + ", " + p1
                + "))" + ");";

        final NewickTreeParser nTP = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader(randomHMM)));
        Cell.isLegitimateHMM(nTP.tree);
        final Object[] program = Cell.createProgram(nTP.tree,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        // Object[] rProgram = Cell.reverseStateMachineProgram(
        // (int[]) program[Cell.PROGRAM],
        // (float[])program[Cell.TRANSITIONSANDEMISSIONS], 3, 2,
        // Integer.MAX_VALUE);
        final int[] program2 = ((int[]) program[Cell.PROGRAM])
                .clone();
        final int[] program3 = ((int[]) program[Cell.PROGRAM])
                .clone();
        final int stateNumber = ((float[]) program[Cell.STARTSTATES]).length;
        Cell.transformProgram((int[]) program[Cell.PROGRAM],
                -(lineLength), stateNumber);
        Cell.transformProgram(program2, (lineLength), stateNumber);
        final Cell.CellCalculator forwardLeftL = Cell.nextSumForward(
                program3,
                (float[]) program[Cell.TRANSITIONS],
                (float[]) program[Cell.EMISSIONS], 2, seq1, seq2);
        final Cell.CellCalculator forwardLeft = Cell.nextSumForward(
                (int[]) program[Cell.PROGRAM],
                (float[]) program[Cell.TRANSITIONS],
                (float[]) program[Cell.EMISSIONS], 2, seq1, seq2);
        final Cell.CellCalculator forwardRight = Cell.nextSumForward(
                program2, (float[]) program[Cell.TRANSITIONS],
                (float[]) program[Cell.EMISSIONS], 2, seq1, seq2);
        return new Object[] { forwardLeftL, forwardLeft, forwardRight };
    }

    public static List getRandomLTPPointsL(final int[][] matrix) {
        final List l = new LinkedList();
        for (int i = 1; i < matrix.length; i++) {
            for (int j = 1; j < matrix[i].length; j++) {
                if ((matrix[i - 1][j] == 0) && (matrix[i][j] == 1)
                        && (matrix[i - 1][j - 1] == 1)
                        && (matrix[i][j - 1] == 1)
                        && (Math.random() > 0.1)) {
                    l.add(new int[] { i, j });
                }
            }
        }
        //l.clear();
        return l;
    } 
    
    public static List getRandomLTPPointsR(final int[][] matrix) {
        final List l = new LinkedList();
        for (int i = 1; i < matrix.length; i++) {
            for (int j = 1; j < matrix[i].length; j++) {
                if ((matrix[i - 1][j] == 1) && (matrix[i][j] == 1)
                        && (matrix[i - 1][j - 1] == 1)
                        && (matrix[i][j - 1] == 0)
                        && (Math.random() > 0.1)) {
                    l.add(new int[] { i, j });
                }
            } 
        }
        //l.clear();
        return l;
    }

    public static int[] convertLTPoints(final List l2) {
        final int[] iA = new int[l2.size() * 2 + 2];
        int j = 0;
        for (final Iterator it = l2.iterator(); it.hasNext();) {
            final int[] iA2 = (int[]) it.next();
            iA[j++] = iA2[0];
            iA[j++] = iA2[1];
            // Debug.pl(" point " + iA2[0] + " " + iA2[1]);
        }
        iA[j] = Integer.MAX_VALUE;
        iA[j + 1] = Integer.MAX_VALUE;
        return iA;
    }

    public void testMatrixIterator() {
        for (int trial = 0; trial < 1000; trial++) {
            Debug.pl(" doing test " + trial);
            final int minMaxOffset = 1 + (int) (Math.random() * 9);
            final int height = 2 * minMaxOffset
                    + (int) (Math.random() * 100), width = 2
                    * minMaxOffset + (int) (Math.random() * 100);

            final int stateNumber = 3;
            final Dimension d1 = MatrixIteratorTest.getRandomSeq(width, 2), d2 = MatrixIteratorTest.getRandomSeq(
                    height, 2);
            final Function_Int seq1 = new Function_Int() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Function_Int#fn(int)
                 */
                public int fn(int x) {
                    return d1.get(x, 0);
                }
            };
            final Function_Int seq2 = new Function_Int() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Function_Int#fn(int)
                 */
                public int fn(int x) {
                    return d2.get(x, 0);
                }
            };
            final Object[] oALarge = MatrixIteratorTest.getStateMachine(4, 1001, seq1, seq2);
            // Object[] oASmall = getStateMachine(4, 2, seq1, seq2);
            // final Cell.CellCalculator setCells = (Cell.CellCalculator) oA[0];
            // Cell.CellCalculator setCells2 =
            // new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */
            // public void calc(float[] cells, int offset, int x, int y) {
            // setCells.calc(cells, offset, d1.get(x, 0), d2.get(y, 0));
            // }
            // };
            final List[] poly = PolygonFillerTest.getRandomPolygon(width,
                    height, minMaxOffset, 0);
            final List lREL = poly[0];
            final List rREL = poly[1];
            int[][] matrix = new int[height][width];
            // Debug.pl(" starting ");

            final List eT = PolygonFillerTest
                    .convertNewEdgeListToOldEdgeList((List) IterationTools
                            .append(
                                    PolygonFiller
                                            .combineEdgeLists(
                                                    PolygonFiller
                                                            .cloneEdgeList(
                                                                    PolygonFillerTest
                                                                            .convertOldEdgeListToNewEdgeList(lREL))
                                                            .iterator(),
                                                    PolygonFiller
                                                            .cloneEdgeList(
                                                                    PolygonFillerTest
                                                                            .convertOldEdgeListToNewEdgeList(rREL))
                                                            .iterator()),
                                    new ArrayList()));
            PolygonFillerTest.fillInMatrix(matrix, eT, 1, 0);
            
            matrix = (int[][]) bp.common.ds.Array.transpose2DMatrix()
                    .fn(matrix);
            
            final List lLTL = MatrixIteratorTest.getRandomLTPPointsL(matrix);
            final List rLTL = MatrixIteratorTest.getRandomLTPPointsR(matrix);
            final int[] lLTA = MatrixIteratorTest.convertLTPoints(lLTL);
            // Debug.pl(" imbetween ");
            final int[] rLTA = MatrixIteratorTest.convertLTPoints(rLTL);
            
            final float[][][] matrix1 = MatrixIteratorTest.computeMatrix(matrix,
            /* setCells2 */(Cell.CellCalculator) oALarge[0],
                    stateNumber, new float[] { 0,
                            Float.NEGATIVE_INFINITY,
                            Float.NEGATIVE_INFINITY },
                    Float.NEGATIVE_INFINITY, lLTA, rLTA);
            final float[][][] matrix2 = new float[matrix.length][matrix.length != 0 ? matrix[0].length
                    : 0][stateNumber];
            for (final float[][] element : matrix2) {
                for (float[] element0 : element) {
                    Arrays.fill(element0,
                            Float.NEGATIVE_INFINITY);
                }
            }

            final MatrixIterator mI = new MatrixIterator(stateNumber, /* 0 */
            1000);
            final Function_Index fn = PolygonFiller.clipPolygons(Generators
                    .iteratorGenerator(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(lREL
                                    .iterator())), Generators
                    .iteratorGenerator(PolygonFillerTest
                            .convertOldEdgeListToNewEdgeList(rREL
                                    .iterator())), width, height);
            final int maxDiagonal = height + width - 2; // inclusive
            int anchorX = 0, anchorY = 0, diagonal = 0;
            float[] values = new float[3 * stateNumber];
            Arrays.fill(values, Float.NEGATIVE_INFINITY);
            values[stateNumber] = 0;
            {
                int i = 0;
                final Object[] oA2 = (Object[]) fn.fn(0);
                matrix2[0][0][0] = 0;
                while (i < maxDiagonal) { // no longer works like that
                    i = maxDiagonal; // += 1 + new
                    // Random().nextInt(maxDiagonal - i);
                    Object[] oA = null;
                    try {
                        oA = (Object[]) fn.fn(i);
                    } catch (final IllegalStateException e) {
                        oA = (Object[]) fn.fn(++i);
                    }
                    oA[0] = PolygonFillerTest
                            .convertNewEdgeListToOldEdgeList((List) oA[0]);
                    oA[1] = PolygonFillerTest
                            .convertNewEdgeListToOldEdgeList((List) oA[1]);
                    List lET = (List) oA[0];
                    List rET = (List) oA[1];
                    final int[] dC = (int[]) oA[2];// 1];
                    /*
                     * if (Math.random() > 0.5 && i < maxDiagonal) { i += 1 +
                     * new Random() .nextInt(maxDiagonal - i); try { oA =
                     * (Object[]) fn.fn(i); } catch (IllegalStateException e) {
                     * oA = (Object[]) fn.fn(++i); } eT = (List) IterationTools
                     * .append(PolygonFiller .combineEdgeLists(eT .iterator(),
                     * ((List) oA[0]) .iterator()), new ArrayList()); dC =
                     * (int[]) oA[1]; }
                     */
                    lET = PolygonFillerTest
                            .convertNewEdgeListToOldEdgeList((List) IterationTools
                                    .append(
                                            PolygonFiller
                                                    .transformEdges(
                                                            PolygonFillerTest
                                                                    .convertOldEdgeListToNewEdgeList(lET
                                                                            .iterator()),
                                                            -anchorX,
                                                            -anchorY),
                                            new ArrayList()));
                    rET = PolygonFillerTest
                            .convertNewEdgeListToOldEdgeList((List) IterationTools
                                    .append(
                                            PolygonFiller
                                                    .transformEdges(
                                                            PolygonFillerTest
                                                                    .convertOldEdgeListToNewEdgeList(rET
                                                                            .iterator()),
                                                            -anchorX,
                                                            -anchorY),
                                            new ArrayList()));
                    // MutableInteger mIX = new MutableInteger(0), mIY = new
                    // MutableInteger(
                    // 0);
                    mI
                            .scanPolygon(
                                    PolygonFillerTest
                                            .convertOldEdgeListToNewEdgeList(lET),
                                    PolygonFillerTest
                                            .convertOldEdgeListToNewEdgeList(rET),
                                    dC[4] - anchorX + 1,
                                    dC[7] - anchorY + 1,
                                    values,
                                    diagonal,
                                    MatrixIteratorTest.cellCalculatorWrapper(
                                            /* setCells2 */(Cell.CellCalculator) oALarge[1],
                                            matrix2, anchorX, anchorY),
                                    MatrixIteratorTest.cellCalculatorWrapper(
                                            /* setCells2 */(Cell.CellCalculator) oALarge[2],
                                            matrix2, anchorX, anchorY),
                                    lLTA, rLTA);
                    diagonal = dC[4] - dC[6];
                    values = new float[((1 + diagonal) * 2 + 1)
                            * stateNumber];
                    mI.getFinalValues(values, dC[4] + dC[5] - anchorX
                            - anchorY);
                    anchorX = dC[6];
                    anchorY = dC[5];
                }
            }
            for (int i = 0; i < matrix1.length; i++) {
                for (int j = 0; j < matrix1[i].length; j++) {
                    for (int k = 0; k < matrix1[i][j].length; k++) {
                        if (matrix1[i][j][k] != matrix2[i][j][k]) {
                            Assert.fail(i + " " + j + " " + k + " "
                                    + matrix1[i][j][k] + " "
                                    + matrix2[i][j][k]);
                        }
                    }
                }
            }
        }
    }

    static Cell.CellCalculator cellCalculatorWrapper(
            final Cell.CellCalculator setCells,
            final float[][][] matrix, final int xOffset,
            final int yOffset) {
        return new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */
            public void calc(final float[] cells, final int offset, int x, int y) {
                x += xOffset;
                y += yOffset;
                setCells.calc(cells, offset, x, y);
                final float[] iA = matrix[x][y];
                // System.arraycopy(cells, 0, iA, 0, iA.length);
                System.arraycopy(cells, offset, iA, 0, iA.length);
            }
        };
    }

    static float[][][] computeMatrix(final int[][] polygon,
            final Cell.CellCalculator setCells, final int stateNumber,
            final float[] startStates, final float minValue, final int[] lLTA,
            final int[] rLTA) {
        final float[][][] matrix = new float[polygon.length + 1][polygon.length != 0 ? polygon[0].length + 1
                : 0][stateNumber];
        for (final float[][] element : matrix) {
            for (float[] element0 : element) {
                Arrays.fill(element0, minValue);
            }
        }
        int lLTAi = 0;
        int rLTAi = 0;
        matrix[1][1] = startStates.clone();
        for (int i = 0; i < polygon.length; i++) {
            for (int j = 0; j < polygon[i].length; j++) {
                if (((i != 0) || (j != 0)) && (polygon[i][j] == 1)) {
                    final float[] cells = new float[stateNumber * 4];
                    System.arraycopy(matrix[i][j + 1], 0, cells,
                            stateNumber, stateNumber);
                    // System.arraycopy(matrix[i][j + 1], 0, cells,
                    // 0, stateNumber);
                    System.arraycopy(matrix[i][j], 0, cells,
                            stateNumber * 2, stateNumber);
                    System.arraycopy(matrix[i + 1][j], 0, cells,
                            stateNumber * 3, stateNumber);
                    // setXY.pro(i, j);
                    if ((i == lLTA[lLTAi]) && (j == lLTA[lLTAi + 1])) {
                        Arrays.fill(cells, stateNumber * 2,
                                stateNumber * 3, minValue);
                        lLTAi += 2;
                    }
                    if ((i == rLTA[rLTAi]) && (j == rLTA[rLTAi + 1])) {
                        Arrays.fill(cells, stateNumber * 2,
                                stateNumber * 3, minValue);
                        rLTAi += 2;
                    }
                    setCells.calc(cells, 0, i, j);
                    System.arraycopy(cells, 0, matrix[i + 1][j + 1],
                            0, stateNumber);
                    // System.arraycopy(cells, stateNumber, matrix[i + 1][j +
                    // 1],
                    // 0, stateNumber);
                }
            }
        }
        final float[][][] matrix2 = new float[polygon.length][polygon.length != 0 ? polygon[0].length
                : 0][];
        for (int i = 0; i < matrix2.length; i++) {
			for (int j = 0; j < matrix2[i].length; j++) {
				matrix2[i][j] = matrix[i + 1][j + 1];
			}
		}
        return matrix2;
    }

    public void testSetDiagonalValues() {
        for (int trial = 0; trial < 1000; trial++) {
            final Random r = new Random();
            final int width = 1 + r.nextInt(100), height = 1 + r
                    .nextInt(100), stateNumber = 1 + r.nextInt(10);
            final MatrixIterator mI = new MatrixIterator(stateNumber, 100);
            for (int i = 0; i < Math.min(width, height); i++) {
                final float[] iA = new float[((i + 1) * 2 + 1)
                        * stateNumber];
                for (int j = 0; j < iA.length; j++) {
					iA[j] = (float) (Math.random() * 10);
				}
                mI.width = width;
                mI.height = height;
                mI.setInitialValues(iA, i);
                boolean b = i % 2 != 0;
                int l = 0;
                for (int j = 0; j <= i; j++) {
                    final float[] iA2 = mI.line;
                    final int m = (b = !b) ? 0 : iA2.length / 2;
                    // float[] iA2 = (b = !b) ? mI.line : mI.pLine;
                    for (int k = 0; k < stateNumber * 2; k++) {
						Assert.assertEquals(iA[l++], iA2[j * stateNumber + k
                                + m], 0);
					}
                }
                final float[] iA2 = mI.line; // (b = !b) ? mI.line : mI.pLine;
                final int m = (b = !b) ? 0 : iA2.length / 2;
                for (int k = 0; k < stateNumber; k++) {
					Assert.assertEquals(iA[l++], iA2[(i + 1) * stateNumber
                            + k + m], 0);
				}
            }
        }
    }

    public void testGetFinalValues() {
        for (int trial = 0; trial < 1000; trial++) {
            final Random r = new Random();
            final int width = 1 + r.nextInt(100), height = 1 + r
                    .nextInt(100), stateNumber = 1 + r.nextInt(10);
            final MatrixIterator mI = new MatrixIterator(stateNumber, width);
            final int max = width + height - 2;
            for (int i = max; i >= Math.max(width, height) - 1; i--) {
                final float[] iA = new float[((max - i + 1) * 2 + 1)
                        * stateNumber];
                for (int j = 0; j < iA.length; j++) {
					iA[j] = (float) (Math.random() * 10);
				}
                mI.width = width;
                mI.height = height;
                boolean b = false;
                int l = 0;
                for (int j = i - height + 1; j < width; j++) {
                    final float[] iA2 = mI.line; // (b = !b) ? mI.line : mI.pLine;
                    final int m = (b = !b) ? 0 : iA2.length / 2;
                    for (int k = 0; k < stateNumber * 2; k++) {
						iA2[j * stateNumber + k + m] = iA[l++];
					}
                }
                float[] iA2 = mI.line; // (b = !b) ? mI.line : mI.pLine;
                final int m = (b = !b) ? 0 : iA2.length / 2;
                // float[] iA2 = (b = !b) ? mI.line : mI.pLine;
                for (int k = 0; k < stateNumber; k++) {
					iA2[width * stateNumber + k + m] = iA[l++];
				}
                iA2 = new float[iA.length];
                mI.getFinalValues(iA2, i);
                Assert.assertTrue(Arrays.equals(iA, iA2));
            }
            for (int i = 0; i < Math.max(width, height) - 1; i++) {
                try {
                    final float[] iA = new float[((i + 1) * 2 + 1)
                            * stateNumber];
                    mI.getFinalValues(iA, i);
                    Assert.fail();
                } catch (final IllegalStateException e) {
                    ;
                }
            }
        }
    }

}