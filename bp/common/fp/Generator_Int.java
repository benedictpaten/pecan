/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Feb 23, 2005
 */
package bp.common.fp;

/**
 * @author benedictpaten
 */
public interface Generator_Int {
    /**
     * Method should return a sequence of ints, no end state is stipulated so
     * caution is required.
     * 
     * @return
     */
    int gen();
}