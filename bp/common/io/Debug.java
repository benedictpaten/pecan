/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * DBAAlignmentParser.java
 *
 * Created on 29 November 2002, 11:35
 */
package bp.common.io;

/**
 * 
 * @author ben
 */
public final class Debug {
    public static boolean ON = true;

    public static boolean STOUT = false;

    public static final boolean DEBUGCODE = true;

    public static void pl(final String s) {
        if (Debug.ON) {
			if (Debug.STOUT) {
				System.out.println(s);
			} else {
				System.err.println(s);
			}
		}
    }

    public static void p(final String s) {
        if (Debug.ON) {
			if (Debug.STOUT) {
				System.out.print(s);
			} else {
				System.err.print(s);
			}
		}
    }

    /**
     * This output if reserved for permanent debug output
     */
    public static class P {

        public static void pl(final String s) {
            if (Debug.ON) {
				System.err.println(s);
			}
        }

        public static void p(final String s) {
            if (Debug.ON) {
				System.err.print(s);
			}
        }
    }
}