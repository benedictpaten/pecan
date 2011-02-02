/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Mar 21, 2005
 */
package bp.common.ds;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author benedictpaten
 */
public class AllTests {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite("Test for bp.common.ds");
        //$JUnit-BEGIN$
        suite.addTestSuite(IntStackTest.class);
        suite.addTestSuite(ScrollingQueueTest.class);
        suite.addTestSuite(LockedObjectTest.class);
        suite.addTestSuite(ArrayTest.class);
        suite.addTestSuite(ScrollingQueue_ToolsTest.class);
        suite.addTestSuite(ScrollingQueue_IntTest.class);
        suite.addTestSuite(ScrollingQueue_IntToolsTest.class);
        //$JUnit-END$
        return suite;
    }
}