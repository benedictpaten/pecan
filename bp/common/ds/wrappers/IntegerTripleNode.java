package bp.common.ds.wrappers;

public class IntegerTripleNode {
    public IntegerTripleNode pointer;

    public int i, j, k;

    public IntegerTripleNode(final int i, final int j, final int k,
            final IntegerTripleNode pointer) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.pointer = pointer;
    }

    @Override
	public String toString() {
        return this.i + " " + this.j + " " + this.k + " "
                + (this.pointer != null ? this.pointer.toString() : "");
    }

}