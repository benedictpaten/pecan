#!/usr/bin/env python

#Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
#
#Released under the MIT license, see LICENSE.txt
#!/usr/bin/env python

import sys
import os
import re
from sets import Set
import logging

"""
A script to estimate a phylogenetic tree for a set of large co-linear sequences.
"""

###user editable

#default prefixes for alignment commands
PECAN_PREFIX = "java -server bp.pecan.Pecan"
PREPECAN_PREFIX = "java -server bp.pecan.utils.PrePecan"
CALL_PECAN = False
#default distance used on star tree, pretty unimportant for current Pecan (0.6)
DEFAULT_DISTANCE = 0.1
#number of iteration to do tree re-estimation
ITERATION_NUMBER = 3
#default logging level
LOGGING_LEVEL = logging.DEBUG

###probably don't look down

logger = logging.getLogger()
logger.setLevel(LOGGING_LEVEL)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(lineno)s %(message)s')
handler = logging.StreamHandler(sys.stderr)
logger.addHandler(handler)


class DistancePair:
    def __init__(self, distance, leaf1, leafNo1, leaf2, leafNo2):
        self.distance = distance
        self.leaf1 = leaf1
        self.leaf2 = leaf2
        self.leafNo1 = leafNo1
        self.leafNo2 = leafNo2
    
    def __cmp__(self, distancePair):
        if self.distance < distancePair.distance:
            return -1
        if self.distance > distancePair.distance:
            return 1
        return 0 #don't care
        #doesn't wort for floats return self.distance.__cmp__(distancePair.distance)

def upgma(distancePairs, leafNo):
    #get min pair
    distancePairs.sort()
    distancePair = distancePairs[0]
    #calculate shared distance
    newLeaf = (distancePair.leaf1, distancePair.leaf2, distancePair.distance)
    if leafNo-1 == 1:
        return newLeaf
    #replace references
    holder1 = {}
    holder2 = {}
    newDistances = []
    for i in distancePairs:
        if i.leaf1 == distancePair.leaf1 and i.leaf2 != distancePair.leaf2:
            holder1[i.leaf2] = i
        if i.leaf1 == distancePair.leaf2 and i.leaf2 != distancePair.leaf1:
            holder2[i.leaf2] = i
    assert len(holder1.keys()) == leafNo-2
    assert len(holder2.keys()) == leafNo-2
    assert Set(holder1.keys()) == Set(holder2.keys())
    for i in holder1.keys():
        j = holder1[i]
        k = holder2[i]
        newDistance = (j.distance*j.leafNo1 + k.distance*k.leafNo1)/(j.leafNo1 + k.leafNo1)
        newDistances.append(DistancePair(newDistance, j.leaf2, j.leafNo2, newLeaf, j.leafNo1 + k.leafNo1))
        newDistances.append(DistancePair(newDistance, newLeaf, j.leafNo1 + k.leafNo1, j.leaf2, j.leafNo2))
    distancePairs = [ i for i in distancePairs if (i.leaf1 != distancePair.leaf1 and i.leaf1 != distancePair.leaf2 and i.leaf2 != distancePair.leaf1 and i.leaf2 != distancePair.leaf2) ] + newDistances
    return upgma(distancePairs, leafNo-1)

def makeDistancePairs(distanceMatrix, seqNo):
    distancePairs = []
    for i in xrange(0, seqNo):
        for j in xrange(i+1, seqNo): 
            #take recipricals to make into distances
            distancePairs.append(DistancePair(1.0/distanceMatrix[i][j], str(i), 1, str(j), 1))
            distancePairs.append(DistancePair(1.0/distanceMatrix[i][j], str(j), 1, str(i), 1))
            logger.info("Making distance pair : %s %s %s " % (str(1.0/distanceMatrix[i][j]), str(i), str(j)))
    return distancePairs

def printTree(tree):
    """
    >>> printTree(('0', ('1', '2', 0.1), 0.4))
    '(0,(1,2))'
    >>> printTree(('0', (('1', '1b', 0.05), '2', 0.1), 0.4))
    '(0,((1,1b),2))'
    >>> printTree(('0', '1', 0.4))
    '(0,1)'
    >>>
    """
    def leafDistance(tree):
        if tree.__class__ == "".__class__:
            return 0.0
        return tree[2]/2
    if tree.__class__ == "".__class__:
        return tree
    #return '(' + printTree(tree[0]) + ':' + ("%f" % (tree[2]/2 - leafDistance(tree[0]))) + ',' + printTree(tree[1]) + ":" + ( "%f" % (tree[2]/2 - leafDistance(tree[1]))) + ')'
    return '(' + printTree(tree[0]) + ',' + printTree(tree[1]) + ')'

"""
Chairmonte matrix
 A    C    G    T    .    N
A   91 -114  -31 -123    0  -43
C -114  100 -125  -31    0  -43
G  -31 -125  100 -114    0  -43
T -123  -31 -114   91    0  -43
.    0    0    0    0    0    0
N  -43  -43  -43  -43    0  -43
"""
nucMatrix = { 
             'A':{ 'A':91, 'C':-114, 'G':-31, 'T':-123 }, 
             'C':{ 'A':-114, 'C':100, 'G':-125, 'T':-31 }, 
             'G':{ 'A':-31, 'C':-125, 'G':100, 'T':-114 }, 
             'T':{ 'A':-123, 'C':-31, 'G':-114, 'T':91 } }

def matchFn(i, j):
    return nucMatrix[i][j]

def countMatches(distanceMatrix, seqNo, fastaIter):
    for column in fastaIter:
        for i in xrange(0, seqNo):
            if column[i] in [ 'A', 'C', 'T', 'G' ]:
                for j in xrange(i+1, seqNo):
                    if column[j] in [ 'A', 'C', 'T', 'G' ]:
                        distanceMatrix[i][j] += matchFn(column[i], column[j])

def makeStarTree(seqNo, counter, defaultDistance):
    """
    >>> makeStarTree(2, 0, 0.1)
    '(0:0.100000, 1:0.000001)'
    >>> makeStarTree(3, 0, 0.1)
    '(0:0.100000, (1:0.100000, 2:0.000001):0.000001)'
    >>> makeStarTree(4, 0, 0.1)
    '(0:0.100000, (1:0.100000, (2:0.100000, 3:0.000001):0.000001):0.000001)'
    >>> makeStarTree(5, 0, 0.1)
    '(0:0.100000, (1:0.100000, (2:0.100000, (3:0.100000, 4:0.000001):0.000001):0.000001):0.000001)'
    >>>
    """
    if seqNo >= 2:
        return '(%i:%f, %s:%f)' % (counter, defaultDistance, makeStarTree(seqNo-1, counter+1, defaultDistance), 0.000001)
    return str(counter)

charMap = { 'a':'A', 'c':'C', 'g':'G', 't':'T' }

def mapCharFn(i):
    if charMap.has_key(i):
        return charMap[i]
    return i
        
def multiFastaRead(fasta, map):
    """
    reads in columns of multiple alignment and returns them iteratively
    """
    f = open(fasta, 'r')
    i = 0
    j = f.read(1)
    l = []
    while j != '':
        if j == '>':
            k = open(fasta, 'r')
            k.seek(i)
            while k.read(1) != '\n':
                pass
            l.append(k)
        i += 1
        j = f.read(1)
    f.close()
    if len(l) != 0:
        while True:
            i = [ i.read(1) for i in l ]
            for j in xrange(0, len(l)):
                while i[j] == '\n':
                    i[j] = l[j].read(1)
            if i[0] == '>' or i[0] == '':
                for j in xrange(1, len(l)):
                    assert i[j] == '>' or i[j] == ''
                break
            for j in xrange(1, len(l)):
                 assert i[j] != '>' and i[j] != ''
            yield [ map(j) for j in i ]
    for i in l:
        i.close()
        
def getLeftToRightLeafOrder(tree):
    if tree.__class__ == "".__class__:
        return ( tree,)
    return getLeftToRightLeafOrder(tree[0]) + getLeftToRightLeafOrder(tree[1])

def main():
    #get sequence files
    seqFiles = sys.argv[1:]
    seqNo = len(seqFiles)
    #run alignment
    treeString = makeStarTree(seqNo, 0, DEFAULT_DISTANCE) + ';'
    for iteration in xrange(0, ITERATION_NUMBER):
        ####edit this line to set
        os.system("%s -F %s -E '%s'" % (PREPECAN_PREFIX, " ".join(seqFiles), treeString))
        #parse alignment into columns
        #and count matches and lengths of matches
        fastaIter = multiFastaRead("output.mfa", mapCharFn)
        distanceMatrix = [ [1.0]*seqNo for i in xrange(0, seqNo) ]
        countMatches(distanceMatrix, seqNo, fastaIter)
        #call upgma
        tree = upgma(makeDistancePairs(distanceMatrix, seqNo), seqNo)
        treeString = printTree(tree) + ';'
        logger.info("On iteration : %i , found tree : %s" % (iteration, treeString))
    print "FINAL_TREE:", treeString
    print "------ sequences ordered according to leaves of given tree -----"
    orderedSequences = " ".join([ seqFiles[int(i)] for i in getLeftToRightLeafOrder(tree) ])
    print "ORDERED_SEQUENCES:", orderedSequences
    os.system("rm output.mfa")
    if CALL_PECAN:
        os.system("%s -F %s -E '%s'" % (PECAN_PREFIX, orderedSequences, treeString))

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()
