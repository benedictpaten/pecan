/*
 * Created on Feb 23, 2005
 */
package bp.common.fp;


/**
 * @author benedictpaten
 */
public class Functions_Int {

    /**
     * Returns input.
     * @return
     */
    public static Function_Int doNothing() {
        return new Function_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int#fn(int)
             */
            public int fn(final int x) {
                return x;
            }
        };
    }
    
    
}