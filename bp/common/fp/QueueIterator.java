/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import java.util.Iterator;

/**
 * For creating a pipe like iterator. Elements are added to the queue using
 * calls to add. Elements are retrieved by calls to the iterator. 
 * 
 * @author benedictpaten
 */
public interface QueueIterator extends Iterator {
    /**
     * Add an element to the end of the queue.
     * 
     * @param o
     */
    void add(Object o);
}