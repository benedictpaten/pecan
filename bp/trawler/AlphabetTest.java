/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Dec 15, 2005
 */
package bp.trawler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.io.FastaOutput_Procedure_Int;

public class AlphabetTest
                         extends TestCase {

    public void testReadInString() throws Exception {
        for(int trial=0; trial<1000; trial++) {
            final byte[][] bAA = TrawlerToolsTest.randomStrings();
            if(bAA.length == 0) {
				continue;
			}
            final File f = File.createTempFile("one", "two");
            f.deleteOnExit();
            final OutputStream oS = new BufferedOutputStream(new FileOutputStream(f));
            for (final byte[] element : bAA) {
               FastaOutput_Procedure_Int.writeFile(oS, "hee", element, 0, element.length);
            }
            oS.close();
            final byte[] bA2 = TrawlerToolsTest.getString(bAA);
            final byte[] bA = Alphabet.readInString(f.toString(), (byte)16, false);
            Assert.assertTrue(Arrays.equals(bA, bA2));
        }
    }
    
    public void testTranslateAlphabet() throws Exception {
        for(int trial=0; trial<1000; trial++) {
            final byte[][] bAA = TrawlerToolsTest.randomStrings();
            final byte[] bA = TrawlerToolsTest.getString(bAA);
            final byte[] bA2 = new byte[bA.length];
            final Alphabet a = new Alphabet();
            for(int i=0; i<bA.length; i++) {
                if(bA[i] < 16) {
					bA2[i] = (byte)a.byteToChar.fn(bA[i]);
				} else {
					bA2[i] = '$';
				}
                if(Math.random() > 0.9) {
                    bA[i] = (byte)100;
                    bA2[i] = (byte)'%';
                }
            }
            Alphabet.translateString(bA2, (byte)'$', (byte)16, a.charToByte, (byte)100, (byte)1); 
        }
    }
    
    public void testParseAlphabet() throws Exception {
        final Alphabet a = new Alphabet();
        
        Object[] oA = Alphabet.parseAlphabet("ACGT", a.legalAlphabetPattern, a.charToByte, a.nucleotideIC);
        byte[] bA = (byte[])oA[0];
        Assert.assertTrue(Arrays.equals(bA, new byte[] { 1, 2, 4, 8 }));
        int[] bA2 = (int[])oA[1];
        Assert.assertTrue(Arrays.equals(bA2, new int[] { 0, 0, 0, 0 }));
        
        oA = Alphabet.parseAlphabet("ATRN", a.legalAlphabetPattern, a.charToByte, a.nucleotideIC);
        bA = (byte[])oA[0];
        Assert.assertTrue(Arrays.equals(bA, new byte[] { 1, 8, 5, 15 }));
        bA2 = (int[])oA[1];
        Assert.assertTrue(Arrays.equals(bA2, new int[] { 0, 0, 1, 2 }));
        
        oA = Alphabet.parseAlphabet("N", a.legalAlphabetPattern, a.charToByte, a.nucleotideIC);
        bA = (byte[])oA[0];
        Assert.assertTrue(Arrays.equals(bA, new byte[] { 15 }));
        bA2 = (int[])oA[1];
        Assert.assertTrue(Arrays.equals(bA2, new int[] { 2 }));
        
        oA = Alphabet.parseAlphabet("", a.legalAlphabetPattern, a.charToByte, a.nucleotideIC);
        bA = (byte[])oA[0];
        Assert.assertTrue(Arrays.equals(bA, new byte[] {  }));
        bA2 = (int[])oA[1];
        Assert.assertTrue(Arrays.equals(bA2, new int[] {  }));
        
        oA = Alphabet.parseAlphabet("ACGTMRWSYKN", a.legalAlphabetPattern, a.charToByte, a.nucleotideIC);
        bA = (byte[])oA[0];
        Assert.assertTrue(Arrays.equals(bA, new byte[] { 1, 2, 4, 8, 3, 5, 9, 6, 10, 12, 15 }));
        bA2 = (int[])oA[1];
        Assert.assertTrue(Arrays.equals(bA2, new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2 }));
        
        
        try {
            oA = Alphabet.parseAlphabet("NA", a.legalAlphabetPattern, a.charToByte, a.nucleotideIC);
            Assert.fail();
        }
        catch(final IllegalArgumentException e) {
            //
        }
        
        try {
            oA = Alphabet.parseAlphabet("ACFN", a.legalAlphabetPattern, a.charToByte, a.nucleotideIC);
            Assert.fail();
        }
        catch(final IllegalArgumentException e) {
            //
        }
        
    }
}
