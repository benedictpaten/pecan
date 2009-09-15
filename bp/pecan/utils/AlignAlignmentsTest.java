/*
 * Created on Dec 2, 2005
 */
package bp.pecan.utils;

import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Generator;
import bp.common.fp.Procedure_Int;

public class AlignAlignmentsTest
                                extends TestCase {

    public void testAncestorToAlignment() {
        final int m = 1000;
        final int g = '-';
        final int[] iA = new int[] { m, m, g, m};//, '-' };
        final int[] iA2 = new int[] { g, m, m, g, m, g, g };
        final int[] out1 = new int[] { '-', 'a', 'a', '-', 'a', 'a', '-', '-'};//, 'a' };
        final int[] out2 = new int[] { 'a', 'a', 'a', 'a', '-', 'a', 'a', 'a'};//, '-' };
        final int[] out3 = new int[8];
        final int[] out4 = new int[8];
        final MutableInteger mI = new MutableInteger(0);
        final MutableInteger mI2 = new MutableInteger(0);
        final Procedure_Int[] outputA = new Procedure_Int[] {
                new Procedure_Int() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Procedure_Int#pro(int)
                     */
                    public void pro(int i) {
                        out3[mI.i++] = i;
                    }
                }, new Procedure_Int() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Procedure_Int#pro(int)
                     */
                    public void pro(int i) {
                        out4[mI2.i++] = i;
                    }
                } };
        final MutableInteger mI3 = new MutableInteger(0);
        final AlignAlignments.AncestorToAlignment aA = new AlignAlignments.AncestorToAlignment(
                1, 1, new Generator() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Generator#gen()
                     */
                    public Object gen() {
                        if(mI3.i < iA2.length) {
							return new Object[] {
							        new Integer(iA2[mI3.i++]),
							        new int[] { 'a' } };
						}
                        return null;
                    }
                }, outputA);
        for (final int element : iA) {
            aA.callMeFirst().pro();
            aA.callMeSecond().pro(element);
            out3[mI.i++] = 'a';
        }
        aA.callMeFirst().pro(); //clean up overhanging residues
        Assert.assertTrue(Arrays.equals(out1, out3));
        Assert.assertTrue(Arrays.equals(out2, out4));
    }
    
}
