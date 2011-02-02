/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Mar 23, 2005
 */
package bp.common.fp;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class ProceduresTest
                           extends TestCase {
    
    public ProceduresTest(final String s) {
        super(s);
    }
     
    public void testRunProcedures() {
        final List<Object> l = new ArrayList<Object>();
        final Procedure[] pA = new Procedure[] {
                new Procedure() {
                    /* (non-Javadoc)
                     * @see bp.common.fp.Procedure#pro(java.lang.Object)
                     */
                    public void pro(Object o) {
                        l.add(o);
                        l.add(new Integer(5));
                    }
                },
                new Procedure() {
                    /* (non-Javadoc)
                     * @see bp.common.fp.Procedure#pro(java.lang.Object)
                     */
                    public void pro(Object o) {
                        l.add(o);
                        l.add(new Integer(6));
                    }
                }
        };
        Procedures.runProcedures(pA).pro(new Integer(4));
        Assert.assertEquals(new Integer(4), l.get(0));
        Assert.assertEquals(new Integer(5), l.get(1));
        Assert.assertEquals(new Integer(4), l.get(2));
        Assert.assertEquals(new Integer(6), l.get(3));
        Assert.assertEquals(l.size(), 4);
    }
}
