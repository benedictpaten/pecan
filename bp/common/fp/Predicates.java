/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import java.util.HashSet;
import java.util.Set;

/**
 * Common predicates. Not all are without state.
 * 
 * @author benedictpaten
 */
public final class Predicates {

    /**
     * 
     * @return false
     */
    public static Predicate alwaysFalse() {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return false;
            }
        };
    }

    /**
     * 
     * @return true
     */
    public static Predicate alwaysTrue() {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return true;
            }
        };
    }

    /**
     * @param bP1
     * @param bP2
     * @return true if both bP1 and bP2 are true for the given object
     */
    public static Predicate and(final Predicate bP1,
            final Predicate bP2) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return bP1.test(o) && bP2.test(o);
            }
        };
    }

    /**
     * Returns true if the given predicate is false.
     * 
     * @author benedictpaten
     */
    public static Predicate inverse(final Predicate bP) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return !bP.test(o);
            }
        };
    }

    /**
     * Create single argument predicate from two argument predicate by
     * substituting left argument with that supplied.
     * 
     * @param bP
     *            two argument predicate
     * @param o
     *            left argument to substitute
     * @return result of bp with o as left argument
     */
    public static Predicate lCurry(final Predicate_2Args bP,
            final Object o) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o2) {
                return bP.test(o, o2);
            }
        };
    }

    /**
     * @param bP1
     * @param bP2
     * @return true if both bP1 and bP2 are true for the given object
     */
    public static Predicate nAnd(final Predicate bP1,
            final Predicate bP2) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return (!bP1.test(o)) && (!bP2.test(o));
            }
        };
    }

    /**
     * @param bP1
     * @param bP2
     * @return true if either bP1 or bP2 are true for the given object
     */
    public static Predicate or(final Predicate bP1,
            final Predicate bP2) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return bP1.test(o) || bP2.test(o);
            }
        };
    }

    /**
     * Takes the input object and maps its return value to the predicate.
     * 
     * @param bP
     *            predicate to test
     * @param polygonClipper
     *            filter function
     * @return
     */
    public final static Predicate pipe(final Predicate bP,
            final Function fn) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return bP.test(fn.fn(o));
            }
        };
    }

    /**
     * Create single argument predicate from two argument predicate by
     * substituting right argument with that supplied.
     * 
     * @param bP
     *            two argument predicate
     * @param o
     *            right argument to substitute
     * @return result of bp with o as right argument
     */
    public static Predicate rCurry(final Predicate_2Args bP,
            final Object o) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o2) {
                return bP.test(o2, o);
            }
        };
    }

    /**
     * Returns true if the function has already been called with an object that
     * is equals to the current object. Maintains a set, so can consume large
     * amounts of memory.
     * 
     * @author benedictpaten
     */
    public static Predicate seen() {
        return new Predicate() {
            Set<Object> set = new HashSet<Object>();

            public boolean test(final Object o) {
                if (this.set.contains(o)) {
					return true;
				}
                this.set.add(o);
                return false;
            }
        };
    }

    /**
     * 
     * @return true while the the sum total value of the {@link Number}objects
     *         is greater than the given number
     */
    public final static Predicate sumIsGreaterThan(final double d) {
        return new Predicate() {
            double sum = 0;

            public final boolean test(final Object o) {
                this.sum += ((Number) o).doubleValue();
                return this.sum > d;
            }
        };
    }

    /**
     * 
     * @return true while the the sum total value of the {@link Number}objects
     *         is less than the given number
     */
    public final static Predicate sumIsLessThan(final double d) {
        return new Predicate() {
            double sum = 0;

            public final boolean test(final Object o) {
                this.sum += ((Number) o).doubleValue();
                return this.sum < d;
            }
        };
    }

    /**
     * Returns a predicate that returns true the specified set number of times.
     * 
     * @param x
     *            number of times to return true
     * @return
     */
    public static Predicate trueXTimes(final int x) {
        return new Predicate() {
            int i = 0;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return this.i++ < x;
            }
        };
    }

    /**
     * 
     * @param bp
     *            two argument predicate with which to give the first two
     *            arguments unpacked from the input {@link Object[]}
     * @return
     */
    public static Predicate unpack(final Predicate_2Args bP) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return bP.test(((Object[]) o)[0], ((Object[]) o)[1]);
            }
        };
    }

    public final static Predicate window(final Predicate_2Args bP,
            final Object first) {
        return new Predicate() {
            Object o = first;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public final boolean test(final Object o2) {
                final boolean b = bP.test(this.o, o2);
                this.o = o2;
                return b;
            }
        };
    }

    /**
     * @param bP1
     * @param bP2
     * @return true if both bP1 and bP2 are true for the given object
     */
    public static Predicate xOr(final Predicate bP1,
            final Predicate bP2) {
        return new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                return bP1.test(o) ^ bP2.test(o);
            }
        };
    }
    
    /**
     * 
     * @return true if index (starting from 0, inclusice) of element is in sorted array.
     */
    public static Predicate indexInArray(
            final int[] iA, final int length) {
        return new Predicate() {
            int i=0;
            int j=0;
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(final Object o) {
                if((this.i < length) && (iA[this.i] == this.j++)) {
                    this.i++;
                    return true;
                }
                return false;
            }
        };
    }

}