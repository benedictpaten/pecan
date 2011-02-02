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

    
def getStartStates(stateMachine):
    def fn(i):
        if i == MATCH:
            return 0
        return Maths.NEG_INFINITY
    return [ fn(i) for i in stateMachine.getStateTypes() ]

def alignScript(stateMachine,
                startStates,
                endStates, 
                diagStart,
                diagBuilder,
                diagEnd,
                
                xStart,
                yStart,
                pointsFile, 
                bottomLeftPointsFile, 
                topRightPointsFile, 
                outputFile,
                seqXFile,
                seqYFile,
                
                diagTraceBackLimit=100000,
                retotallingInterval=50):
    MATCH = SparseAlign.StateMachine.MATCH
    GAP = SparseAlign.StateMachine.GAP
    
    def fn(f):
        def fn2(i):
            i = i.split()
            return (i[0] - xStart, i[1] - yStart)
        return [ fn2(i) for i in open(pointsFile, 'r').readlines() ]
    points = fn(pointsFile)
    points.sort()
    bottomLeftPoints = fn(bottomLeftPointsFile)
    topRightPoints = fn(topRightPointsFile)
    
    def fn(f):
        return "".join([ i[:-1] for i in open("out", 'r').readlines() ])
    seqX = fn(seqXFile)
    seqY = fn(seqYFile)
    
    pPA = {}
    for i in points:
        pPA[i] = 0
    posteriorProbs = SparseAlign.PosteriorProbs(pPA, stateMachine.stateNo())
    
    rescale = SparseAlign.Rescale(sum(points[0][0:2])+1, stateMachine.stateNo()) 
    stateMachine.mapEmissions(rescale.rescaleFn)
    
    bTransition = SparseAlign.\
    BTransitionAndTotalReCalculator(posteriorProbs.total, \
                                    sum(points[0][0:2]), \
                                    sum(points[-1][0:2]),\
                                    retotallingInterval, \
                                    stateMachine.stateNo())
    
    backMatchFn = stateMachine.getFns(join(pP.bTransition, bTransition.bTransition))
    backGapFn = stateMachine.getFns(bTransition.bTransition)
    forwardFn = stateMachine.getFns(SparseAlign.fTransition(stateMachine.stateNo()))
    
    def join(one, two):
        def f(*args):
            two(*args)
            one(*args)
        return f
      
    SparseAlign.\
    sparseAlign(startStates,
                points, 
                bottomLeftPoints, 
                topRightPoints, 
                endStates,
                stateMachine.stateNo(),
                    
                forwardFn((MATCH, GAP)),
                forwardFn[(GAP, GAP)],
                diagStart,
                diagBuilder,
                diagEnd,
                forwardFn[(GAP, MATCH)],
                forwardFn[(MATCH, MATCH)],
                    
                backGapFn[(MATCH, GAP)],
                backGapFn[(GAP, GAP)],
                join(diagStart, posteriorProbs.diagStart),
                join(diagBuilder, posteriorProbs.diagBuilder),
                join(diagEnd, posteriorProbs.diagEnd),
                forwardFn[(GAP, MATCH)],
                forwardFn[(MATCH, MATCH)], 
                    
                posteriorProbs.total,
                rescale, diagTraceBackLimit)
    
    out = open(outputFile, 'w')
    for i in pPA.keys():
        out.write("%s %s %s \n" % (i[0] + xStart, i[1] + yStart, pPA[i]))
    out.close()
    

def main():
    #load alignment model
    stateMachineFile = sys.argv[1]
    exec "import %s as stateProgram" % stateMachineFile
    #first point will no
    pointsFile = sys.argv[2]
    bottomLeftPointsFile = sys.argv[3]
    topRightPointsFile = sys.argv[4]
    outputFile = sys.argv[5]
    seqXFile = sys.argv[6]
    seqYFile = sys.argv[7]
    xStart = sys.argv[8]
    yStart = sys.argv[9]
    
    alignScript(stateProgram.stateMachine,
                stateProgram.startStates,
                stateProgram.endStates, 
                stateProgram.diagStart,
                stateProgram.diagBuilder,
                stateProgram.diagEnd,
                
                pointsFile, 
                bottomLeftPointsFile, 
                topRightPointsFile, 
                outputFile,
                seqXFile,
                seqYFile)

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()