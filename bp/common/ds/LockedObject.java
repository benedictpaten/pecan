/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Aug 18, 2005
 */
package bp.common.ds;

/**
 * @author benedictpaten
 */
public class LockedObject {
    private final Object o;
    private boolean locked;

    /**
     * 
     */
    public LockedObject(final Object o) {
        this.o = o;
        this.locked = false;
    }
    
    public final Object get() {
        if(this.locked) {
			throw new IllegalStateException();
		} else {
            this.locked = true;
            return this.o;
        }
    }
    
    public final void release() {
        if(!this.locked) {
			throw new IllegalStateException();
		}
        this.locked = false;
    }

}
