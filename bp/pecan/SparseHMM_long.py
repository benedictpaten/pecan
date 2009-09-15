#!/usr/bin/env python

import sys
import os
import re
import math
import SparseAlign
import bp.common.maths.Maths as Maths

MATCH_EMISSION_N_L = math.log(0.04)
MATCH_EMISSIONL = math.log(0.12064298095701059)
TRANSVERSION_EMISSIONL = math.log(0.010367271172731285)
TRANSITION_EMISSIONL = math.log(0.01862247669752685)
GAP_EMISSIONL = math.log(0.2)

m = [ 
     MATCH_EMISSIONL,
     TRANSVERSION_EMISSIONL, TRANSITION_EMISSIONL,
     TRANSVERSION_EMISSIONL, MATCH_EMISSION_N_L,
     TRANSVERSION_EMISSIONL, MATCH_EMISSIONL,
     TRANSVERSION_EMISSIONL, TRANSITION_EMISSIONL,
     MATCH_EMISSION_N_L, TRANSITION_EMISSIONL,
     TRANSVERSION_EMISSIONL, MATCH_EMISSIONL,
     TRANSVERSION_EMISSIONL, MATCH_EMISSION_N_L,
     TRANSVERSION_EMISSIONL, TRANSITION_EMISSIONL,
     TRANSVERSION_EMISSIONL, MATCH_EMISSIONL,
     MATCH_EMISSION_N_L, MATCH_EMISSION_N_L,
     MATCH_EMISSION_N_L, MATCH_EMISSION_N_L,
     MATCH_EMISSION_N_L, MATCH_EMISSION_N_L 
 ]

MATCH_E = 0.9703833696510062
GAP_OPEN_E = 0.0129868352330243
GAP_EXTEND_E = 0.7126062401851738
GAP_SWITCH_E = 0.0073673675173412815
JUNK_EXTEND_E = 0.99656342579062

MATCH = math.log(MATCH_E)
GAP_OPEN = math.log(GAP_OPEN_E)
JUNK_OPEN = math.log((1.0 - (MATCH_E + 2 * GAP_OPEN_E)) / 2)

GAP_EXTEND = math.log(GAP_EXTEND_E)
GAP_CLOSE = math.log(1.0 - GAP_EXTEND_E - GAP_SWITCH_E)
GAP_SWITCH = math.log(GAP_SWITCH_E)

JUNK_EXTEND = math.log(JUNK_EXTEND_E)
JUNK_CLOSE = math.log(1.0 - JUNK_EXTEND_E) 

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
        self.stateMachine.addState(GAP_STATEX, self.gap_de, SparseAlign.StateMachine.GAPX)
        self.stateMachine.addState(GAP_STATEY, self.gap_de, SparseAlign.StateMachine.GAPY)
        self.stateMachine.addTransition(MATCH_STATE, MATCH_STATE, self.transitionFn(MATCH))
        self.stateMachine.addTransition(MATCH_STATE, GAP_STATEX, self.transitionFn(0.0))
        self.stateMachine.addTransition(GAP_STATEX, MATCH_STATE, self.transitionFn(0.0))
        self.stateMachine.addTransition(GAP_STATEX, GAP_STATEY, self.transitionFn(GAP_SWITCH))
        self.stateMachine.addTransition(MATCH_STATE, GAP_STATEY, self.transitionFn(0.0))
        self.stateMachine.addTransition(GAP_STATEY, MATCH_STATE, self.transitionFn(0.0))
        self.stateMachine.addTransition(GAP_STATEY, GAP_STATEX, self.transitionFn(GAP_SWITCH))

        self.startStates = [ 0, 0, 0 ] #Maths.NEG_INFINITY, Maths.NEG_INFINITY]
        self.endStates = self.startStates[:]
        self.diagTraceBackLimit = 1
        
        self.COMBINED = GAP_EMISSIONL + GAP_EXTEND
        self.COMBINED2 = GAP_EMISSIONL + JUNK_EXTEND
        
    def match_de(self, x2, y2, x, y):
        #print self.xStart, self.yStart, x2, y2, x, y
        return m[self.xSeq[x - self.xStart]*5 + self.ySeq[y - self.yStart]]

    def gap_de(self, x1, x2, y2):
        i = x2 - x1
        return Maths.logAdd(GAP_OPEN + i*self.COMBINED - GAP_EXTEND + GAP_CLOSE, 
                            JUNK_OPEN + i*self.COMBINED2 - JUNK_EXTEND + JUNK_CLOSE)
        #return i*self.COMBINED - GAP_EXTEND 
    

    def transitionFn(self, i):
        def fn(*args):
            return i
        return fn

    def setSeqs(self, xSeq, xStart, ySeq, yStart):
        self.xStart = xStart
        self.yStart = yStart
        self.xSeq = xSeq
        self.ySeq = ySeq
    
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