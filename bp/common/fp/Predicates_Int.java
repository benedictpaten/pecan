/*
 * Created on Sep 19, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public class Predicates_Int {

    public static Predicate_Int rCurry(final Predicate_Int_2Args test, final int i) {
        return new Predicate_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_Int#test(int)
             */
            public final boolean test(final int j) {
                return test.test(j, i);
            }
        };
    }
    
    public static Predicate_Int lCurry(final Predicate_Int_2Args test, final int i) {
        return new Predicate_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_Int#test(int)
             */
            public final boolean test(final int j) {
                return test.test(i, j);
            }
        };
    }

}