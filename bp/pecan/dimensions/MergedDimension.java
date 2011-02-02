/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import java.util.Iterator;

import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public class MergedDimension extends Dimension {
    final Dimension d, d2;

    final int size;

    /**
     * Takes two dimensions and concatenates them together to create one
     * seamless dimension. Throws a {@link IllegalArgumentException}if the two
     * dimensions do not have equal numbers of sub-dimensions.
     * 
     * @param d
     * @param d2
     */
    public MergedDimension(final Dimension d, final Dimension d2) {
        if (d.subDimensionsNumber() != d2.subDimensionsNumber()) {
			throw new IllegalArgumentException();
		}
        this.d = d;
        this.d2 = d2;
        this.size = d.size() + d2.size();
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
        return x < this.d.size() ? this.d.get(x, y) : this.d2.get(x - this.d.size(), y);
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
        return x < this.d.size() ? this.d.get(x, offset, iA) : this.d2.get(x - this.d.size(),
                offset, iA);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#getReversedSlice()
     */
    @Override
	public Dimension getReversedSlice() {
        return new MergedDimension(this.d2.getReversedSlice(), this.d.getReversedSlice());
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#getSlice(int, int)
     */
    @Override
	public Dimension getSlice(final int start, final int length) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkSliceBounds(start, length, this);
		}
        if (start >= this.d.size()) {
            return this.d2.getSlice(start - this.d.size(), length);
        }
        if (start + length <= this.d.size()) {
            return this.d.getSlice(start, length);
        }
        return new MergedDimension(this.d.getSlice(start, this.d.size() - start), this.d2
                .getSlice(0, length - (this.d.size() - start)));
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
        return new MergedDimension(this.d.getSubDimensionSlice(iA), this.d2
                .getSubDimensionSlice(iA));
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#iterator()
     */
    @Override
	public Iterator iterator() {
        return new DimensionIterator(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#size()
     */
    @Override
	public int size() {
        return this.size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#subDimensionsNumber()
     */
    @Override
	public int subDimensionsNumber() {
        return this.d.subDimensionsNumber();
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimension#getSubDimensionSlice(int[], int, int)
     */
    @Override
	protected Dimension getSubDimensionSlice(final int[] iA, final int start, final int length) {
        return this.getSlice(start, length).getSubDimensionSlice(iA);
    }

}