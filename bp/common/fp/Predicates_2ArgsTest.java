/*
 * Created on Feb 2, 2005
 */
package bp.common.fp;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class Predicates_2ArgsTest
                                 extends TestCase {

    /**
     * Constructor for Predicates_2ArgsTest.
     * @param arg0
     */
    public Predicates_2ArgsTest(final String arg0) {
        super(arg0);
    }
    
    public void testAlwaysTrue() {
        final Predicate_2Args bP = Predicates_2Args.alwaysTrue();
        Assert.assertTrue(bP.test(null, null));
        Assert.assertTrue(bP.test(null, null));
    }
    
    public void testContainedIn() {
        final Predicate_2Args bP = Predicates_2Args.containedIn();
        final Collection c = new HashSet();
        c.add("1");
        Assert.assertTrue(bP.test("1", c));
        Assert.assertFalse(bP.test("2", c));
    }
    
    public void testEquals() {
        final Predicate_2Args bP = Predicates_2Args.equal();
        Assert.assertTrue(bP.test(null, null));
        Assert.assertTrue(bP.test("1", "1"));
        Assert.assertFalse(bP.test("1", null));
        Assert.assertFalse(bP.test(null, "1"));
        Assert.assertFalse(bP.test("1", "2"));
    }
    
    public void testGreaterThan() {
        final Predicate_2Args bP = Predicates_2Args.greaterThan();
        Assert.assertTrue(bP.test(new Double(5), new Double(4)));
        Assert.assertFalse(bP.test(new Double(4), new Double(5)));
        Assert.assertFalse(bP.test(new Double(4), new Double(4)));
        try {
            bP.test(new Double(4), "");
            Assert.fail();
        }
        catch(final ClassCastException e) {
            ;
        }
    }
    
    public void testGreaterThanOrEqual() {
        final Predicate_2Args bP = Predicates_2Args.greaterThanOrEqual();
        Assert.assertTrue(bP.test(new Double(5), new Double(4)));
        Assert.assertFalse(bP.test(new Double(4), new Double(5)));
        Assert.assertTrue(bP.test(new Double(4), new Double(4)));
        try {
            bP.test(new Double(4), "");
            Assert.fail();
        }
        catch(final ClassCastException e) {
            ;
        }
    }
    
    public void testLCurry() {
        final Predicate_2Args bP = Predicates_2Args.lCurry(new Predicate_3Args() {
            /* (non-Javadoc)
             * @see bp.common.fp.Predicate_3Args#test(java.lang.Object, java.lang.Object, java.lang.Object)
             */
            public boolean test(Object o, Object o2, Object o3) {
                return o.equals(Boolean.TRUE) && o2.equals(Boolean.TRUE) && o3.equals(Boolean.FALSE);
            }
        }, Boolean.TRUE);
        Assert.assertTrue(bP.test(Boolean.TRUE, Boolean.FALSE));
        Assert.assertFalse(bP.test(Boolean.FALSE, Boolean.TRUE));
    }
    
    public void testLessThan() {
        final Predicate_2Args bP = Predicates_2Args.lessThan();
        Assert.assertFalse(bP.test(new Double(5), new Double(4)));
        Assert.assertTrue(bP.test(new Double(4), new Double(5)));
        Assert.assertFalse(bP.test(new Double(4), new Double(4)));
        try {
            bP.test(new Double(4), "");
            Assert.fail();
        }
        catch(final ClassCastException e) {
            ;
        }
    }
    
    public void testLessThanOrEquals() {
        final Predicate_2Args bP = Predicates_2Args.lessThanOrEqual();
        Assert.assertFalse(bP.test(new Double(5), new Double(4)));
        Assert.assertTrue(bP.test(new Double(4), new Double(5)));
        Assert.assertTrue(bP.test(new Double(4), new Double(4)));
        try {
            bP.test(new Double(4), "");
            Assert.fail();
        }
        catch(final ClassCastException e) {
            ;
        }
    }
    
    public void testNotEquals() {
        final Predicate_2Args bP = Predicates_2Args.notEqual();
        Assert.assertFalse(bP.test(null, null));
        Assert.assertFalse(bP.test("1", "1"));
        Assert.assertTrue(bP.test("1", null));
        Assert.assertTrue(bP.test(null, "1"));
        Assert.assertTrue(bP.test("1", "2"));
    }
    
    public void testPipe() {
        final Predicate_2Args bP = Predicates_2Args.pipe(Predicates.sumIsGreaterThan(5), Functions_2Args.max());
        Assert.assertTrue(bP.test(new Integer(6), new Integer(2)));
    }
    
    public void testRCurry() {
        final Predicate_2Args bP = Predicates_2Args.rCurry(new Predicate_3Args() {
            /* (non-Javadoc)
             * @see bp.common.fp.Predicate_3Args#test(java.lang.Object, java.lang.Object, java.lang.Object)
             */
            public boolean test(Object o, Object o2, Object o3) {
                return o.equals(Boolean.TRUE) && o2.equals(Boolean.FALSE) && o3.equals(Boolean.TRUE);
            }
        }, Boolean.TRUE);
        Assert.assertTrue(bP.test(Boolean.TRUE, Boolean.FALSE));
        Assert.assertFalse(bP.test(Boolean.FALSE, Boolean.TRUE));
    }

    public void testPipe_Map() {
        final Predicate_2Args bP = Predicates_2Args.pipe(Predicates_2Args.greaterThan(), Functions.parseNumber());
        Assert.assertFalse(bP.test("1", "2"));
        Assert.assertTrue(bP.test("2", "1"));
    }
    
    public void testArraysEqual() {
        final Predicate_2Args bP = Predicates_2Args.arraysEqual();
        Assert.assertTrue(bP.test(new Object[0], new Object[0]));
        Assert.assertTrue(bP.test(null, null));
        Assert.assertTrue(bP.test(new Object[] { new Integer(0) }, new Object[] { new Integer(0) }));
        Assert.assertFalse(bP.test(null, new Object[0]));
        Assert.assertFalse(bP.test(new Object[0], null));
        Assert.assertFalse(bP.test(new Object[] { new Integer(0), new Integer(1) }, new Object[] { new Integer(0) }));
        Assert.assertFalse(bP.test(new Object[] { new Integer(1) }, new Object[] { new Integer(0) }));
       
    }
}
