/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Dec 15, 2005
 */
package bp.trawler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import bp.common.fp.Function_Int;
import bp.common.fp.Predicate_Int;
import bp.common.io.FastaParser_Generator_Int;

public class Alphabet {

    // A C G T [AC]:M [AG]:R [AT]:W [CG]:S [CT]:Y [GT]:K [N]:ACGT
    public final Pattern legalAlphabetPattern = Pattern
            .compile("A?C?G?T?M?R?W?S?Y?K?N?");

    public final byte TERMINAL = 16;

    public final byte WILDCARD = 15;

    public final int[] nucleotideIC = new int[] { Integer.MAX_VALUE,
            0, 0, 1, 0, 1, 1, Integer.MAX_VALUE, 0, 1, 1,
            Integer.MAX_VALUE, 1, Integer.MAX_VALUE, 1, 2 };

    public final Function_Int charToByte =
    // A C G T [AC]:M [AG]:R [AT]:W [CG]:S [CT]:Y [GT]:K [N]:ACGT
    new Function_Int() {
        int[] bA = new int[256];
        {
            Arrays.fill(this.bA, Integer.MAX_VALUE);
            this.bA['A'] = 1;
            this.bA['C'] = 2;
            this.bA['G'] = 4;
            this.bA['T'] = 8;
            this.bA['M'] = 3;
            this.bA['R'] = 5;
            this.bA['W'] = 9;
            this.bA['S'] = 6;
            this.bA['Y'] = 10;
            this.bA['K'] = 12;
            this.bA['N'] = 15;
        }

        public int fn(int x) {
            return this.bA[x];
        }
    };

    public final Predicate_Int validByte = new Predicate_Int() {
        public boolean test(int i) {
            return (i | 15) == 15;
        } 
    };
    
    public final Predicate_Int validChar = new Predicate_Int() {
        public boolean test(int i) {
            return Alphabet.this.validByte.test(Alphabet.this.charToByte.fn(i));
        } 
    };

    public final Function_Int byteToChar =
    // A C G T [AC]:M [AG]:R [AT]:W [CG]:S [CT]:Y [GT]:K [N]:ACGT
    new Function_Int() {
        int[] bA = new int[17];
        {
            this.bA[0] = Integer.MAX_VALUE;
            this.bA[1] = 'A';
            this.bA[2] = 'C';
            this.bA[4] = 'G';
            this.bA[8] = 'T';
            this.bA[3] = 'M';
            this.bA[5] = 'R';
            this.bA[9] = 'W';
            this.bA[6] = 'S';
            this.bA[10] = 'Y';
            this.bA[12] = 'K';
            this.bA[15] = 'N';
        }

        public int fn(int x) {
            return this.bA[x];
        }
    };

    public static byte[] readInString(final String file, final byte terminalChar, boolean useReverseComplement)
            throws IOException {
    	int multiple = 1;
    	if(useReverseComplement) {
    		multiple = 2;
    	}
        final byte[] bA = new byte[(int) (new File(file).length())*multiple];
        final FastaParser_Generator_Int fPG = new FastaParser_Generator_Int(
                new BufferedInputStream(new FileInputStream(file)),
                Integer.MAX_VALUE);
        int i = 0;
        do {
        	int j = FastaParser_Generator_Int.readFile(fPG,
                    Integer.MAX_VALUE, bA, i);
            bA[j++] = terminalChar;
            if(useReverseComplement == true) {
            	for(int k=0; k<j - i; k++) {
            		bA[j++] = bA[i++];
            	}
            }
            i = j;
        } while (fPG.isNewSequence());
        final byte[] bA2 = new byte[i];
        System.arraycopy(bA, 0, bA2, 0, i);
        return bA2;
    }

    public static void translateString(final byte[] bA, final byte terminalChar,
            final byte terminalByte, final Function_Int charToByte,
            final byte randomRangeStart, final byte randomRangeSize) {
        for (int i = 0; i < bA.length; i++) {
            final int j = charToByte.fn(bA[i]);
            if (j == Integer.MAX_VALUE) {
                if (bA[i] == terminalChar) {
                    bA[i] = terminalByte;
                } else {
                    bA[i] = (byte) (randomRangeStart + (Math.random() * randomRangeSize));
                }
            } else {
                bA[i] = (byte) j;
            }
        }
    }

    static final Object[] parseAlphabet(final String workingAlphabet,
            final Pattern legalAlphabetPattern, final Function_Int charToByte,
            final int[] iC) {
        if (!legalAlphabetPattern.matcher(workingAlphabet).matches()) {
			throw new IllegalArgumentException(
                    "Given alphabet is invalid " + workingAlphabet);
		}
        final byte[] alphabet = workingAlphabet.getBytes();
        final int[] alphabetIC = new int[alphabet.length];
        // A C G T [AC]:M [AG]:R [AT]:W [CG]:S [CT]:Y [GT]:K [N]:ACGT
        for (int i = 0; i < alphabet.length; i++) {
            final int j = charToByte.fn(alphabet[i]);
            alphabet[i] = (byte) j;
            alphabetIC[i] = iC[j];
        }
        return new Object[] { alphabet, alphabetIC };
    }

}
