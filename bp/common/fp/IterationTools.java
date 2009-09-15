/*
 * Created on Feb 2, 2005
 */
package bp.common.fp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author benedictpaten
 */
public class IterationTools {

    /**
     * Appends the given object onto the end of the current iterator list.
     * 
     * @param it
     * @param o
     * @return
     */
    public static final Iterator addToEnd(final Iterator it, final Object o) {
        return Iterators.chain(Arrays
                .asList(
                        new Object[] { it,
                                Arrays.asList(new Object[] { o }).iterator() })
                .iterator());
    }

    /**
     * Counts number of elements in iterator until the iterator is empty.
     * Caution, may legally enter an infinite loop.
     * 
     * @param it
     * @return number of elements
     */
    public static final Double noOfElements(final Iterator it) {
        return IterationTools.sum(Iterators.map(it, Functions.constant(new Integer(1))));
    }

    /**
     * Sums the elements in a list until the iterator is empty. Assumes the list
     * is composed of {@link Number}.
     * 
     * @param it
     * @return sum of elements
     */
    public static final Double sum(final Iterator it) {
        return (Double) IterationTools.reduce(it, new Double(0),
                Functions_2Args.sum());
    }

    /**
     * Append the contents of an iterator onto the end of a {@link Collection}.
     * 
     * @param it
     * @param c
     * @return the collection to which the items of the iterator were appended.
     */
    public static final Collection append(final Iterator it, final Collection c) {
        while(it.hasNext()) {
			c.add(it.next());
		}
        return c;
        //return (Collection) IterationTools.reduce(it, c, Functions_2Args
        //        .append());
    }

    /**
     * Tests if two iterators contain the same sequence of object according to a
     * given {@link Predicate_2Args}.
     * 
     * @param it
     * @param it2
     * @param bP
     * @return true if the iterators are of equal length and for all theirs
     *         pairs of elements they are equal
     */
    public static final boolean equals(final Iterator it, final Iterator it2, final Predicate_2Args bP) {
        return !(Iterators.filter(Iterators.zip(it, it2, Object.class),
                Predicates.inverse(Predicates.unpack(bP))).hasNext()
                || it.hasNext() || it2.hasNext());
    }

    /**
     * A left recursive reduce function. Applies the supplied binary function to
     * the first element of the list with the base object o. Then in turn it
     * applies the result of the function and subsequent elements of the list
     * together and eventually returns the final product. The base object is
     * parsed as the second argument to the binary function.
     * 
     * @param it
     * @param o
     * @param polygonClipper
     * @return
     */
    public static final Object reduce(final Iterator it, Object o, final Function_2Args fn) {
        while (it.hasNext()) {
			o = fn.fn(it.next(), o);
		}
        return o;
    }

    /**
     * Like the python join function. Appends the seperator at the beginning and
     * end also.
     * 
     * @param sA
     * @param separator
     * @return
     */
    public static final String join(final Object[] oA, final String separator) {
        return (String) IterationTools.reduce(new GeneratorIterator(Generators.map(Generators
                .arrayGenerator(oA), Functions.getString())), separator, Functions_2Args.rCurry(
                Functions_3Args.flipArguments(), Functions_2Args.pipe(Functions
                        .rCurry(Functions_2Args.concatenate(), separator),
                        Functions_2Args.concatenate())));
    }

    /**
     * Like the python join function. Appends the seperator at the beginning and
     * end also.
     * 
     * @param sA
     * @param separator
     * @return
     */
    public static final String join(final int[] sA, final String separator) {
        final StringBuffer sB = new StringBuffer(separator);
        for (final int element : sA) {
            sB.append(element);
            sB.append(separator);
        }
        return sB.toString();
    }
    
    /**
     * Like the python join function. Appends the seperator at the beginning and
     * end also.
     * 
     * @param sA
     * @param separator
     * @return
     */
    public static final String join(final double[] sA, final String separator) {
        final StringBuffer sB = new StringBuffer(separator);
        for (final double element : sA) {
            sB.append(element);
            sB.append(separator);
        }
        return sB.toString();
    }
    
    /**
     * Like the python join function. Appends the seperator at the beginning and
     * end also.
     * 
     * @param sA
     * @param separator
     * @return
     */
    public static final String join(final float[] sA, final String separator) {
        final StringBuffer sB = new StringBuffer(separator);
        for (final float element : sA) {
            sB.append(element);
            sB.append(separator);
        }
        return sB.toString();
    }
    
    /**
     * Like the python join function. Appends the seperator at the beginning and
     * end also.
     * 
     * @param sA
     * @param separator
     * @return
     */
    public static final String join(final boolean[] sA, final String separator) {
        final StringBuffer sB = new StringBuffer(separator);
        for (final boolean element : sA) {
            sB.append(element);
            sB.append(separator);
        }
        return sB.toString();
    }
}