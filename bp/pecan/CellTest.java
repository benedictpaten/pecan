/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on May 30, 2005
 */
package bp.pecan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.fp.Function_Int;
import bp.common.fp.Functions_Int;
import bp.common.io.ExternalExecution;
import bp.common.io.NewickTreeParser;
import bp.common.maths.Maths;

/**
 * @author benedictpaten
 */
public class CellTest
                     extends TestCase {
    

  
    
    

    final float p0 = Maths.log(0), p1 = Maths.log((float) 0.1),
            p2 = Maths.log((float) 0.2), p3 = Maths.log((float) 0.3),
            p4 = Maths.log((float) 0.4), p5 = Maths.log((float) 0.5),
            p6 = Maths.log((float) 0.6), p7 = Maths.log((float) 0.7),
            p8 = Maths.log((float) 0.8), p9 = Maths.log((float) 0.9),
            p10 = Maths.log((float) 1.0);

    final String randomHMM = "( (c, 'Gg'), (" + this.p5 + ", " + this.p2
            + ", " + this.p3 + ", '" + this.p0 + "'), (" + this.p7 + ", " + this.p1
            + ", " + this.p1 + "," + this.p1 + " )," + "( (0, " + this.p1
            + "), (1, " + this.p7 + "), (2, " + this.p2 + "), 2, (" + this.p2 + ", "
            + this.p2 + ", " + this.p2 + ", " + this.p4 + "))," + "((0, " + this.p5
            + "), (1, " + this.p5 + "), 1, (" + this.p6 + ", " + this.p4 + " )),"
            + "((2, " + this.p8 + "), (0, " + this.p1 + ")," + "(3, " + this.p1
            + "), 3, (" + this.p9 + ", " + this.p1 + "))," + "((2, " + this.p10
            + "), 0, (" + this.p10 + "))" + ");";

    NewickTreeParser.Node n;

    /**
     *  
     */
    @Override
	protected void setUp() throws Exception {
        final NewickTreeParser p = new NewickTreeParser(NewickTreeParser
                .tokenise(new StringReader(this.randomHMM)));
        this.n = p.tree;
    }
    
    
    public void testThreeState_Forwards() throws IOException {
        NewickTreeParser nTP;
        {
            final String s = ExternalExecution.getAbsolutePath("bp/pecan/3State3.hmm.trained");
            final Reader r = new BufferedReader(new FileReader(s));
            nTP = new NewickTreeParser(NewickTreeParser
                    .commentEater(NewickTreeParser.tokenise(r)));
            r.close();
        }
        Cell.isLegitimateHMM(nTP.tree);
        final Object[] program = Cell.createProgram(nTP.tree,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        final float[] startStates = (float[]) program[Cell.STARTSTATES];
        final int stateNumber = startStates.length;
        final int alphabetSize = ((Number) program[Cell.ALPHABETSIZE])
                .intValue();
        final float[] emissions = (float[]) program[Cell.EMISSIONS];
        final int[] programForward = (int[]) program[Cell.PROGRAM];
        final float[] transitionsForward = (float[]) program[Cell.TRANSITIONS];
        final Cell.CellCalculator forwardsLL = Cell.nextSumForward(
                programForward, transitionsForward,
                emissions, alphabetSize, Functions_Int.doNothing(), Functions_Int.doNothing());
        final Cell.CellCalculator forwardsBC = new Cell.ThreeCell().forwards(stateNumber, stateNumber*2, stateNumber*3, Functions_Int.doNothing(), Functions_Int.doNothing());
        for(int trial=0; trial<1000; trial++) {
            final int x = (int)(Math.random()*5);
            final int y = (int)(Math.random()*5);
            final float[] cells1 = new float[stateNumber*4];
            for(int i=stateNumber; i<cells1.length; i++) {
                cells1[i] = (float)0.5; //Math.random();
            }
            final float[] cells2 = cells1.clone();
            forwardsLL.calc(cells1, 0, x, y);
            forwardsBC.calc(cells2, 0, x, y);
            for(int i=0; i<stateNumber; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.001);
			}
            for(int i=stateNumber; i<cells1.length; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.0);
			}
        }
    }
    
    public void testThreeState_Backwards() throws IOException {
        NewickTreeParser nTP;
        {
            final String s = ExternalExecution.getAbsolutePath("bp/pecan/3State3.hmm.trained");
            final Reader r = new BufferedReader(new FileReader(s));
            nTP = new NewickTreeParser(NewickTreeParser
                    .commentEater(NewickTreeParser.tokenise(r)));
            r.close();
        }
        Cell.isLegitimateHMM(nTP.tree);
        final Object[] program = Cell.createProgram(nTP.tree,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        final float[] startStates = (float[]) program[Cell.STARTSTATES];
        final int stateNumber = startStates.length;
        final int alphabetSize = ((Number) program[Cell.ALPHABETSIZE])
                .intValue();
        final float[] emissions = (float[]) program[Cell.EMISSIONS];
        final int[] programForward = (int[]) program[Cell.PROGRAM];
        final float[] transitionsForward = (float[]) program[Cell.TRANSITIONS];
        final Object[] oA = Cell.makeRProgram(programForward,
                transitionsForward, stateNumber);
        final int[] programBackward = (int[]) oA[0];
        final float[] transitionsBackward = (float[]) oA[1];
        final Cell.GetCellCalculator getBackwardsLL = Cell.getBackwardCellCalculator(
                programBackward, transitionsBackward, emissions,
                startStates.length, alphabetSize);
        final Cell.CellCalculator backwardsLL = getBackwardsLL.getCellCalculator(Functions_Int.doNothing(), Functions_Int.doNothing());
        final Cell.CellCalculator backwardsBC = new Cell.ThreeCell().backwards(stateNumber, stateNumber*2, stateNumber*3, Functions_Int.doNothing(), Functions_Int.doNothing());
        for(int trial=0; trial<1000; trial++) {
            final int x = (int)(Math.random()*5);
            final int y = (int)(Math.random()*5);
            final float[] cells1 = new float[stateNumber*4];
            for(int i=stateNumber; i<cells1.length; i++) {
                cells1[i] = (float)Math.random();
            }
            final float[] cells2 = cells1.clone();
            backwardsLL.calc(cells1, 0, x-1, y-1);
            backwardsBC.calc(cells2, 0, x-1, y-1);
            for(int i=0; i<stateNumber; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.005);
			}
            for(int i=stateNumber; i<cells1.length; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.0);
			}
        }
    }
    
    public void testFiveState_Backwards() throws IOException {
        NewickTreeParser nTP;
        {
            final String s = ExternalExecution.getAbsolutePath("bp/pecan/5State3.hmm.trained");
            final Reader r = new BufferedReader(new FileReader(s));
            nTP = new NewickTreeParser(NewickTreeParser
                    .commentEater(NewickTreeParser.tokenise(r)));
            r.close();
        }
        Cell.isLegitimateHMM(nTP.tree);
        final Object[] program = Cell.createProgram(nTP.tree,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        final float[] startStates = (float[]) program[Cell.STARTSTATES];
        final int stateNumber = startStates.length;
        final int alphabetSize = ((Number) program[Cell.ALPHABETSIZE])
                .intValue();
        final float[] emissions = (float[]) program[Cell.EMISSIONS];
        final int[] programForward = (int[]) program[Cell.PROGRAM];
        final float[] transitionsForward = (float[]) program[Cell.TRANSITIONS];
        final Object[] oA = Cell.makeRProgram(programForward,
                transitionsForward, stateNumber);
        final int[] programBackward = (int[]) oA[0];
        final float[] transitionsBackward = (float[]) oA[1];
        final Cell.GetCellCalculator getBackwardsLL = Cell.getBackwardCellCalculator(
                programBackward, transitionsBackward, emissions,
                startStates.length, alphabetSize);
        final Cell.CellCalculator backwardsLL = getBackwardsLL.getCellCalculator(Functions_Int.doNothing(), Functions_Int.doNothing());
        final Cell.CellCalculator backwardsBC = new Cell.FiveCell().backwards(stateNumber, stateNumber*2, stateNumber*3, Functions_Int.doNothing(), Functions_Int.doNothing());
        for(int trial=0; trial<1000; trial++) {
            final int x = (int)(Math.random()*5);
            //int y = x;
            final int y = (int)(Math.random()*5);
            final float[] cells1 = new float[stateNumber*4];
            for(int i=stateNumber; i<cells1.length; i++) {
                cells1[i] = (float)Math.random();
            }
            final float[] cells2 = cells1.clone();
            backwardsLL.calc(cells1, 0, x-1, y-1);
            backwardsBC.calc(cells2, 0, x-1, y-1);
            for(int i=0; i<stateNumber; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.005);
			}
            for(int i=stateNumber; i<cells1.length; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.0);
			}
        }
    }
    
    public void testFiveState_Forwards() throws IOException {
        NewickTreeParser nTP;
        {
            final String s = ExternalExecution.getAbsolutePath("bp/pecan/5State3.hmm.trained");
            final Reader r = new BufferedReader(new FileReader(s));
            nTP = new NewickTreeParser(NewickTreeParser
                    .commentEater(NewickTreeParser.tokenise(r)));
            r.close();
        }
        Cell.isLegitimateHMM(nTP.tree);
        final Object[] program = Cell.createProgram(nTP.tree,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        final float[] startStates = (float[]) program[Cell.STARTSTATES];
        final int stateNumber = startStates.length;
        final int alphabetSize = ((Number) program[Cell.ALPHABETSIZE])
                .intValue();
        final float[] emissions = (float[]) program[Cell.EMISSIONS];
        final int[] programForward = (int[]) program[Cell.PROGRAM];
        final float[] transitionsForward = (float[]) program[Cell.TRANSITIONS];
        final Cell.CellCalculator forwardsLL = Cell.nextSumForward(
                programForward, transitionsForward,
                emissions, alphabetSize, Functions_Int.doNothing(), Functions_Int.doNothing());
        final Cell.CellCalculator forwardsBC = new Cell.FiveCell().forwards(stateNumber, stateNumber*2, stateNumber*3, Functions_Int.doNothing(), Functions_Int.doNothing());
        for(int trial=0; trial<1000; trial++) {
            final int x = (int)(Math.random()*5);
            final int y = (int)(Math.random()*5);
            final float[] cells1 = new float[stateNumber*4];
            for(int i=stateNumber; i<cells1.length; i++) {
                cells1[i] = (float)0.5; //Math.random();
            }
            final float[] cells2 = cells1.clone();
            forwardsLL.calc(cells1, 0, x, y);
            forwardsBC.calc(cells2, 0, x, y);
            for(int i=0; i<stateNumber; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.001);
			}
            for(int i=stateNumber; i<cells1.length; i++) {
				Assert.assertEquals(cells1[i], cells2[i], 0.0);
			}
        }
    }

    public void testIsLegitimateStateMachine() throws Exception {
        this.setUp();
        Cell.isLegitimateHMM(this.n);
        Cell.isLegitimateHMM(this.n);

        //add non-legal regex
        NewickTreeParser.Node m = (NewickTreeParser.Node) this.n.getNodes()
                .get(0);//1);
        m = (NewickTreeParser.Node) m.getNodes().get(0);
        m.o = "[A]T";
        this.shouldntWork();
        this.setUp();

        //alter start prob
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(1);
        m = (NewickTreeParser.Node) m.getNodes().get(0);
        m.o = "" + this.p6;
        this.shouldntWork();
        this.setUp();

        //alter size of start prob array
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(1);
        m.removeNode(m.getNodes().size() - 1); //prob of final node is 0
        this.shouldntWork();
        this.setUp();

        //alter end prob
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(2);
        m = (NewickTreeParser.Node) m.getNodes().get(0);
        m.o = "" + this.p4;
        this.shouldntWork();
        this.setUp();

        //alter size of end prob array
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(2);
        NewickTreeParser.Node p = new NewickTreeParser.Node();
        p.o = "" + this.p0;
        m.addNode(p); //prob of final node is 0
        this.shouldntWork();
        this.setUp();

        //alter connection
        //non-existant state
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(0);
        p = (NewickTreeParser.Node) m.getNodes().get(0);
        p.o = "" + -1;
        this.shouldntWork();
        this.setUp();

        //alter connection
        //non-existant state
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(0);
        p = (NewickTreeParser.Node) m.getNodes().get(0);
        p.o = this.p3 + "";
        this.shouldntWork();
        this.setUp();

        //alter connection
        //illegal probability
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(0);
        p = (NewickTreeParser.Node) m.getNodes().get(1);
        p.o = Math.log(-0.1) + "";
        this.shouldntWork();
        this.setUp();

        //alter connection
        //illegal probability
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(0);
        p = (NewickTreeParser.Node) m.getNodes().get(1);
        p.o = Math.log(5) + "";
        this.shouldntWork();
        this.setUp();

        //alter emission
        //type
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(m.getNodes().size() - 2);
        m.o = "4";
        this.shouldntWork();
        this.setUp();

        //alter emission
        //type
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(m.getNodes().size() - 2);
        m.o = "-1";
        this.shouldntWork();
        this.setUp();

        //alter emission
        //values
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(m.getNodes().size() - 1);
        p = (NewickTreeParser.Node) m.getNodes().get(1);
        p.o = Math.log(-0.01) + "";
        this.shouldntWork();
        this.setUp();

        //alter emission
        //values
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(m.getNodes().size() - 1);
        p = (NewickTreeParser.Node) m.getNodes().get(1);
        p.o = Math.log(0.01) + "";
        this.shouldntWork();
        this.setUp();

        //change emission numbers
        Cell.isLegitimateHMM(this.n);
        m = (NewickTreeParser.Node) this.n.getNodes().get(3);
        m = (NewickTreeParser.Node) m.getNodes().get(m.getNodes().size() - 1);
        p = new NewickTreeParser.Node();
        p.o = "0.0";
        m.addNode(p);
        this.shouldntWork();
        this.setUp();

        //make silent state and put ref preceding it
        final String randomHMM2 = "( (c, '1Gg'), (" + this.p5 + ", " + this.p2
                + ", " + this.p3 + "), (" + this.p7 + ", " + this.p1 + ", " + this.p2
                + ")," + "( (0, " + this.p1 + "), (1, " + this.p7 + "), (2, "
                + this.p2 + "), 2, (" + this.p2 + ", " + this.p2 + ", " + this.p2 + ", "
                + this.p4 + "))," + "((0, " + this.p5 + "), (1, " + this.p5
                + "), 1, (" + this.p6 + ", " + this.p4 + " ))," + "((2, " + this.p9
                + "), (0, " + this.p1 + "), 3, (" + this.p9 + ", " + this.p1 + ")),"
                + "((2, " + this.p9 + "), (3, " + this.p1 + "), 0, (" + this.p10
                + "))" + ");";
        final NewickTreeParser parser = new NewickTreeParser(
                NewickTreeParser
                        .tokenise(new StringReader(randomHMM2)));
        this.n = parser.tree;
        this.shouldntWork();
        this.setUp();
    }

    public void shouldntWork() {
        try {
            Cell.isLegitimateHMM(this.n);
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            ;
        }
    }

    public void testCreateProgram() {
        final Object[] oA = Cell.createProgram(this.n, Integer.MAX_VALUE,
                Integer.MIN_VALUE);
        final float[] start1 = new float[] { (this.p5), (this.p2), (this.p3), (this.p0) }, end1 = new float[] {
                (this.p7), (this.p1), (this.p1), (this.p1) };
        Assert.assertEquals(((float[]) oA[Cell.STARTSTATES]).length,
                start1.length);
        Assert.assertEquals(((float[]) oA[Cell.ENDSTATES]).length,
                end1.length);
        for (int i = 0; i < start1.length; i++) {
			Assert.assertEquals(start1[i],
                    ((float[]) oA[Cell.STARTSTATES])[i], 0.000000001);
		}
        for (int i = 0; i < end1.length; i++) {
			Assert.assertEquals(end1[i], ((float[]) oA[Cell.ENDSTATES])[i],
                    0.000000001);
		}

        final Function_Int fn = (Function_Int) oA[Cell.TRANSLATEALPHABETCHAR];

        Assert.assertEquals(fn.fn((byte) 'c'), 0);
        Assert.assertEquals(fn.fn((byte) 'g'), 1);
        Assert.assertEquals(fn.fn((byte) 'G'), 1);
        for (int i = 0; i < 255; i++) {
            if ((i != (byte) 'c') && (i != (byte) 'g') && (i != (byte) 'G')) {
				Assert.assertEquals(fn.fn(i), Integer.MIN_VALUE);
			}
        }
        final float[] emissions = new float[] { (this.p2), (this.p2), (this.p2), (this.p4),

        (this.p6), (this.p6), (this.p4), (this.p4),

        (this.p9), (this.p1), (this.p9), (this.p1),

        (this.p10), (this.p10), (this.p10), (this.p10)

        };

        Assert.assertEquals(emissions.length, ((float[]) oA[Cell.EMISSIONS]).length);
        for (int i = 0; i < emissions.length; i++) {
			Assert.assertEquals(emissions[i],
                    ((float[]) oA[Cell.EMISSIONS])[i], 0.000000001);
		}

        final float[] transitions = new float[] { Float.NaN,

        this.p1,

        this.p5,

        this.p1,

        Float.NaN,

        this.p7,

        this.p5,

        Float.NaN,

        this.p2,
        
        this.p8,

        this.p10,

        Float.NaN,

        this.p1,

        Float.NaN };

        Assert.assertEquals(transitions.length, ((float[]) oA[Cell.TRANSITIONS]).length);
        for (int i = 0; i < transitions.length; i++) {
			if (Float.isNaN(transitions[i])) {
				Assert.assertTrue(Float
                        .isNaN(((float[]) oA[Cell.TRANSITIONS])[i]));
			} else {
				Assert.assertEquals(transitions[i],
                        ((float[]) oA[Cell.TRANSITIONS])[i],
                        0.000000001);
			}
		}

        final int[] program = new int[] {

        Integer.MAX_VALUE,

        4 * 2 + 0,

        4 * 2 + 1,

        4 * 2 + 2,

        Integer.MAX_VALUE,

        4 + 0,

        4 + 1,

        Integer.MAX_VALUE,

        4 * 3 + 0,
        
        4 * 3 + 2,

        4 * 3 + 3,

        Integer.MAX_VALUE,

        4 * 0 + 2,

        Integer.MAX_VALUE };
        Assert.assertTrue(Arrays.equals(program, (int[]) oA[Cell.PROGRAM]));

    } 
   
}