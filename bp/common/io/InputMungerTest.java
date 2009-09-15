/*
 * Created on Jan 12, 2005
 */
package bp.common.io;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class InputMungerTest
                            extends TestCase {
    InputMunger iM;

    /**
     * Constructor for InputMungerTest.
     * 
     * @param arg0
     */
    public InputMungerTest(final String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        this.iM = new InputMunger();
        this.iM.addWatch("@", (byte) '@', 1, "");
        this.iM.addWatch("A", 1, "");
        this.iM.addWatch_VariableTermsLength("B", "");
        this.iM.addWatch_VariableTermsLength("C", "");
    }
 /*
       * @see TestCase#tearDown()
       */

    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddWatch() {
        try {
            if(this.iM.addWatch_VariableTermsLength("D", (byte) 'C', "")) {
				Assert.fail(this.iM.help());
			}
        } catch (final InputMunger.InputMungerException e) {
        }
        try {
            if(this.iM.addWatch_VariableTermsLength("C", (byte) 'D', "")) {
				Assert.fail(this.iM.help());
			}
        } catch (final InputMunger.InputMungerException e) {
        }
    }

    public void testParseInput() {
        final String[] sA = new String[] { "1 2 3 -@ 1 -B 1 2 3 -A 5.0 -C 1 2 3",
                "-@AB 1 5.0 1 2 3 -C" };
        for (final String element : sA) {
            this.iM.parseInput(element.split(" "));
            Assert.assertTrue(this.iM.watchSet("@"));
            Assert.assertTrue(this.iM.watchSet("A"));
            Assert.assertTrue(this.iM.watchSet("B"));
            Assert.assertTrue(this.iM.watchSet("C"));
            try {
                this.iM.watchSet("D");
                Assert.fail();
            } catch (final InputMunger.InputMungerException e) {
            }
            Assert.assertEquals(this.iM.parseValue(2, "@"), 1, 0.0);
            Assert.assertEquals(this.iM.parseValue(6.0, "A"), 5.0, 0.0);
            Assert.assertEquals(this.iM.watchStrings("B")[0], "1");
            Assert.assertEquals(this.iM.watchStrings("B")[1], "2");
            Assert.assertEquals(this.iM.watchStrings("B")[2], "3");
            this.iM.reset();
        }
        try {
            this.iM.parseInput("-@".split(" "));
            Assert.fail();
        } catch (final InputMunger.InputMungerException e) {
        }
        this.iM.reset();
        try {
            this.iM.parseInput("-@ 1 2".split(" "));
            Assert.fail();
        } catch (final InputMunger.InputMungerException e) {
        }
    }
}