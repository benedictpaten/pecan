/*
 * Created on Dec 14, 2005
 */
package bp.common.fp;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

public class GeneratorToolsTest
                               extends TestCase {

    
    public void testAppend() {
        List l = new ArrayList();
        l.add("1");
        l = (List) GeneratorTools.append(Generators.arrayGenerator(new Object[] { "2" })
                , l);
        Assert.assertEquals(l.get(0), "1");
        Assert.assertEquals(l.get(1), "2");
        Assert.assertEquals(l.size(), 2);
    }
    
}
