/*
 * Created on May 21, 2005
 */
package bp.common.ds;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class IntStackTest
                         extends TestCase {
    public void testIntStack() {
        final IntStack intStack = new IntStack((int) (Math.random() * 100));
        try {
            intStack.unstuff();
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Assert.assertEquals(intStack.gen(), Integer.MAX_VALUE);
        for (int k = 0; k < 10; k++) {
            for (int j = 0; j < 50; j++) {
                int i = 0;
                for (; Math.random() > 0.001; i++) {
					intStack.pro(i);
				}
                if(Math.random() > 0.5) {
                    Assert.assertEquals(intStack.getMark(), i);
                }
                Assert.assertFalse((i > 0) && intStack.empty());
                if(Math.random() > 0.5) {
                    i = (int)(Math.random()*i);
                    intStack.reset(i);
                }
                for (; --i >= 0;) {
					Assert.assertEquals(intStack.gen(), i);
				}
                Assert.assertTrue(intStack.empty());
                Assert.assertEquals(intStack.gen(), Integer.MAX_VALUE);
                while (Math.random() > 0.5) {
                    try {
                        intStack.unstuff();
                        Assert.fail();
                    } catch (final ArrayIndexOutOfBoundsException e) {
                        ;
                    }
                }
            }
            for (int i = 0; Math.random() > 0.001; i++) {
				intStack.pro(i);
			}
            intStack.reset();
            Assert.assertTrue(intStack.empty());
            if(Math.random() > 0.5) {
				Assert.assertEquals(intStack.gen(), Integer.MAX_VALUE);
			}
            try {
                intStack.unstuff();
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
        }
    }
}