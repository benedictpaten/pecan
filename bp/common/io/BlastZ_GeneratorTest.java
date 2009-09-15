/*
 * Created on Jun 10, 2005
 */
package bp.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.pecan.PolygonFiller;
import bp.pecan.PolygonFillerTest;

/**
 * @author benedictpaten
 */
public class BlastZ_GeneratorTest
                                 extends TestCase {
    public void testCigarParser() throws IOException {
        for (int test = 0; test < 1000; test++) {
            final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
            final List l = PolygonFillerTest.convertOldEdgeListToNewEdgeList(PolygonFillerTest.
                    makeRandomEdgeList(0.9, 0.9,
                    (int) (Math.random() * 100)));
            final List l2 = PolygonFillerTest.convertOldEdgeListToNewEdgeList(PolygonFillerTest.
                    makeRandomEdgeList(0.9, 0.9,
                    (int) (Math.random() * 100)));
            final int score = (int) (Math.random() * 1000);
            final int score2 = (int) (Math.random() * 1000);
            this.writeOutEdgeList(l, score, bAOS);
            this.writeOutEdgeList(l2, score2, bAOS);
            final LineNumberReader lNR = new LineNumberReader(new InputStreamReader(
                    new ByteArrayInputStream(bAOS
                            .toByteArray())));
            final BlastZ_Generator.BlastZ blastZ = BlastZ_Generator
                    .parseBlastZ(lNR);
            final BlastZ_Generator.BlastZ blastZ2 = BlastZ_Generator
            .parseBlastZ(lNR);
            Assert.assertEquals(score, blastZ.score);
            Assert.assertEquals(100, blastZ.queryStart);
            Assert.assertEquals(200, blastZ.queryEnd);
            Assert.assertEquals(300, blastZ.targetStart);
            Assert.assertEquals(400, blastZ.targetEnd);
            PolygonFillerTest.listsEquals(PolygonFillerTest.convertNewEdgeListToOldEdgeList(l.iterator()),
                    PolygonFillerTest.convertNewEdgeListToOldEdgeList(blastZ.operations.iterator()), false);
            Assert.assertEquals(score2, blastZ2.score);
            Assert.assertEquals(100, blastZ2.queryStart);
            Assert.assertEquals(200, blastZ2.queryEnd);
            Assert.assertEquals(300, blastZ2.targetStart);
            Assert.assertEquals(400, blastZ2.targetEnd);
            PolygonFillerTest.listsEquals(PolygonFillerTest.convertNewEdgeListToOldEdgeList(l2.iterator()),
                    PolygonFillerTest.convertNewEdgeListToOldEdgeList(blastZ2.operations.iterator()), false);
        }
    }

    public void writeOutEdgeList(final List l, final int score, final ByteArrayOutputStream bAOS) {
        final PrintWriter pW = new PrintWriter(new OutputStreamWriter(bAOS));
        pW.println("a {");
        pW.println("s " + score);
        pW.println("b 101 301");
        pW.println("e 201 401");
        for (final Iterator it = l.iterator(); it.hasNext();) {
            final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
            pW.println("l " + (n.x+1) + " " + (n.y+1) + " " + (n.x + n.yMax - n.y + 1) + " " + (n.yMax+1) + " " + n.z);
        }
        pW.println("}");
        pW.flush();
    }
}