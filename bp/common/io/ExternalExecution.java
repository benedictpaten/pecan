/*
 * Created on Jan 7, 2005
 */
package bp.common.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author benedictpaten
 */
public class ExternalExecution {

    /**
     * Runs a command in a seperate process and catches the output. See
     * {@link java.Runtime.exec}for an explantion of envp.
     * 
     * @param cmd
     *            command to execute
     * @param os
     * @param envp
     *            environment variable list
     * @return returns exit value of the process or -1 if an
     *         InterruptedException is caught
     * @throws IOException
     */
    public static int runSaveOutput(final String[] cmd, final OutputStream oS,
            final String[] envp) throws IOException {
        final Process p = Runtime.getRuntime().exec(cmd, envp);
        final InputStream iS = p.getInputStream();
        final InputStream eS = p.getErrorStream();
        final OutputStream oSP = p.getOutputStream();
        final StreamPump sO = new StreamPump(iS, oS), sE = new StreamPump(eS, System.err);
        sO.start();
        sE.start();
        try {
            p.waitFor();//waiting for process to complete
        } catch (final InterruptedException e) {
            return -1;
        }
        while (sO.isAlive() || sE.isAlive()) {
			;
		}
        iS.close();
        eS.close();
        oSP.close();
        oS.flush();
        return p.exitValue();
    }

    static class StreamPump
                           extends Thread {
        InputStream iS;

        OutputStream oS;

        StreamPump(final InputStream iS, final OutputStream oS) {
            this.iS = iS;
            this.oS = oS;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
		public void run() {
            int b;
            try {
                while ((b = this.iS.read()) != -1) {
                    this.oS.write(b);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the path string of a file name found on the classpath. For files
     * contained within packages prepend the name with the package hierachy,
     * replacing the usual . with a /.
     * 
     * @param s
     * @return
     */
    public static String getAbsolutePath(final String s) {
        final URL f = ClassLoader.getSystemResource(s);
        return f != null ? f.getFile() : null;
    }

    /**
     * Creates an empty file at the specified position in the classpath. If the
     * file already exists then the file is left untouched. Returns null if the
     * parent path is itself invalid.
     * 
     * @param s
     * @return The absolute path name of the new file
     * @throws IOException
     */
    public static String createFileOnClassPath(final String s)
            throws IOException {
        String s2;
        if ((s2 = ExternalExecution.getAbsolutePath(s)) != null) {
            return s2;
        }
        if ((s2 = ExternalExecution.getAbsolutePath(new File(s).getParent())) != null) {
            final File f = new File(s2, new File(s).getName());
            f.createNewFile();
            return f.getAbsolutePath();
        }
        return null;
    }
}