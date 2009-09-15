/*
 * Created on Feb 11, 2005
 */
package bp.pecan.dimensions;

import bp.common.fp.Functions;


/**
 * @author benedictpaten
 */
public class SequenceTest
                         extends AbstractDimensionTest {
    final static int length = 100;

    /**
     * Constructor for SequenceTest.
     * 
     * @param arg0
     */
    public SequenceTest(final String arg0) {
        super(arg0);
    }
    
    public void testTinySequence() {
        final int length = 1;
        final Dimension d = DimensionTools.getSequenceOfFunction(Functions.
                rCurry(this.getFunction(), new Double(0)), length);
        this.testComplete(d, this.getFunction(), length, 1);
    }
    
    public void testZeroLengthSequence() {
        final int length = 0;
        final Dimension d = DimensionTools.getSequenceOfFunction(Functions.
                rCurry(this.getFunction(), new Double(0)), length);
        this.testComplete(d, this.getFunction(), length, 1);
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    @Override
	Dimension getDimension() {
        return DimensionTools.getSequenceOfFunction(Functions.
                rCurry(this.getFunction(), new Double(0)), this.length());
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