#!/usr/bin/env python

import sys
import os
import re
import math

GAP = -1000000

def readExons(exonsFile, max):
    exons = []
    for i in open(exonsFile, 'r').readlines()[1:]:
        if len(i) > 1:
            j = i.split()[1].split("-")
            if len(j) == 2 and int(j[0]) < max:
                exons.append((int(j[0]), int(j[1])))
    return exons

def fileLength(mfaFile):
    i = "".join([ i[:-1] for i in open(mfaFile, 'r').readlines()[1:] ])
    return len(i) - len([ j for j in i if j == '-' ])

def readMFAPairs(mfaFile1, mfaFile2):
    """
    reads in pairs of matches bases between seqs in mfa
    """
    def fn(file):
        return "".join([ i[:-1] for i in open(file, 'r').readlines()[1:] ])
    j = [0]
    def fn2(i):
        if i == '-':
            return GAP
        k = j[0]
        j[0] += 1
        return k
    mfa1 = fn(mfaFile1)
    mfa2 = fn(mfaFile2)
    mfa2 = [ fn2(i) for i in mfa2 ]
    assert len(mfa1) == len(mfa2)
    return [ mfa2[i] for i in xrange(0, len(mfa1)) if mfa1[i] != '-' ]

def calculateOverlap(mfaPairs, exons1, exons2):
    """
    returns
    (EXON_SIZE, 
    NO_MATCHED_TO_EXONS, 
    NO_MATCHED_TO_NON_EXONS,
    NO_MATCHED_TO_GAPS_IN_EXONS, 
    NO_MATCHED_TO_GAPS_NOT_IN_EXONS)
    """
    exonSize = 0
    noMatchToExons = 0
    noMatchToNonExons = 0
    noMatchToGapsInExons = 0
    noMatchToGapsNotInExons = 0
    
    p = -100000
    for i in exons1:
        exonSize += i[1] - i[0]
        for j in xrange(i[0], i[1]):
            k = mfaPairs[j]
            l = k
            if k == GAP:
                l = p  
            for m in exons2:
                if(l >= m[0] and l < m[1]):
                    if k == GAP:
                        noMatchToGapsInExons += 1
                    else:
                        noMatchToExons += 1
                    break
            else:
                if k == GAP:
                    noMatchToGapsNotInExons += 1
                else:
                    noMatchToNonExons += 1
            if k != GAP:
                p = k
    return (exonSize, noMatchToExons, noMatchToNonExons,\
            noMatchToGapsInExons, noMatchToGapsNotInExons)

def calculateIndels(mfaPairs, regions):
    """
    calculates number of gaps of different sizes
    """
    gapLength = 0
    gaps = [0]*1000
    for i in regions:
        for j in xrange(i[0], i[1]):
            k = mfaPairs[j]
            if k == GAP:
                gapLength += 1
            else:
                if gapLength != 0:
                    gaps[gapLength] += 1
                    gapLength = 0
    return gaps

def run(exonsFile1, exonsFile2,
        mfaFile1, mfaFile2, notIndels):
    exons1 = readExons(exonsFile1, fileLength(mfaFile1))
    exons2 = readExons(exonsFile2, fileLength(mfaFile2))
    #print exons1, exons2
    mfaPairs = readMFAPairs(mfaFile1, mfaFile2)
    #print mfaPairs
    assert len(mfaPairs) == fileLength(mfaFile1)
    overallOverlap = calculateOverlap(mfaPairs, exons1, exons2)
    individualOverlap = [ calculateOverlap(mfaPairs, [ i ], exons2)\
                          for i in exons1]
    indelDist = calculateIndels(mfaPairs, exons1)
    if notIndels:
        message = """
        EXON_SIZE, NO_MATCHED_TO_EXONS, 
        NO_MATCHED_TO_NON_EXONS,
        NO_MATCHED_TO_GAPS_IN_EXONS, 
        NO_MATCHED_TO_GAPS_NOT_IN_EXONS
        """
        print "overall", message
        print " ".join([ str(i) for i in overallOverlap])
        for i in individualOverlap:
            print " ".join([ str(j) for j in i])
    else:
        print " ".join([ str(i) for i in indelDist])       

def main():
    run(sys.argv[1], sys.argv[2],
        sys.argv[3], sys.argv[4],
        bool(int(sys.argv[5])))


def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()