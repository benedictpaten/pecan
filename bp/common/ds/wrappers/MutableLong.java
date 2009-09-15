/*
 * MutableLong.java
 *
 * Created on 29 August 2003, 14:49
 */

package bp.common.ds.wrappers;

/**
 * 
 * @author ben
 */
public class MutableLong implements java.lang.Comparable {
    public long i = 0;

    public MutableLong() {
    }

    public MutableLong(final long i) {
        this.i = i;
    }

    @Override
	public final int hashCode() {
        //return (int)(i ^ (i >> 32));
        //this is a compromise code
        return (int) (this.i ^ (this.i >> 16));
        //return (int)i;
    }

    @Override
	public final boolean equals(final Object o) {
        return ((MutableLong) o).i == this.i;
    }

    public final int compareTo(final Object o) {
        final long j = ((MutableLong) o).i;
        return this.i == j ? 0 : this.i > j ? 1 : -1;
    }
}