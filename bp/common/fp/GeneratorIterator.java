/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 16, 2005
 */
package bp.common.fp;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A iterator that takes a generator object and iterates through the generator
 * until null is returned.
 * 
 * @author benedictpaten
 */
public class GeneratorIterator implements Iterator {
    Generator gen;

    Object o;

    boolean notChecked = true;
    
    /**
     *  
     */
    public GeneratorIterator(final Generator gen) {
        this.gen = gen;
        this.o = gen.gen();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        //if(notChecked) {
        //    o = gen.gen();
        //    notChecked = false;
        //}
        return this.o != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    public Object next() {
        //if(notChecked) {
        //    o = gen.gen();
        //    notChecked = false;
        //}
        if (this.o == null) {
			throw new NoSuchElementException();
		}
        final Object rV = this.o;
        this.o = this.gen.gen();
        //o = null;
        //notChecked = true;
        return rV;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}