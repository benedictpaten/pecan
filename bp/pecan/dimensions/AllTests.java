/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

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
        final TestSuite suite = new TestSuite("Test for bp.pecan.dimensions");
        //$JUnit-BEGIN$
        suite.addTestSuite(SlicedDimensionTest.class);
        suite.addTestSuite(MergedDimensionTest.class);
        suite.addTestSuite(DimensionToolsTest.class);
        suite.addTestSuite(SequenceTest.class);
        suite.addTestSuite(ReversedDimensionTest.class);
        suite.addTestSuite(FunctionalDimensionTest.class);
        suite.addTestSuite(PairedDimensionTest.class);
        suite.addTestSuite(ViewTest.class);
        suite.addTestSuite(AlignmentTest.class);
        //$JUnit-END$
        return suite;
    }
}