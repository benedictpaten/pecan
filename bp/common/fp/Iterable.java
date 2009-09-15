/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

import java.util.Iterator;

/**
 * @author benedictpaten
 */
public interface Iterable {
    /**
     * Creates a {@link Iterator} for the object.
     * @return
     */
    public Iterator iterator();
}
