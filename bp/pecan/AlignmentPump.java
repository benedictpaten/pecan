/*
 * Created on Mar 24, 2005
 */
package bp.pecan;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Function_Int_3Args;
import bp.common.fp.Generator;
import bp.common.fp.Predicate_Double_2Args;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_Int_3Args;
import bp.common.fp.Procedures;
import bp.common.io.NewickTreeParser;

/**
 * @author benedictpaten
 */
public class AlignmentPump {

    static final int[] getStartAndEnds(final int nodeNumber,// NewickTreeParser.Node
            // node,
            final int[][] seqStartAndEnds, final int index) {
        // int i = getNodeNumber(node, NewickTreeParser.Node
        // .leafPredicate());
        final int[] iA = new int[nodeNumber * 2];
        for (int j = 0; j < nodeNumber; j++) {
            final int[] iA2 = seqStartAndEnds[index + j];
            iA[j * 2] = iA2[0];
            iA[j * 2 + 1] = iA2[1];
        } 
        return iA;
    } 

    static final int[] getRange(final int size, final int index) {
        final int[] iA = new int[size];
        for (int i = 0; i < size; i++) {
			iA[i] = i + index;
		}
        return iA;
    }

    static final int getNodeNumber(final NewickTreeParser.Node node) {
        if (node.getNodes().size() == 0) {
			return 1;
		}
        int i = 0;
        for (final Iterator<Object> it = node.getNodes().iterator(); it.hasNext();) {
			i += AlignmentPump.getNodeNumber((NewickTreeParser.Node) it.next());
		}
        return i;
    }

    /**
     * Function for creating a progressive multi-sequence alignment pump. The
     * ordering of the alignment corresponds to the ordering of the nodes in the
     * tree by a depth first traversal.
     * 
     * @param output
     *            function to pass chunks of output
     * @param getWeightsForResidue
     *            library function
     * @param seqIDsToStartAndEndsMap
     *            mapping between tree leaf ids and
     * @param inputProcedures
     *            array of procedures to fill with inputs
     * @param index
     *            index into inputProcedures from which to start filling
     *            (inclusive)
     * @param tree
     * @param defaultArrayLength
     * @param adder
     *            add together weights
     * @param greaterThan
     *            first weight greater than second
     * @param defaultNodeValue
     *            default score for first node
     * @return
     */
    public static final int getAlignmentPumps(Procedure output,
            final Librarian.WeightsGetter getWeightsForResidue,
            final Function_Int_3Args getMaxResidue,
            final int[][] seqStartAndEnds, final Procedure[] inputProcedures,
            int index, NewickTreeParser.Node tree,
            final int defaultArrayLength, final Function_Int_2Args adder,
            final DripAligner.Add dripAdder,
            final Predicate_Double_2Args greaterThan,
            final double defaultNodeValue,
            final Procedure_Int_3Args releaseWeights, final int[] scratchA) {
        if (tree.getNodes().size() == 0) {
			inputProcedures[index++] = output;
		} else {
            final List<Object> l = new LinkedList<Object>();
            for (final Iterator<Object> it = tree.getNodes().iterator(); it
                    .hasNext();) {
				l.add(it.next());
			}
            tree = (NewickTreeParser.Node) l.remove(0);
            int nodeNumber = AlignmentPump.getNodeNumber(tree);
            int[] startAndEnds1 = AlignmentPump.getStartAndEnds(nodeNumber,
                    seqStartAndEnds, index);
            int[] seqIndices1 = AlignmentPump.getRange(nodeNumber, index);
            while (l.size() > 0) {
                nodeNumber = 0;
                for (final Iterator<Object> it = l.iterator(); it.hasNext();) {
					nodeNumber += AlignmentPump.getNodeNumber((NewickTreeParser.Node) it
                            .next());
				}
                final int i = index + seqIndices1.length;
                final int[] startAndEnds2 = AlignmentPump.getStartAndEnds(nodeNumber,
                        seqStartAndEnds, i);
                final int[] seqIndices2 = AlignmentPump.getRange(nodeNumber, i);
                final Procedure[] inputs = AlignmentPump.getAlignmentPump(seqIndices1,
                        seqIndices2, startAndEnds1, startAndEnds2,
                        output, getWeightsForResidue, getMaxResidue,
                        defaultArrayLength, adder, dripAdder,
                        greaterThan, defaultNodeValue,
                        releaseWeights, scratchA);
                index = AlignmentPump.getAlignmentPumps(inputs[0],
                        getWeightsForResidue, getMaxResidue,
                        seqStartAndEnds, inputProcedures, index,
                        tree, defaultArrayLength, adder, dripAdder,
                        greaterThan, defaultNodeValue,
                        releaseWeights, scratchA);
                output = inputs[1];
                tree = (NewickTreeParser.Node) l.remove(0);
                nodeNumber = AlignmentPump.getNodeNumber(tree);
                startAndEnds1 = AlignmentPump.getStartAndEnds(nodeNumber,// tree,
                        seqStartAndEnds, index);
                seqIndices1 = AlignmentPump.getRange(nodeNumber, index);
            }
            index = AlignmentPump.getAlignmentPumps(output, getWeightsForResidue,
                    getMaxResidue, seqStartAndEnds, inputProcedures,
                    index, tree, defaultArrayLength, adder,
                    dripAdder, greaterThan, defaultNodeValue,
                    releaseWeights, scratchA);
        }
        return index;
    }

    /**
     * Method creates an alignment pump for two given dimensions.
     * 
     * @param seqsStartsAndEnds1
     *            the coordinates of the start (inclusive) and end points
     *            (exclusive) of the subsequences in the first dimension,
     * @param seqsStartsAndEnds2
     *            the coordinates of the start (inclusive) and end points
     *            (exclusive) for the subsequences in the second dimension
     * @param output
     *            the procedure to which fragments of alignment are passed
     * @param getWeightsForResidue
     *            function for retrieveing weights for residue positions
     * @param defaultArrayLength
     * @param adder
     *            add together weights
     * @param greaterThan
     *            first weight greater than second
     * @param defaultNodeValue
     *            default score for first node
     * @return two procedures for the first and second sequence inputs
     *         respectively, they should be called with chunks of completed
     *         input sequence
     */
    static final Procedure[] getAlignmentPump(final int[] seqs1,
            final int[] seqs2, final int[] seqsStartsAndEnds1,
            final int[] seqsStartsAndEnds2, final Procedure output,
            final Librarian.WeightsGetter getWeightsForResidue,
            final Function_Int_3Args getMaxResidue, final int defaultArrayLength,
            final Function_Int_2Args adder, final DripAligner.Add dripAdder,
            final Predicate_Double_2Args greaterThan,
            final double defaultNodeValue,
            final Procedure_Int_3Args releaseWeights, final int[] scratchA) {
        final WeightTranslator weightTranslator = new WeightTranslator(
                getWeightsForResidue, getMaxResidue, seqs1, seqs2,
                seqsStartsAndEnds1, seqsStartsAndEnds2,
                defaultArrayLength, adder, AlignmentStitcher
                        .convertInput(), releaseWeights, scratchA);
        final Generator dripAligner = new DripAligner(weightTranslator
                .getOutputX(), weightTranslator.getOutputY(),
                dripAdder, greaterThan, defaultNodeValue);
        final AlignmentStitcher alignmentStitcher = new AlignmentStitcher(
                dripAligner, seqsStartsAndEnds1.length / 2,
                seqsStartsAndEnds2.length / 2);
        final Procedure callChain = new Procedure() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure#pro(java.lang.Object)
             */
            public final void pro(Object o) {
                o = alignmentStitcher.gen();
                if (o != null) {
					output.pro(o);
				}
            }
        };
        return new Procedure[] {
                Procedures.runProcedures(new Procedure[] {
                        alignmentStitcher
                                .getInputX(),
                        weightTranslator.getInputX(), callChain }),
                Procedures.runProcedures(new Procedure[] {
                        alignmentStitcher
                                .getInputY(),
                        weightTranslator.getInputY(), callChain }) };
    }

}