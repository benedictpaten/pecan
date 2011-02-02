/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Oct 13, 2005
 */
package bp.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import bp.common.fp.Generator_Int;

/**
 * @author benedictpaten
 */
public class FastaParser_Generator_Int implements Generator_Int {
    private final InputStream iS;

    private String currentTitle = null;

    private final int reservedNumber;
    
    private boolean newSequence = true;

    /**
     * @throws FileNotFoundException
     *  
     */
    public FastaParser_Generator_Int(final InputStream iS, final int reservedNumber) {
        this.iS = iS;
        this.reservedNumber = reservedNumber;
        this.gen();
        if(this.currentTitle == null) {
			throw new IllegalStateException();
		}
    }

    private static String getTitle(final InputStream iS) {
        final StringBuffer sB = new StringBuffer();
        int b;
        try {
            while (((b = iS.read()) != '\n') && (b != -1)) {
				sB.append((char) b);
			}
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(sB.toString());
        }
        return sB.toString();
    }

    public final int gen() {
        int i;
        try {
            i = this.iS.read();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
        switch (i) {
        case '\n':
            return this.gen();
        case '>':
            this.newSequence = true;
            this.currentTitle = FastaParser_Generator_Int.getTitle(this.iS);
            return this.reservedNumber;
        case -1:
            this.newSequence = false;
            return this.reservedNumber;
        default:
            return i;
        }
    }

    public final String getFastaID() {
        return this.currentTitle;
    }

    /**
     * 
     * @param s
     * @param reservedValue
     * @param scratchArray
     * @return the length of the read file
     */
    public static int readFile(final FastaParser_Generator_Int fP, final int reservedValue,
            final byte[] scratchArray, int start) {
        int b;
        while ((b = fP.gen()) != reservedValue) {
            scratchArray[start++] = (byte)b;
        }
        return start;
    }
    
    public boolean isNewSequence() {
        final boolean b = this.newSequence;
        this.newSequence = false;
        return b;
    }
}