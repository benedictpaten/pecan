/*
 * Created on Feb 3, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public class Functions_3Args {

    /**
     * 
     * @return the left argument of the function
     */
    public static final Function_3Args leftArg() {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o, final Object o2, final Object o3) {
                return o;
            }
        };
    }

    /**
     * 
     * @return the right argument of the function
     */
    public static final Function_3Args rightArg() {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o, final Object o2, final Object o3) {
                return o3;
            }
        };
    }

    /**
     * 
     * @return the middle (second) argument of the function
     */
    public static final Function_3Args middleArg() {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o, final Object o2, final Object o3) {
                return o2;
            }
        };
    }

    /**
     * Creates a function to pipe the output of the function to a second
     * {@link Function}.
     * 
     * @return piped function
     */
    public static final Function_3Args pipe(final Function fn2,
            final Function_3Args fn1) {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o, final Object o2, final Object o3) {
                return fn2.fn(fn1.fn(o, o2, o3));
            }
        };
    }

    /**
     * Creates a two argument function by replacing the last argument to the
     * {@link Function_4Args}with the supplied object o.
     * 
     * @param fn_3Args
     * @param o
     * @return
     */
    public static final Function_3Args rCurry(final Function_4Args fn_4Args,
            final Object o) {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o2, final Object o3, final Object o4) {
                return fn_4Args.fn(o2, o3, o4, o);
            }
        };
    }

    /**
     * Creates a two argument function by replacing the last argument to the
     * {@link Function_4Args}with the supplied object o.
     * 
     * @param fn_3Args
     * @param o
     * @return
     */
    public static final Function_3Args lCurry(final Function_4Args fn_4Args,
            final Object o) {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o2, final Object o3, final Object o4) {
                return fn_4Args.fn(o, o2, o3, o4);
            }
        };
    }

    /**
     * Concatenate string arguments. Useful for testing.
     * 
     * @return o + o2 + o3 (strings)
     */
    public static final Function_3Args concatenate() {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o, final Object o2, final Object o3) {
                return ((String) o) + ((String) o2) + ((String) o3);
            }
        };
    }

    /**
     * Feeds the first two arguments to the third argument which must be an
     * instance of {@link Function_2Args}, with the arguments reversed. Hence
     * o3.polygonClipper(o2, o).
     * 
     * @return
     */
    public static Function_3Args flipArguments() {
        return new Function_3Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_3Args#polygonClipper(java.lang.Object,
             *      java.lang.Object, java.lang.Object)
             */
            public Object fn(final Object o, final Object o2, final Object o3) {
                return ((Function_2Args) o3).fn(o2, o);
            }
        };
    }

}