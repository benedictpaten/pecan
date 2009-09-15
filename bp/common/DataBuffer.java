package bp.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DataBuffer {
    File file;

    DataOutputStream out;

    int written = 0;

    public DataBuffer() {
        try {
            this.file = File.createTempFile("buffer", null, null);
            this.file.deleteOnExit();
            this.out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(this.file)));
        } catch (final java.io.IOException e) {
            throw new RuntimeException("Failed to create buffer file");
        }
    }

    public void write(final int n) {
        try {
            this.out.writeInt(n);
            this.written++;
        } catch (final java.io.IOException e) {
            throw new RuntimeException("Could not write out byte");
        }
    }

    public int[] getBackArray() {
        try {
            final int[] iA = new int[this.written];
            this.out.close();
            final DataInputStream in = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(this.file)));
            for (int i = 0; i < iA.length; i++) {
				iA[i] = in.readInt();
			}
            return iA;
        } catch (final java.io.IOException e) {
            throw new RuntimeException(
                    "Failed to get back array from file");
        }
    }

}