/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Oct 21, 2005
 */
package bp.common.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class MultiFastaParser_GeneratorTest
                                           extends TestCase {
    public void testMultiFastaParser_Generator() throws IOException {
        final String[] iDs = new String[] { 
                "foo", "bar", "2", "3", "4", "5","6","7","8","9"
        };
        for (int trial = 0; trial < 1000; trial++) {
            final int seqNo = 1 + (int) (Math.random() * 10);
            final List<int[]> l = new LinkedList<int[]>();
            while (Math.random() > 0.001) {
                final int[] iA = new int[seqNo];
                for (int i = 0; i < iA.length; i++) {
					iA[i] = (int) (Math.random() * 5);
				}
                l.add(iA);
            }
            final File f = File.createTempFile("temp", null);
            final OutputStream oS = new BufferedOutputStream(
                    new FileOutputStream(f));
            for (int i = 0; i < seqNo; i++) {
                final FastaOutput_Procedure_Int fOPI = new FastaOutput_Procedure_Int(
                        oS, iDs[i]);
                for (final Iterator<int[]> it = l.iterator(); it.hasNext();) {
                    final int[] iA = it.next();
                    fOPI.pro(iA[i]);
                }
                fOPI.end();
            }
            oS.close();
            final MultiFastaParser_Generator fPG = new MultiFastaParser_Generator(
                    f.toString());
            final String[] iDS2 = fPG.getFastaIDs();
            Assert.assertEquals(iDS2.length, seqNo);
            for(int i=0; i<iDS2.length; i++) {
				Assert.assertEquals(iDS2[i], iDs[i]);
			}
            for (final Iterator<int[]> it = l.iterator(); it.hasNext();) {
                final int[] iA = it.next();
                final int[] iA2 = (int[]) fPG.gen();
                Assert.assertTrue(Arrays.equals(iA, iA2));
            }
            while (Math.random() > 0.2) {
				Assert.assertEquals(fPG.gen(), null);
			}
            f.delete();
        }
    }
}