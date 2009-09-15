package bp.common.ds.wrappers;

public class IntegerNode
                        extends MutableInteger {
    public IntegerNode pointer;

    private static IntegerNode stack = null;

    public IntegerNode() {
    }

    public IntegerNode(final int i) {
        this.i = i;
    }

    public IntegerNode(final int i, final IntegerNode pointer) {
        this.i = i;
        this.pointer = pointer;
    }

    public IntegerNode end() {
        IntegerNode one = this, two = this.pointer;
        while (two != null) {
            one = two;
            two = two.pointer;
        }
        return one;
    }

    public final static IntegerNode getNode() {
        if (IntegerNode.stack != null) {
            final IntegerNode iN = IntegerNode.stack;
            IntegerNode.stack = IntegerNode.stack.pointer;
            return iN;
        }
        return new IntegerNode();
    }

    public final static IntegerNode getNode(final int i, final IntegerNode pointer) {
        final IntegerNode iN = IntegerNode.getNode();
        iN.set(i, pointer);
        return iN;
    }

    public final static void releaseNodes(final IntegerNode head,
            final IntegerNode tail) {
        tail.pointer = IntegerNode.stack;
        IntegerNode.stack = head;
    }

    public final void set(final int i, final IntegerNode pointer) {
        this.i = i;
        this.pointer = pointer;
    }

    public int size() {
        IntegerNode iN = this.pointer;
        int i = 1;
        while (iN != null) {
            i++;
            iN = iN.pointer;
        }
        return i;
    }

    @Override
	public String toString() {
        return String.valueOf(this.i) + " "
                + ((this.pointer != null) ? this.pointer.toString() : "");
    }
}