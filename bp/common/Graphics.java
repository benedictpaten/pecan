package bp.common;

import java.awt.Button;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.io.IOException;

public class Graphics {

    public static void displayArrayAsImage(final int[] iA, final int width,
            final int height) {
        final Component c = new Button();
        final Image img = c.createImage(new MemoryImageSource(width,
                height, iA, 0, width));
        new Viewer(img);
    }

    public static void writeArrayAsImage(final int[] iA, final int width,
            final int height, final String file) throws java.io.IOException {
        final int[] iA2 = new int[(width + 1) * (height + 1)];
        int k = 0;
        for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				iA2[i * (width + 1) + j] = iA[k++];
			}
		}
        final Component c = new Button();
        final Image img = c.createImage(new MemoryImageSource(width + 1,
                height + 1, iA2, 0, width + 1));
        final BMPFile bmpFile = new BMPFile();
        bmpFile.saveBitmap(file, img, width + 1, height + 1);
    }

    public static void main(final String[] args) throws IOException {
        final int[] iA = new int[1000 * 1000];
        for (int i = 0; i < iA.length / 2; i++) {
			iA[i] = Integer.MIN_VALUE;
		}
        for (int i = iA.length / 2; i < iA.length; i++) {
			iA[i] = Integer.MAX_VALUE;
		}
        //displayArrayAsImage(iA, 1000, 1000);
        Graphics.writeArrayAsImage(iA, 1000, 1000, "output.bmp");
    }

}