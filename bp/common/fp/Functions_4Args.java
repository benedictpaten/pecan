/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 7, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public class Functions_4Args {

    /**
     * Concatenates the string arguments together. Useful for testing.
     * @return the concatenated strings as o + o2 + o3 + o4
     */
    	public static final Function_4Args concatenate() {
    	    return new Function_4Args() {
    	        /* (non-Javadoc)
                 * @see bp.common.fp.Function_4Args#polygonClipper(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
                 */
                public Object fn(final Object o, final Object o2, final Object o3, final Object o4) {
                    return ((String)o) + ((String)o2) + ((String)o3) + ((String)o4);
                }
    	    };
    	}
}
