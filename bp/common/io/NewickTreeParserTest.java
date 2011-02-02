/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on May 29, 2005
 */
package bp.common.io;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Generator;

/**
 * @author benedictpaten
 */
public class NewickTreeParserTest
                                 extends TestCase {

    public static NewickTreeParser.Node getRandomTreeRoot() {
        final NewickTreeParser.Node n = new NewickTreeParser.Node();
        NewickTreeParserTest.getRandomTree(n);
        final NewickTreeParser.Node m = new NewickTreeParser.Node();
        n.addNode(m);
        return n;
    }

    public static void getRandomTree(
            final NewickTreeParser.Node parent) {
        final String[] randomStrings = new String[] { "", "boo", "foo",
                "foo bank" };
        //if (Math.random() > 0.5)
        parent.edgeLength = (int) (Math.random() * 100);
        if (Math.random() > 0.7) {
            do {
                final NewickTreeParser.Node n = new NewickTreeParser.Node(parent);
                NewickTreeParserTest.getRandomTree(n);
            } while (Math.random() > 0.7);
            if (Math.random() > 0.5) {
				parent.o = randomStrings[(int) (Math.random() * randomStrings.length)];
			}
        } else {
			parent.o = randomStrings[(int) (Math.random() * randomStrings.length)];
		}
    }

    public void testNewickTreeParser() {
        for (int trial = 0; trial < 1000; trial++) {
            final NewickTreeParser.Node n = NewickTreeParserTest.getRandomTreeRoot();
            final String s = n.toString();
            Debug.pl(" " + s);
            final NewickTreeParser nTP = new NewickTreeParser(
                    NewickTreeParser.tokenise(new StringReader(s)));
            final NewickTreeParser.Node m = nTP.tree;
            Assert.assertEquals(n.toString(), m.toString());
            this.checkParentConnections(null, n);
        }
    }

    public void checkParentConnections(final NewickTreeParser.Node parent,
            final NewickTreeParser.Node n) {
        if (parent != null) {
            Assert.assertTrue(parent.getNodes().contains(n));
        }
        for (final Iterator<Object> it = n.getNodes().iterator(); it.hasNext();) {
			this.checkParentConnections(n, (NewickTreeParser.Node) it
                    .next());
		}
    }

    public void testTokenise() throws IOException {
        final Generator gen = NewickTreeParser
                .tokenise(new StringReader(
                        "((b#$@oo)b!ar :1.5,('foo man',ma//n:1.6 ,(choo)	) )'':2.0;"));
        final String[] sA2 = new String[] { "(", "(", "b#$@oo", ")", "b!ar",
                ":", "1.5", ",", "(", "foo man", ",", "ma//n", ":",
                "1.6", ",", "(", "choo", ")", ")", ")", "", ":",
                "2.0", ";" };
        for (final String element : sA2) {
        	//System.out.println(" gen " + element + " " + gen.gen());
			Assert.assertEquals((String) gen.gen(), element);
		}
        Assert.assertEquals(gen.gen(), null);
    }

    public void testCommentEater() throws IOException {
        Generator gen = NewickTreeParser
                .tokenise(new StringReader(
                        "((boo)bar :1.5,('foo man'[bar],man:[funky]1.6 ,(choo)	[sit]) )bat:2.0;[sat]"));
        gen = NewickTreeParser.commentEater(gen);
        final String[] sA2 = new String[] { "(", "(", "boo", ")", "bar",
                ":", "1.5", ",", "(", "foo man", ",", "man", ":",
                "1.6", ",", "(", "choo", ")", ")", ")", "bat", ":",
                "2.0", ";" };
        for (final String element : sA2) {
			Assert.assertEquals((String) gen.gen(), element);
		}
        Assert.assertEquals(gen.gen(), null);
    }

    public void testAddNode() {
        final NewickTreeParser.Node n = new NewickTreeParser.Node();
        for (int i = 1; i < 100; i++) {
            NewickTreeParser.Node m;
            if(Math.random() > 0.5) {
				m = new NewickTreeParser.Node(n);
			} else {
                m = new NewickTreeParser.Node();
                n.addNode(m);
            }
            Assert.assertEquals(m.getParent(), n);
            Assert.assertTrue(n.getNodes().contains(m));
            Assert.assertEquals(n.getNodes().size(), i);
        }
        final NewickTreeParser.Node m = new NewickTreeParser.Node();
        final NewickTreeParser.Node p = new NewickTreeParser.Node(m);
        try {
            n.addNode(p);
            Assert.fail();
        }
        catch(final IllegalStateException e) {
        }
    }
    
    public void testRemoveNode() {
        final NewickTreeParser.Node n = new NewickTreeParser.Node();
        for (int i = 0; i < 100; i++) {
            final NewickTreeParser.Node m = new NewickTreeParser.Node();
            n.addNode(m);
        }
        for(int i=99; i>=0; i--) {
            final NewickTreeParser.Node m = (NewickTreeParser.Node)n.getNodes().get(i);
            Assert.assertEquals(m.getParent(), n);
            Assert.assertTrue(n.getNodes().contains(m));
            Assert.assertEquals(n.getNodes().size(), i+1);
            n.removeNode(i);
            Assert.assertEquals(m.getParent(), null);
            Assert.assertFalse(n.getNodes().contains(m));
            Assert.assertEquals(n.getNodes().size(), i);
            try {
                n.removeNode(n.getNodes().size());
                Assert.fail();
            }
            catch(final IndexOutOfBoundsException e) {
            }
        }
    }
    
    public void testSetNode() {
        final NewickTreeParser.Node n = new NewickTreeParser.Node();
        for (int i = 0; i < 100; i++) {
            final NewickTreeParser.Node m = new NewickTreeParser.Node();
            n.addNode(m);
        }
        for(int i=99; i>=0; i--) {
            final NewickTreeParser.Node m = (NewickTreeParser.Node)n.getNodes().get(i);
            Assert.assertEquals(m.getParent(), n);
            Assert.assertTrue(n.getNodes().contains(m));
            Assert.assertEquals(n.getNodes().size(), 100);
            final NewickTreeParser.Node o = new NewickTreeParser.Node();
            Assert.assertEquals(m, n.setNode(i, o));
            Assert.assertEquals(m.getParent(), null);
            Assert.assertFalse(n.getNodes().contains(m));
            Assert.assertEquals(n.getNodes().size(), 100);
            Assert.assertEquals(o.getParent(), n);
            Assert.assertTrue(n.getNodes().contains(o));
            Assert.assertEquals(n.getNodes().size(), 100);
        }
    }
    
}