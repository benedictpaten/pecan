/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Nov 29, 2005
 */
package bp.pecan.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import bp.common.ds.Array;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorTools;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.io.Debug;
import bp.common.io.FastaOutput_Procedure_Int;
import bp.common.io.FastaParser_Generator_Int;
import bp.common.io.InputMunger;
import bp.common.io.NewickTreeParser;
import bp.pecan.Chains;
import bp.pecan.PecanTools;
import bp.pecan.Chains.LocalAligner;
import bp.pecan.Chains.PrimeConstraints;

public class PrePecan {

    static final Logger logger = Logger.getLogger(PrePecan.class
            .getName());

    public static final String TREE = "TREE";

    public static final String SEQUENCES = "SEQUENCES";

    public static final String OUTPUT_FILE = "OUTPUT_FILE";

    public static final String EXONERATE_WORD_LENGTHS = "EXONERATE_WORD_LENGTHS";

    public static final String EXONERATE_MIN_SCORES = "EXONERATE_MIN_SCORES";

    public static final String EXONERATE_SOFT_MASK_SEQUENCES = "EXONERATE_SOFT_MASK_SEQUENCES";

    public static final String EXONERATE_SATURATE_THRESHOLD = "EXONERATE_SATURATE_THRESHOLD";

    public static final String EXONERATE_GAPPED_EXTENSION = "EXONERATE_GAPPED_EXTENSION";

    public static final String EXONERATE_BASIC_COMMAND = "EXONERATE_BASIC_COMMAND";

    public static final String EXONERATE_MAX_DISTANCES = "EXONERATE_MAX_DISTANCE";

    public static final String EXONERATE_MODELS = "EXONERATE_MODELS";

    public static final String EXONERATE_MIN_DISTANCE = "EXONERATE_MIN_DISTANCE";

    public static final String EXONERATE_STRING = "EXONERATE_STRING";

    public static final String CONSISTENCY_TRANSFORM = "CONSISTENCY_TRANSFORM";

    public static final String EDGE_TRIM = "EDGE_TRIM";

    public static final String RESCORE_ALIGNMENTS = "RESCORE_ALIGNMENTS";

    public static final String RELATIVE_ENTROPY_THRESHOLD = "RELATIVE_ENTROPY_THRESHOLD";
    
    public static final String CODING_2_CODING = "CODING_2_CODING";

    // global variables
    public String[] fastaIDs;

    public int[] seqSizes;

    public byte[][] cachedSeqFiles;

    // end global variables

    public String outputFile = "output.mfa";

    public int[] exonerateWordLengths = new int[] { 5, 8, 11 };

    //
    public int[] exonerateMinScores = new int[] { 100, 150, 200 };

    public boolean[] softMask = new boolean[] { false, true, true };

    public int saturateThreshold = 8;

    public boolean[] gappedExtension = new boolean[] { false, false,
            false };

    // word lengths = ln(n / 10)/ln(4)
    public int[] maxDistances = new int[] { 20000, 664000,
            Integer.MAX_VALUE };

    public int minDistance = 200;

    public int edgeTrim = 3;

    public String[] exonerateModels = new String[] { "affine:local",
            "affine:local", "affine:local" };

    public String[] exonerateBasicCommand = new String[] {
            "--showcigar", "true", "--showvulgar", "false",
            "--showalignment", "false", "--querytype", "dna",
            "--targettype", "dna" };

    public String exonerateString = "exonerate";

    public boolean[] consistencyTransform = new boolean[] { false, true, true };

    public boolean rescoreAlignments = false;

    public float relativeEntropyThreshold = 0.65f;
    
    public boolean coding2Coding = false;
    
    public int coding2CodingMinScore = 250;
    
    public int exoneratePartitionAlignerWordLength = 6;

    public int exoneratePartitionAlignerMinScore = 0;

    public boolean exoneratePartitionAlignerGappedExtension = true;

    public String exoneratePartitionAlignerModel = "affine:local";

    public boolean exoneratePartitionAlignerSoftMaskSequences = false;

    public InputMunger setCommandLineArguments(final InputMunger inputMunger) {
        inputMunger
                .noInputString("PrePecan : \nFor the given sequences and tree, create an alignment using the pre-Pecan chaining routines and Exonerate as pair alignment generator");
        inputMunger
                .addWatch(
                        PrePecan.TREE,
                        1,
                        "Newick tree for sequences, unspecified distances are given the value 1.0");
        inputMunger.addWatch_VariableTermsLength(PrePecan.SEQUENCES,
                "Sequence files in fasta format");
        inputMunger.addWatch(PrePecan.OUTPUT_FILE, 1,
                "File in which to write multi-fasta formatted output, default : "
                        + this.outputFile);
        inputMunger
                .addWatch_VariableTermsLength(
                        PrePecan.EXONERATE_WORD_LENGTHS,
                        "Word length of Exonerate hits for recursive divide and conquer with more leniant parameters, default : "
                                + IterationTools.join(
                                        this.exonerateWordLengths, " "));
        inputMunger.addWatch_VariableTermsLength(
                PrePecan.EXONERATE_BASIC_COMMAND,
                "Basic command upon which exonerate is run, default : "
                        + IterationTools.join(this.exonerateBasicCommand,
                                " "));
        inputMunger.addWatch(PrePecan.EXONERATE_STRING, 1,
                "Path to exonerate, default : " + this.exonerateString);

        inputMunger.addWatch_VariableTermsLength(PrePecan.CONSISTENCY_TRANSFORM,
                "Consistency transform the chains between sequences, default : "
                        + IterationTools.join(this.consistencyTransform, " "));
        inputMunger.addWatch(PrePecan.EDGE_TRIM, 1,
                "Amount of edge to trim from diagonals, default : "
                        + this.edgeTrim);
        inputMunger.addWatch(PrePecan.RESCORE_ALIGNMENTS, 0,
                "Rescore alignments, default (flip): "
                        + this.rescoreAlignments);

        inputMunger
                .addWatch_VariableTermsLength(
                        PrePecan.EXONERATE_MIN_SCORES,
                        "Exonerate min scores for recursive divide and conquer with more leniant parameters, default : "
                                + IterationTools.join(
                                        this.exonerateMinScores, " "));

        inputMunger
                .addWatch_VariableTermsLength(
                        PrePecan.EXONERATE_SOFT_MASK_SEQUENCES,
                        "Tell Exonerate sequences are softmasked for recursive divide and conquer with more leniant parameters, default : "
                                + IterationTools.join(this.softMask, " "));

        inputMunger.addWatch(PrePecan.EXONERATE_SATURATE_THRESHOLD, 1,
                "Exonerate saturate threshold, default : "
                        + this.saturateThreshold);

        inputMunger
                .addWatch_VariableTermsLength(
                        PrePecan.EXONERATE_GAPPED_EXTENSION,
                        "Use Exonerate gapped extension mode for recursive divide and conquer with more leniant parameters, default : "
                                + IterationTools.join(
                                        this.gappedExtension, " "));

        inputMunger
                .addWatch_VariableTermsLength(
                        PrePecan.EXONERATE_MAX_DISTANCES,
                        "Max distances for recursive divide and conquer with more leniant parameters, default : "
                                + IterationTools.join(this.maxDistances,
                                        " "));

        inputMunger
                .addWatch_VariableTermsLength(
                        PrePecan.EXONERATE_MODELS,
                        "Exonerate models for recursive divide and conquer with more leniant parameters, default : "
                                + IterationTools.join(
                                        this.exonerateModels, " "));

        inputMunger.addWatch(PrePecan.EXONERATE_MIN_DISTANCE, 1,
                "Min distance for exonerate, default : "
                        + this.minDistance);

        inputMunger
                .addWatch(
                        PrePecan.RELATIVE_ENTROPY_THRESHOLD,
                        1,
                        "Relative entropy threshold below which alignments are discarded, default : "
                                + this.relativeEntropyThreshold);
        inputMunger.
            addWatch(PrePecan.CODING_2_CODING, 0,
                    "Perform pre-alignment with coding2coding model, default : " + this.coding2Coding);

        return inputMunger;
    }

    public void parseArguments(final InputMunger inputMunger) {
        if (inputMunger.watchSet(PrePecan.EXONERATE_WORD_LENGTHS)) {
            this.exonerateWordLengths = Array.convertToInts(inputMunger
                    .watchStrings(PrePecan.EXONERATE_WORD_LENGTHS));
        }

        if (inputMunger.watchSet(PrePecan.EXONERATE_BASIC_COMMAND)) {
			this.exonerateBasicCommand = inputMunger
                    .watchStrings(PrePecan.EXONERATE_BASIC_COMMAND);
		}

        this.exonerateString = inputMunger.parseValue(this.exonerateString,
                PrePecan.EXONERATE_STRING);

        if (inputMunger.watchSet(PrePecan.CONSISTENCY_TRANSFORM)) {
            this.consistencyTransform = Array.convertToBooleans(inputMunger
                    .watchStrings(PrePecan.CONSISTENCY_TRANSFORM));
        }
        
        
        this.edgeTrim = inputMunger.parseValue(this.edgeTrim, PrePecan.EDGE_TRIM);

        this.outputFile = inputMunger.parseValue(this.outputFile, PrePecan.OUTPUT_FILE);

        if (inputMunger.watchSet(PrePecan.RESCORE_ALIGNMENTS)) {
			this.rescoreAlignments = !this.rescoreAlignments;
		}

        if (inputMunger.watchSet(PrePecan.EXONERATE_MIN_SCORES)) {
            this.exonerateMinScores = Array.convertToInts(inputMunger
                    .watchStrings(PrePecan.EXONERATE_MIN_SCORES));
        }

        if (inputMunger.watchSet(PrePecan.EXONERATE_SOFT_MASK_SEQUENCES)) {
            this.softMask = Array.convertToBooleans(inputMunger
                    .watchStrings(PrePecan.EXONERATE_SOFT_MASK_SEQUENCES));
        }

        this.saturateThreshold = inputMunger.parseValue(this.saturateThreshold,
                PrePecan.EXONERATE_SATURATE_THRESHOLD);

        if (inputMunger.watchSet(PrePecan.EXONERATE_GAPPED_EXTENSION)) {
            this.gappedExtension = Array.convertToBooleans(inputMunger
                    .watchStrings(PrePecan.EXONERATE_GAPPED_EXTENSION));
        }

        if (inputMunger.watchSet(PrePecan.EXONERATE_MAX_DISTANCES)) {
            this.maxDistances = Array.convertToInts(inputMunger
                    .watchStrings(PrePecan.EXONERATE_MAX_DISTANCES));
        }

        if (inputMunger.watchSet(PrePecan.EXONERATE_MODELS)) {
            this.exonerateModels = inputMunger
                    .watchStrings(PrePecan.EXONERATE_MODELS);
        }

        this.minDistance = inputMunger.parseValue(this.minDistance,
                PrePecan.EXONERATE_MIN_DISTANCE);

        this.relativeEntropyThreshold = (float) inputMunger.parseValue(
                this.relativeEntropyThreshold, PrePecan.RELATIVE_ENTROPY_THRESHOLD);
        
        if(inputMunger.watchSet(PrePecan.CODING_2_CODING)) {
			this.coding2Coding = !this.coding2Coding;
		}
    }
    
    public List getSeqSizes(final String[] seqFiles) throws IOException {
    	final int sequenceNumber = seqFiles.length;
        String[] fastaIDs = new String[sequenceNumber];
        int[] seqSizes = new int[sequenceNumber];
        byte[][] cachedSeqFiles = new byte[sequenceNumber][];
        {
            int j = 0;
            for (int i = 0; i < sequenceNumber; i++) {
                final InputStream iS = new BufferedInputStream(
                        new FileInputStream(seqFiles[i]));
                final FastaParser_Generator_Int gen = new FastaParser_Generator_Int(
                        iS, Integer.MAX_VALUE);
                fastaIDs[i] = gen.getFastaID();
                int m = 0;
                final byte[] bA = new byte[(int) new File(seqFiles[i])
                        .length()];
                for (int k; (k = gen.gen()) != Integer.MAX_VALUE;) {
					bA[m++] = (byte) k;
				}
                cachedSeqFiles[i] = bA;
                seqSizes[i] = m;
                PrePecan.logger.info(" Sequence " + fastaIDs[i]
                        + " is of length " + seqSizes[i] + " " + j);
                j += m;
                iS.close();
            }
        }
        List l = new LinkedList();
        l.add(fastaIDs);
        l.add(seqSizes);
        l.add(cachedSeqFiles);
        return l;
    }

    public PrimeConstraints runPrePecan(final NewickTreeParser.Node tree,
            final String[] seqFiles) throws IOException {
        final long startTime = System.currentTimeMillis();

        PecanTools.replaceEdgeLengths(tree, Double.MIN_VALUE, 1);
        PrePecan.logger.info("Parsed tree : " + tree);

        final int sequenceNumber = seqFiles.length;
        List l = getSeqSizes(seqFiles);
        this.fastaIDs = (String[])l.get(0);
        this.seqSizes = (int[])l.get(1);
        this.cachedSeqFiles = (byte[][])l.get(2);
        int alignerStartingIndex = 0;
        {
            final int[] iA = this.seqSizes.clone();
            Arrays.sort(iA);
            final int i = iA[sequenceNumber - 1] + iA[sequenceNumber - 2];
            for (; alignerStartingIndex < this.maxDistances.length; alignerStartingIndex++) {
				if (i < this.maxDistances[alignerStartingIndex]) {
					break;
				}
			}
        }

        final double[][] seqDistances = PecanTools.getDistances(tree,
                sequenceNumber, Functions_2Args.sum());
        for (int i = 0; i < seqDistances.length; i++) {
            PrePecan.logger.info("Sequence distances for " + i + " "
                    + IterationTools.join(seqDistances[i], " "));
        }
        final int[][] pairOrdering = PecanTools
                .getPairOrdering(seqDistances);
        for (final int[] element : pairOrdering) {
            PrePecan.logger.info("Pair ordering (ascending distance) : "
                    + IterationTools.join(element, " "));
        }

        // make exonerate aligners
        Chains.Aligner[] aligners = new Chains.Aligner[alignerStartingIndex + 1];
        for (int i = 0; i < aligners.length; i++) {
            LocalAligner lA = PrePecan.makeExonerateAligner(
                    this.exonerateBasicCommand, this.exonerateString,
                    this.exonerateWordLengths[i], this.exonerateMinScores[i],
                    this.gappedExtension[i], this.exonerateModels[i],
                    this.saturateThreshold, this.softMask[i], this.softMask[i]);
            if (this.rescoreAlignments) {
				lA = Chains.rescoreAlignments(lA,
                        this.relativeEntropyThreshold);
			}
            aligners[i] = Chains.alignerConvertor(lA);
        }
        if(this.coding2Coding) {
            LocalAligner lA = PrePecan.makeExonerateAligner(
                    this.exonerateBasicCommand, this.exonerateString,
                    11, this.coding2CodingMinScore,
                    false, "coding2coding",
                    this.saturateThreshold, true, true);
            if (this.rescoreAlignments) {
				lA = Chains.rescoreAlignments(lA,
                        this.relativeEntropyThreshold);
			}
            final Chains.Aligner[] cA = new Chains.Aligner[aligners.length + 1];
            cA[aligners.length] = Chains.alignerConvertor(lA);
            System.arraycopy(aligners, 0, cA, 0, aligners.length);
            aligners = cA;
            alignerStartingIndex++;
            this.maxDistances = Array.concatenate(this.maxDistances, new int[] { Integer.MAX_VALUE });
            this.consistencyTransform = Array.concatenate(this.consistencyTransform, new boolean[] { true });
            PrePecan.logger.info(aligners.length + " " + alignerStartingIndex);
        }

        final Chains.PrimeConstraints pC = new Chains.PrimeConstraints(
                sequenceNumber);

        Chains.makeConsistentChains(pC, pairOrdering, sequenceNumber,
                aligners, alignerStartingIndex, this.minDistance,
                this.maxDistances, this.cachedSeqFiles, this.seqSizes,
                this.consistencyTransform, this.edgeTrim);

        //if (Debug.DEBUGCODE)
        PrePecan.logger.info("Total time taken for Pre_Pecan alignment "
                    + (System.currentTimeMillis() - startTime)
                    / 1000.0);

        return pC;
    }
    
    public static String makeStarTreeP(int sequenceNumber) {
    	assert(sequenceNumber > 0);
    	if(sequenceNumber >= 2) {
    		return "(" + makeStarTreeP(sequenceNumber/2) + ":1.0," +  makeStarTreeP(sequenceNumber/2 + sequenceNumber%2) + ":1.0)";
    	}
    	else {
    		return "A";
    	}
    }
    
    public static String makeStarTree(int sequenceNumber) {
    	return makeStarTreeP(sequenceNumber) + ";";
    }

    public static LocalAligner makeExonerateAligner(
            final String[] basicCommand, final String exonerateString,
            final int exonerateWordLength, final int exonerateMinScore,
            final boolean gappedExtension, final String model,
            final int saturateThreshold, final boolean softMaskTarget,
            final boolean softMaskQuery) {
        final String[] sA = basicCommand;
        final String[] sA2 = new String[sA.length + 15];
        sA2[0] = exonerateString;
        System.arraycopy(sA, 0, sA2, 1, sA.length);
        sA2[sA.length + 1] = "--dnawordlen";
        sA2[sA.length + 2] = exonerateWordLength + "";
        sA2[sA.length + 3] = "--score";
        sA2[sA.length + 4] = exonerateMinScore + "";
        sA2[sA.length + 5] = "--gappedextension";
        sA2[sA.length + 6] = gappedExtension + "";
        sA2[sA.length + 7] = "--model";
        sA2[sA.length + 8] = model;
        sA2[sA.length + 9] = "--saturatethreshold";
        sA2[sA.length + 10] = saturateThreshold + "";
        sA2[sA.length + 11] = "--softmasktarget";
        sA2[sA.length + 12] = softMaskTarget + "";
        sA2[sA.length + 13] = "--softmaskquery";
        sA2[sA.length + 14] = softMaskQuery + "";
        PrePecan.logger.info("Creating exonerate aligner : "
                + IterationTools.join(sA2, " "));
        return Chains.makeExonerateAlignment(sA2);
    }

    public static void main(final String[] args) throws IOException {
        final PrePecan prePecan = new PrePecan();
        final InputMunger inputMunger = new InputMunger();
        inputMunger.addStandardWatches();
        prePecan.setCommandLineArguments(inputMunger);
        if (!inputMunger.parseInput(args)) {
			System.exit(0);
		}
        inputMunger.processStandardWatches();
        prePecan.parseArguments(inputMunger);
        final String[] seqFiles = inputMunger.watchStrings(PrePecan.SEQUENCES);
        final int sequenceNumber = seqFiles.length;
        final String treeFile;
        if (inputMunger.watchSet(PrePecan.TREE)) {
        	treeFile = inputMunger.watchStrings(PrePecan.TREE)[0];
        }
        else {
        	treeFile = prePecan.makeStarTree(sequenceNumber);
        }
        final NewickTreeParser.Node tree = new NewickTreeParser(
                NewickTreeParser.commentEater(NewickTreeParser
                        .tokenise(new StringReader(treeFile)))).tree;
        
        
        
        final PrimeConstraints pC = prePecan.runPrePecan(tree, seqFiles);
        final Generator[][] chains = new Generator[sequenceNumber][sequenceNumber];
        for (int i = 0; i < chains.length; i++) {
			for (int j = i + 1; j < chains.length; j++) {
                final List l = (List) GeneratorTools.append(
                        pC.convertPrimeConstraintsToEdgeList(i, j,
                                true), new LinkedList());
                chains[i][j] = Generators.iteratorGenerator(l
                        .iterator());
                PrePecan.logger.info("Number of edges for i : " + i + ", j : "
                        + j + " : " + l.size());
                chains[j][i] = pC.convertPrimeConstraintsToEdgeList(
                        j, i, true);
                // chains[i][j] =
                // Generators.iteratorGenerator(chainsL[i][j].iterator());
                // chains[j][i] =
                // PolygonFiller.flipEdgeXYDiagonalsCoordinates(Generators.iteratorGenerator(chainsL[i][j].iterator()));
            }
		}
        final File[] tempOutFiles = new File[sequenceNumber];
        final FastaOutput_Procedure_Int[] fastaWriters = new FastaOutput_Procedure_Int[sequenceNumber];
        for (int i = 0; i < prePecan.fastaIDs.length; i++) {

            final File f = File.createTempFile("tmp_output_", ".fa");
            PrePecan.logger.info(" Temp output file for : "
                    + prePecan.fastaIDs[i] + " is : " + f.toString());
            tempOutFiles[i] = f;
            fastaWriters[i] = new FastaOutput_Procedure_Int(
                    new BufferedOutputStream(new FileOutputStream(f)),
                    prePecan.fastaIDs[i]);
        }

        Chains.makeMultipleAlignment(fastaWriters, chains,
                prePecan.cachedSeqFiles, prePecan.seqSizes,
                sequenceNumber, new int[100000], (byte) '-');

        final OutputStream oS = new BufferedOutputStream(
                new FileOutputStream(prePecan.outputFile));
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
