/*
 * Created on Jan 31, 2005
 */
package bp.common.fp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class IteratorsTest
                          extends TestCase {
    Iterator it;

    List list;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        this.list = Arrays.asList(new Object[] { "one", "two", null,
                "three", "four", "five", "six", null });
        this.it = this.list.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        this.it = null;
        this.list = null;
    }

    public void testChain() {
        final Iterator it2 = Iterators.chain(Arrays.asList(
                new Object[] {
                        Arrays.asList(
                                new Object[] { "one", "two", null })
                                .iterator(),
                        Arrays.asList(
                                new Object[] { "three", "four" })
                                .iterator() }).iterator());

        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "one");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "two");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), null);
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "three");
        Assert.assertEquals(it2.next(), "four");
        Assert.assertFalse(it2.hasNext());
        try {
            it2.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public void testCount() {
        final Iterator it = Iterators.count(0);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(it.next(), new Integer(0));
        Assert.assertEquals(it.next(), new Integer(1));
        Assert.assertEquals(it.next(), new Integer(2));
        Assert.assertTrue(it.hasNext());
    }

    public void testFilter() {
        final Iterator it2 = Iterators.filter(this.it, new Predicate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.BooleanPredicate#test(java.lang.Object)
             */
            public boolean test(Object o) {
                return (o != null) && ((String) o).startsWith("f");
            }
        });
        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "four");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "five");
        Assert.assertFalse(it2.hasNext());
        try {
            it2.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public void testMap() {
        final Iterator it2 = Iterators.map(this.it, new Function() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function#polygonClipper(java.lang.Object)
             */
            public Object fn(Object o) {
                return o != null ? ((String) o) + "2" : o;
            }
        });
        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "one2");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "two2");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), null);
        Assert.assertEquals(it2.next(), "three2");
        Assert.assertEquals(it2.next(), "four2");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), "five2");
        Assert.assertEquals(it2.next(), "six2");
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(it2.next(), null);
        Assert.assertFalse(it2.hasNext());
        try {
            it2.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public void testUniq() {
        final Comparator c = new Comparator() {
            /*
             * (non-Javadoc)
             * 
             * @see java.util.Comparator#compare(java.lang.Object,
             *      java.lang.Object)
             */
            public int compare(Object arg0, Object arg1) {
                return ((int[]) arg0)[0] > ((int[]) arg1)[0] ? 1
                        : ((int[]) arg0)[0] < ((int[]) arg1)[0] ? -1
                                : 0;
            }
        };
        final Random r = new Random();
        for (int trial = 0; trial < 1000; trial++) {
            final List l1 = new ArrayList();
            int i = 0;
            while (r.nextDouble() > 0.2) {
				l1.add(new int[] { r.nextInt(10), 0 });
			}
            Collections.sort(l1, c);
            for (int j = 0; j < l1.size(); j++) {
				((int[]) l1.get(i))[1] = i++;
			}
            final int[] iA = (int[]) IterationTools.reduce(Iterators.uniq(
                    l1.iterator(), c, Functions_2Args
                            .concatenateIntArrays()), new int[0],
                    Functions_2Args.rCurry(Functions_3Args
                            .flipArguments(), Functions_2Args
                            .concatenateIntArrays()));
            for (int j = 0; j < i; j++) {
				Assert.assertEquals(j, iA[j * 2 + 1]);
			}
        }
    }

    public void testMerge() {
        final Comparator c = new Comparator() {
            /*
             * (non-Javadoc)
             * 
             * @see java.util.Comparator#compare(java.lang.Object,
             *      java.lang.Object)
             */
            public int compare(Object arg0, Object arg1) {
                return ((int[]) arg0)[0] > ((int[]) arg1)[0] ? 1
                        : ((int[]) arg0)[0] < ((int[]) arg1)[0] ? -1
                                : 0;
            }
        };
        final Random r = new Random();
        for (int trial = 0; trial < 1000; trial++) {
            final List l1 = new ArrayList(), l2 = new ArrayList();
            while (r.nextDouble() > 0.2) {
				l1.add(new int[] { r.nextInt(10) });
			}
            Collections.sort(l1, c);
            while (r.nextDouble() > 0.2) {
				l2.add(new int[] { r.nextInt(10) });
			}
            Collections.sort(l2, c);
            final Iterator mIT = Iterators.merge(l1.iterator(), l2
                    .iterator(), c);
            final List l3 = (List) IterationTools.append(mIT,
                    new ArrayList());
            try {
                mIT.next();
                Assert.fail();
            } catch (final NoSuchElementException e) {
                ;
            }
            l1.addAll(l2);
            Collections.sort(l1, c);
            Assert.assertEquals(l1.size(), l3.size());
            for (int i = 0; i < l1.size(); i++) {
				Assert.assertTrue(l1.get(i) == l3.get(i));
			}
        }
    }

    public void testRepeat() {
        final Iterator it = Iterators.repeat("1", 3);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(it.next(), "1");
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(it.next(), "1");
        Assert.assertEquals(it.next(), "1");
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public void testZip_WithClass() {
        final List l = Arrays.asList(new Object[] { "1", "2", null, "3",
                "4", "5", null, "7" });
        final Iterator it2 = Iterators.zip(this.it, l.iterator(), String.class);
        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                "one", "1" }));
        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                "two", "2" }));
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                null, null }));
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                "three", "3" }));
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                "four", "4" }));
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                "five", "5" }));
        Assert.assertTrue(it2.hasNext());
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                "six", null }));
        Assert.assertTrue(Arrays.equals((String[]) it2.next(), new String[] {
                null, "7" }));
        Assert.assertFalse(it2.hasNext());
        try {
            it2.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public void testSort() {
        Assert.assertTrue(Arrays.equals(((List) IterationTools.append(
                Iterators.sort(Arrays
                        .asList(
                                new Object[] { new Integer(5),
                                        new Integer(1) }).iterator(),
                        new Comparator() {
                            /*
                             * (non-Javadoc)
                             * 
                             * @see java.util.Comparator#compare(java.lang.Object,
                             *      java.lang.Object)
                             */
                            public int compare(final Object arg0,
                                    final Object arg1) {
                                return ((Comparable<Object>) arg0)
                                        .compareTo(arg1);
                            }
                        }), new ArrayList())).toArray(),
                new Object[] { new Integer(1), new Integer(5) }));
    }

    public void testDuplicate() {
        for (int trial = 0; trial < 100; trial++) {
            final List<Integer> l = new LinkedList<Integer>();
            while (Math.random() > 0.1) {
                l.add(new Integer((int) (Math.random() * 100)));
            }
            final Iterator[] itA = Iterators.duplicate(l.iterator());
            final Iterator it = itA[0], it2 = itA[1];
            final Iterator<Integer> it3 = l.iterator(), it4 = l.iterator();
            while (it.hasNext() || it.hasNext()) {
                while (it.hasNext() && (Math.random() > 0.8)) {
					Assert.assertEquals(it.next(), it3.next());
				}
                while (it2.hasNext() && (Math.random() > 0.8)) {
					Assert.assertEquals(it2.next(), it4.next());
				}
            }
        }
    }

}