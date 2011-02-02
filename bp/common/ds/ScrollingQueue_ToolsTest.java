/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Sep 6, 2005
 */
package bp.common.ds;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Generator;

/**
 * @author benedictpaten
 */
public class ScrollingQueue_ToolsTest
                                     extends TestCase {
    public void testQueueGenerator() {
        final ScrollingQueue sQ = new ScrollingQueue(1000, 5);
        final Generator gen = ScrollingQueue_Tools.queueGenerator(sQ);
        sQ.add("1");
        sQ.add(null);
        sQ.add("2");
        Assert.assertEquals(gen.gen(), "1");
        Assert.assertEquals(gen.gen(), null);
        Assert.assertEquals(gen.gen(), "2");
        Assert.assertEquals(gen.gen(), null);
        Assert.assertEquals(gen.gen(), null);
        sQ.add("3");
        Assert.assertEquals(gen.gen(), "3");
    }
}
