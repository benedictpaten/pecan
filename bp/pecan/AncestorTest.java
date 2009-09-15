/*
 * Created on Oct 29, 2005
 */
package bp.pecan;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function;
import bp.common.io.NewickTreeParser;

/**
 * @author benedictpaten
 */
public class AncestorTest
                         extends TestCase {
    
    public void testIsAncestorRepeat() {
        
    }

    public void testFelsensteinsMostProbBases_SingleSeq() {
        final NewickTreeParser.Node d = new NewickTreeParser.Node();
        d.edgeLength = Math.random();
        final Ancestor.SubstitutionMatrixGenerator sMG = new Ancestor.SubstitutionMatrixGenerator() {
            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.consistency.Ancestor.SubstitutionMatrixGenerator#getMatrix(double)
             */
            public double[][] getMatrix(double time) {
                return new double[][] { { 1.0 - time, time },
                        { time, 1.0 - time } };
            }
        };

        final Function fn = new Ancestor.FelsensteinsMostProbBases(d, sMG,
                2);
        final float[][] column = new float[1][]; //{  { Math.random() > 0.5 ? 1 : 0 } };
        if(Math.random() > 0.5) {
            column[0] = new float[] { 1, 0 };
        } else {
			column[0] = new float[] { 0, 1 };
		}
        final float[] fA = (float[]) fn.fn(column);

        for (int i = 0; i < 10; i++) {
            final double Aa = column[0][0];
            final double Ab = column[0][1];

            Assert.assertEquals(fA[0], Aa, 0.0);
            Assert.assertEquals(fA[1], Ab, 0.0);
        }
    }

    public void testFelsensteinsMostProbBases() {
        for (int trial = 0; trial < 1000; trial++) {
            final int A = 0;
            final int B = 1;
            final NewickTreeParser.Node d = new NewickTreeParser.Node();
            d.edgeLength = Math.random();
            final NewickTreeParser.Node c = new NewickTreeParser.Node(d);
            c.edgeLength = Math.random();
            final NewickTreeParser.Node a = new NewickTreeParser.Node(c);
            a.edgeLength = Math.random();
            final NewickTreeParser.Node b = new NewickTreeParser.Node(c);
            b.edgeLength = Math.random();
            final NewickTreeParser.Node e = new NewickTreeParser.Node(d);
            e.edgeLength = Math.random();
            final Ancestor.SubstitutionMatrixGenerator sMG = new Ancestor.SubstitutionMatrixGenerator() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see bp.pecan.consistency.Ancestor.SubstitutionMatrixGenerator#getMatrix(double)
                 */
                public double[][] getMatrix(double time) {
                    return new double[][] { { 1.0 - time, time },
                            { time, 1.0 - time } };
                }
            };
            final Function fn = new Ancestor.FelsensteinsMostProbBases(d,
                    sMG, 2);

            for (int i = 0; i < 100; i++) {
                final float[][] column = new float[3][];
                {
                    final float j = (float)Math.random();
                    column[0] = new float[] { j, 1 - j };
                }
                {
                    final float j = (float)Math.random();
                    column[1] = new float[] { j, 1 - j };
                }
                {
                    final float j = (float)Math.random();
                    column[2] = new float[] { j, 1 - j };
                }
                
                final float[] fA = (float[]) fn.fn(column);

                final double Aa = column[0][0];
                final double Ab = column[0][1];
                final double Ba = column[1][0];
                final double Bb = column[1][1];
                final double Ea = column[2][0];
                final double Eb = column[2][1];

                final double ACaa = sMG.getMatrix(a.edgeLength)[A][A];
                final double ACab = sMG.getMatrix(a.edgeLength)[A][B];
                final double ACba = sMG.getMatrix(a.edgeLength)[B][A];
                final double ACbb = sMG.getMatrix(a.edgeLength)[B][B];

                final double BCaa = sMG.getMatrix(b.edgeLength)[A][A];
                final double BCab = sMG.getMatrix(b.edgeLength)[A][B];
                final double BCba = sMG.getMatrix(b.edgeLength)[B][A];
                final double BCbb = sMG.getMatrix(b.edgeLength)[B][B];

                final double CDaa = sMG.getMatrix(c.edgeLength)[A][A];
                final double CDab = sMG.getMatrix(c.edgeLength)[A][B];
                final double CDba = sMG.getMatrix(c.edgeLength)[B][A];
                final double CDbb = sMG.getMatrix(c.edgeLength)[B][B];

                final double EDaa = sMG.getMatrix(e.edgeLength)[A][A];
                final double EDab = sMG.getMatrix(e.edgeLength)[A][B];
                final double EDba = sMG.getMatrix(e.edgeLength)[B][A];
                final double EDbb = sMG.getMatrix(e.edgeLength)[B][B];

                final double Ca = ((Aa * ACaa) * (Ba * BCaa))
                        + ((Aa * ACaa) * (Bb * BCba))
                        + ((Ab * ACba) * (Ba * BCaa))
                        + ((Ab * ACba) * (Bb * BCba));

                final double Cb = ((Aa * ACab) * (Ba * BCab))
                        + ((Aa * ACab) * (Bb * BCbb))
                        + ((Ab * ACbb) * (Ba * BCab))
                        + ((Ab * ACbb) * (Bb * BCbb));

                final double Da = ((Ca * CDaa) * (Ea * EDaa))
                        + ((Ca * CDaa) * (Eb * EDba))
                        + ((Cb * CDba) * (Ea * EDaa))
                        + ((Cb * CDba) * (Eb * EDba));

                final double Db = ((Ca * CDab) * (Ea * EDab))
                        + ((Ca * CDab) * (Eb * EDbb))
                        + ((Cb * CDbb) * (Ea * EDab))
                        + ((Cb * CDbb) * (Eb * EDbb));

                Assert.assertEquals(fA[0], /* DaEdge */Da, 0.00001);
                Assert.assertEquals(fA[1], /* DbEdge */Db, 0.00001);
            }
        }
    }

    public void testF84PlusGaps() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            final double alpha = Math.random();
            final double gamma = Math.random();
            final double[] dA = new double[5];
            double d = 1.0;
            for (int i = 0; i < 5; i++) {
                dA[i] = Math.random() * d;
                d -= dA[i];
            }
            dA[dA.length - 1] += d;
            d = 0;
            for (final double element : dA) {
                d += element;
            }
            final double[][] dAA = Ancestor.f84WithGaps(dA[0], dA[1],
                    dA[2], dA[3], dA[4], alpha, gamma, time);
            this.testMatrix(dAA, 1.0, 5);
        }
    }

    public void testF84() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            final double alpha = Math.random();
            final double gamma = Math.random();
            final double[] dA = new double[4];
            double d = 1.0;
            for (int i = 0; i < 4; i++) {
                dA[i] = Math.random() * d;
                d -= dA[i];
            }
            dA[dA.length - 1] += d;
            d = 0;
            for (final double element : dA) {
                d += element;
            }
            final double[][] dAA = Ancestor.f84(dA[0], dA[1], dA[2],
                    dA[3], alpha, gamma, time);
            this.testMatrix(dAA, 1.0, 4);
        }
    }

    public void testJukesCantorMatrixWithGaps() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            final double lamda = Math.random();
            final double[][] dAA = Ancestor.jukesCantorWithGaps(lamda)
                    .getMatrix(time);
            this.testMatrix(dAA, 1.0, 5);
            for (int i = 0; i < 5; i++) {
                Assert.assertEquals(dAA[i][i], 1.0 / 5.0 + (4.0 / 5.0)
                        * Math.exp(-5.0 * lamda * time), 0.0001);
                for (int j = 0; j < 5; j++) {
					if (i != j) {
						Assert.assertEquals(1.0 / 5.0 - (1.0 / 5.0)
                                * Math.exp(-5.0 * lamda * time),
                                dAA[i][j], 0.0001);
					}
				}
            }
        }
    }

    public void testK2PWithGaps() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            final double transversionRate = Math.random();
            final double transitionRate = transversionRate
                    + (1.0 - transversionRate) * Math.random();
            final double[][] dAA = Ancestor.k2PWithGaps(transitionRate,
                    transversionRate).getMatrix(time);
            this.testMatrix(dAA, 1.0, 5);
        }
    }

    public void testF81WithGaps() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            double gamma = Math.random();
            final double[] dA = new double[5];
            double d = 1.0;
            for (int i = 0; i < 5; i++) {
                dA[i] = Math.random() * d;
                d -= dA[i];
            }
            dA[dA.length - 1] += d;
            final double[][] dAA = Ancestor.f81WithGaps(dA[0], dA[1],
                    dA[2], dA[3], dA[4], gamma).getMatrix(time);
            this.testMatrix(dAA, 1.0, 5);
            for (int i = 0; i < 5; i++) {
                Assert.assertEquals(dAA[i][i], Math.exp(-gamma * time)
                        + (1 - Math.exp(-gamma * time)) * dA[i],
                        0.0001);
                for (int j = 0; j < 5; j++) {
					if (i != j) {
						Assert.assertEquals((1.0 - Math.exp(-gamma * time))
                                * dA[j], dAA[i][j], 0.0001);
					}
				}
            }
        }
    }

    public void testJukesCantorMatrix() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            final double lamda = Math.random();
            final double[][] dAA = Ancestor.jukesCantor(lamda)
                    .getMatrix(time);
            this.testMatrix(dAA, 1.0, 4);
            for (int i = 0; i < 4; i++) {
                Assert.assertEquals(dAA[i][i],
                        1.0 / 4.0 + (3.0 / 4.0)
                                * Math.exp(-4.0 * lamda * time),
                        0.0001);
                for (int j = 0; j < 4; j++) {
					if (i != j) {
						Assert.assertEquals(
                                1.0 / 4.0 - (1.0 / 4.0)
                                        * Math.exp(-4.0 * lamda
                                                * time),
                                dAA[i][j], 0.0001);
					}
				}
            }
        }
    }

    public void testK2P() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            final double transversionRate = Math.random();
            final double transitionRate = transversionRate
                    + (1.0 - transversionRate) * Math.random();
            final double[][] dAA = Ancestor.k2P(transitionRate,
                    transversionRate).getMatrix(time);
            this.testMatrix(dAA, 1.0, 4);
        }
    }

    public void testF81() {
        for (int trial = 0; trial < 1000; trial++) {
            final double time = Math.random() * 5;
            double gamma = Math.random();
            final double[] dA = new double[4];
            double d = 1.0;
            for (int i = 0; i < 4; i++) {
                dA[i] = Math.random() * d;
                d -= dA[i];
            }
            dA[dA.length - 1] += d;
            final double[][] dAA = Ancestor.f81(dA[0], dA[1], dA[2],
                    dA[3], gamma).getMatrix(time);
            this.testMatrix(dAA, 1.0, 4);
            for (int i = 0; i < 4; i++) {
                Assert.assertEquals(dAA[i][i],
                        Math.exp(-gamma * time) + (1 - Math
                                .exp(-gamma * time))
                                * dA[i], 0.0001);
                for (int j = 0; j < 4; j++) {
					if (i != j) {
						Assert.assertEquals(
                                (1.0 - Math.exp(-gamma * time)) * dA[j],
                                dAA[i][j], 0.0001);
					}
				}
            }
        }
    }

    public void testMatrix(final double[][] dAA, final double sumTo, final int matrixDimension) {
        Assert.assertEquals(dAA.length, matrixDimension);
        for (final double[] dA : dAA) {
            Assert.assertEquals(dA.length, matrixDimension);
            double d = 0;
            for (final double element0 : dA) {
                d += element0;
            }
            Assert.assertEquals(d, sumTo, 0.0000001);
        }
    }
}