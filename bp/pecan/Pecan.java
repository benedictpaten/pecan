/*
 * Created on May 27, 2005
 */
package bp.pecan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import bp.common.ds.FloatStack;
import bp.common.ds.IntStack;
import bp.common.ds.LockedObject;
import bp.common.ds.ScrollingQueue_Int;
import bp.common.ds.ScrollingQueue_IntTools;
import bp.common.fp.Function;
import bp.common.fp.Function_Index;
import bp.common.fp.Function_Index_2Args;
import bp.common.fp.Function_Index_3Args;
import bp.common.fp.Function_Int;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Function_Int_3Args;
import bp.common.fp.Functions_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorTools;
import bp.common.fp.Generator_Int;
import bp.common.fp.Generators;
import bp.common.fp.Generators_Int;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate;
import bp.common.fp.Predicate_Double_2Args;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_2Args;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_Int_2Args;
import bp.common.fp.Procedure_Int_3Args;
import bp.common.fp.Procedure_NoArgs;
import bp.common.fp.Procedures_Int;
import bp.common.io.ArraysToFile;
import bp.common.io.Debug;
import bp.common.io.ExternalExecution;
import bp.common.io.FastaOutput_Procedure_Int;
import bp.common.io.FastaParser_Generator_Int;
import bp.common.io.InputMunger;
import bp.common.io.NewickTreeParser;
import bp.common.maths.Maths;
import bp.pecan.Chains.CutPoint;
import bp.pecan.utils.PrePecan;

/**
 * @author benedictpaten
 */
public class Pecan {

    static final Logger logger = Logger.getLogger(Pecan.class
            .getName());

    static final String HMM = "HMM";

    static final String TRANSITIVE_ANCHORS = "TRANSITIVE_ANCHORS";

    static final String TRANSITIVE_ANCHOR_DIAGONAL_BOUNDARY = "TRANSITIVE_ANCHOR_DIAGONAL_BOUNDARY";

    static final String ANCHOR_DIAGONAL_WIDTH = "ANCHOR_DIAGONAL_WIDTH"; 

    static final String MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT = "MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT";

    static final String MINIMUM_DIAGONAL_GAP_BETWEEN_CUT_POINT_AND_POLYGON = "MINIMUM_DIAGONAL_GAP_BETWEEN_CUT_POINT_AND_POLYGON";

    static final String MINIMUM_GAP_BETWEEN_CUT_POINTS = "MINIMUM_GAP_BETWEEN_CUT_POINTS";

    static final String COMPUTATION_THRESHOLD = "COMPUTATION_THRESHOLD";

    static final String CONSISTENCY_TRANSFORMATION = "CONSISTENCY_TRANSFORMATION";

    static final String USE_DIRECT_BYTE_BUFFERS = "USE_DIRECT_BYTE_BUFFERS";

    static final String MINIMUM_WEIGHT_HEAP_CAPACITY = "MINIMUM_WEIGHT_HEAP_CAPACITY";

    static final String PRE_CLOSE_GAPS_LARGER_THAN = "PRE_CLOSE_GAPS_LARGER_THAN";

    static final String OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS = "OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS";

    static final String REORDER_BY_OUTGROUP_DISTANCE = "REORDER_BY_OUTGROUP_DISTANCE";

    static final String USE_HMM_WITH_JUNK_STATE = "USE_HMM_WITH_JUNK_STATE";

    static final String PROVIDE_CONFIDENCE_VALUES = "PROVIDE_CONFIDENCE_VALUES";

    static final String INCLUDE_NOT_ALIGNED_VALUES = "INCLUDE_NOT_ALIGNED_VALUES";

    static final String WRITE_OUT_SEPERATE_CONFIDENCE_VALUES_FILE = "WRITE_OUT_SEPERATE_CONFIDENCE_VALUES_FILE";

    static final String WRITE_CHARACTER_CONFIDENCE_VALUES_IN_MFA = "WRITE_CHARACTER_CONFIDENCE_VALUES_IN_MFA";

    static final String CONFIDENCE_FILE = "CONFIDENCE_FILE";

    static final String MAXIMUM_DIAGONAL_DISTANCE_BETWEEN_RESCALINGS = "MAXIMUM_DIAGONAL_DISTANCE_BETWEEN_RESCALINGS";
    
    static final String THRESHOLD_WEIGHTS_ACCORDING_TO_GAP_PROBABILITY = "THRESHOLD_WEIGHTS_ACCORDING_TO_GAP_PROBABILITY";
    
    static final String GAP_GAMMA = "GAP_GAMMA";
    
    static final String LOAD_CONSTRAINING_ALIGNMENT_FROM_FILE = "LOAD_CONSTRAINING_ALIGNMENT_FROM_FILE";
    
    static final String CONSTRAINING_ALIGNMENT_FILE_NAME = "CONSTRAINING_ALIGNMENT_FILE_NAME";
    
    static final String OUTPUT_ALIGNMENT_TO_GAP_PROBABILITIES_TO_FILE = "OUTPUT_ALIGNMENT_TO_GAP_PROBABILITIES_TO_FILE";
    
    static final String GAP_PROBABILITIES_FILE = "GAP_PROBABILITIES_FILE";
    
    static final String OUTPUT_TOP_N_ALIGNMENT_PAIRS_TO_FILE = "OUTPUT_ALIGNMENT_PAIRS_TO_FILE";
    	
    static final String MAXIMUM_NUMBER_OF_ALIGNMENT_PAIRS_TO_WRITE_TO_FILE = "MAXIMUM_NUMBER_OF_ALIGNMENT_PAIRS_TO_WRITE_TO_FILE";
    
    static final String ALIGNMENT_PAIR_PROBABILITIES_FILE = "ALIGNMENT_PAIR_PROBABILITIES_FILE";

    public String hmm = null;

    public String outputFile = "output.mfa";

    // anchors
    public boolean transitiveAnchors = false;

    public int transitiveAnchorDiagonalBoundary = 10;

    // dp
    public int anchorDiagonalWidth = 10;

    public int minimumSizeOfDiagonalForCutPoint = 4; // plus edge trim of

    // three in prePecan
    // makes 10 min

    public int minimumDiagonalGapBetweenCutPointAndPolygon = 15;

    public int minimumGapBetweenCutPoints = 500;

    public int maximumDiagonalDistanceBetweenRescalings = 200;

    public int matrixIteratorLineLength = 50000;

    public int matrixStackChunkSize = 1000000;

    public int aboveThresholdStackChunkSize = 5000;

    public int diagonalLineStackChunkSize = 100000;

    // library
    public float computationThreshold = 0.01f;

    public boolean consistencyTransformation = true;

    public int diagonalGapBetweenRelations = 10;

    public int scrollingSequenceWindowLength = 5000;

    public int weightTranslatorArrayLength = 8000;

    public int scratchArraySize = 100000;

    public int averageWeightNumber = 5;

    public float expansionRateOfWeightHeap = 1.5f;

    public boolean useDirectByteBuffers = true;

    public int minimumWeightHeapCapacity = 1000000;

    public int preCloseGapsLargerThan = 10000;

    public int overhangingDiagonalsIntoPreClosedGaps = 4500;

    public int reorderByOutgroupDistance = 500;

    public boolean useHMMWithJunkState = true;

    public boolean provideConfidenceValues = false;

    public boolean includeNotAlignedValues = true;

    public boolean writeOutSeperateConfidenceValuesFile = true;

    public boolean writeCharacterConfidenceValuesInMFA = false;

    public String confidenceFile = "confidence.txt";

    public boolean provideMatchStateValues = false;

    public boolean writeOutSeperateMatchStateValuesFile = true;

    public boolean writeCharacterMatchStateValuesInMFA = false;

    public String matchStateFileSuffix = "matchState";
    
    public boolean thresholdWeightsAccordingToGapProbability = false;
    
    public float gapGamma = 1.0f;
    
    public boolean loadConstrainingAlignmentFromFile = false;
    
    public String constrainingAlignmentFileName = "constraints.txt";
    
    public boolean outputAlignmentToGapProbabilitiesToFile = false;
    
    public String gapProbabilitiesFile = "gaps.txt";
    
    public boolean outputTopNAlignmentPairsToFile = false;
    
    public int maximumNumberOfAlignmentPairsToWriteToFile = 100000;
    
    public String alignmentPairProbabilitiesFile = "pairprobs.txt";
    
    // global variables
    public String[] fastaIDs;

    public int[] seqSizes;

    public byte[][] cachedSeqFiles;
    

    /*
     * static Function_Int[] seqGets12;
     * 
     * static int[][] seqStartsAndEnds12;
     * 
     * static Generator anchorGen12;
     * 
     * static Generator_Int pointGen12;
     */

    public void PECANScript(final NewickTreeParser.Node tree,
            final String[] seqFiles, PrePecan prePecan) throws Exception {
        final long startTime = System.currentTimeMillis();
        // scratch arrays
        final int[] scratchArray = new int[this.scratchArraySize];
        
        /*
         * Do Pecan-AMAP stuff here!
         */
        
        

        Cell.GetCellCalculator getForwardsLL;
        Cell.GetCellCalculator getForwardsL;
        Cell.GetCellCalculator getForwardsR;
        Cell.GetCellCalculator getBackwardsLL;
        Cell.GetCellCalculator getBackwardsL;
        Cell.GetCellCalculator getBackwardsR;
        float[] startStates;
        float[] endStates;
        int stateNumber;
        final int matchState = 0;
        int alphabetSize;
        Function_Int translateChars;
        final int nValue = 4; // needs to be made part of cell spec
        final char nChar = 'N';

        if (this.hmm != null) {
            // statemachine functions
            Pecan.logger.info("HMM model used : "
                    + ExternalExecution.getAbsolutePath(this.hmm));
            NewickTreeParser nTP;
            {
                final Reader r = new BufferedReader(new FileReader(this.hmm));
                nTP = new NewickTreeParser(NewickTreeParser
                        .commentEater(NewickTreeParser.tokenise(r)));// ExternalExecution.getAbsolutePath(hmm))))));
                r.close();
            }
            Pecan.logger.info("Parsed hmm : " + nTP.tree);
            Cell.isLegitimateHMM(nTP.tree);
            final Object[] program = Cell.createProgram(nTP.tree,
                    Integer.MAX_VALUE, Integer.MAX_VALUE);
            startStates = (float[]) program[Cell.STARTSTATES];
            endStates = (float[]) program[Cell.ENDSTATES];
            stateNumber = startStates.length;
            Pecan.logger.info("State number : " + stateNumber);
            alphabetSize = ((Number) program[Cell.ALPHABETSIZE])
                    .intValue();
            translateChars = (Function_Int) program[Cell.TRANSLATEALPHABETCHAR];

            Pecan.logger.info("Alphabet size : " + alphabetSize);
            final float[] emissions = (float[]) program[Cell.EMISSIONS];
            final int[] programForward = (int[]) program[Cell.PROGRAM];
            final float[] transitionsForward = (float[]) program[Cell.TRANSITIONS];
            Object[] oA = Cell.makeRProgram(programForward,
                    transitionsForward, stateNumber);
            final int[] programBackward = (int[]) oA[0];
            final float[] transitionsBackward = (float[]) oA[1];
            final int[] programForwardL = programForward.clone();
            final int[] programForwardR = programForward.clone();
            Cell.transformProgram(programForwardL,
                    -(this.matrixIteratorLineLength + 1),
                    startStates.length);
            Cell.transformProgram(programForwardR,
                    this.matrixIteratorLineLength + 1, startStates.length);
            oA = Cell.makeRProgram(programForwardL,
                    transitionsForward, startStates.length);
            final int[] programBackwardL = (int[]) oA[0];
            oA = Cell.makeRProgram(programForwardR,
                    transitionsForward, startStates.length);
            final int[] programBackwardR = (int[]) oA[0];
 
            getForwardsLL = Cell.getForwardCellCalculator(
                    programForward, transitionsForward, emissions,
                    alphabetSize);
            getForwardsL = Cell.getForwardCellCalculator(
                    programForwardL, transitionsForward, emissions,
                    alphabetSize);
            getForwardsR = Cell.getForwardCellCalculator(
                    programForwardR, transitionsForward, emissions,
                    alphabetSize);
            getBackwardsLL = Cell.getBackwardCellCalculator(
                    programBackward, transitionsBackward, emissions,
                    startStates.length, alphabetSize);
            getBackwardsL = Cell.getBackwardCellCalculator(
                    programBackwardL, transitionsBackward, emissions,
                    startStates.length, alphabetSize);
            getBackwardsR = Cell.getBackwardCellCalculator(
                    programBackwardR, transitionsBackward, emissions,
                    startStates.length, alphabetSize);
        } else {
            Cell.BasicCell basicCell;
            if (this.useHMMWithJunkState) {
				basicCell = new Cell.FiveCell();
			} else {
				basicCell = new Cell.ThreeCell();
			}
            Pecan.logger.info("Using default hmm");
            startStates = basicCell.startStates;
            endStates = basicCell.endStates;
            stateNumber = basicCell.stateNumber;
            alphabetSize = basicCell.alphabetSize;
            translateChars = basicCell.translateChars;
            getForwardsLL = basicCell.getForwardCellCalculator(
                    stateNumber, stateNumber * 2, stateNumber * 3);
            getBackwardsLL = basicCell.getBackwardCellCalculator(
                    stateNumber, stateNumber * 2, stateNumber * 3);
            getForwardsL = basicCell.getForwardCellCalculator(
                    -stateNumber, this.matrixIteratorLineLength
                            * stateNumber,
                    (this.matrixIteratorLineLength + 1) * stateNumber);
            getBackwardsL = basicCell.getBackwardCellCalculator(
                    -stateNumber, this.matrixIteratorLineLength
                            * stateNumber,
                    (this.matrixIteratorLineLength + 1) * stateNumber);
            getForwardsR = basicCell.getForwardCellCalculator(
                    -stateNumber, -(this.matrixIteratorLineLength + 2)
                            * stateNumber,
                    -(this.matrixIteratorLineLength + 1) * stateNumber);
            getBackwardsR = basicCell.getBackwardCellCalculator(
                    -stateNumber, -(this.matrixIteratorLineLength + 2)
                            * stateNumber,
                    -(this.matrixIteratorLineLength + 1) * stateNumber);
        }
        PecanTools.replaceEdgeLengths(tree, Double.MIN_VALUE, 1);
        Pecan.logger.info("Parsed tree : " + tree);

        // now do the distance matrix
        final int sequenceNumber = seqFiles.length;
        final double[][] seqDistances = PecanTools.getDistances(tree,
                sequenceNumber, Functions_2Args.sum());
        for (int i = 0; i < seqDistances.length; i++) {
            Pecan.logger.info("Sequence distances for " + i + " "
                    + IterationTools.join(seqDistances[i], " "));
        }
        final int[][][] sequenceOutgroups = PecanTools.getOutgroups(
                new LockedObject(scratchArray), seqDistances,
                sequenceNumber);
        final int[][] pairOrdering = PecanTools
                .getPairOrdering(seqDistances);
        for (int i = 0; i < sequenceOutgroups.length; i++) {
			for (int j = i + 1; j < sequenceOutgroups.length; j++) {
                Pecan.logger.info(" Sequence outgroups for "
                        + i
                        + " , "
                        + j
                        + " : "
                        + IterationTools.join(
                                sequenceOutgroups[i][j], " "));
            }
		}

        // create polygon generators from anchor files
        final String[] fastaIDs = new String[sequenceNumber];
        final int[][] seqStartAndEnds = new int[sequenceNumber][2];
        final Generator[][][] anchorFileGenerators = new Generator[sequenceNumber][sequenceNumber][2]; // extra
        // just
        // for
        // visual
        // 2];
        Generator cutPointGenerator;
        final Generator[][][] lessThanPoints = new Generator[sequenceNumber][sequenceNumber][3];
        final int leadLength = 10;
        {
        	final Chains.PrimeConstraints pC;
        	if(loadConstrainingAlignmentFromFile) {
            	//This is the stuff for pecan-amap.
        		//prePecan.runPrePecan(tree, seqFiles);
        		pC = Chains.loadConstraintsFromAlignment(constrainingAlignmentFileName, sequenceNumber);
        		List l = prePecan.getSeqSizes(seqFiles);
                this.fastaIDs = (String[])l.get(0);
                this.seqSizes = (int[])l.get(1);
                this.cachedSeqFiles = (byte[][])l.get(2);
        	}
        	else {
        		pC = prePecan.runPrePecan(tree, seqFiles);
        		this.fastaIDs = prePecan.fastaIDs;
                this.seqSizes = prePecan.seqSizes;
                this.cachedSeqFiles = prePecan.cachedSeqFiles;
        	}
            for (int i = 0; i < sequenceNumber; i++) {
                final int xEnd = this.seqSizes[i];
                for (int j = i + 1; j < sequenceNumber; j++) {
                    final int yEnd = this.seqSizes[j];
                    pC.updatePrimeConstraints(i, j, -leadLength - 1,
                            -leadLength - 1, leadLength);
                    pC.updatePrimeConstraints(j, i, -leadLength - 1,
                            -leadLength - 1, leadLength);
                    pC.updatePrimeConstraints(i, j, xEnd, yEnd, 0);
                    pC.updatePrimeConstraints(j, i, yEnd, xEnd, 0);
                }
            }
        
            final List[][] chains = new List[sequenceNumber][sequenceNumber];
            final List[][] cutPoints = new List[sequenceNumber][sequenceNumber];
            for (int i = 0; i < sequenceNumber; i++) {
                for (int j = i + 1; j < sequenceNumber; j++) {
                    final List l = (List) GeneratorTools.append(pC
                            .convertPrimeConstraintsToEdgeList(i, j,
                                    true), new LinkedList());
                    chains[i][j] = l;
                    final int k = i;
                    final int m = j;
                    cutPoints[i][j] = (List) GeneratorTools
                            .append(
                                    Generators
                                            .map(
                                                    Generators
                                                            .filter(
                                                                    PolygonFiller
                                                                            .cutPointGenerator(
                                                                                    Generators
                                                                                            .iteratorGenerator(l
                                                                                                    .iterator()),
                                                                                    this.minimumSizeOfDiagonalForCutPoint,
                                                                                    100),
                                                                    new Predicate() {
                                                                        int pDiagonal = 0;

                                                                        public boolean test(
                                                                                final Object o) {
                                                                            final int[] iA = (int[]) o;
                                                                            if (iA[0]
                                                                                    + iA[1] > this.pDiagonal
                                                                                    + Pecan.this.minimumGapBetweenCutPoints) {
                                                                                this.pDiagonal = iA[0]
                                                                                        + iA[1];
                                                                                return true;
                                                                            }
                                                                            return false;
                                                                        }
                                                                    }),
                                                    new Function() {
                                                        public Object fn(
                                                                final Object o) {
                                                            final int[] iA = (int[]) o;
                                                            return new Chains.CutPoint(
                                                                    k,
                                                                    m,
                                                                    iA[0],
                                                                    iA[1],
                                                                    Pecan.this.minimumDiagonalGapBetweenCutPointAndPolygon);
                                                        }
                                                    }),
                                    new LinkedList());
                }
            }
            // Chains.transformConstraints(anchorDiagonalWidth, pC);
            {
                Chains.LocalAligner localAligner = PrePecan
                        .makeExonerateAligner(
                                prePecan.exonerateBasicCommand,
                                prePecan.exonerateString,
                                prePecan.exoneratePartitionAlignerWordLength,
                                prePecan.exoneratePartitionAlignerMinScore,
                                prePecan.exoneratePartitionAlignerGappedExtension,
                                prePecan.exoneratePartitionAlignerModel,
                                prePecan.saturateThreshold,
                                prePecan.exoneratePartitionAlignerSoftMaskSequences,
                                prePecan.exoneratePartitionAlignerSoftMaskSequences);
                if (prePecan.rescoreAlignments) {
					localAligner = Chains.rescoreAlignments(
                            localAligner,
                            prePecan.relativeEntropyThreshold);
				}
                final Chains.Aligner aligner = Chains
                        .alignerConvertor(localAligner);
                Chains.transformConstraintsAndCloseLargeGaps(pC,
                        chains, this.anchorDiagonalWidth,
                        this.preCloseGapsLargerThan,
                        this.overhangingDiagonalsIntoPreClosedGaps,
                        aligner, this.cachedSeqFiles,
                        pairOrdering);
            }
            for (int i = 0; i < sequenceNumber; i++) {
                final int xEnd = this.seqSizes[i];
                for (int j = i + 1; j < sequenceNumber; j++) {
                    final int yEnd = this.seqSizes[j];
                    pC.updatePrimeConstraints(i, j, -leadLength - 1,
                            -leadLength - 1, leadLength);
                    pC.updatePrimeConstraints(j, i, -leadLength - 1,
                            -leadLength - 1, leadLength);
                    pC.updatePrimeConstraints(i, j, xEnd, yEnd, 0);
                    pC.updatePrimeConstraints(j, i, yEnd, xEnd, 0);
                }
            }

            /*
             * { int noOfConstraints=0; for (int i = 0; i < sequenceNumber; i++) {
             * for(int j=0; j<sequenceNumber; j++) if(i != j) { noOfConstraints +=
             * pC.primeConstraints[i][j].size(); } } Debug.pl("constraints
             * run-down " + Chains.noOfLongGapOptimisations + " " +
             * Chains.PrimeConstraints.noOfConstraintsClipped + " " +
             * noOfConstraints); }
             */

            {
                int j = 0;
                for (int i = 0; i < sequenceNumber; i++) {
                    seqStartAndEnds[i][0] = j;
                    seqStartAndEnds[i][1] = (j += this.seqSizes[i]);
                    fastaIDs[i] = this.fastaIDs[i];
                }
            }
            {
                final Function edgeNodeXYToArray = new Function() {
                    public Object fn(Object o) {
                        PolygonFiller.Node n = (PolygonFiller.Node) o;
                        return new int[] { n.x, n.y };
                    }
                };
                for (int i = 0; i < sequenceNumber; i++) {
                    final int xStart = seqStartAndEnds[i][0];
                    for (int j = i + 1; j < sequenceNumber; j++) {
                        final int yStart = seqStartAndEnds[j][0];
                        {
                            //final String s = PecanTools.getTempFileHandle();
                            final ByteArrayOutputStream bAOS2 = new ByteArrayOutputStream();
                            final List l = (List) GeneratorTools
                                    .append(
                                            PolygonFiller
                                                    .transformEdges(
                                                            PolygonFiller
                                                                    .flipEdgeXYDiagonalsCoordinates(pC
                                                                            .convertPrimeConstraintsToEdgeList(
                                                                                    j,
                                                                                    i,
                                                                                    false)),
                                                            xStart,
                                                            yStart),
                                            new LinkedList());
                            int k = //PecanTools
                                   // .toFile(
                            	AnchorParser_Generator.writeOutEdgeList(
                                            Generators
                                                    .filter(
                                                            Generators
                                                                    .iteratorGenerator(l
                                                                            .iterator()),
                                                            PolygonFiller
                                                                    .isLessThanOrEqual()),
                                                                    bAOS2);
                                      //      s, true);
                            {
                                final Generator gen = AnchorParser_Generator
                                        .readInEdgeList(
                                                new BufferedInputStream(
                                                		new ByteArrayInputStream(bAOS2.toByteArray())),
                                                        //new FileInputStream(
                                                        //        s)),
                                                		
                                                k);
                                final Generator[] gA = Generators
                                        .splitGenerator(gen,
                                                PolygonFiller
                                                        .cloneEdge());
                                anchorFileGenerators[i][j][0] = gA[0];
                                anchorFileGenerators[i][j][1] = gA[1];

                                /*
                                 * this code is just for visuals anchorGen12 =
                                 * PolygonFiller .transformEdges(
                                 * AnchorParser_Generator .readInEdgeList( new
                                 * BufferedInputStream( new FileInputStream(
                                 * s)), k), -anchorDiagonalWidth,
                                 * anchorDiagonalWidth);
                                 */
                            }
                            final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                            k = ArraysToFile.writeOutArray(Generators
                                    .map(Generators.filter(Generators
                                            .iteratorGenerator(l
                                                    .iterator()),
                                            PolygonFiller
                                                    .isLessThan()),
                                            edgeNodeXYToArray), bAOS);
                            final byte[] bA = bAOS.toByteArray();
                            for (int m = 0; m < 3; m++) {
                                lessThanPoints[i][j][m] = ArraysToFile
                                        .readInArray(
                                                new ByteArrayInputStream(
                                                        bA), k, 2);
                            }
                        }
                        {
                            //final String s = PecanTools.getTempFileHandle();
                            final ByteArrayOutputStream bAOS2 = new ByteArrayOutputStream();
                            final List l = (List) GeneratorTools
                                    .append(
                                            PolygonFiller
                                                    .transformEdges(
                                                            PolygonFiller
                                                                    .flipEdgeXYDiagonalsCoordinates(pC
                                                                            .convertPrimeConstraintsToEdgeList(
                                                                                    i,
                                                                                    j,
                                                                                    false)),
                                                            yStart,
                                                            xStart),
                                            new LinkedList());
                            int k = AnchorParser_Generator.writeOutEdgeList(
                                    		Generators
                                                    .filter(
                                                            Generators
                                                                    .iteratorGenerator(l
                                                                            .iterator()),
                                                            PolygonFiller
                                                                    .isLessThanOrEqual()),
                                           bAOS2);
                            {
                                final Generator gen = AnchorParser_Generator
                                        .readInEdgeList(
                                                new BufferedInputStream(
                                                		new ByteArrayInputStream(bAOS2.toByteArray())),
                                                        //new FileInputStream(
                                                        //        s)),
                                                k);
                                final Generator[] gA = Generators
                                        .splitGenerator(gen,
                                                PolygonFiller
                                                        .cloneEdge());
                                anchorFileGenerators[j][i][0] = gA[0];
                                anchorFileGenerators[j][i][1] = gA[1];
                            }
                            final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                            k = ArraysToFile.writeOutArray(Generators
                                    .map(Generators.filter(Generators
                                            .iteratorGenerator(l
                                                    .iterator()),
                                            PolygonFiller
                                                    .isLessThan()),
                                            edgeNodeXYToArray), bAOS);
                            final byte[] bA = bAOS.toByteArray();
                            for (int m = 0; m < 3; m++) {
                                lessThanPoints[j][i][m] = ArraysToFile
                                        .readInArray(
                                                new ByteArrayInputStream(
                                                        bA), k, 2);
                            }
                        }
                    }
                }
            }
            {
                final Comparator c = new Comparator() {
                    public int compare(Object arg0, Object arg1) {
                        CutPoint cP = (CutPoint) arg0;
                        CutPoint cP2 = (CutPoint) arg1;
                        int i = cP.x + cP.y;
                        int j = cP2.x + cP2.y;
                        return i < j ? -1 : i > j ? 1 : 0;
                    }
                };
                final List[][] lAA = new List[sequenceNumber][sequenceNumber];
                final List[][] lAA2 = new List[sequenceNumber][sequenceNumber];
                for (int i = 0; i < sequenceNumber; i++) {
					for (int j = i + 1; j < sequenceNumber; j++) {
                        final List l2 = Chains.breaks(pC, i, j);
                        lAA[i][j] = l2;
                        final List l3 = new LinkedList();
                        lAA2[i][j] = l3;
                        final List l4 = cutPoints[i][j];
                        for (final Iterator it = l2.iterator(); it
                                .hasNext();) {
                            final int[] iA2 = (int[]) it.next();
                            l3.add(new Chains.CutPoint(i, j, iA2[0],
                                    iA2[1], Integer.MAX_VALUE));
                        }
                        cutPoints[i][j] = (List) IterationTools
                                .append(
                                        Iterators
                                                .merge(
                                                        Chains
                                                                .filterCutPoints(
                                                                        l4,
                                                                        l2,
                                                                        this.minimumDiagonalGapBetweenCutPointAndPolygon + 2),
                                                        l3.iterator(),
                                                        c),
                                        new LinkedList());
                        {
                            int k = Integer.MIN_VALUE;
                            if (l2.size() != 0) {
                                final int[] iA = (int[]) l2
                                        .get(l2.size() - 1);
                                k = iA[2] + iA[3] + 2;
                            }
                            if (k < this.seqSizes[i]
                                    + this.seqSizes[j]) {
                                l2.add(new int[] { 0, 0,
                                        this.seqSizes[i] - 1,
                                        this.seqSizes[j] });
                                final CutPoint cP = new Chains.CutPoint(i,
                                        j, this.seqSizes[i] - 1,
                                        this.seqSizes[j] - 1,
                                        Integer.MAX_VALUE);
                                l3.add(cP);
                                cutPoints[i][j].add(cP);
                            }
                        }
                    }
				}
                List l2 = Chains.CutPointOrdering.orderCutPoints(pC,
                        cutPoints);
                l2 = Chains.CutPointReordering.reorder(l2,
                        sequenceNumber, sequenceOutgroups,
                        pairOrdering, this.reorderByOutgroupDistance
                                * (sequenceNumber
                                        * (sequenceNumber - 1) / 2),
                        -10);
                for (int i = 0; i < sequenceNumber; i++) {
					for (int j = i + 1; j < sequenceNumber; j++) {
                        final Iterator it = lAA[i][j].iterator();
                        final Iterator it2 = lAA2[i][j].iterator();
                        while (it.hasNext()) {
                            final int[] iA = (int[]) it.next();
                            final CutPoint cP = (CutPoint) it2.next();
                            // Debug.pl(" cut point "
                            // + IterationTools.join(iA, " ")
                            // + " : " + cP.toString());
                            cP.x = iA[2] + 1;
                            cP.y = iA[3];
                        }
                        if (Debug.DEBUGCODE && it2.hasNext()) {
							throw new IllegalStateException();
						}
                    }
				}
                for (final Iterator it = l2.iterator(); it.hasNext();) {
                    final CutPoint cP = (CutPoint) it.next();
                    cP.x += seqStartAndEnds[cP.s1][0];
                    cP.y += seqStartAndEnds[cP.s2][0];
                }
                final String s = PecanTools.getTempFileHandle();
                final OutputStream oS = new BufferedOutputStream(
                        new FileOutputStream(s));
                final int k = ArraysToFile.writeOutArray(Generators.map(
                        Generators.iteratorGenerator(l2.iterator()),
                        new Function() {
                            public Object fn(Object o) {
                                CutPoint cP = (CutPoint) o;
                                return new int[] { cP.s1, cP.s2,
                                        cP.x, cP.y, cP.tB };
                            }
                        }), oS);
                oS.close();
                cutPointGenerator = Generators.map(ArraysToFile
                        .readInArray(new BufferedInputStream(
                                new FileInputStream(s)), k, 5),
                        new Function() {
                            public Object fn(final Object o) {
                                final int[] iA = (int[]) o;
                                return new CutPoint(iA[0], iA[1],
                                        iA[2], iA[3], iA[4]);
                            }
                        });
            }
            prePecan = null;
        }
        final Generator_Int[] seqGens = new Generator_Int[seqFiles.length];
        {
            for (int i = 0; i < sequenceNumber; i++) {
                final FastaParser_Generator_Int gen2 = new FastaParser_Generator_Int(
                        new BufferedInputStream(new FileInputStream(
                                seqFiles[i])), nChar);
                final Function_Int fn = translateChars;
                seqGens[i] = new Generator_Int() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Generator_Int#gen()
                     */
                    public int gen() {
                        return fn.fn(gen2.gen());
                    }
                };
                Pecan.logger.info("Seq " + seqFiles[i] + " start : "
                        + seqStartAndEnds[i][0]);
                Pecan.logger.info("Seq " + seqFiles[i] + " end : "
                        + seqStartAndEnds[i][1]);
            }
        }
        final Function_Int[] seqGets = new Function_Int[sequenceNumber];
        final Procedure_Int[] seqAdds = new Procedure_Int[sequenceNumber];
        final ScrollingQueue_Int[] sequences = new ScrollingQueue_Int[sequenceNumber];

        for (int i = 0; i < sequenceNumber; i++) {
            final ScrollingQueue_Int sQ = new ScrollingQueue_Int(
                    this.scrollingSequenceWindowLength,
                    seqStartAndEnds[i][0] - 1, true);
            sQ.add(nValue);
            sequences[i] = sQ;
            seqGets[i] = sQ;
            seqAdds[i] = ScrollingQueue_IntTools.fillFromGenerator(
                    seqGens[i], sQ);
        }

        // alignment generators
        final Function[][] alignmentGenerators = new Function[sequenceNumber][sequenceNumber];
        final boolean[][] pairStarts = new boolean[sequenceNumber][sequenceNumber];
        {
            System.gc();
            int initialSize = this.averageWeightNumber
                    * (sequenceNumber * sequenceNumber - sequenceNumber)
                    * 2 * this.scrollingSequenceWindowLength * 12;
            if (initialSize < this.minimumWeightHeapCapacity) {
				initialSize = this.minimumWeightHeapCapacity;
			}
            PairsHeap.initialise(initialSize,
                    this.expansionRateOfWeightHeap, this.useDirectByteBuffers);
        }
        final PairsHeap[][] sQAA = new PairsHeap[sequenceNumber][sequenceNumber];
        final ScrollingQueue_Int[][] maxOffsets = new ScrollingQueue_Int[sequenceNumber][sequenceNumber];
        final Procedure_Int[][] maxOffsetsFiller = new Procedure_Int[sequenceNumber][sequenceNumber];
        for (int j = 0; j < sequenceNumber; j++) {
            for (int k = j + 1; k < sequenceNumber; k++) {
                final PairsHeap[] pHA = PairsHeap.getPair(
                        seqStartAndEnds[j][0], seqStartAndEnds[k][0],
                        this.scrollingSequenceWindowLength);
                sQAA[j][k] = pHA[0];
                sQAA[k][j] = pHA[1];
                maxOffsets[j][k] = new ScrollingQueue_Int(
                        this.scrollingSequenceWindowLength,
                        seqStartAndEnds[j][0], true);
                maxOffsets[k][j] = new ScrollingQueue_Int(
                        this.scrollingSequenceWindowLength,
                        seqStartAndEnds[k][0], true);
                maxOffsetsFiller[j][k] = ScrollingQueue_IntTools
                        .fillFromGenerator(Generators_Int
                                .constant(Integer.MAX_VALUE),
                                maxOffsets[j][k]);
                maxOffsetsFiller[k][j] = ScrollingQueue_IntTools
                        .fillFromGenerator(Generators_Int
                                .constant(Integer.MAX_VALUE),
                                maxOffsets[k][j]);
            }
        }

        final MatrixIterator mI = new MatrixIterator(stateNumber,
                this.matrixIteratorLineLength);
        final FloatStack matrixStack = new FloatStack(this.matrixStackChunkSize);
        final IntStack aboveThresholdStack = new IntStack(
                this.aboveThresholdStackChunkSize);
        final FloatStack diagonalLineStack = new FloatStack(
                this.diagonalLineStackChunkSize);
        final float[] forwardDiagonal = new float[this.matrixIteratorLineLength
                * 2 * stateNumber];
        final float[] reverseDiagonal = new float[this.matrixIteratorLineLength
                * 2 * stateNumber];
        for (int i = 0; i < sequenceNumber; i++) {
            final Function_Int seqGetI = seqGets[i];
            final Procedure_Int seqAddI = seqAdds[i];
            final int xStart = seqStartAndEnds[i][0];
            for (int j = i + 1; j < sequenceNumber; j++) {
                final int yStart = seqStartAndEnds[j][0];
                final int numberOfSequenceOutgroups = sequenceOutgroups[i][j].length;
                Function_Index fn;
                final Procedure_Int edgeListRowsCutUpFeeder;
                final Procedure_Int edgeListColumnsCutUpFeeder;
                Generator edgeListRows;
                Generator edgeListColumns;
                if (this.transitiveAnchors
                        && (numberOfSequenceOutgroups > 0)) {
                    final Function_Index_2Args transitiveAnchorsColumn = PecanTools
                            .transitiveAnchors(
                                    this.transitiveAnchorDiagonalBoundary,
                                    maxOffsets[i][j]);
                    final Function_Index_2Args transitiveAnchorsRow = PecanTools
                            .transitiveAnchors(
                                    this.transitiveAnchorDiagonalBoundary,
                                    maxOffsets[j][i]);
                    final PecanTools.CutUpDiagonals edgeListRowsCutUpFeeder2 = new PecanTools.CutUpDiagonals(
                            anchorFileGenerators[i][j][0]);
                    edgeListRowsCutUpFeeder = edgeListRowsCutUpFeeder2;
                    final PecanTools.CutUpDiagonals edgeListColumnsCutUpFeeder2 = new PecanTools.CutUpDiagonals(
                            anchorFileGenerators[j][i][0]);
                    edgeListColumnsCutUpFeeder = edgeListColumnsCutUpFeeder2;
                    edgeListRows = PolygonFiller
                            .mergeEdgeListWithTransitiveAnchors(
                                    edgeListRowsCutUpFeeder2,
                                    transitiveAnchorsRow,
                                    PolygonFiller
                                            .filterEdgeListByLessThanConstraints(
                                                    lessThanPoints[j][i][0],
                                                    this.anchorDiagonalWidth),
                                    PolygonFiller
                                            .filterEdgeListByLessThanOrEqualConstraints(
                                                    anchorFileGenerators[j][i][1],
                                                    this.anchorDiagonalWidth));
                    edgeListColumns = PolygonFiller
                            .mergeEdgeListWithTransitiveAnchors(
                                    edgeListColumnsCutUpFeeder2,
                                    transitiveAnchorsColumn,
                                    PolygonFiller
                                            .filterEdgeListByLessThanConstraints(
                                                    lessThanPoints[i][j][0],
                                                    this.anchorDiagonalWidth),
                                    PolygonFiller
                                            .filterEdgeListByLessThanOrEqualConstraints(
                                                    anchorFileGenerators[i][j][1],
                                                    this.anchorDiagonalWidth));
                } else {
                    edgeListRowsCutUpFeeder = Procedures_Int
                            .doNothing();
                    edgeListColumnsCutUpFeeder = Procedures_Int
                            .doNothing();
                    lessThanPoints[i][j][0] = null;
                    lessThanPoints[j][i][0] = null;
                    edgeListRows = anchorFileGenerators[i][j][0];
                    edgeListColumns = anchorFileGenerators[j][i][0];
                }
                {
                    final Function xYArrayToEdgeNode = new Function() {
                        public final Object fn(Object o) {
                            int[] iA = (int[]) o;
                            int x = iA[0], y = iA[1];
                            return new PolygonFiller.Node(x, y, y, 0);
                        }
                    };
                    fn = PolygonFiller.polygonIterator(
                            // edgeListRows,
                            PolygonFiller.mergeLessThansEdgeList(
                                    edgeListRows, Generators.map(
                                            lessThanPoints[i][j][2],
                                            xYArrayToEdgeNode)),
                            // edgeListColumns,
                            PolygonFiller.mergeLessThansEdgeList(
                                    edgeListColumns, Generators.map(
                                            lessThanPoints[j][i][2],
                                            xYArrayToEdgeNode)),
                            xStart - leadLength,
                            Integer.MAX_VALUE / 2, yStart
                                    - leadLength,
                            Integer.MAX_VALUE / 2);
                    fn = PolygonFiller.polygonIteratorWithLessThans(
                            fn, lessThanPoints[i][j][1],
                            lessThanPoints[j][i][1], scratchArray);
                }

                /*
                 * { if (i == 0 && j == 1) { fn.fn(xStart + yStart);
                 * 
                 * List three = (List) GeneratorTools.append( Pecan.anchorGen12,
                 * new LinkedList());
                 * 
                 * int xOffset = 0; int yOffset = 0;
                 * 
                 * int[] iA = PolygonFillerTest .getDisplayMatrix(900, 900);
                 * PolygonFiller.transformEdgeList(three, -xStart - xOffset,
                 * -yStart - yOffset); int[] polysA = new int[] { 500, 750, 2000 };
                 * for (int polys = 0; polys < polysA.length; polys++) {
                 * Object[] oA2 = (Object[]) fn.fn(xStart + yStart +
                 * polysA[polys]); List one = (List) oA2[0]; List two = (List)
                 * oA2[1]; for (Iterator it = one.iterator(); it .hasNext();) {
                 * PolygonFiller.Node n = (PolygonFiller.Node) it .next();
                 * Debug.pl(n + " one "); } for (Iterator it = two.iterator();
                 * it .hasNext();) { PolygonFiller.Node n = (PolygonFiller.Node)
                 * it .next(); Debug.pl(n + " two "); }
                 * PolygonFillerTest.checkPolygon(one, two);
                 * PolygonFiller.transformEdgeList(one, -xStart - xOffset,
                 * -yStart - yOffset); PolygonFiller.transformEdgeList(two,
                 * -xStart - xOffset, -yStart - yOffset);
                 * PolygonFillerTest.addLines(iA, one, 900, 900,
                 * PolygonFillerTest.LEFT_COLOUR);
                 * PolygonFillerTest.addLines(iA, two, 900, 900,
                 * PolygonFillerTest.RIGHT_COLOUR); int[] lLTA = (int[]) oA2[3];
                 * int[] rLTA = (int[]) oA2[4]; PolygonFillerTest.addPoints(iA,
                 * 900, 900, lLTA, xStart + xOffset, yStart + yOffset);
                 * PolygonFillerTest.addPoints(iA, 900, 900, rLTA, xStart +
                 * xOffset, yStart + yOffset); }
                 * PolygonFillerTest.fillInHorizontalRows( iA, 900, 900,
                 * PolygonFillerTest.LEFT_COLOUR);
                 * PolygonFillerTest.fillInHorizontalRows( iA, 900, 900,
                 * PolygonFillerTest.RIGHT_COLOUR);
                 * PolygonFillerTest.addLines(iA, three, 900, 900,
                 * PolygonFillerTest.ANCHOR_COLOUR);
                 * 
                 * PolygonFillerTest.displayMatrix(iA, 900, 900); } }
                 */

                final float[] holdingForwardDiagonal = new float[(((this.anchorDiagonalWidth + this.minimumDiagonalGapBetweenCutPointAndPolygon) * 2 + 1) * 2 + 1)
                        * stateNumber];
                Arrays.fill(holdingForwardDiagonal,
                        Float.NEGATIVE_INFINITY);
                System.arraycopy(startStates, 0,
                        holdingForwardDiagonal, startStates.length,
                        startStates.length);
                // cell calculator
                final Function_Int seqGetJ = seqGets[j];

                final Cell.CellCalculator forwardsLL = getForwardsLL
                        .getCellCalculator(seqGetI, seqGetJ);
                final Cell.CellCalculator forwardsL = getForwardsL
                        .getCellCalculator(seqGetI, seqGetJ);
                final Cell.CellCalculator forwardsR = getForwardsR
                        .getCellCalculator(seqGetI, seqGetJ);
                final Cell.CellCalculator backwardsLL = getBackwardsLL
                        .getCellCalculator(seqGetI, seqGetJ);
                final Cell.CellCalculator backwardsL = getBackwardsL
                        .getCellCalculator(seqGetI, seqGetJ);
                final Cell.CellCalculator backwardsR = getBackwardsR
                        .getCellCalculator(seqGetI, seqGetJ);

                final ForwardBackwardMatrixIter.AlignmentGenerator aG = new ForwardBackwardMatrixIter.AlignmentGenerator(
                        startStates.length, matchState, Maths
                                .log(this.computationThreshold),
                        matrixStack, aboveThresholdStack);

                final ForwardBackwardMatrixIter fBMI = new ForwardBackwardMatrixIter(
                        fn, startStates.length, xStart - 1,
                        yStart - 1, 0, holdingForwardDiagonal,
                        forwardDiagonal, reverseDiagonal, endStates,
                        forwardsLL, backwardsLL, forwardsL,
                        forwardsR, backwardsL, backwardsR,
                        aG.forwards, aG.backwards,
                        aG.passRunningTotal, mI, diagonalLineStack,
                        this.maximumDiagonalDistanceBetweenRescalings,
                        aG.reset);
                final Function_Index_3Args fnFB = ForwardBackwardMatrixIter
                        .cutPointAlignmentGenerator(fBMI);
                final Procedure_Int seqAddJ = seqAdds[j];
                {
                    final int m = i;
                    final int n = j;
                    final Function_Index fn2 = fn;
                    alignmentGenerators[i][j] = new Function() {
                        public Object fn(final Object o) {
                            final Chains.CutPoint cutPoint = (Chains.CutPoint) o;
                            final int d = cutPoint.x + cutPoint.y;
                            edgeListColumnsCutUpFeeder.pro(d);
                            edgeListRowsCutUpFeeder.pro(d);
                            if (!pairStarts[m][n]) {
                                fn2.fn(xStart - 1 + yStart - 1); // clip that
                                // top
                                // corner by one
                                pairStarts[m][n] = true;
                                if ((cutPoint.x == xStart)
                                        && (cutPoint.y == yStart)) {
									return new int[] { xStart - 1,
                                            yStart - 1, xStart - 1,
                                            yStart - 1, xStart - 1,
                                            yStart - 1, xStart - 1,
                                            yStart - 1 };
								}
                            }
                            Pecan.logger
                                    .info(" Actually doing "
                                            + cutPoint);
                            int p = cutPoint.x + Pecan.this.anchorDiagonalWidth
                                    + 2;
                            seqAddI.pro(p);
                            p = cutPoint.y + Pecan.this.anchorDiagonalWidth + 2;
                            seqAddJ.pro(p);
                            final Object o2 = fnFB.fn(cutPoint.x,
                                    cutPoint.y, cutPoint.tB);
                            return o2;
                        }
                    };
                }
            }
        }

        final File[] tempOutFiles = new File[sequenceNumber];
        final FastaOutput_Procedure_Int[] fastaWriters = new FastaOutput_Procedure_Int[sequenceNumber];
        final Procedure_Int[] outputFns = new Procedure_Int[sequenceNumber];
        {
            for (int i = 0; i < fastaIDs.length; i++) {
                final String fastaID = fastaIDs[i];
                final File f = File.createTempFile("tmp_output_", ".fa");
                f.deleteOnExit();
                Pecan.logger.info(" Temp output file for : " + fastaID
                        + " is : " + f.toString());
                tempOutFiles[i] = f;
                fastaWriters[i] = new FastaOutput_Procedure_Int(
                        new BufferedOutputStream(
                                new FileOutputStream(f)), fastaID);
                final FastaParser_Generator_Int fPGI = new FastaParser_Generator_Int(
                        new BufferedInputStream(new FileInputStream(
                                seqFiles[i])), Integer.MAX_VALUE);
                final Procedure_Int outputFn = fastaWriters[i];
                outputFns[i] = new Procedure_Int() {
                    public void pro(final int i) {
                        outputFn.pro(i == '-' ? '-' : fPGI.gen());
                    }
                };
            }
        }

        Procedure outputAdaptor = new Procedure() {
            Procedure_2Args convertInput = AlignmentStitcher
                    .convertInput();

            Librarian.FreeMemory freeMem = new Librarian.FreeMemory(
                    sQAA, seqStartAndEnds, sequenceNumber);

            int upto = 0;

            // int pUpto = 0;

            int[] uptoA = new int[sequenceNumber];
            {
                for (int i = 0; i < sequenceNumber; i++) {
					this.uptoA[i] = seqStartAndEnds[i][0] - 1;
				}
            }

            public void pro(Object o) {
                Iterator it = ((List) o).iterator();

                if (it.hasNext()) {
                    int[] iA;
                    do {
                        this.upto++;
                        iA = new int[sequenceNumber];
                        this.convertInput.pro(it.next(), iA);
                        for (int j = 0; j < iA.length; j++) {
                            int k = iA[j];
                            if (k != Integer.MAX_VALUE) {
                                this.uptoA[j] = k;
                                outputFns[j].pro(0);
                                sequences[j].removeFirst();
                                ScrollingQueue_Int[] sQA = maxOffsets[j];
                                for (int m = 0; m < j; m++) {
									sQA[m].removeFirst();
								}
                                for (int m = j + 1; m < sequenceNumber; m++) {
									sQA[m].removeFirst();
								}
                                if (Debug.DEBUGCODE) {
                                    if (sequences[j].firstIndex() != k) {
										throw new IllegalStateException();
									}
                                    for (int m = 0; m < sequenceNumber; m++) {
										if ((m != j)
                                                && (sQA[m]
                                                        .firstIndex() != k + 1)) {
											throw new IllegalStateException(
                                                    sQA[m]
                                                            .firstIndex()
                                                            + " "
                                                            + k
                                                            + " ");
										}
									}
                                }
                            } else {
								outputFns[j].pro('-');
							}
                        }
                    } while (it.hasNext());

                    for (int i = 0; i < sequenceNumber; i++) {
						this.freeMem.pro(i, this.uptoA[i]);
					}
                    for (int i = 0; i < sequenceNumber; i++) {
						this.freeMem.pro(i);
					}
                }
            }
        };

        final Function_Int_2Args add = new Function_Int_2Args() {
            public int fn(int i, int j) {
                return Librarian.sum(i, j);
            }
        };

        final List<Procedure_NoArgs> finishConfidences = new LinkedList<Procedure_NoArgs>();
        if (this.provideConfidenceValues) {
            final List<Procedure_Int> outputConfidences = new LinkedList<Procedure_Int>();
            if (this.writeCharacterConfidenceValuesInMFA) {
                PecanTools.outputTrack(outputConfidences,
                        finishConfidences, this.outputFile,
                        "confidence_values");
            }
            if (this.writeOutSeperateConfidenceValuesFile) {
                PecanTools.outputFloatFile(outputConfidences,
                        finishConfidences, this.confidenceFile);
            }
            final Function getTotal = this.includeNotAlignedValues ? PecanTools
                    .totalScoreMinus(PecanTools.getWeight(sQAA),
                            PecanTools.sumTotal(sQAA)) : PecanTools
                    .totalScore(PecanTools.getWeight(sQAA));
            outputAdaptor = PecanTools.outputTrackAdaptor(getTotal,
                    sequenceNumber, outputConfidences, outputAdaptor);
        }
        
        PrintWriter gapPW = null;
        if (this.outputAlignmentToGapProbabilitiesToFile) {
        	gapPW = PecanTools.getPrintWriter(this.gapProbabilitiesFile);    
            outputAdaptor = 
            	PecanTools.outputGapAlignments(sQAA, sequenceNumber, 
            								   outputAdaptor, gapPW);
        }
        
        PriorityQueue<PairValue> pQueue = new PriorityQueue<PairValue>();
        List<Integer> numberOfPairsRejected = new ArrayList<Integer>();
        numberOfPairsRejected.add(new Integer(0));
        if (this.outputTopNAlignmentPairsToFile) {
            outputAdaptor = 
            	PecanTools.outputAlignmentPairs(sQAA, sequenceNumber, 
            			outputAdaptor, pQueue, numberOfPairsRejected,
            			this.maximumNumberOfAlignmentPairsToWriteToFile);
        }
        
        final Librarian.WeightsGetter getWeightsForLibrary_NoConsistency_NoMultipleMatchStates = Librarian
                .weightsGetter(sQAA);
        final Librarian.WeightsGetter getWeightsForLibrary = this.consistencyTransformation ? Librarian
                .weightsGetter_Consistency(sQAA)
                : (this.thresholdWeightsAccordingToGapProbability ? Librarian.weightsGetter_FilterByGapThreshold(sQAA, this.gapGamma) : getWeightsForLibrary_NoConsistency_NoMultipleMatchStates);
                
        final Function_Int_3Args getMaxResidue = Librarian
                .getMaxResidue(maxOffsets);
        final Procedure_Int_3Args releaseWeights = new Procedure_Int_3Args() {
            public void pro(int i, int j, int k) {
                // do nothing
            };
        };
        final double minValueDrip = 0.0;
        final DripAligner.Add dripAdder = new DripAligner.Add() {
            public double fn(double d, int i) {
                double d2 = d + Float.intBitsToFloat(i);
                if (Debug.DEBUGCODE && (d2 == d)) {
					throw new IllegalStateException(d + " " + d2
                            + " " + Float.intBitsToFloat(i));
				}
                return d2;
            }
        };
        final Predicate_Double_2Args greaterThanDrip = new Predicate_Double_2Args() {
            public boolean test(double d, double d2) {
                return d > d2;
            }
        };
        // alignment pump
        final Procedure[] finishedSeqs = new Procedure[sequenceNumber];
        AlignmentPump.getAlignmentPumps(outputAdaptor,
                getWeightsForLibrary, getMaxResidue, seqStartAndEnds,
                finishedSeqs, 0, tree, this.weightTranslatorArrayLength,
                add, dripAdder, greaterThanDrip, minValueDrip,
                releaseWeights, scratchArray);
        final Procedure_Int[] finishedSeqsAdaptors = new Procedure_Int[sequenceNumber];
        for (int i = 0; i < sequenceNumber; i++) {
            final int start = seqStartAndEnds[i][0];
            final Procedure passThrough = finishedSeqs[i];
            finishedSeqsAdaptors[i] = new Procedure_Int() { // input is
                // inclusive
                int index = start;

                public void pro(final int i) {
                    if (i >= this.index) {
                        final List<int[]> l = new ArrayList<int[]>(i - this.index + 1);
                        while (this.index <= i) {
							l.add(new int[] { this.index++ });
						}
                        passThrough.pro(l);
                    }
                }
            };
        }
        final Procedure_Int_2Args[][] maxOffsetPros = new Procedure_Int_2Args[sequenceNumber][sequenceNumber];
        for (int i = 0; i < sequenceNumber; i++) {
			for (int j = i + 1; j < sequenceNumber; j++) {
                maxOffsetPros[i][j] = Librarian
                        .updateMaxOffsets(
                                i,
                                j,
                                maxOffsets,
                                maxOffsetsFiller,
                                getWeightsForLibrary_NoConsistency_NoMultipleMatchStates,
                                scratchArray, sequenceNumber);
                maxOffsetPros[j][i] = Librarian
                        .updateMaxOffsets(
                                j,
                                i,
                                maxOffsets,
                                maxOffsetsFiller,
                                getWeightsForLibrary_NoConsistency_NoMultipleMatchStates,
                                scratchArray, sequenceNumber);
            }
		}
        // run coordinate alignment
        Librarian.coordinateAlignment(alignmentGenerators,
                cutPointGenerator, finishedSeqsAdaptors, sQAA,
                sequenceNumber, aboveThresholdStack,
                this.diagonalGapBetweenRelations, maxOffsetPros);

        if (Debug.DEBUGCODE) {
            if (!matrixStack.empty()) {
				throw new IllegalStateException();
			}
            if (!aboveThresholdStack.empty()) {
				throw new IllegalStateException();
			}
            if (!diagonalLineStack.empty()) {
				throw new IllegalStateException();
			}
            if (PairsHeap.used() != 0) {
				throw new IllegalStateException(PairsHeap.used()
                        + " used, total capacity : "
                        + PairsHeap.size());
			}
            for (int j = 0; j < sequenceNumber; j++) {
                for (int k = 0; k < sequenceNumber; k++) {
                    if (j != k) {
                        final ScrollingQueue_Int sQ = maxOffsets[j][k];
                        if (sQ.firstIndex() < seqStartAndEnds[j][1] - 1) {
							throw new IllegalStateException(" " + j
                                    + " " + k + " " + sQ.firstIndex()
                                    + " " + seqStartAndEnds[j][1]
                                    + " ");
						}
                        final PairsHeap pH = sQAA[j][k];
                        if (pH.firstIndex() < seqStartAndEnds[j][1] - 1) {
							throw new IllegalStateException(" " + j
                                    + " " + k + " " + pH.firstIndex()
                                    + " " + seqStartAndEnds[j][1]
                                    + " ");
						}
                    }
                }
                if (sQAA[j][j] != null) {
					throw new IllegalStateException();
				}
            }
            for (int i = 0; i < sequenceNumber; i++) {
                for (int j = 0; j < sequenceNumber; j++) {
                    if (i != j) {
                        if (lessThanPoints[i][j][1].gen() != null) {
							throw new IllegalStateException(" 1 " + i
                                    + " " + j);
						}
                    }
                }
            }
        }

        PairsHeap.report();
        final OutputStream oS = new BufferedOutputStream(
                new FileOutputStream(this.outputFile));
        for (int i = 0; i < fastaWriters.length; i++) {
            fastaWriters[i].endAndClose();
            final InputStream iS = new BufferedInputStream(
                    new FileInputStream(tempOutFiles[i]));
            final FastaParser_Generator_Int outputFastaParser = new FastaParser_Generator_Int(
                    iS, Integer.MAX_VALUE);
            final FastaOutput_Procedure_Int fPW = new FastaOutput_Procedure_Int(
                    oS, outputFastaParser.getFastaID());
            int j;
            while ((j = outputFastaParser.gen()) != Integer.MAX_VALUE) {
				fPW.pro(j);
			}
            fPW.end();
            iS.close();
            tempOutFiles[i].delete();
        }
        for (final Iterator<Procedure_NoArgs> it = finishConfidences.iterator(); it.hasNext();) {
			it.next().pro();
		}
        if(this.outputAlignmentToGapProbabilitiesToFile) {
        	gapPW.close();
        } 
        if(this.outputTopNAlignmentPairsToFile) {
        	PrintWriter pW = PecanTools.getPrintWriter(this.alignmentPairProbabilitiesFile);  
        	pW.write("Pairs_rejected: " + numberOfPairsRejected.get(0) + "\n");
        	while(pQueue.size() > 0) {
        		PairValue pValue = pQueue.remove();
        		pW.write(pValue.seq1 + " " + (pValue.pos1 - seqStartAndEnds[pValue.seq1][0])  + " " + 
        				 pValue.seq2 + " " + (pValue.pos2 - seqStartAndEnds[pValue.seq2][0])  + " " + 
        				 pValue.weight + "\n");
        	}
        	pW.close();
        }
        
        // if (Debug.DEBUGCODE)
        Pecan.logger.info("Total time taken for alignment "
                + (System.currentTimeMillis() - startTime) / 1000.0);
    }

    public void setCommandLineArguments(final InputMunger inputMunger)
            throws Exception {
        inputMunger.noInputString("Pecan "
                + "[options and parameters] ");
        // basics
        inputMunger
                .helpString("Pecan [MODIFIER ARGUMENTS]:\n"
                        + "This is an anchored, consistency based multiple alignment \n"
                        + "program written by Benedict Paten. Mail to bjp (AT) ebi.ac.uk");
        inputMunger.addWatch(Pecan.HMM, 1, "Specify hmm file, default : "
                + this.hmm);
        // anchors
        inputMunger.addWatch(Pecan.TRANSITIVE_ANCHORS, 0,
                "Transitive anchors, (flip) default : "
                        + this.transitiveAnchors);
        inputMunger.addWatch(Pecan.TRANSITIVE_ANCHOR_DIAGONAL_BOUNDARY, 1,
                "Width in diagonals surrounding transitive anchors, default : "
                        + this.transitiveAnchorDiagonalBoundary);
        inputMunger.addWatch(Pecan.ANCHOR_DIAGONAL_WIDTH, 1,
                "Width in diagonals surrounding standard anchors, default : "
                        + this.anchorDiagonalWidth);
        // cut point stuff
        inputMunger
                .addWatch(
                        Pecan.MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT,
                        1,
                        "Size of diagonal sufficient to generate a potential cut point, default : "
                                + this.minimumSizeOfDiagonalForCutPoint);
        inputMunger
                .addWatch(
                        Pecan.MINIMUM_DIAGONAL_GAP_BETWEEN_CUT_POINT_AND_POLYGON,
                        1,
                        "The minumum number of diagonal coordinates (x+y) between a cut point and the computed polygon, default : "
                                + this.minimumDiagonalGapBetweenCutPointAndPolygon);
        inputMunger
                .addWatch(
                        Pecan.MINIMUM_GAP_BETWEEN_CUT_POINTS,
                        1,
                        "The minumum number of diagonal coordinates (x+y) between a cut point and the next one, default : "
                                + this.minimumGapBetweenCutPoints);
        inputMunger.addWatch(Pecan.CONSISTENCY_TRANSFORMATION, 0,
                "Consistency transformation, default : "
                        + this.consistencyTransformation);

        inputMunger.addWatch(Pecan.COMPUTATION_THRESHOLD, 1,
                "Threshold for weights to be used in calculations, default : "
                        + this.computationThreshold);

        inputMunger.addWatch(Pecan.USE_DIRECT_BYTE_BUFFERS, 0,
                "Use direct byte buffers (flip), default : "
                        + this.useDirectByteBuffers);

        inputMunger.addWatch(Pecan.MINIMUM_WEIGHT_HEAP_CAPACITY, 1,
                "Set a minimum capacity for the weight heap (bytes), default : "
                        + this.minimumWeightHeapCapacity);

        inputMunger.addWatch(Pecan.PRE_CLOSE_GAPS_LARGER_THAN, 1,
                "Pre close gaps larger than this length, default : "
                        + this.preCloseGapsLargerThan);

        inputMunger
                .addWatch(
                        Pecan.OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS,
                        1,
                        "Size of overhanging border (per sequence) into pre-closed gaps, default : "
                                + this.overhangingDiagonalsIntoPreClosedGaps);
        inputMunger
                .addWatch(
                        Pecan.REORDER_BY_OUTGROUP_DISTANCE,
                        1,
                        "Outgroup reordering diagonals distance (per sequence, internal parameter), default : "
                                + this.reorderByOutgroupDistance);
        inputMunger.addWatch(Pecan.USE_HMM_WITH_JUNK_STATE, 0,
                "Use HMM with junk state, default : "
                        + this.useHMMWithJunkState);

        inputMunger.addWatch(Pecan.PROVIDE_CONFIDENCE_VALUES, 0,
                "Output confidence values, default : "
                        + this.provideConfidenceValues);

        inputMunger.addWatch(Pecan.INCLUDE_NOT_ALIGNED_VALUES, 0,
                "Include not aligned probabilities in confidence value, default : "
                        + this.includeNotAlignedValues);

        inputMunger.addWatch(
                Pecan.WRITE_CHARACTER_CONFIDENCE_VALUES_IN_MFA, 0,
                "Include formated confidence values in MFA file, default : "
                        + this.writeCharacterConfidenceValuesInMFA);

        inputMunger.addWatch(
                Pecan.WRITE_OUT_SEPERATE_CONFIDENCE_VALUES_FILE, 0,
                "Write out a seperate confidence values file, default : "
                        + this.writeOutSeperateConfidenceValuesFile);

        inputMunger.addWatch(Pecan.CONFIDENCE_FILE, 1,
                "File to write out confidence values, default : "
                        + this.confidenceFile);
        
        inputMunger.addWatch(
                Pecan.THRESHOLD_WEIGHTS_ACCORDING_TO_GAP_PROBABILITY, 0,
                "Threshold weights according to the probability of a position being aligned to a  gap, like the AMAP program (doesn't work with consistency -- must turn consistency off), default : "
                        + this.thresholdWeightsAccordingToGapProbability);
        
        inputMunger.addWatch(Pecan.GAP_GAMMA, 1,
                "Multiple by which gap alignment probability is judged (increase to infer more gaps, decrease to infer less), default : "
                        + this.gapGamma);
        
        inputMunger.addWatch(Pecan.LOAD_CONSTRAINING_ALIGNMENT_FROM_FILE, 0,
                "Load constraining alignment from file (the final alignment will include all pairs in this alignment), default : "
                        + this.loadConstrainingAlignmentFromFile);
        
        inputMunger.addWatch(Pecan.CONSTRAINING_ALIGNMENT_FILE_NAME, 1,
                "Name of the constraining input alignment, default : "
                        + this.constrainingAlignmentFileName);
        
        inputMunger.addWatch(Pecan.OUTPUT_ALIGNMENT_TO_GAP_PROBABILITIES_TO_FILE, 0,
                "Output the alignment-to-gap probabilities for each residue in each sequence in the alignment, default : "
                        + this.outputAlignmentToGapProbabilitiesToFile);
        
        inputMunger.addWatch(Pecan.GAP_PROBABILITIES_FILE, 1,
                "Alignment-to-gap probabilities file, default : "
                        + this.gapProbabilitiesFile);
        
        inputMunger.addWatch(Pecan.OUTPUT_TOP_N_ALIGNMENT_PAIRS_TO_FILE, 0,
                "Output top N alignment posterior probas to file (if you unwant modified probs turn off the consistency transform), default : "
                        + this.outputTopNAlignmentPairsToFile);
        	
        inputMunger.addWatch(Pecan.MAXIMUM_NUMBER_OF_ALIGNMENT_PAIRS_TO_WRITE_TO_FILE, 1,
                "The maximum number of posterior probs to write to file, default : "
                        + this.maximumNumberOfAlignmentPairsToWriteToFile);
        
        inputMunger.addWatch(Pecan.ALIGNMENT_PAIR_PROBABILITIES_FILE, 1,
                "File in which to write alignment pair probs, default : "
                        + this.alignmentPairProbabilitiesFile);
    }

    public void parseArguments(final InputMunger inputMunger) {
        this.hmm = inputMunger.parseValue(this.hmm, Pecan.HMM);

        this.anchorDiagonalWidth = inputMunger.parseValue(
                this.anchorDiagonalWidth, Pecan.ANCHOR_DIAGONAL_WIDTH);

        this.minimumSizeOfDiagonalForCutPoint = inputMunger.parseValue(
                this.minimumSizeOfDiagonalForCutPoint,
                Pecan.MINIMUM_SIZE_OF_DIAGONAL_FOR_CUT_POINT);

        this.minimumGapBetweenCutPoints = inputMunger.parseValue(
                this.minimumGapBetweenCutPoints,
                Pecan.MINIMUM_GAP_BETWEEN_CUT_POINTS);

        this.minimumDiagonalGapBetweenCutPointAndPolygon = inputMunger
                .parseValue(
                        this.minimumDiagonalGapBetweenCutPointAndPolygon,
                        Pecan.MINIMUM_DIAGONAL_GAP_BETWEEN_CUT_POINT_AND_POLYGON);

        this.computationThreshold = (float) inputMunger.parseValue(
                this.computationThreshold, Pecan.COMPUTATION_THRESHOLD);

        if (inputMunger.watchSet(Pecan.CONSISTENCY_TRANSFORMATION)) {
            this.consistencyTransformation = !this.consistencyTransformation;
        }

        this.transitiveAnchorDiagonalBoundary = inputMunger.parseValue(
                this.transitiveAnchorDiagonalBoundary,
                Pecan.TRANSITIVE_ANCHOR_DIAGONAL_BOUNDARY);

        if (inputMunger.watchSet(Pecan.TRANSITIVE_ANCHORS)) {
			this.transitiveAnchors = !this.transitiveAnchors;
		}

        if (inputMunger.watchSet(Pecan.USE_DIRECT_BYTE_BUFFERS)) {
			this.useDirectByteBuffers = !this.useDirectByteBuffers;
		}

        this.minimumWeightHeapCapacity = inputMunger.parseValue(
                this.minimumWeightHeapCapacity,
                Pecan.MINIMUM_WEIGHT_HEAP_CAPACITY);

        this.preCloseGapsLargerThan = inputMunger.parseValue(
                this.preCloseGapsLargerThan, Pecan.PRE_CLOSE_GAPS_LARGER_THAN);

        this.overhangingDiagonalsIntoPreClosedGaps = inputMunger
                .parseValue(this.overhangingDiagonalsIntoPreClosedGaps,
                        Pecan.OVERHANGING_DIAGONALS_INTO_PRE_CLOSED_GAPS);

        this.reorderByOutgroupDistance = inputMunger.parseValue(
                this.reorderByOutgroupDistance,
                Pecan.REORDER_BY_OUTGROUP_DISTANCE);
        if (inputMunger.watchSet(Pecan.USE_HMM_WITH_JUNK_STATE)) {
			this.useHMMWithJunkState = !this.useHMMWithJunkState;
		}
        this.outputFile = inputMunger.parseValue(this.outputFile,
                PrePecan.OUTPUT_FILE);

        if (inputMunger.watchSet(Pecan.PROVIDE_CONFIDENCE_VALUES)) {
			this.provideConfidenceValues = !this.provideConfidenceValues;
		}

        if (inputMunger.watchSet(Pecan.INCLUDE_NOT_ALIGNED_VALUES)) {
			this.includeNotAlignedValues = !this.includeNotAlignedValues;
		}

        if (inputMunger
                .watchSet(Pecan.WRITE_CHARACTER_CONFIDENCE_VALUES_IN_MFA)) {
			this.writeCharacterConfidenceValuesInMFA = !this.writeCharacterConfidenceValuesInMFA;
		}

        if (inputMunger
                .watchSet(Pecan.WRITE_OUT_SEPERATE_CONFIDENCE_VALUES_FILE)) {
			this.writeOutSeperateConfidenceValuesFile = !this.writeOutSeperateConfidenceValuesFile;
		}

        this.confidenceFile = inputMunger.parseValue(this.confidenceFile,
                Pecan.CONFIDENCE_FILE);
        
        if (inputMunger
                .watchSet(Pecan.THRESHOLD_WEIGHTS_ACCORDING_TO_GAP_PROBABILITY)) {
			this.thresholdWeightsAccordingToGapProbability = !this.thresholdWeightsAccordingToGapProbability;
		}
        
        this.gapGamma = (float)inputMunger.parseValue(this.gapGamma, Pecan.GAP_GAMMA);
        
        if (inputMunger
                .watchSet(Pecan.LOAD_CONSTRAINING_ALIGNMENT_FROM_FILE)) {
			this.loadConstrainingAlignmentFromFile = !this.loadConstrainingAlignmentFromFile;
		}
        
        this.constrainingAlignmentFileName = inputMunger.parseValue(this.constrainingAlignmentFileName, Pecan.CONSTRAINING_ALIGNMENT_FILE_NAME);
        
        if (inputMunger
                .watchSet(Pecan.OUTPUT_ALIGNMENT_TO_GAP_PROBABILITIES_TO_FILE)) {
			this.outputAlignmentToGapProbabilitiesToFile = !this.outputAlignmentToGapProbabilitiesToFile;
		}
        
        this.gapProbabilitiesFile = inputMunger.parseValue(this.gapProbabilitiesFile, Pecan.GAP_PROBABILITIES_FILE);
        
        if (inputMunger
                .watchSet(Pecan.OUTPUT_TOP_N_ALIGNMENT_PAIRS_TO_FILE)) {
			this.outputTopNAlignmentPairsToFile = !this.outputTopNAlignmentPairsToFile;
		}
        
        this.maximumNumberOfAlignmentPairsToWriteToFile = (int)inputMunger.parseValue(this.maximumNumberOfAlignmentPairsToWriteToFile, Pecan.MAXIMUM_NUMBER_OF_ALIGNMENT_PAIRS_TO_WRITE_TO_FILE);
        
        this.alignmentPairProbabilitiesFile = inputMunger.parseValue(this.alignmentPairProbabilitiesFile, Pecan.ALIGNMENT_PAIR_PROBABILITIES_FILE);
    } 

    public static void main(final String[] args) throws Exception {
        final Pecan pecan = new Pecan();
        final PrePecan prePecan = new PrePecan();
        final InputMunger inputMunger = new InputMunger();
        inputMunger.addStandardWatches();
        prePecan.setCommandLineArguments(inputMunger);
        pecan.setCommandLineArguments(inputMunger);
        if (!inputMunger.parseInput(args)) {
			System.exit(0);
		}
        inputMunger.processStandardWatches();
        prePecan.parseArguments(inputMunger);
        pecan.parseArguments(inputMunger);
        logger.info("Arguments received : " + IterationTools.join(args, " "));
        final String treeFile = inputMunger.watchStrings(PrePecan.TREE)[0];
        final String[] seqFiles = inputMunger
                .watchStrings(PrePecan.SEQUENCES);
        NewickTreeParser.Node tree;
        {
            final Reader r = new StringReader(treeFile);
            tree = new NewickTreeParser(NewickTreeParser
                    .commentEater(NewickTreeParser.tokenise(r))).tree;
            r.close();
        }
        pecan.PECANScript(tree, seqFiles, prePecan);
    }

}