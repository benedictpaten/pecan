/*
 * Created on Feb 14, 2005
 */
package bp.pecan.dimensions;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Functions;
import bp.common.fp.Predicate_2Args;

/**
 * @author benedictpaten
 */
public class DimensionToolsTest
                               extends TestCase {

    public void testGetLinearDimension() {
        final Dimension d = DimensionTools.getLinearSequence(5, 5);
        Assert.assertEquals(d.get(0, 0), 5);
        Assert.assertEquals(d.get(1, 0), 6);
        Assert.assertEquals(d.get(2, 0), 7);
        Assert.assertEquals(d.get(3, 0), 8);
        Assert.assertEquals(d.get(4, 0), 9);
        try {
            d.get(5, 0);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            d.get(0, 1);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
    }

    public void testGetSequenceOfFunction() {
        final Function fn = Functions.doNothing();
        final Dimension s = DimensionTools.getSequenceOfFunction(fn, 100);
        for (int i = 0; i < s.size(); i++) {
			Assert.assertEquals(s.get(i, 0), i);
		}
    }

    public void testDimensionsEqual() {
        final Dimension d1 = new FunctionalDimension(new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
             */
            public int fn(int i, int j) {
                return new int[][] { { 0, 1 }, { 2, 3 } }[i][j];
            }
        }, 2, 2), d2 = new FunctionalDimension(new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
             */
            public int fn(int i, int j) {
                return new int[][] { { 0, 1 }, { 4, 5 } }[i][j];
            }
        }, 2, 2);
        final Predicate_2Args dE = DimensionTools.dimensionsEqual();
        Assert.assertFalse(dE.test(d1, d2));
        Assert.assertTrue(dE.test(d1.getSlice(0, 1), d2.getSlice(0, 1)));
        Assert.assertTrue(dE.test(d1.getSlice(0, 0), d2.getSlice(0, 0)));
        Assert.assertTrue(dE.test(d1, d1));
        Assert.assertTrue(dE.test(d1.getSlice(0, 1), d1.getSlice(0, 1)));
        Assert.assertFalse(dE.test(d1, d1.getSlice(0, 1)));
        Assert.assertFalse(dE.test(d1.getSlice(0, 1), d1.getSlice(1, 1)));
    }

    public void testViewsEqual() {
        final Dimension d1 = new FunctionalDimension(new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
             */
            public int fn(int i, int j) {
                return new int[][] { { 0, 1 }, { 2, 3 } }[i][j];
            }
        }, 2, 2), d2 = new FunctionalDimension(new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
             */
            public int fn(int i, int j) {
                return new int[][] { { 0, 1 }, { 4, 5 } }[i][j];
            }
        }, 2, 2);
        final Predicate_2Args dE = DimensionTools.viewsEqual();
        Assert.assertFalse(dE.test(new View(d1, new int[] { 0, 1 }), new View(d2,
                new int[] { 0, 1 })));
        Assert.assertTrue(dE.test(new View(d1.getSlice(0, 1), new int[] { 0, 1 }),
                new View(d2.getSlice(0, 1), new int[] { 0, 1 })));
        Assert.assertTrue(dE.test(new View(d1.getSlice(0, 0), new int[] { 0, 1 }),
                new View(d2.getSlice(0, 0), new int[] { 0, 1 })));
        Assert.assertTrue(dE.test(new View(d1, new int[] { 0, 1 }), new View(d1,
                new int[] { 0, 1 })));
        Assert.assertTrue(dE.test(new View(d1.getSlice(0, 1), new int[] { 0, 1 }),
                new View(d1.getSlice(0, 1), new int[] { 0, 1 })));
        Assert.assertFalse(dE.test(new View(d1, new int[] { 0, 1 }), new View(d1
                .getSlice(0, 1), new int[] { 0, 1 })));
        Assert.assertFalse(dE.test(new View(d1.getSlice(0, 1), new int[] { 0, 1 }),
                new View(d1.getSlice(1, 1), new int[] { 0, 1 })));
    }

    /**
     *  
     */
    public void testCheckIndexBounds() {
        final Dimension d = new Sequence(new int[2]);
        DimensionTools.checkIndexBounds(1, 0, d);
        DimensionTools.checkIndexBounds(0, 0, d);
        try {
            DimensionTools.checkIndexBounds(-1, 0, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            DimensionTools.checkIndexBounds(0, -1, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            DimensionTools.checkIndexBounds(-1, -1, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            DimensionTools.checkIndexBounds(2, 0, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            DimensionTools.checkIndexBounds(0, 3, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
    }

    /**
     *  
     */
    public void testCheckSliceBounds() {
        final Dimension d = new Sequence(new int[2]);
        DimensionTools.checkSliceBounds(0, 2, d);
        DimensionTools.checkSliceBounds(0, 1, d);
        DimensionTools.checkSliceBounds(1, 1, d);
        DimensionTools.checkSliceBounds(2, 0, d);
        try {
            DimensionTools.checkSliceBounds(-1, 0, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            DimensionTools.checkSliceBounds(0, -1, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            DimensionTools.checkSliceBounds(-1, -1, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            DimensionTools.checkIndexBounds(0, 3, d);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
    }

    public void testCheckSubDimensionsSliceRequest() {
        final Dimension d = new Sequence(new int[2]);
        DimensionTools.checkSubDimensionSliceRequest(new int[] { 0 }, d);
        try {
            DimensionTools.checkSubDimensionSliceRequest(new int[] {}, d);
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
        try {
            DimensionTools.checkSubDimensionSliceRequest(new int[] { 1 }, d);
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
        try {
            DimensionTools.checkSubDimensionSliceRequest(new int[] { 0, 0 }, d);
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
    }
}