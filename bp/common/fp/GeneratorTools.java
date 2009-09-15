/*
 * Created on Dec 14, 2005
 */
package bp.common.fp;

import java.util.Collection;

public final class GeneratorTools {

    public static final Collection append(final Generator gen, final Collection c) {
        Object o;
        while ((o = gen.gen()) != null) {
			c.add(o);
		}
        return c;
    }
}
