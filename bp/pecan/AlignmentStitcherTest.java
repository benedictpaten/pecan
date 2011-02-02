/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Apr 5, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.Array;
import bp.common.fp.Function;
import bp.common.fp.Generator;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Procedure_2Args;

/**
 * @author benedictpaten
 */
public class AlignmentStitcherTest
                                  extends TestCase {

    public AlignmentStitcherTest(final String s) {
        super(s);
    }

    public void testAlignmentStitcher() {
        final List<Object> d = Arrays.asList(new Object[] { new int[] { 3 },
                new int[] { 4 }, new int[] { 5 } });
        final List<Object> alignment = Arrays.asList(new Object[] {
                new boolean[][] { { true, false } },
                new boolean[][] { { true, true }, { true, true } },
                new boolean[][] {} }), left = Arrays
                .asList(new Object[] { d.subList(0, 1),
                        d.subList(1, 3), d.subList(3, 3) }), right = Arrays
                .asList(new Object[] { d.subList(0, 0),
                        d.subList(0, 3), d.subList(3, 3) }), output = Arrays
                .asList(new Object[] {
                        new Object[] { new int[] { 3,
                                Integer.MAX_VALUE } },
                        new Object[] { new int[] { 4, 3 },
                                new int[] { 5, 4 }, },
                        new Object[] {} });
        final AlignmentStitcher aS = new AlignmentStitcher(new Generator() {
            int i = 0;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                return AlignmentStitcherTest.convertOldOutputToNew(Generators.arrayGenerator((Object[]) alignment
                        .get(this.i++)));
            }
        }, 1, 1);
        for (int i = 0; i < 3; i++) {
            aS.getInputX().pro(left.get(i));
            aS.getInputY().pro(right.get(i));
            final List l = new LinkedList();
            IterationTools.append(Iterators.map(((List) aS.gen())
                    .iterator(), new Function() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Function#fn(java.lang.Object)
                 */
                public Object fn(final Object o) {
                    final int[] iA = new int[2];
                    AlignmentStitcher.convertInput().pro(o, iA);
                    return iA;
                }
            }), l);
            Assert.assertTrue(Array.arraysEqual().test(l.toArray(),
                    output.get(i), new Predicate_2Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
                         *      java.lang.Object)
                         */
                        public boolean test(final Object o, final Object o2) {
                            return Arrays.equals((int[]) o,
                                    (int[]) o2);
                        }
                    }));
        }
    }

    public void testConvertInput() {
        final Procedure_2Args pro = AlignmentStitcher.convertInput();
        int[] iA = new int[7];
        pro
                .pro(
                        new Object[] {
                                new Object[] { new int[] { 0 },
                                        new int[] { 4, 90 } },
                                new Object[] {
                                        new int[] { 7 },
                                        new Object[] {
                                                new int[] { Integer.MAX_VALUE },
                                                new Object[] {
                                                        new int[] { 9 },
                                                        new int[] { Integer.MAX_VALUE } }, } } },
                        iA);
        Assert.assertTrue(Arrays.equals(new int[] { 0, 4, 90, 7,
                Integer.MAX_VALUE, 9, Integer.MAX_VALUE }, iA));
        iA = new int[1];
        pro.pro(new int[] { Integer.MAX_VALUE }, iA);
        Assert.assertTrue(Arrays.equals(new int[] { Integer.MAX_VALUE }, iA));
        iA = new int[2];
        pro.pro(new Object[] { new int[] { Integer.MAX_VALUE },
                new int[] { Integer.MAX_VALUE } }, iA);
        Assert.assertTrue(Arrays.equals(new int[] { Integer.MAX_VALUE,
                Integer.MAX_VALUE }, iA));
        iA = new int[4];
        pro.pro(new Object[] { new int[] { 10, 40 },
                new int[] { 20, 5 } }, iA);
        Assert.assertTrue(Arrays.equals(new int[] { 10, 40, 20, 5 }, iA));
    }

    public static Generator convertOldOutputToNew(final Generator gen) {
        return new Generator() {
            public Object gen() {
                final Object o = gen.gen();
                final boolean[] bA = (boolean[]) o;
                if (o != null) {
                    if (Arrays.equals(bA,
                            new boolean[] { true, false })) {
						return new Float(Float.POSITIVE_INFINITY);
					}
                    if (Arrays.equals(bA,
                            new boolean[] { false, true })) {
						return new Float(Float.NEGATIVE_INFINITY);
					}
                    return new Float(0);
                }
                return null;
            }
        };
    }
}