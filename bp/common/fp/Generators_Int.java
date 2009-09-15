/*
 * Created on Apr 3, 2006
 */
package bp.common.fp;

public final class Generators_Int {

    public static final Generator_Int constant(final int constant) {
        return new Generator_Int() {
            public int gen() {
                return constant;
            }
        };
    }

    public static Generator_Int arrayGen(final int[] iA, final int length) {
        return new Generator_Int() {
            int i=0;
            
            public int gen() {
                while(this.i < length) {
					return iA[this.i++];
				}
                return Integer.MAX_VALUE;
            }
        };
    }

}
