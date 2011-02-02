/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function_Int_2Args;

/**
 * @author benedictpaten
 */
public class ViewTest
                     extends TestCase {
    View v;

    public ViewTest(final String arg) {
        super(arg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        this.v = new View(new Sequence(new int[] { 0, 1, 2, 3, 4, 5 }),
                new int[] { 0 });
    }

    public void testGet() {
        for (int i = 0; i < this.v.size(); i++) {
            Assert.assertEquals(this.v.get(i, 0), i);
        }
        final Function_Int_2Args fn = this.v.get();
        for (int i = 0; i < this.v.size(); i++) {
            Assert.assertEquals(fn.fn(i, 0), i);
        }
    }

    public void testGetArray() {
        final int[] iA = new int[1];
        for (int i = 0; i < this.v.size(); i++) {
            Assert.assertEquals(this.v.get(i, 0, iA), 1);
            Assert.assertEquals(iA[0], i);
        }
    }
    
    public void testIterator() {
        final Iterator it = this.v.iterator();
        for(int i=0; i<this.v.size(); i++) {
            Assert.assertTrue(it.hasNext());
            Assert.assertTrue(it.hasNext());
            Assert.assertEquals(((int[])it.next())[0], i);
        }
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        }
        catch(final NoSuchElementException e) {
            ;
        }
    }
    
    public void testSize() {
        Assert.assertEquals(this.v.size(), 6);
    }
    
    public void testSubDimensionsNumber() {
        Assert.assertEquals(this.v.subDimensionsNumber(), 1);
    }
}