/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Dec 14, 2005
 */
package bp.trawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import bp.common.ds.SuffixTree;
import bp.common.fp.Function_Int;
import bp.common.fp.IterationTools;
import bp.common.io.Debug;
import bp.common.io.InputMunger;

public class Trawler {
    static final Logger logger = Logger.getLogger(Trawler.class
            .getName());

    public static final String CONSERVED_FILE = "CONSERVED_FILE";

    public static final String BACKGROUND_FILE = "BACKGROUND_FILE";

    public static final String ALPHABET = "ALPHABET";

    public static final String MAXIMUM_MOTIF_SIZE = "MAXIMUM_MOTIF_SIZE";

    public static final String MAXIMUM_MISMATCHES = "MAXIMUM_MISMATCHES";

    public static final String MINIMUM_OCCURRENCES = "MINIMUM_OCCURRENCES";

    public static final String MINIMUM_ZSCORE = "MINIMUM_ZSCORE";

    public static final String MAXIMUM_ZSCORE = "MAXIMUM_ZSCORE";

    public static final String MAXIMUM_NUMBER_OF_MOTIFS_TO_REPORT = "MAXIMUM_NUMBER_OF_MOTIFS_TO_REPORT";

    public static final String BYTE_MULTIPLE = "BYTE_MULTIPLE";

    public static final String OUTPUT_FILE = "OUTPUT_FILE";

    public static final String ESTIMATE_MAXIMUM_ZSCORE = "ESTIMATE_MAXIMUM_ZSCORE";

    public static final String ESTIMATE_MINIMUM_ZSCORE = "ESTIMATE_MINIMUM_ZSCORE";

    public static final String USE_SHUFFLED_BACKGROUND_SET = "USE_SHUFFLED_BACKGROUND_SET";

    public static final String RUN_SCAN_FUNCTION = "RUN_SCAN_FUNCTION";

    public static final String FRAGMENT_SHUFFLE_SIZE = "FRAGMENT_SHUFFLE_SIZE";

    public static final String NUMBER_OF_TAIL_VALUES_TO_AVERAGE_FROM = "NUMBER_OF_TAIL_VALUES_TO_AVERAGE_FROM";

    public static final String NUMBER_OF_ZSCORE_ESTIMATION_ITERATIONS = "NUMBER_OF_ZSCORE_ESTIMATION_ITERATIONS";
    
    public static final String SEARCH_REVERSE_COMPLEMENT = "SEARCH_REVERSE_COMPLEMENT";

    public static final byte TERMINALCHAR = '$';

    public String workingAlphabet = "ACGTN";

    public int maximumMotifSize = 20;

    public int maximumMismatches = 2;

    public int minimumOccurrences = 20;

    public float minimumZScore = 10f;

    public float maximumZScore = Float.POSITIVE_INFINITY;

    public int maximumNumberOfMotifsToReport = 100000;

    public int byteMultiple = 14;

    public boolean estimateMinimumZScore = true;

    public boolean estimateMaximumZScore = false;

    public boolean runScanFunction = true;

    public boolean useShuffledBackgroundSet = true;
    
    public boolean searchReverseComplement = false;

    public int fragmentShuffleSize = 20;

    public int numberOfTailValuesToAverageFrom = 5;

    public int numberOfZScoreEstimationIterations = 1;

    public String outputFile = "output.motifs";

    public Alphabet alphabet = new Alphabet();

    public InputMunger setCommandLineArguments(final String[] args)
            throws IOException {
        final InputMunger inputMunger = new InputMunger();
        inputMunger
                .noInputString("Trawler, [options and parameters]");
        inputMunger
                .helpString("Trawler :\n"
                        + "This is a suffix tree based motif finder. \n"
                        + "The output is of the following format.. \n"
                        + "[CONSERVED COUNT] [BACKGROUND COUNT] [Z SCORE] [MOTIF]\n"
                        + "Trawler only looks at upper case IUPAC characters.\n"
                        + "Soft masked characters are ignored as are any other characters.\n"
                        + "The score is calculated assuming an approximately normal distribution with\n"
                        + "expected values calculated from sequences and background count\n"
                        + "Program written by Benedict Paten. Mail to bjp (AT) ebi.ac.uk");
        inputMunger.addStandardWatches();
        inputMunger.addWatch(Trawler.CONSERVED_FILE, 1,
                "The fasta file containing conserved sequences");
        inputMunger.addWatch(Trawler.BACKGROUND_FILE, 1,
                "The fasta file containing background sequences");
        inputMunger
                .addWatch(
                        Trawler.ALPHABET,
                        1,
                        "Alphabet to use, give any unspaced array of non-redundant, \n2-redundant and the wild-card IUPAC characters in the following order ACGTMRWSYKN"
                                + ", \n(REMINDER : A C G T [AC]:M [AG]:R [AT]:W [CG]:S [CT]:Y [GT]:K [N]:ACGT) default : "
                                + this.workingAlphabet);
        inputMunger.addWatch(Trawler.MAXIMUM_MOTIF_SIZE, 1,
                "Maximum size of motif, default : "
                        + this.maximumMotifSize);
        inputMunger
                .addWatch(
                        Trawler.MAXIMUM_MISMATCHES,
                        1,
                        "Maximum number of mismatches (2 for an N, 1 for a 2-redundant character), default : "
                                + this.maximumMismatches);
        inputMunger.addWatch(Trawler.MINIMUM_OCCURRENCES, 1,
                "Minimum occurrences of motif in conserved sequence, default : "
                        + this.minimumOccurrences);
        inputMunger.addWatch(Trawler.MINIMUM_ZSCORE, 1,
                "Minimum z-score, default : " + this.minimumZScore);
        inputMunger
                .addWatch(
                        Trawler.MAXIMUM_NUMBER_OF_MOTIFS_TO_REPORT,
                        1,
                        "Maximum number of motifs to report in run (to prevent filling a disk), default : "
                                + this.maximumNumberOfMotifsToReport);
        inputMunger.addWatch(Trawler.BYTE_MULTIPLE, 1,
                "Byte multiple, default : " + this.byteMultiple);
        inputMunger.addWatch(Trawler.OUTPUT_FILE, 1,
                "Output file, default : " + this.outputFile);
        inputMunger.addWatch(Trawler.MAXIMUM_ZSCORE, 1,
                "Maximum z-score, default : " + this.maximumZScore);
        inputMunger.addWatch(Trawler.ESTIMATE_MINIMUM_ZSCORE, 0,
                "Estimate the minimum z-score, (flip) default : "
                        + this.estimateMinimumZScore);
        inputMunger.addWatch(Trawler.ESTIMATE_MAXIMUM_ZSCORE, 0,
                "Estimate the maximum z-score, (flip) default : "
                        + this.estimateMaximumZScore);
        inputMunger.addWatch(Trawler.NUMBER_OF_ZSCORE_ESTIMATION_ITERATIONS,
                1,
                "Number of z-score estimation iterations default : "
                        + this.numberOfZScoreEstimationIterations);
        inputMunger
                .addWatch(
                        Trawler.RUN_SCAN_FUNCTION,
                        0,
                        "Run the actual scan function to find motifs, else no motifs reported, (flip) default : "
                                + this.runScanFunction);
        inputMunger.addWatch(Trawler.NUMBER_OF_TAIL_VALUES_TO_AVERAGE_FROM,
                1,
                "Number of tail values to estimate cut off using, default : "
                        + this.numberOfTailValuesToAverageFrom);
        inputMunger
                .addWatch(
                        Trawler.USE_SHUFFLED_BACKGROUND_SET,
                        0,
                        "Use a shuffled background set for estimating the z-score, (flip) default : "
                                + this.useShuffledBackgroundSet);
        inputMunger.addWatch(Trawler.FRAGMENT_SHUFFLE_SIZE, 1,
                "Size of fragments with which to shuffle background, default : "
                        + this.fragmentShuffleSize);
        inputMunger
        .addWatch(
                Trawler.SEARCH_REVERSE_COMPLEMENT,
                0,
                "Search the reverse complement sequence also, (flip) default : "
                        + this.searchReverseComplement);
        if (!inputMunger.parseInput(args)) {
			return null;
		}
        inputMunger.processStandardWatches();
        this.workingAlphabet = inputMunger.parseValue(this.workingAlphabet,
                Trawler.ALPHABET);
        this.maximumMotifSize = inputMunger.parseValue(this.maximumMotifSize,
                Trawler.MAXIMUM_MOTIF_SIZE);
        this.maximumMismatches = inputMunger.parseValue(this.maximumMismatches,
                Trawler.MAXIMUM_MISMATCHES);
        this.minimumOccurrences = inputMunger.parseValue(
                this.minimumOccurrences, Trawler.MINIMUM_OCCURRENCES);
        this.minimumZScore = (float) inputMunger.parseValue(this.minimumZScore,
                Trawler.MINIMUM_ZSCORE);
        this.maximumZScore = (float) inputMunger.parseValue(this.maximumZScore,
                Trawler.MAXIMUM_ZSCORE);
        this.maximumNumberOfMotifsToReport = inputMunger.parseValue(
                this.maximumNumberOfMotifsToReport,
                Trawler.MAXIMUM_NUMBER_OF_MOTIFS_TO_REPORT);
        this.numberOfZScoreEstimationIterations = inputMunger.parseValue(
                this.numberOfZScoreEstimationIterations,
                Trawler.NUMBER_OF_ZSCORE_ESTIMATION_ITERATIONS);
        this.byteMultiple = inputMunger.parseValue(this.byteMultiple,
                Trawler.BYTE_MULTIPLE);
        this.outputFile = inputMunger.parseValue(this.outputFile, Trawler.OUTPUT_FILE);
        if (inputMunger.watchSet(Trawler.ESTIMATE_MINIMUM_ZSCORE)) {
			this.estimateMinimumZScore = !this.estimateMinimumZScore;
		}
        if (inputMunger.watchSet(Trawler.ESTIMATE_MAXIMUM_ZSCORE)) {
			this.estimateMaximumZScore = !this.estimateMaximumZScore;
		}
        if (inputMunger.watchSet(Trawler.RUN_SCAN_FUNCTION)) {
			this.runScanFunction = !this.runScanFunction;
		}
        this.numberOfTailValuesToAverageFrom = inputMunger.parseValue(
                this.numberOfTailValuesToAverageFrom,
                Trawler.NUMBER_OF_TAIL_VALUES_TO_AVERAGE_FROM);
        this.fragmentShuffleSize = inputMunger.parseValue(
                this.fragmentShuffleSize, Trawler.FRAGMENT_SHUFFLE_SIZE);
        if (inputMunger.watchSet(Trawler.USE_SHUFFLED_BACKGROUND_SET)) {
			this.useShuffledBackgroundSet = !this.useShuffledBackgroundSet;
		}
        if (inputMunger.watchSet(Trawler.SEARCH_REVERSE_COMPLEMENT)) {
			this.searchReverseComplement = !this.searchReverseComplement;
		}
        return inputMunger;
    }

    public float[] tailValues(byte[] conservedA, byte[] backgroundA,
            final float[] rA, final boolean maxes) {
        conservedA = conservedA.clone();
        backgroundA = backgroundA.clone();
        if (this.useShuffledBackgroundSet) {
            Trawler.logger.info("Using shuffled background set");
            final byte[] backgroundA2 = backgroundA.clone();
            TrawlerStatistics.shuffleSequences(backgroundA2,
                    this.fragmentShuffleSize);
            TrawlerStatistics.replaceSequences(conservedA,
                    backgroundA2, this.alphabet.validChar);
        }
        else {
            Trawler.logger.info("Not using shuffled background set");
        }
        final SortedSet<Float> sS = new TreeSet<Float>();
        Trawler.logger.info("Running trawler script to estimate cut off");
        this.runTrawler(conservedA, backgroundA, maxes ? TrawlerStatistics
                .getMaxes(rA, sS, this.numberOfTailValuesToAverageFrom)
                : TrawlerStatistics.getMins(rA, sS,
                        this.numberOfTailValuesToAverageFrom));
        final float[] fA = new float[sS.size()];
        int i = 0;
        for (final Iterator<Float> it = sS.iterator(); it.hasNext();) {
			fA[i++] = it.next().floatValue();
		}
        return fA;
    }

    public void runTrawler(final byte[] conservedA, final byte[] backgroundA,
            final TrawlerTools.PassOut passOut) {
        Trawler.logger.info("Starting Trawler script");
        final long startTime = System.currentTimeMillis();
        byte[] alphabetA;
        int[] alphabetICA;
        {
            final Object[] oA = Alphabet.parseAlphabet(this.workingAlphabet,
                    this.alphabet.legalAlphabetPattern,
                    this.alphabet.charToByte, this.alphabet.nucleotideIC);
            alphabetA = (byte[]) oA[0];
            alphabetICA = (int[]) oA[1];
        }
        Alphabet.translateString(conservedA, Trawler.TERMINALCHAR,
                this.alphabet.TERMINAL, this.alphabet.charToByte, (byte) 50,
                (byte) 20);
        Alphabet.translateString(backgroundA, Trawler.TERMINALCHAR,
                this.alphabet.TERMINAL, this.alphabet.charToByte, (byte) 50,
                (byte) 20);
        Trawler.logger.info("Building conserved file suffix tree ");
        final SuffixTree sT = new SuffixTree(conservedA, this.byteMultiple,
                false, this.alphabet.TERMINAL);
        Trawler.logger.info("Building background file suffix tree ");
        final SuffixTree sT2 = new SuffixTree(backgroundA, this.byteMultiple,
                false, this.alphabet.TERMINAL);
        final int[] counts = new int[conservedA.length];
        Trawler.logger.info("Parsing occurrence counts from conserved tree ");
        TrawlerTools.parseCounts(0, counts, sT);
        final int[] counts2 = new int[backgroundA.length];
        Trawler.logger
                .info("Parsing occurrence counts from background tree ");
        TrawlerTools.parseCounts(0, counts2, sT2);
        final int[] iA = new int[100000];
        final int[] iA2 = new int[100000];
        final byte[] bA = new byte[10000];
        {
            final int i = (alphabetA[alphabetA.length - 1] == this.alphabet.WILDCARD) ? alphabetA.length - 1
                    : alphabetA.length;
            Trawler.logger.info("Calling scan function");
            for (int j = 0; j < i; j++) {
				TrawlerTools.scan(0, 1, 0, 1, 0, 0, iA, iA2,
                        alphabetA, bA, this.minimumOccurrences, sT, sT2,
                        counts, counts2, passOut, alphabetICA,
                        this.maximumMismatches, this.alphabet.WILDCARD,
                        this.maximumMotifSize, j);
			}
        }
        Trawler.logger.info(" Finished trawler script in (time s) : "
                + (System.currentTimeMillis() - startTime) / 1000);
    }

    public static TrawlerTools.PassOut output(final float minZScore,
            final float maxZScore, final float[] rA, final Writer w,
            final Function_Int byteToChar, final int maximumOutputSize) {
        return new TrawlerTools.PassOut() {
            PrintWriter pW = new PrintWriter(w);

            int k = 0;

            public final void pro(final int i, final int j, final int depth,
                    final byte[] string) {
                final float z = TrawlerStatistics.zScore(i, j, depth, rA);
                if ((z > minZScore) && (z <= maxZScore)) {
                    if (this.k >= maximumOutputSize) {
                        Trawler.logger
                                .info("Maximum number of motifs outputted, exiting to avoid breaking the disk");
                        this.pW.close();
                        System.exit(0);
                    }
                    this.k++;
                    Trawler.print(i, j, z, depth, string, byteToChar, this.pW);
                }
            }
        };
    }

    public static final void print(final int i, final int j, final float score,
            final int depth, final byte[] string, final Function_Int byteToChar,
            final PrintWriter pW) {
        pW.write(i + "");
        pW.write('\t');
        pW.write(j + "");
        pW.write('\t');
        pW.write(score + "");
        pW.write('\t');
        for (int k = 0; k < depth; k++) {
			pW.write((char) byteToChar.fn(string[k]));
		}
        pW.write('\n');
    }

    public static void main(final String[] args) throws IOException {
        final Trawler trawler = new Trawler();
        final InputMunger inputMunger = trawler
                .setCommandLineArguments(args);
        if (inputMunger == null) {
			System.exit(0);
		}
        final String conservedFile = inputMunger
                .watchStrings(Trawler.CONSERVED_FILE)[0];
        Trawler.logger.info("Parsing conserved file ");
        final byte[] conservedA = Alphabet.readInString(conservedFile,
                Trawler.TERMINALCHAR, trawler.searchReverseComplement);
        final String backgroundFile = inputMunger
                .watchStrings(Trawler.BACKGROUND_FILE)[0];
        Trawler.logger.info("Parsing background file ");
        final byte[] backgroundA = Alphabet.readInString(backgroundFile,
                Trawler.TERMINALCHAR, trawler.searchReverseComplement);
        final float[] rA = new float[trawler.maximumMotifSize + 1];
        {
            final int[] conservedBA = TrawlerStatistics.buckets(conservedA,
                    trawler.maximumMotifSize,
                    trawler.alphabet.validChar);
            final int[] backgroundBA = TrawlerStatistics.buckets(
                    backgroundA, trawler.maximumMotifSize,
                    trawler.alphabet.validChar);
            rA[0] = Float.NaN;
            for (int i = 1; i < rA.length; i++) {
                rA[i] = ((float) conservedBA[i] + 1)
                        / (backgroundBA[i] + 1);
                Trawler.logger.info("For length : " + i
                        + " , conserved count is : " + conservedBA[i]
                        + " , background count is " + backgroundBA[i]
                        + " , prob is " + rA[i]);
            }
        }
        final Writer w = new BufferedWriter(new FileWriter(
                trawler.outputFile));
        if (trawler.estimateMinimumZScore
                || trawler.estimateMaximumZScore) {
            if (trawler.estimateMinimumZScore
                    && trawler.estimateMaximumZScore) {
                Debug
                        .pl(" Can not estimate both min and max zscores at the same time. ");
                System.exit(0);
            }
            float[] fA = new float[0];
            for (int i = 0; i < trawler.numberOfZScoreEstimationIterations; i++) {
                final float[] fA2 = trawler.tailValues(conservedA,
                        backgroundA, rA,
                        trawler.estimateMinimumZScore);
                final float[] fA3 = new float[fA.length + fA2.length];
                System.arraycopy(fA, 0, fA3, 0, fA.length);
                System.arraycopy(fA2, 0, fA3, fA.length, fA2.length);
                fA = fA3;
            }
            double d = 0;
            for (final float element : fA) {
                d += element;
            }
            d /= fA.length;
            if (trawler.estimateMinimumZScore) {
                Trawler.logger.info("Minimum z score is estimated to be : "
                        + d);
                Trawler.logger.info(" Calculated from values : "
                        + IterationTools.join(fA, " "));
                trawler.minimumZScore = (float) d;
            } else {
                Trawler.logger.info("Maximum z score is estimated to be : "
                        + d);
                Trawler.logger.info(" Calculated from values : "
                        + IterationTools.join(fA, " "));
                trawler.maximumZScore = (float) d;
            }
        }
        if (trawler.runScanFunction) {
			trawler.runTrawler(conservedA, backgroundA, Trawler.output(
                    trawler.minimumZScore, trawler.maximumZScore, rA,
                    w, trawler.alphabet.byteToChar,
                    trawler.maximumNumberOfMotifsToReport));
		}
        w.close();
    }
}
