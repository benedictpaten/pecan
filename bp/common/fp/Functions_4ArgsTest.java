/*
 * Created on Feb 7, 2005
 */
package bp.common.fp;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class Functions_4ArgsTest
                                extends TestCase {
    public void testConcatenate() {
        final Function_4Args fn = Functions_4Args.concatenate();
        Assert.assertEquals(fn.fn("1", "2", "3", "4"), "1234");
    }
}
