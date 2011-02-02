/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Dec 14, 2005
 */
package bp.common.ds;

import java.util.Random;

import junit.framework.TestCase;
import bp.common.ds.SuffixTree.NE;
import bp.common.io.Debug;

public class SuffixTreeTest
                           extends TestCase {

    /* core code end */
    /* public test code */
    public void testSuffixTree() throws Exception {
        for (int trial = 0; trial < 100; trial++) {
            final StringBuffer s = new StringBuffer();
            final Random random = new Random();
            final int seqNumber = (int) (Math.random() * 1000);
            final char[] cA = new char[] { 'A', 'C', 'G', 'T' };
            final byte terminalChar = '%';
            final StringBuffer[] s2 = new StringBuffer[seqNumber];
            for (int i = 0; i < seqNumber; i++) {
                s2[i] = new StringBuffer();
                for (int j = (int) (Math.random() * 1000); j >= 0; j--) {
                    final char c = cA[(int) (Math.random() * cA.length)];
                    s.append(c);
                    s2[i].append(c);
                }
                s.append("%");
            }
            final long start = System.currentTimeMillis();
            final SuffixTree st = new SuffixTree(s.toString().getBytes(),
                    20, false, terminalChar);
            Debug.pl(" Strings parsed into suffix tree in : "
                    + (System.currentTimeMillis() - start)
                    + " milli-seconds , and : "
                    + (1.0 + (st.memoryUsage + st.string.length)
                            * 4.0 / st.string.length)
                    + " bytes/char ");
            for (int i = 0; i < 100; i++) {
                SuffixTreeTest.suffixTreeTrial(
                        st,
                        s2[(int) (Math.random() * seqNumber)]
                                .toString().getBytes());
            }
            Debug.pl(" done , no errors found : try again ");
        }
    }

    static void suffixTreeTrial(final SuffixTree sT, final byte[] string) {
        Debug.pl(" trying ");
        final Random r = new Random();
        int i = r.nextInt(string.length), j = r
                .nextInt(string.length);
		final int k = i;
        if (i > j) {
            i = j;
            j = k;
        }
        final NE ne = new NE(0, 0);
        sT.scanPrefix(ne, i, j, string);
        if (sT.depth(ne.n) + ne.e != j - i) {
            String s = "";
            for (int x = i; x < j; x++) {
				s += (char) string[x];
			}
            throw new IllegalStateException(
                    " string not found in tree : "
                            + sT.pathString(ne.n) + " " + s
                            + " depth ne.n " + sT.depth(ne.n)
                            + " length ne.QueueEdgeList " + ne.e
                            + " " + (j - i));
        }
        if ((ne.n != 0)
                && (sT.depth(sT.suffixLink(ne.n)) + 1 != sT
                        .depth(ne.n))) {
            throw new IllegalStateException(" suffix link incorrect "
                    + sT.pathString(ne.n) + " "
                    + sT.pathString(sT.suffixLink(ne.n)));
        }
    }

}
