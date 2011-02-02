/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 2, 2005
 */
package bp.common.fp;

import java.util.Iterator;
import java.util.List;

/**
 * Higher order iterators composed from iterators in the
 * {@link bp.common.fp.Iterators}class.
 * 
 * @author benedictpaten
 */
public class HOIterators {

    /**
     * Creates an iterator that returns both the elements of the iterator and
     * the given object.
     * 
     * @param o
     *            Iterable object
     * @return Object[] { Object o, Iterator element }
     */
    public static Iterator zipWithObject(final Iterator it, final Object o, final Class c) {  
        return Iterators.zip(Iterators.repeat(o, Integer.MAX_VALUE), it, c);
    }

    /**
     * Create an iterator that returns all pairwise combinations of the list.
     * 
     * @param it
     * @return
     */
    public static Iterator allAgainstAll_ASymmetric_NoDiagonals(final List l, final Class c) {
        final Iterator count = Iterators.count(1);
        return Iterators.chain(Iterators.map(l.iterator(), new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(final Object o) {
                return HOIterators.zipWithObject(l.subList(((Number)count.next()).intValue(), l.size()).iterator(), o, c);
            }
        }));
    }
    
    /**
     * Returns Iterator that recurses 
     * @param it
     * @return
     */
    public static Iterator chainIterables(final Iterator it) {
        return Iterators.chain(Iterators.map(it, Functions.getIterator()));
    }
    
}