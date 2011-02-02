#!/usr/bin/env python

#Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
#
#Released under the MIT license, see LICENSE.txt
import unittest
import random as r
import Maths
import math

class TestCase(unittest.TestCase):
    
    def setUp(self):
        unittest.TestCase.setUp(self)
    
    def tearDown(self):
        unittest.TestCase.tearDown(self)
        
    def testLogAdd(self):
        for trial in xrange(0, 1000):
            d = r.random() * 100
            d2 = d - 20 * r.random()
            d3 = d
            d4 = d2
            if r.random() > 0.5:
                d3 = d2
                d4 = d
            self.assertAlmostEquals(Maths.logAdd(d3, d4), d\
                    + math.log(1 + math.exp(d2 - d)), 0.0001)
    
    def testLogAddQuality(self):
        for trial in xrange(0, 1000):
            d = r.random() * 100
            d2 = d - 20 * r.random()
            d3 = d
            d4 = d2
            if r.random() > 0.5:
                d3 = d2
                d4 = d
            self.assertAlmostEquals(Maths.logAddQuality(d3, d4), d \
                    + math.log(1 + math.exp(d2 - d)), 0)
        
if __name__ == '__main__':
    unittest.main()