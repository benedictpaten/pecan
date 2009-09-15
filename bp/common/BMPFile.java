package bp.common;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.FileOutputStream;

public class BMPFile
                    extends Component {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1386104588557804935L;

	//--- Private constants
    private final static int BITMAPFILEHEADER_SIZE = 14;

    private final static int BITMAPINFOHEADER_SIZE = 40;

    private final byte bfType[] = { 'B', 'M' };

    private int bfSize = 0;

    private final int bfReserved1 = 0;

    private final int bfReserved2 = 0;

    private final int bfOffBits = BMPFile.BITMAPFILEHEADER_SIZE
            + BMPFile.BITMAPINFOHEADER_SIZE;

    private final int biSize = BMPFile.BITMAPINFOHEADER_SIZE;

    private int biWidth = 0;

    private int biHeight = 0;

    private final int biPlanes = 1;

    private final int biBitCount = 24;

    private final int biCompression = 0;

    private int biSizeImage = 0x030000;

    private final int biXPelsPerMeter = 0x0;

    private final int biYPelsPerMeter = 0x0;

    private final int biClrUsed = 0;

    private final int biClrImportant = 0;

    //--- Bitmap raw data
    private int bitmap[];

    //--- File section
    private FileOutputStream fo;

    //--- Default constructor
    public BMPFile() {
    }

    public void saveBitmap(final String parFilename, final Image parImage,
            final int parWidth, final int parHeight) {
        try {
            this.fo = new FileOutputStream(parFilename);
            this.save(parImage, parWidth, parHeight);
            this.fo.close();
        } catch (final Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    /*
     * The saveMethod is the main method of the process. This method
     * will call the convertImage method to convert the memory image
     * to a byte array; method writeBitmapFileHeader creates and
     * writes the bitmap file header; writeBitmapInfoHeader creates
     * the information header; and writeBitmap writes the image.
     *  
     */
    private void save(final Image parImage, final int parWidth, final int parHeight) {
        try {
            this.convertImage(parImage, parWidth, parHeight);
            this.writeBitmapFileHeader();
            this.writeBitmapInfoHeader();
            this.writeBitmap();
        } catch (final Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    /*
     * convertImage converts the memory image to the bitmap format
     * (BRG). It also computes some information for the bitmap info
     * header.
     *  
     */
    private boolean convertImage(final Image parImage, final int parWidth,
            final int parHeight) {
        int pad;
        this.bitmap = new int[parWidth * parHeight];
        final PixelGrabber pg = new PixelGrabber(parImage, 0, 0, parWidth,
                parHeight, this.bitmap, 0, parWidth);
        try {
            pg.grabPixels();
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return (false);
        }
        pad = (4 - ((parWidth * 3) % 4)) * parHeight;
        this.biSizeImage = ((parWidth * parHeight) * 3) + pad;
        this.bfSize = this.biSizeImage + BMPFile.BITMAPFILEHEADER_SIZE
                + BMPFile.BITMAPINFOHEADER_SIZE;
        this.biWidth = parWidth;
        this.biHeight = parHeight;
        return (true);
    }

    /*
     * writeBitmap converts the image returned from the pixel grabber
     * to the format required. Remember: scan lines are inverted in a
     * bitmap file!
     * 
     * Each scan column must be padded to an even 4-byte boundary.
     */
    private void writeBitmap() {
        int size;
        int value;
        int j;
        int i;
        int rowCount;
        int rowIndex;
        int lastRowIndex;
        int pad;
        int padCount;
        final byte rgb[] = new byte[3];
        size = (this.biWidth * this.biHeight) - 1;
        pad = 4 - ((this.biWidth * 3) % 4);
        if (pad == 4) {
			pad = 0; // <==== Bug correction
		}
        rowCount = 1;
        padCount = 0;
        rowIndex = size - this.biWidth;
        lastRowIndex = rowIndex;
        try {
            for (j = 0; j < size; j++) {
                value = this.bitmap[rowIndex];
                rgb[0] = (byte) (value & 0xFF);
                rgb[1] = (byte) ((value >> 8) & 0xFF);
                rgb[2] = (byte) ((value >> 16) & 0xFF);
                this.fo.write(rgb);
                if (rowCount == this.biWidth) {
                    padCount += pad;
                    for (i = 1; i <= pad; i++) {
                        this.fo.write(0x00);
                    }
                    rowCount = 1;
                    rowIndex = lastRowIndex - this.biWidth;
                    lastRowIndex = rowIndex;
                } else {
					rowCount++;
				}
                rowIndex++;
            }
            //--- Update the size of the file
            this.bfSize += padCount - pad;
            this.biSizeImage += padCount - pad;
        } catch (final Exception wb) {
            wb.printStackTrace();
        }
    }

    /*
     * writeBitmapFileHeader writes the bitmap file header to the
     * file.
     *  
     */
    private void writeBitmapFileHeader() {
        try {
            this.fo.write(this.bfType);
            this.fo.write(this.intToDWord(this.bfSize));
            this.fo.write(this.intToWord(this.bfReserved1));
            this.fo.write(this.intToWord(this.bfReserved2));
            this.fo.write(this.intToDWord(this.bfOffBits));
        } catch (final Exception wbfh) {
            wbfh.printStackTrace();
        }
    }

    /*
     * 
     * writeBitmapInfoHeader writes the bitmap information header to
     * the file.
     *  
     */
    private void writeBitmapInfoHeader() {
        try {
            this.fo.write(this.intToDWord(this.biSize));
            this.fo.write(this.intToDWord(this.biWidth));
            this.fo.write(this.intToDWord(this.biHeight));
            this.fo.write(this.intToWord(this.biPlanes));
            this.fo.write(this.intToWord(this.biBitCount));
            this.fo.write(this.intToDWord(this.biCompression));
            this.fo.write(this.intToDWord(this.biSizeImage));
            this.fo.write(this.intToDWord(this.biXPelsPerMeter));
            this.fo.write(this.intToDWord(this.biYPelsPerMeter));
            this.fo.write(this.intToDWord(this.biClrUsed));
            this.fo.write(this.intToDWord(this.biClrImportant));
        } catch (final Exception wbih) {
            wbih.printStackTrace();
        }
    }

    /*
     * 
     * intToWord converts an int to a word, where the return value is
     * stored in a 2-byte array.
     *  
     */
    private byte[] intToWord(final int parValue) {
        final byte retValue[] = new byte[2];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x00FF);
        return (retValue);
    }

    /*
     * 
     * intToDWord converts an int to a double word, where the return
     * value is stored in a 4-byte array.
     *  
     */
    private byte[] intToDWord(final int parValue) {
        final byte retValue[] = new byte[4];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x000000FF);
        retValue[2] = (byte) ((parValue >> 16) & 0x000000FF);
        retValue[3] = (byte) ((parValue >> 24) & 0x000000FF);
        return (retValue);
    }
}