/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Apr 2, 2006
 */
package bp.common.ds;

import java.util.Hashtable;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function_Int_2Args;
import bp.common.io.Debug;

public class IntCrapHashTest
                            extends TestCase {
    public void testIntCrashHash() {
        final Function_Int_2Args sum = new Function_Int_2Args() {
            public int fn(int i, int j) {
                return i + j;
            }
        };
        final long time = System.currentTimeMillis();
        for (int trial = 0; trial < 10000; trial++) {
            // Debug.pl(" trial " + trial);
            final int size = (int) (Math.random() * 500);
            final Hashtable<Integer, Integer> hT = new Hashtable<Integer, Integer>();
            final IntCrapHash hT2 = new IntCrapHash(size);
            while (Math.random() > 0.5) {
                final int entries = (int) (Math.random() * 500);
                int entry = 0;
                while (entry < entries) {
                    final int i = (int) (Math.random() * 5000);
                    final int j = (int) (Math.random() * 5000);
                    hT2.put(i, j, sum);
                    if (hT.containsKey(new Integer(i))) {
						hT.put(new Integer(i), new Integer(
                                hT.get(new Integer(i))
                                        .intValue()
                                        + j));
					} else {
						hT.put(new Integer(i), new Integer(j));
					}
                    entry++;
                }
                final int[] iA = new int[10000];
                int i = hT2.getEntries(iA);
                for (int j = 0; j < i; j += 2) {
                    final int k = iA[j];
                    final int l = iA[j + 1];
                    final int m = hT.remove(new Integer(k))
                            .intValue();
                    if (l != m) {
						Assert.fail();
					}
                }
                Assert.assertEquals(hT.size(), 0);
                hT2.clear();
                i = hT2.getEntries(iA);
                Assert.assertEquals(i, 0);
                Assert.assertEquals(hT2.eI, 0);
                for (final int element : hT2.hash) {
                    Assert.assertEquals(element, Integer.MAX_VALUE);
                }
            }
        }
        Debug.pl((System.currentTimeMillis() - time) / 1000.0f
                + " time ");
    }
}
