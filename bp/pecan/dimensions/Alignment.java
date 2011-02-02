/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import bp.common.fp.Functions;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate;
import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public class Alignment
                      extends AbstractSourceDimension {

    final Dimension d, d2;

    final int[] alignment;

    public Alignment(final Iterator it, final Dimension d, final Dimension d2) {
        this.d = d;
        this.d2 = d2;
        final List l = (List) IterationTools.append(it, new ArrayList());
        this.alignment = new int[l.size() * 2 + 2];
        int j = 0, k = 0;
        for (int i = 0; i < l.size(); i++) {
            final boolean[] bA = (boolean[]) l.get(i);
            if (bA[0] == true) {
				this.alignment[i * 2] = (j++ << 1);
			} else {
				this.alignment[i * 2] = (j << 1) + 1;
			}
            if (bA[1] == true) {
				this.alignment[i * 2 + 1] = (k++ << 1);
			} else {
				this.alignment[i * 2 + 1] = (k << 1) + 1;
			}
        }
        this.alignment[this.alignment.length - 2] = (j << 1) + 1;
        this.alignment[this.alignment.length - 1] = (k << 1) + 1;
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
        return y < this.d.subDimensionsNumber() ? Alignment.isGap(this.alignment[x * 2]) ? Integer.MAX_VALUE
                : this.d.get(Alignment.coordinate(this.alignment[x * 2]), y)
                : Alignment.isGap(this.alignment[x * 2 + 1]) ? Integer.MAX_VALUE : this.d2.get(
                        Alignment.coordinate(this.alignment[x * 2 + 1]), y
                                - this.d.subDimensionsNumber());
    }

    final static int fill(final int[] iA, final int offset, final int noOfPositions) {
        for (int i = 0; i < noOfPositions; i++) {
			iA[offset + i] = Integer.MAX_VALUE;
		}
        return offset + noOfPositions;
    }

    final static boolean isGap(final int i) {
        return (i & 1) != 0;
    }

    final static int coordinate(final int i) {
        return i >>> 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#get(int, int, int[])
     */
    @Override
	public int get(final int x, int offset, final int[] iA) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkIndexBounds(x, 0, this);
		}
        if (!Alignment.isGap(this.alignment[x * 2])) {
			offset = this.d.get(Alignment.coordinate(this.alignment[x * 2]), offset, iA);
		} else {
			offset = Alignment.fill(iA, offset, this.d.subDimensionsNumber());
		}
        if (!Alignment.isGap(this.alignment[x * 2 + 1])) {
			return this.d2.get(Alignment.coordinate(this.alignment[x * 2 + 1]), offset, iA);
		} else {
			return Alignment.fill(iA, offset, this.d2.subDimensionsNumber());
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#getSubDimensionSlice(int[])
     */
    @Override
	public bp.pecan.dimensions.Dimension getSubDimensionSlice(final int[] iA) {
        if (Debug.DEBUGCODE) {
			DimensionTools.checkSubDimensionSliceRequest(iA, this);
		}
        return this.getSubDimensionSlice(iA, 0, this.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#size()
     */
    @Override
	public int size() {
        return (this.alignment.length - 1) / 2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.Dimension#subDimensionsNumber()
     */
    @Override
	public int subDimensionsNumber() {
        return this.d.subDimensionsNumber() + this.d2.subDimensionsNumber();
    }

    Dimension getSubDimensionSlice1(final int[] iA, final int start, final int length) {
        final int j = Alignment.coordinate(this.alignment[start * 2]);
        return this.d.getSubDimensionSlice(iA, j,
                Alignment.coordinate(this.alignment[(start + length) * 2]) - j);
    }

    Dimension getSubDimensionSlice2(final int[] iA, final int start, final int length) {
        final int[] iA2 = new int[iA.length];
        for (int i = 0; i < iA2.length; i++) {
            iA2[i] = iA[i] - this.d.subDimensionsNumber();
        }
        final int j = Alignment.coordinate(this.alignment[start * 2 + 1]);
        return this.d2.getSubDimensionSlice(iA2, j,
                Alignment.coordinate(this.alignment[(start + length) * 2 + 1]) - j);
    }

    static int getIndexOfFirstPosition(int i, final int[] iA) {
        i = Arrays.binarySearch(iA, i);
        if (i < 0) {
			i = Math.abs(i + 1);
		}
        return i;
    }

    static int[][] splitArray(final int[] iA, final int i) {
        final int[] iA2 = new int[i], iA3 = new int[iA.length - i];
        for (int j = 0; j < iA2.length; j++) {
			iA2[j] = iA[j];
		}
        for (int j = 0; j < iA3.length; j++) {
			iA3[j] = iA[i + j];
		}
        return new int[][] { iA2, iA3 };
    }

    static Predicate isNotAllGaps() {
        return new Predicate() {
            public boolean test(final Object o) {
                final int[] iA = (int[]) o;
                if (Debug.DEBUGCODE && (iA.length == 0)) {
					throw new IllegalStateException();
				}
                for (final int element : iA) {
					if (element != Integer.MAX_VALUE) {
						return true;
					}
				}
                return false;
            }
        };
    }

    static Generator arrayMaker(final Iterator it, final Iterator it2) {
        return new Generator() {
            public Object gen() {
                if (it.hasNext()) {
                    final Object o2 = it.next(), o3 = it2.next();
                    return o2 == Boolean.FALSE ? (o3 == Boolean.FALSE ? this.gen()
                            : new boolean[] { false, true }) : new boolean[] {
                            true, o3 == Boolean.FALSE ? false : true };
                }
                return null;
            }
        };
    }

    Generator dimensionGaps(final int start, final int length,
            final int dimensionNo) {
        return new Generator() {
            int i = start;

            public Object gen() {
                return this.i < (start + length) ? Alignment.isGap(Alignment.this.alignment[this.i++ * 2
                        + dimensionNo]) ? Boolean.FALSE : Boolean.TRUE : null;
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.AbstractDimension#getSubDimensionSlice(int[],
     *      int, int)
     */
    @Override
	protected Dimension getSubDimensionSlice(final int[] iA, final int start,
            final int length) {
        if (iA.length == this.subDimensionsNumber()) {
			return this.getSlice(start, length);
		}
        final int i = Alignment.getIndexOfFirstPosition(this.d.subDimensionsNumber(), iA);
        if (i == iA.length) {
            return this.getSubDimensionSlice1(iA, start, length);
        }
        if (i == 0) {
            return this.getSubDimensionSlice2(iA, start, length);
        }
        final int[][] iAA = Alignment.splitArray(iA, i);
        final Iterator it = iAA[0].length == this.d.subDimensionsNumber() ? new GeneratorIterator(
                this.dimensionGaps(start, length, 0))
                : Iterators.map(new View(this.getSlice(start, length), iAA[0])
                        .iterator(), Functions.rCurry(Functions_2Args
                        .predicateWrapper(), Alignment.isNotAllGaps())), it2 = iAA[1].length == this.d2
                .subDimensionsNumber() ? new GeneratorIterator(this.dimensionGaps(
                start, length, 1)) : Iterators.map(new View(this.getSlice(
                start, length), iAA[1]).iterator(), Functions.rCurry(
                Functions_2Args.predicateWrapper(), Alignment.isNotAllGaps()));
        return new Alignment(new GeneratorIterator(Alignment.arrayMaker(it, it2)),
                this.getSubDimensionSlice1(iAA[0], start, length),
                this.getSubDimensionSlice2(iAA[1], start, length));
    }

}