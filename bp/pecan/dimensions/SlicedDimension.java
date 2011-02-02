/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 11, 2005
 */
package bp.pecan.dimensions;

import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public class SlicedDimension
                            extends AbstractSlicedDimension {
    final int start, length;

    /**
     * Takes a dimension and two coordinates and produces a slice like
     * {@link Dimension#getSlice(int, int)}.
     * 
     * @param d
     * @param start start position within d
     * @param length total length of the slice
     */
    SlicedDimension(final Dimension d, final int start, final int length) {
        super(d);
        this.start = start;
        this.length = length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.AbstractSlicedDimension#newCoordinate(int)
     */
    @Override
	protected final int newCoordinate(final int x) {
        return this.start + x;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#getReversedSlice()
     */
    @Override
	public final Dimension getReversedSlice() {
        return new SlicedDimension(this.d.getReversedSlice(), this.d.size() - this.start
                - this.length, this.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#getSlice(int, int)
     */
    @Override
	public final Dimension getSlice(final int start, final int length) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkSliceBounds(start, length, this);
		}
        return new SlicedDimension(this.d, this.start + start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#size()
     */
    @Override
	public final int size() {
        return this.length;
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#getSubDimensionSlice(int[])
     */
    @Override
	public Dimension getSubDimensionSlice(final int[] iA) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkSubDimensionSliceRequest(iA, this);
		}
        	return this.getSubDimensionSlice(iA, this.start, this.length);
    }

}