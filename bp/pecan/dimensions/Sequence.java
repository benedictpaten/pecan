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
public class Sequence
                     extends AbstractSourceDimension {
    int[] sequence;

    public Sequence(final int[] sequence) {
        this.sequence = sequence;
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
        return this.sequence[x];
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#get(int, int, int[])
     */
    @Override
	public int get(final int x, final int offset, final int[] iA) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, 0, this);
		}
        iA[offset] = this.sequence[x];
        return offset + 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#size()
     */
    @Override
	public int size() {
        return this.sequence.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#subDimensionsNumber()
     */
    @Override
	public int subDimensionsNumber() {
        return 1;
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimension#getSubDimensionSlice(int[], int, int)
     */
    @Override
	protected Dimension getSubDimensionSlice(final int[] iA, final int start, final int length) {
        return this.getSlice(start, length);
    }

}