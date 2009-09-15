/*
 * Created on Feb 2, 2005
 */
package bp.common.fp;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author benedictpaten
 */
public class Predicates_2Args {

    /**
     * 
     * @return true
     */
    public static Predicate_2Args alwaysTrue() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return true;
            }
        };
    }

    /**
     * Checks if for membership of a {@link Collection}.
     * 
     * @return true if o is a member of o2
     */
    public static Predicate_2Args containedIn() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return ((Collection) o2).contains(o);
            }
        };
    }

    /**
     * 
     * @return true if o == null ? o2 == null : o.equals(o2)
     */
    public static Predicate_2Args equal() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return o == null ? o2 == null : o.equals(o2);
            }
        };
    }

    /**
     * Takes two instances of {@link Number}. Tests with
     * {@link Number#doubleValue()}
     * 
     * @return true if o is greater than o2
     */
    public static Predicate_2Args greaterThan() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return ((Number) o).doubleValue() > ((Number) o2).doubleValue();
            }
        };
    }

    /**
     * Takes two instances of {@link Number}. Tests with
     * {@link Number#doubleValue()}
     * 
     * @return true if o is greater than or equal to o2
     */
    public static Predicate_2Args greaterThanOrEqual() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return ((Number) o).doubleValue() >= ((Number) o2)
                        .doubleValue();
            }
        };
    }

    /**
     * Takes a {@link Predicate_3Args}and converts it into a
     * {@link Predicate_2Args}by substituting the first argument to the
     * predicate with o.
     * 
     * @param bP
     * @param o
     *            the argument to substitute
     * @return
     */
    public static Predicate_2Args lCurry(final Predicate_3Args bP,
            final Object o) {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o2, final Object o3) {
                return bP.test(o, o2, o3);
            }
        };
    }

    /**
     * Takes two instances of {@link Number}. Tests with
     * {@link Number#doubleValue()}
     * 
     * @return true if o is less than o2
     */
    public static Predicate_2Args lessThan() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return ((Number) o).doubleValue() < ((Number) o2).doubleValue();
            }
        };
    }

    /**
     * Takes two instances of {@link Number}. Tests with
     * {@link Number#doubleValue()}
     * 
     * @return true if o is less than or equal o2
     */
    public static Predicate_2Args lessThanOrEqual() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return ((Number) o).doubleValue() <= ((Number) o2)
                        .doubleValue();
            }
        };
    }

    /**
     * 
     * @return true if (o != null && !o.equals(o2)) || o2 != null
     */
    public static Predicate_2Args notEqual() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return o == null ? o2 != null : !o.equals(o2);
            }
        };
    }

    /**
     * 
     * @param bP
     *            predicate to test
     * @param polygonClipper
     *            function to map arguments to the predicate argument
     * @return
     */
    public static Predicate_2Args pipe(final Predicate bP,
            final Function_2Args fn) {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return bP.test(fn.fn(o, o2));
            }
        };
    }

    /**
     * Takes a {@link Predicate_3Args}and converts it into a
     * {@link Predicate_2Args}by substituting the last argument to the
     * predicate with o.
     * 
     * @param bP
     * @param o
     *            the argument to substitute
     * @return
     */
    public static Predicate_2Args rCurry(final Predicate_3Args bP,
            final Object o3) {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return bP.test(o, o2, o3);
            }
        };
    }

    /**
     * @param args
     * @param function
     * @return
     */
    public static Predicate_2Args pipe(final Predicate_2Args bP,
            final Function fn) {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return bP.test(fn.fn(o), fn.fn(o2));
            }
        };
    }

    /**
     * Creates a function which checks if two object arrays are equals. Wrapper
     * for {@link Arrays#equals(Object[], Object[])}.
     * 
     * @return
     */
    public static Predicate_2Args arraysEqual() {
        return new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                return Arrays.equals((Object[]) o, (Object[]) o2);
            }
        };
    }

}