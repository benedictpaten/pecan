/*
 * Created on Apr 8, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.List;

import bp.common.fp.Procedure_Int_2Args;
import bp.common.io.Debug;

public final class MatrixIterator implements Procedure_Int_2Args {

    public MatrixIterator(final int stateNumber, int lineLength) {
        lineLength = (lineLength + 1) * stateNumber * 2;
        this.line = new float[lineLength];
        this.stateNumber = stateNumber;
        this.scratch = new float[stateNumber];
    }

    private final int stateNumber;

    private Cell.CellCalculator calcL, calcR;

    private Cell.CellCalculator calc;

    final float[] line;

    final float[] scratch;

    private boolean lineFlip;

    private int y;

    int width;

    int height;

    int offset;

    private int initialDiagonal;

    int[] lLTA;

    int[] rLTA;

    int lLTAi;

    int rLTAi;

    int lLT;

    int rLT;

    public final void pro(int x1, final int x2) {
        this.offset = (x1 + 1) * this.stateNumber;
        this.lineFlip = !this.lineFlip;
        if (this.lineFlip) {
            this.calc = this.calcR;
            this.offset += this.line.length / 2;
        } else {
			this.calc = this.calcL;
		}
        if (this.y > this.initialDiagonal) {
            Arrays.fill(this.line, this.offset - this.stateNumber, this.offset,
                    Float.NEGATIVE_INFINITY);
        }
        if (this.lLT == this.y) {
            /*
             * if (Debug.DEBUGCODE && lLTA[lLTAi] != x1) throw new
             * IllegalStateException(lLTA[lLTAi] + " " + x1 + " " + x2 + " " + y + " " +
             * lLTA[lLTAi + 1]);
             */
            if (this.lLTA[this.lLTAi] == x1) {
                final int i = this.offset
                        + (this.lineFlip ? -this.line.length / 2
                                : this.line.length / 2) - this.stateNumber;
                System.arraycopy(this.line, i, this.scratch, 0, this.stateNumber);
                Arrays.fill(this.line, i, i + this.stateNumber,
                        Float.NEGATIVE_INFINITY);
                this.calc.calc(this.line, this.offset, x1++, this.y);
                System.arraycopy(this.scratch, 0, this.line, i, this.stateNumber);
                this.offset += this.stateNumber;
            }
            this.lLTAi += 2;
            this.lLT = this.lLTA[this.lLTAi + 1];
        }
        if (this.rLT == this.y) {
            /*
             * if (Debug.DEBUGCODE && rLTA[rLTAi] > x2 + 2 || rLTA[rLTAi] < x1)
             * throw new IllegalStateException(rLTA[rLTAi] + " " + x1 + " " + x2 + " " +
             * y + " " + rLTA[rLTAi + 1]);
             */
            final int j = this.rLTA[this.rLTAi];
            if ((j >= x1) && (j <= x2)) {
                while (x1 < j) {
                    this.calc.calc(this.line, this.offset, x1++, this.y);
                    this.offset += this.stateNumber;
                }
                final int i = this.offset
                        + (this.lineFlip ? -this.line.length / 2
                                : this.line.length / 2) - this.stateNumber;
                System.arraycopy(this.line, i, this.scratch, 0, this.stateNumber);
                Arrays.fill(this.line, i, i + this.stateNumber,
                        Float.NEGATIVE_INFINITY);
                this.calc.calc(this.line, this.offset, x1++, this.y);
                System.arraycopy(this.scratch, 0, this.line, i, this.stateNumber);
                this.offset += this.stateNumber;
            }
            this.rLTAi += 2;
            this.rLT = this.rLTA[this.rLTAi + 1];
        }
        while (x1 <= x2) {
            this.calc.calc(this.line, this.offset, x1++, this.y);
            this.offset += this.stateNumber;
        }
        this.y++;
    }

    /**
     * Collects the cell values from previously scanned polygons. The values
     * collected lie along a x+y diagonal which spans from width-1 to height-1.
     * It is assumed that the scanned polygon was clipped along this diagonal
     * (inclusive) and that the edges of the diagonal spanned from width to
     * height exclusive, otherwise the returned values will be suprious.
     * 
     * @param finalValues
     */
    public final void getFinalValues(final float[] finalValues, final int diagonal) {
        if (Debug.DEBUGCODE
                && ((diagonal - Math.max(this.height, this.width) < -1) || (diagonal < 0))) {
			throw new IllegalStateException();
		}
        int i = diagonal - this.height + 1, j = 0;
        boolean b = this.lineFlip;
        for (; i < this.width; i++) {
            b = !b;
            if (b) {
				System.arraycopy(this.line, i * this.stateNumber, finalValues,
                        j++ * 2 * this.stateNumber, this.stateNumber * 2);
			} else {
				System.arraycopy(this.line, i * this.stateNumber + this.line.length
                        / 2, finalValues, j++ * 2 * this.stateNumber,
                        this.stateNumber * 2);
			}
        }
        if (!b) {
			System.arraycopy(this.line, i * this.stateNumber, finalValues, j
                    * 2 * this.stateNumber, this.stateNumber);
		} else {
			System.arraycopy(this.line, i * this.stateNumber + this.line.length / 2,
                    finalValues, j * 2 * this.stateNumber, this.stateNumber);
		}
    }

    /**
     * Set the cell values across the initial diagonal, diagonal must be zero or
     * greater and less than either the given height or width.
     * 
     * @param initialValues
     * @param diagonal
     */
    final void setInitialValues(final float[] initialValues, final int diagonal) {
        if (Debug.DEBUGCODE
                && ((Math.min(this.height, this.width) - diagonal <= 0) || (diagonal < 0))) {
			throw new IllegalStateException();
		}
        boolean b = diagonal % 2 != 0;
        int i = 0, j = 0;
        for (; i <= diagonal; i++) {
            b = !b;
            if (b) {
				System.arraycopy(initialValues,
                        j++ * 2 * this.stateNumber, this.line, i * this.stateNumber,
                        this.stateNumber * 2);
			} else {
				System.arraycopy(initialValues,
                        j++ * 2 * this.stateNumber, this.line, i * this.stateNumber
                                + this.line.length / 2, this.stateNumber * 2);
			}
        }
        if (!b) {
			System.arraycopy(initialValues, j * 2 * this.stateNumber,
                    this.line, i * this.stateNumber, this.stateNumber);
		} else {
			System.arraycopy(initialValues, j * 2 * this.stateNumber,
                    this.line, i * this.stateNumber + this.line.length / 2,
                    this.stateNumber);
		}
    }

    /**
     * Compute the pairwise dp matrix for the given polygon with the given x and
     * y sequences. The polygon must contain only two edges with negative
     * diagonals which must lie along the initial (+1 exclusive) and final
     * diagonals (inclusive). The polygon may be offset on the y axis from the
     * top of the initial diagonal by 0 or 1. The final diagonal is bounded by
     * the given width and height. The lowest y coordinate of the polygon must
     * be at height-1 (inclusive). The polygon must not be empty or null.
     * 
     * @param eT
     * 
     * 
     * @param initialValues
     *            these values are placed on the initial diagonal along the
     *            diagonal (x-y) which spans from x = 0 to y = 0, it is assumed
     *            the polygon is shaped appropriately, the values include those
     *            on the diagonal and the -1 diagonal
     * @param initialDiagonal
     *            the input diagonal, which must be 0 or greater
     * @param xIA
     *            sequence on x axis
     * @param yIA
     *            sequence on the y axis
     */
    public final void scanPolygon(final List lET, final List rET, final int width,
            final int height, final float[] initialValues, final int initialDiagonal,
            final Cell.CellCalculator calcL, final Cell.CellCalculator calcR,
            final int[] lLTA, final int[] rLTA) {
        this.calcL = calcL;
        this.calcR = calcR;
        this.width = width;
        this.height = height;
        final int lineLength = (width + 1) * this.stateNumber;
        Arrays.fill(this.line, 0, lineLength, Float.NEGATIVE_INFINITY);
        Arrays.fill(this.line, this.line.length / 2, this.line.length / 2
                + lineLength, Float.NEGATIVE_INFINITY);
        this.setInitialValues(initialValues, initialDiagonal);
        this.initialDiagonal = initialDiagonal;
        // lineFlip = (y = ((Integer) ((List) lET.get(0)).get(0))
        // .intValue()) == 0;
        this.lineFlip = (this.y = ((PolygonFiller.Node) lET.get(0)).y) == 0;
        if (Debug.DEBUGCODE && !this.lineFlip
                && (((PolygonFiller.Node) lET.get(0)).y != 1)) {
			// ((Integer) ((List) lET.get(0)).get(0)).intValue() != 1)
            throw new IllegalStateException(((PolygonFiller.Node) lET
                    .get(0)).y
                    + " ");
		}
        this.lLTA = lLTA;
        this.rLTA = rLTA;
        this.lLTAi = 0;
        this.lLT = lLTA[1];
        this.rLTAi = 0;
        this.rLT = rLTA[1];
        PolygonFiller.scanPolygon(lET.iterator(), rET.iterator(),
                this);
        if (Debug.DEBUGCODE && (this.y != height)) {
			throw new IllegalStateException(this.y + " " + height);
		}
    }
}