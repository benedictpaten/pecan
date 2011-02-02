/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

package bp.common.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

import bp.common.fp.IterationTools;

public class InputMunger {

    static class InputMungerException
                                     extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = -4456579905279386970L;

		public InputMungerException(final String msg) {
            super(msg);
        }
    }

    private static final Logger logger = Logger
            .getLogger(InputMunger.class.getName());

    private static class InputNode {

        int numberOfTerms = 0;

        String description;

        boolean seen = false;

        String[] strings = null;
    }

    static final Pattern helpPattern = Pattern
            .compile("[\\s]*-+[hH][Ee][lL][pP][\\s]*");

    private final static String IGNORE = "IGNORE";

    private static final String CONSOLELOGLEVEL = "consoleLogLevel";

    private static final String LOGFILE = "logFile";

    private static final String LOGGING = "logging";

    private static final String LOGLEVEL = "loggingLevel";

    public static final String FIRST = "first";

    public static final String standardHelp = "-----------\n"
            + "Boolean (yes/no) parameters can be flipped from their defaults by specifying them as command line option\n"
            + "Please prefix numerical values starting with a '-' in braces with a '/'\n"
            + "The following options to the java vm may be useful:\n"
            + "\tjava -Xmx[megabytes]m : max memory for the virtual machine\n"
            + "\t''   -Xms[megabytes]m : min memory to the virtual machine\n"
            + "\t''   -server : start the vm in server rather than client mode,\n"
            + "\t     maybe faster, may have larger memory requirements \n"
            + "\t     and slower start up ";
    
    public static final String basicNoInputString = "--help for more instructions";

    Map<Comparable, InputNode> nameToValues = new HashMap<Comparable, InputNode>(), tagsToValues = new HashMap<Comparable, InputNode>();

    String noInputString = "", helpString = "";

    int offset = 65;

    public InputMunger() {
        this.addWatch_VariableTermsLength(InputMunger.FIRST, (byte) '#', InputMunger.IGNORE);
    }

    /**
     * Add the standard watches. These include watches for logging.
     */
    public void addStandardWatches() {
        this.addWatch(InputMunger.LOGGING, 0, "Set logging");
        this.addWatch(InputMunger.LOGFILE, 1, "Set the log file (default = %t/bp.log)");
        this.addWatch(InputMunger.LOGLEVEL, 1, "Set the logging level");
        this.addWatch(InputMunger.CONSOLELOGLEVEL, 1, "Set the console log level");
    }

    /**
     * Add watch parameter. Watch parameters consist of a String name, used to
     * reference the parameter, an input symbol and a count of the number of
     * expected terms (which must be equal the number given on the command
     * column). A description string is also useful for describing the watch.
     * 
     * @param name
     *            of watch tag
     * @param inputSymbol
     *            the chosen flag
     * @param numberOfTerms
     *            must be equal to or greater than the total number of terms
     *            given
     * @param descriptor
     *            string description
     */
    public boolean addWatch(final String name, final byte inputSymbol,
            final int numberOfTerms, final String descriptor) {
        if (this.nameToValues.containsKey(name)) {
            return false;
        }
        final Byte b = new Byte(inputSymbol);
        if (this.tagsToValues.containsKey(b)) {
			return false;
		}
        final InputNode iN = new InputNode();
        iN.numberOfTerms = numberOfTerms;
        this.nameToValues.put(name, iN);
        this.tagsToValues.put(b, iN);
        if (descriptor != InputMunger.IGNORE) {
			iN.description = "\t-" + (char) inputSymbol + "\t"
                    + descriptor;
		}
        return true;
    }

    /**
     * Add watch with automatic allocation of a flag.
     * 
     * @param name
     * @param numberOfTerms
     * @param descriptor
     */
    public boolean addWatch(final String name, final int numberOfTerms,
            final String descriptor) {
        if (this.offset >= 128) {
			throw new RuntimeException(
                    "The maximum number of options have been specified");
		} 
        if (this.addWatch(name, (byte) this.offset, numberOfTerms, descriptor)) {
            this.offset++;
            if (this.offset == 91) {
				this.offset = 97;
			}
            return true;
        }
        return false;
    }

    public boolean addWatch_VariableTermsLength(final String name,
            final byte inputSymbol, final String descriptor) {
        return this.addWatch(name, inputSymbol, Integer.MAX_VALUE, descriptor);
    }

    public boolean addWatch_VariableTermsLength(final String name,
            final String descriptor) {
        return this.addWatch(name, Integer.MAX_VALUE, descriptor);
    }

    /**
     * Returns the help string printed to standard error.
     * 
     * @return
     */
    public String help() {
        final StringBuffer sB = new StringBuffer();
        sB.append(this.helpString + "\n");
        sB.append("Arguments:\n");
        final Byte[] bA = this.tagsToValues.keySet().toArray(
                new Byte[] {});
        Arrays.sort(bA);
        for (final Byte element : bA) {
            final String s = this.tagsToValues.get(element).description;
            if (s != null) {
				sB.append(s + "\n");
			}
        }
        sB.append(InputMunger.standardHelp);
        return sB.toString();
    }

    /**
     * Sets the help string. The help string is augmented by a description of
     * the watches set and and some JVM info.
     * 
     * @param helpString
     *            the help string.
     */
    public void helpString(final String helpString) {
        this.helpString = helpString;
    }

    /**
     * Sets the no input string.
     * 
     * @param noInputString
     *            the no input string.
     */
    public void noInputString(final String noInputString) {
        this.noInputString = noInputString;
    }

    /**
     * Parse a given set of input tokens. An InputMunger runtime exception will
     * be thrown if the input is unrecognisable. It is acceptable to give
     * multiple flags to to a switch, where any proceeding output will be
     * divided up greedily. Only total mismatches in proceeding token number
     * will cause an exception!
     * <p>
     * Example : [core arguments] -abc 1 2 3 4 If -a takes three arguments, -b
     * no arguments and -c one argument the result would be -a being allocated
     * tokens 1, 2, 3, -b being given no tokens and -c being given token 4.
     * 
     * @param args
     *            the token strings to parse
     * @return true if the input was not empty and no help request was
     *         encountered.
     */
    public boolean parseInput(String[] args) {
        if (args.length == 0) {
            Debug.pl(this.noInputString);
            Debug.pl(InputMunger.basicNoInputString);
            return false;
        }
        if (InputMunger.helpPattern.matcher(args[0]).matches()) {
            Debug.pl(this.help());
            return false;
        }
        final List lArgs = new LinkedList(Arrays.asList(args));
        lArgs.add(0, "-#");
        args = (String[]) lArgs.toArray(new String[] {});
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                final byte[] bA = args[i].getBytes();
                for (int j = 1; j < bA.length; j++) {
                    final Byte b = new Byte(bA[j]);
                    if (!this.tagsToValues.containsKey(b)) {
						throw new InputMungerException(
                                " unrecognised input flag ");
					}
                    final InputNode iN = this.tagsToValues.get(b);
                    iN.seen = true;
                    if (iN.numberOfTerms != Integer.MAX_VALUE) {
                        iN.strings = new String[iN.numberOfTerms];
                        if (args.length <= iN.numberOfTerms + i) {
							throw new InputMungerException(
                                    "Too few input terms for watch");
						}
                        for (int k = 0; k < iN.numberOfTerms; k++) {
                            final String s = args[i + 1 + k];
                            if (s.startsWith("-")) {
								throw new InputMungerException(
                                        "Too few input terms for watch");
							}
                            iN.strings[k] = s;
                        }
                        i += iN.numberOfTerms;
                    } else {
                        final List terms = new ArrayList();
                        while (i + 1 < args.length) {
                            if (args[i + 1].startsWith("-")) {
								break;
							}
                            terms.add(args[++i]);
                        }
                        iN.strings = (String[]) terms
                                .toArray(new String[] {});
                    }
                }
            } else {
				throw new InputMungerException(
                        "Input encountered without a supporting flag");
			}
        }
        return true;
    }

    /**
     * Parses the output value of a given watch. The default value will be used
     * if the watch has not been set.
     * 
     * @param defaultValue
     * @param watch
     * @return
     */
    public double parseValue(final double defaultValue, final String watch) {
        if (this.watchSet(watch)) {
            String s = this.nameToValues
                    .get(watch).strings[0];
            if(s.startsWith("/")) {
				s = s.substring(1);
			}
            final double d = Double.parseDouble(s);
            InputMunger.logger.info(watch + " : " + d);
            return d;
        }
        InputMunger.logger.info(watch + " : " + defaultValue);
        return defaultValue;
    }

    public int parseValue(final int defaultValue, final String watch) {
        if (this.watchSet(watch)) {
            String s = this.nameToValues
                    .get(watch).strings[0];
            if(s.startsWith("/")) {
				s = s.substring(1);
			}
            final int i = Integer.parseInt(s);
            InputMunger.logger.info(watch + " : " + i);
            return i;
        }
        InputMunger.logger.info(watch + " : " + defaultValue);
        return defaultValue;
    }

    public String parseValue(final String defaultValue, final String watch) {
        if (this.watchSet(watch)) {
            final String s = this.nameToValues
                    .get(watch).strings[0];
            InputMunger.logger.info(watch + " : " + s);
            return s;
        }
        InputMunger.logger.info(watch + " : " + defaultValue);
        return defaultValue;
    }

    /**
     * Process the set of standard watches. This assumes the standard watches
     * have been previously set. See addStandardWatches()
     * 
     * @throws IOException
     * 
     */
    public void processStandardWatches() throws IOException {
        Logger.getLogger("").setLevel(Level.OFF);
        if (this.watchSet(InputMunger.LOGGING)) {
            final String logFile = this.parseValue("%tbp.log", InputMunger.LOGFILE);
            final FileHandler fH = new FileHandler(logFile);
            fH.setFormatter(new SimpleFormatter());
            Level level;
            if (this.watchSet(InputMunger.LOGLEVEL)) {
                level = Level.parse(this.watchStrings(InputMunger.LOGLEVEL)[0]);
            } else {
                level = Level.INFO;
            }
            Logger.getLogger("").setLevel(level);
            fH.setLevel(level);
            if (this.watchSet(InputMunger.CONSOLELOGLEVEL)) {
				Logger.getLogger("").getHandlers()[0].setLevel(Level
                        .parse(this.watchStrings(InputMunger.CONSOLELOGLEVEL)[0]));
			}
            Logger.getLogger("").addHandler(fH);
        } 
    }

    /**
     * Resets all the watches that have may have been previously set by a call
     * to parseInput.
     * 
     */
    public void reset() {
        for (final Iterator<InputNode> it = this.nameToValues.values().iterator(); it
                .hasNext();) {
            final InputNode iN = it.next();
            iN.strings = null;
            iN.seen = false;
        }
    }

    /**
     * Check if the watch is set in the output. If the watch does not exist an
     * exception will be thrown.
     * 
     * @param name
     *            watch to check
     * @return if watch set
     */
    public boolean watchSet(final String name) {
        if (!this.nameToValues.containsKey(name)) {
			throw new InputMungerException("Watch does not exist");
		}
        final boolean b = this.nameToValues.get(name).seen;
        InputMunger.logger.info(name + " has been set : " + b);
        return b;
    }

    /**
     * Returns all the tokens given to a watch. Negative numerical values beginning with a '-' will be preappended with a '/'.
     * 
     * @param name
     * @return
     */
    public String[] watchStrings(final String name) {
        final String[] sA = this.nameToValues.get(name).strings;
        InputMunger.logger.info(name + " : " + IterationTools.join(sA, " "));
        return sA;
    }
}