/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import bp.common.fp.Functions;

/**
 * @author benedictpaten
 */
public class MergedDimensionTest
                                extends AbstractDivisibleDimensionTest {

    /**
     * Constructor for MergedDimensionTest.
     * 
     * @param arg0
     */
    public MergedDimensionTest(final String arg0) {
        super(arg0);
    }
    
    public void testTinySequence() {   
        final int length = 1;
        this.testComplete(this.getDimension(length, 0), this.getFunction(), length, 1);
        this.testComplete(this.getDimension(length, 1), this.getFunction(), length, 1);
    }
    
    public void testZeroLengthSequence() {
        final int length = 0;
        this.testComplete(this.getDimension(length, length/2), this.getFunction(), length, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.pecan.dimensions.SequenceTest#getDimension()
     */
    Dimension getDimension(final int length, final int splitPoint) {
        final Dimension d = DimensionTools.getSequenceOfFunction(Functions.rCurry(
                this.getFunction(), new Double(0)), length);
        return new MergedDimension(d.getSlice(0, splitPoint), d.getSlice(
                splitPoint, length - splitPoint));
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    @Override
	Dimension getDimension() {
        return this.getDimension(this.length(), this.length()/2);
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