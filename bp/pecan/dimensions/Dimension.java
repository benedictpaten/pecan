/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 10, 2005
 */
package bp.pecan.dimensions;

import java.util.Iterator;

import bp.common.fp.Iterable;

/**
 * This base class is used to represent sequences and alignments. No conventions
 * exist other than gaps are represented as the value {@link Integer#MAX_VALUE}.
 * 
 * @author benedictpaten
 */
public abstract class Dimension implements Iterable {

    /**
     * Positions start from 0.
     * 
     * @param x 
     *            coordinate
     * @param y
     *            coordinate
     * @return the given position in the Dimension
     */
    public abstract int get(int x, int y);

    /**
     * Fills elements of the given array with a Dimension column.
     * 
     * @param x
     *            coordinate from which to start
     * @param offset
     *            start point from which to fill (inclusive)
     * @param iA
     *            array to fill
     * @return offset into the given array of the final coordinate + 1
     */
    public abstract int get(int x, int offset, int[] iA);

    /**
     * 
     * @return a Dimension whose x axis is reversed
     */
    public abstract Dimension getReversedSlice();

    /**
     * Returns an slice of the x axis. Positions start from 0. Zero length
     * slices beginning at the end of the dimension are legal.
     * 
     * @param start
     *            the x coordinate from which to start the slice (inclusive)
     * @param length
     *            the total length of the slice (end coordinate is x + length,
     *            exclusive)
     * @return sliced Dimension
     *  
     */
    public abstract Dimension getSlice(int start, int length);

    /**
     * Slices the dimension's y axis, selecting the given sub-dimensions
     * numbered in the (pre-sorted) int array. As the array is not guaranteed to
     * be a set any repeated numbers will throw an
     * {@link IllegalArgumentException}as this is undesirable. If iA is of
     * length zero an {@link IllegalArgumentException}will be thrown. If the
     * array is not sorted an {@link IllegalArgumentException}will be thrown.
     * The returned Dimension will have its row ordered according to the order
     * in the original dimension. Not all sub dimensions may be divisible.
     * Consequently an {@link IllegalStateException}will be thrown if a request
     * to slice two or more indivisible dimensions is made.
     * 
     * 
     * @param iA
     *            array containing the numbers of the sub-dimensions to be
     *            selected
     * @return a Dimension containing only the given sub-dimensions
     */
    public abstract Dimension getSubDimensionSlice(int[] iA);

    /**
     * Columns of the Dimension are returned in int[] arrays.
     * 
     * @return an iterator starting from the first x coordinate that returns
     *         columns of the dimension
     */
    public abstract Iterator iterator();

    /**
     * 
     * @return length of the x axis
     */
    public abstract int size();

    /**
     * Number of sub-dimensions in the given dimension.
     * 
     * @return
     */
    public abstract int subDimensionsNumber();

    /**
     * Internal method used during {@link Dimension#getSubDimensionSlice(int[])}
     * function.
     * 
     * @param iA
     * @param start
     * @param length
     * @return
     */
    protected abstract Dimension getSubDimensionSlice(int[] iA,
            int start, int length);

    @Override
	public String toString() {
        final StringBuffer sB = new StringBuffer();
        final int[] iA = new int[this.subDimensionsNumber()];
        for (int i = 0; i < this.size(); i++) {
            this.get(i, 0, iA);
            for (final int element : iA) {
                sB.append(element);
                sB.append("\t");
            }
            sB.append("\n");
        }
        return sB.toString();
    }
}