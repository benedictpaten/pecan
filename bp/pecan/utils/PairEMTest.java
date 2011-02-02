/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Apr 5, 2006
 */
package bp.pecan.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.ds.FloatStack;
import bp.common.fp.Functions_Int;
import bp.common.io.ExternalExecution;
import bp.common.io.NewickTreeParser;
import bp.pecan.Cell;

public class PairEMTest
                       extends TestCase {

    public void testThreeState_Backwards() throws IOException {
        NewickTreeParser nTP;
        {
            final String s = ExternalExecution.getAbsolutePath("bp/pecan/3State.hmm");
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
        final Cell.CellCalculator backwardsLL = PairEM.nextSumBackward(programBackward, transitionsBackward, emissions,
                startStates.length, alphabetSize, Functions_Int.doNothing(), Functions_Int.doNothing(), new FloatStack(1000));
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
    
}
