/*
 * Created on Feb 16, 2005
 */
package bp.pecan.dimensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import bp.common.fp.Function_2Args;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate;
import bp.common.fp.Predicate_2Args;

/**
 * @author benedictpaten
 */
public abstract class AbstractDivisibleDimensionTest
                                                    extends
                                                    AbstractDimensionTest {

    /**
     *  
     */
    public AbstractDivisibleDimensionTest(final String s) {
        super(s);
    }

    @Override
	public void testGetSubDimensionSlice(final Dimension d, final Function_2Args fn) {
        super.testGetSubDimensionSlice(d, fn);
        for (int i = 0; i < 100; i++) {
            final int[] dA = new int[1 + new Random().nextInt(d
                    .subDimensionsNumber())];
            final Set s = new HashSet();
            while (s.size() != dA.length) {
				s.add(new Double(new Random().nextInt(d
                        .subDimensionsNumber())));
			}
            this.logger.fine(s.toString());
            final List l = (List) IterationTools
                    .append(s.iterator(), new ArrayList());
            Collections.sort(l);
            this.logger.fine(l.toString());
            for(int j=0; j<dA.length; j++) {
				dA[j] = ((Double)l.get(j)).intValue();
			}
            final Dimension d2 = d.getSubDimensionSlice(dA);
            final Iterator it = Iterators.filter(new View(d, dA)
                    .iterator(), new Predicate() {/*
                                                   * (non-Javadoc)
                                                   * 
                                                   * @see bp.common.fp.Predicate#test(java.lang.Object)
                                                   */
                public boolean test(Object o) {
                    int[] iA = (int[]) o;
                    for (int element : iA) {
                        if (element != Integer.MAX_VALUE) {
							return true;
						}
                    }
                    return false;
                }
            }), it2 = d2.iterator();
            Assert.assertTrue(IterationTools.equals(it, it2, new Predicate_2Args() {
                public boolean test(final Object o, final Object o2) {
                    final int[] iA = (int[]) o, iA2 = (int[]) o2;
                    for (int k = 0; k < iA.length; k++) {
                        if (iA[k] != iA2[k]) {
							return false;
						}
                    }
                    return true;
                }
            }));
        }
    }

}