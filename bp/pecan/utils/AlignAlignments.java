/*
 * Created on Dec 2, 2005
 */
package bp.pecan.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import bp.common.fp.Function_2Args;
import bp.common.fp.Generator;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_NoArgs;
import bp.common.io.FastaOutput_Procedure_Int;
import bp.common.io.FastaParser_Generator_Int;
import bp.common.io.InputMunger;
import bp.common.io.MultiFastaParser_Generator;
import bp.common.io.NewickTreeParser;
import bp.common.io.NewickTreeParser.Node;
import bp.pecan.Ancestor;
import bp.pecan.Pecan;
import bp.pecan.PecanTools;

public class AlignAlignments {

    public static class AncestorToAlignment {
        final Generator alignment;

        final int seqIndex;

        final int seqNo;

        final Procedure_Int[] outputA;

        int[] nextOut;

        int nextResidue;

        public AncestorToAlignment(final int seqIndex,
                final int seqNo, final Generator alignment,
                final Procedure_Int[] outputA) {
            this.alignment = alignment;
            this.seqIndex = seqIndex;
            this.seqNo = seqNo;
            this.outputA = outputA;
            final Object[] oA = (Object[]) alignment.gen();
            this.nextResidue = ((Integer) oA[0]).intValue();
            this.nextOut = (int[]) oA[1];
        }

        // call when finished
        public Procedure_NoArgs callMeFirst() {
            return new Procedure_NoArgs() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Function#fn(java.lang.Object)
                 */
                public final void pro() {
                    while (AncestorToAlignment.this.nextResidue == '-') {
                        // insert
                        for (int k = 0; k < AncestorToAlignment.this.seqIndex; k++) {
							AncestorToAlignment.this.outputA[k].pro('-');
						}
                        for (int k = 0; k < AncestorToAlignment.this.seqNo; k++) {
							AncestorToAlignment.this.outputA[AncestorToAlignment.this.seqIndex + k].pro(AncestorToAlignment.this.nextOut[k]);
						}
                        for (int k = AncestorToAlignment.this.seqIndex + AncestorToAlignment.this.seqNo; k < AncestorToAlignment.this.outputA.length; k++) {
							AncestorToAlignment.this.outputA[k].pro('-');
						}
                        final Object[] oA = (Object[]) AncestorToAlignment.this.alignment.gen();
                        if (oA != null) {
                            AncestorToAlignment.this.nextResidue = ((Integer) oA[0])
                                    .intValue();
                            AncestorToAlignment.this.nextOut = (int[]) oA[1];
                        } else {
							AncestorToAlignment.this.nextResidue = Integer.MIN_VALUE;
						}
                    }
                }
            };
        }

        public Procedure_Int callMeSecond() {
            return new Procedure_Int() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Procedure_Int#pro(int)
                 */

                public final void pro(final int j) {
                    if (j == '-') {
                        for (int i = 0; i < AncestorToAlignment.this.seqNo; i++) {
                            AncestorToAlignment.this.outputA[AncestorToAlignment.this.seqIndex + i].pro('-');
                        }
                    } else {
                        for (int i = 0; i < AncestorToAlignment.this.nextOut.length; i++) {
                            AncestorToAlignment.this.outputA[AncestorToAlignment.this.seqIndex + i].pro(AncestorToAlignment.this.nextOut[i]);
                        }
                        final Object[] oA = (Object[]) AncestorToAlignment.this.alignment.gen();
                        if (oA != null) {
                            AncestorToAlignment.this.nextResidue = ((Integer) oA[0])
                                    .intValue();
                            AncestorToAlignment.this.nextOut = (int[]) oA[1];
                        }
                    }
                }
            };
        }
    }

    static final Logger logger = Logger.getLogger(PrePecan.class
            .getName());

    public static final String KEEP_ANCESTOR_INSERTS = "KEEP_ANCESTOR_INSERTS";
    
    public static final String TRACK_TARGET_SEQUENCE = "TRACK_TARGET_SEQUENCE";

    public String outputFile = "output.mfa";

    public String[] argumentsToWhoIsTheAncestor = new String[0];

    public boolean keepAncestorInserts = false;
    
    public int targetSequence = Integer.MIN_VALUE;

    public void runAlignAlignments(final NewickTreeParser.Node tree,
            final String[] seqFiles, final WhoIsTheAncestor wITA,
            final Function_2Args aligner) throws Exception {
        // seq gen
        final int sequenceNumber = seqFiles.length;
        final String[][] fastaIDs = new String[sequenceNumber][];

        final List<String> mSAFiles = new LinkedList<String>();
        final List<String> ancestorFiles = new LinkedList<String>();
        {
            final List<Node> subTrees = new LinkedList<Node>();
            final Generator treeGen = PecanTools.leafGenerator(tree);
            for (int i = 0; i < sequenceNumber; i++) {
                if (seqFiles[i].endsWith(".mfa")) {
                    final MultiFastaParser_Generator gen2 = new MultiFastaParser_Generator(
                            seqFiles[i]);
                    final String[] subTreeIDs = gen2.getFastaIDs();
                    fastaIDs[i] = subTreeIDs;
                    final NewickTreeParser.Node n = (NewickTreeParser.Node) treeGen
                            .gen();
                    final NewickTreeParser.Node m;
                    if (subTreeIDs.length > 1) {
                        for (int k = 2; k < subTreeIDs.length; k++) {
							treeGen.gen(); // run out middle positions
						}
                        m = PecanTools
                                .getCommonAncestor(
                                        n,
                                        (NewickTreeParser.Node) treeGen
                                                .gen());
                    } else {
						m = n;
					}
                    subTrees.add(m);

                    final File f = File.createTempFile("temp_profile"
                            + seqFiles[i].substring(0, seqFiles[i]
                                    .length() - 4), ".fa");
                    f.deleteOnExit();
                    final OutputStream oS = new BufferedOutputStream(
                            new FileOutputStream(f));
                    final FastaOutput_Procedure_Int fOPI = new FastaOutput_Procedure_Int(
                            oS, "temp_profile");
                    final File f2 = File.createTempFile("temp_profile"
                            + seqFiles[i].substring(0, seqFiles[i]
                                    .length() - 4), ".fa");
                    f2.deleteOnExit();
                    final OutputStream oS2 = new BufferedOutputStream(
                            new FileOutputStream(f2));
                    final FastaOutput_Procedure_Int fOPI2 = new FastaOutput_Procedure_Int(
                            oS2, "temp_profile");
                    final Generator ancestorGen = wITA.whoIsTheAncestor(m,
                            gen2, subTreeIDs.length);
                    Function_2Args convertToMostProbableDNAResidue_WithRepeats = Ancestor
                            .convertToMostProbableDNAResidue_WithRepeats(
                                    this.keepAncestorInserts);
                    final MultiFastaParser_Generator gen3 = new MultiFastaParser_Generator(
                            seqFiles[i]);
                    if(this.targetSequence != Integer.MIN_VALUE) {
                        final Function_2Args fn = convertToMostProbableDNAResidue_WithRepeats;
                        convertToMostProbableDNAResidue_WithRepeats = new Function_2Args() {
                            public Object fn(final Object o, final Object o2) {
                                final Integer i = (Integer)fn.fn(o, o2);
                                final int[] iA = (int[])gen3.gen();
                                return iA[AlignAlignments.this.targetSequence] == '-' ? new Integer('-') : i;   
                            }
                        };
                    }
                    {
                        float[][] fAA;
                        while ((fAA = (float[][]) ancestorGen.gen()) != null) {
                            final int j = ((Integer) convertToMostProbableDNAResidue_WithRepeats
                                    .fn(fAA[0], fAA[1])).intValue();
                            if (j != '-') {
								fOPI.pro(j);
							}
                            fOPI2.pro(j);
                        }
                    }
                    mSAFiles.add(seqFiles[i]);
                    seqFiles[i] = f.toString();
                    ancestorFiles.add(f2.toString());
                    fOPI.endAndClose();
                    fOPI2.endAndClose();
                    oS.close();
                    oS2.close();
                    gen2.close();
                    gen3.close();
                } else {
                    treeGen.gen();
                    final InputStream iS = new BufferedInputStream(
                            new FileInputStream(seqFiles[i]));
                    final FastaParser_Generator_Int gen = new FastaParser_Generator_Int(
                            iS, Integer.MAX_VALUE);
                    fastaIDs[i] = new String[] { gen.getFastaID() };
                    iS.close();
                }
            }
            // remove msa subtrees from rest of tree
            for (final Iterator<Node> it = subTrees.iterator(); it.hasNext();) {
                final NewickTreeParser.Node n = it
                        .next();
                final NewickTreeParser.Node parent = n.getParent();
                final NewickTreeParser.Node m = new NewickTreeParser.Node();
                m.edgeLength = PecanTools
                        .averagePathLengthToChildren(n);
                parent.setNode(parent.getNodes().indexOf(n), m);
            }
            AlignAlignments.logger
                    .info("Parsed tree after removal of already aligned sequences : "
                            + tree);
        }

        // temp fasta output files
        File[] tempOutFiles;
        FastaOutput_Procedure_Int[] fastaWriters;
        final Procedure_Int[] outputFns;
        final Procedure_NoArgs[] insertFns;
        {
            int totalSequenceNumber = 0;
            for (final String[] element : fastaIDs) {
				totalSequenceNumber += element.length;
			}
            fastaWriters = new FastaOutput_Procedure_Int[totalSequenceNumber];
            tempOutFiles = new File[totalSequenceNumber];
            {
                int k = 0;
                for (final String[] sA : fastaIDs) {
                    for (final String element0 : sA) {
                        final File f = File.createTempFile("tmp_output_",
                                ".fa");
                        AlignAlignments.logger.info(" Temp output file for : "
                                + element0 + " is : " + f.toString());
                        tempOutFiles[k] = f;
                        fastaWriters[k++] = new FastaOutput_Procedure_Int(
                                new BufferedOutputStream(
                                        new FileOutputStream(f)),
                                element0);
                    }
                }
            }
            // build chain of output functions
            {
                outputFns = new Procedure_Int[sequenceNumber];
                final List<Procedure_NoArgs> l = new LinkedList<Procedure_NoArgs>();
                final Iterator<String> mSAFilesIt = mSAFiles.iterator();
                final Iterator<String> ancestorIt = ancestorFiles.iterator();
                int j = 0;
                for (int i = 0; i < fastaIDs.length; i++) {
                    final int k = fastaIDs[i].length;
                    if (k > 1) {
                        // is msa
                        final MultiFastaParser_Generator mFPG = new MultiFastaParser_Generator(
                                mSAFilesIt.next());
                        final FastaParser_Generator_Int fPGI = new FastaParser_Generator_Int(
                                new BufferedInputStream(
                                        new FileInputStream(
                                                ancestorIt.next())),
                                Integer.MAX_VALUE);
                        final Generator gen = new Generator() {
                            /*
                             * (non-Javadoc)
                             * 
                             * @see bp.common.fp.Generator#gen()
                             */
                            public Object gen() {
                                int[] iA = (int[]) mFPG.gen();
                                if (iA != null) {
                                    return new Object[] {
                                            new Integer(fPGI.gen()),
                                            iA };
                                }
                                return null;
                            }
                        };
                        final AlignAlignments.AncestorToAlignment aA = new AlignAlignments.AncestorToAlignment(
                                j, k, gen, fastaWriters);
                        l.add(aA.callMeFirst());
                        outputFns[i] = aA.callMeSecond();
                    } else {
                        // is single sequence
                        final FastaParser_Generator_Int fPGI = new FastaParser_Generator_Int(
                                new BufferedInputStream(
                                        new FileInputStream(
                                                seqFiles[i])),
                                Integer.MAX_VALUE);
                        final Procedure_Int outputFn = fastaWriters[j];
                        outputFns[i] = new Procedure_Int() {
                            /*
                             * (non-Javadoc)
                             * 
                             * @see bp.common.fp.Procedure_Int#pro(int)
                             */
                            public void pro(final int i) {
                                outputFn.pro(i == '-' ? '-' : fPGI
                                        .gen());
                            }
                        };
                    }
                    j += k;
                }
                insertFns = new Procedure_NoArgs[l.size()];
                j = 0;
                for (final Iterator<Procedure_NoArgs> it = l.iterator(); it.hasNext();) {
                    insertFns[j++] = it.next();
                }
            }
        }

        {
            final String initialAlignment = (String) aligner.fn(tree,
                    seqFiles);
            final MultiFastaParser_Generator alignGen = new MultiFastaParser_Generator(
                    initialAlignment);
            int[] iA;
            while ((iA = (int[]) alignGen.gen()) != null) {
                for (final Procedure_NoArgs element : insertFns) {
					element.pro();
				}
                for (int j = 0; j < iA.length; j++) {
                    outputFns[j].pro(iA[j]);
                }
            }
            for (final Procedure_NoArgs element : insertFns) {
				element.pro(); // ensure any trailing gaps are inserted
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

    public void setCommandLineArguments(final InputMunger inputMunger) {
        inputMunger.noInputString("Reconstitute an ancestor");
        inputMunger
                .addWatch(
                        AlignAlignments.KEEP_ANCESTOR_INSERTS,
                        0,
                        "Keep ancestor inserts in reconstituted sequence, (flip parameter), default : "
                                + this.keepAncestorInserts);
        inputMunger
        .addWatch(
                AlignAlignments.TRACK_TARGET_SEQUENCE,
                1,
                "Track target sequence, default : "
                        + this.targetSequence);
    }

    public void parseArguments(final InputMunger inputMunger) {
        this.outputFile = inputMunger.parseValue(this.outputFile,
                PrePecan.OUTPUT_FILE);
        this.targetSequence = inputMunger.parseValue(this.targetSequence, AlignAlignments.TRACK_TARGET_SEQUENCE);
        if(inputMunger.watchSet(AlignAlignments.KEEP_ANCESTOR_INSERTS)) {
			this.keepAncestorInserts = !this.keepAncestorInserts;
		}
    }

    static Function_2Args getPecanAligner(final Pecan pecan,
            final PrePecan prePecan) throws Exception {
        return new Function_2Args() {
            public Object fn(final Object o, final Object o2) {
                try {
                    pecan.PECANScript((NewickTreeParser.Node) o,
                            (String[]) o2, prePecan);
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                }
                return pecan.outputFile;
            }
        };
    }

    public static void main(final String[] args) throws Exception {
        final PrePecan prePecan = new PrePecan();
        final Pecan pecan = new Pecan();
        final AlignAlignments alignAlignments = new AlignAlignments();
        final WhoIsTheAncestor whoIsTheAncestor = new WhoIsTheAncestor();
        final InputMunger inputMunger = new InputMunger();
        inputMunger.addStandardWatches();
        prePecan.setCommandLineArguments(inputMunger);
        pecan.setCommandLineArguments(inputMunger);
        whoIsTheAncestor.setCommandLineArguments(inputMunger);
        alignAlignments.setCommandLineArguments(inputMunger);
        if (!inputMunger.parseInput(args)) {
			System.exit(0);
		}
        inputMunger.processStandardWatches();
        prePecan.parseArguments(inputMunger);
        pecan.parseArguments(inputMunger);
        alignAlignments.parseArguments(inputMunger);
        whoIsTheAncestor.parseArguments(inputMunger);
        NewickTreeParser.Node tree;
        {
            final Reader r = new StringReader(inputMunger
                    .watchStrings(PrePecan.TREE)[0]);
            tree = new NewickTreeParser(NewickTreeParser
                    .commentEater(NewickTreeParser.tokenise(r))).tree;
            r.close();
        }
        final Function_2Args pecanAligner = AlignAlignments.getPecanAligner(pecan, prePecan);
        alignAlignments.runAlignAlignments(tree, inputMunger
                .watchStrings(PrePecan.SEQUENCES), whoIsTheAncestor,
                pecanAligner);
    }
}
