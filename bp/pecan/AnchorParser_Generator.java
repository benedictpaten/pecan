/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on May 24, 2005
 */
package bp.pecan;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import bp.common.fp.Generator;

public final class AnchorParser_Generator {

    public static final int writeOutEdgeList(final Generator edgeList,
            final OutputStream oS) throws IOException {
        final DataOutputStream dIS = new DataOutputStream(oS);
        PolygonFiller.Node m;
        int i = 0;
        while ((m = (PolygonFiller.Node) edgeList.gen()) != null) {
            dIS.writeInt(m.x);
            dIS.writeInt(m.y);
            dIS.writeInt(m.yMax);
            dIS.writeInt(m.z);
            i++;
        }
        dIS.flush();
        return i;
    }

    public static final Generator readInEdgeList(final InputStream iS,
            final int i) {
        return new Generator() {
            DataInputStream dIS = new DataInputStream(iS);

            int j = i;

            public final Object gen() {
                if (this.j-- > 0) {
                    try {
                        return new PolygonFiller.Node(this.dIS.readInt(),
                                this.dIS.readInt(), this.dIS.readInt(), this.dIS
                                        .readInt());
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