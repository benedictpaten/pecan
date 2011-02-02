/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

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
public class SlicedDimensionTest
                                extends AbstractDimensionTest {

    /**
     * Constructor for SlicedDimensionTest.
     * 
     * @param arg0
     */
    public SlicedDimensionTest(final String arg0) {
        super(arg0);
    }
    
    public void testTinySequenceSlice() {
        final int start = 10, length = 1, originalLength = 200;
        this.testComplete(this.getDimension(start, length, originalLength), this.getFunction(), length, 1);
    }
    
    public void testZeroLengthSequenceSlice() {
        final int start = 10, length = 0, originalLength = 200;
        this.testComplete(this.getDimension(start, length, originalLength), this.getFunction(), length, 1);
    }
    
    public void testZeroLengthSequenceSliceOfZeroLengthSequence() {
        final int start = 0, length = 0, originalLength = 0;
        this.testComplete(this.getDimension(start, length, originalLength), this.getFunction(), length, 1);
    }   
    
    Dimension getDimension(final int start, final int length, final int originalLength) {
        final Function_2Args fn = Functions_2Args.lPipe(super.getFunction(), Functions.rCurry(
                Functions_2Args.subtract(), new Double(start)));
        return new SlicedDimension(DimensionTools.getSequenceOfFunction(
                Functions.rCurry(fn, new Double(0)), originalLength), start,
                length);
    }
    

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    @Override
	Dimension getDimension() {
        final int start = 10, originalLength = 200;
        return this.getDimension(start, this.length(), originalLength);
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