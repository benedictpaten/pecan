/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Jan 7, 2005
 */
package bp.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;

import junit.framework.Assert;
import junit.framework.TestCase;
import bp.common.test.TestTools;

/**
 * @author benedictpaten
 */
public class ExternalExecutionTest
                                  extends TestCase {

    public ExternalExecutionTest(final String s) {
        super(s);
    }

    public void testRunSaveOutput() throws IOException {
        File f;
        f = File.createTempFile("externalExecutionTest_testRunSaveOutput",
                ".temp");
        f.deleteOnExit();
        final String cP = System.getProperties().getProperty("java.class.path");
        if (ExternalExecution
                .runSaveOutput(
                        new String[] {
                                "java",
                                "-cp",
                                cP,
                                "bp.common.io.ExternalExecutionTest",
                                ExternalExecution
                                        .getAbsolutePath("bp/common/io/externalExecutionTest_testRunSaveOutput") },
                        new BufferedOutputStream(new FileOutputStream(f.getAbsolutePath())), null) == -1) {
            Assert.fail();
            
        }
        Assert.assertTrue(TestTools
                .testEquality(
                        new BufferedInputStream(new FileInputStream(f)),
                        new BufferedInputStream(
                                new FileInputStream(
                                        ExternalExecution
                                                .getAbsolutePath("bp/common/io/externalExecutionTest_testRunSaveOutput")))));
    }

    public void testGetAbsolutePath() throws IOException {
        final String s = ExternalExecution
                .getAbsolutePath("bp/common/io/externalExecutionTest_testGetAbsolutePath");
        final LineNumberReader lNR = new LineNumberReader(new FileReader(s));
        Assert.assertTrue(lNR.readLine().equals("this is the right file!"));
    }

    public void testCreateFileOnClassPath() throws IOException {
        final String s = "bp/common/io/externalExecutionTest_testCreateFileOnClassPath";
        if (ExternalExecution.getAbsolutePath(s) != null) {
			new File(ExternalExecution.getAbsolutePath(s)).delete();
		}
        final String s2 = ExternalExecution.createFileOnClassPath(s);
        Assert.assertTrue(s2, ExternalExecution.getAbsolutePath(s) != null);
    }

    public static void main(final String[] args) throws IOException {
        final InputStream iS = new BufferedInputStream(new FileInputStream(args[0]));
        int i;
        while ((i = iS.read()) != -1) {
			System.out.print((char) i);
		}
    }
}