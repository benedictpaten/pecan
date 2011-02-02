/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 15, 2005
 */
package bp.pecan.dimensions;

import bp.common.fp.Functions;

/**
 * @author benedictpaten
 */
public class PairedDimensionTest
                                extends AbstractDimensionTest {

    /**
     * @param arg0
     */
    public PairedDimensionTest(final String arg0) {
        super(arg0);
    }
    
    public void testTinySequence() {   
        final int length = 1;
        this.testComplete(this.getDimension(length), this.getFunction(), length, 2);
    }
    
    public void testZeroLengthSequence() {
        final int length = 0;
        this.testComplete(this.getDimension(length), this.getFunction(), length, 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    Dimension getDimension(final int length) {
        return new PairedDimension(DimensionTools
                .getSequenceOfFunction(Functions.rCurry(this.getFunction(),
                        new Double(0)), length), DimensionTools
                .getSequenceOfFunction(Functions.rCurry(this.getFunction(),
                        new Double(1)), length));
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
        return 2;
    }

}