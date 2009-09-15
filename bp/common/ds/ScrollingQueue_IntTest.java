/*
 * Created on Mar 21, 2005
 */
package bp.common.ds;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author benedictpaten
 */
public class ScrollingQueue_IntTest
                               extends TestCase {
    //ScrollingQueue_Int sQ;
    
    ScrollingQueue_Int sQ;
    
    public ScrollingQueue_IntTest(final String s) {
        super(s);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        //sQ = new ScrollingQueue_Int(new ScrollingQueue_Int(0, 5), 0);
        this.sQ = new ScrollingQueue_Int(0, 5, true);
        super.setUp();
    }
    
    
    public void testAdd() {
        this.sQ.add(0);
        this.sQ.add(1);
        this.sQ.add(2);
        this.sQ.add(3);
    }
    
    public void testGet() {
        try {
            this.sQ.get(5);
            Assert.fail();
        }
        catch(final ArrayIndexOutOfBoundsException e) {
            ;
        }
        this.sQ.add(0);
        this.sQ.add(1);
        this.sQ.add(2);
        this.sQ.add(3);
        Assert.assertEquals(this.sQ.get(5), 0);
        Assert.assertEquals(this.sQ.get(6), 1);
        Assert.assertEquals(this.sQ.get(7), 2);
        Assert.assertEquals(this.sQ.get(8), 3);
        for(int i=0; i<10000; i++) {
            this.sQ.add(i);
        }
        for(int i=0; i<10000; i++) {
            Assert.assertEquals(this.sQ.get(9+i), i);
        }
    }
    
    public void testRemoveFirst() {
        this.sQ.add(0);
        this.sQ.add(1);
        this.sQ.add(2);
        this.sQ.add(3);
        this.sQ.removeFirst();
        try {
            this.sQ.get(5);
            Assert.fail();
        }
        catch(final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Assert.assertEquals(this.sQ.get(6), 1);
        Assert.assertEquals(this.sQ.get(7), 2);
        Assert.assertEquals(this.sQ.get(8), 3);
        this.sQ.removeFirst();
        try {
            this.sQ.get(6);
            Assert.fail();
        }
        catch(final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Assert.assertEquals(this.sQ.get(7), 2);
        Assert.assertEquals(this.sQ.get(8), 3);
        this.sQ.removeFirst();
        try {
            this.sQ.get(7);
            Assert.fail();
        }
        catch(final ArrayIndexOutOfBoundsException e) {
            ;
        }
        Assert.assertEquals(this.sQ.get(8), 3);
        this.sQ.removeFirst();
        try {
            this.sQ.get(8);
            Assert.fail();
        }
        catch(final ArrayIndexOutOfBoundsException e) {
            ;
        }
        for(int i=0; i<10000; i++) {
            this.sQ.add(i);
        }
        for(int i=0; i<10000; i++) {
            Assert.assertEquals(this.sQ.get(9+i), i);
            this.sQ.removeFirst();
            try {
                this.sQ.get(9+i);
                Assert.fail();
            }
            catch(final ArrayIndexOutOfBoundsException e) {
                ;
            }
        }
    }
    
    public void testSet() {
        try {
            this.sQ.set(5, 0);
            Assert.fail();
        }
        catch(final ArrayIndexOutOfBoundsException e) {
            ;
        }
        this.sQ.add(0);
        this.sQ.add(1);
        Assert.assertEquals(this.sQ.get(5), 0);
        this.sQ.set(5, 2);
        Assert.assertEquals(this.sQ.get(5), 2);
        this.sQ.removeFirst();
        try {
            this.sQ.set(5, 0);
            Assert.fail();
        }
        catch(final ArrayIndexOutOfBoundsException e) {
            ;
        }
        this.sQ.set(6, 3);
        Assert.assertEquals(this.sQ.get(6), 3);
    }
    
    public void testFirstIndex() {
        Assert.assertEquals(this.sQ.firstIndex(), 5);
        this.sQ.add(0);
        this.sQ.add(1);
        Assert.assertEquals(this.sQ.firstIndex(), 5);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.firstIndex(), 6);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.firstIndex(), 7);
        for(int i=0; i<10000; i++) {
            this.sQ.add(i);
        }
        for(int i=0; i<10000; i++) {
            Assert.assertEquals(this.sQ.firstIndex(), 7+i);
            this.sQ.removeFirst();
        }
    }
    
    public void testLastIndex() {
        Assert.assertEquals(this.sQ.lastIndex(), 5);
        this.sQ.add(0);
        this.sQ.add(1);
        Assert.assertEquals(this.sQ.lastIndex(), 7);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.lastIndex(), 7);
        this.sQ.removeFirst();
        Assert.assertEquals(this.sQ.lastIndex(), 7);
        this.sQ.add(2);
        Assert.assertEquals(this.sQ.lastIndex(), 8);
        for(int i=0; i<10000; i++) {
            this.sQ.add(i);
        }
        for(int i=0; i<10000; i++) {
            Assert.assertEquals(this.sQ.lastIndex(), 8+10000);
            this.sQ.removeFirst();
        }
    }
    
}
