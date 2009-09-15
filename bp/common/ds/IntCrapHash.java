/*
 * Created on Apr 1, 2006
 */
package bp.common.ds;

import java.util.Arrays;

import bp.common.fp.Function_Int_2Args;

public final class IntCrapHash {
    
    int[] hash;
    int[] entries; 
    int[] bP;
    int eI = 0;
    
    int mask;

    /*
     * Uses rubbish has function, but appropriate for chains of near, irregularly spaced integers
     */
    public IntCrapHash(int initialSize) {
        initialSize--;
        this.mask = 1;
        while((initialSize & this.mask) < initialSize) {
			this.mask = this.mask*2 + 1;
		}
        this.hash = new int[this.mask+1];
        Arrays.fill(this.hash, Integer.MAX_VALUE);
        this.entries = new int[this.hash.length*2];
        this.bP = new int[this.hash.length];
    }
    
    private final int getHash(final int i) {
        return i & this.mask;
    }
    
    public final void put(final int i, final int j, final Function_Int_2Args fn) {
        if(this.eI >= this.entries.length) {
			this.resize();
		}
        int l = this.getHash(i);
        int m = this.hash[l];
        while(true) {
            if(m == Integer.MAX_VALUE) {
                this.hash[l] = this.eI;
                this.entries[this.eI] = i;
                this.entries[this.eI+1] = j;
                this.bP[this.eI/2] = l;
                this.eI += 2;
                return;
            }
            if(this.entries[m] == i) {
                this.entries[m+1] = fn.fn(this.entries[m+1], j);
                return;
            }
            l = l+1 < this.hash.length ? l+1 : 0;
            m = this.hash[l];
        }
    }
    
    public final int size() {
        return this.eI/2;
    }
    
    public final int getEntries(final int[] iA) {
        System.arraycopy(this.entries, 0, iA, 0, this.eI);
        return this.eI;
    }
    
    public final void clear() {
        final int i = this.eI/2;
        for(int j=0; j<i; j++) {
			this.hash[this.bP[j]] = Integer.MAX_VALUE;
		} 
        this.eI = 0;
    }
    
    private final void resize() {
        final IntCrapHash iH = new IntCrapHash(this.hash.length*2 + 1);
        for(int i=0; i<this.eI; i+=2) {
			iH.put(this.entries[i], this.entries[i+1], null);
		}
        this.hash = iH.hash;
        this.entries = iH.entries;
        this.bP = iH.bP;
        this.eI = iH.eI;
        this.mask = iH.mask;
    }

}
