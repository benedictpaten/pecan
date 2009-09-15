/*
 * Created on Dec 16, 2005
 */
package bp.trawler;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

import bp.common.fp.Predicate_Int;
import bp.common.io.Debug;

public class TrawlerStatistics {
    static final Logger logger = Logger.getLogger(Trawler.class
            .getName());

    static final float zScore(final int observed, final int background,
            final int length, final float[] rA) {
        final float r = rA[length];
        final float e = background * r;
        final float v = (float) Math.sqrt(e * (1.0f - r));
        return (observed - e) / v;
    }

    static final int[] buckets(final byte[] strings, final int maxLength,
            final Predicate_Int inAlphabet) {
        final int[] iA = new int[maxLength + 1];
        iA[0] = Integer.MAX_VALUE;
        for (int i = 0; i < strings.length;) {
            int j = i;
            while ((j < strings.length) && inAlphabet.test(strings[j])) {
				j++;
			}
            j -= i;
            for (int k = 1; (k <= j) && (k < iA.length); k++) {
                iA[k] += j - k + 1;
            }
            i += j + 1;
        }
        return iA;
    }
    
    static final void replaceSequences(final byte[] strings, final byte[] background,
            final Predicate_Int inAlphabet) {
        int k = 0;
        for (int i = 0; i < strings.length; i++) {
            if(inAlphabet.test(strings[i])) {
                while((k < background.length) && !inAlphabet.test(background[k])) {
                    k++;
                }
                if(k >= background.length) {
                    throw new IllegalStateException(" ran out of back ground sequence for shuffling ");
                }
                strings[i] = background[k++];
            }
        }
    }
    
    static final void shuffleSequences(final byte[] background, final int fragmentSize) {
        if(Debug.DEBUGCODE && (fragmentSize == 0)) {
			throw new IllegalStateException();
		}
        final List<byte[]> l = new LinkedList<byte[]>();
        for(int i=0; i<background.length-1;) {
                final byte[] bA = new byte[i + fragmentSize < background.length-1 ? fragmentSize : background.length-1 - i];
                System.arraycopy(background, i, bA, 0, bA.length);
                l.add(bA);
                i += bA.length;
        }
        Collections.shuffle(l);
        int i=0;
        for(final Iterator<byte[]> it=l.iterator(); it.hasNext();) {
            final byte[] bA = it.next();
            System.arraycopy(bA, 0, background, i, bA.length);
            i += bA.length;
        }
    }
    
    public static TrawlerTools.PassOut getMaxes(final float[] rA, final SortedSet<Float> sS, final int maxSize) {
        return new TrawlerTools.PassOut() {
            float min = sS.size() != 0 ? sS.first().floatValue() : Float.NEGATIVE_INFINITY;
            {
                if(Debug.DEBUGCODE && (maxSize == 0)) {
					throw new IllegalStateException(" Can't use so few mins ");
				}
            }
            
            public final void pro(final int i, final int j, final int depth,
                    final byte[] string) {
               final float z = TrawlerStatistics.zScore(i, j, depth, rA);
               if(Float.isNaN(z)) {
				return;
			}
               if(z > this.min) {
                   if(sS.size() >= maxSize) {
					sS.remove(sS.first());
				}
                   sS.add(new Float(z));
                   this.min = sS.first().floatValue();
               }
               else
                   if(sS.size() < maxSize) {
                       sS.add(new Float(z));
                       this.min = z;
                   }
            }
        };
    }
    
    public static TrawlerTools.PassOut getMins(final float[] rA, final SortedSet<Float> sS, final int maxSize) {
        return new TrawlerTools.PassOut() {
            float max = sS.size() != 0 ? sS.last().floatValue() : Float.POSITIVE_INFINITY;
            {
                if(Debug.DEBUGCODE && (maxSize == 0)) {
					throw new IllegalStateException(" Can't use so few mins ");
				}
            }
            
            public final void pro(final int i, final int j, final int depth,
                    final byte[] string) {
               final float z = TrawlerStatistics.zScore(i, j, depth, rA);
               if(Float.isNaN(z)) {
				return;
			}
               if(z < this.max) {
                   if(sS.size() >= maxSize) {
					sS.remove(sS.last());
				}
                   sS.add(new Float(z));
                   this.max = sS.last().floatValue();
               }
               else
                   if(sS.size() < maxSize) {
                       sS.add(new Float(z));
                       this.max = z;
                   }
            }
        };
    }
}
