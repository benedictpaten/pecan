/*
 * Created on 12 Oct 2006
 */
package bp.scripts;

import java.io.FileWriter;
import java.io.PrintWriter;

public class TestInput {
    public static void main(final String[] args) throws Exception {
        final PrintWriter w = new PrintWriter(new FileWriter("output_file"));
        for (final String element : args) {
            w.println(element);
        }
        w.flush();
        w.close();
    }
}
