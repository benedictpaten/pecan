/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on 12-May-2006
 */
package bp.pecan;

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import bp.common.ds.Array;
import bp.common.fp.Function_Int;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Generator_Int;
import bp.common.fp.Generators_Int;
import bp.common.io.Debug;
import bp.common.maths.Maths;

public final class SparseAlign {

    /* transmissions */

    // single affine
    
      public static final float MATCH = 0.9703833696510062f;
      
      static final float GAP_OPEN = (1.0f - SparseAlign.MATCH) / 2;
      
      static final float GAP_EXTEND = 0.974445284091146f;
      //-0.02588690928541599
      
      static final float GAP_SWITCH = 0.0007315179552849f;
      //-7.2203887919613665
     
    // end single affine
    // double affine
    /*static final float MATCH = 0.9703833696510062f; // -0.030064059121770816

    static final float GAP_OPEN = 0.0129868352330243f; // -4.34381910900448

    static final float GAP_EXTEND = 0.7126062401851738f; // -0.3388262689231553

    static final float GAP_SWITCH = 0.0073673675173412815f; // -4.910694825551255

    static final float JUNK_EXTEND = 0.99656342579062f; // -0.003442492794189331

    static final float JUNK_EXTENDL = Maths.log(JUNK_EXTEND);

    static final float JUNK_CLOSEL = Maths.log(1.0f - JUNK_EXTEND);

    static final float JUNK_OPENL = Maths
            .log((1.0f - (MATCH + 2 * GAP_OPEN)) / 2);*/

    // end double affine

    public static final float MATCHL = Maths.log(SparseAlign.MATCH);

    static final float GAP_SWITCHL = Maths.log(SparseAlign.GAP_SWITCH);

    static final float GAP_OPENL = Maths.log(SparseAlign.GAP_OPEN);

    static final float GAP_EXTENDL = Maths.log(SparseAlign.GAP_EXTEND);

    static final float GAP_CLOSEL = Maths.log((1.0f - SparseAlign.GAP_EXTEND)
            - SparseAlign.GAP_SWITCH);

    /* emissions */

    static final float MATCH_EMISSION_N_L = Maths.log(0.04f);

    static final float MATCH_EMISSIONL = Maths
            .log(0.12064298095701059f);

    static final float TRANSVERSION_EMISSIONL = Maths
            .log(0.010367271172731285f);

    static final float TRANSITION_EMISSIONL = Maths
            .log(0.01862247669752685f);

    static final float GAP_EMISSIONL = Maths.log(0.2f);

    static final float[] emit = new float[] { SparseAlign.MATCH_EMISSIONL,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.TRANSITION_EMISSIONL,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.MATCH_EMISSION_N_L,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.MATCH_EMISSIONL,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.TRANSITION_EMISSIONL,
            SparseAlign.MATCH_EMISSION_N_L, SparseAlign.TRANSITION_EMISSIONL,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.MATCH_EMISSIONL,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.MATCH_EMISSION_N_L,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.TRANSITION_EMISSIONL,
            SparseAlign.TRANSVERSION_EMISSIONL, SparseAlign.MATCH_EMISSIONL,
            SparseAlign.MATCH_EMISSION_N_L, SparseAlign.MATCH_EMISSION_N_L,
            SparseAlign.MATCH_EMISSION_N_L, SparseAlign.MATCH_EMISSION_N_L,
            SparseAlign.MATCH_EMISSION_N_L, SparseAlign.MATCH_EMISSION_N_L };

    public static Function_Int_2Args affineGapFn() {
        return new Function_Int_2Args() {
            public int fn(final int i, final int j) {
                if ((i < 0) || (j < 0) || ((i == 0) && (j == 0))) {
					throw new IllegalStateException(i + " " + j);
				}
                if ((i != 0) && (j != 0)) {
                    return Float.floatToRawIntBits(SparseAlign.GAP_EMISSIONL
                            * (i + j) + SparseAlign.GAP_OPENL + SparseAlign.GAP_SWITCHL
                            + SparseAlign.GAP_EXTENDL * (i + j - 2) + SparseAlign.GAP_CLOSEL);
                }
                return Float.floatToRawIntBits(SparseAlign.GAP_EMISSIONL
                        * (i + j) + SparseAlign.GAP_OPENL + SparseAlign.GAP_EXTENDL
                        * (i + j - 1) + SparseAlign.GAP_CLOSEL);
            }
        };
    }

    /*public static Function_Int_2Args doubleAffineGapFn() {
        return new Function_Int_2Args() {
            public int fn(int i, int j) {
                if (i < 0 || j < 0 || (i == 0 && j == 0))
                    throw new IllegalStateException(i + " " + j);
                if (i != 0 && j != 0) {
                    //Debug.pl(" hello ");
                    return Float.floatToRawIntBits(GAP_EMISSIONL
                            * (i + j) + GAP_OPENL + GAP_SWITCHL
                            + GAP_EXTENDL * (i + j - 2) + GAP_CLOSEL);
                }
                return Float.floatToRawIntBits(Maths.logAdd(
                        GAP_EMISSIONL * (i + j) + JUNK_OPENL
                                + JUNK_EXTENDL * (i + j - 1)
                                + JUNK_CLOSEL, GAP_EMISSIONL
                                * (i + j) + GAP_OPENL + GAP_EXTENDL
                                * (i + j - 1) + GAP_CLOSEL));
            }
        };
    }*/

    public static Function_Int_2Args logGapFn() {
        return new Function_Int_2Args() {
            public int fn(final int i, final int j) {
                if ((i < 0) || (j < 0) || ((i == 0) && (j == 0))) {
					throw new IllegalStateException(i + " " + j);
				}
                if ((i != 0) && (j != 0)) {
					return Float.floatToRawIntBits(SparseAlign.GAP_EMISSIONL
                            * (i + j) + SparseAlign.GAP_OPENL + SparseAlign.GAP_SWITCHL
                            + SparseAlign.GAP_EXTENDL * (i + j - 2) + SparseAlign.GAP_CLOSEL);
				}
                
                //return Float.floatToRawIntBits(GAP_EMISSIONL
                //        * (i + j) + GAP_OPENL + GAP_EXTENDL
                //        * (i + j - 1) + GAP_CLOSEL);
                
                
                return Float.floatToRawIntBits(//Maths.logAdd(
                        ((float)Math.log((i + j - 1 > 0 ? Math.pow(i + j - 1, -0.3) : 1) * 0.00929868352330243)) + SparseAlign./*GAP_OPENL +*/ GAP_CLOSEL +
                        SparseAlign.GAP_EMISSIONL * (i + j));
            }
        };
    }

    public static final double THRESHOLD = Maths.log(0.01);

    public static Generator_Int sparseAlignBoth(final Generator_Int gen,
            final Function_Int getX, final Function_Int getY,
            final Function_Int_2Args gapFn, final int xStart, final int yStart,
            final int xEnd, final int yEnd) {
        final int[] iA = new int[1000000];
        final int[] orig = new int[1000000];
        final double[] dA = new double[1000000];
        Arrays.fill(dA, Double.NEGATIVE_INFINITY);
        iA[0] = xStart;
        iA[1] = yStart;
        dA[0] = Maths.log(1.0);
        int j = 2;
        {
            final SortedMap<Long, Integer> l = new TreeMap<Long, Integer>();
            int i;
            while ((i = gen.gen()) != Integer.MAX_VALUE) {
                l.put(new Long(((long) i << 32) | gen.gen()),
                        new Integer(gen.gen()));
            }
            for (final Iterator<Long> it = l.keySet().iterator(); it.hasNext();) {
                final Long k = it.next();
                orig[j / 2] = l.get(k).intValue();
                iA[j++] = (int) (k.longValue() >> 32);
                iA[j++] = (int) k.longValue();
                // Debug.pl(iA[j-2] + " " + iA[j-1]);
            }
        }
        iA[j] = xEnd;
        iA[j + 1] = yEnd;
        SparseAlign.sparseAlign(dA, iA, j + 2, getX, getY, gapFn, false);
        final double[] dA2 = new double[dA.length];
        Arrays.fill(dA2, Double.NEGATIVE_INFINITY);
        dA2[j / 2] = Maths.log(1.0);
        SparseAlign.sparseAlign_Backwards(dA2, iA, j + 2, getX, getY, gapFn);
        double total = dA2[0] + dA[0];
        final double total2 = dA[j / 2] + dA2[j / 2];
        total = (total + total2) / 2;
        final int[] iA3 = new int[iA.length * 2];
        int k = 0;
        for (int i = 2; i < j; i += 2) {
            final double d = dA[i / 2] + dA2[i / 2] - total;
            Debug.pl(iA[i] + " " + iA[i + 1] + " " + dA[i / 2] + " "
                    + dA2[i / 2] + " " + d + " " + total + " "
                    + total2 + " "
                    + Maths.exp(Float.intBitsToFloat(orig[i / 2]))
                    + " " + Math.exp(d));
            if (d >= SparseAlign.THRESHOLD) {
                iA3[k++] = iA[i];
                iA3[k++] = iA[i + 1];
                iA3[k++] = Float.floatToIntBits((float) d);
            }
        }
        return Generators_Int.arrayGen(iA3, k);
    }

    /*public static void sparseAlign(double[] dA, int[] iA, int length,
            Function_Int getX, Function_Int getY,
            Function_Int_2Args gapFn, boolean backwards) {
        for (int i = 2; i < length; i += 2) {
            int x = iA[i];
            int y = iA[i + 1];
            double d = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < i; j += 2) {
                int x2 = iA[j];
                int y2 = iA[j + 1];
                if (x2 <= x - 1 && y2 <= y - 1) {
                    double p = dA[j / 2];
                    if (x2 == x - 1 && y2 == y - 1) {
                        // match
                        // Debug.pl(getX.fn(x) + " " + getY.fn(y) + " " + x + "
                        // " + y + " " + i + " " + length);
                        p += (backwards ? emit[getX.fn(x2) * 5
                                + getY.fn(y2)] : emit[getX.fn(x) * 5
                                + getY.fn(y)])
                                + MATCHL;
                        d = Maths.logAddQuality(p, d);
                    } else {
                        // gap
                        d = Maths.logAddQuality(Float
                                .intBitsToFloat(gapFn.fn(x - 1 - x2,
                                        y - 1 - y2))
                                + (backwards ? emit[getX.fn(x2) * 5
                                        + getY.fn(y2)] : emit[getX
                                        .fn(x)
                                        * 5 + getY.fn(y)]) + p, d);
                    }
                }
            }
            dA[i / 2] = d;
        }
    }*/

    public static void sparseAlign(final double[] dA, final int[] iA,
            final int length, final Function_Int getX, final Function_Int getY,
            final Function_Int_2Args gapFn, final boolean backwards) {
        for (int i = 2; i < length; i += 2) {
            final int x = iA[i];
            final int y = iA[i + 1];
            double d = Double.NEGATIVE_INFINITY;
            boolean edge = true;
            for (int j = 0; j < i; j += 2) {
                final int x2 = iA[j];
                final int y2 = iA[j + 1];
                if ((x2 <= x - 1) && (y2 <= y - 1)
                        && ((x2 == x - 1) || (y2 == y - 1))) {
                    edge = false;
                }
            }
            for (int j = 0; j < i; j += 2) {
                final int x2 = iA[j];
                final int y2 = iA[j + 1];
                if ((x2 <= x - 1) && (y2 <= y - 1)) {
                    double p = dA[j / 2];
                    if ((x2 == x - 1) && (y2 == y - 1)) {
                        // match
                        // Debug.pl(getX.fn(x) + " " + getY.fn(y) + " " + x + "
                        // " + y + " " + i + " " + length);
                        p += (backwards ? SparseAlign.emit[getX.fn(x2) * 5
                                + getY.fn(y2)] : SparseAlign.emit[getX.fn(x) * 5
                                + getY.fn(y)])
                                + SparseAlign.MATCHL;
                        d = Maths.logAddQuality(p, d);
                    } else {
                        if (edge || ((x2 == x - 1) || (y2 == y - 1))) {
							d = Maths.logAddQuality(Float
                                    .intBitsToFloat(gapFn.fn(x - 1
                                            - x2, y - 1 - y2))
                                    + (backwards ? SparseAlign.emit[getX.fn(x2)
                                            * 5 + getY.fn(y2)]
                                            : SparseAlign.emit[getX.fn(x) * 5
                                                    + getY.fn(y)])
                                    + p, d);
						}
                    }
                }
            }
            dA[i / 2] = d;
        }
    }

    public static void sparseAlign_Backwards(final double[] dA, final int[] iA,
            final int length, final Function_Int getX, final Function_Int getY,
            final Function_Int_2Args gapFn) {
        Array.reverseArray(dA, length / 2);
        Array.reverseArray(iA, length);
        Array.mingle(iA, length);
        for (int i = 0; i < length; i++) {
			iA[i] = 1000000 - iA[i];
		}
        SparseAlign.sparseAlign(dA, iA, length, SparseAlign.invert(getX), SparseAlign.invert(getY),
                gapFn, true);
        for (int i = 0; i < length; i++) {
			iA[i] = (iA[i] - 1000000) * -1;
		}
        Array.mingle(iA, length);
        Array.reverseArray(iA, length);
        Array.reverseArray(dA, length / 2);
    }

    public static Function_Int invert(final Function_Int fn) {
        return new Function_Int() {
            public int fn(final int x) {
                return fn.fn((x - 1000000) * -1);
            }
        };
    }
}
