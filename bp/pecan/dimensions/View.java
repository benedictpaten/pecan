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
import java.util.NoSuchElementException;

import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Iterable;
import bp.common.io.Debug;

/**
 * A simple class for viewing selected rows of a Dimension.
 * 
 * @author benedictpaten
 */
public class View implements Iterable {
    private final Dimension d;

    private final int[] iA;

    /**
     * Similar in concept to {@link Dimension#getSubDimensionSlice(int[])}but
     * different because it simply frames the view rather than extracting the
     * underlying sub-dimensions. The sub dimensions retreived with calls to get
     * will have the same order as given by the array iA.
     * 
     * @param d
     *            the dimension with which to view
     * @param iA
     *            array containing the numbers of the desired columns to view
     */
    public View(final Dimension d, final int[] iA) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkSubDimensionSliceRequest(iA, d);
		}
        this.d = d;
        this.iA = iA;
    }

    /**
     * Like {@link Dimension#get(int, int)}
     * 
     * @param x
     *            column to retrieve
     * @param y
     *            row to retrive
     * @return
     */
    public final int get(final int x, final int y) {
        return this.d.get(x, this.iA[y]);
    }
    
    /**
     * An adaptor for the {@link View#get(int, int)} function.
     * @return
     */
    public Function_Int_2Args get() {
        return new Function_Int_2Args() {
            /* (non-Javadoc)
             * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
             */
            public int fn(final int i, final int j) {
                return View.this.d.get(i, View.this.iA[j]);
            }
        };
    }

    /**
     * Like {@link Dimension#get(int, int, int[])}.
     * 
     * @param x
     *            column to retrieve
     * @param offset
     *            in array from which to place values
     * @param iA2
     *            array to put values in
     * @return offset of last value placed in iA2 + 1
     */
    public final int get(final int x, final int offset, final int[] iA2) {
        for (int i = 0; i < this.iA.length; i++) {
			iA2[offset + i] = this.d.get(x, this.iA[i]);
		}
        return offset + this.iA.length;
    }
    
    public final int subDimensionsNumber() {
        return this.iA.length;
    }

    /**
     * Like {@link Dimension#size()}
     * 
     * @return
     */
    public final int size() {
        return this.d.size();
    }

    /**
     * Returns an iterator for this view. The iterator returns the columns as
     * int[], with only those rows that are part of the view being included.
     */
    public Iterator iterator() {
        return new Iterator() {
            int x = 0;

            public boolean hasNext() {
                return this.x < View.this.d.size();
            }

            public Object next() {
                if (this.x >= View.this.d.size()) {
					throw new NoSuchElementException();
				}
                final int[] iA2 = new int[View.this.iA.length];
                View.this.get(this.x++, 0, iA2);
                return iA2;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }
}