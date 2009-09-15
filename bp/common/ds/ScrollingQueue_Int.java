/*
 * Created on Mar 21, 2005
 */
package bp.common.ds;

import java.util.logging.Logger;

import bp.common.fp.Function_Int;
import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public final class ScrollingQueue_Int implements Function_Int {
    
    static final Logger logger = Logger.getLogger(ScrollingQueue_Int.class
            .getName());
    
    private int[] iA;

    private int firstIndex;
    
    private int lastIndex;
    
    private int mask;
    
    private final int initialSize;
    
    private static final int DECREASE_MULTIPLE = 4;
    
    /**
     *  Guranteed to be at least as big as initial size
     * @param firstIndex TODO
     * @param descreaseDynamically TODO
     */
    public ScrollingQueue_Int(int initialSize, final int firstIndex, final boolean descreaseDynamically) {
        initialSize--;
        this.mask = 1;
        while((initialSize & this.mask) < initialSize) {
			this.mask = this.mask*2 + 1;
		}
        this.iA = new int[this.mask+1];
        this.firstIndex = firstIndex;
        this.lastIndex = this.firstIndex;
        this.initialSize = this.iA.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.ds.Queue#add(java.lang.Object)
     */
    public final void add(final int i) {
        if (this.size() < this.iA.length) {
            this.iA[this.lastIndex++ & this.mask] = i;
        }
        else {
            ScrollingQueue_Int.logger.info(" Increasing size of ScrollingQueue_Int array from " + this.iA.length + " for " + this);
            //new IllegalStateException().printStackTrace();
            final int j = this.mask * 2 + 1;
            final int[] iA2 = new int[j + 1];
            this.copyToNewArray(iA2, j);
            this.add(i);
        }
    }
    
    final void copyToNewArray(final int[] iA2, final int newMask) {
        final int k = (this.firstIndex & this.mask);
        final int l = this.size();
        if(k + l <= this.iA.length) {
            this.copyChunkToNewArray(iA2, newMask, this.firstIndex, l);
        }
        else {
            final int m = this.iA.length-k;
            this.copyChunkToNewArray(iA2, newMask, this.firstIndex, m);
            this.copyChunkToNewArray(iA2, newMask, this.firstIndex + m, l-m);
        }
        this.iA = iA2;
        this.mask = newMask;
    }
    
    final void copyChunkToNewArray(final int[] iA2, final int newMask, final int start, final int length) {
        final int k = (start & this.mask);
        final int l = (start & newMask);
        if(l + length > iA2.length) {
            final int m = iA2.length - l;
            System.arraycopy(this.iA, k, iA2, l, m);
            System.arraycopy(this.iA, k+m, iA2, 0, length - m);
        }
        else {
            System.arraycopy(this.iA, k, iA2, l, length);
        }
    }
    
    public final int fn(final int i) {
        if(Debug.DEBUGCODE && ((i >= this.lastIndex) || (i < this.firstIndex))) {
			throw new ArrayIndexOutOfBoundsException(i + " " + this.lastIndex() + " " + this.firstIndex());
		}
        return this.iA[i & this.mask];
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.ds.Queue#get(int)
     */
    public final int get(final int i) {
        if(Debug.DEBUGCODE && ((i >= this.lastIndex) || (i < this.firstIndex))) {
			throw new ArrayIndexOutOfBoundsException(i + " " + this.lastIndex() + " " + this.firstIndex());
		}
        return this.iA[i & this.mask];
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.ds.Queue#removeFirst()
     */
    public final void removeFirst() {
        if(Debug.DEBUGCODE && (this.firstIndex == this.lastIndex)) {
			throw new ArrayIndexOutOfBoundsException();
		}
        this.firstIndex++;
        if((this.iA.length > this.initialSize) && (this.iA.length > this.size()*ScrollingQueue_Int.DECREASE_MULTIPLE)) {
            ScrollingQueue_Int.logger.info(" Decreasing size of ScrollingQueue_Int array from " + this.iA.length + " for " + this);
            final int j = (this.mask - 1) / 2;
            final int[] iA2 = new int[j + 1];
            this.copyToNewArray(iA2, j);
            this.iA = iA2;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.ds.Queue#set(int, java.lang.Object)
     */
    public final void set(final int i, final int j) {
        if(Debug.DEBUGCODE && ((i >= this.lastIndex) || (i < this.firstIndex))) {
			throw new ArrayIndexOutOfBoundsException();
		}
        this.iA[i & this.mask] = j;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.ds.Queue#size()
     */
    public final int size() {
        return this.lastIndex - this.firstIndex;
    }
    
    public final int firstIndex() {
        return this.firstIndex;
    }
    
    public final int lastIndex() {
        return this.lastIndex;
    }

}