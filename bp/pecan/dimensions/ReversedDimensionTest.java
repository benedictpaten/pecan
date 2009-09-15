/*
 * Created on Feb 11, 2005
 */
package bp.pecan.dimensions;

import bp.common.fp.Function_2Args;
import bp.common.fp.Functions;
import bp.common.fp.Functions_2Args;

/**
 * @author benedictpaten
 */
public class ReversedDimensionTest
                                  extends AbstractDivisibleDimensionTest {
    /**
     * Constructor for ReversedDimensionTest.
     * 
     * @param arg0
     */
    public ReversedDimensionTest(final String arg0) {
        super(arg0);
    }
    
    public void testTinySequence() {   
        final int length = 1;
        this.testComplete(this.getDimension(length), this.getFunction(), length, 1);
    }
    
    public void testZeroLengthSequence() {
        final int length = 0;
        this.testComplete(this.getDimension(length), this.getFunction(), length, 1);
    }

    Dimension getDimension(final int length) {
        final Function_2Args fn = Functions_2Args.lPipe(super.getFunction(), Functions.lCurry(
                Functions_2Args.subtract(), new Double(length - 1)));
        return new ReversedDimension(DimensionTools.getSequenceOfFunction(
                Functions.rCurry(fn, new Double(0)), length));
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    @Override
	Dimension getDimension() {
        return this.getDimension(this.length());
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#length()
     */
    @Override
	int length() {
        return 100;
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#subDimensionsNumber()
     */
    @Override
	int subDimensionsNumber() {
        return 1;
    }

}