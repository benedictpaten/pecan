/*
 * Created on May 30, 2005
 */
package bp.pecan;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import bp.common.ds.Array;
import bp.common.fp.Function_Int;
import bp.common.fp.Function_Int_2Args;
import bp.common.io.NewickTreeParser;
import bp.common.io.NewickTreeParser.Node;
import bp.common.maths.Maths;

/**
 * @author benedictpaten
 */
public final class Cell {

    static final Logger logger = Logger.getLogger(Pecan.class
            .getName());

    static final void checkAlphabet(final NewickTreeParser.Node node) {
        if (node.getNodes().size() == 0) {
			throw new IllegalStateException();
		}
        for (int i = 0; i < node.getNodes().size(); i++) {
			Cell.checkCharacter((Node) node.getNodes().get(i));
		}
    }

    static final void checkBeginProbs(final NewickTreeParser.Node node,
            final int numberOfStates) {
        Cell.checkProbs(node, numberOfStates);
    }

    static final void checkCharacter(final NewickTreeParser.Node node) {
        final String s = (String) node.o;
        if (s.length() == 0) {
			throw new IllegalArgumentException();
		}
        final Pattern p = Pattern.compile("[a-zA-Z]");
        for (int i = 0; i < s.length(); i++) {
			if (!p.matcher("" + s.charAt(i)).matches()) {
				throw new IllegalArgumentException();
			}
		}
    }

    static final float checkConnection(final NewickTreeParser.Node node,
            final int numberOfStates, float runningProb) {
        final float[] dA = Cell.parseFloats(Cell.getStrings(node));
        if (!((dA[0] >= 0) && (dA[0] < numberOfStates))) {
			throw new IllegalArgumentException();
		}
        runningProb = (float) Maths.logAddQuality(runningProb, dA[1]);
        return runningProb;
    }

    static final void checkEmission(final NewickTreeParser.Node node,
            final int alphabetSize, final int stateNumber) {
        final int emission = (int) Float
                .parseFloat((String) ((NewickTreeParser.Node) node
                        .getNodes().get(node.getNodes().size() - 2)).o);
        if (emission == 0) { // silent state
            Cell.checkForConnectionsToProceedingStates(node, stateNumber);
        }
        if (!((emission >= 0) && (emission < 4))) {
			throw new IllegalArgumentException();
		}
        Cell.checkProbs((NewickTreeParser.Node) node.getNodes().get(
                node.getNodes().size() - 1), Integer.MAX_VALUE);
    }

    static final void checkEndProbs(final NewickTreeParser.Node node,
            final int numberOfStates) {
        Cell.checkProbs(node, numberOfStates);
    }

    static final void checkForConnectionsToProceedingStates(
            final Node node, final int stateNumber) {
        for (int i = 0; i < node.getNodes().size() - 2; i++) {
            final float[] dA = Cell.parseFloats(Cell.getStrings((NewickTreeParser.Node) node
                    .getNodes().get(i)));
            if (dA[0] >= stateNumber) {
				throw new IllegalArgumentException();
			}
        }
    }

    static final void checkProbability(float d) {
        d = (float) Math.exp(d);
        if (!((d < 1.000001) && (d > 1.0 - 0.000001))) {
			throw new IllegalArgumentException(" " + d);
		}
    }

    static final void checkProbs(final NewickTreeParser.Node node, final int size) {
        final float[] dA = Cell.parseFloats(Cell.getStrings(node));
        float d = Float.NEGATIVE_INFINITY;
        for (final float element : dA) {
			d = (float) Maths.logAddQuality(element, d);
		}
        if ((size != Integer.MAX_VALUE) && (dA.length != size)) {
			throw new IllegalArgumentException(size + " " + dA.length);
		}
        Cell.checkProbability(d);
    }

    static final void checkState(final NewickTreeParser.Node node,
            final int numberOfStates, final int alphabetSize, final int stateNumber) {
        float d = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < node.getNodes().size() - 2; i++) {
			d = Cell.checkConnection((NewickTreeParser.Node) node
                    .getNodes().get(i), numberOfStates, d);
		}
        Cell.checkProbability(d);
        Cell.checkEmission(node, alphabetSize, stateNumber);
    }

    /**
     * Create a program understandable by
     * {@link Cell#nextSum(int[], int[], Function_Int_2Args, Function_Int_2Args, int, int, int)}
     * 
     * @param node
     *            translate hmm as describvd in
     *            {@link Cell#isLegitimateHMM(NewickTreeParser.Node)}
     * @param translateProbability
     *            translate probability from float to integer, takes a
     *            {@link Number}argument and returns one
     * @param reservedValue
     *            value used to denote transitions between states
     * @param reservedCharacterValue
     *            value returned by translateAlphabet function for values not in
     *            the alphabet
     * @return array containing function to translate between inputs, start
     *         states, end states and the program itself
     */
    public final static Object[] createProgram(
            final NewickTreeParser.Node node, final int reservedValue,
            final int reservedCharacterValue) {
        final int alphabetSize = ((NewickTreeParser.Node) node.getNodes()
                .get(0)).getNodes().size();
        final int[] translateAlphabetChar = Cell.translateAlphabetChar(
                Cell.getStrings((NewickTreeParser.Node) node.getNodes()
                        .get(0)), reservedCharacterValue);
        final float[] startStates = Cell.parseFloats(Cell.getStrings((NewickTreeParser.Node) node
                .getNodes().get(1)));
        final float[] endStates = Cell.parseFloats(Cell.getStrings((NewickTreeParser.Node) node
                .getNodes().get(2)));
        final int stateNumber = node.getNodes().size() - 3;
        final List<Integer> emissionTypes = new LinkedList<Integer>();
        final List<LinkedList> transitionLists = new LinkedList<LinkedList>();
        for (int i = 0; i < stateNumber; i++) {
			transitionLists.add(new LinkedList());
		}
        final List emissions = new LinkedList();
        for (int i = 0; i < stateNumber; i++) {
            Cell.parseState(i, (NewickTreeParser.Node) node.getNodes()
                    .get(i + 3), emissions, emissionTypes,
                    transitionLists, alphabetSize * alphabetSize);
        }
        final List<Integer> program = new LinkedList<Integer>();
        final List<Float> transitions = new LinkedList<Float>();
        program.add(new Integer(reservedValue));
        transitions.add(new Float(Float.NaN));
        for (int i = 0; i < stateNumber; i++) {
            final List l = transitionLists.get(i);
            final int emissionType = emissionTypes.get(i)
                    .intValue();
            for (final Iterator it2 = l.iterator(); it2.hasNext();) {
                final float[] fA = (float[]) it2.next();
                program.add(new Integer(emissionType * stateNumber
                        + (int) fA[0]));
                transitions.add(new Float(fA[1]));
            }
            program.add(new Integer(reservedValue));
            transitions.add(new Float(Float.NaN));
        }
        return new Object[] { Cell.listToIntArray(program),
                Cell.listToFloatArray(transitions),
                Cell.concatenateFloatArrays(emissions), startStates,
                endStates,
                Array.matrix1DLookUp(translateAlphabetChar),
                new Integer(alphabetSize),
                Cell.listToIntArray(emissionTypes) };
    }

    public static final int PROGRAM = 0, TRANSITIONS = 1,
            EMISSIONS = 2, STARTSTATES = 3, ENDSTATES = 4,
            TRANSLATEALPHABETCHAR = 5, ALPHABETSIZE = 6, EMISSION_TYPES = 7;

    static final String[] getStrings(final NewickTreeParser.Node node) {
        final String[] sA = new String[node.getNodes().size()];
        for (int i = 0; i < sA.length; i++) {
			sA[i] = (String) ((NewickTreeParser.Node) node.getNodes()
                    .get(i)).o;
		}
        return sA;
    }

    /**
     * hmm format 0.1 is a Newick tree specified as follows.
     * 
     * <ul>
     * <li>hmm ==> ( ALPHABET, BEGINPROBS, ENDPROBS, STATE x N );
     * <li>ALPHABET ==> ( CHARACTER x N )
     * <li>BEGINPROBS ==> ( PROBABILITY x N )
     * <li>ENDPROBS ==> ( PROBABILITY x N )
     * <li>STATE ==> ( OUTGOING-CONNECTION x N, EMISSIONTYPE, EMISSIONVALUES )
     * <li>OUTGOING-CONNECTION ==> ( TO, PROBABILITY )
     * <li>EMISSIONVALUES ==> ( PROBABILITY x N )
     * </ul>
     * <ul>
     * <li>CHARACTER ==> block of chars, such as aA or g which can be
     * alphabetic or numeric
     * <li>EMISSIONTYPE ==> integer number
     * <li>FROM ==> integer number
     * <li>PROBABILITY ==> (log values) decimal in range 0.0-1.0
     * </ul>
     * 
     * 
     * Where { x N } indicates a comma seperate list.
     * <p>
     * To be valid the sum of outgoing connections for each state must equal one
     * as must the sum of the emission probabilities for each state and the
     * begin and end probabilities. Connections with a zero (silent) emission
     * must not have references to states which precede them, this prevents
     * loops. All states must have at least one incoming and one outgoing
     * connection. The alphabet size must be one or greater. Alphabet characters
     * must be numeric.
     * 
     * @throws IllegalArgumentException
     * 
     * @author benedictpaten
     */
    public final static void isLegitimateHMM(
            final NewickTreeParser.Node node) {
        Cell.checkAlphabet((Node) node.getNodes().get(0));
        final int alphabetSize = ((Node) node.getNodes().get(0)).getNodes()
                .size();
        final int numberOfStates = node.getNodes().size() - 3;
        Cell.checkBeginProbs((Node) node.getNodes().get(1), numberOfStates);
        Cell.checkEndProbs((Node) node.getNodes().get(2), numberOfStates);
        for (int i = 0; i < numberOfStates; i++) {
            Cell.checkState((Node) node.getNodes().get(i + 3),
                    numberOfStates, alphabetSize, i);
        }
    }

    static final int[] listToIntArray(final List<Integer> l) {
        final int[] iA = new int[l.size()];
        for (int i = 0; i < l.size(); i++) {
			iA[i] = l.get(i).intValue();
		}
        return iA;
    }

    static final float[] listToFloatArray(final List<Float> l) {
        final float[] dA = new float[l.size()];
        for (int i = 0; i < l.size(); i++) {
			dA[i] = l.get(i).floatValue();
		}
        return dA;
    }

    static final float[] concatenateFloatArrays(final List l) {
        int i = 0;
        for (final Iterator it = l.iterator(); it.hasNext();) {
			i += ((float[]) it.next()).length;
		}
        final float[] dA = new float[i];
        i = 0;
        for (final Iterator it = l.iterator(); it.hasNext();) {
            final float[] dA2 = (float[]) it.next();
            System.arraycopy(dA2, 0, dA, i, dA2.length);
            i += dA2.length;
        }
        return dA;
    }

    public static final void transformProgram(final int[] program,
            final int lineSize, int stateNumber) {
        for (int i = 0; i < program.length; i++) {
            if (program[i] != Integer.MAX_VALUE) {
                switch (program[i] / stateNumber) {
                case 0:
                    break;
                case 1:
                    program[i] = -stateNumber
                            + (program[i] % stateNumber);
                    break;
                case 2:
                    program[i] = -stateNumber * (lineSize + 1)
                            + (program[i] % stateNumber);
                    break;
                case 3:
                    program[i] = -stateNumber * lineSize
                            + (program[i] % stateNumber);
                    break;
                default:
                    throw new IllegalStateException();
                }
            }
        }
    }

    public interface GetCellCalculator {
        CellCalculator getCellCalculator(Function_Int getX,
                Function_Int getY);
    }

    public static GetCellCalculator getForwardCellCalculator(
            final int[] program, final float[] transitions,
            final float[] emissions, final int alphabetSize) {
        return new GetCellCalculator() {
            public CellCalculator getCellCalculator(
                    final Function_Int getX, final Function_Int getY) {
                return Cell.nextSumForward(program, transitions,
                        emissions, alphabetSize, getX, getY);
            }
        };
    }

    public static final CellCalculator nextSumForward(
            final int[] program, final float[] transitions,
            final float[] emissions, final int alphabetSize,
            final Function_Int getX, final Function_Int getY) {
        return new CellCalculator() {
            private final int jump = alphabetSize * alphabetSize;

            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(int[], int, int)
             */
            public final void calc(final float[] cells, final int offset, final int x,
                    final int y) {
                Cell.nextSumForward(cells, program, transitions,
                        emissions, this.jump, getX.fn(x) * alphabetSize
                                + getY.fn(y), offset);
            }
        };
    }

    public static final void nextSumForward(final float[] cells,
            final int[] program, final float[] transitions,
            final float[] emissions, final int jump, int xyz,
            final int offset) {
        int i = 1, j = 0;
        while (i < program.length) {
            float s = cells[program[i] + offset] + transitions[i];
            i++;
            while (program[i] != Integer.MAX_VALUE) {
                s = Maths.logAdd(s, cells[program[i] + offset]
                        + transitions[i]);
                i++;
            }
            i++;
            cells[j++ + offset] = s + emissions[xyz];
            xyz += jump;
        }
    }

    public static final Object[] makeRProgram(final int[] program,
            final float[] transitions, final int stateNumber) {
        final int[] iA = new int[program.length + stateNumber];
        final float[] fA = new float[iA.length];
        int i = 0;
        int j = 0;
        int k = stateNumber - 1;
        while (i < program.length - 1) {
            if (program[i] == Integer.MAX_VALUE) {
                i++;
                fA[j] = Float.NaN;
                iA[j++] = Integer.MAX_VALUE;
                fA[j] = Float.NaN;
                iA[j++] = program[i]
                        - (program[i] < 0 ? stateNumber
                                - 1
                                - Math.abs((program[i] + 1)
                                        % stateNumber) : program[i]
                                % stateNumber) + k--;
            } else {
                fA[j] = transitions[i];
                iA[j++] = program[i] < 0 ? Math.abs((program[i] + 1)
                        % stateNumber) : stateNumber - 1 - program[i]
                        % stateNumber;
                i++;
            }
        }
        fA[j] = Float.NaN;
        iA[j] = Integer.MAX_VALUE;
        return new Object[] { iA, fA };
    }

    public static GetCellCalculator getBackwardCellCalculator(
            final int[] rProgram, final float[] transitions,
            final float[] emissions, final int stateNumber,
            final int alphabetSize) {
        return new GetCellCalculator() {
            public CellCalculator getCellCalculator(
                    final Function_Int getX, final Function_Int getY) {
                return Cell.nextSumBackward(rProgram, transitions,
                        emissions, stateNumber, alphabetSize, getX,
                        getY);
            }
        };
    }

    public static final CellCalculator nextSumBackward(
            final int[] rProgram, final float[] transitions,
            final float[] emissions, final int stateNumber,
            final int alphabetSize, final Function_Int getX,
            final Function_Int getY) {
        return new CellCalculator() {
            private final int jump = alphabetSize * alphabetSize;

            /*
             * (non-Javadoc)
             * 
             * @see bp.pecan.dpc.Cell.CellCalculator#calc(int[], int, int)
             */
            public final void calc(final float[] cells, final int offset, final int x,
                    final int y) {
                Cell.nextSumBackward(cells, rProgram, transitions,
                        emissions, this.jump,
                        (getX.fn(x + 1) * alphabetSize)
                                + getY.fn(y + 1), offset, stateNumber);
            }
        };
    }

    public static final void nextSumBackward(final float[] cells,
            final int[] program, final float[] transitions,
            final float[] emissions, final int jump, int xyz,
            final int offset, final int stateNumber) {
        Arrays.fill(cells, offset, offset + stateNumber,
                Float.NEGATIVE_INFINITY);
        int i = 1;
        while (i < program.length) {
            final float s = cells[program[i] + offset] + emissions[xyz];
            i++;
            xyz += jump;
            cells[program[i] + offset] = Maths.logAdd(
                    cells[program[i] + offset], s + transitions[i]);
            i++;
            while (program[i] != Integer.MAX_VALUE) {
                cells[program[i] + offset] = Maths.logAdd(
                        cells[program[i] + offset], s
                                + transitions[i]);
                i++;
            }
            i++;
        }
    }

    /**
     * Interface for cell calculator wrapper.
     * 
     * @author benedictpaten
     */
    public interface CellCalculator {
        public void calc(float[] cells, int offset, int x, int y);
    }

    static final void parseConnection(final int state,
            final NewickTreeParser.Node node, final List<LinkedList> transitions) {
        final float[] dA = Cell.parseFloats(Cell.getStrings(node));
        final int to = (int) dA[0];
        final float probability = dA[1];
        final float[] fA = new float[] { state, probability };
        transitions.get(to).add(fA);
    }

    static final float[] parseFloats(final String[] sA) {
        final float[] dA = new float[sA.length];
        for (int i = 0; i < dA.length; i++) {
			dA[i] = Float.parseFloat(sA[i]);
		}
        return dA;
    }

    static final void parseState(final int state,
            final NewickTreeParser.Node node, final List emissions,
            final List<Integer> emissionTypes, final List<LinkedList> transitions, final int arraySize) {
        final int connectionNo = node.getNodes().size() - 2;
        final int emissionType = (int) Float
                .parseFloat((String) ((NewickTreeParser.Node) node
                        .getNodes().get(connectionNo)).o);
        emissionTypes.add(new Integer(emissionType));
        emissions.add(Cell.resizeArray(
                Cell.parseFloats(Cell.getStrings((NewickTreeParser.Node) node
                        .getNodes().get(connectionNo + 1))),
                arraySize, emissionType));
        for (int i = 0; i < connectionNo; i++) {
			Cell.parseConnection(state, (NewickTreeParser.Node) node
                    .getNodes().get(i), transitions);
		}
    }

    static final float[] resizeArray(final float[] dA, final int arraySize,
            final int emissionType) {
        final float[] dA2 = new float[arraySize];
        if ((emissionType == 0) || (emissionType == 3)) {
            int i = 0;
            while (i < dA2.length) {
                System.arraycopy(dA, 0, dA2, i, dA.length);
                i += dA.length;
            }
        }
        if (emissionType == 2) {
            System.arraycopy(dA, 0, dA2, 0, dA.length);
        }
        if (emissionType == 1) {
            for (int i = 0; i < dA.length; i++) {
				for (int j = 0; j < dA.length; j++) {
					dA2[i * dA.length + j] = dA[i];
				}
			}
        }
        return dA2;
    }

    public final static int[] translateAlphabetChar(final String[] sA,
            final int reservedAlphabetChar) {
        final int[] bA = new int[256];
        Arrays.fill(bA, reservedAlphabetChar);
        for (int i = 0; i < sA.length; i++) {
            final String s = sA[i];
            for (int j = 0; j < s.length(); j++) {
                bA[s.charAt(j)] = i;
            }
        }
        return bA;
    }

    public static abstract class BasicCell {
        Function_Int translateChars;

        public float[] startStates;

        public float[] endStates;

        public int stateNumber;

        public int alphabetSize;
        
        public GetCellCalculator getBackwardCellCalculator(
                final int leftIndelOffset, final int matchOffset,
                final int rightIndelOffset) {
            return new GetCellCalculator() {
                public CellCalculator getCellCalculator(
                        final Function_Int getX, final Function_Int getY) {
                    return BasicCell.this.backwards(leftIndelOffset, matchOffset,
                            rightIndelOffset, getX, getY);
                }
            };
        }

        public GetCellCalculator getForwardCellCalculator(
                final int leftIndelOffset, final int matchOffset,
                final int rightIndelOffset) {
            return new GetCellCalculator() {
                public CellCalculator getCellCalculator(
                        final Function_Int getX, final Function_Int getY) {
                    return BasicCell.this.forwards(leftIndelOffset, matchOffset,
                            rightIndelOffset, getX, getY);
                }
            };
        }

        public abstract CellCalculator backwards(
                final int leftIndelOffset, final int matchOffset,
                final int rightIndelOffset, final Function_Int getX,
                final Function_Int getY);

        public abstract CellCalculator forwards(
                final int leftIndelOffset, final int matchOffset,
                final int rightIndelOffset, final Function_Int getX,
                final Function_Int getY);
    }

    public final static class ThreeCell
                                       extends BasicCell {
        static final int MATCH_STATE = 0;

        static final int LEFT_GAP_STATE = 1;

        static final int RIGHT_GAP_STATE = 2;

        static final int INVERSE_MATCH_STATE = 2;

        static final int INVERSE_LEFT_GAP_STATE = 1;

        static final int INVERSE_RIGHT_GAP_STATE = 0;

        static final int N_EMISSION = 4;

        static final float MATCH = 0.972775379521401f; //-0.027602076970648006

        static final float GAP_EXTEND = 0.974445284091146f; //-0.02588690928541599

        static final float GAP_SWITCH = 0.0007315179552849f; //-7.2203887919613665

        static final float MATCHL = Maths.log(ThreeCell.MATCH);

        static final float GAP_OPENL = Maths.log((1.0f - ThreeCell.MATCH) / 2);

        static final float GAP_EXTENDL = Maths.log(ThreeCell.GAP_EXTEND);

        static final float GAP_SWITCHL = Maths.log(ThreeCell.GAP_SWITCH);

        static final float GAP_CLOSEL = Maths.log((1.0f - ThreeCell.GAP_EXTEND)
                - ThreeCell.GAP_SWITCH);
        
        static final float MATCH_EMISSION_N_L = Maths.log(0.04f);

        static final float MATCH_EMISSIONL = Maths.log(0.12064298095701059f);

        static final float TRANSVERSION_EMISSIONL = Maths.log(0.010367271172731285f);

        static final float TRANSITION_EMISSIONL = Maths.log(0.01862247669752685f);
        
        static final float GAP_EMISSIONL = Maths.log(0.2f);
        
        static final float[] emit = new float[] { 
            ThreeCell.MATCH_EMISSIONL, ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.TRANSITION_EMISSIONL, ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.MATCH_EMISSION_N_L,
            ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.MATCH_EMISSIONL, ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.TRANSITION_EMISSIONL, ThreeCell.MATCH_EMISSION_N_L,
            ThreeCell.TRANSITION_EMISSIONL, ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.MATCH_EMISSIONL, ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.MATCH_EMISSION_N_L,
            ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.TRANSITION_EMISSIONL, ThreeCell.TRANSVERSION_EMISSIONL, ThreeCell.MATCH_EMISSIONL, ThreeCell.MATCH_EMISSION_N_L,
            ThreeCell.MATCH_EMISSION_N_L, ThreeCell.MATCH_EMISSION_N_L, ThreeCell.MATCH_EMISSION_N_L, ThreeCell.MATCH_EMISSION_N_L, ThreeCell.MATCH_EMISSION_N_L };  

        public ThreeCell() {
            this.translateChars = Array
                    .matrix1DLookUp(Cell.translateAlphabetChar(
                            new String[] { "aA", "cC", "gG", "tT",
                                    "nNxX" }, 4));

            this.startStates = new float[] { Maths.log(1.0f / 3),
                    Maths.log(1.0f / 3), Maths.log(1.0f / 3) };

            this.endStates = new float[] { Maths.log(1.0f / 3),
                    Maths.log(1.0f / 3), Maths.log(1.0f / 3) };

            this.stateNumber = 3;

            this.alphabetSize = 5;
        }

        @Override
		public CellCalculator backwards(final int leftIndelOffset,
                final int matchOffset, final int rightIndelOffset,
                final Function_Int getX, final Function_Int getY) {
            return new CellCalculator() {
                public final void calc(final float[] cells, final int offset,
                        int x, int y) {
                    x = getX.fn(x + 1);
                    y = getY.fn(y + 1);
                    final float mE = ThreeCell.emit[x * 5 + y];
                    float i = Maths.logAdd(cells[leftIndelOffset
                            + offset + ThreeCell.INVERSE_LEFT_GAP_STATE],
                            cells[rightIndelOffset + offset
                                    + ThreeCell.INVERSE_RIGHT_GAP_STATE]);
                    cells[ThreeCell.INVERSE_MATCH_STATE + offset] = Maths
                            .logAdd(cells[matchOffset + offset
                                    + ThreeCell.INVERSE_MATCH_STATE]
                                    + ThreeCell.MATCHL + mE, i + ThreeCell.GAP_OPENL
                                    + ThreeCell.GAP_EMISSIONL);
                    final float j = cells[matchOffset + offset
                            + ThreeCell.INVERSE_MATCH_STATE]
                            + ThreeCell.GAP_CLOSEL + mE;
                    i = Maths.logAdd(cells[leftIndelOffset + offset
                            + ThreeCell.INVERSE_LEFT_GAP_STATE]
                            + ThreeCell.GAP_EXTENDL + ThreeCell.GAP_EMISSIONL,
                            cells[rightIndelOffset + offset
                                    + ThreeCell.INVERSE_RIGHT_GAP_STATE]
                                    + ThreeCell.GAP_SWITCHL + ThreeCell.GAP_EMISSIONL);
                    cells[ThreeCell.INVERSE_LEFT_GAP_STATE + offset] = Maths
                            .logAdd(j, i);
                    i = Maths.logAdd(cells[rightIndelOffset + offset
                            + ThreeCell.INVERSE_RIGHT_GAP_STATE]
                            + ThreeCell.GAP_EXTENDL + ThreeCell.GAP_EMISSIONL,
                            cells[leftIndelOffset + offset
                                    + ThreeCell.INVERSE_LEFT_GAP_STATE]
                                    + ThreeCell.GAP_SWITCHL + ThreeCell.GAP_EMISSIONL);
                    cells[ThreeCell.INVERSE_RIGHT_GAP_STATE + offset] = Maths
                            .logAdd(j, i);
                }
            };
        }

        @Override
		public CellCalculator forwards(final int leftIndelOffset,
                final int matchOffset, final int rightIndelOffset,
                final Function_Int getX, final Function_Int getY) {
            return new CellCalculator() {
                public final void calc(final float[] cells, final int offset,
                        int x, int y) {
                    x = getX.fn(x);
                    y = getY.fn(y);
                    final float mE = ThreeCell.emit[x * 5 + y];
                    float f = Maths.logAdd(cells[matchOffset + offset
                            + ThreeCell.LEFT_GAP_STATE], cells[matchOffset
                            + offset + ThreeCell.RIGHT_GAP_STATE])
                            + ThreeCell.GAP_CLOSEL;
                    cells[ThreeCell.MATCH_STATE + offset] = Maths.logAdd(
                            cells[matchOffset + offset + ThreeCell.MATCH_STATE]
                                    + ThreeCell.MATCHL, f)
                            + mE;
                    f = Maths.logAdd(cells[leftIndelOffset + offset
                            + ThreeCell.LEFT_GAP_STATE]
                            + ThreeCell.GAP_EXTENDL + ThreeCell.GAP_EMISSIONL,
                            cells[leftIndelOffset + offset
                                    + ThreeCell.RIGHT_GAP_STATE]
                                    + ThreeCell.GAP_SWITCHL + ThreeCell.GAP_EMISSIONL);
                    cells[ThreeCell.LEFT_GAP_STATE + offset] = Maths.logAdd(
                            cells[leftIndelOffset + offset
                                    + ThreeCell.MATCH_STATE]
                                    + ThreeCell.GAP_OPENL + ThreeCell.GAP_EMISSIONL, f);
                    f = Maths.logAdd(cells[rightIndelOffset + offset
                            + ThreeCell.RIGHT_GAP_STATE]
                            + ThreeCell.GAP_EXTENDL + ThreeCell.GAP_EMISSIONL,
                            cells[rightIndelOffset + offset
                                    + ThreeCell.LEFT_GAP_STATE]
                                    + ThreeCell.GAP_SWITCHL + ThreeCell.GAP_EMISSIONL);
                    cells[ThreeCell.RIGHT_GAP_STATE + offset] = Maths.logAdd(
                            cells[rightIndelOffset + offset
                                    + ThreeCell.MATCH_STATE]
                                    + ThreeCell.GAP_OPENL + ThreeCell.GAP_EMISSIONL, f);
                }
            };
        }
    }

    public final static class FiveCell
                                      extends BasicCell {
        static final int MATCH_STATE = 0;

        static final int LEFT_GAP_STATE = 1;

        static final int RIGHT_GAP_STATE = 2;

        static final int LEFT_JUNK_STATE = 3;

        static final int RIGHT_JUNK_STATE = 4;

        static final int INVERSE_MATCH_STATE = 4;

        static final int INVERSE_LEFT_GAP_STATE = 3;

        static final int INVERSE_RIGHT_GAP_STATE = 2;

        static final int INVERSE_LEFT_JUNK_STATE = 1;

        static final int INVERSE_RIGHT_JUNK_STATE = 0;

        static final int N_EMISSION = 4;

        static final float MATCH = 0.9703833696510062f; //-0.030064059121770816

        static final float GAP_OPEN = 0.0129868352330243f; //-4.34381910900448

        static final float GAP_EXTEND = 0.7126062401851738f; //-0.3388262689231553

        static final float GAP_SWITCH = 0.0073673675173412815f; //-4.910694825551255

        static final float JUNK_EXTEND = 0.99656342579062f; //-0.003442492794189331
        //-0.0034400580087498808

        static final float MATCHL = Maths.log(FiveCell.MATCH);

        static final float GAP_OPENL = Maths.log(FiveCell.GAP_OPEN);

        static final float JUNK_OPENL = Maths
                .log((1.0f - (FiveCell.MATCH + 2 * FiveCell.GAP_OPEN)) / 2);

        static final float GAP_EXTENDL = Maths.log(FiveCell.GAP_EXTEND);

        static final float GAP_SWITCHL = Maths.log(FiveCell.GAP_SWITCH);

        static final float GAP_CLOSEL = Maths.log((1.0f - FiveCell.GAP_EXTEND)
                - FiveCell.GAP_SWITCH);

        static final float JUNK_EXTENDL = Maths.log(FiveCell.JUNK_EXTEND);

        static final float JUNK_CLOSEL = Maths.log(1.0f - FiveCell.JUNK_EXTEND);

        static final float MATCH_EMISSION_N_L = Maths.log(0.04f);

        static final float MATCH_EMISSIONL = Maths.log(0.12064298095701059f);

        static final float TRANSVERSION_EMISSIONL = Maths.log(0.010367271172731285f);

        static final float TRANSITION_EMISSIONL = Maths.log(0.01862247669752685f);
        
        static final float GAP_EMISSIONL = Maths.log(0.2f);
        
        static final float[] emit = new float[] { 
                FiveCell.MATCH_EMISSIONL, FiveCell.TRANSVERSION_EMISSIONL, FiveCell.TRANSITION_EMISSIONL, FiveCell.TRANSVERSION_EMISSIONL, FiveCell.MATCH_EMISSION_N_L,
                FiveCell.TRANSVERSION_EMISSIONL, FiveCell.MATCH_EMISSIONL, FiveCell.TRANSVERSION_EMISSIONL, FiveCell.TRANSITION_EMISSIONL, FiveCell.MATCH_EMISSION_N_L,
                FiveCell.TRANSITION_EMISSIONL, FiveCell.TRANSVERSION_EMISSIONL, FiveCell.MATCH_EMISSIONL, FiveCell.TRANSVERSION_EMISSIONL, FiveCell.MATCH_EMISSION_N_L,
                FiveCell.TRANSVERSION_EMISSIONL, FiveCell.TRANSITION_EMISSIONL, FiveCell.TRANSVERSION_EMISSIONL, FiveCell.MATCH_EMISSIONL, FiveCell.MATCH_EMISSION_N_L,
                FiveCell.MATCH_EMISSION_N_L, FiveCell.MATCH_EMISSION_N_L, FiveCell.MATCH_EMISSION_N_L, FiveCell.MATCH_EMISSION_N_L, FiveCell.MATCH_EMISSION_N_L };  

        public FiveCell() {
            this.translateChars = Array
                    .matrix1DLookUp(Cell.translateAlphabetChar(
                            new String[] { "aA", "cC", "gG", "tT",
                                    "nNxX" }, 4));

            this.startStates = new float[] { Maths.log(1.0f / 5),
                    Maths.log(1.0f / 5), Maths.log(1.0f / 5),
                    Maths.log(1.0f / 5), Maths.log(1.0f / 5) };

            this.endStates = this.startStates;

            this.stateNumber = 5;

            this.alphabetSize = 5;
        }

        @Override
		public final CellCalculator backwards(
                final int leftIndelOffset, final int matchOffset,
                final int rightIndelOffset, final Function_Int getX,
                final Function_Int getY) {
            return new CellCalculator() {
                public final void calc(final float[] cells, final int offset,
                        int x, int y) {
                	//System.out.println("computing cell in machine:" + x + " " + y + "\n");
                    x = getX.fn(x + 1);
                    y = getY.fn(y + 1);
                    //float mE = x == N_EMISSION || y == N_EMISSION ? MATCH_EMISSION_N_L
                    //        : x == y ? MATCH_EMISSIONL
                     //               : MISMATCH_EMISSIONL;
                    final float mE = FiveCell.emit[x * 5 + y];
                    float i = Maths.logAdd(cells[leftIndelOffset
                            + offset + FiveCell.INVERSE_LEFT_GAP_STATE],
                            cells[rightIndelOffset + offset
                                    + FiveCell.INVERSE_RIGHT_GAP_STATE])
                            + FiveCell.GAP_OPENL + FiveCell.GAP_EMISSIONL;
                    final float j = Maths.logAdd(cells[leftIndelOffset
                            + offset + FiveCell.INVERSE_LEFT_JUNK_STATE],
                            cells[rightIndelOffset + offset
                                    + FiveCell.INVERSE_RIGHT_JUNK_STATE])
                            + FiveCell.JUNK_OPENL + FiveCell.GAP_EMISSIONL;
                    i = Maths.logAdd(i, j);
                    cells[FiveCell.INVERSE_MATCH_STATE + offset] = Maths
                            .logAdd(cells[matchOffset + offset
                                    + FiveCell.INVERSE_MATCH_STATE]
                                    + FiveCell.MATCHL + mE, i);

                    final float k = cells[matchOffset + offset
                            + FiveCell.INVERSE_MATCH_STATE]
                            + mE;
                    i = Maths.logAdd(cells[leftIndelOffset + offset
                            + FiveCell.INVERSE_LEFT_GAP_STATE]
                            + FiveCell.GAP_EXTENDL + FiveCell.GAP_EMISSIONL,
                            cells[rightIndelOffset + offset
                                    + FiveCell.INVERSE_RIGHT_GAP_STATE]
                                    + FiveCell.GAP_SWITCHL + FiveCell.GAP_EMISSIONL);
                    cells[FiveCell.INVERSE_LEFT_GAP_STATE + offset] = Maths
                            .logAdd(k + FiveCell.GAP_CLOSEL, i);
                    i = Maths.logAdd(cells[rightIndelOffset + offset
                            + FiveCell.INVERSE_RIGHT_GAP_STATE]
                            + FiveCell.GAP_EXTENDL + FiveCell.GAP_EMISSIONL,
                            cells[leftIndelOffset + offset
                                    + FiveCell.INVERSE_LEFT_GAP_STATE]
                                    + FiveCell.GAP_SWITCHL + FiveCell.GAP_EMISSIONL);
                    cells[FiveCell.INVERSE_RIGHT_GAP_STATE + offset] = Maths
                            .logAdd(k + FiveCell.GAP_CLOSEL, i);

                    cells[FiveCell.INVERSE_LEFT_JUNK_STATE + offset] = Maths
                            .logAdd(cells[leftIndelOffset + offset
                                    + FiveCell.INVERSE_LEFT_JUNK_STATE]
                                    + FiveCell.JUNK_EXTENDL + FiveCell.GAP_EMISSIONL, k
                                    + FiveCell.JUNK_CLOSEL);
                    cells[FiveCell.INVERSE_RIGHT_JUNK_STATE + offset] = Maths
                            .logAdd(cells[rightIndelOffset + offset
                                    + FiveCell.INVERSE_RIGHT_JUNK_STATE]
                                    + FiveCell.JUNK_EXTENDL + FiveCell.GAP_EMISSIONL, k
                                    + FiveCell.JUNK_CLOSEL);
                }
            };
        }

        @Override
		public final CellCalculator forwards(
                final int leftIndelOffset, final int matchOffset,
                final int rightIndelOffset, final Function_Int getX,
                final Function_Int getY) {
            return new CellCalculator() {
                public final void calc(final float[] cells, final int offset,
                        int x, int y) {
                    x = getX.fn(x);
                    y = getY.fn(y);
                    //float mEmission = x == N_EMISSION
                      //      || y == N_EMISSION ? MATCH_EMISSION_N_L
                        //    : x == y ? MATCH_EMISSIONL
                          //          : MISMATCH_EMISSIONL;
                    final float mEmission = FiveCell.emit[x * 5 + y];     
                    float f = Maths.logAdd(cells[matchOffset + offset
                            + FiveCell.LEFT_GAP_STATE], cells[matchOffset
                            + offset + FiveCell.RIGHT_GAP_STATE])
                            + FiveCell.GAP_CLOSEL;
                    final float f2 = Maths.logAdd(cells[matchOffset
                            + offset + FiveCell.LEFT_JUNK_STATE],
                            cells[matchOffset + offset
                                    + FiveCell.RIGHT_JUNK_STATE])
                            + FiveCell.JUNK_CLOSEL;
                    f = Maths.logAdd(f, f2);
                    cells[FiveCell.MATCH_STATE + offset] = Maths.logAdd(
                            cells[matchOffset + offset + FiveCell.MATCH_STATE]
                                    + FiveCell.MATCHL, f)
                            + mEmission;
                    f = Maths.logAdd(cells[leftIndelOffset + offset
                            + FiveCell.LEFT_GAP_STATE]
                            + FiveCell.GAP_EXTENDL + FiveCell.GAP_EMISSIONL,
                            cells[leftIndelOffset + offset
                                    + FiveCell.RIGHT_GAP_STATE]
                                    + FiveCell.GAP_SWITCHL + FiveCell.GAP_EMISSIONL);
                    cells[FiveCell.LEFT_GAP_STATE + offset] = Maths.logAdd(
                            cells[leftIndelOffset + offset
                                    + FiveCell.MATCH_STATE]
                                    + FiveCell.GAP_OPENL + FiveCell.GAP_EMISSIONL, f);
                    f = Maths.logAdd(cells[rightIndelOffset + offset
                            + FiveCell.RIGHT_GAP_STATE]
                            + FiveCell.GAP_EXTENDL + FiveCell.GAP_EMISSIONL,
                            cells[rightIndelOffset + offset
                                    + FiveCell.LEFT_GAP_STATE]
                                    + FiveCell.GAP_SWITCHL + FiveCell.GAP_EMISSIONL);
                    cells[FiveCell.RIGHT_GAP_STATE + offset] = Maths.logAdd(
                            cells[rightIndelOffset + offset
                                    + FiveCell.MATCH_STATE]
                                    + FiveCell.GAP_OPENL + FiveCell.GAP_EMISSIONL, f);
                    cells[FiveCell.LEFT_JUNK_STATE + offset] = Maths.logAdd(
                            cells[leftIndelOffset + offset
                                    + FiveCell.MATCH_STATE]
                                    + FiveCell.JUNK_OPENL + FiveCell.GAP_EMISSIONL,
                            cells[leftIndelOffset + offset
                                    + FiveCell.LEFT_JUNK_STATE]
                                    + FiveCell.JUNK_EXTENDL + FiveCell.GAP_EMISSIONL);
                    cells[FiveCell.RIGHT_JUNK_STATE + offset] = Maths.logAdd(
                            cells[rightIndelOffset + offset
                                    + FiveCell.MATCH_STATE]
                                    + FiveCell.JUNK_OPENL + FiveCell.GAP_EMISSIONL,
                            cells[rightIndelOffset + offset
                                    + FiveCell.RIGHT_JUNK_STATE]
                                    + FiveCell.JUNK_EXTENDL + FiveCell.GAP_EMISSIONL);
                }
            };
        }
    }

}