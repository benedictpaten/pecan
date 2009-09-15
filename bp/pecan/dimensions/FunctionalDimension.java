/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import bp.common.fp.Function_Int_2Args;
import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public class FunctionalDimension extends AbstractSourceDimension {
    final Function_Int_2Args fn;
    final int size, subDimensions;
    
    /**
     *  
     */
    public FunctionalDimension(final Function_Int_2Args fn, final int size, final int subDimensions) {
        if((Debug.DEBUGCODE && (size < 0)) || (subDimensions < 1)) {
			throw new IllegalArgumentException();
		}
        this.fn = fn;
        this.size = size;
        this.subDimensions = subDimensions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#get(int, int)
     */
    @Override
	public int get(final int x, final int y) {
        if(Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, y, this);
		}
        return this.fn.fn(x, y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#get(int, int, int[])
     */
    @Override
	public int get(final int x, final int offset, final int[] iA) {
        if(Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, 0, this);
		}
        for(int i=0; i<this.subDimensionsNumber(); i++) {
			iA[offset+i] = this.fn.fn(x, i);
		}
        return offset+this.subDimensionsNumber();
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#size()
     */
    @Override
	public int size() {
        return this.size;
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.Dimension#subDimensionsNumber()
     */
    @Override
	public int subDimensionsNumber() {
        return this.subDimensions;
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimension#getSubDimensionSlice(int[], int, int)
     */
    @Override
	protected Dimension getSubDimensionSlice(final int[] iA, final int start, final int length) {
        if(Debug.DEBUGCODE && (iA.length != this.subDimensionsNumber())) {
			throw new IllegalStateException();
		}
        return this.getSlice(start, length);
    }
}