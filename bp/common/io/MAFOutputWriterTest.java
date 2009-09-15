/*
 * Created on Feb 20, 2006
 */
package bp.common.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MAFOutputWriterTest
                                extends TestCase {
    
    public void testMAFOutputWriterTest() throws IOException {
        final String maf = "##maf version=1 scoring=tba.v8\n"
                + "# tba.v8 (((human chimp) baboon) (mouse rat))\n"
                + "# multiz.v7\n"
                + "# maf_project.v5 _tba_right.maf3 mouse _tba_C\n"
                + "# single_cov2.v4 single_cov2 /dev/stdin\n"
                + "\n"
                + "a score=23262.0\n"
                + "s hg16.chr7 27578828 38 + 158545518 AAA-GGGAATGTTAACCAAATGA---ATTGTCTCTTACGGTG\n"
                + "s panTro1.chr6 28741140 38 + 161576975 AAA-GGGAATGTTAACCAAATGA---ATTGTCTCTTACGGTG\n"
                + "s baboon 116834 38 + 4622798 AAA-GGGAATGTTAACCAAATGA---GTTGTCTCTTATGGTG\n"
                + "s mm4.chr6 53215344 38 + 151104725 -AATGGGAATGTTAAGCAAACGA---ATTGTCTCTCAGTGTG\n"
                + "s rn3.chr4 81344243 40 + 187371129 -AA-GGGGATGCTAAGCCAATGAGTTGTTGTCTCTCAATGTG\n"
                + "\n"
                + "a score=5062.0\n"
                + "s hg16.chr7 27699739 6 + 158545518 TAAAGA\n"
                + "s panTro1.chr6 28862317 6 + 161576975 TAAAGA\n"
                + "s baboon 241163 6 + 4622798 TAAAGA\n"
                + "s mm4.chr6 53303881 6 + 151104725 TAAAGA\n"
                + "s rn3.chr4 81444246 6 + 187371129 taagga\n"
                + "\n"
                + "a score=6636.0\n"
                + "s hg16.chr7 27707221 13 + 158545518 gcagctgaaaaca\n"
                + "s panTro1.chr6 28869787 13 + 161576975 gcagctgaaaaca\n"
                + "s baboon 249182 13 + 4622798 gcagctgaaaaca\n"
                + "s mm4.chr6 53310102 13 + 151104725 ACAGCTGAAAATA\n";

        final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        final MAFOutputWriter mAFOutputWriter = new MAFOutputWriter(bAOS,
                "1", "tba.v8", null);
        mAFOutputWriter
                .writeInfoLine("tba.v8 (((human chimp) baboon) (mouse rat))");
        mAFOutputWriter.writeInfoLine("multiz.v7");
        mAFOutputWriter
                .writeInfoLine("maf_project.v5 _tba_right.maf3 mouse _tba_C");
        mAFOutputWriter
                .writeInfoLine("single_cov2.v4 single_cov2 /dev/stdin");
        File f = File.createTempFile("temp", ".fa");
        f.deleteOnExit();
        OutputStream oS = new BufferedOutputStream(
                new FileOutputStream(f));
        FastaOutput_Procedure_Int.writeFile(oS, "",
                "AAA-GGGAATGTTAACCAAATGA---ATTGTCTCTTACGGTG"
                        .getBytes(), 0, 42);
        FastaOutput_Procedure_Int.writeFile(oS, "",
                "AAA-GGGAATGTTAACCAAATGA---ATTGTCTCTTACGGTG"
                        .getBytes(), 0, 42);
        FastaOutput_Procedure_Int.writeFile(oS, "",
                "AAA-GGGAATGTTAACCAAATGA---GTTGTCTCTTATGGTG"
                        .getBytes(), 0, 42);
        FastaOutput_Procedure_Int.writeFile(oS, "",
                "-AATGGGAATGTTAAGCAAACGA---ATTGTCTCTCAGTGTG"
                        .getBytes(), 0, 42);
        FastaOutput_Procedure_Int.writeFile(oS, "",
                "-AA-GGGGATGCTAAGCCAATGAGTTGTTGTCTCTCAATGTG"
                        .getBytes(), 0, 42);
        oS.close();
        mAFOutputWriter.writeBlock(23262.0f, 0,
                new MultiFastaParser_Generator(f.toString()),
                new String[] { "hg16.chr7", "panTro1.chr6", "baboon",
                        "mm4.chr6", "rn3.chr4" }, new int[] {
                        27578828, 28741140, 116834, 53215344,
                        81344243 }, new int[] { 38, 38, 38, 38, 40 },
                new boolean[] { true, true, true, true, true },
                new int[] { 158545518, 161576975, 4622798, 151104725,
                        187371129 });
        f = File.createTempFile("temp", ".fa");
        f.deleteOnExit();
        oS = new BufferedOutputStream(new FileOutputStream(f));
        FastaOutput_Procedure_Int.writeFile(oS, "", "TAAAGA"
                .getBytes(), 0, 6);
        FastaOutput_Procedure_Int.writeFile(oS, "", "TAAAGA"
                .getBytes(), 0, 6);
        FastaOutput_Procedure_Int.writeFile(oS, "", "TAAAGA"
                .getBytes(), 0, 6);
        FastaOutput_Procedure_Int.writeFile(oS, "", "TAAAGA"
                .getBytes(), 0, 6);
        FastaOutput_Procedure_Int.writeFile(oS, "", "taagga"
                .getBytes(), 0, 6);
        oS.close();
        mAFOutputWriter.writeBlock(5062.0f, 0,
                new MultiFastaParser_Generator(f.toString()),
                new String[] { "hg16.chr7", "panTro1.chr6", "baboon",
                        "mm4.chr6", "rn3.chr4" }, new int[] {
                        27699739, 28862317, 241163, 53303881,
                        81444246 }, new int[] { 6, 6, 6, 6, 6 },
                new boolean[] { true, true, true, true, true },
                new int[] { 158545518, 161576975, 4622798, 151104725,
                        187371129 });
        f = File.createTempFile("temp", ".fa");
        f.deleteOnExit();
        oS = new BufferedOutputStream(new FileOutputStream(f));
        FastaOutput_Procedure_Int.writeFile(oS, "", "gcagctgaaaaca"
                .getBytes(), 0, 13);
        FastaOutput_Procedure_Int.writeFile(oS, "", "gcagctgaaaaca"
                .getBytes(), 0, 13);
        FastaOutput_Procedure_Int.writeFile(oS, "", "gcagctgaaaaca"
                .getBytes(), 0, 13);
        FastaOutput_Procedure_Int.writeFile(oS, "", "ACAGCTGAAAATA"
                .getBytes(), 0, 13);
        oS.close();
        mAFOutputWriter.writeBlock(6636.0f, 0,
                new MultiFastaParser_Generator(f.toString()),
                new String[] { "hg16.chr7", "panTro1.chr6", "baboon",
                        "mm4.chr6" }, new int[] { 27707221, 28869787,
                        249182, 53310102 }, new int[] { 13, 13, 13,
                        13 },
                new boolean[] { true, true, true, true }, new int[] {
                        158545518, 161576975, 4622798, 151104725 });
        mAFOutputWriter.endAndClose();
        final String s = bAOS.toString();
        Assert.assertEquals(s, maf);
    }
}
