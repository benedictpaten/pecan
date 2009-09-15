#!/usr/bin/env python

import sys
import os
import re
import math
import SparseAlign
import bp.common.maths.Maths as Maths

GAP_EMISSIONL = math.log(0.2)

m = [    
#sum of 0.321517749109
0.226325055067,
0.0224727753477,
0.0380073910461,
0.0347125276478,
#sum of 0.189106784376
0.0224727753477,
0.116195875683,
0.014415360933,
0.0360227724124,
#sum of 0.191292685884
0.0380073910461,
0.014415360933,
0.116832509014,
0.0220374248906,
#sum of 0.298082780631
0.0347125276478,
0.0360227724124,
0.0220374248906,
0.205310055681


]

gapEmissions = [ 0.321517749109,
                0.189106784376,
                0.191292685884,
                0.298082780631 ]


GAP_SWITCH = Maths.NEG_INFINITY#math.log(0.0) #0.0007315179552849)
GAP_CLOSE = math.log(1.0) # - 0.0007315179552849)

MATCH_E = 0.9514458438518

#0.9703833696510062 #0.972775379521401
MATCH = math.log(MATCH_E)
GAP_OPEN = math.log((1.0 - MATCH_E) / 2)

class SparseHMM:
    """
    Must have the following fields

    stateMachine
    startStates
    endStates 
    diagStart
    diagBuilder
    diagEnd
    xSeq
    ySeq

    xSeq and ySeq are added by alignment program
    """
    def __init__(self):
        MATCH_STATE = "MATCH_STATE"
        GAP_STATEX = "GAP_STATEX"
        GAP_STATEY = "GAP_STATEY"
        self.xSeq = None
        self.ySeq = None
        self.xStart = None
        self.yStart = None
        
        self.stateMachine = SparseAlign.StateMachine()
        self.stateMachine.addState(MATCH_STATE, self.match_de, SparseAlign.StateMachine.MATCH)
        self.stateMachine.addState(GAP_STATEX, self.gap_deX, SparseAlign.StateMachine.GAPX)
        self.stateMachine.addState(GAP_STATEY, self.gap_deY, SparseAlign.StateMachine.GAPY)
        self.stateMachine.addTransition(MATCH_STATE, MATCH_STATE, self.transitionFn(MATCH))
        self.stateMachine.addTransition(MATCH_STATE, GAP_STATEX, self.transitionFn(GAP_OPEN))
        self.stateMachine.addTransition(GAP_STATEX, MATCH_STATE, self.transitionFn(GAP_CLOSE))
        self.stateMachine.addTransition(GAP_STATEX, GAP_STATEY, self.transitionFn(GAP_SWITCH))
        self.stateMachine.addTransition(MATCH_STATE, GAP_STATEY, self.transitionFn(GAP_OPEN))
        self.stateMachine.addTransition(GAP_STATEY, MATCH_STATE, self.transitionFn(GAP_CLOSE))
        self.stateMachine.addTransition(GAP_STATEY, GAP_STATEX, self.transitionFn(GAP_SWITCH))

        self.startStates = [ 0, 0, 0 ] #Maths.NEG_INFINITY, Maths.NEG_INFINITY]
        self.endStates = self.startStates[:]
        self.diagTraceBackLimit = 1
        
        global m, gapEmissions
        m = [ math.log(i) for i in m ]
        gapEmissions = [ math.log(i) for i in gapEmissions ]
        
        i = [ float(i) for i in open("gapDist", 'r').readlines() ]
        j = sum(i)
        i = [ math.log(k/j) for k in i ]
        self.gapDist = i
        
    def match_de(self, x2, y2, x, y):
        #print self.xStart, self.yStart, x2, y2, x, y
        return m[self.xSeq[x - self.xStart]*4 + self.ySeq[y - self.yStart]]
    
    def gap_deX(self, x1, x2, y2):
        try:
             i = self.xSeqG[x2+1 - self.xStart ] - self.xSeqG[x1+1 - self.xStart]
             return self.gapDist[x2 - x1] + i
        except IndexError:
            print "boo", x1, x2, y2, self.xStart, self.yStart, self.xSeqG
            raise IndexError
        
    
    def gap_deY(self, x1, x2, y2):
        i = self.ySeqG[x2+1 - self.yStart] - self.ySeqG[x1+1 - self.yStart]
        return self.gapDist[x2 - x1] + i
    
    #i*self.COMBINED - GAP_EXTEND 

    def transitionFn(self, i):
        def fn(*args):
            return i
        return fn

    def setSeqs(self, xSeq, xStart, ySeq, yStart):
        self.xStart = xStart
        self.yStart = yStart
        self.xSeq = self.mask(xSeq)
        self.ySeq = self.mask(ySeq)
        self.xSeqG = self.convertToProbs(self.xSeq)
        self.ySeqG = self.convertToProbs(self.ySeq)
    
    def mask(self, i):
         t = { 0:0, 1:1, 2:2, 3:3, 4:0 }
         return [ t[j] for j in i ]
        
    def convertToProbs(self, seq):
        seq = [ gapEmissions[i] for i in seq ]
        for i in xrange(1, len(seq)):
            seq[i] += seq[i-1]
        return [0.0] + seq
    
    def diagBuilder(self, i, j):
        raise NotImplementedError("Shouldn't be called")
            
    def diagStart(self, i, j):
        pass
            
    def diagEnd(self):
        pass
    
    def gapStartX(self, x, x2, y):
        pass
    
    def gapStartY(self, x, x2, y):
        pass
    
    def gapBuilderX(self, x2):
        pass
    
    def gapBuilderY(self, x2):
        pass
    
    def gapEndX(self):
        pass
    
    def gapEndY(self):
        pass

def main():
    pass

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()