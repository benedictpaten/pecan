/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

package bp.common.maths;

import java.util.Arrays;

public class Statistics {
    
    public static double[][] basicStats(final double[][] in) {
        final double[][] m = new double[in[0].length][];
        final double[] k = new double[in.length];
        for (int i = 0; i < in[0].length; i++) {
            for (int j = 0; j < in.length; j++) {
				k[j] = in[j][i];
			}
            m[i] = Statistics.basicStats(k);
        }
        return m;
    }

    public static double[] basicStats(final double[] in) {
        final double[] bs = new double[5];
        bs[0] = Statistics.mean(in);
        bs[1] = Statistics.median(in);
        bs[2] = Statistics.stdev(in);
        bs[3] = Statistics.max(in);
        bs[4] = Statistics.min(in);
        return bs;
    }

    public static double mean(final double[] in) {
        double j = 0.0;
        for (final double element : in) {
			j += element;
		}
        return j / in.length;
    }

    public static double max(final double[] in) {
        double j = Integer.MIN_VALUE;
        for (final double element : in) {
			if (element > j) {
				j = element;
			}
		}
        return j;
    }

    public static double min(final double[] in) {
        double j = Integer.MAX_VALUE;
        for (final double element : in) {
			if (element < j) {
				j = element;
			}
		}
        return j;
    }

    public static double stdev(final double[] in) {
        double j = 0.0;
        final double k = Statistics.mean(in);
        for (final double element : in) {
			j += Math.pow(element - k, 2);
		}
        j /= (in.length - 1);
        return Math.sqrt(j);
    }

    //returns median, in case of even number of array elements,
    // returns value above median point
    public static double median(final double[] in) {
        final double[] sin = in.clone();
        Arrays.sort(sin);
        return sin[sin.length / 2];
    }

    public static class HyperGeometricDistribution {
        double[] logF;

        int N;

        public HyperGeometricDistribution(final int N) {
            this.N = N;
            this.logF = new double[N + 1];
            for (int i = 0; i <= N; i++) {
				this.logF[i] = Statistics.lnGamma(i + 1);
			}
        }

        public final double pValue(final int k, final int n, final int x) {
            return Math.exp(this.logBinomial(k, x)
                    + this.logBinomial(this.N - k, n - x) - this.logBinomial(this.N, n));
        }

        public final double logpValue(final int k, final int n, final int x) {
            return this.logBinomial(k, x) + this.logBinomial(this.N - k, n - x)
                    - this.logBinomial(this.N, n);
        }

        public final double variance(final int k, final int n) {
            return Math.exp(Math.log(this.N - k) + Math.log(k)
                    + Math.log(n) + Math.log(this.N - n) - 2 * Math.log(this.N)
                    - Math.log(this.N - 1));
            //return ((double)(N-k)*k*n*(N-n)) / (N*N*(N-1));
        }

        public final double mean(final int k, final int n) {
            return ((double) n * k) / this.N;
        }

        private final double logBinomial(final int n, final int k) {
            return this.logF[n] - this.logF[k] - this.logF[n - k];
        }
    }

    public static final double hyperGeometricDistribution(final int N,
            final int k, final int n, final int x) { //returns a p value for the
                                   // selection of x correct samples
                                   // from a sample of n values from a
                                   // bag of size N containing k
                                   // 'success' values
        return Math.exp(Statistics.logBinomial(k, x) + Statistics.logBinomial(N - k, n - x)
                - Statistics.logBinomial(N, n));
    }

    public static final double logBinomial(final int n, final int k) {
        return Statistics.lnGamma(n + 1) - Statistics.lnGamma(k + 1) - Statistics.lnGamma(n - k + 1);
    }

    public static final double binomial(final int n, final int k) {
        return Math.exp(Statistics.lnGamma(n + 1) - Statistics.lnGamma(k + 1)
                - Statistics.lnGamma(n - k + 1));
    }

    public static class NToKBinsDistribution {
        double[] logF;

        public int max;

        public NToKBinsDistribution(final int max) {
            this.max = max;
            this.logF = new double[max + 1];
            for (int i = 0; i <= max; i++) {
				this.logF[i] = Statistics.lnGamma(i + 1);
			}
        }

        public final double logNToKBins(final int[] j, final int N, final int R, final int S) { //j is
                                                                        // partition
                                                                        // description,
                                                                        // N is
                                                                        // ball
                                                                        // no,
                                                                        // R is
                                                                        // bin
                                                                        // no
                                                                        // and
                                                                        // S is
                                                                        // no
                                                                        // of
                                                                        // bins
                                                                        // filled.
            return this.logM3(j, N) + this.logF[R] - this.logF[R - S] - N
                    * Math.log(R);
        }

        private final double logM3(final int[] na, final int m) {
            double n = this.logF[m];
            for (int i = 0; i < na.length; i++) {
				n -= this.logF[i + 1] * na[i] + this.logF[na[i]];
			}
            return n;
        }
    }

    public static final double logNToKBins(final int[] j, final int N, final int R,
            final int S) {
        return Statistics.logM3(j, N) + Statistics.lnGamma(R + 1) - Statistics.lnGamma(R - S + 1) - N
                * Math.log(R);
    }

    public static final double nToKBins(final int[] j, final int N, final int R, final int S) {
        return Math.exp(Statistics.logM3(j, N) + Statistics.lnGamma(R + 1)
                - Statistics.lnGamma(R - S + 1) - N * Math.log(R));
    }

    public static final double logM3(final int[] na, final int m) {
        double n = Statistics.lnGamma(m + 1);
        for (int i = 0; i < na.length; i++) {
			n -= Statistics.lnGamma(i + 2) * na[i] + Statistics.lnGamma(na[i] + 1);
		}
        return n;
    }

    public static final double logMultinomial(final int[] na, final int m) {
        double n = Statistics.lnGamma(m + 1);
        for (final int element : na) {
			n -= Statistics.lnGamma(element + 1);
		}
        return n;
    }

    /**
     * *
     * 
     * @(#)lngamma.js * * Copyright (c) 2000 by Sundar Dorai-Raj *
     * @author Sundar Dorai-Raj * Email: sdoraira@vt.edu * This
     *         program is free software; you can redistribute it
     *         and/or * modify it under the terms of the GNU General
     *         Public License * as published by the Free Software
     *         Foundation; either version 2 * of the License, or (at
     *         your option) any later version, * provided that any use
     *         properly credits the author. * This program is
     *         distributed in the hope that it will be useful, * but
     *         WITHOUT ANY WARRANTY; without even the implied warranty
     *         of * MERCHANTABILITY or FITNESS FOR A PARTICULAR
     *         PURPOSE. See the * GNU General Public License for more
     *         details at http://www.gnu.org * *
     */
    static double lnGamma(final double c) {
        final double[] cof = new double[6];
        cof[0] = 76.18009172947146;
        cof[1] = -86.50532032941677;
        cof[2] = 24.01409824083091;
        cof[3] = -1.231739572450155;
        cof[4] = 0.1208650973866179e-2;
        cof[5] = -0.5395239384953e-5;
        final double xx = c;
        double yy = c;
        final double tmp = xx + 5.5 - (xx + 0.5) * Math.log(xx + 5.5);
        double ser = 1.000000000190015;
        for (int j = 0; j <= 5; j++) {
			ser += (cof[j] / ++yy);
		}
        return (Math.log(2.5066282746310005 * ser / xx) - tmp);
    }
}