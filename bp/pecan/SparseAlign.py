#!/usr/bin/env python

import sys
import os
import re
import bp.common.maths.Maths as Maths

SUBTRACT_MAX = 10000000

class StateMachine:
    GAP = 0
    MATCH = 1

    def __init__(self):
        self.__stateNo = 0
        self.__names = {}
        self.__types = [[], []]
        self.__des = []
        self.__outT = []
    
    def stateNo(self):
        return self.__stateNo
  
    def addState(self, name, de, type):
        self.__names[name] = self.__stateNo
        self.__types[type].append(self.__stateNo)
        self.__outT.append([])
        self.__des.append(de)
        self.__stateNo += 1

    def addTransition(self, nameFrom, nameTo, t):
        i = self.__names[nameFrom]
        j = self.__names[nameTo]
        self.__outT[i].append((j, t))

    def fromToFn(self, fromType, toType, action):
        actions = {}
        for j in self.__types[fromType]:
            for t in self.__outT[j]:
                k = t[0]
                if k in self.__types[toType]:
                    if actions.has_key(k):
                        actions[k].append((j, t[1]))
                    else:
                        actions[k] = [(j, t[1])]
        actions = [ (i, actions[i]) for i in actions.keys() ]
               
        def fn(s, sI, s2, sI2, *args):
            for i in actions:
                j = i[0]
                d = self.__des[j](*args)
                for k in i[1]:
                    l = k[0]
                    t = k[1]
                    action(s, sI, s2, sI2, l, j, t(*args), d, *args)
        return fn
    
    def getMatchStates(self):
        return self.__types[self.MATCH]
    
    def getStateTypes(self):
        i = [self.GAP]*self.stateNo()
        for j in self.getMatchStates():
            i[j] = self.MATCH
        return i
        
    def getFns(self, action):
        fn = {}
        for i in self.GAP, self.MATCH:
            for j in self.GAP, self.MATCH:
                fn[(i, j)] = self.fromToFn(i, j, action)
        return fn
    
    def mapEmissions(self, fn):
        self.__des = [ fn(i) for i in self.__des ]

def fTransition(stateNo):  
    def fTransition(s, sI, s2, sI2, i, j, t, de, *args):
        s2[sI2 + j + stateNo] = Maths.logAdd(s2[sI2 + j + stateNo], s[sI + i + stateNo] + t + de)
    return fTransition

def bTransition(s, sI,  s2, sI2, i, j, t, de, *args):
    s[sI + i] = Maths.logAdd(s[sI + i], s2[sI2 + j] + t + de)
    
class Rescale:
    def __init__(self, startDiagonal, stateNo):
        self.__list = [0]*2
        self.__sD = startDiagonal-2
        self.__stateNo = stateNo
    
    def addNewScale(self, states, adP, ad):
        scale = Maths.NEG_INFINITY
        for i in xrange(adP.yS(ad), adP.yS(ad+1)):
            s = adP.e[i]
            scale = Maths.logAdd(scale, \
                                 logSum(states[s+self.__stateNo:s+2*self.__stateNo]))
        i = (adP.yS(ad+1) - adP.yS(ad))*self.__stateNo
        if i is 0:
            i = 1
        i = Maths.log(i)
        if scale is Maths.NEG_INFINITY:
            scale = i
        #print "________________________________________scale ", i, scale, int(scale - i)
        #scale = i
        self.__list.append(self.__list[-1] + int(scale - i))
    
    def rescale(self, x1, y1, x2, y2):
        return self.__list[x2 + y2 - self.__sD] - self.__list[x1 + y1 - self.__sD]
    
    def rescaleFn(self, fn):
        def fn2(*args):
            return fn(*args) - self.rescale(*args) 
        return fn2
    
class BTransitionAndTotalReCalculator:
    def __init__(self, totalFn, startDiagonal, endDiagonal, interval, stateNo):
        self.__totalFn = totalFn
        self.__stateNo = stateNo
        self.__list = []
        i = startDiagonal + interval
        while(i < endDiagonal):
            self.__list.append([i, Maths.NEG_INFINITY])
            i += interval
       
    def bTransition(self, s, sI,  s2, sI2, i, j, t, de, x1, y1, x2, y2):
        sD = x1 + y1
        k = s2[sI2 + j] + t + de
        for m in xrange(len(self.__list)-1, -1, -1):
            l = self.__list[m]
            if sD < l[0]:
                if x2 + y2 < l[0]:
                    self.__list.pop()
                    #print "new total ", l[1]
                    self.__totalFn(l[1])
                else:
                    l[1] = Maths.logAdd(l[1], s[sI + i + self.__stateNo] + k)
            else:
                break   
        s[sI + i] = Maths.logAdd(s[sI + i], k)
    
class PosteriorProbs:
    def __init__(self, pairs, stateNo):
        self.__pairs = pairs
        self.__current = 100
        self.__pa = []
        self.__x = 0
        self.__y = 0
        self.__total = Maths.NEG_INFINITY
        self.__stateNo = stateNo

    def diagBuilder(self, x1, y1):
        self.__x = x1
        self.__y = y1
        if self.__current != 100:
            self.__pa.append(self.__current)
        self.__current = Maths.NEG_INFINITY

    def diagStart(self, x1, y1):
        self.__current = 100
        self.__pa = []
        self.__x = x1
        self.__y = y1
        self.__current = Maths.NEG_INFINITY

    def diagEnd(self):
        if self.__current != 100:
            self.__pa.append(self.__current)
        j = 0.0
        self.__pa.reverse()
        for i in self.__pa:
            j += Maths.exp(i)
            self.__pairs[(self.__x, \
                          self.__y)] += j
            self.__x += 1
            self.__y += 1
           
    def total(self, total):
        self.__total = total
    
    def bTransition(self, s, sI,  s2, sI2, i, j, t, de, *args):
        self.__current = \
        Maths.logAdd(self.__current, 
                      s2[sI2 + j] + t + de + s[sI + self.__stateNo + i] - self.__total)
        
class SparseArray:
    def __init__(self, start, end, points):
        """
        inclusive coordinates
        """
        if end < start:
            raise IndexError("%s, %s" % (start, end))
        self.__yS = []
        self.y = []
        self.e = []
        points.sort()
        self.y = [i[1] for i in points]
        self.e = [i[2] for i in points]
        self.__fI = p = start
        for i in range(0, len(points)):
            j = points[i][0]
            if j < start:
                raise IndexError("%s %s %s" % (start, end, j))
            while(p <= j):
                self.__yS.append(i)
                p += 1
        if p > end + 1:
            raise IndexError("%s %s %s" % (start, end, j))
        while(p <= end+1):
            self.__yS.append(len(points))
            p += 1
            
    def firstXIndex(self):
        return self.__fI
    
    def xLength(self):
        return len(self.__yS)-1

    def yS(self, x):
        try:
            return self.__yS[x - self.__fI]
        except IndexError:
            raise IndexError("%s %s %s" % (x, self.firstXIndex(), self.xLength()))

#diagBuilder add gap
#diagReset
#gap to match - diagGap
#match to match - diagMatch

#match to gap - gap

#gap to gap - gapGap

#position calculator
def sparseAlign(startStates, points, gapPointsBL,
                gapPointsTR, endStates, stateNo,
                
                gap, gapGap,
                diagStart,
                diagBuilder,
                diagEnd,
                diagGap, diagMatch,

                gapR, gapGapR,
                diagStartR,
                diagBuilderR,
                diagEndR, 
                diagGapR, diagMatchR,
                
                totalReporter,
                
                rescale,
                diagTraceBackLimit):
    """
    only the match states must have positive start and finish values
    """
    points.sort()
    points = [(points[j][0], points[j][1], j*stateNo*2) \
                 for j in xrange(0, len(points))]
    
    startX, startY = points[0][0:2]
    endX, endY = points[len(points)-1][0:2]
    
    pH = {}
    for i in points:
        pH[(i[0]-1, i[1]-1)] = i[2]
    i = [len(points)*stateNo*2]
    def fn(p):
        l = []
        for j in p:
            if pH.has_key((j[0], j[1])):
                l.append((j[0], j[1], pH[(j[0], j[1])]))
            else:
                l.append((j[0], j[1], i[0]))
                pH[(j[0], j[1])] = i[0]
                i[0] += stateNo*2
        return l
    gapPointsBL = fn(gapPointsBL)
    gapPointsTR = fn(gapPointsTR)
 
    states = [Maths.NEG_INFINITY]*i[0]
    
    pointsH = {}
    for i in points:
        pointsH[(i[0], i[1])] = i[2]
    def flip(p):
        return [(i[1], i[0], i[2]) for i in p]
    def dc(p):
        return [((i[0] - i[1]), (i[0] + i[1]), i[2]) for i in p]
    xP = SparseArray(startX, endX, points)
    yP = SparseArray(startY, endY, flip(points))
    p = dc(points)
    dP = SparseArray(startX - endY, endX - startY, p)
    adP = SparseArray(startX + startY, endX + endY, flip(p))

    fn = lambda j : [ i for i in j if not pointsH.has_key((i[0]+1, i[1]+1)) ]

    yBLP = SparseArray(startY, endY, flip(gapPointsBL))
    adBLP = SparseArray(startX + startY, endX + endY,\
                       flip(dc(fn(gapPointsBL))))
    
    xTRP = SparseArray(startX, endX, gapPointsTR)
    adTRP = SparseArray(startX + startY, endX + endY,\
                       flip(dc(fn(gapPointsTR))))
    
    i = stateNo*2*(len(points)-1)
    states[stateNo:stateNo*2] = startStates
    states[i:i+stateNo] = endStates
    
    def fIterator():
        for i in xrange(adP.firstXIndex()+1, adP.firstXIndex() \
                     + adP.xLength()):
            rescale.addNewScale(states, adP, i-1)
            yield i
    forwardRecursion(xP, yP, dP, adP,
                     yBLP, adBLP, xTRP, adTRP,
                     states,
                     gap, gapGap,
                     diagStart,
                     diagBuilder,
                     diagEnd, 
                     diagGap, diagMatch, 
                     fIterator(),
                     diagTraceBackLimit)
    i += stateNo
    totalReporter(logSum([ states[i + j] + endStates[j] for j in range(0, stateNo)])) 
    forwardRecursion(xP, yP, dP, adP,
                     yBLP, adBLP, xTRP, adTRP,
                     states,
                     gapR, gapGapR,
                     diagStartR,
                     diagBuilderR,
                     diagEndR, 
                     diagGapR, diagMatchR, 
                     xrange(adP.firstXIndex() \
                     + adP.xLength()-1, adP.firstXIndex(), -1),
                     diagTraceBackLimit)

def doPoint(p, x, y, s, states, gapFn):
    #for i in xrange(p.yS(x+1)-1, p.yS(x)-1, -1):
    for i in xrange(p.yS(x), p.yS(x+1)):
        y2 = p.y[i]
        if y2 >= y:
            break
        s2 = p.e[i]
        gapFn(states, s2, states, s, x, y2, x, y)
        
def doPointR(p, x, y, s, states, gapFn):
    #for i in xrange(p.yS(x+1)-1, p.yS(x)-1, -1):
    for i in xrange(p.yS(x), p.yS(x+1)):
        y2 = p.y[i]
        if y2 >= y:
            break
        s2 = p.e[i]
        gapFn(states, s2, states, s, y2, x, y, x)

def logSum(s):
    f = s[0]
    for i in s[1:]:
        f = Maths.logAdd(f, i)
    return f

def forwardRecursion(xP, yP, dP, adP,
                     yBLP, adBLP, xTRP, adTRP,
                     states,
                     gap, gapGap,
                     diagStart, diagBuilder,
                     diagEnd,
                     diagGap, diagMatch,
                     iterator,
                     diagTraceBackLimit):
    for ad in iterator:
        for i in xrange(adBLP.yS(ad), adBLP.yS(ad+1)):
            d = adBLP.y[i]
            s = adBLP.e[i]
            x = (ad+d)/2
            y = ad-x
            doPoint(xP, x, y, s, states, gap)
        for i in xrange(adTRP.yS(ad), adTRP.yS(ad+1)):
            d = adTRP.y[i]
            s = adTRP.e[i]
            x = (ad+d)/2
            y = ad-x
            doPointR(yP, y, x, s, states, gap)
        
        if ad + 2 < adP.firstXIndex() \
                     + adP.xLength():
            for i in xrange(adP.yS(ad+2), adP.yS(ad+3)):
                d = adP.y[i]
                s = adP.e[i]
                x = (ad+2+d)/2
                y = ad+2-x
            
                doPoint(xP, x-1, y-1, s, states, gap)
                doPointR(yP, y-1, x-1, s, states, gap)
                doPoint(xTRP, x-1, y-1, s, states, gapGap)
                doPointR(yBLP, y-1, x-1, s, states, gapGap)
            
        for i in xrange(adP.yS(ad), adP.yS(ad+1)):
            d = adP.y[i]
            s = adP.e[i]
            x = (ad+d)/2
            y = ad-x
            diagStart(x, y)
            j = dP.yS(d) + dP.y[dP.yS(d):dP.yS(d+1)].index(ad)
            k = 1
            s2 = s
            lim = dP.yS(d)-1
            if j-1-lim > diagTraceBackLimit:
                lim = j-1-diagTraceBackLimit
            for j in xrange(j-1, lim, -1):
                ad2 = dP.y[j]
                if ad2 < ad - 2*k:
                    break
                s3 = dP.e[j]
                diagMatch(states, s3, states, s)
                diagGap(states, s2, states, s)
                diagBuilder(x-k, y-k)
                s2 = s3
                k += 1
            diagGap(states, s2, states, s)
            diagEnd()

def main():
    pass

def _test():
    import doctest      
    return doctest.testmod()

if __name__ == '__main__':
    _test()
    main()
