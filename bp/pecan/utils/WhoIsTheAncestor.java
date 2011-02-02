/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Nov 7, 2005
 */
package bp.pecan.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.logging.Logger;

import bp.common.ds.Array;
import bp.common.fp.Function;
import bp.common.fp.Generator;
import bp.common.io.FastaOutput_Procedure_Int;
import bp.common.io.FastaParser_Generator_Int;
import bp.common.io.InputMunger;
import bp.common.io.MultiFastaParser_Generator;
import bp.common.io.NewickTreeParser;
import bp.pecan.Ancestor;
import bp.pecan.Cell;
import bp.pecan.Pecan;
import bp.pecan.PecanTools;

/**
 * @author benedictpaten
 */
public class WhoIsTheAncestor {
    static final Logger logger = Logger.getLogger(Pecan.class
            .getName());

    public static final String TREE = "TREE";

    public static final String MFA = "MFA";

    public static final String AP = "AP";

    public static final String CP = "CP";

    public static final String GP = "GP";

    public static final String TP = "TP"; 

    public static final String GAPP = "GAPP";

    public static final String ALPHA = "ALPHA";

    public static final String GAMMA = "GAMMA";

    public static final String OUTPUT_FILE = "OUTPUT_FILE";

    public static final String HUMAN_READABLE_OUTPUT = "HUMAN_READABLE_OUTPUT";

    public double aP = 0.24;

    public double cP = 0.24;

    public double gP = 0.24;

    public double tP = 0.24;

    public double gapP = 0.04;

    public double alpha = 0.0;

    public double gamma = 1.25;//5/3f;//1.25;

    public String outputFile = "output.mfa";

    public boolean humanReadableOutput = false;

    public void setCommandLineArguments(final InputMunger inputMunger) {
        inputMunger
                .noInputString("For given tree, mfa and using the F84 + GAPS sub model, reconstitute an ancestor, output is in mfa format with sequence line for probabilities of positions scaled in range 0-100 (inclusive, ascii output)");
        inputMunger.addWatch(WhoIsTheAncestor.TREE, 1, "Newick tree");
        inputMunger.addWatch(WhoIsTheAncestor.MFA, 1,
                "Multi fasta file containing alignment");
        /*inputMunger.addWatch(AP, 1, "Prob of A, default : " + aP);
        inputMunger.addWatch(CP, 1, "Prob of C, default : " + cP);
        inputMunger.addWatch(GP, 1, "Prob of G, default : " + gP);
        inputMunger.addWatch(TP, 1, "Prob of T, default : " + tP);
        inputMunger.addWatch(GAPP, 1, "Prob of -, default : " + gapP);
        inputMunger.addWatch(ALPHA, 1, "Alpha prob, default : "
                + alpha);
        inputMunger.addWatch(GAMMA, 1, "Gamma prob, default : "
                + gamma);*/
        inputMunger.addWatch(WhoIsTheAncestor.OUTPUT_FILE, 1,
                "Output file, default : " + this.outputFile);
        inputMunger
                .addWatch(
                        WhoIsTheAncestor.HUMAN_READABLE_OUTPUT,
                        0,
                        "Make output human readable by scaling probs in 0-10(*, is inclusive 10) brackets (flip parameter), default : "
                                + this.humanReadableOutput);
    }
    
    public void parseArguments(final InputMunger inputMunger) {
        /*aP = inputMunger.parseValue(aP, AP);
        cP = inputMunger.parseValue(cP, CP);
        gP = inputMunger.parseValue(gP, GP);
        tP = inputMunger.parseValue(tP, TP);
        gapP = inputMunger.parseValue(gapP, GAPP);
        alpha = inputMunger.parseValue(alpha, ALPHA);
        gamma = inputMunger.parseValue(gamma, GAMMA);*/
        this.outputFile = inputMunger.parseValue(this.outputFile, WhoIsTheAncestor.OUTPUT_FILE);
        if (inputMunger.watchSet(WhoIsTheAncestor.HUMAN_READABLE_OUTPUT)) {
			this.humanReadableOutput = !this.humanReadableOutput;
		}
    }

    public Generator whoIsTheAncestor(final NewickTreeParser.Node tree,
            final Generator gen, final int seqNo) {
        final Ancestor.SubstitutionMatrixGenerator sMG = Ancestor
                .f84WithGaps(this.aP, this.cP, this.gP, this.tP, this.gapP, this.alpha, this.gamma);
        final int[] bA = Cell.translateAlphabetChar(new String[] { "Aa",
                "Cc", "Gg", "Tt", "Nn" }, Integer.MIN_VALUE);
        final Ancestor.FelsensteinsMostProbBases fMPB = new Ancestor.FelsensteinsMostProbBases(
                tree, sMG, 5);
        final Function ancestorFn = Ancestor
                .felsensteinsMostProbBases_DNAMSAColumn(fMPB, seqNo,
                        Array.matrix1DLookUp(bA));
        final Function ancestorRepeatFn = Ancestor.isAncestorRepeat(
                tree, seqNo);
        return new Generator() {
            int[] column;

            public Object gen() {
                if ((this.column = (int[]) gen.gen()) != null) {
                    final float[] fA = (float[]) ancestorFn.fn(this.column);
                    final float[] fA2 = (float[]) ancestorRepeatFn
                            .fn(this.column);
                    return new float[][] { fA, fA2 };
                }
                return null;
            }
        };
    }
    
    public void runWhoIsTheAncestor(final String treeFile, final String mfaFile)
            throws IOException {
        final NewickTreeParser.Node tree = new NewickTreeParser(
                NewickTreeParser.commentEater(NewickTreeParser
                        .tokenise(new StringReader(treeFile)))).tree;
        PecanTools.replaceEdgeLengths(tree, Double.MIN_VALUE, 1);
        final MultiFastaParser_Generator mfaGen = new MultiFastaParser_Generator(
                mfaFile);
        final Generator ancestorGen = this.whoIsTheAncestor(tree, mfaGen, mfaGen
                .getFastaIDs().length);
        final File[] tempOutFiles = new File[6];
        final FastaOutput_Procedure_Int[] fastaWriters = new FastaOutput_Procedure_Int[6];
        {
            final String[] sA = new String[] { "probA", "probC", "probG",
                    "probT", "probGap", "probRepeat" };
            for (int j = 0; j < sA.length; j++) {
                final File f = File.createTempFile("tmp_output_", ".fa");
                WhoIsTheAncestor.logger.info(" Temp output file for : " + sA[j]
                        + " is : " + f.toString());
                tempOutFiles[j] = f;
                fastaWriters[j] = new FastaOutput_Procedure_Int(
                        new BufferedOutputStream(
                                new FileOutputStream(f)), sA[j]);
            }
        }
        float[][] fAA;
        if (this.humanReadableOutput) {
            final int[] iA = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*' }; 
            while ((fAA = (float[][]) ancestorGen.gen()) != null) {
                float[] fA = fAA[0];
                float f = fA[0];
                for (int i = 1; i < fA.length; i++) {
					f += fA[i];
				}
                for (int i = 0; i < fA.length; i++) {
					fastaWriters[i].pro(iA[Math.round((fA[i]/f) * 10)]);
				}
                fA = fAA[1];
                fastaWriters[fastaWriters.length - 1].pro(iA[Math
                        .round((fA[1]/(fA[0]+fA[1])) * 10)]);
            }
        } else {
            while ((fAA = (float[][]) ancestorGen.gen()) != null) {
                float[] fA = fAA[0];
                float f = fA[0];
                for (int i = 1; i < fA.length; i++) {
					f += fA[i];
				}
                for (int i = 0; i < fA.length; i++) {
					fastaWriters[i].pro(Math.round((fA[i]/f) * 100));
				}
                fA = fAA[1];
                fastaWriters[fastaWriters.length - 1].pro(Math
                        .round((fA[1]/(fA[0]+fA[1])) * 100));
            }
        }
        final OutputStream oS = new BufferedOutputStream(
                new FileOutputStream(this.outputFile));
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

    public static void main(final String[] args) throws IOException {
        final WhoIsTheAncestor wITA = new WhoIsTheAncestor();
        final InputMunger inputMunger = new InputMunger();
        inputMunger.addStandardWatches();
        wITA.setCommandLineArguments(inputMunger);
        if (!inputMunger.parseInput(args)) {
			System.exit(0);
		}
        wITA.parseArguments(inputMunger);
        wITA.runWhoIsTheAncestor(inputMunger.watchStrings(PrePecan.TREE)[0],
                inputMunger.watchStrings(WhoIsTheAncestor.MFA)[0]);
    }
}