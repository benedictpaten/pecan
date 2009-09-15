/*
 * Created on Feb 19, 2005
 */
package bp.common.maths;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Functions_Int_2Args;

/**
 * @author benedictpaten
 */
public class MathsTest
                         extends TestCase {
    
   
    
    public void testSum() {
        final Function_Int_2Args fn = Functions_Int_2Args.sum();
        Assert.assertEquals(10 + 12, fn.fn(10, 12));
    }

    public void testLogAdd() {
        for (int trial = 0; trial < 1000; trial++) {
            final double d = Math.random() * 100;
            final double d2 = d - 20 * Math.random();
            double d3 = d, d4 = d2;
            if (Math.random() > 0.5) {
                d3 = d2;
                d4 = d;
            }
            Assert.assertEquals(Maths.logAdd((float)d3, (float)d4), (float)(d
                    + Math.log(1 + Math.exp(d2 - d))), 0.0001);
        }
    }
    
    public void testLogAddQuality() {
        for (int trial = 0; trial < 1000; trial++) {
            final double d = Math.random() * 100;
            final double d2 = d - 20 * Math.random();
            double d3 = d, d4 = d2;
            if (Math.random() > 0.5) {
                d3 = d2;
                d4 = d;
            }
            Assert.assertEquals(Maths.logAddQuality(d3, d4), d
                    + Math.log(1 + Math.exp(d2 - d)), 0);
        }
    }

}