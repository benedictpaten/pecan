/*
 * Created on Apr 11, 2006
 */
package bp.pecan.utils;

import java.io.IOException;

import bp.common.fp.IterationTools;
import bp.common.io.InputMunger;
import bp.common.maths.Maths;
import bp.pecan.Ancestor;

public class F84Matrix {
    
    public static final String AP = "AP";

    public static final String CP = "CP";

    public static final String GP = "GP";

    public static final String TP = "TP"; 

    public static final String ALPHA = "ALPHA";

    public static final String GAMMA = "GAMMA";
    
    public static final String TIME = "TIME";
    
    public static final String PRINT_MATRIX = "PRINT_MATRIX";
    
    public static final String PRINT_LOG_MATRIX = "PRINT_LOG_MATRIX";

    public double aP = 0.25;

    public double cP = 0.25;

    public double gP = 0.25;

    public double tP = 0.25;

    public double alpha = 0.5f;

    public double gamma = 1.0f;//1.25;
    
    public double time = 1.0;
    
    public boolean printMatrix = true;
    
    public boolean printLogMatrix = true;

    public InputMunger setCommandLineArguments(final InputMunger inputMunger) {
        inputMunger.addWatch(F84Matrix.AP, 1, "Prob of A, default : " + this.aP);
        inputMunger.addWatch(F84Matrix.CP, 1, "Prob of C, default : " + this.cP);
        inputMunger.addWatch(F84Matrix.GP, 1, "Prob of G, default : " + this.gP);
        inputMunger.addWatch(F84Matrix.TP, 1, "Prob of T, default : " + this.tP);
        inputMunger.addWatch(F84Matrix.ALPHA, 1, "Alpha prob, default : "
                + this.alpha);
        inputMunger.addWatch(F84Matrix.GAMMA, 1, "Gamma prob, default : "
                + this.gamma);
        inputMunger.addWatch(F84Matrix.TIME, 1, "Time, default : "
                + this.time);
        inputMunger.addWatch(F84Matrix.PRINT_MATRIX, 0, "Print matrix, default : " + this.printMatrix);
        inputMunger.addWatch(F84Matrix.PRINT_LOG_MATRIX, 0, "Print log matrix, default : " + this.printLogMatrix);
        return inputMunger;
    }

    public void parseArguments(final InputMunger inputMunger) {
        this.aP = inputMunger.parseValue(this.aP, F84Matrix.AP);
        this.cP = inputMunger.parseValue(this.cP, F84Matrix.CP);
        this.gP = inputMunger.parseValue(this.gP, F84Matrix.GP);
        this.tP = inputMunger.parseValue(this.tP, F84Matrix.TP);
        this.alpha = inputMunger.parseValue(this.alpha, F84Matrix.ALPHA);
        this.gamma = inputMunger.parseValue(this.gamma, F84Matrix.GAMMA);
        this.time = inputMunger.parseValue(this.time, F84Matrix.TIME);
        this.printMatrix = inputMunger.watchSet(F84Matrix.PRINT_MATRIX) ? !this.printMatrix : this.printMatrix;
        this.printLogMatrix = inputMunger.watchSet(F84Matrix.PRINT_LOG_MATRIX) ? !this.printLogMatrix : this.printLogMatrix;
    }
    
    public void printMatrix() {
        final Ancestor.SubstitutionMatrixGenerator sMG = Ancestor
        .f84(this.aP, this.cP, this.gP, this.tP, this.alpha, this.gamma);
        final double[][] dAA = sMG.getMatrix(this.time);
        for (final double[] element : dAA) {
			System.out.println(IterationTools.join(element, " "));
		}
    }
    
    public void printLogMatrix() {
        final Ancestor.SubstitutionMatrixGenerator sMG = Ancestor
        .f84(this.aP, this.cP, this.gP, this.tP, this.alpha, this.gamma);
        final double[][] dAA = sMG.getMatrix(this.time);
        System.out.println(" ( ");
        for (final double[] dA : dAA) {
            for(int j=0; j<dA.length; j++) {
				dA[j] = 0.16*dA[j];
			}
            for (final double element0 : dA) {
				System.out.print(Maths.log(element0) + " [ " + element0 + " ] , ");
			}
            System.out.print(Maths.log(0.04) + " [ " + 0.04 + " ] , ");
            System.out.println("\n");
        }
        for(int j=0; j<4; j++) {
			System.out.print(Maths.log(0.04) + " [ " + 0.04 + " ] , ");
		}
        System.out.print(Maths.log(0.04) + " [ " + 0.04 + " ] ");
        System.out.println("\n");
        System.out.println(" ) ");
    }
    
    public static void main(final String[] args) throws IOException {
        final F84Matrix f84Matrix = new F84Matrix();
        final InputMunger inputMunger = new InputMunger();
        inputMunger.addStandardWatches();
        f84Matrix.setCommandLineArguments(inputMunger);
        if (!inputMunger.parseInput(args)) {
			System.exit(0);
		}
        inputMunger.processStandardWatches();
        f84Matrix.parseArguments(inputMunger);
        if(f84Matrix.printMatrix) {
			f84Matrix.printMatrix();
		}
        if(f84Matrix.printLogMatrix) {
        //System.out.println();
            f84Matrix.printLogMatrix();
        }
    }

}
