/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Apr 5, 2005
 */
package bp.common.fp;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class GeneratorsTest
                           extends TestCase {
    public GeneratorsTest(final String s) {
        super(s);
    }

    Generator genNum;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        this.genNum = Generators.arrayGenerator(new Object[] { "one",
                "two", "three", "four", "five", "six", null });
    }

    public void testFilter() {
        final Generator it2 = Generators.filter(this.genNum, new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.BooleanPredicate#test(java.lang.Object)
             */
            public boolean test(Object o) {
                return (o != null) && ((String) o).startsWith("f");
            }
        });
        Assert.assertEquals(it2.gen(), "four");
        Assert.assertEquals(it2.gen(), "five");
        Assert.assertTrue(it2.gen() == null);
        Assert.assertTrue(it2.gen() == null);
        Assert.assertTrue(it2.gen() == null);
        Assert.assertTrue(it2.gen() == null);
    }

    public void testArrayGenerator() {
        Object[] oA = new Object[] { "1", "2" };
        Generator gen = Generators.arrayGenerator(oA);
        Assert.assertEquals(gen.gen(), "1");
        Assert.assertEquals(gen.gen(), "2");
        Assert.assertEquals(gen.gen(), null);
        oA = new Object[] {};
        gen = Generators.arrayGenerator(oA);
        Assert.assertEquals(gen.gen(), null);
    }

    public void testAppend() {
        for (int trial = 0; trial < 100; trial++) {
            final Object[] oA = new Object[(int) (Math.random() * 5)], oA2 = new Object[(int) (Math
                    .random() * 5)];
            int j = 0;
            for (int i = 0; i < oA.length; i++) {
                oA[i] = new Integer(j++);
            }
            for (int i = 0; i < oA2.length; i++) {
                oA2[i] = new Integer(j++);
            }
            final Generator gen = Generators.append(Generators
                    .arrayGenerator(oA), Generators
                    .arrayGenerator(oA2));
            for (final Object element : oA) {
                Assert.assertEquals(gen.gen(), element);
            }
            for (final Object element : oA2) {
                Assert.assertEquals(gen.gen(), element);
            }
            Assert.assertEquals(gen.gen(), null);
        }
    }

    public void testIteratorGenerator() {
        for (int trial = 0; trial < 100; trial++) {
            final Object[] oA = new Object[(int) (Math.random() * 5)];
            final List l = new LinkedList();
            int j = 0;
            for (int i = 0; i < oA.length; i++) {
                oA[i] = new Integer(j++);
                l.add(oA[i]);
            }
            final Generator gen = Generators.arrayGenerator(oA), gen2 = Generators
                    .iteratorGenerator(l.iterator());
            for (int i = 0; i < 100; i++) {
				Assert.assertEquals(gen.gen(), gen2.gen());
			}
        }
    }

    public void testMapGenerator() {
        final Object[] oA = new Object[(int) (Math.random() * 5)];
        for (int i = 0; i < oA.length; i++) {
            oA[i] = "" + i;
        }
        final Generator gen = Generators.map(Generators.arrayGenerator(oA),
                Functions.parseNumber());
        for (final Object element : oA) {
            Assert.assertTrue(gen.gen().equals(new Double((String) element)));
        }
    }

    public void testQueueGenerator() {
        final List l = new LinkedList();
        final Generator gen = Generators.queueGenerator(l);
        Assert.assertEquals(gen.gen(), null);
        l.add("1");
        Assert.assertEquals(gen.gen(), "1");
        Assert.assertEquals(gen.gen(), null);
        l.add("1");
        l.add("2");
        Assert.assertEquals(gen.gen(), "1");
        Assert.assertEquals(gen.gen(), "2");
        Assert.assertEquals(gen.gen(), null);
    }

    public void testSplitGenerator() {
        for (int trial = 0; trial < 1000; trial++) {
            final List l = new LinkedList();
            for(int k=1; Math.random() > 0.1;) {
                l.add("" + k++);
            }
            final Object[] oA = l.toArray();
            final Generator[] genA = Generators.splitGenerator(Generators
                    .arrayGenerator(oA), Functions.doNothing());
            final Generator gen = genA[0];
            final Generator gen2 = genA[1];
            int i = 0;
            int j = 0;
            while ((i < oA.length) || (j < oA.length)) {
                if ((i < oA.length) && (Math.random() > 0.5)) {
                    final String s = (String) gen.gen();
                    Assert.assertTrue(s.equals(oA[i++]));
                }
                if (i == oA.length) {
					Assert.assertEquals(gen.gen(), null);
				}
                if ((j < oA.length) && (Math.random() > 0.5)) {
                    final String s = (String) gen2.gen();
                    Assert.assertTrue(s.equals(oA[j++]));
                }
                if (j == oA.length) {
					Assert.assertEquals(gen2.gen(), null);
				}
            }
            Assert.assertEquals(gen.gen(), null);
            Assert.assertEquals(gen2.gen(), null);
            Assert.assertEquals(gen.gen(), null);
            Assert.assertEquals(gen.gen(), null);
            Assert.assertEquals(gen2.gen(), null);
            Assert.assertEquals(gen2.gen(), null);
        }
    }
    
    public void testLineNumberGenerator() {
        StringWriter sW = new StringWriter();
        sW.write("oh thank you \n");
        sW.write("you are to kind\n");
        Generator gen = Generators.lineGenerator(new StringReader(sW.toString()));
        Assert.assertEquals(gen.gen(), "oh thank you ");
        Assert.assertEquals(gen.gen(), "you are to kind");
        Assert.assertEquals(gen.gen(), null);
        Assert.assertEquals(gen.gen(), null);
        Assert.assertEquals(gen.gen(), null);
        
        sW = new StringWriter();
        gen = Generators.lineGenerator(new StringReader(sW.toString()));
        Assert.assertEquals(gen.gen(), null);
        Assert.assertEquals(gen.gen(), null);
        Assert.assertEquals(gen.gen(), null);
    }
}