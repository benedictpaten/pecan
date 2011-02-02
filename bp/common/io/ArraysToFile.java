/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Dec 14, 2005
 */
package bp.common.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import bp.common.fp.Generator;

public class ArraysToFile {

    public static final int writeOutArray(final Generator edgeList,
            final OutputStream oS) throws IOException {
        final DataOutputStream dIS = new DataOutputStream(oS);
        int[] iA;
        int i = 0;
        while ((iA = (int[]) edgeList.gen()) != null) {
            for (final int element : iA) {
				dIS.writeInt(element);
			}
            i++;
        }
        dIS.flush();
        return i;
    }

    public static final Generator readInArray(final InputStream iS,
            final int size, final int arraySize) {
        return new Generator() {
            DataInputStream dIS = new DataInputStream(iS);

            int j = size;

            public final Object gen() {
                if (this.j-- > 0) {
                    try {
                        final int[] iA = new int[arraySize];
                        for(int i=0; i<arraySize; i++) {
							iA[i] = this.dIS.readInt();
						}
                        return iA;
                    } catch (final IOException e) {
                        e.printStackTrace();
                        throw new IllegalStateException();
                    }
                }
                return null;
            }
        };
    }
}
