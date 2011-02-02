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
import bp.common.maths.Maths as Maths

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

MATCH_STATE = "match"
GAP_STATE = "gap"
__x = [0]
__y = [0]
xSeq = None
ySeq = None

"""
space for parameters
"""

MATCH_EMISSION_N_L = math.log(0.04)
MATCH_EMISSIONL = math.log(0.12064298095701059)
TRANSVERSION_EMISSIONL = math.log(0.010367271172731285)
TRANSITION_EMISSIONL = math.log(0.01862247669752685)

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

def match_de():
    return m[xSeq[x[0]]*5 + ySeq[y[0]]]

GAP_EMISSIONL = math.log(0.2)
GAP_EXTEND = math.log(0.974445284091146)
COMBINED = GAP_EMISSIONL + GAP_EXTEND

def gap_de(x1, y1, x2, y2):
    i = (x2 + y2) - (x1 + y1)
    return i*COMBINED - GAP_EXTEND 

def transitionFn(i):
    i = math.log(i)
    def fn():
        return i
    
MATCH = 0.9703833696510062
GAP_OPEN = (1.0 - MATCH) / 2;
GAP_CLOSE = 1.0 - GAP_EXTEND - GAP_SWITCH
GAP_SWITCH = 0.0007315179552849
    
match_match_t = transitionFn(MATCH)
match_gap_t = transitionFn(GAP_OPEN)
gap_match_t = transitionFn(GAP_CLOSE)
gap_gap_t = transitionFn(GAP_SWITCH)

"""
end space for parameters
"""

stateMachine = SparseAlign.StateMachine()
stateMachine.addState(MATCH_STATE, de, SparseAlign.StateMachine.MATCH)
stateMachine.addState(GAP_STATE, de, SparseAlign.StateMachine.GAP)
stateMachine.addTransition(MATCH_STATE, MATCH_STATE, t)
stateMachine.addTransition(MATCH_STATE, GAP_STATE, t)
stateMachine.addTransition(GAP_STATE, MATCH_STATE, t)
stateMachine.addTransition(GAP_STATE, GAP_STATE, t)

startStates = [ 0, Maths.NEG_INFINITY]
endStates = startStates[:]

def diagBuilder(i, j):
    raise NotImplementedError("Shouldn't be called")
            
def diagStart(i, j):
    __x[0] = i
    __y[0] = j
            
def diagEnd():
    pass

def main():
    pass

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()