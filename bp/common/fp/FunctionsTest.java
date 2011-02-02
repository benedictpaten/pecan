/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class FunctionsTest
                          extends TestCase {

    /**
     * Constructor for FunctionsTest.
     * 
     * @param arg0
     */
    public FunctionsTest(final String arg0) {
        super(arg0);
    }

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

    public void testConstant() {
        final Function fn1 = Functions.constant("1");
        Assert.assertEquals(fn1.fn(null), "1");
    }

    public void testLCurry() {
        final List list = new ArrayList();
        list.add("1");
        final Function fn1 = Functions.lCurry(Functions_2Args.append(), "2");
        Assert.assertEquals(fn1.fn(list), Arrays.asList(new Object[] { "1", "2" }));
    }

    public void testPipe() {
        final Function fn1 = new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(Object o) {
                return new Integer(((Integer) o).intValue() * 10);
            }
        }, fn2 = new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(Object o) {
                return new Integer(((Integer) o).intValue() + 11);
            }
        }, fn3 = Functions.pipe(fn2, fn1);
        Assert.assertEquals(fn3.fn(new Integer(5)), new Integer((5 * 10) + 11));
    }

    public void testRCurry() {
        final List list = new ArrayList();
        list.add("1");
        final Function fn1 = Functions.rCurry(Functions_2Args.append(), list);
        Assert.assertEquals(fn1.fn("2"), Arrays.asList(new Object[] { "1", "2" }));
    }

    public void testUnPack() {
        Function fn1 = Functions.unpack(Functions_2Args.concatenate());
        Assert.assertEquals(fn1.fn(new Object[] { "1", "2" }), "12");
        fn1 = Functions.unpack(Functions_3Args.concatenate());
        Assert.assertEquals(fn1.fn(new Object[] { "1", "2", "3" }), "123");
    }

    public void testAbs() {
        Assert.assertEquals(Functions.abs().fn(new Double(-5)), new Double(5));
    }

    public void testGetNext() {
        Assert.assertEquals(Functions.next().fn(
                Arrays.asList(new Object[] { "a" }).iterator()), "a");
    }

    public void testPack() {
        Assert.assertTrue(Arrays.equals((Object[]) Functions.pack().fn("1"),
                new Object[] { "1" }));
        final Object[] oA = new Object[] { "1" };
        Assert.assertTrue(Arrays.equals((Object[]) Functions.pack().fn(oA),
                new Object[] { oA }));
        Assert.assertTrue(Arrays.equals(oA, new Object[] { "1" }));
        Assert.assertTrue(Arrays.equals((Object[]) Functions.pack().fn(null),
                new Object[] { null }));
    }

    public void testMatrix1DLookUp() {
        final Function fn = Functions.matrix1DLookUp(new double[] { 0, 3, 4, 2 });
        Assert.assertEquals(((Double) fn.fn(new Integer(0))).doubleValue(), 0, 0);
        Assert.assertEquals(((Double) fn.fn(new Integer(1))).doubleValue(), 3, 0);
        Assert.assertEquals(((Double) fn.fn(new Integer(2))).doubleValue(), 4, 0);
        Assert.assertEquals(((Double) fn.fn(new Integer(3))).doubleValue(), 2, 0);
    }
    
    public void testChunkLine() {
        final Function fn = Functions.chunkLine(Pattern.compile(" "));
        Arrays.equals(new String[] { "hello", "big", "cat" }, (String[])fn.fn("hello big cat"));
        Arrays.equals(new String[] { "hello" }, (String[])fn.fn("hello"));
    }

}