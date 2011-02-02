/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 2, 2005
 */
package bp.common.fp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class Functions_2ArgsTest
                                extends TestCase {

    /**
     * Constructor for Functions_2ArgsTest.
     * 
     * @param arg0
     */
    public Functions_2ArgsTest(final String arg0) {
        super(arg0);
    }

    public void testPredicateWrapper() {
        final Function_2Args fn = Functions_2Args.predicateWrapper();
        Assert.assertEquals(Boolean.TRUE, fn.fn(new Double(6), Predicates.rCurry(
                Predicates_2Args.greaterThan(), new Double(5))));
        Assert.assertEquals(Boolean.FALSE, fn.fn(new Double(4), Predicates.rCurry(
                Predicates_2Args.greaterThan(), new Double(5))));
    }

    public void testAppend() {
        final Function_2Args fn1 = Functions_2Args.append();
        final List l = new ArrayList();
        fn1.fn("1", l);
        Assert.assertTrue(l.size() == 1);
        Assert.assertEquals(l.get(0), "1");
    }

    public void testAppendCollection() {
        final List l = new ArrayList(), l2 = new ArrayList();
        l.add("1");
        l2.add("2");
        Functions_2Args.appendCollection().fn(l2, l);
        Assert.assertTrue(Arrays.equals(l.toArray(), new Object[] { "1", "2" }));
        Assert.assertTrue(Arrays.equals(l2.toArray(), new Object[] { "2" }));
        Functions_2Args.appendCollection().fn(new ArrayList(), l);
        Assert.assertTrue(Arrays.equals(l.toArray(), new Object[] { "1", "2" }));
        final List l3 = new ArrayList();
        Functions_2Args.appendCollection().fn(new ArrayList(), l3);
        Assert.assertTrue(Arrays.equals(l3.toArray(), new Object[] {}));
        Functions_2Args.appendCollection().fn(l, l3);
        Assert.assertTrue(Arrays.equals(l3.toArray(), new Object[] { "1", "2" }));
    }

    public void testConcatenate() {
        final Function_2Args fn1 = Functions_2Args.concatenate();
        Assert.assertEquals(fn1.fn("1", "2"), "12");
    }

    public void testDivide() {
        final Function_2Args fn1 = Functions_2Args.divide();
        Assert.assertEquals(fn1.fn(new Integer(12), new Integer(13)), new Double(
                12.0 / 13));
        Assert.assertEquals(fn1.fn(new Double(1.5), new Double(1.7)), new Double(
                1.5 / 1.7));
    }

    public void testLCurry() {
        final Function_2Args fn1 = Functions_2Args.lCurry(Functions_3Args
                .concatenate(), "1");
        Assert.assertEquals("123", fn1.fn("2", "3"));
    }

    public void testLeftArg() {
        final Function_2Args fn = Functions_2Args.leftArg();
        Assert.assertEquals("1", fn.fn("1", "2"));
    }

    public void testLPipe() {
        final Function_2Args fn = Functions_2Args.lPipe(Functions_2Args.sum(),
                Functions.lCurry(Functions_2Args.multiply(), new Double(5)));
        Assert.assertEquals(fn.fn(new Double(5), new Double(2)), new Double(
                (5 * 5.0) + 2.0));
    }

    public void testMap() {
        final Map m = new HashMap();
        m.put("1", "2");
        final Function_2Args fn1 = Functions_2Args.map();
        Assert.assertEquals(fn1.fn("1", m), "2");
    }

    public void testMax() {
        final Function_2Args fn1 = Functions_2Args.max();
        Assert.assertEquals(fn1.fn(new Integer(1), new Integer(10)), new Integer(10));
        Assert.assertEquals(fn1.fn(new Integer(10), new Integer(1)), new Integer(10));
    }

    public void testMin() {
        final Function_2Args fn1 = Functions_2Args.min();
        Assert.assertEquals(fn1.fn(new Integer(1), new Integer(10)), new Integer(1));
        Assert.assertEquals(fn1.fn(new Integer(10), new Integer(1)), new Integer(1));
    }

    public void testMultiply() {
        final Function_2Args fn1 = Functions_2Args.multiply();
        Assert.assertEquals(fn1.fn(new Integer(12), new Integer(13)), new Double(
                12 * 13));
        Assert.assertEquals(fn1.fn(new Double(1.5), new Double(1.7)), new Double(
                1.5 * 1.7));
    }

    public void testPipe() {
        final Function_2Args fn1 = Functions_2Args.pipe(Functions.lCurry(
                Functions_2Args.multiply(), new Integer(5)), Functions_2Args
                .sum());
        Assert.assertEquals(fn1.fn(new Integer(5), new Integer(5)), new Double(50));
    }

    public void testRCurry() {
        final Function_2Args fn1 = Functions_2Args.rCurry(Functions_3Args
                .concatenate(), "3");
        Assert.assertEquals("123", fn1.fn("1", "2"));
    }

    public void testRightArg() {
        final Function_2Args fn = Functions_2Args.rightArg();
        Assert.assertEquals("2", fn.fn("1", "2"));
    }

    public void testRPipe() {
        final Function_2Args fn = Functions_2Args.rPipe(Functions_2Args.sum(),
                Functions.lCurry(Functions_2Args.multiply(), new Double(5)));
        Assert.assertEquals(fn.fn(new Double(5), new Double(2)), new Double(
                (2 * 5.0) + 5.0));
    }

    public void testSubtract() {
        final Function_2Args fn1 = Functions_2Args.subtract();
        Assert.assertEquals(fn1.fn(new Integer(12), new Integer(13)), new Double(
                12 - 13));
        Assert.assertEquals(fn1.fn(new Double(1.5), new Double(1.7)), new Double(
                1.5 - 1.7));
    }

    public void testSum() {
        final Function_2Args fn1 = Functions_2Args.sum();
        Assert.assertEquals(fn1.fn(new Integer(12), new Integer(13)), new Double(
                12 + 13));
        Assert.assertEquals(fn1.fn(new Double(1.5), new Double(1.7)), new Double(
                1.5 + 1.7));
    }

    public void testConcatenateIntArrays() {
        final int[] iA = new int[] { 1, 2 }, iA2 = new int[] { 3, 4 };
        int[] iA4 = (int[]) Functions_2Args.concatenateIntArrays().fn(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA4, new int[] { 1, 2, 3, 4 }));
        Assert.assertTrue(Arrays.equals(iA, new int[] { 1, 2 }));
        Assert.assertTrue(Arrays.equals(iA2, new int[] { 3, 4 }));
        iA4 = (int[]) Functions_2Args.concatenateIntArrays().fn(new int[] {},
                iA4);
        Assert.assertTrue(Arrays.equals(iA4, new int[] { 1, 2, 3, 4 }));
        iA4 = (int[]) Functions_2Args.concatenateIntArrays().fn(iA4,
                new int[] {});
        Assert.assertTrue(Arrays.equals(iA4, new int[] { 1, 2, 3, 4 }));
        iA4 = (int[]) Functions_2Args.concatenateIntArrays().fn(new int[] {},
                new int[] {});
        Assert.assertTrue(Arrays.equals(iA4, new int[] {}));
    }

    public void testConcatenateObjectArrays() {
        final Object[] iA = new Object[] { "1", "2" }, iA2 = new Object[] { "3", "4" };
        Object[] iA4 = (Object[]) Functions_2Args.concatenateArrays().fn(iA,
                iA2);
        Assert.assertTrue(Arrays.equals(iA4, new Object[] { "1", "2", "3", "4" }));
        Assert.assertTrue(Arrays.equals(iA, new Object[] { "1", "2" }));
        Assert.assertTrue(Arrays.equals(iA2, new Object[] { "3", "4" }));
        iA4 = (Object[]) Functions_2Args.concatenateArrays().fn(
                new Object[] {}, iA4);
        Assert.assertTrue(Arrays.equals(iA4, new Object[] { "1", "2", "3", "4" }));
        iA4 = (Object[]) Functions_2Args.concatenateArrays().fn(iA4,
                new Object[] {});
        Assert.assertTrue(Arrays.equals(iA4, new Object[] { "1", "2", "3", "4" }));
        iA4 = (Object[]) Functions_2Args.concatenateArrays().fn(
                new Object[] {}, new Object[] {});
        Assert.assertTrue(Arrays.equals(iA4, new Object[] {}));
    }

    public void testSumLogs() {
        final Function_2Args sumLogs = Functions_2Args.sumLogs();
        for (int trial = 0; trial < 1000; trial++) {
            final double d = Math.random() * 100;
            final double d2 = d - 20 * Math.random();
            double d3 = d, d4 = d2;
            if (Math.random() > 0.5) {
                d3 = d2;
                d4 = d;
            }
            Assert.assertEquals(((Double) sumLogs.fn(new Double(d3), new Double(d4)))
                    .doubleValue(), d + Math.log(1 + Math.exp(d2 - d)), 0);
        }
    }
    
    public void testMatrix2DLookup() {
        final Function_2Args fn = Functions_2Args.matrix2DLookup(new double[] { 1, 2, 3, 4 }, 2);
        Assert.assertEquals(((Number)fn.fn(new Integer(0), new Integer(0))).doubleValue(), 1, 0);
        Assert.assertEquals(((Number)fn.fn(new Integer(1), new Integer(0))).doubleValue(), 2, 0);
        Assert.assertEquals(((Number)fn.fn(new Integer(0), new Integer(1))).doubleValue(), 3, 0);
        Assert.assertEquals(((Number)fn.fn(new Integer(1), new Integer(1))).doubleValue(), 4, 0);
    }
}