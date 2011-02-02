/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Sep 6, 2005
 */
package bp.common.ds;

import bp.common.fp.Generator;

/**
 * @author benedictpaten
 */
public class ScrollingQueue_Tools {

    public static Generator queueGenerator(final ScrollingQueue sQ) {
        return new Generator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (sQ.firstIndex() < sQ.lastIndex()) {
                    final Object o = sQ.get(sQ.firstIndex());
                    sQ.removeFirst();
                    return o;
                } else {
                    sQ.add(null);
                    sQ.removeFirst();
                    return null;
                }
            }
        };
    }
}