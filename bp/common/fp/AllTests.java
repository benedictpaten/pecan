/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 5, 2005
 */
package bp.common.fp;

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
        final TestSuite suite = new TestSuite("Test for bp.common.fp");
        //$JUnit-BEGIN$
        suite.addTestSuite(HOIteratorsTest.class);
        suite.addTestSuite(FunctionsTest.class);
        suite.addTestSuite(Predicates_2ArgsTest.class);
        suite.addTestSuite(IterationToolsTest.class);
        suite.addTestSuite(Functions_2ArgsTest.class);
        suite.addTestSuite(PredicatesTest.class);
        suite.addTestSuite(ListQueueIteratorTest.class);
        suite.addTestSuite(Functions_3ArgsTest.class);
        suite.addTestSuite(IteratorsTest.class);
        suite.addTestSuite(Functions_4ArgsTest.class);
        //$JUnit-END$
        return suite;
    }
}