/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Sep 16, 2005
 */
package bp.pecan;

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
        final TestSuite suite = new TestSuite("Test for bp.pecan.dpc");
        //$JUnit-BEGIN$
        suite.addTestSuite(PolygonFillerTest.class);
        suite.addTestSuite(Pecan_ToolsTest.class);
        suite.addTestSuite(AlignmentStitcherTest.class);
        suite.addTestSuite(ChainsTest.class);
        suite.addTestSuite(AncestorTest.class);
        suite.addTestSuite(MatrixIteratorTest.class);
        suite.addTestSuite(WeightTranslatorTest.class);
        suite.addTestSuite(CellTest.class);
        suite.addTestSuite(AnchorParser_GeneratorTest.class);
        suite.addTestSuite(DripAlignerTest.class);
        suite.addTestSuite(ForwardBackwardMatrixIterTest.class);
        suite.addTestSuite(AlignmentPumpTest.class);
        suite.addTestSuite(LibrarianTest.class);
        //$JUnit-END$
        return suite;
    }
}