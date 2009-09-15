package bp.common.ds;

import bp.common.fp.Generator_Int;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_NoArgs;

/**
 * Nicknacks used with scrolling queues.
 * 
 * @author benedictpaten
 */
public final class ScrollingQueue_IntTools {

    /**
     * Adds values from generator into scrolling queue upto the given index
     * (exclusive)
     * 
     * @param gen
     * @param startIndex
     *            i
     * @param sQ
     * @return
     */
    public static final Procedure_Int fillFromGenerator(final Generator_Int gen,
            final ScrollingQueue_Int sQ) {
        return new Procedure_Int() {

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public final void pro(final int i) {
                while (sQ.lastIndex() < i) {
					sQ.add(gen.gen());
				}
            }
        };
    }

    /**
     * An adaptor for {@link ScrollingQueue_Int#add(int)}.
     * 
     * @param sQ
     * @return
     */
    public static final Procedure_Int add(final ScrollingQueue_Int sQ) {
        return new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public final void pro(final int i) {
                sQ.add(i);
            }
        };
    }

    /**
     * An adaptor for {@link ScrollingQueue_Int#removeFirst()}
     * 
     * @param sQ
     * @return
     */
    public static final Procedure_NoArgs removeFirst(final ScrollingQueue_Int sQ) {
        return new Procedure_NoArgs() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_NoArgs#pro()
             */
            public final void pro() {
                sQ.removeFirst();
            }
        };
    }
}