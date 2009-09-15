/*
 * Created on Feb 28, 2005
 */
package bp.common.ds;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import bp.common.fp.Generator_Int;
import bp.common.fp.Procedure_Int;
import bp.common.io.Debug;

/**
 * Stack containing ints.
 * 
 * @author benedictpaten
 */
public final class IntStack implements Procedure_Int, Generator_Int {
    
    static final Logger logger = Logger.getLogger(IntStack.class
            .getName());

    List<int[]> chunks;

    int[] iA;

    int i, j, chunkSize;

    /**
     * @param chunkSize
     *            size of stack chunks, must be greater than zero or an
     *            {@link IllegalArgumentException}will be thrown.
     */
    public IntStack(final int chunkSize) {
        this.chunks = new ArrayList<int[]>(10);
        if (Debug.DEBUGCODE && (chunkSize == 0)) {
			throw new IllegalArgumentException();
		}
        this.iA = new int[chunkSize];
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
    public final void stuff(final int l) {
        if (this.i < this.iA.length) {
			this.iA[this.i++] = l;
		} else {
            this.i = 1;
            if (++this.j < this.chunks.size()) {
				this.iA = this.chunks.get(this.j);
			} else {
                this.iA = new int[this.chunkSize];
                IntStack.logger.info(" Adding new chunk " + this.chunkSize);
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
    public final int unstuff() {
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

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.fp.Procedure_Int#pro(int)
     */
    public final void pro(final int i) {
        this.stuff(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.fp.Function_Int#polygonClipper(int)
     */
    public final int gen() {
        return !this.empty() ? this.unstuff() : Integer.MAX_VALUE;
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