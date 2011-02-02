/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jan 6, 2005
 */
package bp.common.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class TestToolsTest
                          extends TestCase {
    static final String s = "This is a test to see how we do",
            s2 = "This is a different test string";

    public TestToolsTest(final String s) {
        super(s);
    }

    public void testTestEquality() {
        try {
            Assert.assertTrue(TestTools.testEquality(new ByteArrayInputStream(TestToolsTest.s
                    .getBytes()), new ByteArrayInputStream(TestToolsTest.s.getBytes())));
            Assert.assertFalse(TestTools.testEquality(new ByteArrayInputStream(TestToolsTest.s
                    .getBytes()), new ByteArrayInputStream(TestToolsTest.s2.getBytes())));
        } catch (final IOException e) {
            Assert.assertTrue(false);
        }
    }

    /**
     * This method is useful for making output for {@link this.testTestCommand}.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        System.out.println(TestToolsTest.s);
        for (final String element : args) {
            System.out.print(element);
        }
    }

}