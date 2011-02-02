/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Apr 5, 2005
 */
package bp.common.fp;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import bp.common.ds.ScrollingQueue;

/**
 * @author benedictpaten
 */
public final class Generators {

    public static final Generator arrayGenerator(final Object[] oA) {
        return new Generator() {
            int i = 0;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                return this.i < oA.length ? oA[this.i++] : null;
            }
        };
    }

    /**
     * Returns given value forever.
     * 
     * @param double1
     * @return
     */
    public static final Generator constant(final Object o) {
        return new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                return o;
            }
        };
    }

    /**
     * Appends together two generators. The generator is deemed to be empty when
     * it produces a null value. This null is not returned instead but replaced
     * by the first value from the second generator.
     * 
     * @param generator
     * @param generator2
     * @return
     */
    public static final Generator append(final Generator generator,
            final Generator generator2) {
        return new Generator() {
            Generator active = generator;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                final Object o = this.active.gen();
                if (o == null) {
                    this.active = generator2;
                    return this.active.gen();
                }
                return o;
            }
        };
    }

    /**
     * Converts an iterator into a generator. This conversion will fail if the
     * iterator returns null values.
     * 
     * @param it
     * @return
     */
    public static final Generator iteratorGenerator(final Iterator it) {
        return new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                return it.hasNext() ? it.next() : null;
            }
        };
    }

    /**
     * Passes the input values from the given generator through the function and
     * returns the output.
     * 
     * @param gen
     * @param fn
     * @return
     */
    public static final Generator map(final Generator gen,
            final Function fn) {
        return new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                final Object o = gen.gen();
                return o != null ? fn.fn(o) : null;
            }
        };
    }

    /**
     * If the given list is not empty it removes and returns the first elements
     * from the list, else it returns null.
     * 
     * @param transitiveAnchorsCacheRow
     * @return
     */
    public static final Generator queueGenerator(final List l) {
        return new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                return l.size() != 0 ? l.remove(0) : null;
            }
        };
    }

    public static Generator filter(final Generator generator,
            final Predicate predicate) {
        return new Generator() {
            public final Object gen() {
                Object o = generator.gen();
                while (o != null) {
                    if (predicate.test(o)) {
						return o;
					}
                    o = generator.gen();
                }
                return null;
            }
        };
    }

    /**
     * Splits the generator, does not copy objects so should be cautious to avoid overlap.
     * @param generator
     * @return
     */
    public static final Generator[] splitGenerator(
            final Generator generator, final Function cloneFunction) {
        final ScrollingQueue sQ = new ScrollingQueue(100, 0);
        final ScrollingQueue sQ2 = new ScrollingQueue(100, 0);
        return new Generator[] { Generators.feedGenerator(generator, sQ, sQ2, cloneFunction),
                Generators.feedGenerator(generator, sQ2, sQ, cloneFunction) };
    }

    public static final Generator feedGenerator(final Generator generator,
            final ScrollingQueue s, final ScrollingQueue s2, final Function function) {
        return new Generator() {
            public final Object gen() {
                if (s.size() > 0) {
					return s.removeFirst();
				}
                final Object o = generator.gen();
                if(o != null) {
					s2.add(function.fn(o));
				}
                return o;
            }
        };
    }

    public static Generator lineGenerator(final Reader r) {
        return new Generator() {
            LineNumberReader lNR = new LineNumberReader(r);
    
            public Object gen() {
                try {
                    final String s = this.lNR.readLine();
                    return s;
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                }
            };
        };
    }
}