package bp.common.ds.wrappers;


public class IntegerPairNode
                            extends IntegerNode {
    public IntegerPairNode pointer;

    public int i, j;

    public IntegerPairNode(final int i, final int j) {
        this.i = i;
        this.j = j;
    }

    public IntegerPairNode(final int i, final int j, final IntegerPairNode pointer) {
        this.i = i;
        this.j = j;
        this.pointer = pointer;
    }

    @Override
	public String toString() {
        return this.i + " " + this.j + " "
                + (this.pointer != null ? this.pointer.toString() : "");
    }

    @Override
	public int size() {
        IntegerPairNode iN = this.pointer;
        int i = 1;
        while (iN != null) {
            i++;
            iN = iN.pointer;
        }
        return i;
    }
}