/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 23, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public final class Functions_Int_2Args {

    /**
     * 
     * @return function to add together two values
     */
    public static final Function_Int_2Args sum() {
        return new Function_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
             */
            public int fn(final int i, final int j) {
                return i + j;
            }
        };
    }

    public static final Function_Int_2Args max() {
        return new Function_Int_2Args() {
            public final int fn(final int i, final int j) {
                return i > j ? i : j;
            }
        };
    }
    
    public static final Function_Int_2Args min() {
        return new Function_Int_2Args() {
            public final int fn(final int i, final int j) {
                return i < j ? i : j;
            }
        };
    }

    
}
