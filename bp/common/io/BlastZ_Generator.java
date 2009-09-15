/*
 * Created on Jun 10, 2005
 */
package bp.common.io;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bp.common.io.CigarParser_Generator.Cigar;
import bp.pecan.PolygonFiller;

/**
 * @author benedictpaten
 */
public class BlastZ_Generator {

    /**
     * See {@link BlastZ_Generator#convertToEdgeList(Cigar)}.
     * 
     * @author benedictpaten
     */
    public static class BlastZ {
       
        public int queryStart, queryEnd, targetStart, targetEnd,
                score;

        //public int queryStrand, targetStrand;

        public List operations;

        public BlastZ(final int queryStart, final int queryEnd,
                final int targetStart,
                final int targetEnd, final int score,
                final List operations) {
            this.queryStart = queryStart;
            this.queryEnd = queryEnd;
            this.targetStart = targetStart;
            this.targetEnd = targetEnd;
            this.score = score;
            this.operations = operations;
        }

        @Override
		public String toString() {
            final StringBuffer sB = new StringBuffer("blastz: "
                    + this.queryStart
                    + " "
                    + this.queryEnd
                    //+ " "
                    //+ (queryStrand == PLUS ? "+"
                    //        : queryStrand == MINUS ? "-" : ".")
                    + " "
                    + this.targetStart
                    + " "
                    + this.targetEnd
                    //+ " "
                    //+ (targetStrand == PLUS ? "+"
                    //        : targetStrand == MINUS ? "-" : ".")
                    + " " + this.score);
            for (final Iterator it=this.operations.iterator(); it.hasNext();) {
                sB.append(it.next());
            }
            return sB.toString();
        }

    }

    static Pattern scoreP = Pattern
        .compile("[\\s]*s ([0-9]+)[\\s]*");
    static Pattern beginP = Pattern
    .compile("[\\s]*b ([0-9]+) ([0-9]+)[\\s]*");
    static Pattern endP = Pattern
    .compile("[\\s]*e ([0-9]+) ([0-9]+)[\\s]*");
    static Pattern lP = Pattern
    .compile("[\\s]*l ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+)[\\s]*");
    public static BlastZ parseBlastZ(final LineNumberReader lNR)
            throws IOException {
        String s;
        while ((s = lNR.readLine()) != null) {
            if (s.equals("a {")) {
                //  a {
                //    s 20990
                //    b 3 3
                //    e 376 346
                //    l 3 3 272 272 84
                //    l 274 273 314 313 73
                //    l 344 314 376 346 70
                //  }
                //Cigar(String queryId, int queryStart, int queryEnd,
                //        int queryStrand, String targetId, int targetStart,
                //        int targetEnd, int targetStrand, int score,
                //        int[] operations)
                Matcher m = BlastZ_Generator.scoreP.matcher(lNR.readLine());
                m.matches();
                final int score = Integer.parseInt(m.group(1));
                m = BlastZ_Generator.beginP.matcher(lNR.readLine());
                m.matches();
                final int queryStart = Integer.parseInt(m.group(1))-1;
                final int targetStart = Integer.parseInt(m.group(2))-1;
                m = BlastZ_Generator.endP.matcher(lNR.readLine());
                m.matches();
                final int queryEnd = Integer.parseInt(m.group(1))-1;
                final int targetEnd = Integer.parseInt(m.group(2))-1;
                final List matches = new LinkedList();
                while (!(s = lNR.readLine()).equals("}")) {
                    m = BlastZ_Generator.lP.matcher(s);
                    m.matches();
                    matches.add(new PolygonFiller.Node(Integer.parseInt(m.group(1))-1, 
                            Integer.parseInt(m.group(2))-1, Integer.parseInt(m.group(4))-1, 1)); //Integer.parseInt(m.group(5))));
                }
                return new BlastZ(queryStart, queryEnd,
                        targetStart, targetEnd, score, matches);
            }
        }
        return null;
    }
}