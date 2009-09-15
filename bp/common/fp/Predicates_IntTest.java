/*
 * Created on Sep 19, 2005
 */
package bp.common.fp;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class Predicates_IntTest
                               extends TestCase {
    public void testLCurry() {
        final Predicate_Int test = Predicates_Int.lCurry(new Predicate_Int_2Args() {
            /* (non-Javadoc)
             * @see bp.common.fp.Predicate_Int_2Args#test(int, int)
             */
            public boolean test(int i, int j) {
                return i > j;
            }
        }, 10);
        Assert.assertTrue(test.test(8));
        Assert.assertTrue(test.test(9));
        Assert.assertFalse(test.test(10));
        Assert.assertFalse(test.test(11));
    }
    
    public void testRCurry() {
        final Predicate_Int test = Predicates_Int.rCurry(new Predicate_Int_2Args() {
            /* (non-Javadoc)
             * @see bp.common.fp.Predicate_Int_2Args#test(int, int)
             */
            public boolean test(int i, int j) {
                return i > j;
            }
        }, 10);
        Assert.assertTrue(test.test(11));
        Assert.assertTrue(test.test(12));
        Assert.assertFalse(test.test(10));
        Assert.assertFalse(test.test(9));
    }
}
