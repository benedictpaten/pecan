/*
 * Created on Aug 12, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public class Procedures_Int_2Args {

    public static Procedure_Int_2Args flipArguments(final Procedure_Int_2Args pro) {
        return new Procedure_Int_2Args() {
            /* (non-Javadoc)
             * @see bp.common.fp.Procedure_Int_2Args#pro(int, int)
             */
            public void pro(final int i, final int j) {
                	pro.pro(j, i);
            }
        };
    }

}
