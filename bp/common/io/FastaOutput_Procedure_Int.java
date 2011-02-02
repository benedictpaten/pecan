/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jun 10, 2005
 */
package bp.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import bp.common.fp.Generator_Int;
import bp.common.fp.Procedure_Int;

/**
 * Writes out a fasta file progressively, casting int arguments to chars.
 * 
 * @author benedictpaten
 */
public class FastaOutput_Procedure_Int implements Procedure_Int {
    private OutputStream oS;

    private int index = 0;

    private static final int lineWidth = 60;

    /**
     * Assumes the writer starts at the beginning of a line.
     * 
     * @throws IOException
     *  
     */
    public FastaOutput_Procedure_Int(final OutputStream oS, final String iD) throws IOException {
        this.oS = oS;
        oS.write('>');
        final byte[] bA = iD.getBytes();
        for (final byte element : bA) {
			oS.write(element);
		}
        oS.write('\n');
    }
   
    /*
     * (non-Javadoc)
     * 
     * @see bp.common.fp.Procedure_Int#pro(int)
     */
    public void pro(final int i) {
        try {
            if (this.index >= FastaOutput_Procedure_Int.lineWidth) {
                this.oS.write('\n');
                this.oS.write((char) i);
                this.index = 1;
            } else {
                this.oS.write((char) i);
                this.index++;
            }
        } catch (final IOException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Call this when finished writing the sequence. Flushes the stream and
     * inserts a new line char.
     *  
     */
    public void end() throws IOException {
        this.oS.write('\n');
        this.oS.flush();
    }

    /**
     * Like end, but also closes the stream.
     * 
     * @throws IOException
     */
    public void endAndClose() throws IOException {
        this.end();
        this.oS.close();
    }
    
    public static final void writeFile(final OutputStream oS, final String title, final byte[] cA, final int start,
            final int length) throws FileNotFoundException, IOException {
        final FastaOutput_Procedure_Int fOP = new FastaOutput_Procedure_Int(oS, title);
        for(int i=start; i<start+length; i++) {
			fOP.pro(cA[i]);
		}
        fOP.end();
    }
    
    public static final void writeFile(final OutputStream oS, final String title, final Generator_Int gen, final int nullValue) throws FileNotFoundException, IOException {
        final FastaOutput_Procedure_Int fOP = new FastaOutput_Procedure_Int(oS, title);
        int i;
        while((i = gen.gen()) != nullValue) {
			fOP.pro(i);
		}
        fOP.end();
    }

}