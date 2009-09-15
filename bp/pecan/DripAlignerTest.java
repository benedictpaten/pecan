/*
 * Created on Mar 24, 2005
 */
package bp.pecan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.SkipList;
import bp.common.ds.wrappers.MutableInteger;
import bp.common.fp.Generator;
import bp.common.fp.GeneratorIterator;
import bp.common.fp.Generator_Int;
import bp.common.fp.Predicate_Double_2Args;
import bp.common.io.Debug;
import bp.pecan.DripAligner.Node;

/**
 * @author benedictpaten
 */
public class DripAlignerTest
                            extends TestCase {
    public DripAlignerTest(final String s) {
        super(s);
    }

    SkipList column, row;

    int[] rowWeights;

    DripAligner.Add adder;

    Predicate_Double_2Args greaterThan;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        this.column = new SkipList();
        this.row = new SkipList();
        final DripAligner.Node goldenNode = new DripAligner.Node(-1, -1,
                Double.longBitsToDouble(0), null);
        final DripAligner.Node n = new DripAligner.Node(3, 10, Double
                .longBitsToDouble(6), new DripAligner.Node(2,
                7, Double.longBitsToDouble(4),
                new DripAligner.Node(1, 2, Double
                        .longBitsToDouble(2),
                        new DripAligner.Node(0, 1, Double
                                .longBitsToDouble(1),
                                goldenNode))));
        final DripAligner.Node m = new DripAligner.Node(3, 8, Double
                .longBitsToDouble(5), n.n);
        final DripAligner.Node p = new DripAligner.Node(3, 5, Double
                .longBitsToDouble(4), new DripAligner.Node(2,
                3, Double.longBitsToDouble(3), n.n.n));
        final DripAligner.Node q = new DripAligner.Node(5, 4, Double
                .longBitsToDouble(4), p.n);
        final DripAligner.Node r = new DripAligner.Node(9, 4, Double
                .longBitsToDouble(5), q.n);
        this.column.insert(-1, goldenNode);
        this.column.insert(10, n);
        this.column.insert(8, m);
        this.column.insert(5, p);
        this.column.insert(3, p.n);
        this.column.insert(2, p.n.n);
        this.column.insert(1, p.n.n.n);
        this.row.insert(-1, goldenNode);
        this.row.insert(9, r);
        this.row.insert(5, q);
        this.rowWeights = new int[] { 6, 2, 4, 1, 11, 2, 10, 2 };
        this.adder = new DripAligner.Add() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.DripAligner.Add#fn(double, int)
             */
            public double fn(final double d, final int i) {
                return Double.longBitsToDouble(Double
                        .doubleToLongBits(d)
                        + i);
            }
        };
        this.greaterThan = new Predicate_Double_2Args() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Predicate_Int_2Args#test(int, int)
             */
            public boolean test(final double i, final double j) {
                return Double.doubleToLongBits(i) > Double
                        .doubleToLongBits(j);
            }
        };
    }

    boolean equal(final DripAligner.Node n, final int x, final int y, final double score,
            final int z) {
        return (n.x == x) && (n.y == y) && (n.score == score) && (n.z == z);
    }

    public void testSwitchBackPointers() {
        DripAligner.Node n = (DripAligner.Node) this.column
                .searchLessThan(Integer.MAX_VALUE);
        n = DripAligner.switchBackPointers(n);
        Assert.assertTrue(this.equal(n, -1, -1,
                Double.longBitsToDouble(0), 0));
        Assert.assertTrue(this.equal(n.n, 0, 1,
                Double.longBitsToDouble(1), 0));
        Assert.assertTrue(this.equal(n.n.n, 1, 2, Double
                .longBitsToDouble(2), 0));
        Assert.assertTrue(this.equal(n.n.n.n, 2, 7, Double
                .longBitsToDouble(4), 0));
        Assert.assertTrue(this.equal(n.n.n.n.n, 3, 10, Double
                .longBitsToDouble(6), 0));
        Assert.assertEquals(n.n.n.n.n.n, null);
        n = new DripAligner.Node(1, 2, Double
                .longBitsToDouble(3), null);
        n = DripAligner.switchBackPointers(n);
        Assert.assertTrue(this.equal(n, 1, 2, Double.longBitsToDouble(3),
                0));
        Assert.assertEquals(n.n, null);
        try {
            DripAligner.switchBackPointers(null);
            Assert.fail();
        } catch (final RuntimeException e) {
            ;
        }
    }

    public void testTraceBackAndLabel() {
        final DripAligner.Node n = (DripAligner.Node) this.column
                .searchLessThan(Integer.MAX_VALUE);
        final DripAligner.Node m = (DripAligner.Node) this.column
                .searchLessThan(n.y);
        final DripAligner.Node p = (DripAligner.Node) this.column
                .searchLessThan(m.y);
        Assert.assertEquals(DripAligner.traceBackAndLabel(p, 1, 2), null);
        Assert.assertTrue(this.equal(p, 3, 5, Double.longBitsToDouble(4),
                1));
        Assert.assertTrue(this.equal(p.n, 2, 3,
                Double.longBitsToDouble(3), 1));
        Assert.assertTrue(this.equal(p.n.n, 1, 2, Double
                .longBitsToDouble(2), 1));
        Assert.assertTrue(this.equal(p.n.n.n, 0, 1, Double
                .longBitsToDouble(1), 1));
        Assert.assertTrue(this.equal(p.n.n.n.n, -1, -1, Double
                .longBitsToDouble(0), 1));
        Assert.assertEquals(p.n.n.n.n.n, null);
        final DripAligner.Node q = DripAligner.traceBackAndLabel(m, 2, 1);
        Assert.assertTrue(this.equal(q, 1, 2, Double.longBitsToDouble(2),
                1));
        Assert.assertTrue(this.equal(q.n, 0, 1,
                Double.longBitsToDouble(1), 1));
        Assert.assertTrue(this.equal(q.n.n, -1, -1, Double
                .longBitsToDouble(0), 1));
        Assert.assertEquals(q.n.n.n, null);
        Assert.assertTrue(this.equal(m, 3, 8, Double.longBitsToDouble(5),
                2));
        Assert.assertTrue(this.equal(m.n, 2, 7,
                Double.longBitsToDouble(4), 2));
        Assert.assertTrue(this.equal(m.n.n, 1, 2, Double
                .longBitsToDouble(2), 1));
        Assert.assertEquals(DripAligner.traceBackAndLabel(n, 2, 1), null);
    }

    public void testGetAlignment() {
        final DripAligner.Node n = DripAligner.getAlignment(this.row, this.column);
        Assert.assertTrue(this.equal(n, 0, 1, Double.longBitsToDouble(1),
                1));
        Assert.assertTrue(this.equal(n.n, 1, 2,
                Double.longBitsToDouble(2), 1));
        Assert.assertEquals(n.n.n, null);
        DripAligner.Node m = (DripAligner.Node) this.column
                .searchLessThan(Integer.MAX_VALUE);
        Assert.assertTrue(this.equal(m, 3, 10, Double.longBitsToDouble(6),
                2));
        Assert.assertTrue(this.equal(m.n, 2, 7,
                Double.longBitsToDouble(4), 2));
        Assert.assertTrue(this.equal(m.n.n, 1, 2, Double
                .longBitsToDouble(2), 1));
        Assert.assertEquals(m.n.n.n, null);
        m = (DripAligner.Node) this.row.searchLessThan(Integer.MAX_VALUE);
        Assert.assertTrue(this.equal(m, 9, 4, Double.longBitsToDouble(5),
                1));
        Assert.assertTrue(this.equal(m.n, 2, 3,
                Double.longBitsToDouble(3), 1));
        Assert.assertTrue(this.equal(m.n.n, 1, 2, Double
                .longBitsToDouble(2), 1));
        Assert.assertEquals(m.n.n.n, null);
        Assert.assertEquals(DripAligner.getAlignment(this.row, this.column), null);
        Assert.assertTrue(this.equal(n, 0, 1, Double.longBitsToDouble(1),
                1));
        Assert.assertTrue(this.equal(n.n, 1, 2,
                Double.longBitsToDouble(2), 1));
        Assert.assertEquals(n.n.n, null);
        m = (DripAligner.Node) this.column
                .searchLessThan(Integer.MAX_VALUE);
        Assert.assertTrue(this.equal(m, 3, 10, Double.longBitsToDouble(6),
                2));
        Assert.assertTrue(this.equal(m.n, 2, 7,
                Double.longBitsToDouble(4), 2));
        Assert.assertTrue(this.equal(m.n.n, 1, 2, Double
                .longBitsToDouble(2), 1));
        Assert.assertEquals(m.n.n.n, null);
        m = (DripAligner.Node) this.row.searchLessThan(Integer.MAX_VALUE);
        Assert.assertTrue(this.equal(m, 9, 4, Double.longBitsToDouble(5),
                1));
        Assert.assertTrue(this.equal(m.n, 2, 3,
                Double.longBitsToDouble(3), 1));
        Assert.assertTrue(this.equal(m.n.n, 1, 2, Double
                .longBitsToDouble(2), 1));
        Assert.assertEquals(m.n.n.n, null);
    }

    public void testDoLineAndSyncLine() {
        final DripAligner.GetFocusCoordinate fC = new DripAligner.GetFocusCoordinate() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.DripAligner.GetFocusCoordinate#get(bp.pecan.consistency.DripAligner.Node)
             */
            public int get(Node n) {
                return n.x;
            }
        };
        DripAligner.doLine(this.row, this.rowWeights, 5, 3,
                new DripAligner.GenerateNode() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.pecan.consistency.DripAligner.GenerateNode#gen(int,
                     *      int, int, bp.pecan.consistency.DripAligner.Node)
                     */
                    public Node gen(final int i, final int j, final double score,
                            final Node pN) {
                        return new DripAligner.Node(i, j, score, pN);
                    }
                }, fC, this.adder, this.greaterThan);
        DripAligner
                .syncWithOtherLine(this.row, this.column, 5, fC, this.greaterThan);
        DripAligner.Node n = (DripAligner.Node) this.row.search(3);
        Assert.assertTrue(this.equal(n, 3, 5, Double.longBitsToDouble(4),
                0));
        Assert.assertTrue(this.equal(n.n, 2, 3,
                Double.longBitsToDouble(3), 0));
        n = (DripAligner.Node) this.row.search(6);
        Assert.assertTrue(this.equal(n, 6, 5, Double.longBitsToDouble(6),
                0));
        Assert.assertTrue(this.equal(n.n, 5, 4,
                Double.longBitsToDouble(4), 0));
        n = (DripAligner.Node) this.row.search(10);
        Assert.assertTrue(this.equal(n, 10, 5, Double.longBitsToDouble(7),
                0));
        Assert.assertTrue(this.equal(n.n, 9, 4,
                Double.longBitsToDouble(5), 0));
        int i = 0;
        for (final SkipList.Iterator it = this.row.iterator(); it.hasNext();) {
            it.next();
            i++;
        }
        Assert.assertEquals(i, 3);
    }

    public void testInputLineProcedure() {
        final Generator_Int linePro = DripAligner.inputLineProcedure(this.row,
                new MutableInteger(), this.column, new MutableInteger(3),
                new Generator() {
                    List<int[]> l = new ArrayList<int[]>();
                    {
                        this.l.add(new int[] {});
                        this.l.add(new int[] {});
                        this.l.add(new int[] {});
                        this.l.add(new int[] {});
                        this.l.add(new int[] {});
                        this.l.add(DripAlignerTest.this.rowWeights);
                        this.l.add(null);
                        this.l.add(new int[] { 5, 10 });
                        this.l.add(null);
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.common.fp.Generator#gen()
                     */
                    public Object gen() {
                        return this.l.remove(0);
                    }
                }, new DripAligner.GenerateNode() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.pecan.consistency.DripAligner.GenerateNode#gen(int,
                     *      int, int, bp.pecan.consistency.DripAligner.Node)
                     */
                    public Node gen(int i, int j, double score,
                            Node pN) {
                        return new DripAligner.Node(i, j, score, pN);
                    }
                }, new DripAligner.GetFocusCoordinate() {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see bp.pecan.consistency.DripAligner.GetFocusCoordinate#get(bp.pecan.consistency.DripAligner.Node)
                     */
                    public int get(Node n) {
                        return n.x;
                    }
                }, this.adder, this.greaterThan);
        Assert.assertEquals(linePro.gen(), 6);
        DripAligner.Node n = (DripAligner.Node) this.row.search(3);
        Assert.assertTrue(this.equal(n, 3, 5, Double.longBitsToDouble(4),
                0));
        Assert.assertTrue(this.equal(n.n, 2, 3,
                Double.longBitsToDouble(3), 0));
        n = (DripAligner.Node) this.row.search(6);
        Assert.assertTrue(this.equal(n, 6, 5, Double.longBitsToDouble(6),
                0));
        Assert.assertTrue(this.equal(n.n, 5, 4,
                Double.longBitsToDouble(4), 0));
        n = (DripAligner.Node) this.row.search(10);
        Assert.assertTrue(this.equal(n, 10, 5, Double.longBitsToDouble(7),
                0));
        Assert.assertTrue(this.equal(n.n, 9, 4,
                Double.longBitsToDouble(5), 0));
        int i = 0;
        for (final SkipList.Iterator it = this.row.iterator(); it.hasNext();) {
            it.next();
            i++;
        }
        Assert.assertEquals(i, 3);
        Assert.assertEquals(linePro.gen(), 7);
        n = (DripAligner.Node) this.row.search(3);
        Assert.assertTrue(this.equal(n, 3, 5, Double.longBitsToDouble(4),
                0));
        Assert.assertTrue(this.equal(n.n, 2, 3,
                Double.longBitsToDouble(3), 0));
        n = (DripAligner.Node) this.row.search(5);
        Assert.assertTrue(this.equal(n, 5, 6, Double.longBitsToDouble(14),
                0));
        Assert.assertTrue(this.equal(n.n, 3, 5,
                Double.longBitsToDouble(4), 0));
        i = 0;
        for (final SkipList.Iterator it = this.row.iterator(); it.hasNext();) {
            it.next();
            i++;
        }
        Assert.assertEquals(i, 2);
    }

    public void testTranslateNodes() {
        final DripAligner dA = new DripAligner(null, null, null, null, 0);
        final DripAligner.Node silentNode = new DripAligner.Node(5, 5,
                Double.longBitsToDouble(4),
                new DripAligner.Node(6, 6, Double
                        .longBitsToDouble(4),
                        new DripAligner.Node(7, 8, Double
                                .longBitsToDouble(6), null)));
        silentNode.z = DripAligner.SILENTBIT;
        silentNode.n.z = DripAligner.SILENTBIT;
        DripAligner.Node n = new DripAligner.Node(0, 1, Double
                .longBitsToDouble(1), new DripAligner.Node(2,
                2, Double.longBitsToDouble(2),
                new DripAligner.Node(4, 4, Double
                        .longBitsToDouble(3),
                        new DripAligner.Node(5, 5, Double
                                .longBitsToDouble(4),
                                silentNode))));
        Generator gen = DripAlignerTest.convertBackToOldOutput(dA.translateNodes(n));
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { false, true })); // -, 0
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, true })); // 0, 1
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, false })); // 1, -
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, true })); // 2, 2
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, false })); // 3, -
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { false, true })); // -, 3
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, true })); // 4, 4
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, true })); // 5, 5
        // silent node ignored
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, false })); // 6, -
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { false, true })); // -, 6
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { false, true })); // -, 7
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, true })); // 7, 8
        n = new DripAligner.Node(8, 10, Double
                .longBitsToDouble(1), null);
        gen = DripAlignerTest.convertBackToOldOutput(dA.translateNodes(n));
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { false, true })); // -, 9
        Assert.assertTrue(Arrays.equals((boolean[]) gen.gen(),
                new boolean[] { true, true })); // 8, 10
    }

    public int[][] getMatrix(final int rowNumber, final int columnNumber,
            final double sparsePercentage) {
        final int[][] m = new int[rowNumber][columnNumber];
        for (final int[] element : m) {
			for (int j = 0; j < element.length; j++) {
				if (Math.random() > sparsePercentage) {
					element[j] = (int) (100 * Math.random());
				}
			}
		}
        return m;
    }

    public Generator matrixGenerator(final int[][] matrix,
            final double continueProb) {
        return new Generator() {
            int i = 0;

            /*
             * (non-Javadoc)
             * 
             * @see bp.common.fp.Generator#gen()
             */
            public Object gen() {
                if ((Math.random() <= continueProb)
                        && (this.i < matrix.length)) {
                    int k = 0;
                    for (int j = 0; j < matrix[this.i].length; j++) {
                        if (matrix[this.i][j] > 0) {
							k++;
						}
                    }
                    final int[] weights = new int[k * 2];
                    k = 0;
                    for (int j = 0; j < matrix[this.i].length; j++) {
                        if (matrix[this.i][j] > 0) {
                            weights[k * 2] = j;
                            weights[k * 2 + 1] = matrix[this.i][j];
                            k++;
                        }
                    }
                    this.i++;
                    return weights;
                } else {
					return null;
				}
            }
        };
    }

    public int[][] matrixTranspose(final int[][] m, final int rowNumber,
            final int columnNumber) {
        final int[][] n = new int[columnNumber][rowNumber];
        for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
                n[j][i] = m[i][j];
            }
		}
        return n;
    }

    public Iterator<boolean[]> getWeightList(final int[][] matrix, final int rowNumber,
            final int columnNumber) {
        final int[][] traceBack = new int[rowNumber + 1][columnNumber + 1];
        final int[][] activeMatrix = new int[rowNumber + 1][columnNumber + 1];
        Arrays.fill(traceBack[0], 2);
        traceBack[0][0] = Integer.MAX_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int bestScore = activeMatrix[i + 1][j], bestNode = 2;
                if (activeMatrix[i][j + 1] > bestScore) {
                    bestScore = activeMatrix[i][j + 1];
                    bestNode = 0;
                }
                if ((matrix[i][j] > 0)
                        && (matrix[i][j] + activeMatrix[i][j] > bestScore)) {
                    bestScore = matrix[i][j] + activeMatrix[i][j];
                    bestNode = 1;
                }
                activeMatrix[i + 1][j + 1] = bestScore;
                traceBack[i + 1][j + 1] = bestNode;
            }
        }
        {
            int i = traceBack.length - 1, j = traceBack[0].length - 1;
            final ArrayList<boolean[]> l = new ArrayList<boolean[]>();
            while (traceBack[i][j] != Integer.MAX_VALUE) {
                switch (traceBack[i][j]) {
                case 0:
                    l.add(new boolean[] { false, true });
                    i--;
                    break;
                case 1:
                    l.add(new boolean[] { true, true });
                    i--;
                    j--;
                    break;
                case 2:
                    l.add(new boolean[] { true, false });
                    j--;
                    break;
                }
            }
            Collections.reverse(l);
            return l.iterator();
        }
    }

    int score(final Iterator<boolean[]> it, final int[][] matrix) {
        int i = 0, j = 0, score = 0;
        while (it.hasNext()) {
            final boolean[] bA = it.next();
            try {
                if (Arrays.equals(bA, new boolean[] { true, true })) {
					score += matrix[j][i];
				}
            } catch (final ArrayIndexOutOfBoundsException e) {
                throw e;
            }
            if (bA[0] == true) {
				i++;
			}
            if (bA[1] == true) {
				j++;
			}
        }
        return score;
    }

    int[] length(final Iterator<boolean[]> it) {
        int i = 0, j = 0;
        while (it.hasNext()) {
            final boolean[] bA = it.next();
            if (bA[0] == true) {
				i++;
			}
            if (bA[1] == true) {
				j++;
			}
        }
        return new int[] { i, j };
    }

    public void testDripAligner() {
        for (int trials = 0; trials < 1000; trials++) {
            final int rowNumber = (int) (100 * Math.random()), columnNumber = (int) (100 * Math
                    .random());
            final int[][] matrix = this.getMatrix(rowNumber, columnNumber, Math
                    .random());
            /*
             * int rowNumber = 3, columnNumber = 68; int[][] matrix = new
             * int[rowNumber][columnNumber]; matrix[0][46] = 96; matrix[0][59] =
             * 65; matrix[2][19] = 96; matrix[2][23] = 8; matrix[2][30] = 60;
             * matrix[2][35] = 20; matrix[2][43] = 34;
             */
            final Generator dA = new DripAligner(
                    this.matrixGenerator(this.matrixTranspose(matrix,
                            rowNumber, columnNumber), Math.random()),
                    this.matrixGenerator(matrix, Math.random()), this.adder,
                    this.greaterThan, 0);
            final List<boolean[]> l = new ArrayList<boolean[]>();
            Object o;
            {
                int row = 0, column = 0;
                while ((row < rowNumber) || (column < columnNumber)) {
                    if ((o = dA.gen()) != null) {
						for (final Iterator it = new GeneratorIterator(
                                DripAlignerTest.convertBackToOldOutput((Generator) o)); it.hasNext();) {
                            final boolean[] bA = (boolean[]) it.next();
                            if (bA[0] == true) {
								column++;
							}
                            if (bA[1] == true) {
								row++;
							}
                            l.add(bA);
                        }
					}
                }
            }
            for (int j = 0; j < 10000; j++) {
                Assert.assertEquals(dA.gen(), null);
            }
            final int score1 = this.score(this.getWeightList(matrix, rowNumber,
                    columnNumber), matrix);
            final int score2 = this.score(l.iterator(), matrix);
            if (score1 != score2) {
            }
            Assert.assertEquals(score1, score2);
            final int[] lengths1 = this.length(this.getWeightList(matrix, rowNumber,
                    columnNumber));
            final int[] lengths2 = this.length(l.iterator());
            Assert.assertTrue(Arrays.equals(lengths1, lengths2));
        }
    }

    public static Generator convertBackToOldOutput(final Generator gen) {
        return new Generator() {
            public Object gen() {
                final Object o = gen.gen();
                if (!(o instanceof Float) && (o != null)) {
                    Debug.pl(" shit ");
                }
                final Float f = (Float) o;
                if (f != null) {
                    if (f.floatValue() == Float.POSITIVE_INFINITY) {
						return new boolean[] { true, false };
					}
                    if (f.floatValue() == Float.NEGATIVE_INFINITY) {
						return new boolean[] { false, true };
					}
                    return new boolean[] { true, true };
                }
                return null;
            }
        };
    }
}