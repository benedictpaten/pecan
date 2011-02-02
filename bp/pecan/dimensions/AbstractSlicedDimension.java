/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 11, 2005
 */
package bp.pecan.dimensions;

import java.util.Iterator;

import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public abstract class AbstractSlicedDimension extends Dimension {
    protected final Dimension d;
    
    protected AbstractSlicedDimension(final Dimension d) {
        this.d = d;
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#get(int, int)
     */
    @Override
	public final int get(final int x, final int y) {
        if(Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, y, this);
		}
        return this.d.get(this.newCoordinate(x), y);
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#get(int, int, int[])
     */
    @Override
	public final int get(final int x, final int offset, final int[] iA) {
        if(Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, 0, this);
		}
        return this.d.get(this.newCoordinate(x), offset, iA);
    }
    
    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimension#getSubDimensionSlice(int[], int, int)
     */
    @Override
	protected Dimension getSubDimensionSlice(final int[] iA, final int start, final int length) {
        return this.d.getSubDimensionSlice(iA, start, length);
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#iterator()
     */
    @Override
	public final Iterator iterator() {
        return new DimensionIterator(this);
    }
    
    abstract protected int newCoordinate(int x); 

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#subDimensionsNumber()
     */
    @Override
	public final int subDimensionsNumber() {
        return this.d.subDimensionsNumber();
    }

}
