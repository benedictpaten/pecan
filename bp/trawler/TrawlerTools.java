/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

/*
 * Created on Dec 14, 2005
 */
package bp.trawler;

import bp.common.ds.SuffixTree;

public final class TrawlerTools {

    static final boolean matches(final byte i, final byte matchChar) {
        return (i | matchChar) == matchChar;
    }

    static final int scan(final int[] iA, final int start, final int end,
            final SuffixTree sT, final int depth, final byte matchChar) {
        int i = end;
        for (int j = start; j < end; j++) {
            final int node = iA[j];
            if (sT.isLeaf(node)) {
                if (TrawlerTools.matches(sT.string[(node / 2) + depth], matchChar)) {
                    iA[i++] = node;
                }
            } else {
                if (depth < sT.depth(node)) {
                    if (TrawlerTools.matches(sT.string[sT.headPosition(node)
                            + depth], matchChar)) {
                        iA[i++] = node;
                    }
                } else {
                    int k = sT.firstChild(node);
                    do {
                        if (sT.isLeaf(k)) {
                            if (TrawlerTools.matches(sT.string[(k / 2) + depth],
                                    matchChar)) {
                                iA[i++] = k;
                            }
                        } else {
                            if (TrawlerTools.matches(sT.string[sT.headPosition(k)
                                    + depth], matchChar)) {
                                iA[i++] = k;
                            }
                        }
                        k = sT.siblingOrSuffixLink(k);
                    } while (sT.isNode(k));
                }
            }
        }
        return i;
    }

    interface PassOut {
        void pro(int i, int j, int depth, byte[] string);
    }

    static final void scan(final int start, final int end, final int start2, final int end2,
            final int depth, final int mismatches, final int[] iA, final int[] iA2,
            final byte[] alphabet, final byte[] string, final int occurence,
            final SuffixTree sT, final SuffixTree sT2, final int[] counts,
            final int[] counts2, final PassOut passOut, final int[] alphabetMisMatches,
            final int wildCard, final int maxMismatches, final int maximumMotifSize) {
        for (int i = 0; i < alphabet.length; i++) {
            TrawlerTools.scan(start, end, start2, end2, depth, mismatches, iA,
                    iA2, alphabet, string, occurence, sT, sT2,
                    counts, counts2, passOut, alphabetMisMatches,
                    maxMismatches, wildCard, maximumMotifSize, i);
        }
    }

    static final void scan(final int start, final int end, final int start2, final int end2,
            final int depth, final int mismatches, final int[] iA, final int[] iA2,
            final byte[] alphabet, final byte[] string, final int occurence,
            final SuffixTree sT, final SuffixTree sT2, final int[] counts,
            final int[] counts2, final PassOut passOut, final int[] alphabetMisMatches,
            final int maxMismatches, final int wildCard, final int maximumMotifSize,
            final int i) {
        final int j = mismatches + alphabetMisMatches[i];
        if (j > maxMismatches) {
			return;
		}
        final byte b = alphabet[i];
        final int k = TrawlerTools.scan(iA, start, end, sT, depth, b);
        final int l = TrawlerTools.getCount(end, k, iA, counts, sT);
        if (l >= occurence) {
            final int m = TrawlerTools.scan(iA2, start2, end2, sT2, depth, b);
            final int n = TrawlerTools.getCount(end2, m, iA2, counts2, sT2);
            string[depth] = b;
            if (b != wildCard) {
				passOut.pro(l, n, depth + 1, string);
			}
            if (depth + 1 < maximumMotifSize) {
				TrawlerTools.scan(end, k, end2, m, depth + 1, j, iA, iA2,
                        alphabet, string, occurence, sT, sT2, counts,
                        counts2, passOut, alphabetMisMatches,
                        wildCard, maxMismatches, maximumMotifSize);
			}
        }
    }

    static final int getCount(final int start, final int end, final int[] iA,
            final int[] counts, final SuffixTree sT) {
        int i = 0;
        for (int j = start; j < end; j++) {
			i += TrawlerTools.getCount(iA[j], counts, sT);
		}
        return i;
    }

    static final int getCount(final int node, final int[] iA, final SuffixTree sT) {
        return sT.isLeaf(node) ? 1 : iA[sT.headPosition(node)];
    }

    static final int parseCounts(final int node, final int[] iA, final SuffixTree sT) {
        if (sT.isLeaf(node)) {
			return 1;
		}
        int i = 0;
        int j = sT.firstChild(node);
        while (sT.isNode(j)) {
            i += TrawlerTools.parseCounts(j, iA, sT);
            j = sT.siblingOrSuffixLink(j);
        }
        iA[sT.headPosition(node)] = i;
        return i;
    }
}
