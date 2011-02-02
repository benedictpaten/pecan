/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Aug 18, 2005
 */
package bp.common.ds;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class LockedObjectTest
                             extends TestCase {
    public void testLockedObject() {
        final LockedObject lO = new LockedObject(new Integer(5));
        Assert.assertEquals(((Integer)lO.get()).intValue(), 5);
        lO.release();
        Assert.assertEquals(((Integer)lO.get()).intValue(), 5);
        lO.release();
        Assert.assertEquals(((Integer)lO.get()).intValue(), 5);
        try {
            lO.get();
            Assert.fail();
        }
        catch(final IllegalStateException e) {
            ;
        }
        try {
            lO.get();
            Assert.fail();
        }
        catch(final IllegalStateException e) {
            ;
        }
        lO.release();
        Assert.assertEquals(((Integer)lO.get()).intValue(), 5);
        lO.release();
        try {
            lO.release();
            Assert.fail();
        }
        catch(final IllegalStateException e) {
            ;
        }
        try {
            lO.release();
            Assert.fail();
        }
        catch(final IllegalStateException e) {
            ;
        }
    }
}
