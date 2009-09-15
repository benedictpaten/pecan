/*
 * Created on Mar 27, 2005
 */
package bp.pecan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.IntCrapHash;
import bp.common.ds.ScrollingQueue_Int;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Function_Int;
import bp.common.fp.Function_Int_2Args;
import bp.common.fp.Function_Int_3Args;
import bp.common.fp.Functions_Int_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.IterationTools;
import bp.common.fp.Procedure_2Args;
import bp.common.fp.Procedure_Int_3Args;
import bp.pecan.dimensions.Alignment;
import bp.pecan.dimensions.Dimension;
import bp.pecan.dimensions.FunctionalDimension;

/**
 * @author benedictpaten
 */
public class WeightTranslatorTest
                                 extends TestCase {

    /**
     *  
     */
    public WeightTranslatorTest(final String s) {
        super(s);
    }

    public void testAddToResidueMap() {
        final Dimension d = this.getRandomDimension(new int[] { 3, 10, 11, 15,
                16, 20 });
        final Dimension[] dA = this.chopUp(d);
        final ScrollingQueue_Int[] sQA = new ScrollingQueue_Int[] {
                new ScrollingQueue_Int(10, 3, true),
                new ScrollingQueue_Int(10, 11, true),
                new ScrollingQueue_Int(10, 16, true) };
        int offsetIndex = 0;
        for (final Dimension element : dA) {
            WeightTranslator.WT.addToResidueMap(sQA,
                    element.iterator(), offsetIndex);
            offsetIndex += element.size();
        }
        for (int i = 0; i < d.subDimensionsNumber(); i++) {
            for (int j = 0; j < d.size(); j++) {
                if (d.get(j, i) != Integer.MAX_VALUE) {
                    Assert.assertEquals(sQA[i].get(d.get(j, i)), j);
                }
            }
        }
    }

    public void testConvertToWeightArray() {
        IntCrapHash m = new IntCrapHash(50);
        m.put(5, 6, null);
        m.put(1, 3, null);
        m.put(9, 2, null);
        int[] wA = WeightTranslator.WT.convertToWeightArray(m);
        final int[] iA2 = new int[m.size()*2];
        m.getEntries(iA2);
        Assert.assertTrue(Arrays.equals(wA, iA2));
        Assert.assertEquals(m.size(), wA.length / 2);
        m = new IntCrapHash(0);
        wA = WeightTranslator.WT.convertToWeightArray(m);
        Assert.assertEquals(wA.length, 0);
    }

    public void testRemoveFirstPositions() {
        final int[] startAndEnds = new int[] { 3, 10, 11, 15, 16, 20 };
        Dimension d = this.getRandomDimension(startAndEnds);
        final ScrollingQueue_Int[] sQA = new ScrollingQueue_Int[] {
                new ScrollingQueue_Int(10, 3, true),
                new ScrollingQueue_Int(10, 11, true),
                new ScrollingQueue_Int(10, 16, true) };
        WeightTranslator.WT.addToResidueMap(sQA, d.iterator(), 0);
        int offset = 0;
        while (d.size() != 0) {
            for (int i = 0; i < d.subDimensionsNumber(); i++) {
                for (int j = 0; j < d.size(); j++) {
                    if (d.get(j, i) != Integer.MAX_VALUE) {
                        Assert.assertEquals(sQA[i].get(d
                                .get(j, i)), j + offset);
                    }
                }
                if (d.get(0, i) != Integer.MAX_VALUE) {
                    Assert.assertEquals(sQA[i].firstIndex(), d.get(0, i));
                    //assertEquals(startAndEnds[i * 2], d.get(0, i));
                }
            }
            final int[] iA = new int[d.subDimensionsNumber()];
            d.get(0, 0, iA);
            WeightTranslator.WT.removeFirstPositions(sQA,
            /* startAndEnds, */iA);
            d = d.getSlice(1, d.size() - 1);
            offset++;
        }
    }

    Dimension getRandomDimension(final int[] startAndEnds) {
        final Dimension[] dA = new Dimension[startAndEnds.length / 2];
        for (int i = 0; i < dA.length; i++) {
            final int[] iA = new int[startAndEnds[i * 2 + 1]
                    - startAndEnds[i * 2]];
            int j = 0;
            for (int k = startAndEnds[i * 2]; k < startAndEnds[i * 2 + 1]; k++) {
				iA[j++] = k;
			}
            dA[i] = new FunctionalDimension(new Function_Int_2Args() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Function_Int_2Args#polygonClipper(int, int)
                 */
                public int fn(final int i, final int j) {
                    return iA[i];
                }
            }, iA.length, 1);
        }
        Dimension alignment = dA[0];
        for (int i = 1; i < dA.length; i++) {
            final int dSize = alignment.size(), d2Size = dA[i].size();
            alignment = new Alignment(new GeneratorIterator(
                    new Generator() {
                        int i = 0;

                        int j = 0;

                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Generator#gen()
                         */
                        public Object gen() {
                            if ((this.i < dSize) && (this.j < d2Size)) {
                                if (Math.random() > 0.5) {
                                    this.i++;
                                    this.j++;
                                    return new boolean[] { true, true };
                                }
                                if (Math.random() > 0.5) {
                                    this.i++;
                                    return new boolean[] { true,
                                            false };
                                }
                                this.j++;
                                return new boolean[] { false, true };
                            }
                            if (this.i++ < dSize) {
								return new boolean[] { true, false };
							}
                            if (this.j++ < d2Size) {
								return new boolean[] { false, true };
							}
                            return null;
                        }
                    }), alignment, dA[i]);
        }
        return alignment;
    }

    void addToWeightMap(final Map<Integer, Map[]> weightMap, final int i, final int iSeq, final int j,
            final int jSeq) {
        if (!weightMap.containsKey(new Integer(i))) {
            final Map[] oA = new Map[30];
            for (int k = 0; k < oA.length; k++) {
                oA[k] = new TreeMap();
            }
            weightMap.put(new Integer(i), oA);//new TreeMap());
        }
        //Map m = (Map) weightMap.get(new Integer(i));
        if (!weightMap.containsKey(new Integer(j))) {
            final Map[] oA = new Map[30];
            for (int k = 0; k < oA.length; k++) {
                oA[k] = new TreeMap();
            }
            weightMap.put(new Integer(j), oA);//new TreeMap());
            //weightMap.put(new Integer(j), new TreeMap());
        }
        //Map m2 = (Map) weightMap.get(new Integer(j));
        final Map[] oA = weightMap.get(new Integer(i));
        final Map[] oA2 = weightMap.get(new Integer(j));
        final int weight = (int) (Math.random() * 100);
        oA[jSeq].put(new Integer(j), new Integer(weight));
        oA2[iSeq].put(new Integer(i), new Integer(weight));
        //m.put(new Integer(j), new Integer(weight));
        // m2.put(new Integer(i), new Integer(weight));
    }

    void addRandomSubDimensionWeights(final Map<Integer, Map[]> weightMap, final Dimension d,
            final int seqOffset) {
        if (d.size() > 0) {
            while (Math.random() < 0.999) {
                final int from = (int) (d.size() * Math.random()), to = (int) (d
                        .size() * Math.random());
                final int fromS = (int) (d.subDimensionsNumber() * Math
                        .random());
                final int toS = (int) (d.subDimensionsNumber() * Math
                        .random());
                if ((d.get(from, fromS) != Integer.MAX_VALUE)
                        && (d.get(to, toS) != Integer.MAX_VALUE)) {
                    this.addToWeightMap(weightMap, d.get(from, fromS),
                            fromS + seqOffset, d.get(to, toS), toS
                                    + seqOffset);
                }
            }
        }
    }

    Librarian.WeightsGetter getRandomWeightFunction(final Dimension d,
            final Dimension d2) {
        final Map<Integer, Map[]> weightMap = new HashMap<Integer, Map[]>();
        //if (d.size() > d2.size()) {
        //    Dimension d3 = d2;
        //    d2 = d;
        //    d = d3;
        //}
        this.addRandomSubDimensionWeights(weightMap, d, 0);
        this.addRandomSubDimensionWeights(weightMap, d2, d
                .subDimensionsNumber());
        if (d2.size() > 0) {
            for (int i = 0; i < d.size(); i++) {
                final int[] iA = new int[d.subDimensionsNumber()];
                d.get(i, 0, iA);
                for (int k = 0; k < iA.length;) {
                    if (Math.random() > 0.8) {
                        k++;
                        continue;
                    }
                    if (iA[k] != Integer.MAX_VALUE) {
                        int to = i + Math.random() > 0.5 ? (int) (Math
                                .random() * 5)
                                : -(int) (Math.random() * 5);
                        to = to >= d2.size() ? d2.size() - 1 : to;
                        to = to < 0 ? 0 : to;
                        final int[] iA2 = new int[d2.subDimensionsNumber()];
                        d2.get(to, 0, iA2);
                        int l = (int) (Math.random() * iA2.length);
                        while (true) {
                            //int l = (int) (Math.random() * iA2.length);
                            if (iA2[l] != Integer.MAX_VALUE) {
                                to = iA2[l];
                                break;
                            }
                            l = (int) (Math.random() * iA2.length);
                        }
                        this.addToWeightMap(weightMap, iA[k], k, to, l
                                + d.subDimensionsNumber());
                    }
                }
            }
        }
        return new Librarian.WeightsGetter() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.Librarian.WeightsGetter#fn(int, int,
             *      int)
             */
            public int fn(final int i, final int iSeq, final int jSeq, final int[] iA, final int startsIndex) {
                if (weightMap.containsKey(new Integer(i))) {
                    //Map m = (Map) weightMap.get(new Integer(i));
                    final Map m = weightMap.get(new Integer(i))[jSeq];
                    //int[] weights = new int[m.size() * 2];
                    int j = 0;
                    int previous = -1;
                    for (final Iterator it = m.keySet().iterator(); it
                            .hasNext();) {
                        final Integer iN = (Integer) it.next();
                        if (previous >= iN.intValue()) {
							throw new RuntimeException();
						}
                        previous = iN.intValue();
                        iA[j++] = iN.intValue();
                        iA[j++] = ((Integer) m.get(iN))
                                .intValue();
                    }
                    return m.size()*2;//weights;
                }
                return 0;//new int[] {};
            }
        };
        /*
         * return new Function_Index() { public Object fn(int i) { if
         * (weightMap.containsKey(new Integer(i))) { Map m = (Map)
         * weightMap.get(new Integer(i)); int[] weights = new int[m.size() * 2];
         * int j = 0; int previous = -1; for (Iterator it =
         * m.keySet().iterator(); it .hasNext();) { Integer iN = (Integer)
         * it.next(); if (previous >= iN.intValue()) throw new
         * RuntimeException(); previous = iN.intValue(); weights[j++] =
         * iN.intValue(); weights[j++] = ((Integer) m.get(iN)) .intValue(); }
         * return weights; } else return new int[] {}; } };
         */
    }

    Dimension[] chopUp(Dimension d) {
        final List<Dimension> l = new ArrayList<Dimension>();
        while (d.size() != 0) {
            final int i = (int) (Math.random() * 10);
            final Dimension d2 = d.getSlice(0, d.size() < i ? d.size() : i);
            d = d.getSlice(d2.size(), d.size() - d2.size());
            l.add(d2);
        }
        if (Math.random() > 0.5) {
			l.add(d);
		}
        return l.toArray(new Dimension[] {});
    }

    int upToWhatPoint(final Librarian.WeightsGetter randomWeightFn,//Function_Index
            // randomWeightFn,
            final Function_Int residueMap, final int beginIndex, final int maxIndex,
            final int oppositeMaxIndex, final Dimension d, final Dimension d2, final int offset) {
        int i = Integer.MIN_VALUE;
        for (int k = beginIndex; k < maxIndex; k++) {
            final Map<Integer, MutableInteger> m = this.getWeights(randomWeightFn, residueMap, k, d, d2, offset);
            for (final Iterator<Integer> it = m.keySet().iterator(); it.hasNext();) {
                final int j = it.next().intValue();
                i = j > i ? j : i;
            }
            if (i >= oppositeMaxIndex) {
				return k;
			}
        }
        return maxIndex;
    }

    Map<Integer, MutableInteger> getWeights(final Librarian.WeightsGetter randomWeightFunction, //Function_Index
            // randomWeightFunction,
            final Function_Int residueMap, final int index, final Dimension d,
            final Dimension d2, final int offset) {
        final int[] scratchA = new int[1000];
        final int[] iA = new int[d.subDimensionsNumber()];
        d.get(index, 0, iA);
        final Map<Integer, MutableInteger> m = new HashMap<Integer, MutableInteger>();
        for (int i = 0; i < iA.length; i++) {
            if (iA[i] != Integer.MAX_VALUE) {
                for (int k = 0; k < d2.subDimensionsNumber(); k++) {
                    final int n = randomWeightFunction.fn(iA[i], i, k
                            + offset, scratchA, 0);
                    for (int j = 0; j < n; j += 2) {
                        if (residueMap.fn(scratchA[j]) != Integer.MAX_VALUE) {
                            if (!m.containsKey(new Integer(residueMap
                                    .fn(scratchA[j])))) {
								m.put(new Integer(residueMap
                                        .fn(scratchA[j])),
                                        new MutableInteger(0));
							}
                            m.get(new Integer(
                                    residueMap.fn(scratchA[j]))).i += scratchA[j + 1];
                        }
                    }
                }
            }
        }
        return m;
    }

    /**
     * @param i
     * @return
     */
    void weightsCorrect(final int[] weights,
            final Librarian.WeightsGetter randomWeightFunction,
            //Function_Index randomWeightFunction,
            final Function_Int residueMap, final int index, final Dimension d,
            final Dimension d2, final int offset) {
        final Map<Integer, MutableInteger> m = this.getWeights(randomWeightFunction, residueMap, index,
                d, d2, offset);
        for (int i = 0; i < weights.length; i += 2) {
            if (m.containsKey(new Integer(weights[i]))) {
                Assert.assertEquals(m.get(new Integer(
                        weights[i])).i, weights[i + 1]);
            } else {
                Assert.fail();
            }
        }
        //assertEquals(weights.length / 2, l.size());
    }

    Function_Int residueMapping(final Dimension d) {
        final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        for (int i = 0; i < d.size(); i++) {
            final int[] iA = new int[d.subDimensionsNumber()];
            d.get(i, 0, iA);
            for (final int element : iA) {
                if (element != Integer.MAX_VALUE) {
                    m.put(new Integer(element), new Integer(i));
                }
            }
        }
        return new Function_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Function_Int#polygonClipper(int)
             */
            public int fn(final int x) {
                if (m.containsKey(new Integer(x))) {
					return m.get(new Integer(x))
                            .intValue();
				}
                return Integer.MAX_VALUE;
            }
        };
    }

    //multiple subDimension sequences vs another
    public void testWeightTranslator() {
        for (int trials = 0; trials < 1000; trials++) {
            final int[] startEnds1 = new int[(int) (Math.random() * 5 + 1) * 2], startEnds2 = new int[(int) (Math
                    .random() * 5 + 1) * 2];
            {
                int j = 0;
                for (int i = 0; i < startEnds1.length; i += 2) {
                    startEnds1[i] = j;
                    j += (int) (Math.random() * 50);
                    startEnds1[i + 1] = j;
                    j += (int) (Math.random() * 3);
                }
                for (int i = 0; i < startEnds2.length; i += 2) {
                    startEnds2[i] = j;
                    j += (int) (Math.random() * 50);
                    startEnds2[i + 1] = j;
                    j += (int) (Math.random() * 3);
                }
            }
            final Dimension d = this.getRandomDimension(startEnds1), d2 = this.getRandomDimension(startEnds2);
            final Function_Int residueMap1 = this.residueMapping(d), residueMap2 = this.residueMapping(d2);
            /* Function_Index */
            final Librarian.WeightsGetter randomWeightFunction = this.getRandomWeightFunction(
                    d, d2);
            final Function_Int_3Args getMaxResidue = new Function_Int_3Args() {
                int[] iA = new int[1000];
                public int fn(int i, int j, int k) {
                    int l = randomWeightFunction.fn(i, j, k, this.iA, 0);
                    if(l > 0) {
                        return this.iA[l-2];
                    }
                    return Integer.MAX_VALUE;
                };
            };
            final int[] seqs = new int[startEnds1.length / 2];
            for (int i = 0; i < seqs.length; i++) {
                seqs[i] = i;
            }
            final int[] seqs2 = new int[startEnds2.length / 2];
            for (int i = 0; i < seqs2.length; i++) {
				seqs2[i] = i + seqs.length;
			}
            final Dimension[] dA1 = this.chopUp(d), dA2 = this.chopUp(d2);
            final WeightTranslator wT = new WeightTranslator(
                    new Librarian.WeightsGetter() {
                        public int fn(int i, int iSeq, int jSeq, int[] iA, int startsIndex) {
                            int k = randomWeightFunction.fn(i, iSeq, jSeq, iA, 0);
                            int[] iA2 = new int[k];
                            System.arraycopy(iA, 0, iA2, 0, k);
                            for(int j=0; j<k; j+=2) {
                                iA[j] = iA2[k-2-j];
                                iA[j+1] = iA2[k-1-j];
                            }
                            return k;
                        };
                    },
                    getMaxResidue, seqs, seqs2, startEnds1,
                    startEnds2, (int) (Math.random() * 50), Functions_Int_2Args
                            .sum(), new Procedure_2Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Procedure_2Args#pro(java.lang.Object,
                         *      java.lang.Object)
                         */
                        public void pro(Object o, Object o2) {
                            int[] iA = (int[]) o, iA2 = (int[]) o2;
                            System.arraycopy(iA, 0, iA2, 0,
                                    iA2.length);
                        }
                    }, new Procedure_Int_3Args() {
                        int[][] iAA = new int[startEnds1.length/2 + startEnds2.length/2][];
                        {
                            int[] iA = new int[d.subDimensionsNumber() + d2.subDimensionsNumber()];
                            for(int i=0; i<startEnds1.length; i+=2) {
								iA[i/2] = startEnds1[i];
							}
                            for(int i=0; i<startEnds2.length; i+=2) {
								iA[startEnds1.length/2 + i/2] = startEnds2[i];
							}
                            for(int i=0; i<this.iAA.length; i++) {
								this.iAA[i] = iA.clone();
							}
                        }
                        /* (non-Javadoc)
                         * @see bp.common.fp.Procedure_Int_3Args#pro(int, int, int)
                         */
                        public void pro(int i, int j, int k) {
                            Assert.assertEquals(this.iAA[k][j]++, i);
                        }
                    }, new int[10000]);
            int d1Offset = 0, d2Offset = 0, d1Done = 0, d2Done = 0;
            {
                int i = 0, j = 0;
                while ((i < dA1.length) || (j < dA2.length)) {
                    if ((Math.random() > 0.5) && (i < dA1.length)) {
                        final Dimension dF = dA1[i++];
                        d1Offset += dF.size();
                        final List l = new LinkedList();
                        IterationTools.append(dF.iterator(), l);
                        wT.getInputX().pro(l);
                    } else if (j < dA2.length) {
                        final Dimension dF = dA2[j++];
                        d2Offset += dF.size();
                        final List l = new LinkedList();
                        IterationTools.append(dF.iterator(), l);
                        wT.getInputY().pro(l);
                    }
                    final int d1DoneN = this.upToWhatPoint(randomWeightFunction,
                            residueMap2, 0, d1Offset, d2Offset, d, d2, d.subDimensionsNumber());
                    final int d2DoneN = this.upToWhatPoint(randomWeightFunction,
                            residueMap1, 0, d2Offset, d1Offset, d2, d, 0);
                    while (d1Done < d1DoneN) {
                        this.weightsCorrect((int[]) wT.getOutputX().gen(),
                                randomWeightFunction, residueMap2,
                                d1Done++, d, d2, d.subDimensionsNumber());
                    }
                    Assert.assertEquals(wT.getOutputX().gen(), null);
                    while (d2Done < d2DoneN) {
                        this.weightsCorrect((int[]) wT.getOutputY().gen(),
                                randomWeightFunction, residueMap1,
                               d2Done++, d2, d, 0);
                    }
                    Assert.assertEquals(wT.getOutputY().gen(), null);
                }
                Assert.assertEquals(d1Offset, d1Done);
                Assert.assertEquals(d2Offset, d2Done);
                Assert.assertEquals(d1Offset, d.size());
                Assert.assertEquals(d2Offset, d2.size());
            }
        }
    }

}