/*
 * Created on Feb 15, 2005
 */
package bp.pecan.dimensions;

import bp.common.io.Debug;

/**
 * 
 * @author benedictpaten
 */
public class PairedDimension
                            extends AbstractSourceDimension {
    final Dimension d, d2;

    /**
     * Pairs two dimensions together without any gaps. The dimensions must
     * therefore be of equal length. The resulting alignment is indivisible and
     * will throw a {@link IllegalStateException}if a slice operation attempts
     * to slice them apart.
     * 
     * @param d
     * @param d2
     */
    PairedDimension(final Dimension d, final Dimension d2) {
        if (Debug.DEBUGCODE && (d.size() != d2.size())) {
			throw new IllegalArgumentException();
		}
        this.d = d;
        this.d2 = d2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#get(int, int)
     */
    @Override
	public int get(final int x, final int y) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, y, this);
		}
        return y < this.d.subDimensionsNumber() ? this.d.get(x, y) : this.d2.get(x, y
                - this.d.subDimensionsNumber());
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#get(int, int, int[])
     */
    @Override
	public int get(final int x, int offset, final int[] iA) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, 0, this);
		}
        offset = this.d.get(x, offset, iA);
        return this.d2.get(x, offset, iA);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.AbstractDimension#getSubDimensionSlice(int[],
     *      int, int)
     */
    @Override
	protected Dimension getSubDimensionSlice(final int[] iA, final int start,
            final int length) {
        if (Debug.DEBUGCODE && (iA.length != this.subDimensionsNumber())) {
			throw new IllegalStateException();
		}
        return this.getSlice(start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#size()
     */
    @Override
	public int size() {
        return this.d.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#subDimensionsNumber()
     */
    @Override
	public int subDimensionsNumber() {
        return this.d.subDimensionsNumber() + this.d2.subDimensionsNumber();
    }

}