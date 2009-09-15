/*
 * Created on May 17, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public class Procedures_Int {

    /**
     * Does nothing
     * 
     * @author benedictpaten
     */
    public static Procedure_Int doNothing() {
        return new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(final int i) {
                ;
            }
        };
    }

    /**
     * @param ints
     * @return
     */
    public static Procedure_Int runProcedures(
            final Procedure_Int[] procedure_Ints) {
        return new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(final int i) {
                for (final Procedure_Int element : procedure_Ints) {
					element.pro(i);
				}
            }
        };
    }

    /**
     * Calls the given procedure upto and including the passed index.
     * @param pro procedure to call
     * @param start first index (inclusive)
     * @return
     */
    public static Procedure_Int uptoAndIncluding(
            final Procedure_Int pro, final int start) {
        return new Procedure_Int() {
            int i = start;
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(final int j) {
                	while(this.i <= j) {
						pro.pro(this.i++);
					}
            }
        };
    }
}