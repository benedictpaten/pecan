/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

package bp.common.maths;

import bp.common.fp.Function_Int;

public final class Maths {

    /**
     * Function counts the number of on bits in an integer via fast lookup.
     * 
     * @return
     */
    public static Function_Int bitCounter() {
        return new Function_Int() {
            final int mask;

            int[] table;

            {
                this.table = new int[(int) Math.pow(2, 16)];
                for (int i = 0; i < this.table.length; i++) {
                    int j = 0, k = i;
                    while (k != 0) {
                        if ((k & 1) != 0) {
							j++;
						}
                        k = (k >> 1);
                    }
                    this.table[i] = j;
                }
                this.mask = ((int) Math.pow(2, 16)) - 1;
            }

            public int fn(final int n) {
                return this.table[(n & this.mask)] + this.table[n >>> 16];
            }
        };
    }

    public static final float exp(final float x) {
        // return exp(x);
        if (x > -2) {
            if (x > -0.5) {
                if (x > 0) {
					return (float) Math.exp(x);
				}
                return ((((0.03254409303190190000f * x + 0.16280432765779600000f)
                        * x + 0.49929760485974900000f)
                        * x + 0.99995149601363700000f)
                        * x + 0.99999925508501600000f);
            }
            if (x > -1) {
				return ((((0.01973899026052090000f * x + 0.13822379685007000000f)
                        * x + 0.48056651562365000000f)
                        * x + 0.99326940370383500000f)
                        * x + 0.99906756856399500000f);
			}
            return ((((0.00940528203591384000f * x + 0.09414963667859410000f)
                    * x + 0.40825793595877300000f)
                    * x + 0.93933625499130400000f)
                    * x + 0.98369508190545300000f);
        }
        if (x > -8) {
            if (x > -4) {
				return ((((0.00217245711583303000f * x + 0.03484829428350620000f)
                        * x + 0.22118199801337800000f)
                        * x + 0.67049462206469500000f)
                        * x + 0.83556950223398500000f);
			}
            return ((((0.00012398771025456900f * x + 0.00349155785951272000f)
                    * x + 0.03727721426017900000f)
                    * x + 0.17974997741536900000f)
                    * x + 0.33249299994217400000f);
        }
        if (x > -16) {
			return ((((0.00000051741713416603f * x + 0.00002721456879608080f)
                    * x + 0.00053418601865636800f)
                    * x + 0.00464101989351936000f)
                    * x + 0.01507447981459420000f);
		}
        return 0;
    }

    public static final double log(final double x) {
        return Math.log(x);
    }

    public static final float log(final float x) {
        return (float) Maths.log((double) x);
    }

    public static final double logAddQuality(final double x, final double y) {
        if (x < y) {
			return x == Float.NEGATIVE_INFINITY ? y : Math.log(Math
                    .exp(x - y) + 1)
                    + y;
		}
        return y == Float.NEGATIVE_INFINITY ? x : Math.log(Math.exp(y
                - x) + 1)
                + x;
    }

    public static final float LOG_UNDERFLOW_THRESHOLD = 7.5f;

    public static final float LOG_ZERO = -2e20f;

    // three decimal places
    public static final float logAdd(final float x, final float y) {
        // return (float)logAddQuality(x, y);
        if (x < y) {
			return ((x <= Maths.LOG_ZERO) || (y - x >= Maths.LOG_UNDERFLOW_THRESHOLD)) ? y
                    : Maths.lookup(y - x) + x;
		}
        return ((y <= Maths.LOG_ZERO) || (x - y >= Maths.LOG_UNDERFLOW_THRESHOLD)) ? x
                : Maths.lookup(x - y) + y;
    }

    public static final float lookup(final float x) {
        // return (float)Math.log (Math.exp(x) + 1);
        if (x <= 2.50f) {
            if (x <= 1.00f) {
                return ((-0.009350833524763f * x + 0.130659527668286f)
                        * x + 0.498799810682272f)
                        * x + 0.693203116424741f;
            }
            return ((-0.014532321752540f * x + 0.139942324101744f)
                    * x + 0.495635523139337f)
                    * x + 0.692140569840976f;
        }
        if (x <= 4.50f) {
            return ((-0.004605031767994f * x + 0.063427417320019f)
                    * x + 0.695956496475118f)
                    * x + 0.514272634594009f;
        }
        return ((-0.000458661602210f * x + 0.009695946122598f) * x + 0.930734667215156f)
                * x + 0.168037164329057f; 
        
        /*if (x <= 1.00f) {
            return ((-0.009350833524763f * x + 0.130659527668286f)
                    * x + 0.498799810682272f)
                    * x + 0.693203116424741f;
        }
        if (x <= 2.50f) {
            return ((-0.014532321752540f * x + 0.139942324101744f)
                    * x + 0.495635523139337f)
                    * x + 0.692140569840976f;
        }
        if (x <= 4.50f) {
            return ((-0.004605031767994f * x + 0.063427417320019f)
                    * x + 0.695956496475118f)
                    * x + 0.514272634594009f;
        }
        return ((-0.000458661602210f * x + 0.009695946122598f) * x + 0.930734667215156f)
                * x + 0.168037164329057f;*/
    }
}