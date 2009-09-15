/*
 * Created on Feb 3, 2005
 */
package bp.common.fp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class HOIteratorsTest
                            extends TestCase {

    /**
     *  
     */
    public HOIteratorsTest(final String s) {
        super(s);
    }

    public void testZipWithObjects() {
        final Iterator it = HOIterators.zipWithObject(Arrays.asList(
                new Object[] { new Integer(0), new Integer(1) }).iterator(),
                "1", Object.class);
        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(Arrays.equals(new Object[] { "1", new Integer(0) },
                (Object[]) it.next()));
        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(Arrays.equals(new Object[] { "1", new Integer(1) },
                (Object[]) it.next()));
        Assert.assertFalse(it.hasNext());
    }

    public void testAllAgainstAll_ASymmetric_NoDiagonals() {
        final Iterator it = HOIterators.allAgainstAll_ASymmetric_NoDiagonals(Arrays
                .asList(new Object[] { "1", "2", "3" }), Object.class);
        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(Arrays.equals(new Object[] { "1", "2" }, (Object[]) it
                .next()));
        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(Arrays.equals(new Object[] { "1", "3" }, (Object[]) it
                .next()));
        Assert.assertTrue(Arrays.equals(new Object[] { "2", "3" }, (Object[]) it
                .next()));
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public void testChainIterables() {
        final Iterator it2 = HOIterators.chainIterables(Arrays.asList(
                new Object[] { Arrays.asList(new Object[] { "one", "two" }),
                        Arrays.asList(new Object[] { "three", "four" }) })
                .iterator());

        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "one");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "two");
        Assert.assertEquals(it2.next(), "three");
        Assert.assertEquals(it2.next(), "four");
        Assert.assertFalse(it2.hasNext());
        try {
            it2.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }
}