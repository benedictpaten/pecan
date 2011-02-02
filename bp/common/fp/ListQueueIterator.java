/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A queue iterator based upon a {@link java.util.LinkedList}.
 * @author benedictpaten
 */
public class ListQueueIterator implements QueueIterator {
    List<Object> list;
    
    /**
     * 
     */
    public ListQueueIterator() {
        this.list = new LinkedList<Object>();
    }

    /* (non-Javadoc)
     * @see bp.common.fp.QueueIterator#add(java.lang.Object)
     */
    public void add(final Object o) {
        this.list.add(o);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return this.list.size() != 0;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
        if(this.list.size() == 0) {
			throw new NoSuchElementException("Queue is empty");
		}
        return this.list.remove(0);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new RuntimeException("Method not implemented yet");
    }

}
