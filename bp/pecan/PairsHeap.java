/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 9, 2006
 */
package bp.pecan;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Logger;

import bp.common.ds.ScrollingQueue_Int;
import bp.common.io.Debug;

final class PairsHeap {
    static final Logger logger = Logger.getLogger(PairsHeap.class
            .getName());

    private static IntBuffer iB;

    private static float expansionRate;

    private static int stackPointer;

    private static boolean useDirectBuffers;

    private static int used;
    
    private static int maxSize = 0;
    
    public static long totalUsed12 = 0;

    private final ScrollingQueue_Int sQI;

    private PairsHeap partner;

    final boolean weightPointers;

    private int finishedUpto;

    private PairsHeap(final int firstIndex, final int startLength,
            final boolean weightPointers) {
        this.sQI = new ScrollingQueue_Int(startLength, firstIndex, true);
        this.weightPointers = weightPointers;
    }

    public static void initialise(int size, final float expansionRate2,
            final boolean useDirectBuffers2) {
        if (Debug.DEBUGCODE && (size < 12)) {
			throw new IllegalStateException();
		}
        size -= size % 12;
        if (Debug.DEBUGCODE && (expansionRate2 <= 1.0)) {
			throw new IllegalStateException();
		}
        PairsHeap.logger.info(" Initialising pairs heap to capacity (bytes) : "
                + size);
        PairsHeap.expansionRate = expansionRate2;
        PairsHeap.useDirectBuffers = useDirectBuffers2;
        PairsHeap.iB = PairsHeap.getBuffer(size);
        PairsHeap.iB.put(0, Integer.MAX_VALUE);
        PairsHeap.stackPointer = 0;
        PairsHeap.used = 0;
    }

    final private static IntBuffer getBuffer(final int size) {
        try {
            return PairsHeap.useDirectBuffers ? ByteBuffer.allocateDirect(size)
                    .asIntBuffer() : ByteBuffer.allocate(size)
                    .asIntBuffer();
        } catch (final OutOfMemoryError e) {
            PairsHeap.logger
                    .info("Caught out of memory exception for pairs heap, calling the gc and trying to allocate non-directly");
            System.gc();
            return ByteBuffer.allocate(size).asIntBuffer();
        }
    }

    public static PairsHeap[] getPair(final int firstIndexI,
            final int firstIndexJ, final int startLength) {
        final PairsHeap pH = new PairsHeap(firstIndexI, startLength, false);
        final PairsHeap pH2 = new PairsHeap(firstIndexJ, startLength, true);
        pH.partner = pH2;
        pH2.partner = pH;
        return new PairsHeap[] { pH, pH2 };
    }

    final private static void enlarge() {
        PairsHeap.logger.info(" Calling enlarge with iB capacity (bytes) " + 4
                * PairsHeap.iB.capacity() + " " + PairsHeap.expansionRate);
        final int i = PairsHeap.iB.capacity();
        int j = 3 + (int) (i * PairsHeap.expansionRate);
        j += 3 - (j % 3);
        final IntBuffer iB2 = PairsHeap.getBuffer(j * 4);
        PairsHeap.iB.clear();
        iB2.put(PairsHeap.iB);
        iB2.put(i, Integer.MAX_VALUE);
        Librarian.logger.info("Expanded to iB " + 4 * PairsHeap.iB.capacity());
        PairsHeap.iB = iB2;
    }

    final public int firstIndex() {
        return this.sQI.firstIndex();
    }

    final public int lastActualIndex() {
        return this.sQI.lastIndex();
    }

    final public void tryToRemoveUpto(final int i) {
        if (this.weightPointers) {
            while (this.sQI.firstIndex() < i) {
				this.releaseFirst();
			}
            this.partner.tryToRemoveUpto(this.partner.finishedUpto);
            return;
        }
        this.finishedUpto = i;
        int j = this.sQI.firstIndex();
        while (j < i) {
            final int k = this.getRightmostPoint(j);
            if ((k == Integer.MAX_VALUE) || (k < this.partner.firstIndex())) {
				this.releaseFirst();
			} else {
				return;
			}
            j = this.sQI.firstIndex();
        }
    }

    final private void releaseFirst() {
        final int j = this.sQI.firstIndex();
        if (this.sQI.lastIndex() == j) {
			this.sQI.add(Integer.MAX_VALUE);
		}
        final int i = this.sQI.get(j);
        this.sQI.removeFirst();
        PairsHeap.release(i);
    }

    final static private void release(final int i) {
        if (i != Integer.MAX_VALUE) {
            PairsHeap.used--;
            int j = i;
            while (true) {
                final int k = PairsHeap.iB.get(j);
                if (k != Integer.MAX_VALUE) {
					j = k;
				} else {
					break;
				}
                PairsHeap.used--;
            }
            PairsHeap.iB.put(j, PairsHeap.stackPointer);
            PairsHeap.stackPointer = i;
        }
    }

    final static public int used() {
        return PairsHeap.used;
    }

    final static public int size() {
        return PairsHeap.iB.capacity() / 3;
    }
    
    public final static void report() {
        //if(Debug.DEBUGCODE) 
        //    logger.info(" Max used weights : " + maxSize + " , currently used : " + used);
    	logger.info("Max used weights : " + PairsHeap.maxSize + " , currently used : " + PairsHeap.used + " " + PairsHeap.totalUsed12);
    }

    final static private int getFree() {
        PairsHeap.totalUsed12++;
        PairsHeap.used++;
        if(Debug.DEBUGCODE && (PairsHeap.used > PairsHeap.maxSize)) {
			PairsHeap.maxSize = PairsHeap.used;
		}
        final int i = PairsHeap.stackPointer;
        PairsHeap.stackPointer = PairsHeap.iB.get(PairsHeap.stackPointer);
        if (PairsHeap.stackPointer == Integer.MAX_VALUE) {
            if (i + 3 < PairsHeap.iB.capacity()) {
                PairsHeap.stackPointer = i + 3;
                PairsHeap.iB.put(i + 3, Integer.MAX_VALUE);
            } else {
                PairsHeap.stackPointer = PairsHeap.iB.capacity();
                PairsHeap.enlarge();
            }
        }
        return i;
    }

    final int getHeapIndex(final int i) {
        return i < this.sQI.lastIndex() ? this.sQI.get(i) : Integer.MAX_VALUE;
    }

    final void setHeapIndex(final int i, final int j) {
        if (i < this.sQI.lastIndex()) {
            this.sQI.set(i, j);
        } else {
            do {
                this.sQI.add(Integer.MAX_VALUE);
            } while (this.sQI.lastIndex() <= i);
            this.sQI.set(i, j);
        }
    }

    final public int getRightmostPoint(int i) {
        i = this.getHeapIndex(i);
        if (i == Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
        return PairsHeap.iB.get(i + 1);
    }
    
    final public int getLeftmostPoint(int i) {
        i = this.getHeapIndex(i);
        if (i == Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
        int k = PairsHeap.iB.get(i+1);
        while(true) {
            i = PairsHeap.iB.get(i);
            if(i == Integer.MAX_VALUE) {
				return k;
			}
            k = PairsHeap.iB.get(i+1);
        }
    }
    
    final public int getWeight(int i, final int j, final int nullValue) {
        i = this.getHeapIndex(i);
        if (i != Integer.MAX_VALUE) {
            int k;
            while((k = PairsHeap.iB.get(i+1)) > j) {
                i = PairsHeap.iB.get(i);
                if(i == Integer.MAX_VALUE) {
					return nullValue;
				}
            }
            if(k == j) {
                return PairsHeap.iB.get(this.weightPointers ? PairsHeap.iB.get(i + 2) : i+2);
            }
        }
        return nullValue;
    }

    final public int get(int i, int iAIndex, final int[] iA) {
        if (this.weightPointers) {
            i = this.getHeapIndex(i);
            while (i != Integer.MAX_VALUE) {
                iA[iAIndex] = PairsHeap.iB.get(i + 1);
                iA[iAIndex + 1] = PairsHeap.iB.get(PairsHeap.iB.get(i + 2));
                iAIndex += 2;
                i = PairsHeap.iB.get(i);
            }
            return iAIndex;
        }
        i = this.getHeapIndex(i);
        while (i != Integer.MAX_VALUE) {
            iA[iAIndex] = PairsHeap.iB.get(i + 1);
            iA[iAIndex + 1] = PairsHeap.iB.get(i + 2);
            iAIndex += 2;
            i = PairsHeap.iB.get(i);
        }
        return iAIndex;
    }

    final public int sumTotal(int i, final int nothing) {
        if (this.weightPointers) {
            i = this.getHeapIndex(i);
            if (i != Integer.MAX_VALUE) {
                int total = PairsHeap.iB.get(PairsHeap.iB.get(i + 2));
                i = PairsHeap.iB.get(i);
                while (i != Integer.MAX_VALUE) {
                    total = Librarian.sum(total, PairsHeap.iB
                            .get(PairsHeap.iB.get(i + 2)));
                    i = PairsHeap.iB.get(i);
                }
                return total;
            }
            return nothing;
        }
        i = this.getHeapIndex(i);
        if (i != Integer.MAX_VALUE) {
            int total = PairsHeap.iB.get(i + 2);
            i = PairsHeap.iB.get(i);
            while (i != Integer.MAX_VALUE) {
                total = Librarian.sum(total, PairsHeap.iB.get(i + 2));
                i = PairsHeap.iB.get(i);
            }
            return total;
        }
        return nothing;
    }

    final private int addJ(final int i, final int j, final int weight) {
        int k = this.getHeapIndex(i);
        if (k == Integer.MAX_VALUE) {
            k = PairsHeap.getFree();
            this.setHeapIndex(i, k);
            PairsHeap.iB.put(k, Integer.MAX_VALUE);
            PairsHeap.iB.put(k + 1, j);
            PairsHeap.iB.put(k + 2, weight);
            return k;
        }
        int l = PairsHeap.iB.get(k + 1);
        if (l < j) {
            final int m = PairsHeap.getFree();
            this.setHeapIndex(i, m);
            PairsHeap.iB.put(m, k);
            PairsHeap.iB.put(m + 1, j);
            PairsHeap.iB.put(m + 2, weight);
            return m;
        }
        int m;
        do {
            m = k;
            k = PairsHeap.iB.get(k);
            if (k == Integer.MAX_VALUE) {
                k = PairsHeap.getFree();
                PairsHeap.iB.put(m, k);
                PairsHeap.iB.put(k, Integer.MAX_VALUE);
                PairsHeap.iB.put(k + 1, j);
                PairsHeap.iB.put(k + 2, weight);
                return k;
            }
            l = PairsHeap.iB.get(k + 1);
        } while (l > j);
        l = PairsHeap.getFree();
        PairsHeap.iB.put(m, l);
        PairsHeap.iB.put(l, k);
        PairsHeap.iB.put(l + 1, j);
        PairsHeap.iB.put(l + 2, weight);
        return l;
    }

    final public int add(final int i, final int j, final int weight) {
        int k = this.getHeapIndex(i);
        if (k == Integer.MAX_VALUE) {
            k = PairsHeap.getFree();
            this.setHeapIndex(i, k);
            PairsHeap.iB.put(k, Integer.MAX_VALUE);
            PairsHeap.iB.put(k + 1, j);
            this.addWeight(i, j, k, weight);
            return k;
        }
        return this.addI(i, j, k, weight);
    }

    final private void addWeight(final int i, final int j, final int k, final int weight) {
        if (this.weightPointers) {
            final int l = this.partner.addJ(j, i, weight) + 2;
            PairsHeap.iB.put(k + 2, l);
            return;
        }
        PairsHeap.iB.put(k + 2, weight);
        this.partner.addJ(j, i, k + 2);
    }

    final private void setWeight(final int k, int weight) {
        if (this.weightPointers) {
            final int l = PairsHeap.iB.get(k);
            weight = Librarian.sum(PairsHeap.iB.get(l), weight);
            PairsHeap.iB.put(l, weight);
            return;
        }
        PairsHeap.iB.put(k, Librarian.sum(PairsHeap.iB.get(k), weight));
    }

    final private int addI(final int i, final int j, int k, final int weight) {
        int l = PairsHeap.iB.get(k + 1);
        if (l < j) {
            final int m = PairsHeap.getFree();
            this.setHeapIndex(i, m);
            PairsHeap.iB.put(m, k);
            PairsHeap.iB.put(m + 1, j);
            this.addWeight(i, j, m, weight);
            return m;
        }
        if (l > j) {
            int m;
            do {
                m = k;
                k = PairsHeap.iB.get(k);
                if (k == Integer.MAX_VALUE) {
                    k = PairsHeap.getFree();
                    PairsHeap.iB.put(m, k);
                    PairsHeap.iB.put(k, Integer.MAX_VALUE);
                    PairsHeap.iB.put(k + 1, j);
                    this.addWeight(i, j, k, weight);
                    return k;
                }
                l = PairsHeap.iB.get(k + 1);
            } while (l > j);
            if (l == j) {
                this.setWeight(k + 2, weight);
                return k;
            }
            l = PairsHeap.getFree();
            PairsHeap.iB.put(m, l);
            PairsHeap.iB.put(l, k);
            PairsHeap.iB.put(l + 1, j);
            this.addWeight(i, j, l, weight);
            return l;
        }
        this.setWeight(k + 2, weight);
        return k;
    }
}