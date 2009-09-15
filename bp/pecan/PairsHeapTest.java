/*
 * Created on Feb 9, 2006
 */
package bp.pecan;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.io.Debug;

public class PairsHeapTest
                          extends TestCase {

    public void testPairsHeap() {
        final int[] iA = new int[100000];
        PairsHeap.initialise(12 + (int)(Math.random()*20), 2.0f, false);
        for (int trial = 0; trial < 1000; trial++) {
            Debug.pl(" trial " + trial);
            int firstIndex = (int) (Math.random() * 1000);
            int lastIndex = firstIndex - 1;
            final int startLength = 1 + (int) (Math.random() * 1000);
            final PairsHeap[] lib = PairsHeap.getPair(
                    0, firstIndex, startLength);
            final PairsHeap pH = lib[1];
            final Map<Integer, Map<Integer, Integer>> hM = new HashMap<Integer, Map<Integer, Integer>>();
            int numberOfElements = 0;
            int peakNumberOfElements = 0;
            while (Math.random() > 0.01) {
                final int from = firstIndex + (int) (Math.random() * 100);
                int to = (int) (Math.random() * 10);
                int weight = Float.floatToIntBits((float) Math
                        .random());
                while (Math.random() > 0.3) {
                    if (from > lastIndex) {
						lastIndex = from;
					}
                    if (hM.containsKey(new Integer(from))) {
                        final Map<Integer, Integer> m = hM.get(new Integer(from));
                        if (m.containsKey(new Integer(to))) {
							m
                                    .put(
                                            new Integer(to),
                                            new Integer(
                                                    Float
                                                            .floatToIntBits(Float
                                                                    .intBitsToFloat(m
                                                                            .get(new Integer(
                                                                                    to))
                                                                            .intValue())
                                                                    + Float
                                                                            .intBitsToFloat(weight))));
						} else {
                            m.put(new Integer(to),
                                    new Integer(weight));
                            numberOfElements++;
                        }
                    } else {
                        final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
                        m.put(new Integer(to), new Integer(weight));
                        hM.put(new Integer(from), m);
                        numberOfElements++;
                    }
                    pH.add(from, to, weight);
                    //if(PairsHeap.used()*2 != numberOfElements)
                    //    fail();
                    //assertEquals(PairsHeap.used(), numberOfElements*2);
                    if (numberOfElements > peakNumberOfElements) {
						peakNumberOfElements = numberOfElements;
					}
                    to = (int) (Math.random() * 10);
                    weight = Float.floatToIntBits((float) Math
                            .random());
                }
                if (numberOfElements > peakNumberOfElements) {
					peakNumberOfElements = numberOfElements;
				}
                while (Math.random() > 0.5) {
                    if (lastIndex >= firstIndex) {
                        final int index = firstIndex
                                + (int) (Math.random() * (lastIndex - firstIndex));
                        if (hM.containsKey(new Integer(index))) {
                            final Map m = hM.get(new Integer(index));
                            final int j = pH.get(index, 0, iA);
                            {
                                int k = Integer.MAX_VALUE;
                                for (int i = 0; i < j; i += 2) {
                                    Assert.assertTrue(k > iA[i]);
                                    k = iA[i];
                                }
                            }
                            Assert.assertEquals(m.size(), j / 2);
                            float total = 0;
                            for (int i = 0; i < j; i += 2) {
                                final int k = ((Integer) m.get(new Integer(
                                        iA[i]))).intValue();
                                Assert.assertEquals(iA[i + 1], k);
                                Assert.assertEquals(iA[i + 1], pH.getWeight(index, iA[i], Float.floatToIntBits(0.0f)));
                                while(Math.random() > 0.1f) {
                                    Assert.assertEquals(Integer.MAX_VALUE, pH.getWeight(index, 1000 + (int)(Math.random() * 1000), Integer.MAX_VALUE));
                                    Assert.assertEquals(Integer.MAX_VALUE, pH.getWeight(index, -1000 + (int)(Math.random() * -1000), Integer.MAX_VALUE));
                               }
                                total += Float.intBitsToFloat(iA[i + 1]);
                            }
                            Assert.assertEquals(Float.intBitsToFloat(pH.sumTotal(index, Float.floatToIntBits(0.0f))), total, 0.0001);
                        } else {
                            Assert.assertEquals(pH.get(index, 0, iA), 0);
                        }
                    }
                }
                //assertEquals(PairsHeap.used(), numberOfElements*2);
                while ((Math.random() > 0.8) && (firstIndex <= lastIndex)) {
                    pH.tryToRemoveUpto(pH.firstIndex() + 1);
                    if (hM.containsKey(new Integer(firstIndex))) {
                        final Map m = hM.remove(new Integer(
                                firstIndex));
                        numberOfElements -= m.size();
                    }
                    //assertEquals(PairsHeap.used(), numberOfElements*2);
                    firstIndex++;
                }
            }
            int k = 0;
            for (int i = firstIndex; i <= lastIndex; i++) {
                k += pH.get(i, 0, iA);
                pH.tryToRemoveUpto(pH.firstIndex() + 1);
            }
            lib[0].tryToRemoveUpto(2000+1);
            Assert.assertEquals(pH.firstIndex(), pH.lastActualIndex());
            Assert.assertEquals(k / 2, numberOfElements);
            Assert.assertEquals(PairsHeap.used(), 0);
            // assertEquals(free, pH.capacity());
            //if (averageWeightNumber * startLength <= peakNumberOfElements)
             //   assertTrue(peakNumberOfElements * 2 >= free);
        }
    }
}
