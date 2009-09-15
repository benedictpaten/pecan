/*
 * Created on Feb 2, 2005
 */
package bp.common.fp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class IterationToolsTest
                               extends TestCase {
    public IterationToolsTest(final String s) {
        super(s);
    }

    public void testNoOfElements() {
        final Iterator it = Arrays
                .asList(
                        new Object[] { new Integer(1), new Integer(2),
                                new Integer(3) }).iterator();
        Assert.assertEquals(IterationTools.noOfElements(it), new Double(3));
    }

    public void testSum() {
        final Iterator it = Arrays
                .asList(
                        new Object[] { new Integer(1), new Integer(2),
                                new Integer(3) }).iterator();
        Assert.assertEquals(IterationTools.sum(it), new Double(1 + 2 + 3));
    }

    public void testAppend() {
        List l = new ArrayList();
        l.add("1");
        l = (List) IterationTools.append(Arrays.asList(new Object[] { "2" })
                .iterator(), l);
        Assert.assertEquals(l.get(0), "1");
        Assert.assertEquals(l.get(1), "2");
        Assert.assertEquals(l.size(), 2);
    }

    public void testGetIterator() {
        Functions.getIterator().fn(new ArrayList());
        try {
            Functions.getIterator().fn(new Integer(0));
            Assert.fail();
        } catch (final RuntimeException e) {
            ;
        }
    }

    public void testEquals() {
        List l = Arrays.asList(new Object[] { "1", "2", "3" });
        Assert.assertTrue(IterationTools.equals(l.iterator(), l.iterator(),
                Predicates_2Args.equal()));
        Assert.assertFalse(IterationTools.equals(l.iterator(), l.iterator(),
                Predicates_2Args.notEqual()));
        Assert.assertFalse(IterationTools.equals(l.iterator(), Arrays.asList(
                new Object[] { "1", "2", "3", "4" }).iterator(),
                Predicates_2Args.equal()));
        Assert.assertFalse(IterationTools.equals(Arrays.asList(
                new Object[] { "1", "2", "3", "4" }).iterator(), l.iterator(),
                Predicates_2Args.equal()));
        Assert.assertFalse(IterationTools.equals(l.iterator(), Arrays.asList(
                new Object[] { "1", "2", "4" }).iterator(), Predicates_2Args
                .equal()));
        Assert.assertFalse(IterationTools.equals(Arrays.asList(
                new Object[] { "1", "2", "4" }).iterator(), l.iterator(),
                Predicates_2Args.equal()));
        l = Arrays.asList(new Object[] {});
        Assert.assertTrue(IterationTools.equals(l.iterator(), l.iterator(),
                Predicates_2Args.equal()));

    }

    public void testReduce() {
        String s = "";
        final List list = Arrays.asList(new Object[] { "one", "two", null, "three",
                "four", "five", "six", null });
        final Iterator it = list.iterator();
        for (int i = 0; i < list.size(); i++) {
			if (list.get(i) != null) {
				s += (String) list.get(i);
			}
		}
        Assert.assertEquals(IterationTools.reduce(it, "", new Function_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_2Args#polygonClipper(java.lang.Object,
             *      java.lang.Object)
             */
            public Object fn(final Object o, final Object o2) {
                return o != null ? ((String) o2) + ((String) o) : o2;
            }
        }), s);
    }

    public void testAddToEnd() {
        Iterator it = IterationTools.addToEnd(Arrays.asList(
                new Object[] { "1", "2" }).iterator(), "3");
        Assert.assertTrue(Arrays.equals(((List) IterationTools.append(it,
                new ArrayList())).toArray(), new Object[] { "1", "2", "3" }));
        it = IterationTools.addToEnd(Arrays.asList(new Object[] {}).iterator(),
                "3");
        Assert.assertTrue(Arrays.equals(((List) IterationTools.append(it,
                new ArrayList())).toArray(), new Object[] { "3" }));
        it = IterationTools.addToEnd(Arrays.asList(new Object[] { "1", "2" })
                .iterator(), null);
        Assert.assertTrue(Arrays.equals(((List) IterationTools.append(it,
                new ArrayList())).toArray(), new Object[] { "1", "2", null }));
    }

    public void testJoin() {
        for (int trial = 0; trial < 100; trial++) {
            final String[] sA = new String[(int) (Math.random() * 5)];
            String s = " ";
            for (int i = 0; i < sA.length; i++) {
                sA[i] = "" + i;
                s += sA[i] + " ";
            }
            Assert.assertEquals(s, IterationTools.join(sA, " "));
        }
    }
    
    public void testJoinInt() {
        for (int trial = 0; trial < 100; trial++) {
            final int[] sA = new int[(int) (Math.random() * 5)];
            String s = " ";
            for (int i = 0; i < sA.length; i++) {
                sA[i] = i;
                s += sA[i] + " ";
            }
            Assert.assertEquals(s, IterationTools.join(sA, " "));
        }
    }
    
    public void testJoinDouble() {
        for (int trial = 0; trial < 100; trial++) {
            final double[] sA = new double[(int)(Math.random() * 5)];
            String s = " ";
            for (int i = 0; i < sA.length; i++) {
                sA[i] = i;
                s += sA[i] + " ";
            }
            Assert.assertEquals(s, IterationTools.join(sA, " "));
        }
    }
    
    public void testJoinFloat() {
        for (int trial = 0; trial < 100; trial++) {
            final float[] sA = new float[(int)(Math.random() * 5)];
            String s = " ";
            for (int i = 0; i < sA.length; i++) {
                sA[i] = i;
                s += sA[i] + " ";
            }
            Assert.assertEquals(s, IterationTools.join(sA, " "));
        }
    }
    
    public void testJoinBoolean() {
        for (int trial = 0; trial < 100; trial++) {
            final boolean[] sA = new boolean[(int)(Math.random() * 5)];
            String s = " ";
            for (int i = 0; i < sA.length; i++) {
                sA[i] = Math.random() > 0.5;
                s += sA[i] + " ";
            }
            Assert.assertEquals(s, IterationTools.join(sA, " "));
        }
    }
}