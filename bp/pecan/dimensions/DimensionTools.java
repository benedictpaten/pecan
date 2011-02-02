/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import java.util.Arrays;

import bp.common.fp.Function;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Predicates_2Args;

/**
 * @author benedictpaten
 */
public class DimensionTools {

    public static Predicate_2Args dimensionsEqual() {
        return Predicates_2Args.pipe(DimensionTools.viewsEqual(), new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                final Dimension d = (Dimension) o;
                final int[] iA = new int[d.subDimensionsNumber()];
                for (int i = 0; i < iA.length; i++) {
					iA[i] = i;
				}
                return new View(d, iA);
            }
        });
    }

    public static Predicate_2Args viewsEqual() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                final View v = (View) o, v2 = (View) o2;
                if ((v.size() != v2.size())
                        || (v.subDimensionsNumber() != v2.subDimensionsNumber())) {
					return false;
				}
                for (int i = 0; i < v.size(); i++) {
                    for (int j = 0; j < v.subDimensionsNumber(); j++) {
                        if (v.get(i, j) != v2.get(i, j)) {
							return false;
						}
                    }
                }
                return true;
            }
        };
    }

    /**
     * Creates a sequence object using the g
     * 
     * @param polygonClipper
     * @param length
     * @return
     */
    static Sequence getSequenceOfFunction(final Function fn, final int length) {
        final int[] iA = new int[length];
        for (int i = 0; i < iA.length; i++) {
            iA[i] = ((Number) fn.fn(new Double(i))).intValue();
        }
        return new Sequence(iA);
    }

    /**
     * Get dimension of length length with values starting from start.
     * 
     * @param start
     * @param length
     */
    public static Dimension getLinearSequence(final int start, final int length) {
        return new FunctionalDimension(new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int_2Args#fn(int, int)
             */
            public int fn(final int i, final int j) {
                return start + i;
            }
        }, length, 1);
    }

    /**
     * Function for checking if an index request to a dimension should raise an
     * {@link ArrayIndexOutOfBoundsException}.
     * 
     * @param start
     *            start point of slice op
     * @param length
     *            length of slice
     * @param d
     *            dimension from which to slice
     */
    static void checkSliceBounds(final int start, final int length, final Dimension d) {
        if ((start < 0) || (length < 0) || (start + length > d.size())) {
			throw new ArrayIndexOutOfBoundsException(start + " " + length + " "
                    + d.size());
		}
    }

    /**
     * Function for checking if an index request to a dimension should raise an
     * {@link ArrayIndexOutOfBoundsException}.
     * 
     * @param x
     *            column coordinate
     * @param y
     *            row coordinate
     * @param d
     *            dimension from which to check bounds
     */
    static void checkIndexBounds(final int x, final int y, final Dimension d) {
        if ((x < 0) || (y < 0) || (x >= d.size()) || (y >= d.subDimensionsNumber())) {
			throw new ArrayIndexOutOfBoundsException();
		}
    }

    /**
     * Function for checking if an array contains legal slices for a
     * subDimension slicing operation. Throws a
     * {@link ArrayIndexOutOfBoundsException}.
     * 
     * @param iA
     *            the array containing the subDimensions to slice
     * @param d
     *            the dimension to check against
     */
    static void checkSubDimensionSliceRequest(final int[] iA, final Dimension d) {
        if (iA.length == 0) {
			throw new IllegalArgumentException();
		}
        final int[] iA2 = iA.clone();
        Arrays.sort(iA2);
        for (int i = 0; i < iA.length; i++) {
            if ((iA[i] >= d.subDimensionsNumber()) || (iA[i] < 0)
                    || (iA[i] != iA2[i])) {
				throw new IllegalArgumentException();
			}
        }
        for (int i = 1; i < iA2.length; i++) {
            if (iA2[i] == iA2[i - 1]) {
				throw new IllegalArgumentException("Duplicated element");
			}
        }
    }

}