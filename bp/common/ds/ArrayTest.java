/*
 * Created on May 6, 2005
 */
package bp.common.ds;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function_Int;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Functions_Int_2Args;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Predicates_2Args;

/**
 * @author benedictpaten
 */
public class ArrayTest
                      extends TestCase {
    
    public void testIndicesOf() {
        final int[] iA = new int[] { 0, 4, 6, 8, 9, 1, 3, 4, 5, 4, 4 };
        Assert.assertTrue(Arrays.equals(Array.indicesOf(iA, iA.length, 4), new int[] { 1, 7, 9, 10 }));
        Assert.assertTrue(Arrays.equals(Array.indicesOf(iA, iA.length-1, 4), new int[] { 1, 7, 9 }));
        Assert.assertTrue(Arrays.equals(Array.indicesOf(iA, iA.length-2, 4), new int[] { 1, 7 }));
        Assert.assertTrue(Arrays.equals(Array.indicesOf(iA, 2, 4), new int[] { 1 }));
        Assert.assertTrue(Arrays.equals(Array.indicesOf(iA, 1, 4), new int[] {  }));
        Assert.assertTrue(Arrays.equals(Array.indicesOf(iA, 0, 4), new int[] {  }));
    }
    
    public void testConcatenate_Int() {
        int[] iA = new int[] { 0, 1, 2, 3, 4 };
        int[] iA2 = new int[] { 5, 6, 7 };
        int[] iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }));
        iA = new int[] { };
        iA2 = new int[] { 5, 6, 7 };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new int[] { 5, 6, 7 }));
        iA = new int[] { };
        iA2 = new int[] {  };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new int[] {  }));
        iA = new int[] { 5, 6, 7 };
        iA2 = new int[] {  };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new int[] { 5, 6, 7 }));
        iA = new int[] { 1 };
        iA2 = new int[] {  };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new int[] { 1 }));
    }
    
    public void testConcatenate_Boolean() {
        boolean[] iA = new boolean[] { true, false, true,false,false };
        boolean[] iA2 = new boolean[] {false, true, true };
        boolean[] iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new boolean[] { true,false, true,false,false,false,true,true }));
        iA = new boolean[] { };
        iA2 = new boolean[] {false,true,true };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new boolean[] {false,true,true }));
        iA = new boolean[] { };
        iA2 = new boolean[] {  };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new boolean[] {  }));
        iA = new boolean[] {false,true,true };
        iA2 = new boolean[] {  };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new boolean[] {false,true,true }));
        iA = new boolean[] {false };
        iA2 = new boolean[] {  };
        iA3 = Array.concatenate(iA, iA2);
        Assert.assertTrue(Arrays.equals(iA3, new boolean[] {false }));
    }
    
    public void testIndexOf() {
        final String[] sA = new String[] { "0", "1", "2", "3", "4", "5" };
        for(int i=0; i<6; i++) {
			Assert.assertEquals(Array.indexOf(sA, "" + i), i);
		}
        Assert.assertEquals(Array.indexOf(sA, "7"), Integer.MAX_VALUE);
    }

    public void testFill() {
        float[] fA = new float[6];
        Array.fill(new float[] { 1f, 2f, 3f }, fA, 2);
        Assert.assertTrue(Arrays.equals(
                new float[] { 1f, 2f, 3f, 1f, 2f, 3f }, fA));
        fA = new float[0];
        Array.fill(new float[] { 1f, 2f, 3f }, fA, 0);
        Assert.assertTrue(Arrays.equals(
                new float[] { }, fA));
        fA = new float[9];
        Array.fill(new float[] { 1f, 2f, 3f }, fA, 2);
        Assert.assertTrue(Arrays.equals(
                new float[] { 1f, 2f, 3f, 1f, 2f, 3f, 0f, 0f, 0f }, fA));
    }

    public void testGetMin() {
        Assert.assertEquals(2, Array.getMin(new int[] { 9, 2, 6, 8, 5, 7 }));
        Assert.assertEquals(2, Array.getMin(new int[] { 2, 6, 8, 5, 7 }));
        Assert.assertEquals(2, Array.getMin(new int[] { 9, 6, 8, 5, 7, 2 }));
        Assert.assertEquals(5, Array.getMin(new int[] { 5 }));
        Assert.assertEquals(Integer.MIN_VALUE, Array.getMin(new int[] { 5,
                Integer.MIN_VALUE }));
        Assert.assertEquals(Integer.MIN_VALUE, Array.getMin(new int[] {
                Integer.MIN_VALUE, 5 }));
        Assert.assertEquals(Integer.MAX_VALUE, Array.getMin(new int[] {}));
    }

    public void testGetMax() {
        Assert.assertEquals(8, Array.getMax(new int[] { 2, 6, 8, 5, 7 }));
        Assert.assertEquals(5, Array.getMax(new int[] { 5 }));
        Assert.assertEquals(Integer.MAX_VALUE, Array.getMax(new int[] { 5,
                Integer.MAX_VALUE }));
        Assert.assertEquals(Integer.MAX_VALUE, Array.getMax(new int[] {
                Integer.MAX_VALUE, 5 }));
        Assert.assertEquals(Integer.MIN_VALUE, Array.getMax(new int[] {}));
    }

    public void testMerge_TwoStep() {
        for (int trial = 0; trial < 1000; trial++) {
            final SortedMap<Object, Object> sM = new TreeMap<Object, Object>();
            while (Math.random() > 0.05) {
                sM.put(new Integer((int) (Math.random() * 100)),
                        new Integer((int) (Math.random() * 100)));
            }
            final List<Object> l = new LinkedList<Object>(), l2 = new LinkedList<Object>(), l3 = new LinkedList<Object>();
            for (final Iterator<Object> it = sM.keySet().iterator(); it.hasNext();) {
                final Object o = it.next();
                int i = 0;
                if (Math.random() > 0.5) {
                    l.add(o);
                    l.add(sM.get(o));
                    i++;
                }
                if ((i == 0) || (Math.random() > 0.5)) {
                    l2.add(o);
                    l2.add(sM.get(o));
                    i++;
                }
                if (i == 2) {
                    l3.add(o);
                    l3.add(new Integer(((Integer) sM.get(o))
                            .intValue() * 2));
                }
            }
            for (final Iterator<Object> it = l3.iterator(); it.hasNext();) {
				sM.put(it.next(), it.next());
			}
            final int[] iA = new int[l.size() + (int) (Math.random() * 100)], iA2 = new int[l2
                    .size()
                    + (int) (Math.random() * 100)];
            for (int i = 0; i < l.size(); i++) {
				iA[i] = ((Integer) l.get(i)).intValue();
			}
            for (int i = 0; i < l2.size(); i++) {
				iA2[i] = ((Integer) l2.get(i)).intValue();
			}
            final int[] iA3 = new int[1000];
            final int lI = Array.merge_TwoStep(iA, l.size(), iA2,
                    l2.size(), Functions_Int_2Args.sum(), iA3);
            int i = 0;
            for (final Iterator<Object> it = sM.keySet().iterator(); it.hasNext();) {
                final Object o = it.next();
                Assert.assertEquals(((Integer) o).intValue(), iA3[i++]);
                Assert.assertEquals(((Integer) sM.get(o)).intValue(),
                        iA3[i++]);
            }
            Assert.assertEquals(i, lI);
        }
    }

    public void testIntersection() {
        int[] iA = new int[] { 1, 2, 7, 8 }, iA2 = new int[] { 2, 8,
                9 };
        int[] iA3 = new int[5];
        Assert.assertEquals(Array.intersection(iA, 4, iA2, 3, iA3), 2);
        Assert.assertTrue(Arrays.equals(iA3, new int[] { 2, 8, 0, 0, 0 }));

        iA = new int[] { 1, 2, 7, 8 };
        iA2 = new int[] { 2, 8, 9 };
        iA3 = new int[5];
        Assert.assertEquals(Array.intersection(iA, 2, iA2, 1, iA3), 1);
        Assert.assertTrue(Arrays.equals(iA3, new int[] { 2, 0, 0, 0, 0 }));

        iA = new int[] { 1, 2, 7, 8 };
        iA2 = new int[] { 2, 8, 9 };
        iA3 = new int[5];
        Assert.assertEquals(Array.intersection(iA, 1, iA2, 3, iA3), 0);
        Assert.assertTrue(Arrays.equals(iA3, new int[] { 0, 0, 0, 0, 0 }));

        iA = new int[] { 1, 2, 7, 8 };
        iA2 = new int[] { 2, 8, 9 };
        iA3 = new int[0];
        Assert.assertEquals(Array.intersection(iA, 0, iA2, 0, iA3), 0);
        Assert.assertTrue(Arrays.equals(iA3, new int[] {}));
    }

    public void testSum() {
        double[] dA = new double[] { 5, 1, 10 }, dA2 = new double[] {
                -1, 0, 12 };
        Assert.assertTrue(Arrays.equals(Array.sum(dA, dA2, Functions_2Args
                .sum()), new double[] { 4, 1, 22 }));

        dA = new double[] {};
        dA2 = new double[] {};
        Assert.assertTrue(Arrays.equals(Array.sum(dA, dA2, Functions_2Args
                .sum()), new double[] {}));

        dA = new double[] { 5, 1, 10 };
        dA2 = new double[] {};
        try {
            Array.sum(dA, dA2, Functions_2Args.sum());
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        try {
            Array.sum(dA2, dA, Functions_2Args.sum());
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
    }

    public void testArraysEqual() {
        final Predicate_2Args bP = Predicates_2Args.rCurry(Array
                .arraysEqual(), Predicates_2Args.equal());
        Assert.assertTrue(bP.test(null, null));
        Assert.assertTrue(bP.test(new Object[0], new Object[0]));
        Assert.assertTrue(bP.test(new Object[][] { { "1" }, { "2" } },
                new Object[][] { { "1" }, { "2" } }));
        Assert.assertTrue(bP.test(new Object[][][] { { {}, { "1" } },
                { { "2", "3" } } }, new Object[][][] {
                { {}, { "1" } }, { { "2", "3" } } }));
        Assert.assertFalse(bP.test(new Object[0], new Object[2]));
        Assert.assertFalse(bP.test(
                new Object[][] { { "1", "1.5" }, { "2" } },
                new Object[][] { { "1" }, { "2" } }));
        Assert.assertFalse(bP.test(new Object[][][] { { {}, { "1" } },
                { { "2", "3" } } }, new Object[][][] {
                { {}, { "1" } }, { { "2", "3" }, {} } }));
    }

    public void testBinarySearch() {
        for (int i = 0; i < 1000; i++) {
            final int[] iA = new int[(int) (Math.random() * 50)];
            int k = 0;
            for (int j = 0; j < iA.length; j++) {
                k += 1 + (int) (Math.random() * 100);
                iA[j] = k;
            }
            final Function_Int fn = Array.binarySearch(iA);
            int l = 0;
            for (int j = 0; j < k + 100; j++) {
                Assert.assertEquals(fn.fn(j), l);
                if ((l < iA.length) && (j == iA[l])) {
					l++;
				}
            }
        }
    }

    public void testBinarySearch_TwoStep() {
        for (int i = 0; i < 1000; i++) {
            final int[] iA = new int[((int) (Math.random() * 50)) * 2];
            int k = 0;
            for (int j = 0; j < iA.length; j += 2) {
                k += 1 + (int) (Math.random() * 100);
                iA[j] = k;
                iA[j + 1] = (int) (Integer.MAX_VALUE * Math.random());
            }
            int l = 0;
            for (int j = 0; j < k + 100; j++) {
                // if(Array.binarySearchIndex_TwoStep(iA, j) != l/2)
                // fail();
                Assert.assertEquals(Array.binarySearchIndex_TwoStep(iA, j),
                        l);
                if ((l < iA.length) && (j == iA[l])) {
					l += 2;
				}
            }
        }
    }

    public void testMatrix1DLookUp() {
        final Function_Int fn = Array
                .matrix1DLookUp(new int[] { 0, 3, 4, 2 });
        Assert.assertEquals(fn.fn(0), 0);
        Assert.assertEquals(fn.fn(1), 3);
        Assert.assertEquals(fn.fn(2), 4);
        Assert.assertEquals(fn.fn(3), 2);
    }

    public void testMatrix2DLookup() {
        final Function_Int_2Args fn = Array.matrix2DLookup(new int[] { 1,
                2, 3, 4 }, 2);
        Assert.assertEquals(fn.fn(0, 0), 1);
        Assert.assertEquals(fn.fn(1, 0), 2);
        Assert.assertEquals(fn.fn(0, 1), 3);
        Assert.assertEquals(fn.fn(1, 1), 4);
    }

    public void testTranspose2DMatrix() {
        int[][] iA = new int[3][2];
        for (final int[] element : iA) {
            for (int j = 0; j < element.length; j++) {
                element[j] = new Random().nextInt(100);
            }
        }
        int[][] iA2 = (int[][]) Array.transpose2DMatrix().fn(iA);
        for (int i = 0; i < iA.length; i++) {
            for (int j = 0; j < iA[i].length; j++) {
                Assert.assertEquals(iA[i][j], iA2[j][i]);
            }
        }
        iA = new int[3][0];
        iA2 = (int[][]) Array.transpose2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 0);
        iA = new int[0][3];
        iA2 = (int[][]) Array.transpose2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 0);
        iA = new int[0][0];
        iA2 = (int[][]) Array.transpose2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 0);
    }

    public void testReverseXAxisOf2DMatrix() {
        int[][] iA = new int[3][2];
        for (final int[] element : iA) {
            for (int j = 0; j < element.length; j++) {
                element[j] = new Random().nextInt(100);
            }
        }
        int[][] iA2 = (int[][]) Array.reverseXAxisOf2DMatrix().fn(iA);
        for (int i = 0; i < iA.length; i++) {
            for (int j = 0; j < iA[i].length; j++) {
                Assert.assertEquals(iA[i][j], iA2[2 - i][j]);
            }
        }
        iA = new int[3][0];
        iA2 = (int[][]) Array.reverseXAxisOf2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 3);
        Assert.assertEquals(iA2[0].length, 0);
        Assert.assertEquals(iA2[1].length, 0);
        Assert.assertEquals(iA2[2].length, 0);
        iA = new int[0][3];
        iA2 = (int[][]) Array.reverseXAxisOf2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 0);
        iA = new int[0][0];
        iA2 = (int[][]) Array.reverseXAxisOf2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 0);
    }

    public void testReverseYAxisOf2DMatrix() {
        int[][] iA = new int[3][2];
        for (final int[] element : iA) {
            for (int j = 0; j < element.length; j++) {
                element[j] = new Random().nextInt(100);
            }
        }
        int[][] iA2 = (int[][]) Array.reverseYAxisOf2DMatrix().fn(iA);
        for (int i = 0; i < iA.length; i++) {
            for (int j = 0; j < iA[i].length; j++) {
                Assert.assertEquals(iA[i][j], iA2[i][1 - j]);
            }
        }
        iA = new int[3][0];
        iA2 = (int[][]) Array.reverseYAxisOf2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 3);
        Assert.assertEquals(iA2[0].length, 0);
        Assert.assertEquals(iA2[1].length, 0);
        Assert.assertEquals(iA2[2].length, 0);
        iA = new int[0][3];
        iA2 = (int[][]) Array.reverseYAxisOf2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 0);
        iA = new int[0][0];
        iA2 = (int[][]) Array.reverseYAxisOf2DMatrix().fn(iA);
        Assert.assertEquals(iA2.length, 0);
    }

    public void testReverseArray() {
        for (int trial = 0; trial < 1000; trial++) {
            final int[] iA = new int[(int) (Math.random() * 100)];
            for (int i = 0; i < iA.length; i++) {
				iA[i] = (int) (Math.random() * 100);
			}
            final int[] iA2 = iA.clone();
            final int size = (int) (Math.random() * (iA.length + 1));
            Array.reverseArray(iA2, size);
            for (int i = 0; i < size; i++) {
				Assert.assertEquals(iA[i], iA2[size - 1 - i]);
			}
        }
    }

    public void testReverseArray_Float() {
        for (int trial = 0; trial < 1000; trial++) {
            final float[] iA = new float[(int) (Math.random() * 100)];
            for (int i = 0; i < iA.length; i++) {
				iA[i] = (float) (Math.random() * 100);
			}
            final int size = (int) (Math.random() * (iA.length + 1));
            final int offset = (int) (Math.random() * 100);
            final float[] iA2 = new float[iA.length + offset];
            System.arraycopy(iA, 0, iA2, offset, size);
            Array.reverseArray(iA2, offset, offset + size);
            for (int i = 0; i < size; i++) {
				Assert.assertEquals(iA[i], iA2[offset + size - 1 - i], 0);
			}
        }
    }

    public void testReverseArray_Double() {
        for (int trial = 0; trial < 1000; trial++) {
            final double[] iA = new double[(int) (Math.random() * 100)];
            for (int i = 0; i < iA.length; i++) {
				iA[i] = (Math.random() * 100);
			}
            final double[] iA2 = iA.clone();
            final int size = (int) (Math.random() * (iA.length + 1));
            Array.reverseArray(iA2, size);
            for (int i = 0; i < size; i++) {
				Assert.assertEquals(iA[i], iA2[size - 1 - i], 0);
			}
        }
    }

    public void testMingle() {
        try {
            Array.mingle(new int[1], 1);
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
        Array.mingle(new int[0], 0);
        final int[] iA = new int[] { 0, 1, 2, 3, 4, 5 };
        Array.mingle(iA, 6);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 1, 0, 3, 2, 5, 4 }));
    }
    
    public void testUniq() {
        final int[] iA = new int[] { 1, 1, 3, 5, 5, 8, 7, 9, 9, 9 };
        final int[] iA2 = new int[10];
        int i = Array.uniq(iA, iA2);
        Assert.assertEquals(i, 6);
        Assert.assertTrue(Arrays.equals(new int[] { 1, 3, 5, 8, 7, 9, 0, 0, 0, 0 }, iA2));
        i = Array.uniq(iA, iA);
        Assert.assertEquals(i, 6);
        Assert.assertTrue(Arrays.equals(new int[] { 1, 3, 5, 8, 7, 9, 7, 9, 9, 9 }, iA));
        Assert.assertEquals(Array.uniq(new int[0], new int[0]), 0);
        i = Array.uniq(new int[] { 0 }, iA2);
        Assert.assertEquals(i, 1);
        Assert.assertTrue(Arrays.equals(new int[] { 0, 3, 5, 8, 7, 9, 0, 0, 0, 0 }, iA2));
    }

    public void testConvertToInts() {
        String[] sA = new String[] { "1", "4", "7" };
        Arrays.equals(new int[] { 1, 4, 7 }, Array.convertToInts(sA));
        sA = new String[] { "1" };
        Arrays.equals(new int[] { 1 }, Array.convertToInts(sA));
        sA = new String[] { };
        Arrays.equals(new int[] { }, Array.convertToInts(sA));
    }
    
    public void testConvertToBooleans() {
        String[] sA = new String[] { "true", "TruE", "False" };
        Arrays.equals(new boolean[] { true, true, false }, Array.convertToBooleans(sA));
        sA = new String[] { "true" };
        Arrays.equals(new boolean[] { true }, Array.convertToBooleans(sA));
        sA = new String[] { };
        Arrays.equals(new boolean[] { }, Array.convertToBooleans(sA));
    }
}