/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jan 6, 2005
 */
package bp.common.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import bp.common.io.ExternalExecution;

/**
 * This class contains methods used for testing. It should/can be used with
 * JUnit style tests.
 * 
 * @author benedictpaten
 */
public class TestTools {

    /**
     * Run the specified command and test it for equality against the contents
     * of the specified file. Save the contents of the command in the output
     * file.
     * 
     * @param command
     *            command to run
     * @param file
     *            file containing example command output
     * @param outputFile
     * @return true if command and example command output equal
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean testCommand(final String[] command, final String file,
            final String outputFile) throws FileNotFoundException, IOException {
        return TestTools.testCommand(command, new BufferedInputStream(
                new FileInputStream(file)), outputFile);
    }

    /**
     * Run the specified command and test it for equality against the contents
     * of the specified file. If saveOutput is true the contents of the command
     * run is saved in a file whose name as file with .2 as a suffix.
     * 
     * @param command
     *            command to run
     * @param file
     *            file containing example command output
     * @param saveOutput
     *            if to save output in temp file
     * @return true if command and example command output equal
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean testCommand(final String[] command, final String file,
            final boolean saveOutput) throws FileNotFoundException, IOException {
        if (saveOutput) {
            return TestTools.testCommand(command, file, file + ".new");
        } else {
            return TestTools.testCommand(command, new BufferedInputStream(
                    new FileInputStream(file)));
        }
    }

    /**
     * Tests if the output of the string command is the same as the input of
     * iS2.
     * 
     * @param command
     * @param iS2
     * @return true if command and inputstream contain the same contents.
     */
    public static boolean testCommand(final String[] command, final InputStream iS2) {
        File tempFile;
        try {
            tempFile = File.createTempFile("TestTools_testCommand", ".temp");
            tempFile.deleteOnExit();
            return TestTools.testCommand(command, iS2, tempFile.getPath());
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException("A temp file could not be created");
        }
    }

    /**
     * As testCommand(command, is2) but with the option to save the output.
     * 
     * @param command
     * @param iS2
     * @param outputFile
     * @return
     * @throws IOException
     */
    public static boolean testCommand(final String[] command, final InputStream iS2,
            final String outputFile) throws IOException {
        ExternalExecution.runSaveOutput(command, new BufferedOutputStream(
                new FileOutputStream(outputFile)), null);
        return TestTools.testEquality(new BufferedInputStream(new FileInputStream(
                outputFile)), iS2);
    }

    /**
     * 
     * @param iS
     * @param file
     * @return true if the contents of the file and inputStream are the same
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean testEquality(final InputStream iS, final String file)
            throws FileNotFoundException, IOException {
        return TestTools.testEquality(iS, new BufferedInputStream(new FileInputStream(
                file)));
    }

    /**
     * 
     * @param iS
     * @param iS2
     * @return true if the two independent inputstreams contain the same
     *         contents
     * @throws IOException
     */
    public static boolean testEquality(final InputStream iS, final InputStream iS2)
            throws IOException {
        int i;
        while ((i = iS.read()) != -1) {
            if (iS2.read() != i) {
				return false;
			}
        }
        if (iS2.read() != -1) {
            return false;
        }
        return true;
    }
}