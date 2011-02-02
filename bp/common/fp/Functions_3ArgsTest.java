/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 3, 2005
 */
package bp.common.fp;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class Functions_3ArgsTest
                                extends TestCase {

    /**
     * Constructor for Functions_3ArgsTest.
     * 
     * @param arg0
     */
    public Functions_3ArgsTest(final String arg0) {
        super(arg0);
    }

    public void testRightArg() {
        final Function_3Args fn = Functions_3Args.rightArg();
        Assert.assertEquals("3", fn.fn("1", "2", "3"));
    }

    public void testMiddleArg() {
        final Function_3Args fn = Functions_3Args.middleArg();
        Assert.assertEquals("2", fn.fn("1", "2", "3"));
    }

    public void testLeftArg() {
        final Function_3Args fn = Functions_3Args.leftArg();
        Assert.assertEquals("1", fn.fn("1", "2", "3"));
    }

    public void testPipe() {
        final Function_3Args fn = Functions_3Args.pipe(Functions.doNothing(),
                Functions_3Args.concatenate());
        Assert.assertEquals("123", fn.fn("1", "2", "3"));
    }
    
    public void testLCurry() {
        final Function_3Args fn = Functions_3Args.lCurry(Functions_4Args.concatenate(), "1");
        Assert.assertEquals("1234", fn.fn("2", "3", "4"));
    }
    
    public void testRCurry() {
        final Function_3Args fn = Functions_3Args.rCurry(Functions_4Args.concatenate(), "4");
        Assert.assertEquals("1234", fn.fn("1", "2", "3"));
    }
    
    public void testFlipArguments() {
        Assert.assertEquals((String)Functions_3Args.flipArguments().fn("2", "1", Functions_2Args.concatenate()), "12");
    }

}