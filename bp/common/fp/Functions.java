/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Common functions.
 * 
 * @author benedictpaten
 */
public class Functions {

    /**
     * 
     * 
     * @return the input object
     */
    public static Function doNothing() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                return o;
            }
        };
    }

    /**
     * Java does not supply an Iterable interface, but objects in the
     * {@link Collection}are all iterable. The returned method checks if the
     * object is either a member of {@link Collection}framework or if any it
     * implements {@link Iterable}and returns an iterator.
     * 
     * @return function that returns an iterator object for the given argument
     */
    public static Function getIterator() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                return o instanceof Collection ? ((Collection) o).iterator()
                        : ((Iterable) o).iterator();
            }
        };
    }

    /**
     * Function to return a constant value.
     * 
     * @param o
     * @return the constant value o
     */
    public static Function constant(final Object o) {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o2) {
                return o;
            }
        };
    }

    /**
     * Creates a unary input function from a binary input function by always
     * supply the Object o as the left argument.
     * 
     * @param fn1
     * @param o
     * @return unary function
     */
    public static Function lCurry(final Function_2Args fn1, final Object o) {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o2) {
                return fn1.fn(o, o2);
            }
        };
    }

    /**
     * Returns a function to parse a number from a string using the
     * {@link java.util.Double}constructor. The return value of the function is
     * a {@link java.util.Double}.
     * 
     * @author benedictpaten
     */
    public static final Function parseNumber() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.util.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                return new Double((String) o);
            }
        };
    }

    /**
     * Creates a function that pipes the output of fn1 into fn2 and returns the
     * result.
     * 
     * @param fn2
     *            the second function to apply
     * @param fn1
     *            the first function to apply
     * @return
     */
    public static Function pipe(final Function fn2, final Function fn1) {
        return new Function() {

            public Object fn(final Object o) {
                return fn2.fn(fn1.fn(o));
            }
        };
    }

    /**
     * Creates a unary input function from a binary input function by always
     * supply the Object o as the right argument.
     * 
     * @param fn1
     * @param o
     * @return unary function
     */
    public static Function rCurry(final Function_2Args fn1, final Object o) {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o2) {
                return fn1.fn(o2, o);
            }
        };
    }

    /**
     * Returns a function that takes a single {@link Object[]}and unpacks the
     * arguments for the given function left to right.
     * 
     * @param polygonClipper
     *            function to which arguments will be unpacked
     * @return
     */
    public static final Function unpack(final Function_2Args fn) {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                return fn.fn(((Object[]) o)[0], ((Object[]) o)[1]);
            }
        };
    }

    /**
     * Returns a function that takes a single {@link Object[]}and unpacks the
     * arguments for the given function left to right.
     * 
     * @param polygonClipper
     *            function to which arguments will be unpacked
     * @return
     */
    public static final Function unpack(final Function_3Args fn) {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                return fn.fn(((Object[]) o)[0], ((Object[]) o)[1],
                        ((Object[]) o)[2]);
            }
        };
    }

    /**
     * Returns the absolute difference between two {@link Number}.
     * 
     * @return
     */
    public final static Function abs() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o) {
                return new Double(Math.abs(((Number) o).doubleValue()));
            }
        };
    }

    /**
     * Returns the next element from the supplied iterator.
     * 
     * @author benedictpaten
     */
    public final static Function next() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o) {
                return ((Iterator) o).next();
            }
        };
    }

    /**
     * Wraps the input in a single element object array.
     * 
     * @return
     */
    public static Function pack() {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                return new Object[] { o };
            }
        };
    }

    /**
     * Takes arguments as {@link Number}and returns the corresponding value in
     * matrix.
     * 
     * @param ds
     * @return
     */
    public static Function matrix1DLookUp(final double[] ds) {
        return new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#fn(java.lang.Object)
             */
            public Object fn(final Object o) {
                return new Double(ds[((Number) o).intValue()]);
            }
        };
    }

    public static Function getString() {
       return new Function() {
         public Object fn(final Object o) {
             return o.toString();
        }  
       };
    }

    public static Function chunkLine(final Pattern p) {
        return new Function() {
            public Object fn(final Object o) {
                final String s= (String)o;
                return p.split(s);
            }
        };
    }
}