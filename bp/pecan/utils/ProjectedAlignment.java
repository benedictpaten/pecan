/*
 * Created on Nov 9, 2005
 */
package bp.pecan.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import bp.common.io.FastaOutput_Procedure_Int;
import bp.common.io.FastaParser_Generator_Int;
import bp.common.io.InputMunger;
import bp.common.io.MultiFastaParser_Generator;
import bp.pecan.Pecan;

/**
 * @author benedictpaten
 */
public class ProjectedAlignment {

    static final Logger logger = Logger.getLogger(Pecan.class
            .getName());

    public static final String TREE = "TREE";

    public static final String MFA = "MFA";

    public static final String PROJECTED_SEQUENCES = "PROJECTED_SEQUENCES";

    public static void main(final String[] args) throws IOException {
        final InputMunger inputMunger = new InputMunger();
        inputMunger
                .noInputString("For the given alignment, create an alignment containing only columns which contain one of the given sequences");
        inputMunger.addStandardWatches();
        inputMunger.addWatch(ProjectedAlignment.MFA, 1,
                "Multi fasta file containing alignment");
        inputMunger.addWatch(ProjectedAlignment.PROJECTED_SEQUENCES, 1, "Row numbers of projected sequences ");
        if (!inputMunger.parseInput(args)) {
			System.exit(0);
		}
        inputMunger.processStandardWatches();
        final String mfaFile = inputMunger.watchStrings(ProjectedAlignment.MFA)[0];
        final String[] projectedSequencesS = inputMunger.watchStrings(ProjectedAlignment.PROJECTED_SEQUENCES);
        final int[] projectedSequences = new int[projectedSequencesS.length];
        for(int i=0; i<projectedSequences.length; i++) {
			projectedSequences[i] = Integer.parseInt(projectedSequencesS[i]);
		}
        final MultiFastaParser_Generator gen = new MultiFastaParser_Generator(
                mfaFile);
        final String[] fastaIDs = gen.getFastaIDs();
        final File[] tempOutFiles = new File[fastaIDs.length];
        final FastaOutput_Procedure_Int[] fastaWriters = new FastaOutput_Procedure_Int[fastaIDs.length];
        {
            for (int j = 0; j < fastaIDs.length; j++) {
                final File f = File.createTempFile("tmp_output_", ".fa");
                ProjectedAlignment.logger.info(" Temp output file for : " + fastaIDs[j]
                        + " is : " + f.toString());
                tempOutFiles[j] = f;
                fastaWriters[j] = new FastaOutput_Procedure_Int(
                        new BufferedOutputStream(
                                new FileOutputStream(f)), fastaIDs[j]);
            }
        }
        int[] column;
        while ((column = (int[]) gen.gen()) != null) {
            for (final int element : projectedSequences) {
				if(column[element] != '-') {
                    for (int j = 0; j < fastaWriters.length; j++) {
						fastaWriters[j].pro(column[j]);
					}
                    break;
                }
			}
        }
        final OutputStream oS = new BufferedOutputStream(
                new FileOutputStream("output.mfa"));
        for (int i = 0; i < fastaWriters.length; i++) {
            fastaWriters[i].endAndClose();
            final FastaParser_Generator_Int outputFastaParser = new FastaParser_Generator_Int(
                    new BufferedInputStream(new FileInputStream(
                            tempOutFiles[i])), Integer.MAX_VALUE);
            final FastaOutput_Procedure_Int fPW = new FastaOutput_Procedure_Int(
                    oS, outputFastaParser.getFastaID());
            int j;
            while ((j = outputFastaParser.gen()) != Integer.MAX_VALUE) {
				fPW.pro(j);
			}
            fPW.end();
            tempOutFiles[i].delete();
        }
    }
}
