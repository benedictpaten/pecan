/*
 * SuffixTree.java
 *
 * Created on 29 August 2003, 13:56
 */

package bp.common.ds;

import bp.common.io.Debug;

/**
 * This implementation is clean roomed from a description given in Stefan
 * Kurtz's 97 paper on memory effcient suffix trees
 */
public final class SuffixTree {
    /** Creates a new instance of SuffixTree2 */
    public SuffixTree(final byte[] string, final double byteMultiple,
            final boolean FITEXACT, final byte terminalChar) {
        this.TERMINALCHAR = terminalChar;
        this.string = string;
        this.leaves = new int[string.length];
        this.branches = new int[(int) (string.length * (byteMultiple - 5) / 4) + 100]; // changed
        // 31/8/04
        // + 100 deals with the zero length st!
        // Debug.pl(" length of branches is " + branches.length);
        this.looseMcCreightsAlgorithm();
        if (FITEXACT) {
            this.leaves = new int[string.length];
            this.branches = new int[this.memoryUsage + 64];
            this.looseMcCreightsAlgorithm();
        }
    }
    
    public final byte TERMINALCHAR;

    /* core code start */
    public static final int NOTANODE = 0x20000000; /* 2^29 */

    public static final int DEPTHISLOW = 0x80000000; /* 2^31 */

    public static final int DISTANCE = 0xf8000000; /* 2^32 - 2^27 */

    public static final int IDISTANCE = ~SuffixTree.DISTANCE;

    public static final int REFERENCE = 0x1fffffff; /* 2^29 - 1 */

    public static final int SIBLINGORSUFFIXLINK = 0x3fffffff; /* 2^30 - 1 */

    public static final int ISIBLINGORSUFFIXLINK = ~SuffixTree.SIBLINGORSUFFIXLINK;

    public static final int FIRSTCHILD1 = 0x7ffffff; /* 2^27 - 1 */

    public static final int IFIRSTCHILD1 = ~SuffixTree.FIRSTCHILD1;

    public static final int FIRSTCHILD2 = 0xc0000000; /*
                                                         * 2^32 - 2^30
                                                         */
    public static final int IFIRSTCHILD2 = ~SuffixTree.FIRSTCHILD2;

    public static final int DEPTHLOW = 0x3ff; /* 2^10 -1 */

    public static final int DEPTHHIGH = 0x7ffffff; /* 2^27 - 1 */

    public static final int SUFFIXLINKLOW1 = 0x7ffffc00; /* 2^31 - 2^10 */

    public static final int SUFFIXLINKLOW2 = 0xf8000000; /* 2^32 - 2^27 */

    public static final int SUFFIXLINKLOW3 = 0xc0000000; /* 2^32 - 2^30 */

    public static final int HEADPOSITION = 0x7ffffff; /* 2^27 - 1 */

    public byte[] string;

    public int[] leaves, branches;

    public int memoryUsage, internalNodeCount = 1;

    private void looseMcCreightsAlgorithm() {
        int q = 2, p = 1, sP = 1, d = -1;
        NE ne = new NE(0, 0);
        this.constructLeafNode(this.makeLeaf(0));
        this.constructSmallNode(0, 0, 0, 0);
        this.setFirstChild(0, this.makeLeaf(0));
        while (sP < this.string.length) {
            if (ne.e > 0) { // split edge and add small node + leaf
                if (p > 31) {
                    this.constructLargeNode(q - 2, d + 1 + ne.e, sP - 1,
                            (q += 2), p); // sP represents the head
                    // position
                    p = 0;
                }
                this.constructSmallNode(q, p, d + ne.e, sP);
                this.internalNodeCount++;
                final int rc = this.getChild(ne.n, this.string[sP + d]);
                this.setNthChild(ne.n, q);
                this.setFirstChild(q, rc);
                // setParent(rc, q);
                this.constructLeafNode(this.makeLeaf(sP));
                this.setNthChild(q, this.makeLeaf(sP));
                q += 2;
                p++;
            } else { // finalise any preceding chain of nodes and then
                // trace prefix
                if (p > 0) {
                    this.constructLargeNode(q - 2, d + 1, sP - 1, ne.n, p); // sP
                    // represents
                    // the
                    // head
                    // position
                    p = 0;
                    q += 2;
                }
                ne = this.scanPrefix(ne, sP, this.string.length, this.string); // string
                // length
                // is
                // recessive
                // to
                // the
                // presence
                // of
                // the
                // TERMINALCHAR
                d = this.depth(ne.n);
                if (ne.e > 0) {
					// or needs to be added
                    continue; // the leaf and node will be added at
				}
                // the top of the next loop
                this.constructLeafNode(this.makeLeaf(sP));
                this.setNthChild(ne.n, this.makeLeaf(sP)); // leaf is added
            }
            { // traverse suffix link (if present) and rescan any path
                // down suffix, update loop variables
                /*
                 * !!!! aa int one = depth(ne.n) + ne.QueueEdgeList, two = ne.n; //
                 * debug end
                 */
                sP++;
                ne.n = this.suffixLink(ne.n); // if root - this call will
                // maintain start from the
                // root
                final int i = ne.e;
                ne.e = 0;
                if (i > 0) {
					ne = this.rescan(ne, sP, sP + d - 1 + i, this.string);
				}
                d = this.depth(ne.n);
                /*
                 * !!!! aa if(two != 0 && one != depth(ne.n) + ne.QueueEdgeList +
                 * 1) throw new SuffixTreeException(" wrong " + ne.n + " " +
                 * ne.QueueEdgeList); // debug end
                 */
            }
        }
        this.memoryUsage = q;
    }

    public static class NE {
        public int n = 0, e = 0; // QueueEdgeList refers to offset from n

        public NE(final int n, final int e) {
            this.n = n;
            this.e = e;
        }
    }

    private final int compare(final byte a, final byte b) {
        return a == b ? 0 : a == this.TERMINALCHAR ? 1 : b == this.TERMINALCHAR ? -1 : a > b ? 1
                : -1;
    }

    private void constructLeafNode(final int n) {
        /*
         * !!!! aa if(!isLeaf(n) | !isNode(n)) throw new SuffixTreeException(" n
         * is not a leaf/node " + n); // debug end
         */
        this.leaves[n / 2] = SuffixTree.NOTANODE;
    }

    private void constructSmallNode(final int q, final int p, final int depth,
            final int headPosition) { // node 'constructor'
        final int i = (depth + p) < 32 ? depth + p : 31;
        this.branches[q] = (i - p) << 27;
        this.branches[q + 1] = SuffixTree.NOTANODE;
        if (p == 0) {
            this.branches[q + 2 * i + 2] = (depth < 32) ? 0 : depth - 31; // depth
            // can
            // be
            // low
            // or
            // high
            // as
            // no
            // suffix
            // link
            // is
            // set
            // present
            this.branches[q + 2 * i + 3] = headPosition + i;
        }
    }

    private void constructLargeNode(final int n, final int depth,
            final int headPosition, int suffixLink, int p) {
        /* !!!! aa int y = suffixLink; /* */
        if (depth > SuffixTree.DEPTHLOW) {
            this.branches[n + 2] = depth;
            this.branches[n + 3] = headPosition;
            int i = this.firstChild(n), j = this.siblingOrSuffixLink(i);
            while (this.isNode(j)) {
                i = j;
                j = this.siblingOrSuffixLink(j);
            }
            this.setSiblingOrSuffixLink(i, (suffixLink | SuffixTree.NOTANODE));
        } else {
            suffixLink /= 2;
            this.branches[n + 2] = depth
                    | ((suffixLink << 10) & SuffixTree.SUFFIXLINKLOW1)
                    | SuffixTree.DEPTHISLOW;
            this.branches[n + 3] = headPosition
                    | ((suffixLink << 6) & SuffixTree.SUFFIXLINKLOW2);
            this.leaves[headPosition] |= ((suffixLink << 4) & SuffixTree.SUFFIXLINKLOW3);
        }
        while (p-- > 0) {
			this.setDistance(n - 2 * p, p);
        /*
         * !!!! aa if(n != 0 && n+4 != y && depth(y)+1 != depth(n)) throw new
         * SuffixTreeException(" suffix link is not one shallower than node n " +
         * n + " depth(n) " + depth(n) + " sL " + y + " depth(sL) " + depth(y) + "
         * firstchild " + firstChild(n) + " firstChild " + firstChild(y) + "
         * distance " + distance(n) + " distance " + distance(y) + "
         * headPosition " + headPosition(n) + " headPosition " + headPosition(y) + "
         * "); // debug end
         */
		}
    }

    public final int depth(final int n) {
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n)) throw new SuffixTreeException(" n : " +
         * n + " is not a node " + (!isNode(n)) + " or is a leaf : " +
         * isLeaf(n)); // debug end
         */
        // int d = distance(n), l = n + d*2 + 2;
        // return ((branches[l] & DEPTHISLOW) != 0 ? branches[l] &
        // DEPTHLOW : branches[l] & DEPTHHIGH) + d;
        final int d = this.distance(n), m = this.branches[n + d * 2 + 2];
        return ((m & SuffixTree.DEPTHISLOW) != 0 ? m & SuffixTree.DEPTHLOW : m & SuffixTree.DEPTHHIGH)
                + d;
        // return (branches[n + d*2 + 2] & DEPTHLOW) + d;
    }

    private final int distance(final int n) {
        return this.branches[n] >>> 27;
    }

    public final int firstChild(final int n) {
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n)) throw new SuffixTreeException(" n : " +
         * n + " is not a node " + isNode(n) + " or is a leaf : " + isLeaf(n)); //
         * debug end
         */
        return (this.branches[n] & SuffixTree.FIRSTCHILD1)
                | ((this.branches[n + 1] & SuffixTree.FIRSTCHILD2) >>> 3);
    }

    public final byte getByte(final int n, final int d) {
        return this.string[(this.isLeaf(n) ? n / 2 : this.headPosition(n)) + d];
    }

    public final int getChild(final int n, final byte c) {
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n)) throw new SuffixTreeException(" n : " +
         * n + " is not a node " + isNode(n) + " or is a leaf : " + isLeaf(n)); //
         * debug end
         */
        int nc = this.firstChild(n);
		final int depth = this.depth(n);
        while (this.isNode(nc) && (this.compare(this.getByte(nc, depth), c) < 0)) {
			nc = this.siblingOrSuffixLink(nc);
		}
        return this.isNode(nc) && (this.compare(c, this.getByte(nc, depth)) == 0) ? nc
                : SuffixTree.NOTANODE;
    }

    public final int getChild(final int n, final int d, final byte c) {
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n)) throw new SuffixTreeException(" n : " +
         * n + " is not a node " + isNode(n) + " or is a leaf : " + isLeaf(n)); //
         * debug end
         */
        int nc = this.firstChild(n);
        while (this.isNode(nc)) {
            final int b = this.getByte(nc, d);
            if (b >= c) {
				return b == c ? nc : SuffixTree.NOTANODE;
			}
            nc = this.siblingOrSuffixLink(nc);
        }
        return SuffixTree.NOTANODE;
    }

    public int getSuffixLink(int n) {
        while (this.isNode(n)) {
			n = this.siblingOrSuffixLink(n);
		}
        return n & SuffixTree.REFERENCE;
    }

    public final int headPosition(final int n) {
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n)) throw new SuffixTreeException(" n : " +
         * n + " is not a node " + isNode(n) + " or is a leaf : " + isLeaf(n)); //
         * debug end
         */
        final int d = this.distance(n);
        return (this.branches[n + d * 2 + 3] & SuffixTree.HEADPOSITION) - d;
    }

    public final boolean isLeaf(final int n) {
        return n % 2 == 1;
    }

    public final boolean isNode(final int n) {
        return n < SuffixTree.NOTANODE;
    }

    public final int makeLeaf(final int n) {
        return (n << 1) + 1;
    }

    public NE rescan(final NE ne, final int i, final int j, final byte[] string) {
        /*
         * !!!! aa if(!isNode(ne.n) || isLeaf(ne.n)) throw new
         * SuffixTreeException(" rescan called with leaf or NOTANODE " + ne.n); //
         * debug end
         */
        int nc, d = this.depth(ne.n);
        while ((d < j - i)
                && this.isNode((nc = this.getChild(ne.n, string[i + d])))) {
            if (!this.isLeaf(nc) && (this.depth(nc) <= j - i)) {
                ne.n = nc;
                d = this.depth(ne.n); // added
                continue; // added
                // return rescan(ne, i, j, string);
            }
            ne.e = (j - i) - d;
            return ne;
        }
        // else { removed
        ne.e = 0;
        return ne;
    }

    public NE scanPrefix(final NE ne, final int i, final int j, final byte[] string) {
        /*
         * !!!! aa if(!isNode(ne.n) || isLeaf(ne.n)) throw new
         * SuffixTreeException(" scanPrefix called with leaf or NOTANODE " +
         * ne.n); // debug end
         */
        int nc, d = this.depth(ne.n), d2;
        while ((d < j - i)
                && this.isNode((nc = this.getChild(ne.n, string[i + d])))) {
            d2 = this.isLeaf(nc) ? string.length : this.depth(nc);
            for (int k = 0; (k + d < d2) && (k + d < j - i); k++) {
                if ((string[i + d + k] == this.TERMINALCHAR)
                        || (string[i + d + k] != this.getByte(nc, k + d))) {
                    ne.e = k;
                    return ne;
                }
                ne.e = k + 1;
            }
            if (!this.isLeaf(nc) && (d2 <= j - i)) {
                ne.n = nc;
                d = this.depth(ne.n);
                continue;
                // return scanPrefix(ne, i, j, string);
            }
            return ne;
        }
        // else {
        ne.e = 0;
        return ne;
        // }
    }

    private final void setDistance(final int n, final int d) { // d = 0 - 31
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n) | d > 31) throw new
         * SuffixTreeException(" n : " + n + " is not a node " + !isNode(n) + "
         * or is a leaf " + isLeaf(n) + " distance is > 31 " + (d > 31) + " " +
         * d); // debug end
         */
        this.branches[n] = (this.branches[n] & SuffixTree.IDISTANCE) | (d << 27);
    }

    private final void setFirstChild(final int n, final int nc) {
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n) | !isNode(nc)) throw new
         * SuffixTreeException(" n : " + n + " is not a node " + !isNode(n) + "
         * or is a leaf " + isLeaf(n) + " nc : " + nc + " is not a node " +
         * !isNode(nc)); // debug end
         */
        this.branches[n] = (this.branches[n] & SuffixTree.IFIRSTCHILD1)
                | (nc & SuffixTree.FIRSTCHILD1);
        this.branches[n + 1] = (this.branches[n + 1] & SuffixTree.IFIRSTCHILD2)
                | ((nc << 3) & SuffixTree.FIRSTCHILD2);
    }

    private void setNthChild(final int n, final int nc) { // depth value is
        // included because it
        // may not be set in
        // created node
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n) | !isNode(nc)) throw new
         * SuffixTreeException(" n : " + n + " is not a node " + !isNode(n) + "
         * or is a leaf " + isLeaf(n) + " nc : " + nc + " is not a node " +
         * !isNode(nc)); // debug end
         */
        final int fc = this.firstChild(n), depth = this.depth(n);
        this.setSibling(fc, nc, depth);
        if (this.compare(this.getByte(nc, depth), this.getByte(fc, depth)) <= 0) {
			this.setFirstChild(n, nc);
		}
    }

    private void setSibling(int n, final int rn, final int depth) {
        /*
         * !!!! aa if(!isNode(n) | !isNode(rn)) throw new SuffixTreeException("
         * n : " + n + " is not a node " + !isNode(n) + " rn : " + rn + " is not
         * a node " + !isNode(rn)); // debug end
         */
        int m = SuffixTree.NOTANODE;
        final byte rnC = this.getByte(rn, depth);
        while (this.isNode(n) && (this.compare(this.getByte(n, depth), rnC) < 0)) {
            m = n;
            n = this.siblingOrSuffixLink(n);
        }
        if (this.isNode(m)) {
			this.setSiblingOrSuffixLink(m, rn);
		}
        if (this.isNode(n) && (this.compare(rnC, this.getByte(n, depth)) == 0)
                && (rnC != this.TERMINALCHAR)) {
            final int i = this.siblingOrSuffixLink(n);
            this.setSiblingOrSuffixLink(n, SuffixTree.NOTANODE);
            n = i;
        }
        this.setSiblingOrSuffixLink(rn, n);
    }

    private final void setSiblingOrSuffixLink(final int a, final int b) {
        /*
         * !!!! aa if(!isNode(a)) throw new SuffixTreeException(" a : " + a + "
         * is not a node " + !isNode(a) + " b : " + b + " is not a node " +
         * !isNode(b)); // debug end
         */
        if (this.isLeaf(a)) {
            this.leaves[a / 2] = (this.leaves[a / 2] & SuffixTree.ISIBLINGORSUFFIXLINK)
                    | b;
        } else {
            this.branches[a + 1] = (this.branches[a + 1] & SuffixTree.ISIBLINGORSUFFIXLINK)
                    | b;
        }
    }

    public final int siblingOrSuffixLink(int n) {
        /*
         * !!!! aa if(!isNode(n)) throw new SuffixTreeException(" n : " + n + "
         * is not a node "); // debug end
         */
        n = this.isLeaf(n) ? this.leaves[n / 2] : this.branches[n + 1];
        return n & SuffixTree.SIBLINGORSUFFIXLINK;
    }

    public int suffixLink(final int n) {
        /*
         * !!!! aa if(!isNode(n) | isLeaf(n)) throw new SuffixTreeException(" n : " +
         * n + " is not a node " + isNode(n) + " or is a leaf : " + isLeaf(n)); //
         * debug end
         */
        if (this.distance(n) != 0) {
            /*
             * !!!! aa if(depth(n+2)+1 != depth(n)) throw new
             * SuffixTreeException(" suffix link is not one shallower than node
             * n " + n + " depth(n) " + depth(n) + " sL " + (n+2) + " depth(sL) " +
             * depth((n+2))); // debug end
             */
            return n + 2;
        }
        if ((this.branches[n + 2] & SuffixTree.DEPTHISLOW) == 0) {
            int i = this.firstChild(n);
            while (this.isNode(i)) {
				i = this.siblingOrSuffixLink(i);
			}
            /*
             * !!!! aa int y = i & REFERENCE; if(n != 0 && depth(y)+1 !=
             * depth(n)) throw new SuffixTreeException(" suffix link is not one
             * shallower than node n " + n + " depth(n) " + depth(n) + " sL " +
             * y + " depth(sL) " + depth(y) + " firstchild " + firstChild(n) + "
             * firstChild " + firstChild(y) + " distance " + distance(n) + "
             * distance " + distance(y) + " headPosition " + headPosition(n) + "
             * headPosition " + headPosition(y) + " "); // debug end
             */
            return i & SuffixTree.REFERENCE;
        }
        /*
         * !!!! aa int y = 2*(((branches[n+2] & SUFFIXLINKLOW1) >>> 10) |
         * ((branches[n+3] & SUFFIXLINKLOW2) >>> 6) | ((leaves[headPosition(n)] &
         * SUFFIXLINKLOW3) >>> 4)); if(n != 0 && depth(y)+1 != depth(n)) throw
         * new SuffixTreeException(" suffix link is not one shallower than node
         * n " + n + " depth(n) " + depth(n) + " sL " + y + " depth(sL) " +
         * depth(y) + " firstchild " + firstChild(n) + " firstChild sL" +
         * firstChild(y) + " distance " + distance(n) + " distance sL " +
         * distance(y) + " headPosition " + headPosition(n) + " headPosition sL " +
         * headPosition(y) + " "); // debug end
         */
        return 2 * (((this.branches[n + 2] & SuffixTree.SUFFIXLINKLOW1) >>> 10)
                | ((this.branches[n + 3] & SuffixTree.SUFFIXLINKLOW2) >>> 6) | ((this.leaves[this.headPosition(n)] & SuffixTree.SUFFIXLINKLOW3) >>> 4));
    }

    public String nodeString(final int n) {
        String s;
        if (this.isLeaf(n)) {
            s = "Leaf : " + (n / 2);
        } else {
            s = "Node : " + n + " depth : " + this.depth(n)
                    + ", headPosition : " + this.headPosition(n)
                    + ", suffixLink : " + this.suffixLink(n)
                    + " , distance : " + this.distance(n)
                    + ", children : ";
            try {
                int nc = this.firstChild(n);
                while (this.isNode(nc)) {
                    s += ((char) this.getByte(nc, this.depth(n))) + "-p-" + nc
                            + " , ";
                    nc = this.siblingOrSuffixLink(nc);
                }
            } catch (final Exception e) {
                Debug.pl(" caught exception in children " + e);
            }
        }
        return s;
    }

    public String pathString(final int n, final int d) {
        final StringBuffer sB = new StringBuffer();
        int i = this.isLeaf(n) ? n / 2 : this.headPosition(n);
		final int bound = i + d;
        while ((i < bound) && (this.string[i] != this.TERMINALCHAR)) {
            sB.append(this.string[i++]);
        }
        return sB.toString();
    }

    public String pathString(final int n) {
        final StringBuffer sB = new StringBuffer();
        int i = this.isLeaf(n) ? n / 2 : this.headPosition(n);
        final int bound = this.isLeaf(n) ? this.string.length : this.headPosition(n)
                + this.depth(n);
        while ((i < bound) && (this.string[i] != this.TERMINALCHAR)) {
			sB.append((char) this.string[i++]);
		}
        return sB.toString();
    }

    @Override
	public String toString() {
        return this.toString(0, 0);
    }

    public String toString(final int n, int j) {
        String s = "";
        for (int i = j; i > 0; i--) {
			s += "-";
		}
        final int d = this.depth(n), hP = this.headPosition(n);
        try {
            for (int i = j + hP; i < hP + d; i++) {
				s += (char) this.string[i];
			}
        } catch (final RuntimeException e) {
            Debug.pl(" error , j : " + j + " , hp : " + hP
                    + " , d : " + d + " n : " + n);
            throw e;
        }
        j = s.length();
        // s += nodeString(n);
        int nc = this.firstChild(n);
        while (this.isNode(nc)) { // 0x10000000 = 2^28
            if (this.isLeaf(nc)) {
                s += "\n";
                for (int i = j; i > 0; i--) {
					s += "-";
				}
                for (int i = j + (nc / 2); i < this.string.length; i++) {
                    s += (char) this.string[i];
                    if (this.string[i] == this.TERMINALCHAR) {
						break;
					}
                }
            } else {
                s += "\n" + this.toString(nc, j);
            }
            nc = this.siblingOrSuffixLink(nc);
        }
        return s;
    }

    /* test code end */
    /* special functions - cheeky functions */

    public void dropNodeEnds(final int p, final int d) { // snip off those
        // annoying split TERMINALCHAR
        // ends
        int d2;
        if (!this.isLeaf(p) && ((d2 = this.depth(p)) < d)) {
            int n = this.firstChild(p), m = this.siblingOrSuffixLink(n);
            this.dropNodeEnds(n, d);
            while (this.isNode(m)) {
                if (this.getByte(m, d2) == this.TERMINALCHAR) {
                    this.setSiblingOrSuffixLink(n, (p | SuffixTree.NOTANODE));
                    break;
                }
                this.dropNodeEnds(m, d);
                n = m;
                m = this.siblingOrSuffixLink(m);
            }
        }
    }
}