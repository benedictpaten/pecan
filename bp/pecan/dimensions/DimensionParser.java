/*
 * Created on Feb 17, 2005
 */
package bp.pecan.dimensions;

import java.io.InputStreamReader;

/**
 * @author benedictpaten
 */
public interface DimensionParser {
    
    Dimension[] parseDimensions(InputStreamReader reader);
}
