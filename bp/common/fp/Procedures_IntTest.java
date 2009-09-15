/*
 * Created on May 26, 2005
 */
package bp.common.fp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class Procedures_IntTest
                               extends TestCase {

    public void testRunProcedures() {
        final List<Integer> l = new ArrayList<Integer>();
        final Procedure_Int[] pA = new Procedure_Int[] {
                new Procedure_Int() {
                    /* (non-Javadoc)
                     * @see bp.common.fp.Procedure#pro(java.lang.Object)
                     */
                    public void pro(int i) {
                        l.add(new Integer(i));
                        l.add(new Integer(5));
                    }
                },
                new Procedure_Int() {
                    /* (non-Javadoc)
                     * @see bp.common.fp.Procedure#pro(java.lang.Object)
                     */
                    public void pro(int i) {
                        l.add(new Integer(i));
                        l.add(new Integer(6));
                    }
                }
        };
        Procedures_Int.runProcedures(pA).pro(4);
        Assert.assertEquals(new Integer(4), l.get(0));
        Assert.assertEquals(new Integer(5), l.get(1));
        Assert.assertEquals(new Integer(4), l.get(2));
        Assert.assertEquals(new Integer(6), l.get(3));
        Assert.assertEquals(l.size(), 4);
    }
    
    public void testUptoAndIncluding() {
        final int[] iA = new int[10];
        final Procedure_Int pro = Procedures_Int.uptoAndIncluding(new Procedure_Int() {
            /* (non-Javadoc)
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(int i) {
                iA[i] = i;
            }
        }, 0);
        pro.pro(9);
        Assert.assertTrue(Arrays.equals(iA, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }));
    }
}
