/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jan 7, 2005
 */
package bp.common;

import junit.framework.Test;
import junit.framework.TestSuite;
import bp.common.io.ExternalExecutionTest;

/**
 * @author benedictpaten
 */
public class AllTests {

    public static Test suite() {
        final TestSuite suite = new TestSuite("Test for bp.util");
        //$JUnit-BEGIN$
        suite.addTestSuite(ExternalExecutionTest.class);
        //$JUnit-END$
        return suite;
    }
}