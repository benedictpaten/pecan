/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on May 24, 2005
 */
package bp.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Generator_Int;

/**
 * @author benedictpaten
 */
public class Fasta_Test
                       extends TestCase {

    public void testFasta() throws IOException {
        for (int trial = 0; trial < 10; trial++) {
            final ByteArrayOutputStream w = new ByteArrayOutputStream();
            final char[] cA = new char[] { 'a', 't', 'c', 'g' };
            final double d = Math.random();
            final String title = "" + d
                    + " ging gang goo atcg nvw !@#$%^&*()";
            final FastaOutput_Procedure_Int fPW = new FastaOutput_Procedure_Int(
                    w, title);
            final int length = (int) (Math.random() * 10000);
            final char[] string = new char[length];
            for (int i = 0; i < string.length; i++) {
                string[i] = cA[(int) (Math.random() * 4)];
                fPW.pro(string[i]);
            }
            fPW.endAndClose();
            final FastaParser_Generator_Int fPG = new FastaParser_Generator_Int(
                    new ByteArrayInputStream(w.toByteArray()),
                    Integer.MAX_VALUE);
            final StringBuffer sB = new StringBuffer();
            int c;
            Assert.assertEquals(title, fPG.getFastaID());
            while ((c = fPG.gen()) != Integer.MAX_VALUE) {
				sB.append((char) c);
			}
            Assert.assertEquals(new String(string), sB.toString());
            for (int i = 0; i < 100; i++) {
				Assert.assertEquals(fPG.gen(), Integer.MAX_VALUE);
			}
        }
        {
            try {
                final FastaParser_Generator_Int fPG = new FastaParser_Generator_Int(
                        new ByteArrayInputStream(new byte[] { 's',
                                'e', 'q' }), Integer.MAX_VALUE);
                Assert.fail();
            } catch (final IllegalStateException e) {
                ;
            }
        }
    }

    public void testFastaByteBufs() throws IOException {
        for (int trial = 0; trial < 100; trial++) {
            final ByteArrayOutputStream w = new ByteArrayOutputStream();
            final byte[] cA = new byte[] { 'a', 't', 'c', 'g' };
            final double d = Math.random();
            final String title = "" + d + " gingng goo atcg nvw !@#$%^&*()";
            int length = (int) (Math.random() * 1000);
            final byte[] string = new byte[length];
            for (int i = 0; i < string.length; i++) {
                string[i] = cA[(int) (Math.random() * 4)];
            }
            final int offset = (int) (Math.random() * string.length);
            length = (int) (Math.random() * (string.length - offset));
            if(Math.random() > 0.5) {
            FastaOutput_Procedure_Int.writeFile(w, title, string,
                    offset, length);
            }
            else {
                final int offset2 = offset;
                final int length2 = length;
                FastaOutput_Procedure_Int.writeFile(w, title, new Generator_Int() {
                    int i=0;
                    public int gen() {
                        if(this.i<length2) {
                            return string[offset2 + this.i++];
                        }
                        return Integer.MAX_VALUE;
                    }
                }, Integer.MAX_VALUE);
            }
            final byte[] bA = new byte[length];
            final FastaParser_Generator_Int fPG = new FastaParser_Generator_Int(
                    new ByteArrayInputStream(w.toByteArray()),
                    Integer.MAX_VALUE);
            Assert.assertEquals(FastaParser_Generator_Int
                    .readFile(fPG, Integer.MAX_VALUE, bA, 0), length);
            Assert.assertEquals(title, fPG.getFastaID());
            for (int i = 0; i < length; i++) {
				Assert.assertEquals(string[i + offset], bA[i]);
			}
        }
    }
}