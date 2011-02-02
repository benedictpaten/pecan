#!/usr/bin/env python

#Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
#
#Released under the MIT license, see LICENSE.txt
#!/usr/bin/env python

import sys
import os
import re
import random
import math

def keyWords(d):
    if "MATRIX" in d:
        d["MATRIX"] = "".join(os.popen("java bp.pecan.utils.F84Matrix -L %s" % d["MATRIX"]).readlines())

def randoms(d):
    randomP=re.compile("RANDOM\((.*)\)")
    for i in d.keys():
        if randomP.search(d[i]):
            d[i] = str(float(randomP.search(d[i]).group(1)) * random.random())
            
def main():
    d={}
    pair=re.compile("\[\!([^=]*)=(.*)\]")
    for i in open(sys.argv[1]).readlines():
        if pair.search(i):
            j = pair.search(i)
            d[j.group(1)] = j.group(2)
    keyWords(d)
    randoms(d)
    pair=re.compile("\[\$([^\]]*)\]")
    split=re.compile(",")
    for i in open(sys.argv[1]).readlines():
        if pair.search(i):
            j = pair.search(i).group(1)
            j = split.split(j)
            a = 0.0000000001
            for k in j:
                if k[1] is "$":
                    a += float(d[k[2:]]) * int(k[0])
                else:
                    a += float(d[k])
            for k in j:
                if k[1] is "$":
                    k = k[2:]
                d[k] = str(float(d[k])/a)
    for i in d.keys():
        if "MATRIX" not in i:
            d[i] = str(math.log(float(d[i])))
    pair=re.compile("([^\[]*)\[\#([^\]]*)\]")
    end=re.compile("([^\[\]]*)$")
    for i in open(sys.argv[1]).readlines():
        if pair.search(i):
            j=""
            for k in pair.findall(i):
                j = j + k[0] + d[k[1]] + " [#" + k[1] + "] "
            print j + end.search(i).group(1)[:-1]    
        else:
            print i[:-1]

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()

