/*
 * Created on Feb 1, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public class ListQueueIteratorTest
                                  extends AbstractQueueIteratorTest {

    /**
     * Constructor for ListQueueIteratorTest.
     * @param arg0
     */
    public ListQueueIteratorTest(final String arg0) {
        super(arg0);
        this.q = new ListQueueIterator();
    }

}
