#!/usr/bin/env python

#Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
#
#Released under the MIT license, see LICENSE.txt
#!/usr/bin/env python

import sys
import os
import re
import math


NEG_INFINITY = -1e30000

def exp(x):
    #return math.exp(x)
    if x > -2:
        if x > -0.5:
            if x > 0:
                return math.exp(x)
            return ((((0.03254409303190190000 * x + 0.16280432765779600000)\
                      * x + 0.49929760485974900000)\
                      * x + 0.99995149601363700000)\
                      * x + 0.99999925508501600000)
        if x > -1:
            return ((((0.01973899026052090000 * x + 0.13822379685007000000)\
                    * x + 0.48056651562365000000)\
                    * x + 0.99326940370383500000)\
                    * x + 0.99906756856399500000)
        return ((((0.00940528203591384000 * x + 0.09414963667859410000)\
                * x + 0.40825793595877300000)\
                * x + 0.93933625499130400000)\
                * x + 0.98369508190545300000)
    if x > -8:
        if x > -4:
            return ((((0.00217245711583303000 * x + 0.03484829428350620000)\
                    * x + 0.22118199801337800000)\
                    * x + 0.67049462206469500000)\
                    * x + 0.83556950223398500000)
        return ((((0.00012398771025456900 * x + 0.00349155785951272000)\
                * x + 0.03727721426017900000)\
                * x + 0.17974997741536900000)\
                * x + 0.33249299994217400000)

    if x > -16:
        return ((((0.00000051741713416603 * x + 0.00002721456879608080)\
                * x + 0.00053418601865636800)\
                * x + 0.00464101989351936000)\
                * x + 0.01507447981459420000)
    return 0
    

def log(x):
    return math.log(x)

def logAddQuality(x, y):
    if x < y:
        if x <= NEG_INFINITY:
            return y
        return math.log(math.exp(x - y) + 1) + y
    if y <= NEG_INFINITY:
        return x
    return math.log(math.exp(y - x) + 1) + x

__LOG_UNDERFLOW_THRESHOLD = 7.5
__LOG_ZERO = -2e20

    # three decimal places
def logAdd(x, y):
    """
    if x < y:
        if x <= __LOG_ZERO or y - x >= __LOG_UNDERFLOW_THRESHOLD:
            return y
        return lookup(y - x) + x
    if y <= __LOG_ZERO or x - y >= __LOG_UNDERFLOW_THRESHOLD:
         return x
    return lookup(x - y) + y;
    """
    return logAddQuality(x, y)

def lookup(x):
    #return (float)Math.log (Math.exp(x) + 1);
    if x <= 2.50: 
        if x <= 1.00:
            return ((-0.009350833524763 * x + 0.130659527668286)\
                    * x + 0.498799810682272)\
                    * x + 0.693203116424741
        return ((-0.014532321752540 * x + 0.139942324101744)\
                * x + 0.495635523139337)\
                * x + 0.692140569840976
    if x <= 4.50:
        return ((-0.004605031767994 * x + 0.063427417320019)\
                * x + 0.695956496475118)\
                * x + 0.514272634594009
    return ((-0.000458661602210 * x + 0.009695946122598) * x + 0.930734667215156)\
            * x + 0.168037164329057

def main():
    pass

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()