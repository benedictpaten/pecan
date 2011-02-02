/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 16, 2005
 */
package bp.common.fp;

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class GeneratorIteratorTest
                                  extends TestCase {
    Iterator it;

    public GeneratorIteratorTest(final String s) {
        super(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        this.it = new GeneratorIterator(new Generator() {
            int i = 0;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (this.i < 10) {
					return new Integer(this.i++);
				} else {
					return null;
				}
            }
        });
    }

    public void testAll() {
        Assert.assertTrue(this.it.hasNext());
        for (int i = 0; i < 10; i++) {
            if (Math.random() > 0.5) {
				Assert.assertTrue(this.it.hasNext());
			}
            Assert.assertEquals(this.it.next(), new Integer(i));
        }
        Assert.assertFalse(this.it.hasNext());
        try {
            this.it.next();
            Assert.fail();
        }
        catch(final NoSuchElementException e) {
            ;
        }
        try {
            this.it.remove();
            Assert.fail();
        }
        catch(final UnsupportedOperationException e) {
            ;
        }
    }
}