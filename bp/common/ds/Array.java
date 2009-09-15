/*
 * Created on May 6, 2005
 */
package bp.common.ds;

import java.util.Arrays;
import java.util.regex.Pattern;

import bp.common.fp.Function;
import bp.common.fp.Function_2Args;
import bp.common.fp.Function_Int;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Predicate_3Args;
import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public final class Array {

    /**
     * Transpose 2d matrix
     * 
     * @return
     */
    public static Function transpose2DMatrix() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                final int[][] iA = (int[][]) o;
                final int[][] iA2 = new int[iA.length != 0 ? iA[0].length
                        : 0][iA.length];
                for (int i = 0; i < iA2.length; i++) {
                    for (int j = 0; j < iA2[i].length; j++) {
                        iA2[i][j] = iA[j][i];
                    }
                }
                return iA2;
            }
        };
    }

    /**
     * Inverts the x axis of a matrix so point iA[iA.length -1 + n][N] -->
     * iA[n][N] and vice versa etc..
     * 
     * @return
     */
    public static Function reverseXAxisOf2DMatrix() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                final int[][] iA = (int[][]) o;
                final int[][] iA2 = new int[iA.length][iA.length != 0 ? iA[0].length
                        : 0];
                for (int i = 0; i < iA2.length; i++) {
                    for (int j = 0; j < iA2[i].length; j++) {
                        iA2[i][j] = iA[iA.length - i - 1][j];
                    }
                }
                return iA2;
            }
        };
    }

    /**
     * Inverts the y axis of a matrix so point iA[N][iA.length -1 + n] -->
     * iA[N][0] and vice versa etc..
     * 
     * @return
     */
    public static Function reverseYAxisOf2DMatrix() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                final int[][] iA = (int[][]) o;
                final int[][] iA2 = new int[iA.length][iA.length != 0 ? iA[0].length
                        : 0];
                for (int i = 0; i < iA2.length; i++) {
                    for (int j = 0; j < iA2[i].length; j++) {
                        iA2[i][j] = iA[i][iA[i].length - 1 - j];
                    }
                }
                return iA2;
            }
        };
    }

    /**
     * Reverse the values in an array between 0 (inclusive) and size
     * (exlcusive).
     * 
     * @param iA
     * @param size
     */
    public static void reverseArray(final int[] iA, final int size) {
        int k = size - 1;
        for (int i = 0; i < size / 2;) {
            final int j = iA[i];
            iA[i] = iA[k];
            iA[k] = j;
            i++;
            k--;
        }
    }

    /**
     * Reverse the values in an array between 0 (inclusive) and size
     * (exlcusive).
     * 
     * @param iA
     * @param start
     *            TODO
     * @param end
     */
    public static void reverseArray(final float[] iA, final int start, final int end) {
        int k = end - 1;
        final int l = (end - start) / 2 + start;
        for (int i = start; i < l;) {
            final float j = iA[i];
            iA[i] = iA[k];
            iA[k] = j;
            i++;
            k--;
        }
    }

    /**
     * Reverse the values in an array between 0 (inclusive) and size
     * (exlcusive).
     * 
     * @param iA
     * @param size
     */
    public static void reverseArray(final double[] iA, final int size) {
        int k = size - 1;
        for (int i = 0; i < size / 2;) {
            final double j = iA[i];
            iA[i] = iA[k];
            iA[k] = j;
            i++;
            k--;
        }
    }

    /**
     * Flips the position of every other word and its partner. Example { 1, 2,
     * 3, 4 } --> { 2, 1, 4, 3 }. If the array is not of even length then an
     * {@link IllegalArgumentException}will be thrown.
     * 
     * @param iA
     */
    public static void mingle(final int[] iA, final int length) {
        if (Debug.DEBUGCODE && (length % 2 != 0)) {
			throw new IllegalArgumentException();
		}
        for (int i = 0; i < length; i += 2) {
            final int j = iA[i];
            iA[i] = iA[i + 1];
            iA[i + 1] = j;
        }
    }

    /**
     * Binary search, returns index which is equal or greater
     */
    public static final int binarySearchIndex_TwoStep(final int[] iA, final int i) {
        for (int j = 0; j < iA.length; j += 2) {
            if (iA[j] >= i) {
				return j;
			}
        }
        return iA.length;
        /*
         * if (iA.length == 0 || i > iA[iA.length - 2]) return iA.length; int j =
         * iA.length / 4, k = j, m = iA[k << 1]; while (j != 0) { if (m < i) {
         * k += j & 1; j /= 2; k += j; m = iA[k << 1]; } else if (m > i) { k -=
         * j & 1; j /= 2; k -= j; m = iA[k << 1]; } else return k << 1; }
         * return (m >= i ? k : k + 1) << 1;
         */
    }

    /**
     * Binary search, returns index which is equal or greater
     */
    public static final int binarySearchIndex_TwoStep(final int[] iA,
            final int i, final int s, final int l) {
        if (i > iA[s + l - 2]) {
			return s + l;
		}
        int j = l / 4, k = j, m = iA[(k << 1) + s];
        while (j != 0) {
            if (m < i) {
                k += j & 1;
                j /= 2;
                k += j;
                m = iA[(k << 1) + s];
            } else if (m > i) {
                k -= j & 1;
                j /= 2;
                k -= j;
                m = iA[(k << 1) + s];
            } else {
				return (k << 1) + s;
			}
        }
        return ((m >= i ? k : k + 1) << 1) + s;
    }

    public static Function_Int_2Args matrix2DLookup(
            final int[] matrix, final int rowLength) {
        return new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
             */
            public int fn(final int x, final int y) {
                return matrix[y * rowLength + x];
            }
        };
    }

    public static Function_Int matrix1DLookUp(final int[] matrix) {
        return new Function_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int#polygonClipper(int)
             */
            public int fn(final int x) {
                return matrix[x];
            }
        };
    }

    /**
     * Look up index of value in matrix greater than or equal to input by
     * {@link Arrays#binarySearch(byte[], byte)}. IF multiple same values then
     * no guarantee which index will be reported.
     * 
     * @param matrix
     * @return
     */
    public static Function_Int binarySearch(final int[] matrix) {
        return new Function_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int#fn(int)
             */
            public int fn(final int i) {
                return Array.binarySearch(i, matrix);
            }
        };
    }

    /**
     * Look up index of value in matrix greater than or equal to input by
     * {@link Arrays#binarySearch(byte[], byte)}. IF multiple same values then
     * no guarantee which index will be reported.
     * 
     * @param i
     *            value
     * @param matrix
     *            matrix to search in
     * @return
     */
    public static final int binarySearch(final int i, final int[] matrix) {
        final int j = Arrays.binarySearch(matrix, i);
        return j < 0 ? -(j + 1) : j;
    }

    /**
     * Checks if two (multi-dimensional) array objects are equivalent by
     * recursively analysing there contents and checking if the atomic parts are
     * equal according to the third supplied argument which must be an instance
     * of {@link Predicate_2Args}. Object o is the first multi-dimension array,
     * o2 the second and o3 an instance of {@link Predicate_2Args}.
     * 
     * @return
     */
    public static Predicate_3Args arraysEqual() {
        return new Predicate_3Args() {
            public boolean test(final Object o, final Object o2, final Object o3) {
                if (o instanceof Object[]) {
                    if (o2 instanceof Object[]) {
                        if (((Object[]) o).length != ((Object[]) o2).length) {
							return false;
						}
                        for (int i = 0; i < ((Object[]) o).length; i++) {
                            if (!this.test(((Object[]) o)[i],
                                    ((Object[]) o2)[i], o3)) {
								return false;
							}
                        }
                        return true;
                    } else {
						return false;
					}
                }
                return ((Predicate_2Args) o3).test(o, o2);
            }
        };
    }

    /**
     * Sums the each pair of elements of the two given lists and places them in
     * the first.
     * 
     * @param ds
     * @param ds2
     * @return
     */
    public static double[] sum(final double[] ds, final double[] ds2,
            final Function_2Args add) {
        final double[] dA = ds.clone();
        if (Debug.DEBUGCODE && (ds.length != ds2.length)) {
			throw new ArrayIndexOutOfBoundsException();
		}
        for (int i = 0; i < dA.length; i++) {
			dA[i] = ((Number) add.fn(new Double(dA[i]), new Double(
                    ds2[i]))).doubleValue();
		}
        return dA;
    }

    /**
     * Creates the intersection of two sorted sets. Assumes the lists are
     * sufficiently large. Duplicates in the lists cause the behaviour to be
     * undefined.
     * 
     * @param iA
     *            sorted list
     * @param i
     *            max index of first list (exclusive)
     * @param iA2
     *            sorted list
     * @param j
     *            max index of second list
     * @param iA3
     *            list in which to place result
     * @return size of eventual sorted list
     */
    public static int intersection(final int[] iA, final int i, final int[] iA2, final int j,
            final int[] iA3) {
        int k = 0, l = 0, m = 0;
        if ((i == 0) || (j == 0)) {
			return 0;
		}
        while (true) {
            if (iA[k] < iA2[l]) {
                if (++k >= i) {
					break;
				}
            } else if (iA[k] > iA2[l]) {
                if (++l >= j) {
					break;
				}
            } else {
                iA3[m++] = iA[k];
                if ((++k >= i) || (++l >= j)) {
					break;
				}
            }
        }
        return m;
    }

    /**
     * Merges together the contents of two weight lists.
     * 
     * @param iA
     *            first list of format { index, weight }xN
     * @param iAL
     *            max index of first list (exclusive)
     * @param iA2
     * @param iA2L
     * @param add
     * @param iAScratch
     *            scratch index
     * @return
     */
    public static int merge_TwoStep(final int[] iA, final int iAL, final int[] iA2,
            final int iA2L, final Function_Int_2Args add, final int[] iAScratch) {
        int i = 0, j = 0, k = 0;
        if ((i < iAL) && (j < iA2L)) {
			while (true) {
                if (iA[i] < iA2[j]) {
                    iAScratch[k] = iA[i];
                    iAScratch[k + 1] = iA[i + 1];
                    i += 2;
                    k += 2;
                    if (i >= iAL) {
						break;
					}
                } else if (iA[i] > iA2[j]) {
                    iAScratch[k] = iA2[j];
                    iAScratch[k + 1] = iA2[j + 1];
                    j += 2;
                    k += 2;
                    if (j >= iA2L) {
						break;
					}
                } else {
                    iAScratch[k] = iA[i];
                    iAScratch[k + 1] = add.fn(iA[i + 1], iA2[j + 1]);
                    i += 2;
                    j += 2;
                    k += 2;
                    if ((i >= iAL) || (j >= iA2L)) {
						break;
					}
                }
            }
		}
        System.arraycopy(iA, i, iAScratch, k, iAL - i);
        k += iAL - i;
        System.arraycopy(iA2, j, iAScratch, k, iA2L - j);
        k += iA2L - j;
        return k;
    }

    /**
     * 
     * @param iA
     * @return the minimum value in the array or returns
     *         {@link Integer#MAX_VALUE}if array is of length 0.
     */
    public static final int getMin(final int[] iA) {
        if (iA.length > 0) {
            int i = iA[0];
            for (int j = 1; j < iA.length; j++) {
				if (iA[j] < i) {
					i = iA[j];
				}
			}
            return i;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 
     * @param iA
     * @return the maxiumum value in the array or returns
     *         {@link Integer#MIN_VALUE}if array is of length 0.
     */
    public static int getMax(final int[] iA) {
        if (iA.length > 0) {
            int i = iA[0];
            for (int j = 1; j < iA.length; j++) {
				if (iA[j] > i) {
					i = iA[j];
				}
			}
            return i;
        }
        return Integer.MIN_VALUE;
    }

    public static final void fill(final float[] from, final float[] to, int times) {
        int i = 0;
        while (times-- > 0) {
            System.arraycopy(from, 0, to, i, from.length);
            i += from.length;
        }
    }

    public static final int uniq(final int[] iA, final int[] iA2) {
        if (iA.length > 0) {
            int i = 1;
            int j = iA[0];
            iA2[0] = j;
            for (int k = 1; k < iA.length; k++) {
                final int l = iA[k];
                if (l != j) {
                    j = l;
                    iA2[i++] = l;
                }
            }
            return i;
        }
        return 0;
    }

    /**
     * 
     * @param oA
     * @param o
     * @return index of object {@link Object#equals(java.lang.Object)} in
     *         unsorted array or {@link Integer#MAX_VALUE} if not found.
     */
    public static final int indexOf(final Object[] oA, final Object o) {
        for (int i = 0; i < oA.length; i++) {
            if (oA[i].equals(o)) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static final int[] convertToInts(final String[] sA) {
        final int[] iA = new int[sA.length];
        for (int i = 0; i < sA.length; i++) {
            iA[i] = Integer.parseInt(sA[i]);
        }
        return iA;
    }

    public static final boolean[] convertToBooleans(final String[] sA) {
        final boolean[] iA = new boolean[sA.length];
        final Pattern p = Pattern.compile("[tT][rR][uU][eE]");
        for (int i = 0; i < sA.length; i++) {
            iA[i] = p.matcher(sA[i]).matches();
        }
        return iA;
    }

    public static int[] concatenate(final int[] iA, final int[] iA2) {
        final int[] iA3 = new int[iA.length + iA2.length];
        System.arraycopy(iA, 0, iA3, 0, iA.length);
        System.arraycopy(iA2, 0, iA3, iA.length, iA2.length);
        return iA3;
    }

    public static boolean[] concatenate(final boolean[] iA, final boolean[] iA2) {
        final boolean[] iA3 = new boolean[iA.length + iA2.length];
        System.arraycopy(iA, 0, iA3, 0, iA.length);
        System.arraycopy(iA2, 0, iA3, iA.length, iA2.length);
        return iA3;
    }

    public static int[] indicesOf(final int[] iA, final int length, final int i) {
        int j = 0;
        for (int k = 0; k < length; k++) {
            if (iA[k] == i) {
				j++;
			}
        }
        final int[] iA2 = new int[j];
        j = 0;
        for (int k = 0; k < length; k++) {
            if (iA[k] == i) {
				iA2[j++] = k;
			}
        }
        return iA2;
    }

}