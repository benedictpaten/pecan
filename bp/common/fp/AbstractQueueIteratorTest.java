/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class AbstractQueueIteratorTest
                                      extends TestCase {
    QueueIterator q;
    
    /**
     * 
     */
    public AbstractQueueIteratorTest(final String s) {
        super(s);
    }
    
    public void testIterator() {
        Assert.assertFalse(this.q.hasNext());
        this.q.add("1");
        Assert.assertTrue(this.q.hasNext());
        Assert.assertEquals(this.q.next(), "1");
        Assert.assertFalse(this.q.hasNext());
        this.q.add("2");
        this.q.add("3");
        Assert.assertEquals(this.q.next(), "2");
        Assert.assertEquals(this.q.next(), "3");
        Assert.assertFalse(this.q.hasNext());
    }
}
