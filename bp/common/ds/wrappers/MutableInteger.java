/*
 * MutableInteger.java
 *
 * Created on 29 August 2003, 14:47
 */

package bp.common.ds.wrappers;

/**
 * 
 * @author ben
 */
public class MutableInteger implements java.lang.Comparable {
    public int i = 0;

    public MutableInteger(final int i) {
        this.i = i;
    }

    public MutableInteger() {
    }

    @Override
	public final int hashCode() {
        return this.i;
    }

    @Override
	public final boolean equals(final Object o) {
        return ((MutableInteger) o).i == this.i;
    }

    public final int compareTo(final Object o) {
        final int j = ((MutableInteger) o).i;
        return this.i == j ? 0 : this.i > j ? 1 : -1;
    }

    @Override
	public String toString() {
        return " " + this.i + " ";
    }
}