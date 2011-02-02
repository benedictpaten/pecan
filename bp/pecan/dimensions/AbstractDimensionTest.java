/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 11, 2005
 */
package bp.pecan.dimensions;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.logging.Logger;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function_2Args;
import bp.common.fp.Functions;
import bp.common.fp.Functions_2Args;

/**
 * @author benedictpaten
 */
public abstract class AbstractDimensionTest
                                           extends TestCase {

    Logger logger = Logger.getLogger("");

    /**
     * Constructor for AbstractDimensionTest.
     * 
     * @param arg0
     */
    public AbstractDimensionTest(final String arg0) {
        super(arg0);
    }

    /**
     * This is a method designed to stress a dimension implementation.
     *  
     */
    public void testComplete(final Dimension d, final Function_2Args fn, final int size,
            final int subDimensions) {
        this.testSize(d, size);
        this.testSubDimensionsNumber(d, subDimensions);
        this.coreTests(d, fn);
        this.testGetReversedSlice(d, fn);
        this.testGetSlice(d, fn);
        this.testGetSubDimensionSlice(d, fn);
    }

    public void coreTests(final Dimension d, final Function_2Args fn) {
        this.testGet(d, fn);
        this.testGetArray(d, fn);
        this.testIterator(d, fn);
        this.testGet(d, fn);
    }

    Function_2Args getFunction() {
        return Functions_2Args.rPipe(Functions_2Args.multiply(), Functions
                .lCurry(Functions_2Args.sum(), new Double(1)));
    }
    
    abstract Dimension getDimension();
    
    abstract int length();
    
    abstract int subDimensionsNumber();

    /*
     * @see TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGet() {
        this.testGet(this.getDimension(), this.getFunction());
    }

    public void testGet(final Dimension d, final Function_2Args fn) {
        for (int i = 0; i < d.size(); i++) {
            for (int j = 0; j < d.subDimensionsNumber(); j++) {
                final int k = d.get(i, j);
                Assert.assertEquals(new Double(k), fn.fn(new Double(i), new Double(j)));
            }
        }
        int[] iA = new int[] { -1, d.size() };
        for (final int element : iA) {
            try {
                d.get(element, 0);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
        }
        iA = new int[] { -1, d.subDimensionsNumber() };
        for (final int element : iA) {
            try {
                d.get(0, element);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
        }
    }
    
    public void testGetArray() {
        this.testGetArray(this.getDimension(), this.getFunction());
    }

    public void testGetArray(final Dimension d, final Function_2Args fn) {
        final int[] iA = new int[d.subDimensionsNumber()];
        for (int i = 0; i < d.size(); i++) {
            final int j = d.get(i, 0, iA);
            Assert.assertEquals(j, d.subDimensionsNumber());
            for (int k = 0; k < d.subDimensionsNumber(); k++) {
                Assert.assertEquals(new Double(iA[k]), fn.fn(new Double(i),
                        new Double(k)));
            }
        }
        final int[] iA2 = new int[] { -1, d.size() };
        for (final int element : iA2) {
            try {
                d.get(element, 0, iA);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
        }
    }
    
    public void testGetReversedSlice() {
        this.testGetReversedSlice(this.getDimension(), this.getFunction());
    }

    public void testGetReversedSlice(Dimension d, Function_2Args fn) {
        d = d.getReversedSlice();
        fn = Functions_2Args.lPipe(fn, Functions.lCurry(Functions_2Args
                .subtract(), new Double(d.size() - 1)));
        this.coreTests(d, fn);
    }
    
    public void testGetSlice() {
        this.testGetSlice(this.getDimension(), this.getFunction());
    }

    public void testGetSlice(final Dimension d, final Function_2Args fn) {
        try { //check nonsense start point
            d.getSlice(-1, 1);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try { //check nonsense length
            d.getSlice(0, -1);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try { //check out of bounds
            d.getSlice(0, d.size() + 1);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try { //check dual out of bounds and nonsense start
            d.getSlice(-1, -1);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Dimension d2 = d.getSlice(d.size(), 0);
        Assert.assertEquals(d2.size(), 0);
        for (int i = 0; i < 100; i++) {
            final int start = d.size() > 0 ? new Random().nextInt(d.size()) : 0;
            final int length = d.size() > 0 ? new Random().nextInt(d.size() - start) : 0;
            d2 = d.getSlice(start, length);
            final Function_2Args fn2 = Functions_2Args.lPipe(fn, Functions.lCurry(
                    Functions_2Args.sum(), new Double(start)));
            this.coreTests(d2, fn2);
        }
    }
    
    public void testGetSubDimensionSlice() {
        this.testGetSubDimensionSlice(this.getDimension(), this.getFunction());
    }

    public void testGetSubDimensionSlice(final Dimension d, final Function_2Args fn) {
        try {
            d.getSubDimensionSlice(new int[0]);
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
        try {
            d.getSubDimensionSlice(new int[] { -1 });
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
        try {
            final int[] iA = new int[d.subDimensionsNumber() + 1];
            for (int i = 0; i < iA.length; i++) {
				iA[i] = iA[i];
			}
            d.getSubDimensionSlice(iA);
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
        final int[] iA = new int[d.subDimensionsNumber()];
        for (int i = 0; i < iA.length; i++) {
            iA[i] = i;
        }
        this.coreTests(d.getSubDimensionSlice(iA), fn);
    }
    
    public void testIterator() {
        this.testIterator(this.getDimension(), this.getFunction());
    }

    public void testIterator(final Dimension d, final Function_2Args fn) {
        final Iterator it = d.iterator();
        int i = 0;
        while (it.hasNext()) {
            final int[] iA2 = (int[]) it.next();
            for (int j = 0; j < iA2.length; j++) {
                Assert.assertEquals(new Double(iA2[j]), fn.fn(new Double(i),
                        new Double(j)));
            }
            i++;
        }
        Assert.assertEquals(i, d.size());
        try {
            it.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }
    
    public void testSize() {
        this.testSize(this.getDimension(), this.length());
    }

    public void testSize(final Dimension d, final int size) {
        Assert.assertEquals(d.size(), size);
    }
    
    public void testSubDimensionsNumber() {
       this.testSubDimensionsNumber(this.getDimension(), this.subDimensionsNumber());
    }

    public void testSubDimensionsNumber(final Dimension d, final int subDimensionsNumber) {
        Assert.assertEquals(d.subDimensionsNumber(), subDimensionsNumber);
    }

}