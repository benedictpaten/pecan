#!/usr/bin/env python

#Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
#
#Released under the MIT license, see LICENSE.txt
#!/usr/bin/env python

import sys
import os
import re
import math
import SparseAlign

def main():
    #load the 
    os.system("java bp.pecan.PECAN -E '(h, c);' -F HUMAN_1k DOG_1k -h 1000000 -A")
    matrix = SparseAlign.EM.readMatrix("eMFile", 3)
    i = matrix[SparseAlign.StateMachine.MATCH][SparseAlign.StateMachine.GAPX]
    j = matrix[SparseAlign.StateMachine.MATCH][SparseAlign.StateMachine.GAPY]
    i = [ i[k] + j[k] for k in xrange(0, len(i))]
    i = SparseAlign.EM.toProb(i)
    f = open("gapDist", 'w')
    for j in i:
        f.write("%s\n" % j)
    f.close()

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()