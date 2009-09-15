/*
 * Created on Oct 21, 2005
 */
package bp.common.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bp.common.fp.Generator;

/**
 * @author benedictpaten
 */
public class MultiFastaParser_Generator implements Generator {
    private final FastaParser_Generator_Int[] fPGA;
    private final InputStream[] iSA;
    private boolean finished;
    private final String[] iDs;

    public MultiFastaParser_Generator(final String mfa) throws IOException {
        final List<Integer> starts = new LinkedList<Integer>();
        InputStream iS;
        iS = new BufferedInputStream(new FileInputStream(mfa));
        int i = 0, j;
        while ((j = iS.read()) != -1) {
            if (j == '>') {
                starts.add(new Integer(i));
            }
            i++;
        }
        iS.close();
        i = 0;
        this.fPGA = new FastaParser_Generator_Int[starts.size()];
        this.iDs = new String[starts.size()];
        this.iSA = new InputStream[this.fPGA.length];
        for (final Iterator<Integer> it = starts.iterator(); it.hasNext();) {
            j = it.next().intValue();
            iS = new FileInputStream(mfa);
            iS.skip(j);
            iS = new BufferedInputStream(iS);
            this.iSA[i] = iS;
            final FastaParser_Generator_Int fPG = new FastaParser_Generator_Int(
                    iS, Integer.MAX_VALUE);
            this.fPGA[i] = fPG;
            this.iDs[i++] = fPG.getFastaID();
        }
        this.finished = this.fPGA.length == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bp.common.fp.Generator#gen()
     */
    public Object gen() {
        if(this.finished) {
			return null;
		}
        final int[] iA = new int[this.fPGA.length];
        for (int i = 0; i < this.fPGA.length; i++) {
            iA[i] = this.fPGA[i].gen();
        }
        if (iA[0] == Integer.MAX_VALUE) {
            this.finished = true;
            if(false && Debug.DEBUGCODE) {
                for (int i = 1; i < iA.length; i++) {
					if (iA[i] != Integer.MAX_VALUE) {
						throw new IllegalStateException();
					}
				}
            }
            for (final InputStream element : this.iSA) {
				try {
                    element.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                }
			}
            return null;
        }
        return iA;
    }
    
    public void close() throws IOException {
        for (final InputStream element : this.iSA) {
			element.close();
		}
    }

    public String[] getFastaIDs() {
        return this.iDs;
    }
}