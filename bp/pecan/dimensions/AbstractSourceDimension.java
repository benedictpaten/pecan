/*
 * Created on Feb 11, 2005
 */
package bp.pecan.dimensions;

import java.util.Iterator;

import bp.common.io.Debug;


/**
 * @author benedictpaten
 */
public abstract class AbstractSourceDimension extends Dimension {
    

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#getReversedSlice()
     */
    @Override
	public Dimension getReversedSlice() {
        	return new ReversedDimension(this);
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#getSlice(int, int)
     */
    @Override
	public Dimension getSlice(final int start, final int length) {
        if(Debug.DEBUGCODE) {
			DimensionTools.checkSliceBounds(start, length, this);
		}
        return new SlicedDimension(this, start, length);
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
        return this.getSubDimensionSlice(iA, 0, this.size());
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#iterator()
     */
    @Override
	public Iterator iterator() {
        return new DimensionIterator(this);
    }
    
}
