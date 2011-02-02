/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jun 10, 2005
 */
package bp.common.io;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Generator;
import bp.common.fp.IterationTools;
import bp.pecan.PolygonFiller;
import bp.pecan.PolygonFillerTest;

/**
 * @author benedictpaten
 */
public class CigarParser_GeneratorTest
                                      extends TestCase {
    public void testCigarParser() throws IOException {
        final String s = "cigar: gallus 567 14343 + Mus 421 19141 + 1010 M 76 I 1062 D 1080 I 12 D 12 M 121 I 1 M 11 I 5222 D 8097 I 12 D 12 M 89 I 3 M 68 D 1 M 11 I 3696 D 5260 I 12 D 12 M 104 D 1 M 14 I 3202 D 3691 I 12 D 12 M 48";
        final String s2 = "cigar: gallus 12367 643 - Mus 5093 18396 + 149 M 10 D 2 M 28 I 1179 D 6578 I 9 D 10 M 6 I 1 M 46 I 2 M 5 I 1 M 7 I 6122 D 5912 M 7 I 1 M 50 I 4215 D 607 I 12 D 12 M 23";
        final String s3 = "cigar: gallus 12367 643 - Mus 5093 18396 + 149";
        final String s4 = "cigar: g1a4l5lus 12367 643 . M@#$%^&&*us 5093 18396 + 149";
        final CigarParser_Generator.Cigar c = CigarParser_Generator
                .parseCigar(new LineNumberReader(new StringReader(s)));
        Assert.assertEquals(s, c.toString());
        final CigarParser_Generator.Cigar c2 = CigarParser_Generator
                .parseCigar(new LineNumberReader(new StringReader(s2)));
        Assert.assertEquals(s2, c2.toString());
        final CigarParser_Generator.Cigar c3 = CigarParser_Generator
                .parseCigar(new LineNumberReader(new StringReader(s3)));
        Assert.assertEquals(s3, c3.toString());
        final CigarParser_Generator.Cigar c4 = CigarParser_Generator
                .parseCigar(new LineNumberReader(new StringReader(s4)));
        Assert.assertEquals(s4, c4.toString());
    }

    public void testConvertToEdgeList() throws IOException {
        for (int trial = 0; trial < 1000; trial++) {
            final List eT = (List) IterationTools.append(PolygonFillerTest
                    .clipUpperDiagonalEdgeList(PolygonFillerTest
                            .makeRandomEdgeList(Math.random() * 0.8,
                                    Math.random() * 0.8, 1000)
                            .iterator()), new LinkedList());
            final CigarParser_Generator.Cigar c = CigarParser_GeneratorTest.makeCigar(PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(eT))));
            final Generator gen = PolygonFillerTest.convertNewEdgeListToOldEdgeList(CigarParser_Generator
                    .convertToEdgeList(c));
            for (final Iterator it = eT.iterator(); it.hasNext();) {
                final List l = (List) it.next(), l2 = (List) gen.gen();
                Assert.assertEquals(l.size(), l2.size());
                Assert.assertEquals(l.get(0), l2.get(0));
                for (int i = 1; i < l.size(); i++) {
					Assert.assertTrue(Arrays.equals((int[]) l.get(i),
                            (int[]) l2.get(i)));
				}
            }
            Assert.assertEquals(gen.gen(), null);
        }
    }

    static CigarParser_Generator.Cigar makeCigar(final List edgeList)
            throws IOException {
        if (edgeList.size() > 0) {
            int xP = -1, yP = -1;
            final CigarParser_Generator.Cigar c = CigarParser_Generator
                    .parseCigar(new LineNumberReader(
                            new StringReader("cigar: gallus " + 0
                                    + " " + Integer.MAX_VALUE
                                    + " + Mus " + 0 + " "
                                    + Integer.MAX_VALUE + " + "
                                    + Integer.MAX_VALUE)));
            final List<int[]> ops = new LinkedList<int[]>();
            for (final Iterator it = edgeList.iterator(); it.hasNext();) {
                final List l = (List) it.next();
                final int x0 = ((int[]) l.get(1))[PolygonFillerTest.X];
                final int y0 = ((Integer) l.get(0)).intValue();
                final int y1 = ((int[]) l.get(1))[PolygonFillerTest.YMAX];
                final int x1 = y1 - y0 + x0;
                if (x0 > xP + 1) {
                    ops.add(new int[] {
                            CigarParser_Generator.Cigar.INSERT,
                            x0 - xP - 1 });
                }
                if (y0 > yP + 1) {
                    ops.add(new int[] {
                            CigarParser_Generator.Cigar.DELETE,
                            y0 - yP - 1 });
                }
                ops.add(new int[] {
                        CigarParser_Generator.Cigar.MATCH,
                        y1 - y0 + 1 });
                xP = x1;
                yP = y1;
            }
            c.operations = new int[ops.size() * 2];
            for (int i = 0; i < ops.size(); i++) {
                final int[] op = ops.get(i);
                c.operations[i * 2] = op[0];
                c.operations[i * 2 + 1] = op[1];
            }
            return c;
        }
        return CigarParser_Generator
                .parseCigar(new LineNumberReader(
                        new StringReader(
                                "cigar: gallus 567 14343 + Mus 421 19141 + 1010")));
    }

}