/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import junit.framework.Assert;
import bp.common.fp.Function_Int_2Args;

/**
 * @author benedictpaten
 */
public class FunctionalDimensionTest
                                    extends AbstractDimensionTest {

    /**
     * @param arg0
     */
    public FunctionalDimensionTest(final String arg0) {
        super(arg0);
    }
    
    
    public void testConstructor() {
        try {
            this.getDimension(100, 0);
            Assert.fail();
        }
        catch(final IllegalArgumentException e) {
            ;
        }
        try {
            this.getDimension(-1, 1);
            Assert.fail();
        }
        catch(final IllegalArgumentException e) {
            ;
        }
    }
    
    public void testSequence() {
        final int length = 100;
        final int subDimensions = 10;
        this.testComplete(this.getDimension(length, subDimensions), this.getFunction(), length, subDimensions);
    }
    

    public void testTinySequence() {
        final int length = 1;
        final int subDimensions = 10;
        this.testComplete(this.getDimension(length, subDimensions), this.getFunction(), length, subDimensions);
    }
    

    public void testZeroLengthSequence() {
        final int length = 0;
        final int subDimensions = 10;
        this.testComplete(this.getDimension(length, subDimensions), this.getFunction(), length, subDimensions);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    Dimension getDimension(final int length, final int subDimensions) {
        return new FunctionalDimension(new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function__Int_2Args#polygonClipper(int)
             */
            public int fn(final int x, final int y) {
                return ((Number) FunctionalDimensionTest.this.getFunction().fn(new Double(x), new Double(y)))
                        .intValue();
            }
        }, length, subDimensions);
    }


    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    @Override
	Dimension getDimension() {
        return this.getDimension(this.length(), this.subDimensionsNumber());
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
        return 10;
    }


}