/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Dec 16, 2005
 */
package bp.trawler;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Predicate_Int;

public class TrawlerStatisticsTest
                                  extends TestCase {
    
    public void testZScore() {
        for(int trial=0; trial<1000; trial++) {
            final int observed = (int)(Math.random()*100);
            final int background = (int)(Math.random()*100);
            final int length = (int)(Math.random()*10);
            final int sD = (int)(Math.random()*10);
            final float[] fA = new float[length];
            for(int i=0; i<length; i++) {
				fA[i] = (float)Math.random();
			}
            final int l2 = (int)(Math.random()*length);
            if(l2 == 0) {
				continue;
			}
            final float f = TrawlerStatistics.zScore(observed, background, l2, fA);
            final double d = (observed - (background*fA[l2]))/Math.sqrt((background*fA[l2])*(1.0 - fA[l2]));
            Assert.assertEquals(f, (float)d, 0.0001);
        }
    }

    public void testBuckets() {
        for (int trial = 0; trial < 100; trial++) {
            final byte[][] bAA = TrawlerToolsTest.randomStrings();
            final byte[] bA = TrawlerToolsTest.getString(bAA);
            final int length = (int) (Math.random() * 100);
            final int[] rA = TrawlerStatistics.buckets(bA, length,
                    new Predicate_Int() {
                public boolean test(int i) {
                    return (i | 15) == 15;
                }
            });
            Assert.assertEquals(Integer.MAX_VALUE, rA[0]);
            if (length > 0) {
                Assert.assertEquals(bA.length - bAA.length, rA[1]);
                final int k = (int) (Math.random() * length);
                if (k == 0) {
					continue;
				}
                int j = 0;
                for (final byte[] element : bAA) {
                    if (element.length >= k) {
						j += element.length - k + 1;
					}
                }
                Assert.assertEquals(rA[k], j);
            }
        }
    }
    
    public void testReplaceSequences() {
        for (int trial = 0; trial < 100;) {
            final byte[][] bAA = TrawlerToolsTest.randomStrings();
            final byte[] bA = TrawlerToolsTest.getString(bAA);
            final byte[][] bAA2 = TrawlerToolsTest.randomStrings();
            final byte[] bA2 = TrawlerToolsTest.getString(bAA2);
            final byte[] bA3 = bA.clone();
            final Predicate_Int p =  new Predicate_Int() {
                public boolean test(int i) {
                    return (i | 15) == 15;
                }
            };
            if(TrawlerStatisticsTest.countActivePositions(bA, p) > TrawlerStatisticsTest.countActivePositions(bA2, p)) {
				continue;
			}
            trial++;
            TrawlerStatistics.replaceSequences(bA, bA2, p);
            int i=0;
            int j=0;
            while(i < bA.length) {
                if(p.test(bA[i])) {
                    while(!p.test(bA2[j])) {
						j++;
					}
                    Assert.assertEquals(bA2[j++], bA[i]);
                } else {
					Assert.assertEquals(bA3[i], bA[i]);
				}
                i++;
            }
        }
    }
    
    public static int countActivePositions(final byte[] bA, final Predicate_Int p) {
        int j=0;
        for (final byte element : bA) {
            if(p.test(element)) {
				j++;
			}
        }
        return j;
    }
    
    public void testShuffleSequences() {
        for (int trial = 0; trial < 100; trial++) {
            final byte[][] bAA = TrawlerToolsTest.randomStrings();
            final byte[] bA = TrawlerToolsTest.getString(bAA);
            final byte end = bA.length > 0 ? bA[bA.length-1] : 0;
            final Predicate_Int p =  new Predicate_Int() {
                public boolean test(int i) {
                    return (i | 15) == 15;
                }
            };
            int j=0;
            for(int i=0; i<bA.length-1; i++) {
                if(p.test(bA[i])) {
					j++;
				}
            }
            final int fragmentSize = 1 + (int)(Math.random() * 100);
            TrawlerStatistics.shuffleSequences(bA, fragmentSize);
            int k=0;
            for (final byte element : bA) {
                if(p.test(element)) {
					k++;
				}
            }
            if(bA.length > 0) {
				Assert.assertEquals(bA[bA.length-1], end);
			}
            Assert.assertEquals(j, k);
        }
    }
    
    public void testGetMaxes() {
        for (int trial = 0; trial < 100; trial++) {
            final float[] rA = new float[100];
            Arrays.fill(rA, 0.5f);
            final SortedSet<Float> sS = new TreeSet<Float>();
            final int maxNumber = (int)(1 + Math.random()*100);
            final TrawlerTools.PassOut passOut = TrawlerStatistics.getMaxes(rA, sS, maxNumber);
            final SortedSet<Float> sS2 = new TreeSet<Float>();
            while(Math.random() > 0.001) {
                final int i = (int)(Math.random() * 20);
                final int j = (int)(Math.random() * 20);
                final int k = (int)(Math.random() * 20);
                final float z = TrawlerStatistics.zScore(i, j, k, rA);
                if(Float.isNaN(z)) {
					continue;
				}
                if((sS2.size() < maxNumber) || (z > sS2.first().floatValue())) {
                    if(sS2.size() >= maxNumber) {
                        sS2.remove(sS2.first());
                    }
                    sS2.add(new Float(z));
                }
                passOut.pro(i, j, k, new byte[10]);
            }
            Assert.assertTrue(sS.equals(sS2));
        }
    }
    
    public void testGetMins() {
        for (int trial = 0; trial < 100; trial++) {
            final float[] rA = new float[100];
            Arrays.fill(rA, 0.5f);
            final SortedSet<Float> sS = new TreeSet<Float>();
            final int maxNumber = (int)(1 + Math.random()*100);
            final TrawlerTools.PassOut passOut = TrawlerStatistics.getMins(rA, sS, maxNumber);
            final SortedSet<Float> sS2 = new TreeSet<Float>();
            while(Math.random() > 0.001) {
                final int i = (int)(Math.random() * 20);
                final int j = (int)(Math.random() * 20);
                final int k = (int)(Math.random() * 20);
                final float z = TrawlerStatistics.zScore(i, j, k, rA);
                if(Float.isNaN(z)) {
					continue;
				}
                if((sS2.size() < maxNumber) || (z < sS2.last().floatValue())) {
                    if(sS2.size() >= maxNumber) {
                        sS2.remove(sS2.last());
                    }
                    sS2.add(new Float(z));
                }
                passOut.pro(i, j, k, new byte[10]);
            }
            Assert.assertTrue(sS.equals(sS2));
        }
    }
}
