/*
 * Created on Feb 11, 2005
 */
package bp.pecan.dimensions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author benedictpaten
 */
public class DimensionIterator implements Iterator {
    Dimension d;
    int i;
    
    /**
     * 
     */
    public DimensionIterator(final Dimension d) {
        this.d = d;
        this.i = 0;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return this.i < this.d.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
        if(this.i >= this.d.size()) {
			throw new NoSuchElementException();
		}
        final int[] iA = new int[this.d.subDimensionsNumber()];
        this.d.get(this.i++, 0, iA);
        return iA;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
