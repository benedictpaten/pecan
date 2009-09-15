/*
 * Created on Jun 29, 2005
 */
package bp.pecan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.Array;
import bp.common.ds.FloatStack;
import bp.common.ds.IntStack;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Function_Index;
import bp.common.fp.Function_Index_3Args;
import bp.common.fp.Function_Int;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Procedure;
import bp.common.io.Debug;
import bp.common.io.ExternalExecution;
import bp.common.io.NewickTreeParser;
import bp.common.maths.Maths;
import bp.pecan.dimensions.Dimension;

/**
 * @author benedictpaten
 */
public class ForwardBackwardMatrixIterTest
                                          extends TestCase {
    
    

    static Object[] getStateMachine_Float(final Function_Int getX,
            final Function_Int getY, final String hmmName, int lineLength)
            throws FileNotFoundException {
        //parse hmm
        final NewickTreeParser nTP = new NewickTreeParser(NewickTreeParser
                .commentEater(NewickTreeParser
                        .tokenise(new BufferedReader(new FileReader(
                                ExternalExecution
                                        .getAbsolutePath(hmmName))))));
        Cell.isLegitimateHMM(nTP.tree);
        final Object[] program = Cell.createProgram(nTP.tree,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        final int[] pFLL = (int[])program[Cell.PROGRAM];
        final float[] e = (float[])program[Cell.EMISSIONS];
        final float[] tF = (float[])program[Cell.TRANSITIONS];
        final float[] ss = (float[])program[Cell.STARTSTATES];
        Object[] oA = Cell.makeRProgram(pFLL, tF, ss.length);
        final int[] pBLL = (int[])oA[0];
        final float[] tB = (float[])oA[1];
        final int[] pFL = pFLL.clone();
        Cell.transformProgram(pFL, -lineLength, ss.length);
        oA = Cell.makeRProgram(pFL, tF, ss.length);
        final int[] pBL = (int[])oA[0];
        
        final int[] pFR = pFLL.clone();
        Cell.transformProgram(pFR, lineLength, ss.length);
        oA = Cell.makeRProgram(pFR, tF, ss.length);
        final int[] pBR = (int[])oA[0];
        
        //Object[] rProgram = Cell.reverseStateMachineProgram(
          //      (int[]) program[Cell.PROGRAM],
            //    (float[]) program[Cell.TRANSITIONSANDEMISSIONS], 3,
              //  5, Integer.MAX_VALUE);
        final Cell.CellCalculator fLL = Cell.nextSumForward(pFLL, tF, e, 5, getX, getY);
        final Cell.CellCalculator bLL = Cell.nextSumBackward(pBLL, tB, e, ss.length, 5, getX, getY);
        final Cell.CellCalculator fL = Cell.nextSumForward(pFL, tF, e, 5, getX, getY);
        final Cell.CellCalculator bL = Cell.nextSumBackward(pBL, tB, e, ss.length, 5, getX, getY);
        final Cell.CellCalculator fR = Cell.nextSumForward(pFR, tF, e, 5, getX, getY);
        final Cell.CellCalculator bR = Cell.nextSumBackward(pBR, tB, e, ss.length, 5, getX, getY);
        //Cell.CellCalculator forward = Cell.nextSum(
        //        (int[]) program[Cell.PROGRAM],
         //       (float[]) program[Cell.TRANSITIONSANDEMISSIONS],
         //       5, getX, getY);
        //Cell.CellCalculator backward = Cell.nextSumBackwards(
          //      (int[]) rProgram[Cell.PROGRAM],
          //      (float[]) rProgram[Cell.TRANSITIONSANDEMISSIONS],
          //     5, getX, getY);
        //Cell.ForwardBackwards cFB = new Cell.ForwardBackwards(
        //        forward, backward, getX, getY);
        //return new Object[] { forward, backward };
        return new Object[] { fLL, bLL, fL, fR, bL, bR };
    }

    Dimension d1, d2;

    List lREL, rREL, eT;

    MatrixIterator mI;

    int minMaxOffset, height, width, stateNumber;

   
    Function_Index polygonClipper;

    Random r;

    Cell.CellCalculator setCellsBackwardsL;
    
    Cell.CellCalculator setCellsBackwardsR;
    
    Cell.CellCalculator setCellsBackwardsLL;

    Cell.CellCalculator setCellsForwardsL;
    
    Cell.CellCalculator setCellsForwardsR;
    
    Cell.CellCalculator setCellsForwardsLL;

    float[] startStates, endStates;
    
    List lLTL = new LinkedList();
    
    List rLTL = new LinkedList();

    float[][][] backwardMatrix() {
        int[][] matrix = new int[this.height][this.width];
        PolygonFillerTest.fillInMatrix(matrix, PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.eT))), 1, 0);
        matrix = (int[][]) Array.transpose2DMatrix().fn(matrix);
        matrix = (int[][]) Array.reverseXAxisOf2DMatrix().fn(matrix);
        matrix = (int[][]) Array.reverseYAxisOf2DMatrix().fn(matrix);
        final float[] eS = this.endStates.clone();
        Array.reverseArray(eS, 0, eS.length);
        final int[] rLTL2 = new int[1000];
        final int[] lLTL2 = new int[1000];
        PolygonFiller.reverseLessThanCoordinates(MatrixIteratorTest.convertLTPoints(this.rLTL), rLTL2, this.width - 1, this.height -1); 
        PolygonFiller.reverseLessThanCoordinates(MatrixIteratorTest.convertLTPoints(this.lLTL), lLTL2, this.width - 1, this.height - 1);
        final float[][][] matrixB = MatrixIteratorTest.computeMatrix(
                matrix, new Cell.CellCalculator() {
                    /* (non-Javadoc)
                     * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
                     */
                    public void calc(float[] cells, int offset, int x, int y) {
                        ForwardBackwardMatrixIterTest.this.setCellsBackwardsLL.calc(cells, offset, ForwardBackwardMatrixIterTest.this.width - 1 - x, ForwardBackwardMatrixIterTest.this.height - 1 - y);
                    }
                }, this.stateNumber, eS, Float.NEGATIVE_INFINITY, 
                rLTL2, lLTL2);
        final float[][][] matrixB2 = new float[matrixB.length][matrixB[0].length][this.stateNumber];
        for (int i = 0; i < matrixB.length; i++) {
			for (int j = 0; j < matrixB[i].length; j++) {
				for (int k = 0; k < matrixB[i][j].length; k++) {
					matrixB2[matrixB.length - 1 - i][matrixB[i].length
                            - 1 - j][this.stateNumber - 1 - k] = matrixB[i][j][k];
				}
			}
		}
        return matrixB2;
    }

    public void fillMatrix(final float[][][] matrix) {
        for (final float[][] element : matrix) {
            for (float[] element0 : element) {
                Arrays.fill(element0, Float.NEGATIVE_INFINITY);
            }
        }
    }

    float[][][] forwardMatrix() {
        int[][] matrix = new int[this.height][this.width];
        PolygonFillerTest.fillInMatrix(matrix, PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.eT))), 1, 0);
        matrix = (int[][]) Array.transpose2DMatrix().fn(matrix);
        return MatrixIteratorTest.computeMatrix(matrix, 
                this.setCellsForwardsLL, this.stateNumber, this.startStates
                        .clone(), Float.NEGATIVE_INFINITY, MatrixIteratorTest.convertLTPoints(this.lLTL), MatrixIteratorTest.convertLTPoints(this.rLTL));
    }

    float[] getBackwardDiagonal(final int x0, final int y0, final int x1, final int y1,
            final int xMax, final int yMax, final float[][][] matrix) {
        final float[] diagonal = new float[ matrix.length == 0 ? 1000 : 5*matrix[0].length*this.stateNumber];
        int j = y0, k = 0;
        if (y0 + 1 < yMax) {
            System.arraycopy(matrix[x0][y0 + 1], 0, diagonal, k,
                    this.stateNumber);
        } else {
			Arrays.fill(diagonal, k, k + this.stateNumber, Float.NEGATIVE_INFINITY);
		}
        k += this.stateNumber;
        for (int i = x0; i <= x1; i++) {
            System.arraycopy(matrix[i][j], 0, diagonal, k,
                    this.stateNumber);
            k += this.stateNumber;
            if (i + 1 < xMax) {
                System.arraycopy(matrix[i + 1][j], 0, diagonal, k,
                        this.stateNumber);
            } else {
				Arrays.fill(diagonal, k, k + this.stateNumber, Float.NEGATIVE_INFINITY);
			}
            k += this.stateNumber;
            j--;
        }
        return diagonal;
    }

    float[] getDiagonal(final int x0, final int y0, final int x1, final int y1,
            final float[][][] matrix) {
        final float[] diagonal = new float[5000];
        int j = y0, k = 0;
        for (int i = x0; i <= x1; i++) {
            if (i - 1 >= 0) {
                System.arraycopy(matrix[i - 1][j], 0, diagonal, k,
                        this.stateNumber);
            } else {
				Arrays.fill(diagonal, k, k + this.stateNumber, Float.NEGATIVE_INFINITY);
			}
            k += this.stateNumber;
            System.arraycopy(matrix[i][j], 0, diagonal, k,
                    this.stateNumber);
            k += this.stateNumber;
            j--;
        }
        if (y1 - 1 >= 0) {
			System.arraycopy(matrix[x1][y1 - 1], 0, diagonal, k,
                    this.stateNumber);
		} else {
			Arrays.fill(diagonal, k, k + this.stateNumber, Float.NEGATIVE_INFINITY);
		}
        return diagonal;
    }

    public Function_Index getExtendedPolygonIterator() {
        final List l = new LinkedList();
        for(final Iterator it=this.lLTL.iterator();it.hasNext();) {
            final int[] iA = (int[])it.next();
            l.add(new int[] { iA[1], iA[0] });
        }
        return PolygonFiller.polygonIteratorWithLessThans(PolygonFiller.clipPolygons(Generators.iteratorGenerator(IterationTools.addToEnd(
                PolygonFiller.cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.lREL)).iterator(),
                new PolygonFiller.Node(this.width, this.height, this.height, 1))),
                Generators.iteratorGenerator(IterationTools.addToEnd(PolygonFiller.cloneEdgeList(
                        PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.rREL)).iterator(), new PolygonFiller.Node(
                        this.width, this.height, this.height, 1))), this.width + 1,
                        this.height + 1), Generators.iteratorGenerator(this.rLTL.iterator()), Generators.iteratorGenerator(l.iterator()), new int[1000]);
    }

    Procedure setCells(final MutableInteger x,
            final MutableInteger y, final float[][][] matrix) {
        return new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public void pro(final Object o) {
                final float[] iA = matrix[x.i][y.i];
                for (int i = 0; i < iA.length; i++) {
					iA[i] = ((float[]) o)[i] + iA[i];
				}
            }
        };
    }

    Procedure setCellsBackwards(final MutableInteger x,
            final MutableInteger y, final float[][][] matrix) {
        return new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public void pro(final Object o) {
                final float[] iA = matrix[x.i][y.i];
                for (int i = 0; i < iA.length; i++) {
					iA[i] = ((float[]) o)[iA.length - 1 - i];
				}
            }
        };
    }

    static Function_Int getD(final Dimension d) {
        return new Function_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int#fn(int)
             */
            public int fn(final int x) {
                return d.get(x, 0);
            }
        };
    }

    protected void setup(final int minWidth) throws Exception {
        super.setUp();
        this.r = new Random();
        this.minMaxOffset = 1 + (int) (Math.random() * 5);
        this.height = 2 * this.minMaxOffset + (int) (Math.random() * 300);
        this.width = 2 * this.minMaxOffset + (int) (Math.random() * 300);
        this.stateNumber = 3;
        this.d1 = MatrixIteratorTest.getRandomSeq(this.width + 1, 4);
        this.d2 = MatrixIteratorTest.getRandomSeq(this.height + 1, 4);
        final Object[] oA = ForwardBackwardMatrixIterTest.getStateMachine_Float(ForwardBackwardMatrixIterTest.getD(this.d1), ForwardBackwardMatrixIterTest.getD(this.d2),
                "bp/pecan/asymmetric.hmm", 1001);

        this.setCellsForwardsLL = (Cell.CellCalculator) oA[0];
        this.setCellsBackwardsLL = (Cell.CellCalculator) oA[1];
        this.setCellsForwardsL = (Cell.CellCalculator) oA[2];
        this.setCellsForwardsR = (Cell.CellCalculator) oA[3];
        this.setCellsBackwardsL = (Cell.CellCalculator) oA[4];
        this.setCellsBackwardsR = (Cell.CellCalculator) oA[5];

        final List[] poly = PolygonFillerTest.getRandomPolygon(this.width,
                this.height, this.minMaxOffset, minWidth);
        this.lREL = poly[0];
        this.rREL = poly[1];
        //lLTL = MatrixIteratorTest.getRandomLTPoints(PolygonFillerTest.convertOldEdgeListToNewEdgeList(lREL));
        //rLTL = MatrixIteratorTest.getRandomLTPoints(PolygonFillerTest.convertOldEdgeListToNewEdgeList(rREL));
        
        this.eT = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) IterationTools.append(PolygonFiller
                .combineEdgeLists(PolygonFiller.cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.lREL))
                        .iterator(), PolygonFiller
                        .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.rREL)).iterator()),
                new ArrayList()));
        
        
        int[][] matrix = new int[this.height][this.width];
        PolygonFillerTest.fillInMatrix(matrix, PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.eT))), 1, 0);
        matrix = (int[][]) Array.transpose2DMatrix().fn(matrix);
        
        this.lLTL = MatrixIteratorTest.getRandomLTPPointsL(matrix);
        this.rLTL = MatrixIteratorTest.getRandomLTPPointsR(matrix);
        
        
        this.mI = new MatrixIterator(this.stateNumber, 1000);
        this.polygonClipper = PolygonFiller.clipPolygons(Generators.iteratorGenerator(PolygonFiller
                .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.lREL)).iterator()), Generators.iteratorGenerator(PolygonFiller
                .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(this.rREL)).iterator()), this.width, this.height);
        this.startStates = new float[] { (float)Math.log(1.0 / 3),
                (float)Math.log(1.0 / 3), (float)Math.log(1.0 / 3) };
        this.endStates = new float[] { (float)Math.log(1.0 / 3),
                (float)Math.log(1.0 / 3), (float)Math.log(1.0 / 3) };
    }

    float[][][] sumForwardBackwardMatrices(final float[][][] fM,
            final float[][][] bM) {
        final float[][][] pM = new float[fM.length][fM[0].length][this.stateNumber];
        for (int i = 0; i < fM.length; i++) {
            for (int j = 0; j < fM[i].length; j++) {
                for (int k = 0; k < fM[i][j].length; k++) {
                    pM[i][j][k] = fM[i][j][k] + bM[i][j][k];
                }
            }
        }
        return pM;
    }
    
    public void testAddTogetherDiagonals() throws Exception {
        for (int trial = 0; trial < 100; trial++) {
            Debug.pl(" trial " + trial);
            this.setup(0); 
            final float[][][] fM = this.forwardMatrix();
            final float[][][] bM = this.backwardMatrix();
            final float[][][] pM = this.sumForwardBackwardMatrices(fM, bM);
            int fD = this.r.nextInt(this.height + this.width - 2 - this.minMaxOffset); //inclusive
            Object[] oA = (Object[]) this.polygonClipper.fn(fD);
            final int[] dC = (int[]) oA[2];//1];
            oA = (Object[]) this.polygonClipper.fn(this.height + this.width - 2);
            final int[] dC2 = (int[]) oA[2];//1];
            float total = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < this.stateNumber; i++) {
                total = Maths.logAdd(total,
                        pM[pM.length - 1][pM[0].length - 1][i]);
            }
            float totalS = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < this.stateNumber; i++) {
                totalS = Maths.logAdd(totalS, pM[0][0][i]);
            }
            Assert.assertEquals(total, totalS, 0.1);
            final float[] forwardDiag = this.getDiagonal(dC[6], dC[7], dC[4],
                    dC[5], fM);
            final float[] backwardDiag = this.getBackwardDiagonal(dC2[0], dC2[1],
                    dC2[2], dC2[3], this.width, this.height, bM);
            final int diagLength = (dC2[2] - dC2[0] + 1) * 2 + 1;
            int forwardDiagStartOffset, forwardDiagEndOffset;
            float[] iA;
            if (dC[6] + dC[7] + 2 == dC2[0] + dC2[1]) {
                iA = new float[this.stateNumber];
                System.arraycopy(forwardDiag, this.stateNumber, iA, 0,
                        this.stateNumber);
                forwardDiagStartOffset = 1;
                forwardDiagEndOffset = 1;
            } else {
                forwardDiagStartOffset = (dC[6] - dC[7] - 1)
                        - (dC2[0] - dC2[1] - 1);
                forwardDiagEndOffset = (dC[4] - dC[5] + 1)
                        - (dC2[0] - dC2[1] - 1);
                iA = forwardDiag;
            }
            final int[] lLTA = MatrixIteratorTest.convertLTPoints(this.lLTL);
            final int[] rLTA = MatrixIteratorTest.convertLTPoints(this.rLTL);
            final int x0 = dC[6];
            final int y0 = dC[5];
            final int fDiagC = dC[4] - x0;
            fD = x0 + y0 + fDiagC;
            final boolean isOnForwardLineL = ForwardBackwardMatrixIterTest.isOnTheLine(lLTA, fD + 1, Integer.MAX_VALUE);
            final boolean isOnBackwardLineL = ForwardBackwardMatrixIterTest.isOnTheLine(lLTA, fD + 2, Integer.MAX_VALUE);
            final boolean isOnForwardLineR = ForwardBackwardMatrixIterTest.isOnTheLine(rLTA, fD + 1, Integer.MAX_VALUE);
            final boolean isOnBackwardLineR = ForwardBackwardMatrixIterTest.isOnTheLine(rLTA, fD + 2, Integer.MAX_VALUE);
            Assert.assertEquals(ForwardBackwardMatrixIter
                            .addTogetherDiagonals(iA, backwardDiag,
                                    diagLength,
                                    forwardDiagStartOffset,
                                    forwardDiagEndOffset, dC2[0],
                                    dC2[1] + 1, this.setCellsForwardsLL,
                                    this.stateNumber, isOnForwardLineL, isOnBackwardLineL,
                                    isOnForwardLineR, isOnBackwardLineR), total, 0.1);
        }
    }

   
    public void testPosteriorMatrixIterator() throws Exception {
        for (int trial = 0; trial < 100; trial++) {
            this.setup(0);
            Debug.pl(" trial " + trial);
            final float[][][] fMatrix = this.forwardMatrix();
            final float[][][] bMatrix = this.backwardMatrix();
            final float[][][] pM = this.sumForwardBackwardMatrices(fMatrix,
                    bMatrix);
            float total = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < this.stateNumber; i++) {
				total = Maths.logAdd(total, pM[0][0][i]);
			}
            final float[] holdingDiagonalValues = new float[10000];
            final float[] diagonalValues = new float[10000], reverseDiagonalValues = new float[10000];
            final FloatStack floatStack = new FloatStack(10000);
            Arrays.fill(holdingDiagonalValues, Float.NEGATIVE_INFINITY);
            for (int i = 0; i < this.stateNumber; i++) {
				holdingDiagonalValues[this.stateNumber + i] = this.startStates[i];
			}

            final Function_Index fn = this.getExtendedPolygonIterator();
            fn.fn(0);
            final IntStack iS = new IntStack(1000);
            final ForwardBackwardMatrixIter.AlignmentGenerator aG = new ForwardBackwardMatrixIter.AlignmentGenerator(
                    this.stateNumber, 0,
                    Float.NEGATIVE_INFINITY, floatStack, iS);
            final ForwardBackwardMatrixIter fBMI = new ForwardBackwardMatrixIter(
                    fn, this.stateNumber, 0, 0, 0, 
                    holdingDiagonalValues,
                    diagonalValues,
                    reverseDiagonalValues, this.endStates,
                    this.setCellsForwardsLL, this.setCellsBackwardsLL,
                    this.setCellsForwardsL, this.setCellsForwardsR,
                    this.setCellsBackwardsL, this.setCellsBackwardsR,
                    //setCellsForwards, setCellsBackwards,
                    aG.forwards, aG.backwards, aG.passRunningTotal, this.mI, new FloatStack(1000), 50, aG.reset);
            final Function_Index_3Args pMIT = ForwardBackwardMatrixIter
                    .cutPointAlignmentGenerator(fBMI);

            Assert.assertTrue(floatStack.empty());
            int diag = 0;
            while (true) {
                diag += 2 + (int) (Math.random() * (this.width - 1
                        + this.height - 1 - diag));
                if (diag < this.width - 1 + this.height - 1) {
					fBMI.addPolygon(diag);
				} else {
					break;
				}
            }
            final int[] dC = (int[]) pMIT.fn(this.width - 1, this.height - 1, 0);
            final int mark = iS.getMark();
            final float[][][] matrixComputed = new float[this.width][this.height][1];
            while(!iS.empty()) {
                final int x = iS.gen();
                final int y = iS.gen();
                final float w = Float.intBitsToFloat(iS.gen());
                matrixComputed[x][y][0] = w;
            }
            for (int i = 0; i < this.width; i++) {
                for (int j = 0; j < this.height; j++) {
                    if ((pM[i][j][0] != Float.NEGATIVE_INFINITY)
                            && !((i == 0) && (j == 0))
                            && !((i == this.width - 1) && (j == this.height - 1))) {
                        Assert.assertEquals(
                                pM[i][j][0]
                                        - total,
                                matrixComputed[i][j][0],
                                0.1);
                    }
                }
            }
            for(final Iterator it=this.lLTL.iterator(); it.hasNext();) {
                final int[] iA = (int[])it.next();
                if(matrixComputed[iA[0]][iA[1]][0] != 0) {
					Assert.fail(matrixComputed[iA[0]][iA[1]][0] + " ");
				}
            }
            for(final Iterator it=this.rLTL.iterator(); it.hasNext();) {
                final int[] iA = (int[])it.next();
                if(matrixComputed[iA[0]][iA[1]][0] !=  0) {
					Assert.fail(matrixComputed[iA[0]][iA[1]][0] + " ");
				}
            }
        }
    }

    public void testGetEndDistribution() throws Exception {
        final Random rand = new Random();
        for (int trial = 0; trial < 100; trial++) {
            this.setup(0);
            final Object[] oA = (Object[]) this.polygonClipper.fn(rand
                    .nextInt(this.height + this.width - 1));
            final int[] dC = (int[]) oA[2];//1];
            final float[] diagValues = new float[10000];
            final int r = (int) (Math.random() * (dC[4] - dC[6] + 2));
            ForwardBackwardMatrixIter.getReversedEndDistribution(dC,
                    this.stateNumber, dC[6] - dC[7] - 2 + (r * 2 + 1),
                    this.endStates, diagValues);
            final int diagSize = ((dC[4] - dC[6] + 2) * 2 + 1)
                    * this.stateNumber;
            Array.reverseArray(diagValues, 0, diagSize);
            int i = 0;
            for (; i < (r * 2 + 1) * this.stateNumber; i++) {
				Assert.assertEquals(diagValues[i], Float.NEGATIVE_INFINITY, 0);
			}
            for (int j = 0; j < this.stateNumber; j++) {
				Assert.assertEquals(diagValues[i + j], this.endStates[j], 0.00000001);
			}
            i += this.stateNumber;
            for (; i < diagSize; i++) {
				Assert.assertEquals(diagValues[i], Float.NEGATIVE_INFINITY, 0.000000001);
			}
        }
    }

    public void testRescaleLine() {

    }

    static final boolean isOnTheLine(final int[] lTA, final int diag, final int max) {
        int i = 0;
        while (lTA[i] != Integer.MAX_VALUE) {
            final int k = lTA[i] + lTA[i + 1];
            if (k == diag) {
                return true;
            }
            i += 2;
            if(i >= max) {
				return false;
			}
        }
        return false;
    }
}