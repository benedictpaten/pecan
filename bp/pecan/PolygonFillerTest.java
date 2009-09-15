/*
 * Created on Feb 22, 2005
 */
package bp.pecan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.Graphics;
import bp.common.ds.Array;
import bp.common.ds.SkipList;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Function_2Args;
import bp.common.fp.Function_Index;
import bp.common.fp.Function_Index_2Args;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.GeneratorTools;
import bp.common.fp.Generator_Int;
import bp.common.fp.Generators;
import bp.common.fp.IterationTools;
import bp.common.fp.Iterators;
import bp.common.fp.Predicate;
import bp.common.fp.Predicate_2Args;
import bp.common.fp.Predicates;
import bp.common.fp.Predicates_2Args;
import bp.common.fp.Procedure_Int;
import bp.common.fp.Procedure_Int_2Args;
import bp.common.io.Debug;
import bp.pecan.Chains.PrimeConstraints;
import bp.pecan.PolygonFiller.Node;

/**
 * @author benedictpaten
 */
public class PolygonFillerTest
                              extends TestCase {
    Procedure_Int setX, setY;

    List<Integer> listX, listY;

    public static final int SCORE = 2;

    public static final int YSTART = 3;

    public static final int XMAX = 1;

    public static final int XINC = 2;

    public static final int X = 1;

    public static final int YMAX = 0;

    public PolygonFillerTest(final String s) {
        super(s);
    }
    
    public void testFilterEdgeListByLessThanOrEqualConstraints() {
        for(int trial=0; trial<1000; trial++) {
            Debug.pl(" trial " + trial);
            final List l = PolygonFillerTest.convertOldEdgeListToNewEdgeList((List) IterationTools
                    .append(
                            PolygonFillerTest
                                    .clipUpperDiagonalEdgeList(PolygonFillerTest.makeRandomEdgeList(
                                            Math.random() * 0.9,
                                            Math.random() * 0.9,
                                            (int) (Math.random() * 500))
                                            .iterator()),
                            new LinkedList()));

            final List l2 = PolygonFillerTest.convertOldEdgeListToNewEdgeList((List) IterationTools
                    .append(
                            PolygonFillerTest
                                    .clipUpperDiagonalEdgeList(PolygonFillerTest.makeRandomEdgeList(
                                            Math.random() * 0.9,
                                            Math.random() * 0.9,
                                            (int) (Math.random() * 500))
                                            .iterator()),
                            new LinkedList()));

            for (final Iterator it = l.iterator(); it.hasNext();) {
                final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
                n.yMax = n.y;
            }
            
            final int border = (int)(Math.random()*10);
            final List l3 = (List)GeneratorTools.append(Generators.filter(Generators.iteratorGenerator(l.iterator()), 
                    PolygonFiller.filterEdgeListByLessThanOrEqualConstraints(Generators.iteratorGenerator(l2.iterator()), border)), new LinkedList());
            final List l4 = (List)GeneratorTools.append(Generators.filter(Generators.iteratorGenerator(l.iterator()), 
                    PolygonFillerTest.filterEdgeListByLessThanOrEqualConstraints(Generators.iteratorGenerator(l2.iterator()), border)), new LinkedList());
            int pX = -10000;
            int pY = -10000;
            for(final Iterator it=l3.iterator(); it.hasNext();) {
                final Node n = (Node)it.next();
                if(n.x <= pX) {
					throw new IllegalStateException();
				}
                pX = n.x;
                if(n.y <= pY) {
					throw new IllegalStateException();
				}
                pY = n.y;
            }
            Assert.assertTrue(IterationTools.equals(l3.iterator(),
                    l4.iterator(), 
                    Predicates_2Args.equal()));
        }
    }
    
    public static final Predicate filterEdgeListByLessThanOrEqualConstraints(
            final Generator lessThans, final int border) {
        return new Predicate() {
            int lTX;

            int lTXMax;

            int lTY;
            {
                this.getLT();
            }

            public final boolean test(final Object o) {
                final PolygonFiller.Node n = (PolygonFiller.Node) o;
                while (n.x > this.lTX) {
					this.getLT();
				}
                return n.y < this.lTY;
            }

            final void getLT() {
                if (this.lTX < this.lTXMax) {
                    this.lTX++;
                    this.lTY++;
                } else {
                    final PolygonFiller.Node m = (PolygonFiller.Node) lessThans
                            .gen();
                    if (m != null) {
                        this.lTX = m.y + border;
                        this.lTY = m.x - border;
                        this.lTXMax = m.yMax + border;
                    } else {
                        this.lTX = Integer.MAX_VALUE;
                        this.lTY = Integer.MAX_VALUE;
                    }
                }
            }
        };
    }
    
    public void testMergeLessThansEdgeList() {
        for(int trial=0; trial<1000; trial++) {
            Debug.pl(" trial " + trial);
            final List l = PolygonFillerTest.convertOldEdgeListToNewEdgeList((List) IterationTools
                    .append(
                            PolygonFillerTest
                                    .clipUpperDiagonalEdgeList(PolygonFillerTest.makeRandomEdgeList(
                                            Math.random() * 0.9,
                                            Math.random() * 0.9,
                                            (int) (Math.random() * 500))
                                            .iterator()),
                            new LinkedList()));

            final List l2 = PolygonFillerTest.convertOldEdgeListToNewEdgeList((List) IterationTools
                    .append(
                            PolygonFillerTest
                                    .clipUpperDiagonalEdgeList(PolygonFillerTest.makeRandomEdgeList(
                                            Math.random() * 0.9,
                                            Math.random() * 0.9,
                                            (int) (Math.random() * 500))
                                            .iterator()),
                            new LinkedList()));

            for (final Iterator it = l2.iterator(); it.hasNext();) {
                final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
                n.yMax = n.y;
            }
            
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
                n.yMax = n.y;
            }
            
            final List l3 = (List)GeneratorTools.append(PolygonFillerTest.mergeLessThansEdgeList(Generators.iteratorGenerator(l.iterator()), Generators.iteratorGenerator(l2.iterator())), new LinkedList());
            int pX = -10000;
            int pY = -10000;
            for(final Iterator it=l3.iterator(); it.hasNext();) {
                final Node n = (Node)it.next();
                if(n.x <= pX) {
					throw new IllegalStateException();
				}
                pX = n.x;
                if(n.y <= pY) {
					throw new IllegalStateException();
				}
                pY = n.y;
            }
            Assert.assertTrue(IterationTools.equals(l3.iterator(),
                    new GeneratorIterator(PolygonFiller.mergeLessThansEdgeList(Generators.iteratorGenerator(l.iterator()), Generators.iteratorGenerator(l2.iterator()))), 
                    Predicates_2Args.equal()));
        }
    }
    
    public static final Generator mergeLessThansEdgeList(
            final Generator edgeList, final Generator lessThans) {
        return Generators.iteratorGenerator(Iterators.uniq(Iterators.merge(new GeneratorIterator(lessThans), new GeneratorIterator(edgeList), new Comparator() {
            public int compare(final Object arg0, final Object arg1) {
                final Node m = (Node)arg0;
                final Node n = (Node)arg1;
                return m.x < n.x ? -1 : m.x > n.x ? 1 : m.y >= n.y ? -1 : 1;
            }
        }), new Comparator() {
            public int compare(final Object arg0, final Object arg1) {
                final Node m = (Node)arg0;
                final Node n = (Node)arg1;
                return (m.y >= n.y) || (m.x == n.x) ? 0 : -1;
            }
        }, new Function_2Args() {
            public Object fn(final Object o, final Object o2) {
                final Node m = (Node)o;
                final Node n = (Node)o2;
                if((m.x < n.x) || (m.y >= n.y)) {
					return m;
				}
                if(Debug.DEBUGCODE && (m.x != n.x)) {
					throw new IllegalStateException(m + " " + n);
				}
                return n;
            }
        }));
    }
    
    public void testReverseLessThanCoordinates() {
        final int[] iA = new int[] { 5, 10, 8, 12, 13, 17, Integer.MAX_VALUE, Integer.MAX_VALUE, 0 };
        final int[] iA2 = new int[10];
        PolygonFiller.reverseLessThanCoordinates(iA, iA2, 20, 10);
        final int[] iA3 =  new int[] {  20 - 13 + 1, 10 - 17 + 1, 20 - 8 + 1, 10 - 12 + 1, 20 - 5 + 1, 10 - 10 + 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0 };
        Assert.assertTrue(Arrays.equals(iA2, iA3));
    }
    
    public static void fillInHorizontalRowsLeft(final int[] iA, final int xMax, final int yMax, final int colour, final int backgroundColour) {
        for(int y=0; y<yMax-1; y++) {
            for(int x=0; x<xMax; x++) {
                if((iA[PolygonFillerTest.zCoord(x, y, yMax)] == colour) && (iA[PolygonFillerTest.zCoord(x, y+1, yMax)] == backgroundColour) && ((x+1 >= xMax) || (iA[PolygonFillerTest.zCoord(x+1, y+1, yMax)] == backgroundColour)) &&
                        ((x-1 < 0) || (iA[PolygonFillerTest.zCoord(x-1, y+1, yMax)] == backgroundColour))) {
                    while((x<xMax) && (iA[PolygonFillerTest.zCoord(x, y+1, yMax)] == backgroundColour)) {
                        iA[PolygonFillerTest.zCoord(x, y, yMax)] = colour;
                        x++;
                    }
                    //break;
                }
            }
        }
    }
    
    public static void fillInHorizontalRowsRight(final int[] iA, final int xMax, final int yMax, final int colour, final int backgroundColour) {
        for(int y=1; y<yMax; y++) {
            for(int x=0; x<xMax; x++) {
                if((iA[PolygonFillerTest.zCoord(x, y, yMax)] == colour) && (iA[PolygonFillerTest.zCoord(x, y-1, yMax)] == backgroundColour) && ((x+1 >= xMax) || (iA[PolygonFillerTest.zCoord(x+1, y-1, yMax)] == backgroundColour)) &&
                        ((x-1 < 0) || (iA[PolygonFillerTest.zCoord(x-1, y-1, yMax)] == backgroundColour))) {
                    while((x>0) && (iA[PolygonFillerTest.zCoord(x-1, y, yMax)] == backgroundColour) && (iA[PolygonFillerTest.zCoord(x-1, y-1, yMax)] == backgroundColour)) {
                        iA[PolygonFillerTest.zCoord(x-1, y, yMax)] = colour;
                        x--;
                    }
                    //break;
                }
            }
        }
    }
    
    
    public static int zCoord(final int x, final int y, final int yMax) {
        return y * yMax + x;
    }
    
    public static final int BACK_GROUND_COLOUR = ((255 << 24) | (255 << 8) | 255 | (255 << 16));
    public static final int LEFT_COLOUR = ((255 << 24) | 1); //255);
    public static final int RIGHT_COLOUR = ((255 << 24) | 0);//(255 << 8) | 255 | (255 << 16));
    public static final int ANCHOR_COLOUR = ((255 << 24) | (255 << 8));
    
    public static int[] getDisplayMatrix(final int xMax, final int yMax) {
        {
            final int[] iA = new int[xMax * yMax];
            Arrays.fill(iA, PolygonFillerTest.BACK_GROUND_COLOUR);
            return iA;
        }
    }
    
    public static void addLines(final int[] iA, final List row, final int xMax, final int yMax, final int colour) {
        for(final Iterator it=row.iterator(); it.hasNext();) {
            final PolygonFiller.Node n = (PolygonFiller.Node)it.next();
            int x = n.x;
            int y = n.y;
            Debug.pl(n + " ");
            while(y <= n.yMax) {
                if((x < xMax) && (y < yMax) && (x >= 0) && (y >= 0)) {
					iA[y * yMax + x] = colour;
				}
                x += n.z;
                y++;
            }
        }
    }
    
    public static void addPoints(final int[] iA, final int xMax, final int yMax, final int[] points, final int xStart, final int yStart) {
        for(int i=0; (i<points.length) && (points[i] != Integer.MAX_VALUE); i+=2) {
            Debug.pl(" points" + points[i] + " "  + points[i+1]);
            final int j = (points[i+1] - yStart) * yMax + (points[i] - xStart);
            if((j < iA.length) && (j > 0)) {
				iA[j] = ((255 << 24) | (255 << 16));
			}
        }
    }
    
    public static void addPointsValue(final int[] iA, final int xMax, final int yMax, final Generator_Int points, final int xStart, final int yStart, final int backgroundColour) {
        int x;
        while((x = points.gen()) != Integer.MAX_VALUE) {
            final int y = points.gen();
            final float f = (float)Math.exp(Float.intBitsToFloat(points.gen()));
            final int j = PolygonFillerTest.zCoord(x - xStart, y - yStart, yMax);
            if((j < iA.length) && (j > 0) && (iA[j] == backgroundColour)) {
				iA[j] =  PolygonFillerTest.getColourIntensity(PolygonFillerTest.getIntensity(f));
			}
        }
    }
    
    public static void addColourKey(final int[] iA, final int xStart, final int yStart, final int xMax, final int yMax, final int width) {
        for(int i=0; i<=10; i++) {
			PolygonFillerTest.addColourBlock(iA, xStart + i * width, yStart, width, PolygonFillerTest.LEFT_COLOUR, PolygonFillerTest.getColourIntensity(i*0.1f + 0.0001f), xMax, yMax);
		}
    }
    
    public static void addColourBlock(final int[] iA, final int xStart, final int yStart, final int width, final int borderColour, final int colour, final int xMax, final int yMax) {
        for(int x=0; x<width; x++) {
            iA[PolygonFillerTest.zCoord(x + xStart, 0 + yStart, yMax)] = borderColour;
            iA[PolygonFillerTest.zCoord(x + xStart, width-1 + yStart, yMax)] = borderColour;
        }
        for(int y=1; y<width-1; y++) {
            iA[PolygonFillerTest.zCoord(0 + xStart, y + yStart, yMax)] = borderColour;
            iA[PolygonFillerTest.zCoord(width-1 + xStart, y + yStart, yMax)] = borderColour;
            for(int x=1; x<width-1; x++) {
                iA[PolygonFillerTest.zCoord(x + xStart, y + yStart, yMax)] = colour;
            }
        }
    }
    
    
    public static float getIntensity(float f) {
        float threshold = (float)Math.log(0.01);
        f = ((float)Math.log(f) - threshold) / -threshold;
        //Debug.pl(" f " + f);
        return f;
        //return f;
    }
    
    public static float getIntensity2(final float f) {
        return (float)Math.pow(f, 1/1.4f);
        //float threshold = (float)Math.log(0.0001);
        //return  ((float)Math.log(f) - threshold) / -threshold;
        //return f;
    }
    
    public static int getColourIntensity(float f) {
        if((f > 1.0001f) || (f < 0.0f)) {
			throw new IllegalStateException(f + " ");
		}
        if(f > 1.0f) {
			f = 1.0f;
		}
        return (((int)(255 * PolygonFillerTest.getIntensity2(f)) + 0) << 24) | (((int)(255 - (255 * f))) << 0) | /*(((int)(255 - (255 * f))) << 16)*/ 0 | (((int)(255 * f)) << 16); // (((int)(155 * f) + 100) << 24) | (255 << 16); 
    }
    
    
    public static void displayMatrix(final int[] iA, final int xMax, final int yMax) {
        Graphics.displayArrayAsImage(iA, yMax, xMax);
        while (true) {
            ;
        }   
    }
    
    public static void checkPolygon(final List row, final List column) {
       PolygonFillerTest.checkPolygon(PolygonFillerTest.convertNewEdgeListToOldEdgeList((List)IterationTools.append(PolygonFiller.
               combineEdgeLists(row.iterator(), 
               column.iterator()), new LinkedList())), 0, 0);
    }

    public void testMergeEdgeListWithTransitiveAnchors() {
        for (int trial = 0; trial < 1000; trial++) {
            Debug.pl(" trial " + trial);
            final List l = PolygonFillerTest.convertOldEdgeListToNewEdgeList((List) IterationTools
                    .append(
                            PolygonFillerTest
                                    .clipUpperDiagonalEdgeList(PolygonFillerTest.makeRandomEdgeList(
                                            Math.random() * 0.9,
                                            Math.random() * 0.9,
                                            (int) (Math.random() * 500))
                                            .iterator()),
                            new LinkedList()));

            final List l2 = PolygonFillerTest.convertOldEdgeListToNewEdgeList((List) IterationTools
                    .append(
                            PolygonFillerTest
                                    .clipUpperDiagonalEdgeList(PolygonFillerTest.makeRandomEdgeList(
                                            Math.random() * 0.9,
                                            Math.random() * 0.9,
                                            (int) (Math.random() * 500))
                                            .iterator()),
                            new LinkedList()));

            for (final Iterator it = l2.iterator(); it.hasNext();) {
                final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
                n.yMax = n.y;
            }

            final List l3 = new LinkedList();
            l3.addAll(l);
            for (final Iterator it = l2.iterator(); it.hasNext();) {
                final PolygonFiller.Node n = (PolygonFiller.Node) it.next();
                source: {
                    for (final Iterator it2 = l.iterator(); it2.hasNext();) {
                        final PolygonFiller.Node m = (PolygonFiller.Node) it2
                                .next();
                        if (n.x >= m.x) {
                            if (n.x <= m.x + m.yMax - m.y) {
								break source;
							}
                            if (n.y <= m.yMax) {
								break source;
							}
                        }
                        else
                            if (n.y >= m.y) {
								break source;
							}
                    }
                    l3.add(n);
                }
            }
            Collections.sort(l3);
            final Iterator it2 = l2.iterator();
            final List l4 = (List) IterationTools.append(
                    new GeneratorIterator(PolygonFiller
                            .mergeEdgeListWithTransitiveAnchors(
                                    Generators.iteratorGenerator(l
                                            .iterator()), new Function_Index_2Args() {
                                        PolygonFiller.Node n = null;
                                        public Object fn(int i, int j) {
                                            if(this.n == null) {
                                                if(!it2.hasNext()) {
													return null;
												}
                                                this.n = (PolygonFiller.Node)it2.next();
                                            }
                                            if(this.n.y < i) {
                                                this.n = null;
                                                return this.fn(i, j);
                                            }
                                            if(this.n.y < j) {
                                                Object rV = this.n;
                                                this.n = null;
                                                return rV;
                                            }
                                            return null;
                                        };
                                    }, Predicates.alwaysTrue(), Predicates.alwaysTrue())),
                    new LinkedList());
           
            Assert.assertTrue(IterationTools.equals(l3.iterator(), l4
                    .iterator(), new Predicate_2Args() {
                 /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
                 *      java.lang.Object)
                 */
                public boolean test(final Object o, final Object o2) {
                    if(((Comparable<Object>) o).compareTo(o2) != 0) {
						Assert.fail();
					}
                    return true;
                }
            }));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        this.listX = new ArrayList<Integer>();
        this.listY = new ArrayList<Integer>();
        this.setX = new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(final int i) {
                PolygonFillerTest.this.listX.add(new Integer(i));
            }
        };
        this.setY = new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(final int i) {
                PolygonFillerTest.this.listY.add(new Integer(i));
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        this.listX = null;
        this.listY = null;
        this.setX = null;
        this.setY = null;
        super.tearDown();
    }

    public static boolean listsEquals(final Iterator it, final Iterator it2,
            final boolean isNonOverlappingChain) {
        return IterationTools.equals(it, it2, new Predicate_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
             *      java.lang.Object)
             */
            public boolean test(final Object o, final Object o2) {
                final List l = (List) o, l2 = (List) o2;
                Assert.assertTrue(l.get(0).equals(l2.get(0)));
                if (isNonOverlappingChain) {
					Assert.assertTrue(l.size() == 2);
				}
                Assert.assertTrue(l.size() == l2.size());
                for (int i = 1; i < l.size(); i++) {
					Assert.assertTrue(Arrays.equals((int[]) l.get(i),
                            (int[]) l2.get(i)));
				}
                return true;
            }
        });
    }

    public static Generator convertOldEdgeListToNewEdgeList(
            final Generator gen) {
        return new Generator() {
            List<Node> l = new LinkedList<Node>();

            public Object gen() {
                if (this.l.size() != 0) {
					return this.l.remove(0);
				}
                final List l2 = (List) gen.gen();
                if (l2 == null) {
					return null;
				}
                final int y = ((Integer) l2.get(0)).intValue();
                for (final Iterator it = l2.listIterator(1); it.hasNext();) {
                    final int[] iA = (int[]) it.next();
                    this.l.add(new PolygonFiller.Node(
                            iA[PolygonFillerTest.X], y,
                            iA[PolygonFillerTest.YMAX],
                            iA[PolygonFillerTest.XINC]));
                }
                return this.gen();
            }
        };
    }

    public static List convertOldEdgeListToNewEdgeList(final List l) {
        return (List) IterationTools.append(new GeneratorIterator(
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(l.iterator()))),
                new LinkedList());
    }

    public static Iterator convertOldEdgeListToNewEdgeList(final Iterator it) {
        return ((List) IterationTools.append(new GeneratorIterator(
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(it))), new LinkedList()))
                .iterator();
    }

    public static Iterator convertNewEdgeListToOldEdgeList(final Iterator it) {
        return ((List) IterationTools.append(new GeneratorIterator(
                PolygonFillerTest.convertNewEdgeListToOldEdgeList(Generators
                        .iteratorGenerator(it))), new LinkedList()))
                .iterator();
    }

    public static List convertNewEdgeListToOldEdgeList(final List l) {
        return (List) IterationTools.append(new GeneratorIterator(
                PolygonFillerTest.convertNewEdgeListToOldEdgeList(Generators
                        .iteratorGenerator(l.iterator()))),
                new LinkedList());
    }

    public static Generator convertNewEdgeListToOldEdgeList(
            final Generator gen) {
        return new Generator() {
            PolygonFiller.Node n = null;

            public Object gen() {
                if (this.n == null) {
                    this.n = (PolygonFiller.Node) gen.gen();
                }
                if (this.n == null) {
					return null;
				}
                final List<Object> l = PolygonFillerTest.createLine(this.n.x, this.n.y, this.n.yMax, this.n.z);
                PolygonFiller.Node m = (PolygonFiller.Node) gen.gen();
                while ((m != null) && (m.y == this.n.y)) {
                    final List<Object> l2 = PolygonFillerTest.createLine(m.x, m.y, m.yMax, m.z);
                    l.add(l2.get(1));
                    m = (PolygonFiller.Node) gen.gen();
                }
                this.n = m;
                return l;
            }
        };
    }

    public static List<Object> convertNewEdgeToOldEdge(final PolygonFiller.Node n) {
        return PolygonFillerTest.createLine(n.x, n.y, n.yMax, n.z);
    }

    public static List<Object> createLine(final int x, final int yStart, final int yEnd,
            final int gradient) {
        final List<Object> l = new LinkedList<Object>();
        l.add(new Integer(yStart));
        final int[] e = new int[3];
        e[PolygonFillerTest.X] = x;
        e[PolygonFillerTest.XINC] = gradient;
        e[PolygonFillerTest.YMAX] = yEnd;
        l.add(e);
        return l;
    }

    public void testNERPoints_Generator() {
        final List l = Arrays.asList(new Object[] { PolygonFillerTest.createLine(5, 6, 8, 1),
                PolygonFillerTest.createLine(10, 10, 10, 1) });
        List l3 = (List) IterationTools.append(new GeneratorIterator(
                PolygonFiller.nERPoints(
                        PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                .iteratorGenerator(l.iterator())), 4,
                        -1, -1, 16, 17)), new LinkedList());
        List<Object> l2 = Arrays.asList(new Object[] {
                new int[] { -1, -1, 5, 6 },
                new int[] { 7, 8, 10, 10 },
                new int[] { 10, 10, 16, 17 } });
        Assert.assertEquals(l3.size(), l2.size());
        for (int i = 0; i < l2.size(); i++) {
            Assert.assertTrue(Arrays.equals((int[]) l3.get(i), (int[]) l2
                    .get(i)));
        }

        l3 = (List) IterationTools.append(new GeneratorIterator(
                PolygonFiller.nERPoints(
                        PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                .iteratorGenerator(l.iterator())), 5,
                        -1, -1, 16, 17)), new LinkedList());
        l2 = Arrays.asList(new Object[] { new int[] { -1, -1, 5, 6 },
                new int[] { 10, 10, 16, 17 } });
        Assert.assertEquals(l3.size(), l2.size());
        for (int i = 0; i < l2.size(); i++) {
            Assert.assertTrue(Arrays.equals((int[]) l3.get(i), (int[]) l2
                    .get(i)));
        }

        l3 = (List) IterationTools.append(new GeneratorIterator(
                PolygonFiller.nERPoints(
                        PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                .iteratorGenerator(l.iterator())),
                        12, -1, -1, 16, 17)), new LinkedList());
        l2 = Arrays.asList(new Object[] { new int[] { -1, -1, 5, 6 },
                new int[] { 10, 10, 16, 17 } });
        Assert.assertEquals(l3.size(), l2.size());
        for (int i = 0; i < l2.size(); i++) {
            Assert.assertTrue(Arrays.equals((int[]) l3.get(i), (int[]) l2
                    .get(i)));
        }

        l3 = (List) IterationTools.append(new GeneratorIterator(
                PolygonFiller.nERPoints(
                        PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                .iteratorGenerator(l.iterator())),
                        13, -1, -1, 16, 17)), new LinkedList());
        l2 = Arrays.asList(new Object[] {});
        Assert.assertEquals(l3.size(), l2.size());
        for (int i = 0; i < l2.size(); i++) {
            Assert.assertTrue(Arrays.equals((int[]) l3.get(i), (int[]) l2
                    .get(i)));
        }
    }

    public void testCutPointGenerator() {
        for (int trial = 0; trial < 1000; trial++) {
            final LinkedList l = (LinkedList) IterationTools
                    .append(
                            PolygonFillerTest
                                    .clipUpperDiagonalEdgeList(PolygonFillerTest.makeRandomEdgeList(
                                            0.98 * Math.random(),
                                            0.98 * Math.random(),
                                            (int) (Math.random() * 200))
                                            .iterator()),
                            new LinkedList());
            final List l5 = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(l)));
            final List l3 = new LinkedList();
            final int minScore = 1 + (int) (Math.random() * 20);
            final int divScore = minScore
                    * ((int) (2 + Math.random() * 10));
            for (final Iterator it = l.iterator(); it.hasNext();) {
                final List l2 = (List) it.next();
                final int y = ((Integer) l2.get(0)).intValue();
                for (final Iterator it2 = l2.listIterator(1); it2.hasNext();) {
                    final int[] iA = (int[]) it2.next();
                    int i = iA[PolygonFillerTest.YMAX] - y + 1;
                    if (i >= minScore) {
                        final int j = 1 + i / divScore;
                        int inc = i / (j + 1);
                        inc = inc == 0 ? 1 : inc;
                        i = j * inc;
                        for (int k = 1; k * inc <= i; k++) {
                            l3.add(new int[] {
                                    iA[PolygonFillerTest.X] - 1 + k
                                            * inc, y - 1 + k * inc });
                        }
                    }
                }
            }
            final List l4 = (List) IterationTools
                    .append(
                            new GeneratorIterator(
                                    PolygonFiller
                                            .cutPointGenerator(
                                                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                                            .iteratorGenerator(l
                                                                    .iterator())),
                                                    minScore,
                                                    divScore)),
                            new LinkedList());
            Assert.assertTrue(IterationTools.equals(l3.iterator(), l4
                    .iterator(), new Predicate_2Args() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
                 *      java.lang.Object)
                 */
                public boolean test(final Object o, final Object o2) {
                    return Arrays.equals((int[]) o, (int[]) o2);
                }
            }));
        }
    }

    static void fillInMatrixWithEdges(final int[][] matrix, final List eT) {
        for (final Iterator it = eT.iterator(); it.hasNext();) {
            final List l = (List) it.next();
            for (final Iterator it2 = l.listIterator(1); it2.hasNext();) {
                int y = ((Integer) l.get(0)).intValue();
                final int[] iA = (int[]) it2.next();
                while (y <= iA[PolygonFillerTest.YMAX]) {
                    matrix[iA[PolygonFillerTest.X]][y++] = 1;
                    iA[PolygonFillerTest.X] += iA[PolygonFillerTest.XINC];
                }
            }
        }
    }

    public void testPolygonIterator() {
        for (int trial = 0; trial < 1000; trial++) {
            final int xMin = (int) (Math.random() * 30), xMax = (int) (xMin + Math
                    .random() * 30), yMin = (int) (Math.random() * 30), yMax = yMin
                    + (int) (Math.random() * 30);
            final List l = PolygonFillerTest.makeRandomEdgeList(Math.random() * 0.95, Math
                    .random() * 0.95, 100);
            final List l2 = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(l)));
            PolygonFillerTest.flipXYDiagonalCoordinates(l2);
            final Function_Index fn = PolygonFiller
                    .polygonIterator(
                            PolygonFiller
                                    .clipBoundariesOfDiagonalList(
                                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                                    .iteratorGenerator(PolygonFillerTest
                                                            .clipUpperDiagonalEdgeList(PolygonFillerTest.convertNewEdgeListToOldEdgeList(
                                                                    PolygonFiller
                                                                            .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(l)))
                                                                    .iterator()))),
                                            xMin, xMax, yMin, yMax),
                            PolygonFiller
                                    .clipBoundariesOfDiagonalList(
                                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                                    .iteratorGenerator(PolygonFillerTest
                                                            .clipUpperDiagonalEdgeList(l2
                                                                    .iterator()))),
                                            yMin, yMax, xMin, xMax),
                            xMin, xMax, yMin, yMax);
            final Object[] oA = (Object[]) fn.fn(xMax - 1 + yMax - 1);
            final int[] dC = (int[]) oA[2];
            Assert.assertEquals(dC[0], xMin - 1);
            Assert.assertEquals(dC[1], yMin - 1);
            Assert.assertEquals(dC[2], xMin - 1);
            Assert.assertEquals(dC[3], yMin - 1);
            Assert.assertEquals(dC[4], xMax - 1);
            Assert.assertEquals(dC[5], yMax - 1);
            Assert.assertEquals(dC[6], xMax - 1);
            Assert.assertEquals(dC[7], yMax - 1);
            PolygonFillerTest.checkPolygon((List) IterationTools.append(
                    PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                            .combineEdgeLists(((List) oA[0])
                                    .iterator(), ((List) oA[1])
                                    .iterator())), new ArrayList(
                            ((List) oA[0]).size()
                                    + ((List) oA[1]).size())), 1, 0);
            try {
                fn.fn(xMax + yMax);
                Assert.fail();
            } catch (final IllegalStateException e) {
                ;
            }
        }
    }

    static List flipXY(final List l) {
        final SkipList sK = new SkipList();
        final List l2 = new LinkedList();
        for (int i = 0; i < l.size(); i++) {
            final List l3 = (List) l.get(i);
            final int y = ((Integer) l3.get(0)).intValue();
            for (int j = 1; j < l3.size(); j++) {
                final int[] iA = (int[]) l3.get(j);
                List<Object> l4 = (List<Object>) sK.search(iA[PolygonFillerTest.X]);
                if (l4 == null) {
                    l4 = new LinkedList<Object>();
                    l4.add(new Integer(iA[PolygonFillerTest.X]));
                    sK.insert(iA[PolygonFillerTest.X], l4);
                }
                final int[] iA2 = (int[]) PolygonFillerTest.createLine(
                        y,
                        iA[PolygonFillerTest.X],
                        iA[PolygonFillerTest.X]
                                + iA[PolygonFillerTest.YMAX] - y, 1)
                        .get(1);
                PolygonFillerTest.inSort(l4, 1,
                        iA2[PolygonFillerTest.X], iA2);
            }
        }
        for (final SkipList.Iterator it = sK.iterator(); it.hasNext();) {
			l2.add(it.next());
		}
        return l2;
    }

    public void testClipBoundariesOfDiagonalList() {
        final Random r = new Random();
        for (int trial = 0; trial < 1000; trial++) {
            final int xMin = r.nextInt(20), xMax = xMin + r.nextInt(20), yMin = r
                    .nextInt(20), yMax = yMin + r.nextInt(20);
            List l = PolygonFillerTest.makeRandomEdgeList(Math.random(), Math.random(),
                    100);
            l = (List) IterationTools.append(PolygonFillerTest
                    .clipUpperDiagonalEdgeList(l.iterator()),
                    new ArrayList());

            final boolean[][] matrix = new boolean[1000][100];
            final List l2 = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                    .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(l)));
            for (int i = 0; i < l2.size(); i++) {
                final List j = (List) l2.get(i);
                for (int k = 1; k < j.size(); k++) {
                    int y = ((Integer) j.get(0)).intValue();
                    final int[] iA = (int[]) j.get(k);
                    while (y <= iA[PolygonFillerTest.YMAX]) {
                        if ((iA[PolygonFillerTest.X] >= xMin)
                                && (iA[PolygonFillerTest.X] < xMax)
                                && (y >= yMin) && (y < yMax)) {
							matrix[iA[PolygonFillerTest.X]][y] = true;
						}
                        y++;
                        iA[PolygonFillerTest.X] += iA[PolygonFillerTest.XINC];
                    }
                }
            }

            final Iterator it = new GeneratorIterator(
                    PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                            .clipBoundariesOfDiagonalList(
                                    Generators
                                            .iteratorGenerator(PolygonFiller
                                                    .cloneEdgeList(
                                                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(l))
                                                    .iterator()),
                                    xMin, xMax, yMin, yMax)));
            final boolean[][] matrix2 = new boolean[1000][100];
            for (; it.hasNext();) {
                final List j = (List) it.next();
                for (int k = 1; k < j.size(); k++) {
                    int y = ((Integer) j.get(0)).intValue();
                    final int[] iA = (int[]) j.get(k);
                    while (y <= iA[PolygonFillerTest.YMAX]) {
                        matrix2[iA[PolygonFillerTest.X]][y++] = true;
                        iA[PolygonFillerTest.X] += iA[PolygonFillerTest.XINC];
                    }
                }
            }

            Assert.assertTrue(Array.arraysEqual().test(matrix, matrix2,
                    new Predicate_2Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Predicate_2Args#test(java.lang.Object,
                         *      java.lang.Object)
                         */
                        public boolean test(final Object o, final Object o2) {
                            return Arrays.equals((boolean[]) o,
                                    (boolean[]) o2);
                        }
                    }));
        }
    }

    public void testScanPolygon() {
        final List eT = new ArrayList();
        final List<Object> eT2 = new ArrayList<Object>();
        eT.add(eT2);
        eT2.add(new Integer(0));
        eT2.add(new int[] { 1, 2, -1 });
        eT2.add(new int[] { 1, 2, 1 });
        final List<Object> eT3 = new ArrayList<Object>();
        eT.add(eT3);
        eT3.add(new Integer(2));
        eT3.add(new int[] { 4, 0, 1 });
        eT3.add(new int[] { 4, 4, -1 });
        PolygonFillerTest.scanPolygon(eT, PolygonFillerTest
                .scanSpan(this.setX), this.setY);
        int[] iA = new int[] { 0, 1, 2, 3, 4 };
        for (int i = 0; i < iA.length; i++) {
            Assert.assertEquals(this.listY.get(i), new Integer(iA[i]));
        }
        Assert.assertEquals(this.listY.size(), 5);
        iA = new int[] { 2, 1, 2, 3, 0, 1, 2, 3, 4, 1, 2, 3, 2 };
        for (int i = 0; i < iA.length; i++) {
            Assert.assertEquals(this.listX.get(i), new Integer(iA[i]));
        }
        Assert.assertEquals(this.listX.size(), iA.length);
    }

    public void testCloneEdgeList() {
        final List eT = new ArrayList();
        final List<Object> eT2 = new ArrayList<Object>();
        eT.add(eT2);
        eT2.add(new Integer(0));
        eT2.add(new int[] { 1, 2, -1 });
        eT2.add(new int[] { 1, 2, 1 });
        final List<Object> eT3 = new ArrayList<Object>();
        eT.add(eT3);
        eT3.add(new Integer(2));
        eT3.add(new int[] { 4, 0, 1 });
        eT3.add(new int[] { 4, 4, -1 });
        final List eTC = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                .cloneEdgeList(PolygonFillerTest.convertOldEdgeListToNewEdgeList(eT)));
        Assert.assertTrue(eTC != eT);
        Assert.assertTrue(eTC.size() == eT.size());
        final List eTC2 = (List) eTC.get(0), eTC3 = (List) eTC.get(1);
        Assert.assertTrue(eT2 != eTC2);
        Assert.assertTrue(eTC2.size() == eT2.size());
        Assert.assertEquals(eT2.get(0), eTC2.get(0)); // can be == as immutable
        // objects
        for (int i = 1; i < eT2.size(); i++) {
            Assert.assertTrue(Arrays.equals((int[]) eT2.get(i), (int[]) eTC2
                    .get(i)));
            Assert.assertTrue(eT2.get(i) != eTC2.get(i));
        }
        Assert.assertTrue(eT3 != eTC3);
        Assert.assertTrue(eTC3.size() == eT3.size());
        Assert.assertEquals(eT3.get(0), eTC3.get(0)); // can be == as immutable
        // objects
        for (int i = 1; i < eT3.size(); i++) {
            Assert.assertTrue(Arrays.equals((int[]) eT3.get(i), (int[]) eTC3
                    .get(i)));
            Assert.assertTrue(eT3.get(i) != eTC3.get(i));
        }
    }

    public void testReverseCoordinates2() {
        final Random r = new Random();
        for (int trial = 0; trial < 1000; trial++) {
            final int minMaxOffset = 1 + (int) (Math.random() * 9);
            final int height = 2 * minMaxOffset
                    + (int) (Math.random() * 100), width = 2
                    * minMaxOffset + (int) (Math.random() * 100);
            final Object[] oA = PolygonFillerTest.getRandomlyCutPolygon(width, height,
                    minMaxOffset);
            List lET = (List) oA[0];
            List rET = (List) oA[1];
            final List<int[]> coordinates = new ArrayList<int[]>();
            final MutableInteger y = new MutableInteger(
                    ((Number) ((List) lET.get(0)).get(0)).intValue());
            PolygonFiller.scanPolygon(PolygonFiller.cloneEdgeList(
                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(lET)).iterator(),
                    PolygonFiller.cloneEdgeList(
                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(rET))
                            .iterator(), new Procedure_Int_2Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Procedure_Int_2Args#pro(int, int)
                         */
                        public void pro(int i, final int j) {
                            while (i <= j) {
								coordinates
                                        .add(new int[] { i++, y.i });
							}
                            y.i++;
                        }
                    });
            final List<int[]> coordinates2 = new ArrayList<int[]>();
            lET = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                    .reverseCoordinates(
                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(lET),
                            width - 1, height - 1));
            rET = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                    .reverseCoordinates(
                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(rET),
                            width - 1, height - 1));
            y.i = ((Number) ((List) rET.get(0)).get(0)).intValue();
            PolygonFiller.scanPolygon(PolygonFiller.cloneEdgeList(
                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(rET)).iterator(),
                    PolygonFiller.cloneEdgeList(
                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(lET))
                            .iterator(), new Procedure_Int_2Args() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see bp.common.fp.Procedure_Int_2Args#pro(int, int)
                         */
                        public void pro(int i, final int j) {
                            while (i <= j) {
								coordinates2.add(0, new int[] {
                                        width - i++ - 1,
                                        height - y.i - 1 });
							}
                            y.i++;
                        }
                    });
            Assert.assertEquals(coordinates.size(), coordinates2.size());
            for (int j = 0; j < coordinates.size(); j++) {
				Assert.assertTrue(Arrays.equals(coordinates.get(j),
                        coordinates2.get(j)));
			}
        }
    }

    public int[] getUpperEdgePath(final Iterator it, final int columnSize) {
        final int[] line = new int[columnSize];
        Arrays.fill(line, Integer.MIN_VALUE);
        while (it.hasNext()) {
            final List l = (List) it.next();
            final int yStart = ((Integer) l.get(0)).intValue();
            for (final ListIterator it2 = l.listIterator(1); it2.hasNext();) {
                final int[] e = (int[]) it2.next();
                int j = e[PolygonFillerTest.X];
                for (int i = yStart; i <= e[PolygonFillerTest.YMAX]; i++) {
                    if (j > line[i]) {
						line[i] = j;
					}
                    j++;
                }
            }
        }
        {
            int j = -1;
            for (int i = 0; i < line.length; i++) {
                if (line[i] <= j) {
					line[i] = Integer.MIN_VALUE;
				} else {
					j = line[i];
				}
            }
        }
        return line;
    }

    public static List makeRandomEdgeList(final double rowProb,
            final double columnProb, final int columnSize) {
        final List l = new ArrayList();
        for (int i = 0; i < columnSize; i++) {
            if (Math.random() <= columnProb) {
                final List<Object> l2 = new ArrayList<Object>();
                l2.add(new Integer(i));
                l.add(l2);
                int x = 0;
                do {
                    final int[] e = new int[3];
                    x += (int) (Math.random() * 20);
                    e[PolygonFillerTest.X] = x;
                    e[PolygonFillerTest.YMAX] = i
                            + (int) (Math.random() * 20);
                    e[PolygonFillerTest.XINC] = 1;
                    if (e[PolygonFillerTest.YMAX] >= columnSize) {
						e[PolygonFillerTest.YMAX] = columnSize - 1;
					}
                    l2.add(e);
                } while (Math.random() <= rowProb);
            }
        }
        return l;
    }

    public static List makeRandomEdgeList(final double rowProb,
            final double columnProb, final int heightMinOffset,
            final int heightMaxOffset, final int widthMinOffset,
            final int widthMaxOffset) {
        final List l = new ArrayList();
        for (int i = heightMinOffset; i < heightMaxOffset; i++) {
            if (Math.random() <= columnProb) {
                final List<Object> l2 = new ArrayList<Object>();
                l2.add(new Integer(i));
                int x = widthMinOffset;
                do {
                    final int[] e = new int[3];
                    e[PolygonFillerTest.YMAX] = i
                            + (int) (Math.random() * 20);
                    e[PolygonFillerTest.XINC] = 1;
                    if (e[PolygonFillerTest.YMAX] >= heightMaxOffset) {
						e[PolygonFillerTest.YMAX] = heightMaxOffset - 1;
					}
                    x += Math.random() * 20;
                    if (x + e[PolygonFillerTest.YMAX] - i < widthMaxOffset) {
						e[PolygonFillerTest.X] = x;
					} else {
						break;
					}
                    l2.add(e);
                } while (Math.random() <= rowProb);
                if (l2.size() > 1) {
					l.add(l2);
				}
            }
        }
        return l;
    }

    void listAndLineEqual(final Iterator it, final int[] iA) {
        int j = 0;
        while (it.hasNext()) {
            final List l2 = (List) it.next();
            final int yStart = ((Integer) l2.get(0)).intValue();
            while (j < yStart) {
				Assert.assertEquals(iA[j++], Integer.MIN_VALUE);
			}
            if (l2.size() != 2) {
				Assert.fail();
			}
            Assert.assertEquals(l2.size(), 2);
            final int[] e = (int[]) l2.get(1);
            int k = e[PolygonFillerTest.X];
            while (j <= e[PolygonFillerTest.YMAX]) {
                Assert.assertEquals(iA[j++], k);
                k += e[PolygonFillerTest.XINC];
            }
        }
        while (j < iA.length) {
			Assert.assertEquals(iA[j++], Integer.MIN_VALUE);
		}
    }

    public void testClipUpperEdgeList() {
        for (int i = 0; i < 1000; i++) {
            final int columnSize = (int) (Math.random() * 200);
            final List l = PolygonFillerTest.makeRandomEdgeList(Math.random(), Math.random(), // Math.random(),
                    // Math.random(),
                    columnSize);
            final int[] iA = this.getUpperEdgePath(l.iterator(), columnSize);
            final Iterator it = PolygonFillerTest
                    .clipUpperDiagonalEdgeList(l.iterator());
            final List l2 = (List) IterationTools.append(it,
                    new ArrayList());
            this.listAndLineEqual(l2.iterator(), iA);
        }
    }

    public void testTransformEdges() {
        final List eT = new ArrayList();
        final List<Object> eT2 = new ArrayList<Object>(), eT3 = new ArrayList<Object>();
        eT.add(eT2);
        eT.add(eT3);
        eT2.add(new Integer(2));
        eT3.add(new Integer(3));
        eT2.add(new int[] { 6, 1, 1 });
        eT2.add(new int[] { 6, 2, 1 });
        eT3.add(new int[] { 5, 4, 1 });
        final Iterator it = eT.iterator();
        final Iterator it2 = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                .transformEdges(PolygonFillerTest.convertOldEdgeListToNewEdgeList(eT
                        .iterator()), 3, 5));
        while (it.hasNext()) {
            final List l = (List) it.next(), l2 = (List) it2.next();
            Assert.assertEquals(l.size(), l2.size());
            Assert.assertEquals(((Integer) l.get(0)).intValue() + 5,
                    ((Integer) l2.get(0)).intValue());
            for (int i = 1; i < l.size(); i++) {
                final int[] iA = (int[]) l.get(i), iA2 = (int[]) l2.get(i);
                Assert.assertEquals(iA[PolygonFillerTest.X] + 3,
                        iA2[PolygonFillerTest.X]);
                Assert.assertEquals(iA[PolygonFillerTest.XINC],
                        iA2[PolygonFillerTest.XINC]);
                Assert.assertEquals(iA[PolygonFillerTest.YMAX] + 5,
                        iA2[PolygonFillerTest.YMAX]);
            }
        }
        Assert.assertFalse(it2.hasNext());
    }

    public void testAddVerticalEdgesLeft() {
        for (int i = 0; i < 1000; i++) {
            final int columnSize = (int) (Math.random() * 500);
            final List l = PolygonFillerTest.makeRandomEdgeList(Math.random() * 0.98, Math
                    .random() * 0.98, columnSize);
            final int[] iA = this.getUpperEdgePath(l.iterator(), columnSize);
            int k = 0;
            for (int j = 0; j < iA.length; j++) {
                if (iA[j] < k) {
					iA[j] = k;
				} else if (iA[j] > k) {
					k = iA[j];
				}
            }
            final Iterator it = PolygonFillerTest.convertNewEdgeListToOldEdgeList(new GeneratorIterator(
                    PolygonFiller
                            .addVerticalEdgesLeft(
                                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                            .iteratorGenerator(PolygonFillerTest
                                                    .clipUpperDiagonalEdgeList(l
                                                            .iterator()))),
                                    0, 0, columnSize)));
            this.listAndLineEqual(it, iA);
        }
    }

    public void testAddVerticalEdgesRight() {
        for (int i = 0; i < 1000; i++) {
            final int columnSize = (int) (Math.random() * 500);
            final List l = PolygonFillerTest.makeRandomEdgeList(Math.random() * 0.98, Math
                    .random() * 0.98, columnSize);
            final int[] iA = this.getUpperEdgePath(l.iterator(), columnSize);
            int k = 1000000 - 1;
            for (int j = iA.length - 1; j >= 0; j--) {
                final int m = iA[j];
                iA[j] = k;
                if (m != Integer.MIN_VALUE) {
					k = m - 1;
				}
            }
            final List l2 = (List) IterationTools.append(PolygonFillerTest
                    .clipUpperDiagonalEdgeList(l.iterator()),
                    new ArrayList());
            final List orig = (List) IterationTools
                    .append(
                            new GeneratorIterator(
                                    PolygonFiller
                                            .addVerticalEdgesRight(
                                                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                                            .iteratorGenerator(l2
                                                                    .iterator())),
                                                    1000000, 0,
                                                    columnSize)),
                            new ArrayList());
            final List l3 = PolygonFillerTest.convertNewEdgeListToOldEdgeList(orig);
            this.listAndLineEqual(l3.iterator(), iA);
        }
    }

    public void testCombineEdgeLists() {
        final List eT = new ArrayList();
        final List eT2 = new ArrayList(), eT3 = new ArrayList();
        Iterator it = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                .combineEdgeLists(PolygonFillerTest.convertOldEdgeListToNewEdgeList(eT
                        .iterator()),
                        PolygonFillerTest.convertOldEdgeListToNewEdgeList(eT2
                                .iterator())));
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
        eT.add(eT2);
        eT.add(eT3);
        eT2.add(new Integer(2));
        eT3.add(new Integer(3));
        eT2.add(new int[] { 6, 1, 1 });
        eT3.add(new int[] { 5, 4, 1 });
        eT3.add(new int[] { 6, 6, 1 });
        eT3.add(new int[] { 5, 7, 1 });
        final List eT_2 = new ArrayList();
        final List<Object> eT2_2 = new ArrayList<Object>(), eT3_2 = new ArrayList<Object>();
        eT_2.add(eT2_2);
        eT_2.add(eT3_2);
        eT2_2.add(new Integer(1));
        eT3_2.add(new Integer(3));
        eT2_2.add(new int[] { 6, 100, 1 });
        eT3_2.add(new int[] { 5, 6, 1 });
        eT3_2.add(new int[] { 5, 8, 1 });
        it = PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                .combineEdgeLists(PolygonFillerTest.convertOldEdgeListToNewEdgeList(eT
                        .iterator()),
                        PolygonFillerTest.convertOldEdgeListToNewEdgeList(eT_2
                                .iterator())));
        List l = (List) it.next();
        Assert.assertEquals(((Integer) l.get(0)).intValue(), 1);
        Assert.assertTrue(Arrays.equals((int[]) l.get(1), new int[] { 6,
                100, 1 }));
        Assert.assertEquals(l.size(), 2);
        l = (List) it.next();
        Assert.assertEquals(((Integer) l.get(0)).intValue(), 2);
        Assert.assertTrue(Arrays.equals((int[]) l.get(1), new int[] { 6, 1,
                1 }));
        Assert.assertEquals(l.size(), 2);
        l = (List) it.next();
        Assert.assertEquals(((Integer) l.get(0)).intValue(), 3);
        Assert.assertTrue(Arrays.equals((int[]) l.get(1), new int[] { 5, 4,
                1 }));
        Assert.assertTrue(Arrays.equals((int[]) l.get(2), new int[] { 6, 6,
                1 }));
        Assert.assertTrue(Arrays.equals((int[]) l.get(3), new int[] { 5, 6,
                1 }));
        Assert.assertTrue(Arrays.equals((int[]) l.get(4), new int[] { 5, 7,
                1 }));
        Assert.assertTrue(Arrays.equals((int[]) l.get(5), new int[] { 5, 8,
                1 }));
        Assert.assertEquals(l.size(), 6);
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public void testFlipEdgeXYDiagonalsCoordinates() {
        final List eT = new ArrayList();
        final List<Object> eT2 = new ArrayList<Object>(), eT3 = new ArrayList<Object>();
        eT.add(eT2);
        eT.add(eT3);
        eT2.add(new Integer(2));
        eT3.add(new Integer(7));
        eT2.add(new int[] { 6, 1, 1 });
        eT3.add(new int[] { 10, 8, 1 });
        Iterator it = PolygonFillerTest.convertNewEdgeListToOldEdgeList(new GeneratorIterator(
                PolygonFiller
                        .flipEdgeXYDiagonalsCoordinates(PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                .iteratorGenerator(eT.iterator())))));
        Object[] oA = ((List) it.next()).toArray();
        Assert.assertEquals(oA[0], new Integer(1));
        Assert.assertTrue(Arrays
                .equals((int[]) oA[1], new int[] { 5, 2, 1 }));
        Assert.assertEquals(oA.length, 2);
        oA = ((List) it.next()).toArray();
        Assert.assertEquals(oA[0], new Integer(8));
        Assert.assertTrue(Arrays.equals((int[]) oA[1],
                new int[] { 11, 7, 1 }));
        Assert.assertEquals(oA.length, 2);
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
        it = PolygonFillerTest.convertNewEdgeListToOldEdgeList(new GeneratorIterator(
                PolygonFiller
                        .flipEdgeXYDiagonalsCoordinates(PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                .iteratorGenerator(new ArrayList()
                                        .iterator())))));
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail();
        } catch (final NoSuchElementException e) {
            ;
        }
    }

    public static Object[] getRandomlyCutPolygon(final int width,
            final int height, final int minMaxOffset) {
        final Random r = new Random();
        final List[] poly = PolygonFillerTest.getRandomPolygon(width, height, minMaxOffset, 0);
        final List lREL = poly[0];
        final List rREL = poly[1];
        final Function_Index fn = PolygonFiller.clipPolygons(
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(lREL.iterator())),
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(rREL.iterator())), width,
                height);
        final int i = r.nextInt(height + width - 1);
        final Object[] oA = (Object[]) fn.fn(i);
        oA[0] = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) oA[0]);
        oA[1] = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) oA[1]);
        return oA;
    }

    public static List[] getRandomPolygon(final int width, final int height,
            final int maxOffset, final int minOffset) {
        final List rEL = PolygonFillerTest.makeRandomEdgeList(
                Math.random(), Math.random(), maxOffset, height
                        - maxOffset, maxOffset, width - maxOffset);
        int diagOffset = minOffset
                + (int) (Math.random() * (maxOffset - minOffset));
        List lREL = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) IterationTools
                .append(PolygonFiller.transformEdges(PolygonFiller
                        .cloneEdgeList(
                                PolygonFillerTest.convertOldEdgeListToNewEdgeList(rEL))
                        .iterator(), -diagOffset, diagOffset),
                        new ArrayList()));
        lREL = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) IterationTools
                .append(
                        new GeneratorIterator(
                                PolygonFiller
                                        .addVerticalEdgesLeft(
                                                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                                        .iteratorGenerator(PolygonFillerTest
                                                                .clipUpperDiagonalEdgeList(lREL
                                                                        .iterator()))),
                                                0, 0, height)),
                        new ArrayList()));

        diagOffset = (int) (Math.random() * maxOffset);
        List rREL = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) IterationTools
                .append(PolygonFiller.transformEdges(PolygonFiller
                        .cloneEdgeList(
                                PolygonFillerTest.convertOldEdgeListToNewEdgeList(rEL))
                        .iterator(), diagOffset, -diagOffset),
                        new ArrayList()));
        rREL = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) IterationTools
                .append(
                        new GeneratorIterator(
                                PolygonFiller
                                        .addVerticalEdgesRight(
                                                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                                                        .iteratorGenerator(PolygonFillerTest
                                                                .clipUpperDiagonalEdgeList(rREL
                                                                        .iterator()))),
                                                width, 0, height)),
                        new ArrayList()));
        return new List[] { lREL, rREL };
    }

    public void testClipPolygon_BoundaryConditions() {
        Function_Index fn = PolygonFiller.clipPolygons(
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .arrayGenerator(new Object[] {})),
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .arrayGenerator(new Object[] {})), 0, 0);
        try {
            fn.fn(0);
            Assert.fail();
        } catch (final RuntimeException e) {
            ;
        }
        List eT = new ArrayList();
        eT.add(PolygonFillerTest.createLine(0, 0, 0, 0));
        fn = PolygonFiller.clipPolygons(
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(eT.iterator())),
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(eT.iterator())), 1, 1);
        try {
            final Object[] oA = (Object[]) fn.fn(1);
            Assert.fail();
        } catch (final RuntimeException e) {
            ;
        }
        eT = new ArrayList();
        eT.add(PolygonFillerTest.createLine(0, 0, 0, 0));
        fn = PolygonFiller.clipPolygons(
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(eT.iterator())),
                PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                        .iteratorGenerator(eT.iterator())), 1, 1);
        final Object[] oA = (Object[]) fn.fn(0);
        // oA[0] = convertNewEdgeListToOldEdgeList((List)oA[0]);
        // oA[1] = convertNewEdgeListToOldEdgeList((List)oA[1]);
        eT = (List) IterationTools.append(
                PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                        .combineEdgeLists(((List) oA[0]).iterator(),
                                ((List) oA[1]).iterator())),
                new ArrayList(((List) oA[0]).size()
                        + ((List) oA[1]).size()));
        // eT = (List) oA[0];
        Assert.assertTrue(eT.size() == 1);
        eT = (List) eT.get(0);
        Assert.assertTrue(eT.size() == 3);
        Assert.assertEquals(eT.get(0), new Integer(0));
        Assert.assertTrue(Arrays.equals((int[]) eT.get(1), new int[3]));
        Assert.assertTrue(Arrays.equals((int[]) eT.get(2), new int[] { 0, 0,
                -1 }));
        Assert.assertTrue(Arrays.equals(new int[8], (int[]) oA[2]));
        try {
            fn.fn(1);
            Assert.fail();
        } catch (final RuntimeException e) {
            ;
        }
    }

    public void testClipPolygon() {
        for (int trial = 0; trial < 1000; trial++) {
            final int minMaxOffset = 1 + (int) (Math.random() * 9);
            final int height = 2 * minMaxOffset
                    + (int) (Math.random() * 100), width = 2
                    * minMaxOffset + (int) (Math.random() * 100);
            final List[] poly = PolygonFillerTest.getRandomPolygon(width, height,
                    minMaxOffset, 0);
            final List lREL = poly[0];
            final List rREL = poly[1];
            final int[][] matrix = new int[height][width];
            List eT = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) IterationTools
                    .append(
                            PolygonFiller
                                    .combineEdgeLists(
                                            PolygonFiller
                                                    .cloneEdgeList(
                                                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(lREL))
                                                    .iterator(),
                                            PolygonFiller
                                                    .cloneEdgeList(
                                                            PolygonFillerTest.convertOldEdgeListToNewEdgeList(rREL))
                                                    .iterator()),
                            new ArrayList()));
            PolygonFillerTest.fillInMatrix(matrix, eT, 1, 0);
            final Function_Index fn = PolygonFiller.clipPolygons(
                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                            .iteratorGenerator(lREL.iterator())),
                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(Generators
                            .iteratorGenerator(rREL.iterator())),
                    width, height);
            final int maxDiagonal = height + width - 2; // inclusive
            {
                int i = -1;
                while (i < maxDiagonal) {
                    int[] dC = this.getDiagonalCoordinates(matrix, i + 1,
                            1);
                    if (dC == null) {
						dC = this.getDiagonalCoordinates(matrix, i + 2, 1);
					}
                    final int iO = i;
                    i += 1 + new Random().nextInt(maxDiagonal - i);
                    int[] dC2 = this.getDiagonalCoordinates(matrix, i, 1);
                    if (dC2 == null) {
						dC2 = this.getDiagonalCoordinates(matrix, i - 1, 1);
					}
                    Object[] oA = null;
                    try {
                        oA = (Object[]) fn.fn(i);
                        oA[0] = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) oA[0]);
                        oA[1] = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) oA[1]);
                    } catch (final IllegalStateException e) {
                        if (iO + 1 == i) {
                            oA = (Object[]) fn.fn(++i);
                            oA[0] = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) oA[0]);
                            oA[1] = PolygonFillerTest.convertNewEdgeListToOldEdgeList((List) oA[1]);
                            dC2 = this.getDiagonalCoordinates(matrix, i, 1);
                            if (dC2 == null) {
								dC2 = this.getDiagonalCoordinates(matrix,
                                        i - 1, 1);
							}
                        } else {
							Assert.fail();
						}
                    }
                    // eT = (List) oA[0];
                    final int[] dC3 = (int[]) oA[2];

                    Assert.assertEquals(dC[2], dC3[0]);
                    Assert.assertEquals(dC[3], dC3[1]);
                    Assert.assertEquals(dC[0], dC3[2]);
                    Assert.assertEquals(dC[1], dC3[3]);

                    Assert.assertEquals(dC2[0], dC3[4]);
                    Assert.assertEquals(dC2[1], dC3[5]);
                    Assert.assertEquals(dC2[2], dC3[6]);
                    Assert.assertEquals(dC2[3], dC3[7]);
                    int m = dC[1];
                    for (int k = dC[0]; k < dC[2]; k++) {
						Assert.assertEquals(matrix[k][m++], 2);
					}
                    eT = (List) IterationTools
                            .append(
                                    PolygonFillerTest.convertNewEdgeListToOldEdgeList(PolygonFiller
                                            .combineEdgeLists(
                                                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(((List) oA[0])
                                                            .iterator()),
                                                    PolygonFillerTest.convertOldEdgeListToNewEdgeList(((List) oA[1])
                                                            .iterator()))),
                                    new ArrayList(((List) oA[0])
                                            .size()
                                            + ((List) oA[1]).size()));
                    PolygonFillerTest.fillInMatrix(matrix, eT, 2, 1);
                    m = dC[1];
                    for (int k = dC[0]; k < dC[2]; k++) {
                        Assert.assertEquals(matrix[k][m++], 2);
                    }
                }
            }
            for (final int[] element : matrix) {
				for (int element0 : element) {
					Assert.assertTrue((element0 == 0) || (element0 == 2));
				}
			}
        }
    }

    int[] getDiagonalCoordinates(final int[][] matrix, final int diagonal,
            final int label) {
        final int[] dC = new int[4];
        int j = 0;
        int i = 0;
        for (; i < matrix.length; i++) {
            if ((diagonal - i >= 0) && (diagonal - i < matrix[i].length)
                    && (matrix[i][diagonal - i] == label)) {
                dC[j++] = diagonal - i;
                dC[j++] = i;
                break;
            }
        }
        for (; i < matrix.length; i++) {
            if ((diagonal - i >= 0) && (diagonal - i < matrix[i].length)
                    && (matrix[i][diagonal - i] != label)) {
                dC[j++] = diagonal - (i - 1);
                dC[j++] = i - 1;
                break;
            }
        }
        if (j == 2) {
            if (diagonal - matrix.length < 0) {
                dC[j++] = 0;
                dC[j++] = diagonal;
            } else {
                dC[j++] = diagonal - (matrix.length - 1);
                dC[j++] = matrix.length - 1;
            }
        }
        return j == 4 ? dC : null;
    }

    public static Generator checkEdge(final Generator generator,
            final PrimeConstraints testConstraints, final int i, final int j, final int iStart, final int jStart) {
        return new Generator() {
            public Object gen() {
                final PolygonFiller.Node n = (PolygonFiller.Node)generator.gen();
                if(n == null) {
					return null;
				}
                Chains.Node m = new Chains.Node(n.x-iStart, n.x-iStart + n.yMax - n.y, n.z, null, null);
                final Chains.Node p = new Chains.Node(n.y-jStart, n.yMax-jStart, n.z, m, null);
                m.n = p;
                final int x = m.xS;
                final int y = m.n.xS;
                final int xMax = m.xE;
                final int yMax = m.n.xE;
                final int z = m.n.score;
                //Debug.pl(m + " ");
                m = testConstraints.filterByConstraints(i, j, m);
                //Debug.pl(m + " ");
                //m = Chains.sortOppositeChain(m);
                //m = testConstraints.filterByConstraints(j, i, m);
                //Debug.pl(m + " ");
                //m = Chains.sortOppositeChain(m);
                if((x != m.xS) || (xMax != m.xE) || (y != m.n.xS) || (yMax != m.n.xE) || (m.score != z)) {
					throw new IllegalStateException();
				}
                return n;
            }
        };
    }

    /**
     * Takes an edge list (see {@link scanPolygon}) and clips the edges to
     * produce the bounds of an enclosed polygon. The input edge list may
     * contain edges that overlap and are disjoint. The produced list of
     * diagonals will not overlap in either X or Y dimensions.
     * 
     * @param pN
     *            (potentially) disjoint edge list
     * @return
     */
    public static Iterator clipUpperDiagonalEdgeList(final Iterator it) {
        return new GeneratorIterator(new Generator() {
            // create current line list
            LinkedList<int[]> cLine = new LinkedList<int[]>();

            // next line holder
            List nLine;

            // initialise xMax, cLineY and nLineY to 0
            // xMax and cLineY are exclusive of the currently finished edges,
            // nLineY is equal to the new line number
            int xMax = 0, nLineY = 0;
            {
                if (it.hasNext()) {
                    this.nLine = (List) it.next();
                    this.nLineY = ((Integer) this.nLine.get(0)).intValue();
                } else {
                    this.nLine = new ArrayList(0);
                    this.nLineY = Integer.MAX_VALUE;
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                // while the current line list is not empty and the
                // the y coordinate end of maximum current edge is less than the
                // index of the
                // new line
                while ((this.cLine.size() != 0)
                        && (this.cLine.getLast()[PolygonFillerTest.YMAX] < this.nLineY)) {
                    // remove the rightmost edge from the line list
                    // update the xMax to the final value of the rightmost x
                    // coordinate of
                    // the removed edge
                    // return the visible portion of the right most edge to the
                    // output
                    // iterator
                    final int[] e = this.cLine.removeLast();
                    final List<Object> l = new ArrayList<Object>(2);
                    l.add(new Integer(e[PolygonFillerTest.YMAX]
                            - e[PolygonFillerTest.XMAX] + this.xMax));
                    final int i = e[PolygonFillerTest.XMAX];
                    e[PolygonFillerTest.X] = this.xMax;
                    this.xMax = i + 1;
                    l.add(e);
                    return l;
                }
                // if rightmost edge in the new line creates a new rightmost
                // edge in the
                // current line and if the
                // current rightmost edge
                // at y-1 has an x coordinate greater than xMax then
                // update xMax and
                // return the visible portion of this right most edge to the
                // output
                // iterator
                if (this.cLine.size() != 0) {
                    final int[] e = (int[]) this.nLine.get(this.nLine.size() - 1);
                    int[] e2 = this.cLine.getLast();
                    final int x = e2[PolygonFillerTest.XMAX]
                            - e2[PolygonFillerTest.YMAX] + this.nLineY;
                    if (x < e[PolygonFillerTest.X]) {
                        if (x > this.xMax) {
                            final List<Object> l = new ArrayList<Object>(2);
                            l
                                    .add(new Integer(
                                            e2[PolygonFillerTest.YMAX]
                                                    - e2[PolygonFillerTest.XMAX]
                                                    + this.xMax));
                            e2 = new int[3];
                            e2[PolygonFillerTest.X] = this.xMax;
                            e2[PolygonFillerTest.YMAX] = this.nLineY - 1;
                            e2[PolygonFillerTest.XINC] = 1;
                            l.add(e2);
                            this.xMax = e[PolygonFillerTest.X];
                            return l;
                        } else if (e[PolygonFillerTest.X] > this.xMax) {
							this.xMax = e[PolygonFillerTest.X];
						}
                    }
                } else {
                    if (this.nLine.size() != 0) {
                        final int[] e = (int[]) this.nLine.get(this.nLine.size() - 1);
                        if (e[PolygonFillerTest.X] > this.xMax) {
							this.xMax = e[PolygonFillerTest.X];
						}
                    }
                }

                for (int i = this.nLine.size() - 1; i > 0; i--) {
                    // for each point in the new list search to find its
                    // insertion
                    // point in
                    // the list
                    // whilst doing so check for each edge whose current x
                    // coordinate is
                    // greater than or equal to its if the maximum x coordinate
                    // of
                    // the new
                    // edge is greater than the maximum x coordinate of the
                    // edge
                    // in the
                    // list, if so discard the edge
                    // when the insertion point is located, insert edge
                    // remove progressively edges leftwoods in the list obscured
                    // by
                    // the
                    // newly inserted edge
                    {
                        final int[] e = (int[]) this.nLine.get(i);
                        final int eXMax = e[PolygonFillerTest.X]
                                + e[PolygonFillerTest.YMAX] - this.nLineY;
                        for (final ListIterator<int[]> it = this.cLine
                                .listIterator(this.cLine.size()); true;) {
                            if (it.hasPrevious()) {
                                int[] e2 = it.previous();
                                if (e2[PolygonFillerTest.XMAX]
                                        - e2[PolygonFillerTest.YMAX]
                                        + this.nLineY < e[PolygonFillerTest.X]) {
                                    if (eXMax >= this.xMax) {
                                        e[PolygonFillerTest.XMAX] = eXMax;
                                        it.next();
                                        it.add(e);
                                        it.previous();
                                        while (it.hasPrevious()) {
                                            e2 = it
                                                    .previous();
                                            if (e2[PolygonFillerTest.XMAX] <= e[PolygonFillerTest.XMAX]) {
												it.remove();
											} else {
												break;
											}
                                        }
                                    }
                                    break;
                                } else if (e2[PolygonFillerTest.XMAX] >= eXMax) {
									break;
								}
                            } else {
                                if (eXMax >= this.xMax) {
                                    e[PolygonFillerTest.XMAX] = eXMax;
                                    this.cLine.addFirst(e);
                                }
                                break;
                            }
                        }
                    }
                }
                // get the next new line
                if (it.hasNext()) {
                    this.nLine = (List) it.next();
                    this.nLineY = ((Integer) this.nLine.get(0)).intValue();
                } else {
                    // if the new line is null
                    if (this.cLine.size() != 0) {
                        // if the new current line list is empty return null
                        // else set the next maximum line index to infinity
                        this.nLineY = Integer.MAX_VALUE;
                        this.nLine.clear();
                    } else {
						return null;
					}
                }
                // call recursively
                return this.gen();
            }
        });
    }

    /**
     * Insert the edge into the sorted list starting from position start.
     * 
     * @param l
     *            the list in which to insert the object
     * @param start
     *            the first position from which to start looking to insert
     *            (inclusive)
     * @param o
     *            the object to insert (an int[] array)
     * @return the position the object was inserted at plus one
     */
    static int inSort(final List<Object> l, final int start, final int currentY, final int[] e) {
        for (int i = start; i < l.size(); i++) {
            final int[] e2 = (int[]) l.get(i);
            if ((e[PolygonFillerTest.X] < e2[PolygonFillerTest.X])
                    || ((e[PolygonFillerTest.X] == e2[PolygonFillerTest.X]) && (e[PolygonFillerTest.X]
                            + e[PolygonFillerTest.XINC]
                            * (e[PolygonFillerTest.YMAX] - currentY) < e2[PolygonFillerTest.X]
                            + e2[PolygonFillerTest.XINC]
                            * (e2[PolygonFillerTest.YMAX] - currentY)))) {
                l.add(i, e);
                return i;
            }
        }
        l.add(e);
        return l.size() - 1;
    }

    /**
     * Scans a column in ascending order and calls scanSpan for each pair of
     * coordinates within the polygon.
     * 
     * @param eT
     *            the edge table
     * @param aET
     *            the active edge table
     * @param y
     *            the y coordinate
     * @param scanSpan
     *            the function to call for pairs of coordinates contained
     *            (inclusive both ends) with the polygon
     */
    static void scanLine(final List eT, final List<Object> aET, final int y,
            final Procedure_Int_2Args scanSpan) {
        // insert edges from ET into aET for which ymin is = y (according to
        // ascending x coordinate)
        if (eT.size() > 0) {
            final List l = (List) eT.get(0);
            if (((Integer) l.get(0)).intValue() == y) {
                eT.remove(0);
                final Iterator it = l.iterator();
                it.next();
                int i = 0;
                for (; it.hasNext();) {
                    final int[] e = (int[]) it.next();
                    i = PolygonFillerTest.inSort(aET, i, y, e);
                }
            }
        }
        // fill column using parity
        for (final Iterator<Object> it = aET.iterator(); it.hasNext();) {
            final int[] e = (int[]) it.next();
            final int[] e2 = (int[]) it.next();
            scanSpan.pro(e[PolygonFillerTest.X],
                    e2[PolygonFillerTest.X]);
        }
        // remove edges from aET for which ymax is = y, now after the
        // computation
        for (final Iterator<Object> it = aET.iterator(); it.hasNext();) {
            final int[] e = (int[]) it.next();
            if (e[PolygonFillerTest.YMAX] == y) {
				it.remove();
			}
        }
        // update x values
        for (final Iterator<Object> it = aET.iterator(); it.hasNext();) {
            final int[] e = (int[]) it.next();
            PolygonFillerTest.updateEdge(e);
        }
        if (Debug.DEBUGCODE) {
            int pX = Integer.MIN_VALUE;
            for (final Iterator<Object> it = aET.iterator(); it.hasNext();) {
                final int[] e = (int[]) it.next();
                if (e[PolygonFillerTest.X] < pX) {
					throw new IllegalStateException();
				}
                pX = e[PolygonFillerTest.X];
            }
        }
    }

    /**
     * Update the edges x coordinate by adding its gradient to it.
     * 
     * @param QueueEdgeList
     */
    static void updateEdge(final int[] e) {
        e[PolygonFillerTest.X] += e[PolygonFillerTest.XINC];
    }

    /**
     * Scans the contents of a complete polygon specified by the given edgeList.
     * For each row the setY function is called with the row number. For each
     * span of x coordinates in a row the spanX function is called.
     * <p>
     * Edge lists must be composed as follows.
     * <ul>
     * <li>Lines must not cross
     * <li>Horizontal edges must not be specified
     * <li>Gradients may be either +1, 0 or -1
     * <li>Lines must be sorted by increasing x coordinate, where their is a
     * tie they must be distinguished by increasing finishing point
     * </ul>
     * 
     * 
     * @param eT
     *            edge table to iterate through (it is modified by iteration)
     * @param spanX
     *            function to call for spans of x, boths end inclusive
     *            coordinates
     * @param setY
     *            function to call to state the next column
     */
    static void scanPolygon(final List eT, final Procedure_Int_2Args spanX,
            final Procedure_Int setY) {
        final List<Object> aET = new LinkedList<Object>();
        if (eT.size() > 0) {
            int y = ((Integer) ((List) eT.get(0)).get(0)).intValue();
            do {
                setY.pro(y);
                PolygonFillerTest.scanLine(eT, aET, y, spanX);
                y++;
            } while ((eT.size() != 0) || (aET.size() != 0));
        }
    }

    /**
     * Spans a pair of coordinates inclusively at both ends.
     * 
     * @param x
     *            start
     * @param x2
     *            finish
     * @param setX
     *            function to call for each x value
     */
    static void scanSpan(int x, final int x2, final Procedure_Int setX) {
        while (x <= x2) {
			setX.pro(x++);
		}
    }

    /**
     * Wrapper for scanSpan.
     * 
     * @param setX
     * @return
     */
    static Procedure_Int_2Args scanSpan(final Procedure_Int setX) {
        return new Procedure_Int_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int_2Args#pro(int, int)
             */
            public void pro(final int i, final int j) {
                PolygonFillerTest.scanSpan(i, j, setX);
            }
        };
    }

    /**
     * Creates a generator which iterates through the spans of a given polygon.
     * A span is a pair of (inclusive) z coordinates which are contained in a
     * row of the interior of the polygon. A z coordinate is a linearisation of
     * a pair of x, y coordinates.
     * 
     * @param eT
     *            edge table to iterate through (is modified)
     * @return
     */
    static Generator getSpanGenerator(final List eT,
            final int rowLength) {
        return new Generator() {
            final List<int[]> l = new LinkedList<int[]>();

            List<Object> aET = new LinkedList<Object>();

            int y = ((Integer) ((List) eT.get(0)).get(0)).intValue(),
                    x0 = rowLength * this.y;

            Procedure_Int_2Args spanX = new Procedure_Int_2Args() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.common.fp.Procedure_Int_2Args#pro(int, int)
                 */
                public void pro(int i, int j) {
                    l.add(new int[] { i + x0, j + x0 });
                }
            };

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if (this.l.size() != 0) {
                    return this.l.remove(0);
                } else if ((eT.size() != 0) || (this.aET.size() != 0)) {
                    PolygonFillerTest.scanLine(eT, this.aET, this.y, this.spanX);
                    this.y++;
                    this.x0 += rowLength;
                    return this.gen();
                } else {
					return null;
				}
            }
        };
    }

    /**
     * Flips the XY coordinates of an edgelist composed only of diagonals.
     * Destroys input list in the process.
     * 
     * @param l
     * @return
     */
    public static void flipXYDiagonalCoordinates(final List l) {
        final SkipList sK = new SkipList();
        for (int i = 0; i < l.size(); i++) {
            final List l2 = (List) l.get(i);
            final int y = ((Integer) l2.get(0)).intValue();
            for (int j = 1; j < l2.size(); j++) {
                final int[] iA = (int[]) l2.get(j);
                final int x = iA[PolygonFillerTest.X];
                List<Object> l3 = (List<Object>) sK.search(iA[PolygonFillerTest.X]);
                if (l3 == null) {
                    l3 = new LinkedList<Object>();
                    l3.add(new Integer(iA[PolygonFillerTest.X]));
                    sK.insert(x, l3);
                }
                iA[PolygonFillerTest.YMAX] = x
                        + iA[PolygonFillerTest.YMAX] - y;
                iA[PolygonFillerTest.X] = y;
                if (Debug.DEBUGCODE
                        && (iA[PolygonFillerTest.XINC] != 1)) {
					throw new IllegalArgumentException();
				}
                PolygonFillerTest.inSort(l3, 1, x, iA);
            }
        }
        l.clear();
        for (final SkipList.Iterator it = sK.iterator(); it.hasNext();) {
			l.add(it.next());
		}
    }

    public static void fillInMatrix(final int[][] matrix,
            final List eT, final int label, final int pLabel) {
        final MutableInteger y = new MutableInteger();
        final MutableInteger inLine = new MutableInteger();
        PolygonFillerTest.scanPolygon(eT, PolygonFillerTest
                .scanSpan(new Procedure_Int() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Procedure_Int#pro(int)
                     */
                    public void pro(final int i) {
                        inLine.i = 0;
                        Assert.assertEquals(matrix[y.i][i], pLabel, 0);
                        matrix[y.i][i] = label;
                    }
                }), new Procedure_Int() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(final int i) {
                Assert.assertEquals(inLine.i, 0);
                inLine.i = 1;
                y.i = i;
            }
        });
    }

    public static void checkPolygon(final List eT, final int label,
            final int pLabel) {
        final MutableInteger y = new MutableInteger(-1);
        final MutableInteger inLine = new MutableInteger();
        final MutableInteger inLine2 = new MutableInteger();
        final MutableInteger pX1 = new MutableInteger(0);
        final MutableInteger pX2 = new MutableInteger(0);
        PolygonFillerTest.scanPolygon(eT, new Procedure_Int_2Args() {
            boolean first = true;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int_2Args#pro(int, int)
             */
            public void pro(final int i, final int j) {
                Assert.assertEquals(inLine2.i, 0);
                inLine.i = 0;
                inLine2.i = 1;
                if (!this.first) {
                    if(!PolygonFillerTest.checkOverlap(pX1.i, pX2.i, i, j)) {
						throw new IllegalStateException(pX1.i + " " + pX2.i + " " + i + " " + j);
					}
                    Assert.assertTrue(PolygonFillerTest.checkOverlap(pX1.i, pX2.i, i, j));
                } else {
					this.first = false;
				}
                pX1.i = i;
                pX2.i = j;
            }
        }, new Procedure_Int() {
            boolean first = true;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Procedure_Int#pro(int)
             */
            public void pro(final int i) {
                Assert.assertEquals(inLine.i, 0);
                if(inLine.i != 0) {
					throw new IllegalStateException();
				}
                inLine.i = 1;
                inLine2.i = 0;
                if (!this.first) {
                    if(y.i + 1 != i) {
						throw new IllegalStateException();
					}
                    Assert.assertEquals(y.i + 1, i);
                } else {
					this.first = false;
				}
                y.i = i;
            }
        });
    }

    static boolean checkOverlap(final int i, final int j, final int k, final int l) {
        return (k >= i-1) && (k <= j + 1) && (l >= k) && (l >= j-1);
    }

}