/*
 * Created on Jun 28, 2005
 */
package bp.pecan;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import bp.common.ds.FloatStack;
import bp.common.fp.Function_Index;
import bp.common.fp.Function_Index_3Args;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_NoArgs;
import bp.common.io.Debug;
import bp.common.maths.Maths;

/**
 * @author benedictpaten
 */
public final class ForwardBackwardMatrixIter {

    static final Logger logger = Logger
            .getLogger(ForwardBackwardMatrixIter.class.getName());

    List<Object[]> fStack = new LinkedList<Object[]>(), rStack = new LinkedList<Object[]>();

    boolean holdingFDiagonalEmpty;

    final float[] holdingFDiagonal;

    final float[] fDiag;

    final float[] rDiag;

    final float[] eStates;

    int x0, y0, fDiagC, uptoDiagC;

    final Function_Index polygonGen;

    final List<Object[]> polygonsToMerge;

    final int stateNumber;

    final Cell.CellCalculator computeCellsFLL;

    final Cell.CellCalculator computeCellsBLL;

    final Cell.CellCalculator computeCellsFL;

    final Cell.CellCalculator computeCellsBL;

    final Cell.CellCalculator computeCellsFR;

    final Cell.CellCalculator computeCellsBR;

    final Cell.CellCalculator passCellsF, passCellsB;

    final Procedure_Int passRunningTotal;

    final Procedure_NoArgs reset;

    final MatrixIterator mI;

    final FloatStack lineStack;

    final int maxDistanceBetweenRescale;

    // static int cellsComputed12 = 0;

    /**
     * 
     */
    public ForwardBackwardMatrixIter(final Function_Index polygonGen,
            final int stateNumber, final int x0, final int y0, final int fDiagC,
            final float[] holdingFDiagonal, final float[] fDiag, final float[] rDiag,
            final float[] eStates, final Cell.CellCalculator computeCellsFLL,
            final Cell.CellCalculator computeCellsBLL,
            final Cell.CellCalculator computeCellsFL,
            final Cell.CellCalculator computeCellsFR,
            final Cell.CellCalculator computeCellsBL,
            final Cell.CellCalculator computeCellsBR,
            final Cell.CellCalculator passCellsF,
            final Cell.CellCalculator passCellsB,
            final Procedure_Int passRunningTotal, final MatrixIterator mI,
            final FloatStack lineStack, final int maxDistanceBetweenRescale,
            final Procedure_NoArgs reset) {
        this.polygonGen = polygonGen;
        this.stateNumber = stateNumber;
        this.x0 = x0;
        this.y0 = y0;
        this.uptoDiagC = x0 + y0;
        this.fDiagC = fDiagC;
        this.holdingFDiagonal = holdingFDiagonal;
        this.fDiag = fDiag;
        this.rDiag = rDiag;
        this.eStates = eStates;
        this.computeCellsFLL = computeCellsFLL;
        this.computeCellsBLL = computeCellsBLL;
        this.computeCellsFL = computeCellsFL;
        this.computeCellsFR = computeCellsFR;
        this.computeCellsBL = computeCellsBL;
        this.computeCellsBR = computeCellsBR;
        this.passCellsF = passCellsF;
        this.passCellsB = passCellsB;
        this.passRunningTotal = passRunningTotal;
        this.mI = mI;
        this.polygonsToMerge = new LinkedList<Object[]>();
        this.lineStack = lineStack;
        this.maxDistanceBetweenRescale = maxDistanceBetweenRescale;
        this.reset = reset;
    }

    final Object[] getPolygon(final int diag) {
        while (this.polygonsToMerge.size() != 0) {
            this.addPolygon(this.polygonsToMerge.remove(0));
        }
        final Object[] p = (Object[]) this.polygonGen.fn(diag);
        final int[] dC = (int[]) p[2];
        this.uptoDiagC = dC[6] + dC[7];
        return p;
    }

    public final void addPolygon(final int diag) {
        int i = Integer.MIN_VALUE;
        while (true) {
            if (this.currentDiag() + this.maxDistanceBetweenRescale < (diag - 1)) {
				i = this.currentDiag() + this.maxDistanceBetweenRescale;
			} else if (i < diag) {
				i = diag;
			} else {
				break;
			}
            this.addPolygon(this.getPolygon(i));
        }
    }

    private final void addPolygon(final Object[] p) {
        this.rStack.add(new Object[] { p,
                new BackwardEvent(new Cell.CellCalculator() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int,
                     *      int)
                     */
                    public final void calc(final float[] cells, final int offset,
                            final int x, final int y) {
                        ForwardBackwardMatrixIter.this.computeCellsBL.calc(cells, offset, x, y);
                        ForwardBackwardMatrixIter.this.passCellsB.calc(cells, offset, x, y);
                    }
                }, new Cell.CellCalculator() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int,
                     *      int)
                     */
                    public final void calc(final float[] cells, final int offset,
                            final int x, final int y) {
                        ForwardBackwardMatrixIter.this.computeCellsBR.calc(cells, offset, x, y);
                        ForwardBackwardMatrixIter.this.passCellsB.calc(cells, offset, x, y);
                    }
                }, this.mI) });
        this.fStack.add(new Object[] { p,
                new ForwardEvent(new Cell.CellCalculator() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int,
                     *      int)
                     */
                    public final void calc(final float[] cells, final int offset,
                            final int x, final int y) {
                        ForwardBackwardMatrixIter.this.computeCellsFL.calc(cells, offset, x, y);
                        ForwardBackwardMatrixIter.this.passCellsF.calc(cells, offset, x, y);
                    }
                }, new Cell.CellCalculator() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int,
                     *      int)
                     */
                    public final void calc(final float[] cells, final int offset,
                            final int x, final int y) {
                        ForwardBackwardMatrixIter.this.computeCellsFR.calc(cells, offset, x, y);
                        ForwardBackwardMatrixIter.this.passCellsF.calc(cells, offset, x, y);
                    }
                }, this.mI) });
    }

    public final void addCutPointGap(final int diag) {
        final Object[] p = this.getPolygon(diag);
        this.rStack
                .add(new Object[] {
                        p,
                        new BackwardEvent(this.computeCellsBL,
                                this.computeCellsBR, this.mI) });
        this.polygonsToMerge.add(p);
    }

    public final int currentDiag() {
        return this.uptoDiagC;
    }

    public final void cutPoint(final int cutX, final int cutY) {
        System.arraycopy(this.holdingFDiagonal, 0, this.fDiag, 0,
                ((this.fDiagC + 1) * 2 + 1) * this.stateNumber);
        final int[] dC = (int[]) ((Object[]) this.rStack.get(this.rStack
                .size() - 1)[0])[2];
        ForwardBackwardMatrixIter.getReversedEndDistribution(dC, this.stateNumber, cutX - cutY,
                this.eStates, this.rDiag);
        this.traceBackEvents(dC[4] + 1, dC[7] + 1, dC[4] - dC[6] + 1);
        System.arraycopy(this.fDiag, 0, this.holdingFDiagonal, 0,
                ((this.fDiagC + 1) * 2 + 1) * this.stateNumber);
    }

    static interface Event {
        /**
         * Event in the forward backward dynamic programming matrix.
         * 
         * @param x0
         *            column corner coordinate of matrix
         * @param y0
         *            row corner coordinate of matrix
         * @param initialDiagonal
         *            x+y diagonal coordinate of diag as offset from x0, y0
         * @param fDiagC
         *            input diagonal
         * @param dC
         *            coordinates of polygon, see
         *            {@link PolygonFiller#clipPolygons(Iterator, Iterator)}
         * @param eT
         *            edge table of polygon
         * @return
         */
        void event(int x, int y, int diagC, float[] diag, int[] dC,
                List lET, List rET, int[] lLTA, int[] rLTA);
    }

    final void traceBackEvents(int x1, int y1, int rDiagC) {
        int[] dC = null;
        if (this.polygonsToMerge.size() != 0) {
            // has cut point, but no need to store reverse diag
            ForwardBackwardMatrixIter.rescaleLine(this.rDiag, ((rDiagC + 1) * 2 + 1) * this.stateNumber);
            final Object[] oA = this.rStack.remove(this.rStack.size() - 1);
            final Object[] p = (Object[]) oA[0];
            dC = (int[]) p[2];
            ((Event) oA[1]).event(x1, y1, rDiagC, this.rDiag, dC,
                    (List) p[0], (List) p[1], (int[]) p[3],
                    (int[]) p[4]);
            x1 = dC[2];
            y1 = dC[1];
            rDiagC = x1 - dC[0];
        }
        while (this.rStack.size() != 0) {
            ForwardBackwardMatrixIter.rescaleLine(this.rDiag, ((rDiagC + 1) * 2 + 1) * this.stateNumber);
            final Object[] oA = this.rStack.remove(this.rStack.size() - 1);
            final Object[] p = (Object[]) oA[0];
            dC = (int[]) p[2];
            ((Event) oA[1]).event(x1, y1, rDiagC, this.rDiag, dC,
                    (List) p[0], (List) p[1], (int[]) p[3],
                    (int[]) p[4]);
            final int rDiagonalSize = ((dC[2] - dC[0] + 1) * 2 + 1)
                    * this.stateNumber;
            for (int i = 0; i < rDiagonalSize; i++) {
				// push backwards
                this.lineStack.stuff(this.rDiag[i]);
			}
            x1 = dC[2];
            y1 = dC[1];
            rDiagC = x1 - dC[0];
        }
        while (this.fStack.size() != 0) {
            final Object[] oA = this.fStack.remove(0);
            final Object[] p = (Object[]) oA[0];
            dC = (int[]) p[2];
            // rescale line
            ForwardBackwardMatrixIter.rescaleLine(this.fDiag, ((this.fDiagC + 1) * 2 + 1) * this.stateNumber);
            final int rDiagonalSize = ((dC[2] - dC[0] + 1) * 2 + 1)
                    * this.stateNumber;
            for (int i = 0; i < rDiagonalSize; i++) {
				// reversed line
                this.rDiag[i] = this.lineStack.unstuff();
			}
            // add together diagonals
            final int[] lLTA = (int[]) p[3];
            final int[] rLTA = (int[]) p[4];
            // int fD = dC[0] + dC[1];
            final boolean onForwardLineL = (lLTA[0] != Integer.MAX_VALUE)
                    && (lLTA[0] == dC[0]) && (lLTA[1] == dC[1]); // lLTA[0] +
            // lLTA[1] ==
            // fD;
            final boolean onBackwardLineL = (lLTA[0] != Integer.MAX_VALUE)
                    && (lLTA[0] == dC[0]) && (lLTA[1] == dC[1] + 1); // lLTA[0] +
            // lLTA[1]
            // == fD+1;
            final boolean onForwardLineR = (rLTA[0] != Integer.MAX_VALUE)
                    && (rLTA[0] == dC[2]) && (rLTA[1] == dC[3]); // rLTA[0] +
            // rLTA[1] ==
            // fD;
            final boolean onBackwardLineR = (rLTA[0] != Integer.MAX_VALUE)
                    && (rLTA[0] == dC[2] + 1) && (rLTA[1] == dC[3]); // rLTA[0] +
            // rLTA[1]
            // == fD+1;
            this.passRunningTotal.pro(Float
                    .floatToRawIntBits(this.addTogetherDiagonals(this.x0, this.y0,
                            this.fDiagC, this.fDiag, dC, this.rDiag, onForwardLineL,
                            onBackwardLineL, onForwardLineR,
                            onBackwardLineR)));
            ((Event) oA[1]).event(this.x0, this.y0, this.fDiagC, this.fDiag, dC,
                    (List) p[0], (List) p[1], lLTA, rLTA);
            this.x0 = dC[6];
            this.y0 = dC[5];
            this.fDiagC = dC[4] - this.x0;
        }
        this.reset.pro();
        if (Debug.DEBUGCODE && !this.lineStack.empty()) {
			throw new IllegalStateException(this.lineStack.getMark()
                    + " not empty ");
		}
    }

    /**
     * Returns outline diagonal coordinates of current stack. In the same format
     * as {@link PolygonFiller#clipPolygons(Iterator, Iterator)}. Will throw an
     * exception if the stack is empty.
     * 
     * @return
     */
    final int[] getPolygonCoordinatesOfCurrentStack() {
        final int[] dC = new int[8];
        if (Debug.DEBUGCODE && (this.rStack.size() == 0)) {
			throw new IllegalStateException();
		}
        final int[] fC = (int[]) ((Object[]) this.rStack.get(0)[0])[2];
        final int[] eC = (int[]) ((Object[]) this.rStack.get(this.rStack
                .size() - 1)[0])[2];
        System.arraycopy(fC, 0, dC, 0, 4);
        System.arraycopy(eC, 4, dC, 4, 4);
        return dC;
    }

    /**
     * Creates a reversed diagonal for computing a polygon in reverse. Inputs
     * must all be in the forward direction and the output diagonal will be in
     * the reverse direction.
     * 
     * @param dC
     *            coordinates of polygon to be reversed
     * @param stateNumber
     *            number of states
     * @param cutPointDiagonal
     *            x-y diagonal of the cut point
     * @param endStates
     *            in forward direction
     * @param diagonalValues
     *            values to be filled with reverse diagonal
     * @param Float.NEGATIVE_INFINITY
     *            value which should be used as initial values for computations
     */
    final static void getReversedEndDistribution(final int[] dC,
            final int stateNumber, final int cutPointDiagonal, final float[] endStates,
            final float[] diagonalValues) {
        final int initialDiagonal = dC[4] - dC[6] + 2, initialDiagonalSize = (1 + 2 * initialDiagonal)
                * stateNumber;
        Arrays.fill(diagonalValues, 0, initialDiagonalSize,
                Float.NEGATIVE_INFINITY);
        final int offset = initialDiagonalSize - 1
                - (cutPointDiagonal - (dC[6] - dC[7] - 2))
                * stateNumber;
        if (Debug.DEBUGCODE && (offset > initialDiagonalSize - 1)) {
			throw new ArrayIndexOutOfBoundsException();
		}
        for (int i = 0; i < stateNumber; i++) {
			diagonalValues[offset - i] = endStates[i];
		}
    }

    final float addTogetherDiagonals(final int x0, final int y0, final int fDiagC,
            final float[] forwardDiag, final int[] dC, final float[] reverseDiag,
            final boolean isOnForwardLineL, final boolean isOnBackwardLineL,
            final boolean isOnForwardLineR, final boolean isOnBackwardLineR) {
        final int rDiagonalSize = (dC[2] - dC[0] + 1) * 2 + 1;
        int forwardDiagonalOffsetStart, forwardDiagonalOffsetEnd;
        float[] iA;
        if (x0 + y0 + fDiagC + 2 == dC[0] + dC[1]) {
            // two
            // apart, so
            // must be
            // corrected
            // for!
            forwardDiagonalOffsetStart = 1;
            forwardDiagonalOffsetEnd = 1;
            iA = new float[this.stateNumber];
            System.arraycopy(forwardDiag, this.stateNumber, iA, 0,
                    this.stateNumber);
        } else {
            forwardDiagonalOffsetStart = (x0 - y0 - fDiagC - 1)
                    - (dC[0] - dC[1] - 1);
            forwardDiagonalOffsetEnd = (x0 - y0 + fDiagC + 1)
                    - (dC[0] - dC[1] - 1);
            iA = forwardDiag;
        }
        return ForwardBackwardMatrixIter.addTogetherDiagonals(iA, reverseDiag, rDiagonalSize,
                forwardDiagonalOffsetStart, forwardDiagonalOffsetEnd,
                dC[0], dC[1] + 1, this.computeCellsFLL, this.stateNumber,
                isOnForwardLineL, isOnBackwardLineL,
                isOnForwardLineR, isOnBackwardLineR);
    }

    /**
     * Gets the total probability flowing through two diagonals.
     * 
     * @param forwardDiagonal
     * @param reverseDiagonals
     * @param diagonalSize
     *            size of the reverse diagonal
     * @param forwardDiagonalOffsetStart
     *            offset of the leftmost point forward diagonal from the left
     *            most point of the reverse diagonal in x - y coordinates
     * @param forwardDiagonalOffsetEnd
     *            offset of the rightmost point in the forward diagonal from the
     *            left most point of the reverse diagonal in x - y coordinates
     * @param xStart
     *            x coordinate of the bottom left point on the reverseDiagonal
     * @param yStart
     *            y coordinate of the bottom left point on the reverseDiagonal
     * @param computeCellsForward
     * @param setXY
     * @param adder
     * @param lAdder
     * @param stateNumber
     * @param Float.NEGATIVE_INFINITY
     *            base value in calculations
     * 
     * @return
     */
    final static float addTogetherDiagonals(final float[] forwardDiagonal,
            final float[] reverseDiagonals, int diagonalSize,
            final int forwardDiagonalOffsetStart,
            final int forwardDiagonalOffsetEnd, final int xStart, final int yStart,
            final Cell.CellCalculator computeCellsForward, final int stateNumber,
            boolean isOnForwardLineL, final boolean isOnBackwardLineL,
            boolean isOnForwardLineR, final boolean isOnBackwardLineR) {
        if (isOnBackwardLineR) {
            diagonalSize -= 1;
        }
        float total = Float.NEGATIVE_INFINITY;
        final float[] cells = new float[4 * stateNumber];
        Arrays.fill(cells, Float.NEGATIVE_INFINITY);
        for (int i = forwardDiagonalOffsetStart > 0 ? forwardDiagonalOffsetStart
                + (forwardDiagonalOffsetStart % 2)
                : (isOnBackwardLineL ? 2 : 0); (i < diagonalSize)
                && (i < forwardDiagonalOffsetEnd); i += 2) {
            System.arraycopy(forwardDiagonal,
                    (i - forwardDiagonalOffsetStart) * stateNumber,
                    cells, stateNumber * 2, stateNumber);
            computeCellsForward.calc(cells, 0, xStart + i / 2, yStart
                    - i / 2);
            for (int j = 0; j < stateNumber; j++) {
				total = Maths.logAdd(total, cells[j]
                        + reverseDiagonals[stateNumber * i + j]);
			}
        }
        for (int i = 1; i < diagonalSize; i += 2) {
            if (i - 1 >= forwardDiagonalOffsetStart) {
				System.arraycopy(forwardDiagonal, stateNumber
                        * (i - 1 - forwardDiagonalOffsetStart),
                        cells, stateNumber, stateNumber);
			}
            if (!isOnForwardLineL || (i > 1)) {
                if (!isOnForwardLineR || (i + 2 < diagonalSize)) {
					System.arraycopy(forwardDiagonal,
                            (i - forwardDiagonalOffsetStart)
                                    * stateNumber, cells,
                            stateNumber * 2, stateNumber);
				} else {
					Arrays.fill(cells, stateNumber * 2,
                            stateNumber * 3, Float.NEGATIVE_INFINITY);
				}
            } else {
				Arrays.fill(cells, stateNumber * 2, stateNumber * 3,
                        Float.NEGATIVE_INFINITY);
			}
            if (i + 1 < forwardDiagonalOffsetEnd) {
				System.arraycopy(forwardDiagonal, stateNumber
                        * (i + 1 - forwardDiagonalOffsetStart),
                        cells, stateNumber * 3, stateNumber);
			} else {
				Arrays.fill(cells, stateNumber * 3, cells.length,
                        Float.NEGATIVE_INFINITY);
			}
            computeCellsForward.calc(cells, 0, xStart + i / 2, yStart
                    - i / 2 - 1);
            for (int j = 0; j < stateNumber; j++) {
				total = Maths.logAdd(total, cells[j]
                        + reverseDiagonals[stateNumber * i + j]);
			}
        }
        return total;
    }

    public static final class ForwardEvent implements Event {
        Cell.CellCalculator forwardsL;

        Cell.CellCalculator forwardsR;

        MatrixIterator mI;

        public ForwardEvent(final Cell.CellCalculator forwardsL,
                final Cell.CellCalculator forwardsR, final MatrixIterator mI) {
            this.forwardsL = forwardsL;
            this.forwardsR = forwardsR;
            this.mI = mI;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bp.pecan.dpc.ForwardBackwardMatrixIter.Event#event(int, int,
         *      int, int[], int[], java.util.List)
         */
        public final void event(int x, int y, final int diagC,
                final float[] diag, final int[] dC, final List lET, final List rET,
                final int[] lLTA, final int[] rLTA) {
            // occurs after backwards event so no need to clone
            PolygonFiller.transformEdgeList(lET, -x, -y);
            PolygonFiller.transformEdgeList(rET, -x, -y);
            final int[] iA = new int[lLTA.length];
            final int[] iA2 = new int[rLTA.length];
            PolygonFiller.transformCoordinates(lLTA, iA, -x, -y);
            PolygonFiller.transformCoordinates(rLTA, iA2, -x, -y);
            this.mI.scanPolygon(lET, rET, dC[4] - x + 1, dC[7] - y + 1,
                    diag, diagC, ForwardBackwardMatrixIter.addToArgs(x, y, this.forwardsL),
                    ForwardBackwardMatrixIter.addToArgs(x, y, this.forwardsR), iA, iA2);
            this.mI.getFinalValues(diag, dC[4] + dC[5] - x - y);
        }
    }

    public static final class BackwardEvent implements Event {
        Cell.CellCalculator backwardsL;

        Cell.CellCalculator backwardsR;

        MatrixIterator mI;

        public BackwardEvent(final Cell.CellCalculator backwardsL,
                final Cell.CellCalculator backwardsR, final MatrixIterator mI) {
            this.backwardsL = backwardsL;
            this.backwardsR = backwardsR;
            this.mI = mI;
        }

        /*
         * (non-Javadoc)
         * 
         * @see bp.pecan.dpc.ForwardBackwardMatrixIter.Event#rEvent(int, int,
         *      int, int[], int[], java.util.List)
         */
        public final void event(final int x, final int y, final int diagC,
                final float[] diag, final int[] dC, List lET, List rET,
                final int[] lLTA, final int[] rLTA) {
            lET = PolygonFiller.reverseCoordinates(lET, x, y);
            rET = PolygonFiller.reverseCoordinates(rET, x, y);
            final int[] lLTAr = new int[lLTA.length];
            PolygonFiller.reverseLessThanCoordinates(lLTA, lLTAr, x,
                    y);
            final int[] rLTAr = new int[rLTA.length];
            PolygonFiller.reverseLessThanCoordinates(rLTA, rLTAr, x,
                    y);
            this.mI.scanPolygon(rET, lET, x - dC[0] + 1, y - dC[3] + 1,
                    diag, diagC, ForwardBackwardMatrixIter.subtractArgsFrom(x, y, this.backwardsL),
                    ForwardBackwardMatrixIter.subtractArgsFrom(x, y, this.backwardsR), rLTAr, lLTAr);
            this.mI.getFinalValues(diag, x + y - dC[0] - dC[1]);
        }
    }

    static final Cell.CellCalculator addToArgs(final int i,
            final int j, final Cell.CellCalculator calc) {
        return new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */
            public final void calc(final float[] cells, final int offset, final int x,
                    final int y) {
                calc.calc(cells, offset, x + i, y + j);
            }
        };
    }

    static final Cell.CellCalculator subtractArgsFrom(final int i,
            final int j, final Cell.CellCalculator calc) {
        return new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */
            public final void calc(final float[] cells, final int offset, final int x,
                    final int y) {
                calc.calc(cells, offset, i - x, j - y);
            }
        };
    }

    public static final void rescaleLine(final float[] line, final int lineLength) {
        float k = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < lineLength; i++) {
			k = Maths.logAdd(k, line[i]);
		}
        k -= lineLength;
        for (int i = 0; i < lineLength; i++) {
            line[i] -= k;
        }
    }

    public static final class AlignmentGenerator {

        float total;

        final int matchState, reverseMatchState;

        final float greaterThanThreshold;

        final FloatStack stack;

        final Procedure_Int output;

        public AlignmentGenerator(final int stateNumber, final int matchState,
                final float greaterThanThreshold, final FloatStack stack,
                final Procedure_Int output) {
            this.matchState = matchState;
            this.reverseMatchState = stateNumber - 1 - matchState;
            this.greaterThanThreshold = greaterThanThreshold;
            this.stack = stack;
            this.output = output;
        }

        public final Cell.CellCalculator forwards = new Cell.CellCalculator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(float[], int, int)
             */
            public final void calc(float[] cells, int offset, int x,
                    int y) {
            	//System.out.println("computing cell" + x + " " + y + "\n");
                float i = (cells[offset + AlignmentGenerator.this.matchState] + AlignmentGenerator.this.stack
                        .unstuff())
                        - AlignmentGenerator.this.total;
                if (Debug.DEBUGCODE && Float.isNaN(i)) {
					throw new IllegalStateException(
                            " Value is not a number " + x + " " + y
                                    + " "
                                    + cells[offset + AlignmentGenerator.this.matchState]
                                    + " " + AlignmentGenerator.this.total);
				}
                if (i > AlignmentGenerator.this.greaterThanThreshold) {
                    // if(outputValues)
                    // System.out.println(" basepair " + x + " " + y + " " +
                    // Maths.exp(i));
                    if (Debug.DEBUGCODE && (i > 0.1)) {
						ForwardBackwardMatrixIter.logger.info(" Value greater than one "
                                + Math.exp(i) + " " + AlignmentGenerator.this.total + " " + x
                                + " " + y);
					}
                    AlignmentGenerator.this.output.pro(Float
                            .floatToRawIntBits(i > 0.0f ? 0.0f : i));
                    AlignmentGenerator.this.output.pro(y);
                    AlignmentGenerator.this.output.pro(x);
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
            		//System.out.println("computing cell back" + x + " " + y + "\n");
                    AlignmentGenerator.this.stack.stuff(cells[offset
                            + AlignmentGenerator.this.reverseMatchState]);
            }
        };

        public final Procedure_Int passRunningTotal = new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public final void pro(int i) {
                AlignmentGenerator.this.total = Float.intBitsToFloat(i);
                if (Debug.DEBUGCODE
                        && (Float.isNaN(AlignmentGenerator.this.total) || Float
                                .isInfinite(AlignmentGenerator.this.total))) {
					throw new IllegalStateException(
                            " Total is unacceptable " + AlignmentGenerator.this.total);
				}
            }
        };

        public final Procedure_NoArgs reset = new Procedure_NoArgs() {
            public final void pro() {
                if (Debug.DEBUGCODE && !AlignmentGenerator.this.stack.empty()) {
                    // check that all is okay
                    throw new IllegalStateException();
                }
            }
        };
    }

    public static Function_Index_3Args cutPointAlignmentGenerator(
            final ForwardBackwardMatrixIter fBMI) {
        return new Function_Index_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Index_3Args#fn(int, int, int)
             */
            public final Object fn(final int i, final int j, final int k) {
                final int d = fBMI.currentDiag(), d2 = i + j - k - 1;
                if (Debug.DEBUGCODE && (d > d2)) {
					throw new IllegalStateException(d + " " + d2);
				}
                if (d < d2) {
					fBMI.addPolygon(d2);
				}
                final int[] dC = fBMI.getPolygonCoordinatesOfCurrentStack();
                if (k != 0) {
					fBMI.addCutPointGap(i + j - 1);
				}

                /*
                 * { int xStart = 1000; int yStart = 2000; if (i > xStart && j >
                 * yStart) { Debug.pl(i + " " + j); //System.exit(0); if(i <
                 * 1999 && j < 2999) fBMI.addPolygon(2000 + 3000);
                 * //fn.fn(xStart + yStart);
                 * 
                 * List three = (List) GeneratorTools.append( Pecan.anchorGen12,
                 * new LinkedList());
                 * 
                 * int xOffset = 0; int yOffset = 0; List lessThans = new
                 * LinkedList(); int[] iA = PolygonFillerTest
                 * .getDisplayMatrix(900, 900);
                 * PolygonFiller.transformEdgeList(three, -xStart - xOffset,
                 * -yStart - yOffset); //int[] polysA = new int[] { 500, 750,
                 * 2000 }; for (int polys = 0; polys < fBMI.rStack.size();
                 * polys++) { Object[] oA2 = (Object[])fBMI.rStack.get(polys);
                 * //(Object[]) fn.fn(xStart // + yStart + polysA[polys]); oA2 =
                 * (Object[])oA2[0]; List one = (List) oA2[0]; List two = (List)
                 * oA2[1]; for (Iterator it = one.iterator(); it .hasNext();) {
                 * PolygonFiller.Node n = (PolygonFiller.Node) it .next();
                 * Debug.pl(n + " one "); } for (Iterator it = two.iterator();
                 * it .hasNext();) { PolygonFiller.Node n = (PolygonFiller.Node)
                 * it .next(); Debug.pl(n + " two "); }
                 * //PolygonFillerTest.checkPolygon(one, two); one =
                 * PolygonFiller.cloneEdgeList(one); two =
                 * PolygonFiller.cloneEdgeList(two);
                 * PolygonFiller.transformEdgeList(one, -xStart - xOffset,
                 * -yStart - yOffset); PolygonFiller.transformEdgeList(two,
                 * -xStart - xOffset, -yStart - yOffset);
                 * PolygonFillerTest.addLines(iA, one, 900, 900,
                 * PolygonFillerTest.LEFT_COLOUR);
                 * PolygonFillerTest.addLines(iA, two, 900, 900,
                 * PolygonFillerTest.RIGHT_COLOUR); int[] lLTA = (int[]) oA2[3];
                 * int[] rLTA = (int[]) oA2[4]; lessThans.add(lLTA.clone());
                 * lessThans.add(rLTA.clone()); }
                 * 
                 * PolygonFillerTest.fillInHorizontalRowsLeft( iA, 900, 900,
                 * PolygonFillerTest.LEFT_COLOUR,
                 * PolygonFillerTest.BACK_GROUND_COLOUR);
                 * //PolygonFillerTest.fillInHorizontalRowsLeft( // iA, 900,
                 * 900, // PolygonFillerTest.RIGHT_COLOUR,
                 * PolygonFillerTest.BACK_GROUND_COLOUR);
                 * //PolygonFillerTest.addLines(iA, three, 900, // 900,
                 * PolygonFillerTest.ANCHOR_COLOUR);
                 * //PolygonFillerTest.fillInHorizontalRowsRight( // iA, 900,
                 * 900, // PolygonFillerTest.LEFT_COLOUR,
                 * PolygonFillerTest.BACK_GROUND_COLOUR);
                 * PolygonFillerTest.fillInHorizontalRowsRight( iA, 900, 900,
                 * PolygonFillerTest.RIGHT_COLOUR,
                 * PolygonFillerTest.BACK_GROUND_COLOUR);
                 * PolygonFillerTest.addLines(iA, three, 900, 900,
                 * PolygonFillerTest.ANCHOR_COLOUR); for(Iterator
                 * it2=lessThans.iterator(); it2.hasNext();) { int[] lLTA =
                 * (int[])it2.next(); for(int xx=0; xx<lLTA.length; xx+=2)
                 * Debug.pl(" lLTA " + lLTA[xx] + " " + lLTA[xx+1] + " ");
                 * PolygonFillerTest.addPoints(iA, 900, 900, lLTA, xStart +
                 * xOffset, yStart + yOffset); //PolygonFillerTest.addPoints(iA,
                 * 900, 900, // rLTA, xStart + xOffset, yStart // + yOffset);
                 * //for(int xx=0; xx<rLTA.length; xx+=2) // Debug.pl(" rLTA " +
                 * rLTA[xx] + " " + rLTA[xx+1] + " "); } fBMI.cutPoint(i, j);
                 * //PolygonFillerTest.addPointsValue(iA, 900, 900,
                 * Pecan.pointGen12, xStart + xOffset, yStart + yOffset,
                 * PolygonFillerTest.BACK_GROUND_COLOUR);
                 * //PolygonFillerTest.addColourKey(iA, 100, 500, 900, 900, 20);
                 * PolygonFillerTest.displayMatrix(iA, 900, 900); } }
                 */

                fBMI.cutPoint(i, j);
                return dC;
            }
        };
    }
}