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
public class ReversedDimension
                              extends AbstractSlicedDimension {

    /**
     * Takes the given dimension and creates a Dimension whose column order is
     * reversed.
     * 
     * @param d
     */
    ReversedDimension(final Dimension d) {
        super(d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#getReversedSlice()
     */
    @Override
	public final Dimension getReversedSlice() {
        return this.d;
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
        return new ReversedDimension(this.d.getSlice(
                this.newCoordinate(start) - (length - 1), length));
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.AbstractSlicedDimension#newCoordinate(int)
     */
    @Override
	protected final int newCoordinate(final int x) {
        return (this.d.size() - 1) - x;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#size()
     */
    @Override
	public final int size() {
        return this.d.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#getSubDimensionSlice(int[])
     */
    @Override
	public Dimension getSubDimensionSlice(final int[] iA) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkSubDimensionSliceRequest(iA, this);
		}
        return new ReversedDimension(this.getSubDimensionSlice(iA, 0, this.d.size()));
    }
}