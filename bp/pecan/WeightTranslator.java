/*
 * Created on Mar 24, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bp.common.ds.IntCrapHash;
import bp.common.ds.ScrollingQueue_Int;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Function;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Function_Int_3Args;
import bp.common.fp.Generator;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_2Args;
import bp.common.fp.Procedure_Int_3Args;
import bp.common.io.Debug;

/**
 * Progressively iterates through the fragments of two passed input sequences
 * and generates the corresponding weight arrays between the sequences when the
 * corresponding parts of the two sequences are both available.
 * 
 * @author benedictpaten
 */
class WeightTranslator {

    static class WT implements Procedure, Generator {
    
        /**
         * Adds the residues in the given dimension fragment to the residueMap,
         * so that it can be established in unitary time where abouts a residue
         * in a subdimension maps in the alignment.
         * 
         * @param residueMap
         *            map containing mappings from residue values to coordinates
         *            in the alignment
         * @param d
         *            the dimension fragment to add to the map
         */
        static final void addToResidueMap(
                final ScrollingQueue_Int[] residueMap, final Iterator it,
                final int offset) {
            for (int i = 0; it.hasNext(); i++) {
                final int k = i + offset;
                final int[] iA = (int[]) it.next();
                for (int j = 0; j < iA.length; j++) {
                    if (iA[j] != Integer.MAX_VALUE) {
                        if (Debug.DEBUGCODE
                                && (iA[j] != residueMap[j].lastIndex())) {
							throw new IllegalStateException(
                                    "Residue being added is out of sync with residue map");
						}
                        residueMap[j].add(k);
                    }
                }
            }
        }
    
        /**
         * Converts a given map into a weight array with a format { { COORDINATE
         * WEIGHT }xN }. The weight list is not sorted.
         * 
         * @param weightMap
         *            mpa to convert
         * @return
         */
        static final int[] convertToWeightArray(final IntCrapHash weightMap) {
            final int[] weights = new int[weightMap.size() * 2];
            weightMap.getEntries(weights);
            return weights;
        }
    
        /**
         * Retrieves the residues with the largest offset into the other
         * sequences for the residues in the given column.
         * 
         * @param column
         * @param startAndEndPoints
         * @param getWeights
         */
        static final void getMaxResidues(final int[] column,
                final int[] maxResidues, final int[] seqs, final int[] oppositeSeqs,
                final Function_Int_3Args getMaxResidue) {
            for (int j = 0; j < column.length; j++) {
                if (column[j] != Integer.MAX_VALUE) {
                    final int k = column[j];
                    final int l = seqs[j];
                    for (int m = 0; m < oppositeSeqs.length; m++) {
                        final int n = getMaxResidue.fn(k, l,
                                oppositeSeqs[m]);
                        if ((n != Integer.MAX_VALUE) && (n > maxResidues[m])) {
							maxResidues[m] = n;
						}
                    }
                }
            }
        }
    
        /**
         * Collates the weights of interest between on column of a dimension and
         * another dimension.
         * 
         * @param column
         *            of dimension
         * @param weightMap
         *            map in which to place weights between the column and the
         *            other dimension
         * @param startAndEndPoints
         *            the start and end points of the sequences in the other
         *            dimension
         * @param residueMap
         *            maps residues in opposite subdimension to coordinates in
         *            the opposite dimension
         * @param getWeights
         *            function for getting weights associated with a residue
         */
        static final void doLine(final int[] column, final IntCrapHash weightMap,
                final int[] seqs, final int[] oppositeSeqs,
                final ScrollingQueue_Int[] residueMap,
                final Librarian.WeightsGetter getWeights, final int[] scratchA,
                final Function_Int_2Args adder,
                final Procedure_Int_3Args releaseWeights) {
            for (int i = 0; i < column.length; i++) {
                if (column[i] != Integer.MAX_VALUE) {
                    final int j = column[i];
                    final int k = seqs[i];
                    for (int l = 0; l < oppositeSeqs.length; l++) {
                        final int m = oppositeSeqs[l];
                        final int n = getWeights.fn(j, k, m, scratchA, 0);
                        releaseWeights.pro(j, k, m);
                        WT.doLine(scratchA, n, weightMap, residueMap[l],
                                adder);
                    }
                }
            }
        }
    
        static final void doLine(final int[] weights, final int weightsLength,
                final IntCrapHash weightMap, final ScrollingQueue_Int residueMap,
                final Function_Int_2Args adder) {
            for (int i = 0; (i < weightsLength)
                    && (residueMap.firstIndex() <= weights[i]); i += 2) {
                WT.mI.i = residueMap.get(weights[i]);
                weightMap.put(residueMap.get(weights[i]), weights[i + 1], adder);
            }
        }
    
        /**
         * Updates the start and end points array and residue map to remove the
         * first position from the sub dimension alignment.
         * 
         * @param residueMap
         * @param startAndEndPoints
         * @param d
         */
        static final void removeFirstPositions(
                final ScrollingQueue_Int[] residueMap, final int[] iA) {
            for (int i = 0; i < iA.length; i++) {
                if (iA[i] != Integer.MAX_VALUE) {
                    if (Debug.DEBUGCODE
                            && (iA[i] != residueMap[i].firstIndex())) {
						throw new IllegalStateException(
                                "Residue map out of sync with alignment");
					}
                    residueMap[i].removeFirst();
                }
            }
        }
    
        static final MutableInteger mI = new MutableInteger();
    
        final List d;
    
        final Librarian.WeightsGetter getWeightsForResidue;
    
        final Function_Int_3Args getMaxResidue;
    
        final Procedure_Int_3Args releaseWeights;
    
        final Function_Int_2Args adder;
    
        final ScrollingQueue_Int[] residueMap;
    
        final ScrollingQueue_Int[] oppositeResidueMap;
    
        final int[] seqs;
    
        final int[] oppositeSeqs;
    
        final int[] maximumResidues;
    
        final int[] column;
    
        final int[] scratchColumn;
    
        int maximumResiduesIndex, offsetIndex = 0;
    
        final IntCrapHash weightMap = new IntCrapHash(200);
    
        final Procedure_2Args convertInput;
    
        final int[] scratchA;
    
        /**
         * 
         */
        WT(final ScrollingQueue_Int[] residueMap,
                final ScrollingQueue_Int[] oppositeResidueMap, final int[] seqs,
                final int[] oppositeSeqs,
                final Librarian.WeightsGetter getWeightsForResidue,
                final Function_Int_3Args getMaxResidue,
                final Function_Int_2Args adder,
                final Procedure_2Args convertInput,
                final Procedure_Int_3Args releaseWeights, final int[] scratchA) {
            this.d = new LinkedList();
            this.residueMap = residueMap;
            this.seqs = seqs;
            this.oppositeSeqs = oppositeSeqs;
            this.oppositeResidueMap = oppositeResidueMap;
            this.maximumResidues = new int[oppositeResidueMap.length];
            Arrays.fill(this.maximumResidues, Integer.MIN_VALUE);
            this.maximumResiduesIndex = this.maximumResidues.length;
            this.getWeightsForResidue = getWeightsForResidue;
            this.adder = adder;
            this.convertInput = convertInput;
            this.column = new int[residueMap.length];
            this.scratchColumn = new int[residueMap.length];
            this.releaseWeights = releaseWeights;
            this.scratchA = scratchA;
            this.getMaxResidue = getMaxResidue;
        }
    
        /*
         * (non-Javadoc)
         * 
         * @see bp.common.fp.Generator#gen()
         */
        public Object gen() {
            if (this.maximumResiduesIndex == this.maximumResidues.length) {
                // if no remaining column return
                // else get the set of maximum point for the other sequence
                if (this.d.size() == 0) {
					return null;
				}
                this.maximumResiduesIndex = 0;
                this.convertInput.pro(this.d.get(0), this.column);
                WT.getMaxResidues(this.column, this.maximumResidues, this.seqs,
                        this.oppositeSeqs, this.getMaxResidue);
            }
            do {
                if (this.oppositeResidueMap[this.maximumResiduesIndex]
                        .lastIndex() > this.maximumResidues[this.maximumResiduesIndex]) {
					this.maximumResiduesIndex++;
				} else {
					return null;
				}
            } while (this.maximumResiduesIndex < this.maximumResidues.length);
            // do line
            WT.doLine(this.column, this.weightMap, this.seqs, this.oppositeSeqs,
                    this.oppositeResidueMap, this.getWeightsForResidue,
                    this.scratchA, this.adder, this.releaseWeights);
            // translate map into weight array
            final int[] wA = WT.convertToWeightArray(this.weightMap);
            // remove the weights from the residue map and update the start
            // positions array
            // and remove the first residue from the array
            this.convertInput.pro(this.d.remove(0), this.scratchColumn);
            WT.removeFirstPositions(this.residueMap, this.scratchColumn);
            // clear the weightMap for the next compilation of weights
            this.weightMap.clear();
            return wA;
        }
    
        /*
         * (non-Javadoc)
         * 
         * @see bp.common.fp.Procedure#pro(java.lang.Object)
         */
        public void pro(final Object o) {
            final int size = this.d.size();
            IterationTools.append(((List) o).iterator(), this.d);
            WT.addToResidueMap(this.residueMap, Iterators.map(this.d
                    .listIterator(size), new Function() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Function#fn(java.lang.Object)
                 */
                public Object fn(final Object o) {
                    WT.this.convertInput.pro(o, WT.this.scratchColumn);
                    return WT.this.scratchColumn;
                }
            }), this.offsetIndex);
            this.offsetIndex += this.d.size() - size;
        }
    }

    WT rowWeightTranslator, columnWeightTranslator;

    /**
     * 
     * @param getWeightsForResidue
     *            function to retrieve the weights for a sequence residue
     * @param seqStartsAndEnds1
     *            the start and end points of the residues (inclusive and
     *            exclusively respectively) in the first sequence
     * @param seqStartsAndEnds2
     *            the start and end points of the residues (inclusive and
     *            exclusively respectively) in the second sequence
     * @param defaultArrayLength
     *            the likely size of the open window
     * @param adder
     *            adds together weight values
     */
    public WeightTranslator(
            final Librarian.WeightsGetter getWeightsForResidue,
            final Function_Int_3Args getMaxResidue, final int[] seqs1,
            final int[] seqs2, final int[] seqStartsAndEnds1,
            final int[] seqStartsAndEnds2, final int defaultArrayLength,
            final Function_Int_2Args adder, final Procedure_2Args convertInput,
            final Procedure_Int_3Args releaseWeights, final int[] scratchA) {
        ScrollingQueue_Int[] rowResidueMap, columnResidueMap;
        rowResidueMap = new ScrollingQueue_Int[seqs1.length];
        for (int i = 0; i < rowResidueMap.length; i++) {
			rowResidueMap[i] = new ScrollingQueue_Int(
                    defaultArrayLength, seqStartsAndEnds1[i * 2], true);
		}
        columnResidueMap = new ScrollingQueue_Int[seqs2.length];
        for (int i = 0; i < columnResidueMap.length; i++) {
			columnResidueMap[i] = new ScrollingQueue_Int(
                    defaultArrayLength, seqStartsAndEnds2[i * 2], true);
		}
        this.rowWeightTranslator = new WT(rowResidueMap, columnResidueMap,
                seqs1, seqs2, getWeightsForResidue, getMaxResidue,
                adder, convertInput, releaseWeights, scratchA);
        this.columnWeightTranslator = new WT(columnResidueMap,
                rowResidueMap, seqs2, seqs1, getWeightsForResidue,
                getMaxResidue, adder, convertInput, releaseWeights,
                scratchA);
    }

    /**
     * 
     * @return the procedure for inputing the first input sequence
     */
    public Procedure getInputX() {
        return this.rowWeightTranslator;
    }

    /**
     * 
     * @return the procedure for inputing the second input sequence
     */
    public Procedure getInputY() {
        return this.columnWeightTranslator;
    }

    /**
     * 
     * @return the generator for retrieving the weights for the first sequence
     */
    public Generator getOutputX() {
        return this.rowWeightTranslator;
    }

    /**
     * 
     * @return the generator for retrieving the weights for the second sequence
     */
    public Generator getOutputY() {
        return this.columnWeightTranslator;
    }
}