/*
 * Created on May 27, 2005
 */
package bp.common.ds;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Generator_Int;
import bp.common.fp.Procedure_Int;

/**
 * @author benedictpaten
 */
public class ScrollingQueue_IntToolsTest
                                        extends TestCase {
    public void testFillFromGenerator() {
        final Generator_Int genInt = new Generator_Int() {
            int[] iA = new int[100];

            int index = 0;
            {
                for (int i = 0; i < this.iA.length; i++) {
					this.iA[i] = i;
				}
            }

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator_Int#gen()
             */
            public int gen() {
                return this.iA[this.index++];
            }
        };
        final ScrollingQueue_Int sQ = new ScrollingQueue_Int(10, 5, true);
        final Procedure_Int pro = ScrollingQueue_IntTools.fillFromGenerator(genInt,
                sQ);
        pro.pro(5);
        Assert.assertEquals(sQ.lastIndex(), 5);
        pro.pro(6);
        Assert.assertEquals(sQ.lastIndex(), 6);
        Assert.assertEquals(sQ.get(5), 0);
        pro.pro(8);
        Assert.assertEquals(sQ.lastIndex(), 8);
        Assert.assertEquals(sQ.get(6), 1);
        Assert.assertEquals(sQ.get(7), 2);
    }

}