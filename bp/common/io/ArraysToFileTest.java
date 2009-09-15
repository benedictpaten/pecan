/*
 * Created on Dec 14, 2005
 */
package bp.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Generator;
import bp.common.fp.Generators;

public class ArraysToFileTest
                             extends TestCase {

    public void testAnchorParser_Generator() throws IOException {
        for (int trial = 0; trial < 10000; trial++) {
            final List l = new LinkedList();
            while(Math.random() > 0.1) {
                l.add(new int[] { (int)(Math.random()*10000000), (int)(Math.random()*1000000) });
            }
            final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
            final int number = ArraysToFile.writeOutArray(Generators.iteratorGenerator(l.iterator()), bAOS);
            final Generator gen = ArraysToFile.readInArray(
                    new ByteArrayInputStream(bAOS.toByteArray()), number, 2);
            for (int i = 0; i < l.size(); i++) {
                final int[] iA = (int[])l.get(i);
                final int[] iA2 = (int[]) gen.gen();
                Assert.assertTrue(Arrays.equals(iA, iA2));
            }
            Assert.assertTrue(gen.gen() == null);
            Assert.assertTrue(gen.gen() == null);
            Assert.assertTrue(gen.gen() == null);
            Assert.assertTrue(gen.gen() == null);
            Assert.assertTrue(gen.gen() == null);
        }
    }
}
