#!/usr/bin/env python

import sys
import os
import re
import math
import unittest
import random as r
import SparseAlign
import bp.common.maths.Maths as Maths

class TestCase(unittest.TestCase):
    
    def setUp(self):
        unittest.TestCase.setUp(self)
    
    def tearDown(self):
        unittest.TestCase.tearDown(self)
        
    def testSparseArray(self):
        for test in xrange(0, 100):
            p = list(set([ (r.randrange(0, 100), r.randrange(0, 100)) for i in xrange(0, 200)]))
            p = [ (i[0], i[1], r.randrange(0, 100)) for i in p]
            start = -r.randrange(0, 100)
            end = r.randrange(100, 200)
            sP = SparseAlign.SparseArray(start, end, p[:])
            pS = p[:]
            pS.sort()
            for i in p:
                j = sP.yS(i[0])
                k = sP.y[j:sP.yS(i[0]+1)]
                assert i[1] in k, "%s %s %s" % (i, k, pS)
                assert sP.e[j + k.index(i[1])] == i[2], "%s %s" % (sP.e[j + k.index(i[1])], i[2])
                l = k[:]
                l = list(set(l))
                l.sort()
                assert l == k, "%s %s" % (l, k)
            for i in xrange(start, end+1):
                sP.yS(i)
                sP.yS(i+1)
    
    def totalsEqual(self, s1, s2, stateNo):
        self.assertAlmostEqual(s, s2, 0.0001)
    
    def testSparseAlign(self):
        for test in xrange(0, 100):
            stateMachine = makeRandomStateMachine()
            size = int(1 + 10*r.random())
            points = getPoints(size, 3, 3)
            t1, t2, ppA = self.sparseAlignSimple(stateMachine, points, size)
            t3, t4, ppA2 = self.sparseAlignReal(stateMachine, points)
            print "totals %s %s %s %s" % (t1, t2, t3, t4)
            print ppA
            #print t1, t4
            infT = -10e30000
            if t1 == infT and t4 == infT and t2 == infT:
                continue
            #self.assertAlmostEqual(t1, t4, 3)
            #self.assertAlmostEqual(t1, t2, 3)
            #self.assertAlmostEqual(t3, t4, 3)
            #self.assertAlmostEqual(t1, t3, 3)
            
            for i in ppA:
                j = math.exp(ppA[i])
                ppA[i] = j
            
            print ppA
            print "finish"
            print ppA2
            for i in ppA:
                j = ppA[i]    
                assert j <= 1.02
                assert j >= 0.0
                self.assertAlmostEqual(ppA[i], ppA2[i], 1)
            self.assertEqual(len(ppA), len(ppA2))
            
    def testSparseAlignBig(self):
        print "sparse align big"
        for test in xrange(0, 100):
            stateMachine = makeRandomStateMachine()
            size = int(1 + 100*r.random())
            points = getPoints(size, 0.1, 0.1)
            #t1, t2, ppA = self.sparseAlignSimple(stateMachine, points, size)
            t3, t4, ppA = self.sparseAlignReal(stateMachine, points)
            #print "totals %s %s %s %s" % (t1, t2, t3, t4)
            print ppA
            #print t1, t4
            infT = -10e30000
            if t3 == infT:
                continue
            #self.assertAlmostEqual(t1, t4, 3)
            #self.assertAlmostEqual(t1, t2, 3)
            #self.assertAlmostEqual(t3, t4, 3)
            #self.assertAlmostEqual(t1, t3, 3)
            
            #print ppA
            print t3
            print "finish"
            for i in ppA:
                j = ppA[i] 
                #print j   
                assert j <= 1.01
                assert j >= -0.01
                #self.assertAlmostEqual(ppA[i], ppA2[i], 4)
            #self.assertEqual(len(ppA), len(ppA2))
        
    def sparseAlignReal(self, stateMachine, points):
        p = points["points"][:]
        gp = points["gapPoints"][:]
        blp = points["blGaps"][:]
        trp = points["trGaps"][:]
        
        pPA = {}
        for i in p:
            pPA[i] = 0
            
        def join(one, two):
            def f(*args):
                two(*args)
                one(*args)
            return f
        
        total = [0]
        def totalGetter(t):
            total[0] = t
            pP.total(t)
            
        
        
        #totalFn, startDiagonal, endDiagonal, interval
        rescale = SparseAlign.Rescale(1, stateMachine.stateNo())
        
        stateMachine.mapEmissions(rescale.rescaleFn)
        
        pPoints = p[:]
        pPoints.sort()
        pPoint = pPoints[len(pPoints)-1]
        bTransition = SparseAlign.BTransitionAndTotalReCalculator(totalGetter, 0, pPoint[0] + pPoint[1], r.randrange(1, 2 + 1.5*(pPoint[0] + pPoint[1])), stateMachine.stateNo())
        pP = SparseAlign.PosteriorProbs(pPA, stateMachine.stateNo())
        fn = stateMachine.getFns(join(pP.bTransition, bTransition.bTransition))
        fn_2 = stateMachine.getFns(bTransition.bTransition)
        fn2 = stateMachine.getFns(SparseAlign.fTransition(stateMachine.stateNo()))
        
        x1 = [0]
        y1 = [0]
        x2 = [0]
        y2 = [0]
        def diagBuilder(i, j):
            x2[0] = i
            y2[0] = j
            
        def diagStart(i, j):
            x1[0] = i
            y1[0] = j
            x2[0] = i
            y2[0] = j
            
        def diagEnd():
            pass
                
        def dFW(diagFn):
            def f(s1, sI, s2, sI2, ):
                return diagFn(s1, sI, s2, sI2, x2[0]-1, y2[0]-1, x1[0], y1[0])
            return f
            
        def fn3(i):
            if i == MATCH:
                return 0
            return Maths.NEG_INFINITY
        startStates = [ fn3(i) for i in stateMachine.getStateTypes() ]
        SparseAlign.sparseAlign(startStates[:],#*stateMachine.stateNo(),
                    p, blp, trp, 
                    startStates[:],#[0]*stateMachine.stateNo(),
                    stateMachine.stateNo(),
                    
                    fn2[(MATCH, GAP)],
                    fn2[(GAP, GAP)],
                    diagStart,
                    diagBuilder,
                    diagEnd,
                    dFW(fn2[(GAP, MATCH)]),
                    dFW(fn2[(MATCH, MATCH)]),
                    
                    fn_2[(MATCH, GAP)],
                    fn_2[(GAP, GAP)],
                    join(diagStart, pP.diagStart),
                    join(diagBuilder, pP.diagBuilder),
                    join(diagEnd, pP.diagEnd),
                    dFW(fn[(GAP, MATCH)]),
                    dFW(fn[(MATCH, MATCH)]), 
                    
                    totalGetter,
                    rescale, 1000000)
        return (total[0], total[0], pPA)
        
            
    def sparseAlignSimple(self, stateMachine, points, size):
        p = points["points"][:]
        gp = points["gapPoints"][:]
        blp = points["blGaps"][:]
        trp = points["trGaps"][:]
        
        print "points ", p
        print "gapPoints ", gp
        print "blPoints ", blp
        print "trPoints ", trp
        
        pPA = {}
        for i in p:
            pPA[i] = Maths.NEG_INFINITY
        
        def makeMatrix(values):
            matrix = [ [False]*size for i in xrange(0, size) ]
            for i in values:
                matrix[i[0]][i[1]] = True
            return matrix
        fMatrix = [ [ [Maths.NEG_INFINITY]*stateMachine.stateNo() for j in xrange(0, size) ] for i in xrange(0, size) ]
        bMatrix = [ [ [Maths.NEG_INFINITY]*stateMachine.stateNo() for j in xrange(0, size) ] for i in xrange(0, size) ]
    
        def fn3(i):
            if i == MATCH:
                return 0
            return Maths.NEG_INFINITY
        startStates = [ fn3(i) for i in stateMachine.getStateTypes() ]
    
    
        fMatrix[0][0] = startStates[:]  #[0]*stateMachine.stateNo()
        bMatrix[size-1][size-1] = startStates[:]#[0]*stateMachine.stateNo()
        
        #fMatrix[0][0] = [0]*stateMachine.stateNo()
        #bMatrix[size-1][size-1] = [0]*stateMachine.stateNo()
    
        def bTransition(s, sI, s2, sI2, i, j, t, de, *args):
            s2[i] = Maths.logAdd(s2[i], s[j] + t + de)
    
        print "doing backwards "
    
        fn = stateMachine.getFns(bTransition)
        computeMatrixR(bMatrix, makeMatrix(gp),
                       makeMatrix(p),
                       makeMatrix(blp),
                       makeMatrix(trp),
                       fn[(MATCH, GAP)],
                       fn[(GAP, GAP)],
                       fn[(GAP, MATCH)],
                       fn[(MATCH, MATCH)])
        stateNo = stateMachine.stateNo()
        total = SparseAlign.logSum([ bMatrix[0][0][i] + startStates[i] for i in range(0, stateNo)]) 
        #total = bMatrix[0][0][0]#SparseAlign.logSum(bMatrix[0][0])
        stateNo = stateMachine.stateNo()
        
        print "doing forwards "
        
        def fTransition(s, sI, s2, sI2, i, j, t, de, x2, y2, x1, y1):
            l = s[i] + t + de
            s2[j] = Maths.logAdd(s2[j], l)
            l += bMatrix[x1][y1][j] - total
            #print x1, x2, "boo"
            if x2 >= x1:
                raise IndexError()
            for k in xrange(0, x1-x2):
                pPA[(x1-k, y1-k)] = Maths.logAdd(pPA[(x1-k, y1-k)], l)
        fn = stateMachine.getFns(fTransition)
        
        def fTransition2(s, sI, s2, sI2, i, j, t, de, x2, y2, x1, y1):
            l = s[i] + t + de
            s2[j] = Maths.logAdd(s2[j], l)
        fn2 = stateMachine.getFns(fTransition2)
        
        print "gp", gp
        
        computeMatrix(fMatrix, makeMatrix(gp),
                      makeMatrix(p),
                      makeMatrix(blp),
                      makeMatrix(trp),
                      fn2[(MATCH, GAP)],
                      fn2[(GAP, GAP)],
                      fn[(GAP, MATCH)],
                      fn[(MATCH, MATCH)])
        print "fmatrix ", fMatrix
        
        print "bmatrix ", bMatrix
        
        print fMatrix[0][0], "cro", SparseAlign.logSum(bMatrix[0][0])
        return (SparseAlign.logSum([ fMatrix[size-1][size-1][i] + startStates[i] for i in range(0, stateNo)]), 
                SparseAlign.logSum([ bMatrix[0][0][i] + startStates[i] for i in range(0, stateNo)]) , pPA)
        #return (SparseAlign.logSum(fMatrix[size-1][size-1]), \
        #SparseAlign.logSum(bMatrix[0][0]), pPA)
        #totalsEqual(fMatrix[size-1][size-1], bMatrix[0][0], stateMachine.stateNo())   
        #return (fMatrix[size-1][size-1], bMatrix[0][0], pPA)
        
def getRandomFn():
    xA = [ math.log(r.random()) for i in xrange(0, 100) ]
    yA = [ math.log(r.random()) for i in xrange(0, 100) ]
    def d(x2, y2, x, y):
        return sum(xA[x2:x]) + sum(yA[y2:y])
    return d

GAP = SparseAlign.StateMachine.GAP
MATCH = SparseAlign.StateMachine.MATCH
    
def makeRandomStateMachine():
    stateMachine = SparseAlign.StateMachine()
    states = [ ("state_%s" % i) for i in xrange(0, r.randrange(2, 10))]
    states[0] = (states[0], MATCH)
    states[1] = (states[1], GAP)
    for i in range(2, len(states)):
       states[i] = (states[i], r.randrange(0, 2))      
    for i in states:
        if i[1] == GAP:
            stateMachine.addState(i[0], getRandomFn(), GAP)
        else:
            stateMachine.addState(i[0], getRandomFn(), MATCH)
    stateMachine.addTransition(states[0][0], states[1][0], getRandomFn())#randomTransitionFn())
    stateMachine.addTransition(states[1][0], states[0][0], getRandomFn())#randomTransitionFn())
    stateMachine.addTransition(states[0][0], states[0][0], getRandomFn())#randomTransitionFn())
    stateMachine.addTransition(states[1][0], states[1][0], getRandomFn())#randomTransitionFn())
    while r.random() > 0.1:
        stateMachine.addTransition(states[r.randrange(0, len(states))][0], states[r.randrange(0, len(states))][0], getRandomFn())
    return stateMachine
    
def getPoints(size, ratio, ratio2):
    def min2(i):
        if i < 2:
            return 2
        return int(i)
    p = [ (r.randrange(0, size), r.randrange(0, size)) for i in xrange(0, r.randrange(0, min2(ratio*size*size)))] #30*size*size))]
    p = [(0, 0)] + p
    p.append((size -1, size -1))
    p = list(set(p))
    gp = [ (i[0]-1, i[1]-1) for i in p if i[0] > 0 and i[1] > 0 ] # and not (i[0]-1 == 0 and i[1]-1 == 0)]
    j = set(p + gp)
    xg = list(set([ (r.randrange(0, size), r.randrange(0, size)) for i in xrange(0, r.randrange(0, min2(ratio2*size*size)))]))
    yg = list(set([ (r.randrange(0, size), r.randrange(0, size)) for i in xrange(0, r.randrange(0, min2(ratio2*size*size)))]))
    #xg = []#list(set([ (r.randrange(0, size), r.randrange(0, size)) for i in xrange(0, r.randrange(0, 100))]).difference(j))
    #yg = []#list(set([ (r.randrange(0, size), r.randrange(0, size)) for i in xrange(0, r.randrange(0, 100))]).difference(j))
    return { "points":p, "gapPoints":gp, "blGaps":xg, "trGaps":yg }
       
def computeMatrix(matrix,
                  legalGapMatrix,
                  legalMatchMatrix,
                  legalBLGapGapMatrix,
                  legalTRGapGapMatrix,
                  
                  gap, gapGap,
                  diagGap, diagMatch):
    for i in xrange(0, len(matrix[0])):
        for j in xrange(0, len(matrix[i])):
            if legalGapMatrix[i][j]:
                for k in xrange(0, i):
                    if legalMatchMatrix[k][j]:
                        gap(matrix[k][j], 0, matrix[i][j], 0, k, j, i, j)
                    if legalBLGapGapMatrix[k][j]:
                        gapGap(matrix[k][j], 0, matrix[i][j], 0, k, j, i, j)
                for k in xrange(0, j):
                    if legalMatchMatrix[i][k]:
                        gap(matrix[i][k], 0, matrix[i][j], 0, i, k, i, j)
                    if legalTRGapGapMatrix[i][k]:
                        gapGap(matrix[i][k], 0, matrix[i][j], 0, i, k, i, j)
            
            if legalBLGapGapMatrix[i][j] and not legalGapMatrix[i][j]:
                for k in xrange(0, j):
                    if legalMatchMatrix[i][k]:
                        gap(matrix[i][k], 0, matrix[i][j], 0, i, k, i, j)
                
            if legalTRGapGapMatrix[i][j] and not legalGapMatrix[i][j]:
                for k in xrange(0, i):
                    if legalMatchMatrix[k][j]:
                        gap(matrix[k][j], 0, matrix[i][j], 0, k, j, i, j)
            
            if legalMatchMatrix[i][j]:
                for k in xrange(1, i+1):
                    if j-k >= 0:
                        if legalGapMatrix[i-k][j-k]:
                            diagGap(matrix[i-k][j-k], 0, matrix[i][j], 0, i-k, j-k, i, j)
                        if legalMatchMatrix[i-k][j-k]:
                            diagMatch(matrix[i-k][j-k], 0, matrix[i][j], 0, i-k, j-k, i, j)
                        else:
                            break
                        
                            
def computeMatrixR(matrix,
                   legalGapMatrix,
                   legalMatchMatrix,
                   legalBLGapGapMatrix,
                   legalTRGapGapMatrix,
                  
                   gap, gapGap,
                   diagGap, diagMatch):
    for i in xrange(len(matrix)-1, -1, -1):
        dd = matrix[i]
        for j in xrange(len(matrix[i])-1, -1, -1):
            if legalMatchMatrix[i][j]:
                for k in xrange(i+1, len(matrix[0])):
                    if legalGapMatrix[k][j]:
                        gap(matrix[k][j], 0, matrix[i][j], 0, i, j, k, j)
                    
                    if legalTRGapGapMatrix[k][j] and not legalGapMatrix[k][j]:
                        gap(matrix[k][j], 0, matrix[i][j], 0, i, j, k, j)
                
                for k in xrange(j+1, len(matrix[0])):
                    if legalGapMatrix[i][k]:
                        gap(matrix[i][k], 0, matrix[i][j], 0, i, j, i, k)
                    
                    if legalBLGapGapMatrix[i][k] and not legalGapMatrix[i][k]:
                        gap(matrix[i][k], 0, matrix[i][j], 0, i, j, i, k)
                
            if legalBLGapGapMatrix[i][j]:
                for k in xrange(i+1, len(matrix[0])):
                    if legalGapMatrix[k][j]:
                        gapGap(matrix[k][j], 0, matrix[i][j], 0, i, j, k, j)
                        
            if legalTRGapGapMatrix[i][j]:
                for k in xrange(j+1, len(matrix[0])):
                    if legalGapMatrix[i][k]:
                        gapGap(matrix[i][k], 0, matrix[i][j], 0, i, j, i, k)
            
            if legalGapMatrix[i][j]:
                for k in xrange(1, len(matrix[0])):
                    if j+k < len(matrix[0]) and i+k < len(matrix[0]):
                        if legalMatchMatrix[i+k][j+k]:
                            diagGap(matrix[i+k][j+k], 0, matrix[i][j], 0, i, j, i+k, j+k)
                        else:
                            break
                        
            if legalMatchMatrix[i][j]:
                for k in xrange(1, len(matrix[0])):
                    if j+k < len(matrix[0]) and i+k < len(matrix[0]):
                        if legalMatchMatrix[i+k][j+k]:
                            diagMatch(matrix[i+k][j+k], 0, matrix[i][j], 0, i, j, i+k, j+k)
                        else:
                            break
        
if __name__ == '__main__':
    unittest.main()

def main():
    pass

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()