/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on May 24, 2005
 */
package bp.pecan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Generator;
import bp.common.fp.Generators;

/**
 * @author benedictpaten
 */
public class AnchorParser_GeneratorTest
                                       extends TestCase {

    public void testAnchorParser_Generator() throws IOException {
        for (int trial = 0; trial < 10000; trial++) {
            final List l = PolygonFillerTest.makeRandomEdgeList(Math.random(), Math
                    .random(), (int) (Math.random() * 100));
            for(final Iterator it=l.iterator(); it.hasNext();) {
                final List l2 = (List)it.next();	
                for (final Iterator it2 = l2.listIterator(1); it2.hasNext();) {
                    final int[] iA = (int[]) it2.next();
                    iA[PolygonFillerTest.XINC] = (int)(Math.random()*10);
                }
            }
            final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
            final int number = AnchorParser_Generator.writeOutEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators.iteratorGenerator(l.iterator())),
                    bAOS);
            final Generator gen = AnchorParser_Generator.readInEdgeList(
                    new ByteArrayInputStream(bAOS.toByteArray()), number);
            final Generator gen2 = PolygonFillerTest.convertNewEdgeListToOldEdgeList(gen);
            for (int i = 0; i < l.size(); i++) {
                final List l2 = (List) l.get(i);
                final List l3 = (List) gen2.gen();
                Assert.assertEquals(l2.size(), l3.size());
                Assert.assertEquals(l2.get(0), l3.get(0));
                for (int j = 1; j < l2.size(); j++) {
					if(!Arrays.equals((int[]) l2.get(j), (int[]) l3
                            .get(j))) {
                        final int[] iA1 = (int[])l2.get(j), iA2 = (int[])l3.get(j);
                        Assert.fail();
                    }
				}
            }
        }
    }
}