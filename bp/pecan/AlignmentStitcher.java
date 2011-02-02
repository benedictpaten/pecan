/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Mar 24, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import bp.common.fp.Generator;
import bp.common.fp.IterationTools;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_2Args;
import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public final class AlignmentStitcher implements Generator {

    Generator alignmentGenerator;

    Procedure rowProcedure, columnProcedure;

    final List row, column;

    final int[] rowGap, columnGap;

    /**
     * 
     */
    public AlignmentStitcher(final Generator alignmentGenerator,
            final int rowSubDimensionNumber, final int columnSubDimensionNumber) {
        super();
        this.alignmentGenerator = alignmentGenerator;
        this.row = new LinkedList();
        this.column = new LinkedList();
        this.rowGap = new int[rowSubDimensionNumber];
        Arrays.fill(this.rowGap, Integer.MAX_VALUE);
        this.columnGap = new int[columnSubDimensionNumber];
        Arrays.fill(this.columnGap, Integer.MAX_VALUE);
        this.rowProcedure = new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public final void pro(final Object o) {
                AlignmentStitcher.this.row.addAll((List) o);
            }
        };
        this.columnProcedure = new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public final void pro(final Object o) {
                AlignmentStitcher.this.column.addAll((List) o);
            }
        };
    }

    public Procedure getInputX() {
        return this.rowProcedure;
    }

    public Procedure getInputY() {
        return this.columnProcedure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.fp.Generator#gen()
     */
    public final Object gen() {
        final Generator gen = (Generator) this.alignmentGenerator.gen();
        if (gen != null) {
            final List<Object[]> l = new LinkedList<Object[]>();
            Float f;
            while ((f = (Float) gen.gen()) != null) {
                final float f2 = f.floatValue();
                if (f2 == Float.POSITIVE_INFINITY) {
                    l.add(new Object[] { this.row.remove(0), this.columnGap });
                } else {
                    if (f2 == Float.NEGATIVE_INFINITY) {
                        l.add(new Object[] { this.rowGap,
                                this.column.remove(0) });
                    } else {
                        l.add(new Object[] { this.row.remove(0),
                                this.column.remove(0) });
                    }
                }
            }
            return l;
        }
        return null;
    }

    public static final Procedure_2Args convertInput() {
        return new Procedure_2Args() {

            public final void pro(final Object o, final Object o2) {
                final int i = this.getCos(o, (int[]) o2, 0);
                if (Debug.DEBUGCODE && (i != ((int[]) o2).length)) {
					throw new IllegalArgumentException(i + " " + ((int[]) o2).length + "  ( " + IterationTools.join((int[]) o2, " ") + " ) ");
				}
            }

            final int getCos(final Object o, final int[] iA, int i) {
                if (o instanceof Object[]) {
                    final Object[] oA = (Object[]) o;
                    i = this.getCos(oA[0], iA, i);
                    return this.getCos(oA[1], iA, i);
                }
                if (o instanceof int[]) {
                    final int[] iA2 = (int[]) o;
                    System.arraycopy(iA2, 0, iA, i, iA2.length);
                    return i + iA2.length;
                }
                return i;
            }
        };
    }

}