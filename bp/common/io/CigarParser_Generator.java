/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jun 10, 2005
 */
package bp.common.io;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bp.common.fp.Generator;
import bp.pecan.PolygonFiller;

/**
 * @author benedictpaten
 */
public class CigarParser_Generator {

    /**
     * See {@link CigarParser_Generator#convertToEdgeList(Cigar)}.
     * 
     * @author benedictpaten
     */
    public static class Cigar {
        public static final int MATCH = 0, INSERT = 1, DELETE = 2;

        public String queryId, targetId;

        public int queryStart, queryEnd, targetStart, targetEnd,
                score;

        public static final int PLUS = 0, MINUS = 1, NA = 2;

        public int queryStrand, targetStrand;

        public int[] operations;

        public Cigar(final String queryId, final int queryStart, final int queryEnd,
                final int queryStrand, final String targetId, final int targetStart,
                final int targetEnd, final int targetStrand, final int score,
                final int[] operations) {
            this.queryId = queryId;
            this.queryStart = queryStart;
            this.queryEnd = queryEnd;
            this.queryStrand = queryStrand;
            this.targetId = targetId;
            this.targetStart = targetStart;
            this.targetEnd = targetEnd;
            this.targetStrand = targetStrand;
            this.score = score;
            this.operations = operations;
        }

        @Override
		public String toString() {
            final StringBuffer sB = new StringBuffer("cigar: "
                    + this.queryId
                    + " "
                    + this.queryStart
                    + " "
                    + this.queryEnd
                    + " "
                    + (this.queryStrand == Cigar.PLUS ? "+"
                            : this.queryStrand == Cigar.MINUS ? "-" : ".")
                    + " "
                    + this.targetId
                    + " "
                    + this.targetStart
                    + " "
                    + this.targetEnd
                    + " "
                    + (this.targetStrand == Cigar.PLUS ? "+"
                            : this.targetStrand == Cigar.MINUS ? "-" : ".")
                    + " " + this.score);
            for (int i = 0; i < this.operations.length; i += 2) {
                switch (this.operations[i]) {
                case MATCH:
                    sB.append(" M " + this.operations[i + 1]);
                    break;
                case INSERT:
                    sB.append(" I " + this.operations[i + 1]);
                    break;
                case DELETE:
                    sB.append(" D " + this.operations[i + 1]);
                    break;
                }
            }
            return sB.toString();
        }

    }

    //static Pattern p = Pattern.compile("cigar: (.+) ([0-9]+) ([0-9]+) ([\\+\\-\\.]) (.+) ([0-9]+) ([0-9]+) ([\\+\\-\\.]) ([0-9]+)( (.*))*");
    static Pattern p = Pattern.compile("cigar:\\s+(.+)\\s+([0-9]+)\\s+([0-9]+)\\s+([\\+\\-\\.])\\s+(.+)\\s+([0-9]+)\\s+([0-9]+)\\s+([\\+\\-\\.])\\s+([0-9]+)(\\s+(.*)\\s*)*");

    /**
     * Parses a cigar sequence and places the operations in an array in the
     * format { op, length }xN. From the exonerate man page..
     * <li>-S | --showsugar true/false
     * <p>
     * Display "sugar" output for ungapped alignments. Sugar is Simple UnGapped
     * Alignment Report, which displays ungapped alignments one-per-line. The
     * sugar line starts with the string "sugar:" for easy extraction from the
     * output, and is followed by the the following 9 fields in the order below:
     * <ul>
     * <li>query_id
     * <p>
     * Query identifier
     * <li>query_start
     * <p>
     * Query position at alignment start
     * <li>query_end
     * <p>
     * Query position alignment end
     * <li>query_strand
     * <p>
     * Strand of query matched
     * <li>target_id
     * <p>|
     * <li>target_start
     * <p>| the same 4 fields target_end | for the target sequence
     * <li>target_strand
     * <p>|
     * <li>score
     * <p>
     * The raw alignment score
     * </ul>
     * <li>--showcigar
     * <p>
     * Show the alignments in "cigar" format. Cigar is a Compact Idiosyncratic
     * Gapped Alignment Report, which displays gapped alignments one-per-line.
     * The format starts with the same 9 fields as sugar output (see above), and
     * is followed by a series of <operation, length> pairs where operation is
     * one of match, insert or delete, and the length describes the number of
     * times this operation is repeated.
     * 
     * @param r
     * @return
     * @throws IOException
     */
    public static Cigar parseCigar(final LineNumberReader lNR)
            throws IOException {
        String s;
        while ((s = lNR.readLine()) != null) {
            final Matcher m = CigarParser_Generator.p.matcher(s);
            if (m.matches()) {
                int[] operations;
                if (m.group(11) != null) {
                	 //System.err.println(" This has gone wrong " + s + " ");
                    final String[] ops = m.group(11).split(" ");
                    operations = new int[ops.length];
                    for (int i = 0; i < operations.length; i += 2) {
                        if (ops[i].length() != 1) {
							throw new IllegalArgumentException();
						}
                        switch (ops[i].charAt(0)) {
                        case 'M':
                            operations[i] = Cigar.MATCH;
                            break;
                        case 'I':
                            operations[i] = Cigar.INSERT;
                            break;
                        case 'D':
                            operations[i] = Cigar.DELETE;
                            break;
                        default:
                            throw new IllegalStateException();
                        }
                        operations[i + 1] = Integer
                                .parseInt(ops[i + 1]);
                    }
                } else {
					operations = new int[0];
				}
                return new Cigar(m.group(1), Integer.parseInt(m
                        .group(2)), Integer.parseInt(m.group(3)),
                        CigarParser_Generator.getStrandCode(m.group(4)), m.group(5),
                        Integer.parseInt(m.group(6)), Integer
                                .parseInt(m.group(7)),
                        CigarParser_Generator.getStrandCode(m.group(8)), Integer.parseInt(m
                                .group(9)), operations);
            }
        }
        return null;
    }

    public static int getStrandCode(final String s) {
        if (s.equals("+")) {
			return Cigar.PLUS;
		} else if (s.equals("-")) {
			return Cigar.MINUS;
		} else if (s.equals(".")) {
			return Cigar.NA;
		} else {
			throw new IllegalArgumentException();
		}
    }

    /**
     * Creates an edge list containing only the matching diagonals from the
     * Cigar.
     * 
     * @param c
     * @return
     */
    public static Generator convertToEdgeList(final Cigar c) {
        return new Generator() {
            int indexSeq1 = c.queryStart, indexSeq2 = c.targetStart;

            int operationIndex;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (this.operationIndex < c.operations.length) {
                    final int op = c.operations[this.operationIndex];
                    final int opLength = c.operations[this.operationIndex + 1];
                    this.operationIndex += 2;
                    if (opLength == 0) {
						return this.gen();
					}
                    switch (op) {
                    case Cigar.MATCH:
                        final PolygonFiller.Node l = new PolygonFiller.Node(this.indexSeq1,
                                this.indexSeq2, this.indexSeq2 + opLength - 1,
                                1);
                        this.indexSeq1 += opLength;
                        this.indexSeq2 += opLength;
                        return l;
                    case Cigar.INSERT:
                        this.indexSeq1 += opLength;
                        return this.gen();
                    case Cigar.DELETE:
                        this.indexSeq2 += opLength;
                        return this.gen();
                    default:
                        throw new IllegalArgumentException();
                    }
                }
                return null;
            }
        };
    }
}