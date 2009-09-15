/*
 * Created on Jan 28, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public interface Predicate {

    /**
     * Test if an object meets a requirement.
     * 
     * @param o
     *            object to which to apply test
     * @return
     */
    boolean test(Object o);
}