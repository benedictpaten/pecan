/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 20, 2006
 */
package bp.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import bp.common.fp.Generator;
import bp.common.fp.GeneratorTools;

/**
 *  
 *  ##maf version=1 scoring=tba.v8
 *  # tba.v8 (((human chimp) baboon) (mouse rat))
 *  # multiz.v7
 *  # maf_project.v5 _tba_right.maf3 mouse _tba_C
 *  # single_cov2.v4 single_cov2 /dev/stdin
 *  
 *  a score=23262.0
 *  s hg16.chr7 27578828 38 + 158545518 AAA-GGGAATGTTAACCAAATGA---ATTGTCTCTTACGGTG
 *  s panTro1.chr6 28741140 38 + 161576975 AAA-GGGAATGTTAACCAAATGA---ATTGTCTCTTACGGTG
 *  s baboon 116834 38 + 4622798 AAA-GGGAATGTTAACCAAATGA---GTTGTCTCTTATGGTG
 *  s mm4.chr6 53215344 38 + 151104725 -AATGGGAATGTTAAGCAAACGA---ATTGTCTCTCAGTGTG
 *  s rn3.chr4 81344243 40 + 187371129 -AA-GGGGATGCTAAGCCAATGAGTTGTTGTCTCTCAATGTG
 *
 *  a score=5062.0
 *  s hg16.chr7    27699739 6 + 158545518 TAAAGA
 *  s panTro1.chr6 28862317 6 + 161576975 TAAAGA
 *  s baboon         241163 6 +   4622798 TAAAGA
 *  s mm4.chr6     53303881 6 + 151104725 TAAAGA
 *  s rn3.chr4     81444246 6 + 187371129 taagga
 *  
 *  a score=6636.0
 *  s hg16.chr7    27707221 13 + 158545518 gcagctgaaaaca
 *  s panTro1.chr6 28869787 13 + 161576975 gcagctgaaaaca
 *  s baboon         249182 13 +   4622798 gcagctgaaaaca
 *  s mm4.chr6     53310102 13 + 151104725 ACAGCTGAAAATA
 * @author benedictpaten
 */

public class MAFOutputWriter {

        private PrintWriter pW;
        
        public final Pattern whiteSpacePattern = Pattern.compile("[\\s]+");
        
        public static final boolean ORIENTATION_FORWARD = true;
        
        public static final boolean ORIENTATION_BACKWARD = false;

        /**
         * Assumes the writer starts at the beginning of a line.
         * 
         * @throws IOException
         *  
         */
        public MAFOutputWriter(final OutputStream oS, final String version, final String scoring, final String program) throws IOException {
            this.pW = new PrintWriter(new OutputStreamWriter(oS));
            if(Debug.DEBUGCODE && (version == null)) {
				throw new IllegalStateException();
			}
            this.pW.print("##maf version=" + version);
            if(scoring != null) {
                this.pW.print(" scoring=");
                this.pW.print(scoring);
            }
            if(program != null) {
                this.pW.print(" program=");
                this.pW.print(program);
            }
            this.pW.write('\n');
        }
        
        public void writeInfoLine(final String info) {
            this.pW.print("# ");
            this.pW.print(info);
            this.pW.print("\n");
        }
       
        public void writeBlock(final float score, final int pass, final Generator gen, final String[] names, final int[] starts, final int[] sizes, final boolean[] orientations, final int[] parentSeqSize) {
            this.pW.print("\na score=");
            this.pW.print(score);
            if(pass > 0) {
                this.pW.print(" pass=");
                this.pW.print(pass);
            }
            final List l = (List)GeneratorTools.append(gen, new LinkedList());
            for(int i=0; i<names.length; i++) {
                this.pW.write('\n');
                this.pW.print("s ");
                this.pW.print(names[i].replaceAll("[\\s]+", "_"));
                this.pW.write(' ');
                this.pW.print(orientations[i] == MAFOutputWriter.ORIENTATION_FORWARD ? starts[i] : parentSeqSize[i] - (starts[i] + sizes[i]));
                this.pW.write(' ');
                this.pW.print(sizes[i]);
                this.pW.write(' ');
                this.pW.print(orientations[i] == MAFOutputWriter.ORIENTATION_FORWARD ? "+" : "-");
                this.pW.write(' ');
                this.pW.print(parentSeqSize[i]);
                this.pW.write(' ');
                for(final Iterator it=l.iterator();it.hasNext();) {
                    final int[] iA = (int[])it.next();
                    final int j = iA[i];
                    this.pW.write(j == Integer.MAX_VALUE ? '-' : iA[i]);
                }
            }
            this.pW.write('\n');   
        }

        /**
         * Call this when finished writing the sequence. Flushes the stream and
         * inserts a new line char.
         *  
         */
        public void end() throws IOException {
            this.pW.flush();
        }

        /**
         * Like end, but also closes the stream.
         * 
         * @throws IOException
         */
        public void endAndClose() throws IOException {
            this.end();
            this.pW.close();
        }
       
}
