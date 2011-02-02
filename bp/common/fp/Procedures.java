/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Mar 23, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public class Procedures {

    /**
     * Runs a given list of procedures by passing the input argument to each in
     * turn.
     * 
     * @param procedures
     * @return
     */
    public static Procedure runProcedures(final Procedure[] procedures) {
        return new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public void pro(final Object o) {
                for (final Procedure element : procedures) {
                    element.pro(o);
                }
            }
        };
    }

    /**
     * Does nothing.
     * 
     * @return
     */
    public static Procedure doNothing() {
        return new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public void pro(final Object o) {
                ;
            }
        };
    }

}