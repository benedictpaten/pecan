/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 2, 2005
 */
package bp.common.fp;

import java.util.Collection;
import java.util.Map;

/**
 * @author benedictpaten
 */
public class Functions_2Args {

    /**
     * Append the first argument to the second. The second argument must be a
     * member of the {@link java.util.Collections}framework.
     * 
     * @return {@link Collection}
     */
    public static final Function_2Args append() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                ((Collection<Object>) o2).add(o);
                return o2;
            }
        };
    }

    /**
     * Takes the contents of a {@link Collection}(left argument) and appends
     * this to the right {@link Collection}. Like
     * {@link Collection#addAll(java.util.Collection)}.
     * 
     * @return
     */
    public static final Function_2Args appendCollection() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                ((Collection) o2).addAll((Collection) o);
                return o2;
            }
        };
    }

    /**
     * Returns a function to concatenate two int arrays together. Neither input
     * arrays is modified.
     * 
     * @return new array containing both the first and second array
     */
    public static Function_2Args concatenateIntArrays() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                final int[] iA1 = (int[]) o, iA2 = (int[]) o2;
                final int[] iA = new int[iA1.length + iA2.length];
                System.arraycopy(iA1, 0, iA, 0, iA1.length);
                System.arraycopy(iA2, 0, iA, iA1.length, iA2.length);
                return iA;
            }
        };
    }

    /**
     * Concatenate string arguments. Useful for testing.
     * 
     * @return o + o2 (strings)
     */
    public static final Function_2Args concatenate() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return ((String) o) + ((String) o2);
            }
        };
    }

    /**
     * Casts the two arguments to {@link Number}and returns a {@link Double}
     * containing the result of the first argument divided by the second as a
     * {@link Number#doubleValue()}.
     * 
     * @return {@link Double}
     */
    public static final Function_2Args divide() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return new Double(((Number) o).doubleValue()
                        / ((Number) o2).doubleValue());
            }
        };
    }

    /**
     * Creates a two argument function by replacing the first argument to the
     * {@link Function_3Args}with the supplied object o.
     * 
     * @param fn_3Args
     * @param o
     * @return
     */
    public static final Function_2Args lCurry(final Function_3Args fn_3Args,
            final Object o) {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o2, final Object o3) {
                return fn_3Args.fn(o, o2, o3);
            }
        };
    }

    /**
     * 
     * @return the left argument of the function
     */
    public static final Function_2Args leftArg() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return o;
            }
        };
    }

    /**
     * Creates a {@link Function_2Args}by piping the left argument through a
     * {@link Function}first.
     * 
     * @param fn2
     * @param fn1
     * @return
     */
    public static final Function_2Args lPipe(final Function_2Args fn2,
            final Function fn1) {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return fn2.fn(fn1.fn(o), o2);
            }
        };
    }

    /**
     * Wraps a {@link Map}object.
     * 
     * @return the value mapped to key o in map o2
     */
    public static final Function_2Args map() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return ((Map) o2).get(o);
            }
        };
    }

    /**
     * Uses {@link Comparable#compareTo(java.lang.Object)}to compare the
     * objects.
     * 
     * @return function to find the maximum of two objects
     */
    public static final Function_2Args max() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return ((Comparable<Object>) o).compareTo(o2) > 0 ? o : o2;
            }
        };
    }

    /**
     * Uses {@link Comparable#compareTo(java.lang.Object)}to compare the
     * objects.
     * 
     * @return function to find the minimum of two objects
     */
    public static final Function_2Args min() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return ((Comparable<Object>) o).compareTo(o2) < 0 ? o : o2;
            }
        };
    }

    /**
     * Casts the two arguments to {@link Number}and returns a {@link Double}
     * containing the result of their multiplied {@link Number#doubleValue()}
     * values.
     * 
     * @return {@link Double}
     */
    public static final Function_2Args multiply() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return new Double(((Number) o).doubleValue()
                        * ((Number) o2).doubleValue());
            }
        };
    }

    /**
     * Creates a function to pipe the output of the function to a second
     * {@link Function}.
     * 
     * @return piped function
     */
    public static final Function_2Args pipe(final Function fn2,
            final Function_2Args fn1) {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return fn2.fn(fn1.fn(o, o2));
            }
        };
    }

    /**
     * Takes a predicate and returns {@link Boolean#TRUE}if the result is true
     * and {@link Boolean#FALSE}if the result is false. Object o is the
     * argument to test and Object o2 is the {@link Predicate}.
     * 
     * @return
     */
    public static final Function_2Args predicateWrapper() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return ((Predicate) o2).test(o) ? Boolean.TRUE : Boolean.FALSE;
            }
        };
    }

    /**
     * Creates a two argument function by replacing the last argument to the
     * {@link Function_3Args}with the supplied object o.
     * 
     * @param fn_3Args
     * @param o
     * @return
     */
    public static final Function_2Args rCurry(final Function_3Args fn_3Args,
            final Object o) {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o2, final Object o3) {
                return fn_3Args.fn(o2, o3, o);
            }
        };
    }

    /**
     * 
     * @return the right argument of the function
     */
    public static final Function_2Args rightArg() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return o2;
            }
        };
    }

    /**
     * Creates a {@link Function_2Args}by piping the right argument through a
     * {@link Function}first.
     * 
     * @param fn2
     * @param fn1
     * @return
     */
    public static final Function_2Args rPipe(final Function_2Args fn2,
            final Function fn1) {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return fn2.fn(o, fn1.fn(o2));
            }
        };
    }

    /**
     * Casts the two arguments to {@link Number}and returns a {@link Double}
     * containing the result of the first value minus the second as a
     * {@link Number#doubleValue()}.
     * 
     * @return {@link Double}
     */
    public static final Function_2Args subtract() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return new Double(((Number) o).doubleValue()
                        - ((Number) o2).doubleValue());
            }
        };
    }

    /**
     * Casts the two arguments to {@link Number}and returns a {@link Double}
     * containing the result of their summed {@link Number#doubleValue()}
     * values.
     * 
     * @return {@link Double}
     */
    public static final Function_2Args sum() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return new Double(((Number) o).doubleValue()
                        + ((Number) o2).doubleValue());
            }
        };
    }

    /**
     * Like {@link Functions_2Args#concatenateIntArrays()}but for Object
     * arrays.
     * 
     * @return
     */
    public static Function_2Args concatenateArrays() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                final Object[] iA1 = (Object[]) o, iA2 = (Object[]) o2;
                final Object[] iA = new Object[iA1.length + iA2.length];
                System.arraycopy(iA1, 0, iA, 0, iA1.length);
                System.arraycopy(iA2, 0, iA, iA1.length, iA2.length);
                return iA;
            }
        };
    }

    /**
     * Adds together two log values. Assumes arguments are {@link Number}s.
     * 
     * @return
     */
    public static Function_2Args sumLogs() {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#fn(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                final double n = ((Number) o).doubleValue(), m = ((Number) o2)
                        .doubleValue();
                if (n > m) {
                    final double i = m - n;
                    return new Double(n + Math.log(1 + Math.exp(i)));
                } else {
                    final double i = n - m;
                    return new Double(m + Math.log(1 + Math.exp(i)));
                }
            }
        };
    }

    /**
     * @param ds
     * @param i
     * @return
     */
    public static Function_2Args matrix2DLookup(final double[] matrix,
            final int rowLength) {
        return new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#fn(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                final int x = ((Number) o).intValue(), y = ((Number) o2).intValue();
                return new Double(matrix[y * rowLength + x]);
            }
        };
    }

}