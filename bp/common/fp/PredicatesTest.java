/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class PredicatesTest
                           extends TestCase {

    /**
     * Constructor for PredicateToolsTest.
     * 
     * @param arg0
     */
    public PredicatesTest(final String arg0) {
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

    public void testAnd() {
        Assert.assertFalse(Predicates.and(Predicates.alwaysTrue(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertFalse(Predicates.and(Predicates.alwaysFalse(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertFalse(Predicates.and(Predicates.alwaysFalse(),
                Predicates.alwaysTrue()).test(null));
        Assert.assertTrue(Predicates.and(Predicates.alwaysTrue(),
                Predicates.alwaysTrue()).test(null));
    }

    public void testConstant() {
        Assert.assertTrue(Predicates.alwaysTrue().test(null));
        Assert.assertFalse(Predicates.alwaysFalse().test(null));
    }

    public void testInverse() {
        final Predicate p = Predicates.inverse(new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate#test(java.lang.Object)
             */
            public boolean test(Object o) {
                return o == null;
            }
        });
        Assert.assertTrue(p.test(""));
        Assert.assertFalse(p.test(null));
    }

    public void testOr() {
        Assert.assertTrue(Predicates.or(Predicates.alwaysTrue(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertFalse(Predicates.or(Predicates.alwaysFalse(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertTrue(Predicates.or(Predicates.alwaysFalse(),
                Predicates.alwaysTrue()).test(null));
        Assert.assertTrue(Predicates.or(Predicates.alwaysTrue(),
                Predicates.alwaysTrue()).test(null));
    }

    public void testSeen() {
        final Predicate p = Predicates.seen();
        Assert.assertFalse(p.test("1"));
        Assert.assertFalse(p.test("2"));
        Assert.assertTrue(p.test("1"));
        Assert.assertTrue(p.test("2"));
    }

    public void testTrueXTimes() {
        final Predicate bP = Predicates.trueXTimes(2);
        Assert.assertTrue(bP.test(null));
        Assert.assertTrue(bP.test(null));
        Assert.assertFalse(bP.test(null));
        Assert.assertFalse(bP.test(null));
    }

    public void testXOr() {
        Assert.assertTrue(Predicates.xOr(Predicates.alwaysTrue(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertFalse(Predicates.xOr(Predicates.alwaysFalse(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertTrue(Predicates.xOr(Predicates.alwaysFalse(),
                Predicates.alwaysTrue()).test(null));
        Assert.assertFalse(Predicates.xOr(Predicates.alwaysTrue(),
                Predicates.alwaysTrue()).test(null));
    }

    public void testNAnd() {
        Assert.assertFalse(Predicates.nAnd(Predicates.alwaysTrue(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertTrue(Predicates.nAnd(Predicates.alwaysFalse(),
                Predicates.alwaysFalse()).test(null));
        Assert.assertFalse(Predicates.nAnd(Predicates.alwaysFalse(),
                Predicates.alwaysTrue()).test(null));
        Assert.assertFalse(Predicates.nAnd(Predicates.alwaysTrue(),
                Predicates.alwaysTrue()).test(null));
    }

    public void testSumIsGreaterThan() {
        final Predicate bP = Predicates.sumIsGreaterThan(10);
        final Iterator it = Iterators.filter(Arrays
                .asList(
                        new Object[] { new Integer(10), new Integer(1),
                                new Integer(-5) }).iterator(), bP);
        Assert.assertEquals(it.next(), new Integer(1));
        Assert.assertFalse(it.hasNext());
    }

    public void testSumIsLessThan() {
        final Predicate bP = Predicates.sumIsLessThan(10);
        final Iterator it = Iterators.filter(Arrays
                .asList(
                        new Object[] { new Integer(10), new Integer(1),
                                new Integer(-5) }).iterator(), bP);
        Assert.assertEquals(it.next(), new Integer(-5));
        Assert.assertFalse(it.hasNext());
    }

    public void testFilter() {
        Assert.assertTrue(Predicates.pipe(Predicates.lCurry(Predicates_2Args.equal(), new Double("1")),
                Functions.parseNumber()).test("1"));
    }

    public void testWindow() {
        final Predicate bP = Predicates.window(Predicates_2Args.equal(), null);
        Assert.assertTrue(bP.test(null));
        Assert.assertFalse(bP.test("1"));
        Assert.assertTrue(bP.test("1"));
    }

    public void testLCurry() {
        final Predicate bP = Predicates.lCurry(Predicates_2Args.greaterThan(),
                new Double(5));
        Assert.assertTrue(bP.test(new Double(4)));
        Assert.assertFalse(bP.test(new Double(5)));
        Assert.assertFalse(bP.test(new Double(6)));
    }

    public void testRCurry() {
        final Predicate bP = Predicates.rCurry(Predicates_2Args.greaterThan(),
                new Double(5));
        Assert.assertFalse(bP.test(new Double(4)));
        Assert.assertFalse(bP.test(new Double(5)));
        Assert.assertTrue(bP.test(new Double(6)));
    }
    
    public void testUnpack() {
        final Predicate bP = Predicates.unpack(Predicates_2Args.greaterThan());
        Assert.assertTrue(bP.test(new Object[] { new Double(0), new Double(-1) }));
        Assert.assertFalse(bP.test(new Object[] { new Double(-1), new Double(0) }));
    }
    
    public void testIndexInArray() {
        Predicate bP = Predicates.indexInArray(new int[] { 2, 3, 6, 8 }, 4);
        Assert.assertTrue(Arrays.equals(IterationTools.append(Iterators.filter(Arrays.asList(new Object[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }).
                iterator(), bP), new LinkedList()).toArray(), new Object[] { "2", "3", "6", "8" }));
        bP = Predicates.indexInArray(new int[] { 0, 2, 3, 6, 8, 0, 10 }, 4);
        Assert.assertTrue(Arrays.equals(IterationTools.append(Iterators.filter(Arrays.asList(new Object[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }).
                iterator(), bP), new LinkedList()).toArray(), new Object[] { "0", "2", "3", "6" }));
        bP = Predicates.indexInArray(new int[] { 0, 2, 3, 6, 8, 0, 10 }, 0);
        Assert.assertTrue(Arrays.equals(IterationTools.append(Iterators.filter(Arrays.asList(new Object[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }).
                iterator(), bP), new LinkedList()).toArray(), new Object[] { }));
        bP = Predicates.indexInArray(new int[] { 0, 2, 3, 7, 0, 10 }, 4);
        Assert.assertTrue(Arrays.equals(IterationTools.append(Iterators.filter(Arrays.asList(new Object[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }).
                iterator(), bP), new LinkedList()).toArray(), new Object[] { "0", "2", "3", "7" }));
    }

}