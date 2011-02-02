/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

package bp.pecan;

public class PairValue implements Comparable<PairValue> {
	public int seq1;
	public int seq2; 
	public int pos1;
	public int pos2;
	public float weight;
	
	public PairValue(int seq1P, int seq2P, int pos1P, int pos2P, float weightP) {
		this.seq1 = seq1P;
		this.seq2 = seq2P;
		this.pos1 = pos1P;
		this.pos2 = pos2P;
		this.weight = weightP;
	}
	
	public int compareTo(PairValue o) {
		if(this.weight < o.weight) {
			return -1;
		}
		if(this.weight > o.weight) {
			return 1;
		}
		return 0;
	}
}
