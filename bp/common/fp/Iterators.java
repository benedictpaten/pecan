/*
 * Created on Jan 31, 2005
 */
package bp.common.fp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public class Iterators {

    /**
     * Takes a sorted list and merges sequentially elements that are equal
     * according to a supplied comparator. The order of arguments to the merger
     * function corresponds to the ordering in the list, where multiple
     * identical elements will be called as follows.. { { { 1, 2 }, 3 }, 4 }
     * etc.
     * 
     * @param it
     * @return
     */
    public static final Iterator uniq(final Iterator it,
            final Comparator c, final Function_2Args merger) {
        return new Iterator() {
            Object p = null, p2;

            {
                if (it.hasNext()) {
					this.p2 = it.next();
				}
                this.getNext();
            }

            public final boolean hasNext() {
                return this.p != null;
            }

            private final void getNext() {
                this.p = this.p2;
                if (it.hasNext()) {
                    this.p2 = it.next();
                    if (c.compare(this.p, this.p2) == 0) {
                        this.p2 = merger.fn(this.p, this.p2);
                        this.getNext();
                    }
                } else {
                    this.p2 = null;
                }
            }

            public Object next() {
                if (Debug.DEBUGCODE && (this.p == null)) {
                    throw new NoSuchElementException();
                }
                final Object rV = this.p;
                this.getNext();
                return rV;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Merges together two sorted iterators into a single sorted iterator. For
     * objects which are deemed equal, the object from the first iterator will
     * take precedence.
     * 
     * @param it
     *            first list
     * @param it2
     *            second
     * @param c
     *            comparator to determine order
     * @return merged iterator
     */
    public static final Iterator merge(final Iterator it,
            final Iterator it2, final Comparator c) {
        return new Iterator() {
            Object o, o2, o3;

            {
                if (it.hasNext()) {
					this.o = it.next();
				}
                if (it2.hasNext()) {
					this.o2 = it2.next();
				}
                this.getNext();
            }

            public final boolean hasNext() {
                return this.o3 != null;
            }

            private final void getNext() {
                if (this.o != null) {
                    if (this.o2 != null) {
                        final int i = c.compare(this.o, this.o2);
                        if (i <= 0) {
                            this.o3 = this.o;
                            this.o = it.hasNext() ? it.next() : null;
                        } else {
                            this.o3 = this.o2;
                            this.o2 = it2.hasNext() ? it2.next() : null;
                        }
                    } else {
                        this.o3 = this.o;
                        this.o = it.hasNext() ? it.next() : null;
                    }
                } else {
                    if (this.o2 != null) {
                        this.o3 = this.o2;
                        this.o2 = it2.hasNext() ? it2.next() : null;
                    } else {
                        this.o3 = null;
                    }
                }
            }

            public Object next() {
                if (Debug.DEBUGCODE && (this.o3 == null)) {
                    throw new NoSuchElementException();
                }
                final Object rV = this.o3;
                this.getNext();
                return rV;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * Returns an iterator that counts up from the specified number. At each
     * call to next() the counter value is returned and then one is subsequently
     * added to the value of the counter.
     * 
     * @param i
     *            number from which to count (inclusive)
     * @return {@link Integer}
     */
    public static final Iterator count(final int i) {
        return new Iterator() {
            int j = i;

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#hasNext()
             */
            public final boolean hasNext() {
                return true;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#next()
             */
            public final Object next() {
                return new Integer(this.j++);
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#remove()
             */
            public final void remove() {
                throw new IllegalStateException(
                        "Meaningless operation");
            }
        };
    }

    /**
     * Concatenates a list of iterators together to produce a single iterator.
     * 
     * @param it
     *            an iterator over a list of iterators
     * @return iterator
     */
    public static final Iterator chain(final Iterator it) {
        return new Iterator() {
            final Object END = new Object[] {};

            Iterator it2;

            Object o;
            {
                if (it.hasNext()) {
                    this.it2 = (Iterator) it.next();
                    this.getNext();
                } else {
					this.o = this.END;
				}
            }

            public final void getNext() {
                while (this.it2.hasNext()) {
                    this.o = this.it2.next();
                    return;
                }
                while (it.hasNext()) {
                    this.it2 = (Iterator) it.next();
                    this.getNext();
                    return;
                }
                this.o = this.END;
            }

            public final boolean hasNext() {
                return this.o != this.END;
            }

            public final Object next() {
                if (this.o == this.END) {
					throw new NoSuchElementException();
				}
                final Object rV = this.o;
                this.getNext();
                return rV;
            }

            public final void remove() {
                throw new RuntimeException("Not implemented");
            }
        };
    }

    /**
     * Filter the output of an iterator according to the supplied predicate.
     * 
     * @param it
     *            the unfiltered iterator
     * @param bP
     *            predicate to test
     * @return filtered iterator
     */
    public static final Iterator filter(final Iterator it,
            final Predicate bP) {
        return new GeneratorIterator(new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                while (it.hasNext()) {
                    final Object o = it.next();
                    if (bP.test(o)) {
						return o;
					}
                }
                return null;
            }
        });
    }

    /**
     * Apply polygonClipper to each element of the list in turn.
     * 
     * @param it
     * @param polygonClipper
     * @return
     */
    public static final Iterator map(final Iterator it, final Function fn) {
        return new Iterator() {

            public final boolean hasNext() {
                return it.hasNext();
            }

            public final Object next() {
                return fn.fn(it.next());
            }

            public final void remove() {
                throw new RuntimeException("Not implemented");
            }

        };
    }

    /**
     * Zips together the elements of two iterators into a single iterator.
     * Elements are stored in a two element Object array. The iterator is
     * exhausted when either one of the underlying iterators is exhausted.
     * 
     * @param it
     * @param it2
     * @param c
     *            make array of given class
     * @return
     */
    public static final Iterator zip(final Iterator it, final Iterator it2,
            final Class c) {
        return new Iterator() {

            public boolean hasNext() {
                return it.hasNext() && it2.hasNext();
            }

            public final Object next() {
                final Object[] oA = (Object[]) Array.newInstance(c, 2);
                oA[0] = it.next();
                oA[1] = it2.next();
                return oA;
            }

            public final void remove() {
                throw new RuntimeException("Not yet implemented");
            }

        };
    }

    /**
     * Return the same element a specified number of times.
     * 
     * @param o
     *            object to return
     * @param times
     *            number of times to return o
     * @return
     */
    public static final Iterator repeat(final Object o, final int times) {
        return new Iterator() {
            int i = times;

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#hasNext()
             */
            public final boolean hasNext() {
                return this.i > 0;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#next()
             */
            public final Object next() {
                if (this.i-- <= 0) {
					throw new NoSuchElementException();
				}
                return o;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#remove()
             */
            public final void remove() {
                throw new RuntimeException("Not yet implemented");
            }
        };
    }

    /**
     * Concatenates the contents of the array into a list, sorts it and returns
     * an iterator over this list.
     * 
     * @param it
     * @param c
     * @return
     */
    public static final Iterator sort(final Iterator it, final Comparator c) {
        final List l = (List) IterationTools.append(it, new ArrayList());
        Collections.sort(l, c);
        return l.iterator();
    }

    /**
     * Makes a copy of the iterator such that both iterators returned iterators
     * will return the same stream of elements. It destorys the incoming
     * iterator.
     * 
     * @param iterator
     * @return
     */
    public static final Iterator[] duplicate(final Iterator<Integer> it) {
        final List<Object> l = new LinkedList<Object>(), l2 = new LinkedList<Object>();
        final Generator gen = new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (l2.size() == 0) {
                    if (it.hasNext()) {
                        Object o = it.next();
                        l.add(o);
                        return o;
                    }
                    return null;
                }
                return l2.remove(0);
            }
        };
        final Generator gen2 = new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (l.size() == 0) {
                    if (it.hasNext()) {
                        Object o = it.next();
                        l2.add(o);
                        return o;
                    }
                    return null;
                }
                return l.remove(0);
            }
        };
        return new Iterator[] { new GeneratorIterator(gen),
                new GeneratorIterator(gen2) };
    }

}