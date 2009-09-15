/*
 * Created on Dec 15, 2005
 */
package bp.trawler;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.SuffixTree;

public class TrawlerToolsTest
                             extends TestCase {
    
    public void testScanMotifs() {
        final byte[] bA = new byte[] { 1, 8, 2, 1, 8, 2, 2, 4, 4, 4, 1, 2,
                8, 1, 16, 8, 8, 4, 1, 1, 1, 16 };
        final byte[] bA2 = new byte[] { 1, 8, 2, 1, 8, 2, 2, 4, 4, 4, 1, 2,
                8, 1, 16, 8, 8, 4, 1, 1, 1, 16 };
        final String s = new String(bA);
        final String s2 = new String(bA2);
        final SuffixTree sT = new SuffixTree(bA, 25, false, (byte) 16);
        final SuffixTree sT2 = new SuffixTree(bA2, 25, false, (byte) 16);
        final int[] counts = new int[bA.length];
        TrawlerTools.parseCounts(0, counts, sT);
        final int[] counts2 = new int[bA2.length];
        TrawlerTools.parseCounts(0, counts2, sT2);
        TrawlerTools.scan(0, 1, 0, 1, 0, 0, new int[1000],
                new int[1000], new byte[] { 1, 2, 4, 8, 15 },
                new byte[1000], 2, sT, sT2, counts, counts2,
                new TrawlerTools.PassOut() {
                    public void pro(final int i, final int j, final int depth,
                            final byte[] string) {
                        final byte[] bA = new byte[depth];
                        System.arraycopy(string, 0, bA, 0, depth);
                        //assertFalse(bA[0] == 15);
                        Assert.assertFalse(depth > 5);
                        Assert.assertFalse(bA[bA.length-1] == 15);
                        Assert.assertEquals(TrawlerToolsTest.this.count(s, new String(bA)), i);
                        Assert.assertEquals(TrawlerToolsTest.this.count(s2, new String(bA)), j);
                        Assert.assertTrue(i >= 2);
                    }
                }, new int[] { 0, 0, 0, 0, 2 }, 15, 5, 2);
    }

    public static byte[][] randomStrings() {
        final byte[][] bAA = new byte[(int) (Math.random() * 10)][];
        for (int i = 0; i < bAA.length; i++) {
            final byte[] bA = new byte[(int) (Math.random() * 100)];
            for (int j = 0; j < bA.length; j++) {
				bA[j] = new byte[] { 1, 2, 4, 8 }[(int) (Math
                        .random() * 4)];
			}
            bAA[i] = bA;
        }
        return bAA;
    }

    public static byte[] getString(final byte[][] bAA) {
        int k = 0;
        for (final byte[] element : bAA) {
			k += element.length;
		}
        final byte[] bA = new byte[k + bAA.length];
        k = 0;
        for (final byte[] bA2 : bAA) {
            for (final byte element0 : bA2) {
                bA[k++] = element0;
            }
            bA[k++] = 16;
        }
        return bA;
    }

    public byte[] getRandomString() {
        final byte[] bA = new byte[(int) (Math.random() * 12)];
        for (int i = 0; i < bA.length; i++) {
			bA[i] = new byte[] { 1, 2, 4, 8 }[(int) (Math.random() * 4)];
		}
        return bA;
    }
    
    public byte[] getRandomRedundantString() {
        final byte[] bA = new byte[(int) (Math.random() * 12)];
        for (int i = 0; i < bA.length; i++) {
			bA[i] = new byte[] { 1, 2, 4, 8, 3, 5, 9, 6, 10, 12, 15 }[(int) (Math.random() * 11)];
		}
        return bA;
    }

    int count(final String s, final String s2) {
        int j = 0;
        if((s2.length() == 0)) {
            return s.length();
        }
        final byte[] bA = s2.getBytes();
        for (int i = 0; i + s2.length() <= s.length(); i++) {
            final String s3 = s.substring(i, i + s2.length());
            final byte[] bA2 = s3.getBytes();
            source:
            {
                for(int k=0; k<bA2.length; k++) {
					if((bA2[k] | bA[k]) != bA[k]) {
						break source;
					}
				}
                j++;
            }
        }
        return j;
    }

    public void testScan() {
        final int[] iA = new int[10000];
        for (int trial = 0; trial < 100; trial++) {
            final byte[][] bAA = TrawlerToolsTest.randomStrings();
            final byte[] bA = TrawlerToolsTest.getString(bAA);
            final String s = new String(bA);
            if(s.length() == 0) {
				continue;
			}
            final SuffixTree sT = new SuffixTree(bA, 15, false, (byte) 16);
            final int[] counts = new int[bA.length];
            TrawlerTools.parseCounts(0, counts, sT);
            while (Math.random() > 0.01) {
                final byte[] bA2 = this.getRandomRedundantString();
                int start = 0;
                int end = 1;
                iA[0] = 0;
                final int c = this.count(s, new String(bA2));
                for (int i = 0; i < bA2.length; i++) {
                    final int j = TrawlerTools.scan(iA, start, end, sT, i,
                            bA2[i]);
                    start = end;
                    end = j;
                }
                final int c2 = TrawlerTools.getCount(start, end, iA,
                        counts, sT);
                Assert.assertEquals(c, c2);
            }
        }
    }

    public void testCount() {
        for (int trial = 0; trial < 100; trial++) {
            final byte[][] bAA = TrawlerToolsTest.randomStrings();
            final byte[] bA = TrawlerToolsTest.getString(bAA);
            final String s = new String(bA);
            if(s.length() == 0) {
				continue;
			}
            final SuffixTree sT = new SuffixTree(bA, 100, false, (byte) 16);
            final int[] counts = new int[bA.length];
            TrawlerTools.parseCounts(0, counts, sT);
            while (Math.random() > 0.01) {
                final byte[] bA2 = this.getRandomString();
                    final int c = this.count(s, new String(bA2));
                    if (c > 0) {
                        final int n = this.preScan(0, 0, bA2, sT);
                        final int c2 = TrawlerTools.getCount(n, counts, sT);
                        if(c != c2) {
                                Assert.fail();
                        }
                    }
            }
        }
    }
    
    int preScan(int n, int d, final byte[] bA, final SuffixTree sT) {
        while(d < bA.length) {
            n = sT.getChild(n, bA[d]);
            if(sT.isLeaf(n)) {
				return n;
			}
            d = sT.depth(n);
        }
        return n;
    }


}
