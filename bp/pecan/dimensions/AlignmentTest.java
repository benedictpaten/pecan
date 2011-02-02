/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import bp.common.ds.Array;
import bp.common.fp.Function_2Args;
import bp.common.fp.Functions;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Generator;
import bp.common.fp.Predicate;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Predicates_2Args;

/**
 * @author benedictpaten
 */
public class AlignmentTest
                          extends AbstractDivisibleDimensionTest {
    final Dimension topD;
    
    final Function_2Args topFn;
    /**
     *  
     */
    public AlignmentTest(final String s) {
        super(s);
        final Object[] oA = this.makeRandomAlignment(this.subDimensionsNumber(), this.length());
        this.topD = (Dimension)oA[0];
        this.topFn = (Function_2Args)oA[1];
    }
    
    public void testAlignment() {
        final int subDimensionsNumber = 10, length = 100;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, length);
        this.testComplete((Dimension)oA[0], (Function_2Args)oA[1], length, subDimensionsNumber);
    }
    
    public void testTinyAlignment() {
        final int subDimensionsNumber = 10, length = 1;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, length);
        this.testComplete((Dimension)oA[0], (Function_2Args)oA[1], length, subDimensionsNumber);
    }
    
    public void testZeroLengthAlignment() {
        final int subDimensionsNumber = 10, length = 0;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, length);
        this.testComplete((Dimension)oA[0], (Function_2Args)oA[1], length, subDimensionsNumber);
    }
    
    public void testMinimumAlignment() {
        final int subDimensionsNumber = 2, length = 100;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, length);
        this.testComplete((Dimension)oA[0], (Function_2Args)oA[1], length, subDimensionsNumber);
    }
    
    public void testReverseAlignment() {
        final int subDimensionsNumber = 10, length = 100;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, length);
        final Function_2Args fn = Functions_2Args.lPipe((Function_2Args)oA[1], Functions.lCurry(
                Functions_2Args.subtract(), new Double(length - 1)));
        this.testComplete(((Dimension)oA[0]).getReversedSlice(), fn, length, subDimensionsNumber);
    }
    
    Dimension getDimension(final int start, final int length, final int originalLength) {
        final Function_2Args fn = Functions_2Args.lPipe(super.getFunction(), Functions.rCurry(
                Functions_2Args.subtract(), new Double(start)));
        return new SlicedDimension(DimensionTools.getSequenceOfFunction(
                Functions.rCurry(fn, new Double(0)), originalLength), start,
                length);
    }
    
    public void testAlignmentSlice() {
        final int subDimensionsNumber = 10, length = 100;
        final int start = 10, originalLength = 200;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, originalLength);
        final Function_2Args fn = Functions_2Args.lPipe((Function_2Args)oA[1], Functions.rCurry(
                Functions_2Args.sum(), new Double(start)));
        this.testComplete(((Dimension)oA[0]).getSlice(start, length), fn, length, subDimensionsNumber);
    }
    
    public void testTinyAlignmentSlice() {
        final int subDimensionsNumber = 10, length = 1;
        final int start = 10, originalLength = 200;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, originalLength);
        final Function_2Args fn = Functions_2Args.lPipe((Function_2Args)oA[1], Functions.rCurry(
                Functions_2Args.sum(), new Double(start)));
        this.testComplete(((Dimension)oA[0]).getSlice(start, length), fn, length, subDimensionsNumber);
    }
    
    public void testZeroLengthAlignmentSlice() {
        final int subDimensionsNumber = 10, length = 0;
        final int start = 10, originalLength = 200;
        final Object[] oA = this.makeRandomAlignment(subDimensionsNumber, originalLength);
        final Function_2Args fn = Functions_2Args.lPipe((Function_2Args)oA[1], Functions.rCurry(
                Functions_2Args.sum(), new Double(start)));
        this.testComplete(((Dimension)oA[0]).getSlice(start, length), fn, length, subDimensionsNumber);
    }

    Object[] makeRandomAlignment(final int subDimensionsNumber, final int length) {
        final List<Object[]> l = new ArrayList<Object[]>();
        for (int i = 0; i < subDimensionsNumber; i++) {
            l.add(new Object[] { this.getSequence(i, length), super.getFunction() });
        }
        while (l.size() > 1) {
            final int i = new Random().nextInt(l.size() - 1);
            final Dimension dL = (Dimension) l.get(i)[0], dR = (Dimension) l
                    .get(i + 1)[0];
            final Function_2Args fnL = (Function_2Args) l.get(i)[1], fnR = (Function_2Args) l
                    .get(i + 1)[1];
            int k = 0;
            for (int j = 0; j < i; j++) {
				k += ((Dimension) l.get(j)[0])
                        .subDimensionsNumber();
			}
            l.set(i, this.joinDimension(fnL, fnR, dL, dR, k, length));
            l.remove(i + 1);
        }
        return l.get(0);
    }

    Dimension getSequence(final int y, final int length) {
        return DimensionTools.getSequenceOfFunction(Functions.rCurry(super
                .getFunction(), new Double(y)), length);
    }

    Object[] joinDimension(final Function_2Args fnL, final Function_2Args fnR,
            final Dimension dL, final Dimension dR, final int lSubDimensions, final int length) {
        final List l = new ArrayList();
        for (int i = 0; i < length; i++) {
			l.add(Math.random() < (1.0 / 3.0) ? new boolean[] { true, true }
                    : Math.random() < 0.5 ? new boolean[] { false, true }
                            : new boolean[] { true, false });
		}
        return new Object[] {
                new Alignment(l.iterator(), dL, dR),
                this.getFunction(fnL, fnR, l, dL.subDimensionsNumber()
                        + lSubDimensions) };
    }

    Function_2Args getFunction(final Function_2Args fnL,
            final Function_2Args fnR, final List l, final int lSubDimensions) {
        return new Function_2Args() { /*
                                       * (non-Javadoc)
                                       * 
                                       * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
                                       *      java.lang.Object)
                                       */

            public Object fn(final Object o, final Object o2) {
                final int x = ((Number) o).intValue();
                final boolean[] bA = (boolean[]) l.get(x);
                final int y = ((Number) o2).intValue();
                final int z = y < lSubDimensions ? 0 : 1;
                if (bA[z] == true) {
                    int j = 0;
                    for (int i = 0; i < x; i++) {
						if (((boolean[]) l.get(i))[z] == true) {
							j++;
						}
					}
                    return z == 0 ? fnL.fn(new Integer(j), new Integer(y))
                            : fnR.fn(new Integer(j), new Integer(y));
                } else {
					return new Double(Integer.MAX_VALUE);
				}
            }
        };
    }

    public void testGetIndexOfFirstPosition() {
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(0, new int[] { 3, 4, 8,
                11 }), 0);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(1, new int[] { 3, 4, 8,
                11 }), 0);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(2, new int[] { 3, 4, 8,
                11 }), 0);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(3, new int[] { 3, 4, 8,
                11 }), 0);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(4, new int[] { 3, 4, 8,
                11 }), 1);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(5, new int[] { 3, 4, 8,
                11 }), 2);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(6, new int[] { 3, 4, 8,
                11 }), 2);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(7, new int[] { 3, 4, 8,
                11 }), 2);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(8, new int[] { 3, 4, 8,
                11 }), 2);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(9, new int[] { 3, 4, 8,
                11 }), 3);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(10, new int[] { 3, 4, 8,
                11 }), 3);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(11, new int[] { 3, 4, 8,
                11 }), 3);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(12, new int[] { 3, 4, 8,
                11 }), 4);
        Assert.assertEquals(Alignment.getIndexOfFirstPosition(13, new int[] { 3, 4, 8,
                11 }), 4);
    }

    public void testSplitArray() {
        final Predicate_2Args bP = Predicates_2Args.rCurry(Array
                .arraysEqual(), new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(Object o, Object o2) {
                return Arrays.equals((int[]) o, (int[]) o2);
            }
        });
        Assert.assertTrue(bP.test(Alignment.splitArray(new int[] { 3, 4, 8, 11 }, 0),
                new int[][] { {}, { 3, 4, 8, 11 } }));
        Assert.assertTrue(bP.test(Alignment.splitArray(new int[] { 3, 4, 8, 11 }, 1),
                new int[][] { { 3 }, { 4, 8, 11 } }));
        Assert.assertTrue(bP.test(Alignment.splitArray(new int[] { 3, 4, 8, 11 }, 2),
                new int[][] { { 3, 4 }, { 8, 11 } }));
        Assert.assertTrue(bP.test(Alignment.splitArray(new int[] { 3, 4, 8, 11 }, 3),
                new int[][] { { 3, 4, 8 }, { 11 } }));
        Assert.assertTrue(bP.test(Alignment.splitArray(new int[] { 3, 4, 8, 11 }, 4),
                new int[][] { { 3, 4, 8, 11 }, {} }));
    }

    public void testIsNotAllGaps() {
        final Predicate bP = Alignment.isNotAllGaps();
        Assert.assertTrue(bP.test(new int[] { 1, 2 }));
        Assert.assertTrue(bP.test(new int[] { 1, Integer.MAX_VALUE }));
        Assert.assertFalse(bP.test(new int[] { Integer.MAX_VALUE }));
        Assert.assertFalse(bP.test(new int[] { Integer.MAX_VALUE,
                Integer.MAX_VALUE }));
        try {
            Assert.assertTrue(bP.test(new int[0]));
            Assert.fail();
        } catch (final IllegalStateException e) {
            ;
        }
    }

    public void testArrayMaker() {
        final List l = Arrays.asList(new Object[] { Boolean.TRUE, Boolean.FALSE,
                Boolean.TRUE, Boolean.FALSE, Boolean.FALSE }), l2 = Arrays
                .asList(new Object[] { Boolean.TRUE, Boolean.FALSE,
                        Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
        final Generator gN = Alignment.arrayMaker(l.iterator(), l2.iterator());
        Assert.assertTrue(Arrays.equals((boolean[]) gN.gen(), new boolean[] { true,
                true }));
        Assert.assertTrue(Arrays.equals((boolean[]) gN.gen(), new boolean[] { true,
                false }));
        Assert.assertTrue(Arrays.equals((boolean[]) gN.gen(), new boolean[] { false,
                true }));
        Assert.assertEquals(gN.gen(), null);
    }

    /* (non-Javadoc)
     * @see bp.pecan.dimensions.AbstractDimensionTest#getDimension()
     */
    @Override
	Dimension getDimension() {
        return this.topD;
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
    
    @Override
	Function_2Args getFunction() {
        return this.topFn;
    }
    

}