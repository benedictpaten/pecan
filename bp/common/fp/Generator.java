/*
 * Created on Feb 16, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public interface Generator {

    /**
     * Method should return a sequence of objects and then null when exhausted.
     * The method should not throw an exception to indicate it is exhausted.
     * Generators may return null and then later return non-null values.
     * 
     * @return
     */
    Object gen();
}