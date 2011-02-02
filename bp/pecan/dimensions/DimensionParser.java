/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

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
