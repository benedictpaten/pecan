/*
 * Created on Mar 21, 2005
 */
package bp.common.ds;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class ScrollingQueueTest
                               extends TestCase {
    ScrollingQueue sQ;

    public ScrollingQueueTest(final String s) {
        super(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        this.sQ = new ScrollingQueue(1, 5);
        super.setUp();
    }

    public void testAdd() {
        this.sQ.add("");
        this.sQ.add("1");
        this.sQ.add("2");
        this.sQ.add("3");
    }

    public void testGet() {
        try {
            this.sQ.get(5);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        this.sQ.add("");
        this.sQ.add("1");
        this.sQ.add("2");
        this.sQ.add("3");
        Assert.assertEquals(this.sQ.get(5), "");
        Assert.assertEquals(this.sQ.get(6), "1");
        Assert.assertEquals(this.sQ.get(7), "2");
        Assert.assertEquals(this.sQ.get(8), "3");
        for (int i = 0; i < 10000; i++) {
            this.sQ.add(new Integer(i));
        }
        for (int i = 0; i < 10000; i++) {
            Assert.assertEquals(this.sQ.get(9 + i), new Integer(i));
        }
    }

    public void testRemoveFirst() {
        this.sQ.add("");
        this.sQ.add("1");
        this.sQ.add("2");
        this.sQ.add("3");
        this.sQ.removeFirst();
        try {
            this.sQ.get(5);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Assert.assertEquals(this.sQ.get(6), "1");
        Assert.assertEquals(this.sQ.get(7), "2");
        Assert.assertEquals(this.sQ.get(8), "3");
        this.sQ.removeFirst();
        try {
            this.sQ.get(6);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Assert.assertEquals(this.sQ.get(7), "2");
        Assert.assertEquals(this.sQ.get(8), "3");
        this.sQ.removeFirst();
        try {
            this.sQ.get(7);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Assert.assertEquals(this.sQ.get(8), "3");
        this.sQ.removeFirst();
        try {
            this.sQ.get(8);
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        for (int i = 0; i < 10000; i++) {
            this.sQ.add(new Integer(i));
        }
        for (int i = 0; i < 10000; i++) {
            Assert.assertEquals(this.sQ.get(9 + i), new Integer(i));
            this.sQ.removeFirst();
            try {
                this.sQ.get(9 + i);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
        }
    }

    public void testSet() {
        try {
            this.sQ.set(5, "");
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        this.sQ.add("");
        this.sQ.add("1");
        Assert.assertEquals(this.sQ.get(5), "");
        this.sQ.set(5, "2");
        Assert.assertEquals(this.sQ.get(5), "2");
        this.sQ.removeFirst();
        try {
            this.sQ.set(5, "");
            Assert.fail();
        } catch (final ArrayIndexOutOfBoundsException e) {
            ;
        }
        this.sQ.set(6, "3");
        Assert.assertEquals(this.sQ.get(6), "3");
    }

    public void testFirstIndex() {
        Assert.assertEquals(this.sQ.firstIndex(), 5);
        this.sQ.add("");
        this.sQ.add("1");
        Assert.assertEquals(this.sQ.firstIndex(), 5);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.firstIndex(), 6);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.firstIndex(), 7);
        for (int i = 0; i < 10000; i++) {
            this.sQ.add(new Integer(i));
        }
        for (int i = 0; i < 10000; i++) {
            Assert.assertEquals(this.sQ.firstIndex(), 7 + i);
            this.sQ.removeFirst();
        }
    }

    public void testLastIndex() {
        Assert.assertEquals(this.sQ.lastIndex(), 5);
        this.sQ.add("");
        this.sQ.add("1");
        Assert.assertEquals(this.sQ.lastIndex(), 7);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.lastIndex(), 7);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.lastIndex(), 7);
        this.sQ.add("2");
        Assert.assertEquals(this.sQ.lastIndex(), 8);
        for (int i = 0; i < 10000; i++) {
            this.sQ.add(new Integer(i));
        }
        for (int i = 0; i < 10000; i++) {
            Assert.assertEquals(this.sQ.lastIndex(), 8 + 10000);
            this.sQ.removeFirst();
        }
    }

    public void testRemoveUpto() throws Exception {
        for (int trial = 0; trial < 100; trial++) {
            this.setUp();
            this.sQ.add("");
            this.sQ.add("1");
            this.sQ.add("2");
            this.sQ.add("3");
            this.sQ.get(5);
            this.sQ.removeUpto(6);
            try {
                this.sQ.get(5);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
            Assert.assertEquals(this.sQ.get(6), "1");
            Assert.assertEquals(this.sQ.get(7), "2");
            Assert.assertEquals(this.sQ.get(8), "3");
            this.sQ.get(6);
            this.sQ.removeUpto(7);
            try {
                this.sQ.get(6);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
            Assert.assertEquals(this.sQ.get(7), "2");
            Assert.assertEquals(this.sQ.get(8), "3");
            this.sQ.removeUpto(9);
            try {
                this.sQ.get(7);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
            try {
                this.sQ.get(8);
                Assert.fail();
            } catch (final ArrayIndexOutOfBoundsException e) {
                ;
            }
            for (int i = 0; i < 1000; i++) {
                this.sQ.add(new Integer(i));
            }
            for (int i = 0; i < 1000;) {
                final int j = i + (int) (Math.random() * (1001 - i));
                for (int k = i; k < j; k++) {
					Assert.assertEquals(this.sQ.get(9 + k), new Integer(k));
				}
                this.sQ.removeUpto(j + 9);
                for (int k = i; k < j; k++) {
                    try {
                        this.sQ.get(k + 9);
                        Assert.fail();
                    } catch (final ArrayIndexOutOfBoundsException e) {
                        ;
                    }
                }
                for (int k = j; (k < 1000) && (k < j + 100); k++) {
					Assert.assertEquals(this.sQ.get(9 + k), new Integer(k));
				}
                i = j;
            }
        }
    }
}