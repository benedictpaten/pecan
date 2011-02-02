/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jun 10, 2005
 */
package bp.pecan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.PriorityQueue;

import bp.common.ds.LockedObject;
import bp.common.ds.ScrollingQueue_Int;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Function;
import bp.common.fp.Function_2Args;
import bp.common.fp.Function_Index_2Args;
import bp.common.fp.Function_Int_4Args;
import bp.common.fp.Function_Int_5Args;
import bp.common.fp.Generator;
import bp.common.fp.Procedure;
import bp.common.fp.Procedure_2Args;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_NoArgs;
import bp.common.io.Debug;
import bp.common.io.FastaOutput_Procedure_Int;
import bp.common.io.FastaParser_Generator_Int;
import bp.common.io.NewickTreeParser;
import bp.common.io.NewickTreeParser.Node;


/**
 * Functions for the main Pecan script
 * 
 * @author benedictpaten
 */
public final class PecanTools {

    static final Logger logger = Logger.getLogger(Pecan.class
            .getName());

    public static Generator leafGenerator(
            final NewickTreeParser.Node n) {
        return new Generator() {
            Stack<Object> s = new Stack<Object>();
            {
                this.s.push(n);
                this.s.push(n.getNodes().iterator());
            }

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public final Object gen() {
                if (!this.s.empty()) {
                    final Iterator it = (Iterator) this.s.pop();
                    if (it.hasNext()) {
                        this.s.push(it);
                        final NewickTreeParser.Node m = (NewickTreeParser.Node) it
                                .next();
                        this.s.push(m);
                        this.s.push(m.getNodes().iterator());
                    } else {
                        final NewickTreeParser.Node m = (NewickTreeParser.Node) this.s
                                .pop();
                        if (m.getNodes().size() == 0) {
							return m;
						}
                    }
                    return this.gen();
                }
                return null;
            }
        };
    }

    public static int[][] getPairOrdering(final double[][] dAA) {
        final int[][] iAA = new int[(dAA.length * dAA.length - dAA.length) / 2][];
        final SortedMap<Double, List<int[]>> sM = new TreeMap<Double, List<int[]>>();
        for (int i = 0; i < dAA.length; i++) {
            for (int j = i + 1; j < dAA.length; j++) {
                final Double d = new Double(dAA[i][j]);
                final int[] iA = new int[] { i, j };
                if (sM.containsKey(d)) {
                    final List<int[]> l = sM.get(d);
                    l.add(iA);
                } else {
                    final List<int[]> l = new LinkedList<int[]>();
                    l.add(iA);
                    sM.put(d, l);
                }
            }
        }
        int i = 0;
        while (sM.size() != 0) {
            final List l = sM.remove(sM.firstKey());
            for (final Iterator it = l.iterator(); it.hasNext();) {
                iAA[i++] = (int[]) it.next();
            }
        }
        return iAA;
    }

    /**
     * Convenience function for
     * {@link PecanTools#getOutgroups(LockedObject, double[][], int)}.
     * 
     * @param scratchArray
     * @param seqDistances
     * @param sequenceNumber
     * @return
     */
    public static int[][][] getOutgroups(final LockedObject scratchArray,
            final double[][] seqDistances, final int sequenceNumber) {
        final int[][][] iAAA = new int[sequenceNumber][sequenceNumber][];
        for (int i = 0; i < sequenceNumber; i++) {
			for (int j = i + 1; j < sequenceNumber; j++) {
                iAAA[i][j] = PecanTools.getOutgroups(scratchArray,
                        seqDistances[i], seqDistances[j],
                        sequenceNumber, i, j);
                iAAA[j][i] = iAAA[i][j];
            }
		}
        return iAAA;
    }

    /**
     * Expects self distances to min-values.
     * 
     * @param scratchArray
     * @param seqDistances
     * @param i
     * @param j
     * @return
     */
    public static int[] getOutgroups(final LockedObject scratchArray,
            final double[] dA1, final double[] dA2, final int sequenceNumber, final int i,
            final int j) {
        int k = 0;
        final double ijD = dA1[j];
        final int[] sA = (int[]) scratchArray.get();
        for (int m = 0; m < sequenceNumber; m++) {
            if ((m != i) && (m != j)) {
                if (((dA1[m] < ijD) || ((dA1[m] == ijD) && (m < j)))
                        && ((dA2[m] < ijD) || ((dA2[m] == ijD) && (m < i)))) {
                    sA[k++] = m;
                }
            }
        }
        final int[] iA2 = new int[k];
        System.arraycopy(sA, 0, iA2, 0, k);
        scratchArray.release();
        return iA2;
    }

    static class CutUpDiagonals implements Generator, Procedure_Int {

        int x, y = Integer.MAX_VALUE, yMax = Integer.MIN_VALUE;

        final Generator gen;

        int cutDiagonal = Integer.MIN_VALUE;

        CutUpDiagonals(final Generator gen) {
            this.gen = gen;
        }

        public final void setCutUpPoint(final int i) {
            this.cutDiagonal = i;
        }

        public final void pro(final int i) {
            this.setCutUpPoint(i);
        }

        /*
         * (non-Javadoc)
         * 
         * @see bp.common.fp.Generator#gen()
         */
        public final Object gen() {
            if (this.y <= this.yMax) {
                if (this.y + this.x > this.cutDiagonal) {
					return new PolygonFiller.Node(this.x++, this.y, this.y++, 1);
				}

                // if stretches over cutpoint
                if (2 * this.yMax + this.x - this.y > this.cutDiagonal) {
                    final int i = (this.cutDiagonal - this.x - this.y) / 2;
                    final PolygonFiller.Node l = new PolygonFiller.Node(this.x,
                            this.y, this.y + i, 1);
                    this.x += i + 1;
                    this.y += i + 1;
                    return l;
                }
                final PolygonFiller.Node l = new PolygonFiller.Node(this.x, this.y,
                        this.yMax, 1);
                this.y = this.yMax + 1;
                return l;
            }
            final PolygonFiller.Node line = (PolygonFiller.Node) this.gen.gen();
            if (line == null) {
				return null;
			}
            this.y = line.y; // ((Integer) line.get(0)).intValue();
            // int[] iA = (int[]) line.get(1);
            this.x = line.x; // iA[PolygonFiller.X];
            if (2 * line.yMax + this.x - this.y > this.cutDiagonal) {
                if (Debug.DEBUGCODE
                        && (this.cutDiagonal != Integer.MIN_VALUE)
                        && (this.x + this.y > this.cutDiagonal)) {
					PecanTools.logger.info("An unintended " + this.cutDiagonal + " "
                            + this.x + " " + this.y);
				}
                if (this.x + this.y <= this.cutDiagonal) {
                    final int i = (this.cutDiagonal - this.x - this.y) / 2;
                    final PolygonFiller.Node l = new PolygonFiller.Node(this.x,
                            this.y, this.y + i, 1);
                    this.x += i + 1;
                    this.y += i + 1;
                    this.yMax = line.yMax;// iA[PolygonFiller.YMAX];
                    return l;
                }
                this.yMax = line.yMax; // iA[PolygonFiller.YMAX];
                return this.gen();
            }
            return line;
        }
    }

    public static void replaceEdgeLengths(final NewickTreeParser.Node tree,
            final double oldValue, final double newValue) {
        for (final Iterator<Object> it = tree.getNodes().iterator(); it.hasNext();) {
            PecanTools.replaceEdgeLengths((NewickTreeParser.Node) it.next(),
                    oldValue, newValue);
        }
        if (tree.edgeLength == oldValue) {
			tree.edgeLength = newValue;
		}
    }

    /**
     * Gets the distances between all pairs of sequences in the tree. Distances
     * equal to the given null distance are excluded from calculation. If all
     * values are equal to this value on a path then the total distance will be
     * equal to the null distance.
     * 
     * @param tree
     * @param seqNo
     * @return
     */
    public static double[][] getDistances(final NewickTreeParser.Node tree,
            final int seqNo, final Function_2Args addLengths) {
        final double[][] dM = new double[seqNo][seqNo];
        PecanTools.getDistances(tree, dM, 0, addLengths);
        return dM;
    }

    private static double[] getDistances(final NewickTreeParser.Node tree,
            final double[][] dM, final int i, final Function_2Args addLengths) {
        if (tree.getNodes().size() != 0) {
            final List<Object> l = tree.getNodes(), l2 = new LinkedList<Object>();
            int j = i;
            for (final Iterator<Object> it = l.iterator(); it.hasNext();) {
                final double[] dA = PecanTools.getDistances((NewickTreeParser.Node) it
                        .next(), dM, j, addLengths);
                int k = i;
                for (final Iterator<Object> it2 = l2.iterator(); it2.hasNext();) {
                    final double[] dA2 = (double[]) it2.next();
                    for (int m = 0; m < dA.length; m++) {
                        for (int n = 0; n < dA2.length; n++) {
                            final double d = ((Number) addLengths.fn(
                                    new Double(dA[m]), new Double(
                                            dA2[n]))).doubleValue();
                            dM[j + m][k + n] = d;
                            dM[k + n][j + m] = d;
                        }
                    }
                    k += dA2.length;
                }
                l2.add(dA);
                j += dA.length;
            }
            final double[] dA = new double[j - i];
            j = 0;
            for (final Iterator<Object> it = l2.iterator(); it.hasNext();) {
                final double[] dA2 = (double[]) it.next();
                System.arraycopy(dA2, 0, dA, j, dA2.length);
                j += dA2.length;
            }
            final Double d = new Double(tree.edgeLength);
            for (int k = 0; k < dA.length; k++) {
				dA[k] = ((Number) addLengths.fn(new Double(dA[k]), d))
                        .doubleValue();
			}
            return dA;
        }
        dM[i][i] = Double.NaN;
        return new double[] { tree.edgeLength };
    }

    /**
     * start is inclusive, end exclusive
     * 
     * @param border
     * @param sQIJ
     * @return
     */
    public static Function_Index_2Args transitiveAnchors(
            final int border, final ScrollingQueue_Int sQI) {
        return new Function_Index_2Args() {
            int pI = sQI.firstIndex();

            int pJ = Integer.MIN_VALUE;

            public final Object fn(final int start, final int end) {
                if (this.pI < start + border) {
					this.pI = start + border;
				}
                while (true) {
                    if ((this.pI >= end - border) || (this.pI >= sQI.lastIndex())) {
						return null;
					}
                    if (this.pI >= sQI.firstIndex()) {
                        final int j = sQI.get(this.pI);
                        if ((j != Integer.MAX_VALUE) && (j > this.pJ)) {
                            this.pJ = j;
                            if (true) {
                                final PolygonFiller.Node n = new PolygonFiller.Node(
                                        this.pJ + border, this.pI - border, this.pI
                                                - border, 1);
                                this.pI++;
                                return n;
                            }
                        }
                    }
                    this.pI++;
                }
            }
        };
    }

    /**
     * Outputs an edgeList provided as a {@link bp.common.fp.Generator}into a
     * temporary file and returns the string name.
     * 
     * @return
     */
    public static int toFile(final Generator gen, final String f, final boolean append) {
        try {
            final OutputStream oS = new BufferedOutputStream(
                    new FileOutputStream(f, append));
            final int i = AnchorParser_Generator.writeOutEdgeList(gen, oS);
            oS.close();
            return i;
        } catch (final IOException e1) {
            throw new IllegalStateException();
        }
    }

    public static String getTempFileHandle() {
        File f;
        try {
            f = File.createTempFile("edgeList", null);
            f.deleteOnExit();
        } catch (final IOException e) {
            throw new IllegalStateException();
        }
        return f.getAbsolutePath();
    }

    /**
     * @param n
     * @param m
     * @return
     */
    public static Node getCommonAncestor(Node n, Node m) {
        final List<Node> l = new ArrayList<Node>();
        while (n != null) {
            l.add(n);
            n = n.getParent();
        }
        while (m != null) {
            if (l.contains(m)) {
				return m;
			}
            m = m.getParent();
        }
        return null;
    }

    public static double averagePathLengthToChildren(final Node m) {
        final MutableInteger mI = new MutableInteger(0);
        final double d = PecanTools.totalPathLengthToChildren(m, mI);
        return d / mI.i;
    }

    /**
     * @param m
     * @return
     */
    private static double totalPathLengthToChildren(final Node m,
            final MutableInteger mI) {
        if (m.getNodes().size() == 0) {
            mI.i++;
            return m.edgeLength;
        }
        double d = 0;
        final int i = mI.i;
        for (final Iterator<Object> it = m.getNodes().iterator(); it.hasNext();) {
			d += PecanTools.totalPathLengthToChildren((NewickTreeParser.Node) it
                    .next(), mI);
		}
        return (mI.i - i) * m.edgeLength + d;
    }

    public static Function_Int_4Args sumTotal(final PairsHeap[][] pHAA) {
        return new Function_Int_4Args() {
            public int fn(final int i, final int j, final int k, final int l) {
                return pHAA[i][j].sumTotal(k, l);
            }
        };
    }

    public static Function_Int_5Args getWeight(
            final PairsHeap[][] pHAA) {
        return new Function_Int_5Args() {
            public int fn(final int i, final int j, final int k, final int l, final int m) {
                return pHAA[i][j].getWeight(k, l, m);
            }
        };
    }

    public static Function totalScore(
            final Function_Int_5Args getWeight) {
        return new Function() {
            public Object fn(final Object o) {
                return new Float(PecanTools.totalScore((int[]) o, getWeight));
            }
        };
    }

    static float totalScore(final int[] iA, final Function_Int_5Args getWeight) {
        float startValue = 0.0f;
        int seqs = 0;
        final int baseValue = Float.floatToRawIntBits(0.0f);
        for (int i = 0; i < iA.length; i++) {
            final int j = iA[i];
            if (j != Integer.MAX_VALUE) {
                seqs++;
                // PairsHeap[] pH = pHA[i];
                for (int k = i + 1; k < iA.length; k++) {
                    final int m = iA[k];
                    if (m != Integer.MAX_VALUE) {
                        final float n = Float.intBitsToFloat(getWeight.fn(
                                i, k, j, m, baseValue));
                        // pH[k].getWeight(j, m, baseValue));
                        startValue += n;
                    }
                }
            }
        }
        if (seqs != 1) {
			startValue /= (seqs * seqs - 1) / 2;
		}
        return startValue;
    }
    
    public static Function totalScoreMinus(
            final Function_Int_5Args getWeight,
            final Function_Int_4Args getTotal) {
        return new Function() {
            public Object fn(final Object o) {
                return new Float(PecanTools.totalScore_Minus((int[]) o,
                        getWeight, getTotal));
            }
        };
    }

    static float totalScore_Minus(final int[] iA,
            final Function_Int_5Args getWeight, final Function_Int_4Args getTotal) {
        float startValue = 0.0f;
        int seqs = 0;
        final int baseValue = Float.floatToRawIntBits(0.0f);
        for (int i = 0; i < iA.length; i++) {
            final int j = iA[i];
            if (j != Integer.MAX_VALUE) {
                seqs++;
                // PairsHeap[] pH = pHA[i];
                for (int k = 0; k < i; k++) {
                    final int m = iA[k];
                    if (m == Integer.MAX_VALUE) {
                        final float n = 1.0f - Float
                                .intBitsToFloat(getTotal.fn(i, k, j,
                                        baseValue));
                        // pH[k].sumTotal(j, baseValue));
                        startValue += n;
                    }
                }
                for (int k = i + 1; k < iA.length; k++) {
                    final int m = iA[k];
                    if (m != Integer.MAX_VALUE) {
                        final float n = Float.intBitsToFloat(getWeight.fn(
                                i, k, j, m, baseValue));
                        // pH[k].getWeight(j, m, baseValue));
                        startValue += n;
                    } else {
                        final float n = 1.0f - Float
                                .intBitsToFloat(getTotal.fn(i, k, j,
                                        baseValue));
                        // pH[k].sumTotal(j, baseValue));
                        startValue += n;
                    }
                }
            }
        }
        startValue /= (seqs * seqs - 1) / 2
                + ((iA.length - seqs) * seqs);
        return startValue;
    }

    public static void outputTrack(final List<Procedure_Int> outputConfidences,
            final List<Procedure_NoArgs> finishConfidences, final String outputFile, final String header)
            throws IOException {
        final File f = File.createTempFile("tmp_output_", ".fa");
        f.deleteOnExit();
        final FastaOutput_Procedure_Int out = new FastaOutput_Procedure_Int(
                new BufferedOutputStream(new FileOutputStream(f)),
                "pecan confidence values");
        outputConfidences.add(new Procedure_Int() {
            char[] cA = new char[] { '0', '1', '2', '3', '4', '5',
                    '6', '7', '8', '9', '*' };

            public void pro(final int i) {
                final float f = Float.intBitsToFloat(i);
                final int j = Math.round(f * 10);
                out.pro(this.cA[j]);
            }
        });
        finishConfidences.add(new Procedure_NoArgs() {
            public void pro() {
                try {
                    out.endAndClose();
                    final OutputStream oS = new BufferedOutputStream(
                            new FileOutputStream(outputFile, true));
                    final InputStream iS = new BufferedInputStream(
                            new FileInputStream(f));
                    final FastaParser_Generator_Int outputFastaParser = new FastaParser_Generator_Int(
                            iS, Integer.MAX_VALUE);
                    FastaOutput_Procedure_Int.writeFile(oS,
                            header,
                            outputFastaParser, Integer.MAX_VALUE);
                    iS.close();
                    oS.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                }
            }
        });
    }

    public static void outputFloatFile(final List<Procedure_Int> outputConfidences,
            final List<Procedure_NoArgs> finishConfidences, final String confidenceFile)
            throws IOException {
        final PrintWriter pW = new PrintWriter(
                new BufferedOutputStream(new FileOutputStream(
                        confidenceFile)));
        outputConfidences.add(new Procedure_Int() {
            public void pro(final int i) {
                final float f = Float.intBitsToFloat(i);
                pW.println(f);
            }
        });
        finishConfidences.add(new Procedure_NoArgs() {
            public void pro() {
                pW.close();
            }
        });
    }

    public static Procedure outputTrackAdaptor(final Function getTotal, final int sequenceNumber, 
    		final List<Procedure_Int> outputConfidences, final Procedure originalOutputAdaptor) {
         return new Procedure() {

            Procedure_2Args convertPositions = AlignmentStitcher
                    .convertInput();

            int[] iA = new int[sequenceNumber];

            public void pro(final Object o) {
                for (final Iterator it = ((List) o).iterator(); it
                        .hasNext();) {
                    final Object o2 = it.next();
                    this.convertPositions.pro(o2, this.iA);
                    final float f = ((Float) getTotal.fn(this.iA)).floatValue();
                    final int i = Float.floatToRawIntBits(f);
                    for (final Iterator<Procedure_Int> it2 = outputConfidences.iterator(); it2
                            .hasNext();) {
                        it2.next().pro(i);
                    }
                }
                originalOutputAdaptor.pro(o);
            }
        };
    }
    
    public static PrintWriter getPrintWriter(String outputFile) {
    	PrintWriter pW = null;
    	try {
 		   pW = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
 	   	} catch (FileNotFoundException e) {
 		   e.printStackTrace();
 		   throw new IllegalStateException();
 	   	}
 	   	return pW;
    }
    
    public static Procedure outputAlignmentPairs(final PairsHeap[][] pHAA, 
    											 final int sequenceNumber, 
    											 final Procedure originalOutputAdaptor,
    											 final PriorityQueue<PairValue> pQueue,
    											 final List<Integer> numberOfPairsRejected,
    											 final int maxSize) {
       return new Procedure() {
    	   Procedure_2Args convertPositions = AlignmentStitcher.convertInput();
    	   int[] iA = new int[sequenceNumber];
    	   int[] iA2 = new int[1000];
    	   int numberRejected = 0;
    	
           public void pro(final Object o) {
               for (final Iterator it = ((List) o).iterator(); it.hasNext();) {
                   final Object o2 = it.next();
                   this.convertPositions.pro(o2, this.iA);
                   for(int i=0; i<this.iA.length; i++) {
                	   if(this.iA[i] != Integer.MAX_VALUE) {
                		   for(int j=i+1; j<sequenceNumber; j++) {
                			   int k = pHAA[i][j].get(this.iA[i], 0, this.iA2);
                			   for(int l=0; l<k; l+=2) {
                				   float f = Float.intBitsToFloat(iA2[l+1]);
                    			   if(pQueue.size() >= maxSize) {
                    				   PairValue pairValue = pQueue.peek();
                    				   if(pairValue.weight < f) {
                    					   numberOfPairsRejected.set(0, new Integer(this.numberRejected++));
                    					   pQueue.poll();
                    					   pairValue = new PairValue(i, j, this.iA[i], this.iA2[l], f);
                    					   pQueue.add(pairValue);
                    				   }
                    			   }
                    			   else {
                    				   PairValue pairValue = new PairValue(i, j, this.iA[i], this.iA2[l], f);
                					   pQueue.add(pairValue);
                    			   }
                			   }
                		   }
                	   }
                   }
               }
               originalOutputAdaptor.pro(o);
           }
       };
   }
    
   public static Procedure outputGapAlignments(final PairsHeap[][] pHAA, final int sequenceNumber, 
			 								   final Procedure originalOutputAdaptor, 
			 								   final PrintWriter pW) {
    	return new Procedure() {
    		Procedure_2Args convertPositions = AlignmentStitcher.convertInput();
    		int[] iA = new int[sequenceNumber];
    		int[] iA2 = new int[1000];
    		int[] seqs = new int[sequenceNumber];

    		public void pro(final Object o) {
    			for (final Iterator it = ((List) o).iterator(); it.hasNext();) {
    				final Object o2 = it.next();
    				this.convertPositions.pro(o2, this.iA);
    				for(int i=0; i<this.iA.length; i++) {
    					if(this.iA[i] != Integer.MAX_VALUE) {
    						for(int j=0; j<sequenceNumber; j++) {
    							if(i != j) {
    								float f = 0.0f;
    								int k = pHAA[i][j].get(this.iA[i], 0, this.iA2);
    								for(int l=0; l<k; l+=2) {
    									f += Float.intBitsToFloat(this.iA2[l+1]);
    								}
    								pW.write(i + " " + this.seqs[i] + " " + j + " " + (1.0f - f) + "\n");
    							}
    						}
    						this.seqs[i]++;
    					}
    				}
    			}
    			originalOutputAdaptor.pro(o);
    		}
    	};
   }
}