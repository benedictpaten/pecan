/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 28, 2005
 */
package bp.common.ds;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import bp.common.io.Debug;

/**
 * Stack containing float. To avoid cost of int ==> float conversion
 * 
 * @author benedictpaten
 */
public final class FloatStack {
    
    static final Logger logger = Logger.getLogger(FloatStack.class
            .getName());

    List<float[]> chunks;

    float[] iA;

    int i, j, chunkSize;

    /**
     * @param chunkSize
     *            size of stack chunks, must be greater than zero or an
     *            {@link IllegalArgumentException}will be thrown.
     */
    public FloatStack(final int chunkSize) {
        this.chunks = new ArrayList<float[]>(10);
        if (Debug.DEBUGCODE && (chunkSize == 0)) {
			throw new IllegalArgumentException();
		}
        this.iA = new float[chunkSize];
        this.chunks.add(this.iA);
        this.chunkSize = chunkSize;
        this.i = 0;
        this.j = 0;
    }

    /**
     * Add word to stack.
     * 
     * @param l
     */
    public final void stuff(final float l) {
        if (this.i < this.iA.length) {
			this.iA[this.i++] = l;
		} else {
            this.i = 1;
            if (++this.j < this.chunks.size()) {
				this.iA = this.chunks.get(this.j);
			} else {
                this.iA = new float[this.chunkSize];
                FloatStack.logger.info(" Adding new float chunk " + this.chunkSize);
                this.chunks.add(this.iA);
            }
            this.iA[0] = l;
        }
    }

    /**
     * Function to ints from stack
     * 
     * @return
     */
    public final float unstuff() {
        if (this.i > 0) {
			return this.iA[--this.i];
		}
        if (this.j > 0) {
            this.j--;
            this.iA = this.chunks.get(this.j);
            this.i = this.iA.length - 1;
            return this.iA[this.i];
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Reset the stuffer
     *  
     */
    public final void reset() {
        this.i = 0;
        this.j = 0;
        this.iA = this.chunks.get(0);
    }

    /**
     * Indicates if the stack is empty.
     * 
     * @return
     */
    public final boolean empty() {
        return (this.i == 0) && (this.j == 0);
    }

    /**
     * Location on stack from bottom. (exclusive)
     * 
     * @return
     */
    public final int getMark() {
        return this.j * this.chunkSize + this.i;
    }

    /**
     * Set stack to given location (exclusive).
     * 
     * @param l
     */
    public final void reset(final int l) {
        this.i = l % this.chunkSize;
        this.j = l / this.chunkSize;
        this.iA = this.chunks.get(this.j);
    }
}