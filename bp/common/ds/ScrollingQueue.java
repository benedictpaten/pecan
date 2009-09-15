/*
 * Created on Mar 21, 2005
 */
package bp.common.ds;

import java.util.logging.Logger;

import bp.common.io.Debug;

/**
 * @author benedictpaten
 */
public class ScrollingQueue {
    
    static final Logger logger = Logger.getLogger(ScrollingQueue.class
            .getName());
    
    private Object[] iA;

    private int firstIndex;

    private int lastIndex;

    private int mask;

    /**
     * Guranteed to be at least as big as initial size
     * 
     * @param firstIndex
     *            TODO
     */
    public ScrollingQueue(int initialSize, final int firstIndex) {
        initialSize--;
        this.mask = 1;
        while ((initialSize & this.mask) < initialSize) {
			this.mask = this.mask * 2 + 1;
		}
        this.iA = new Object[this.mask + 1];
        this.firstIndex = firstIndex;
        this.lastIndex = this.firstIndex;
    }

    public final void add(final Object i) {
        if (this.size() < this.iA.length) {
            this.iA[this.lastIndex++ & this.mask] = i;
        } else {
            ScrollingQueue.logger.info(" Increasing size of ScrollingQueue array from " + this.iA.length + " for " + this);
            final int j = this.mask * 2 + 1;
            final Object[] iA2 = new Object[j + 1];
            final int k = (this.firstIndex & this.mask);
            int l = (this.firstIndex & j);
            if (this.iA.length - k > iA2.length - l) {
                final int m = iA2.length - l;
                System.arraycopy(this.iA, k, iA2, l, m);
                l = this.iA.length - (k + m);
                System.arraycopy(this.iA, k + m, iA2, 0, l);
                System.arraycopy(this.iA, 0, iA2, l, k);
            } else {
                System.arraycopy(this.iA, k, iA2, l, this.iA.length - k);
                l += this.iA.length - k;
                if (l + k < iA2.length) {
					System.arraycopy(this.iA, 0, iA2, l, k);
				} else {
                    System.arraycopy(this.iA, 0, iA2, l, iA2.length - l);
                    System.arraycopy(this.iA, iA2.length - l, iA2, 0, k
                            - (iA2.length - l));
                }
            }
            this.iA = iA2;
            this.mask = j;
            this.add(i);
        }
    }

    public final Object get(final int i) {
        if (Debug.DEBUGCODE && ((i >= this.lastIndex) || (i < this.firstIndex))) {
			throw new ArrayIndexOutOfBoundsException(i + " " + this.lastIndex + " " + this.firstIndex);
		}
        return this.iA[i & this.mask];
    }

    public final Object removeFirst() {
        if (Debug.DEBUGCODE && (this.firstIndex == this.lastIndex)) {
			throw new ArrayIndexOutOfBoundsException();
		}
        final int i = this.firstIndex & this.mask;
        final Object o = this.iA[i];
        this.iA[i] = null;
        this.firstIndex++;
        return o;
    }

    public final void removeUpto(final int i) {
        if ((Debug.DEBUGCODE && (i > this.lastIndex)) || (i < this.firstIndex)) {
			throw new ArrayIndexOutOfBoundsException();
		}
        while (this.firstIndex < i) {
            this.iA[this.firstIndex & this.mask] = null;
            this.firstIndex++;
        }
    }

    public final void set(final int i, final Object j) {
        if (Debug.DEBUGCODE && ((i >= this.lastIndex) || (i < this.firstIndex))) {
			throw new ArrayIndexOutOfBoundsException();
		}
        this.iA[i & this.mask] = j;
    }

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