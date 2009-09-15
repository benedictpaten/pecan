package bp.common.ds.wrappers;

public class ObjectNode {
    public Object o;

    public ObjectNode pointer;

    private static ObjectNode stack = null;

    public ObjectNode() {
    }

    public ObjectNode(final Object o, final ObjectNode pointer) {
        this.o = o;
        this.pointer = pointer;
    }

    public ObjectNode end() {
        ObjectNode one = this, two = this.pointer;
        while (two != null) {
            one = two;
            two = two.pointer;
        }
        return one;
    }

    public final static ObjectNode getNode() {
        if (ObjectNode.stack != null) {
            final ObjectNode oN = ObjectNode.stack;
            ObjectNode.stack = ObjectNode.stack.pointer;
            return oN;
        } else {
			return new ObjectNode();
		}
    }

    public final static ObjectNode getNode(final Object o,
            final ObjectNode pointer) {
        final ObjectNode oN = ObjectNode.getNode();
        oN.set(o, pointer);
        return oN;
    }

    public final static void releaseNodes(final ObjectNode head,
            final ObjectNode tail) {
        tail.pointer = ObjectNode.stack;
        ObjectNode.stack = head;
    }

    public final void set(final Object o, final ObjectNode pointer) {
        this.o = o;
        this.pointer = pointer;
    }

    @Override
	public String toString() {
        return " ( " + ((this.pointer != null) ? this.pointer.toString() : "") + " : "
                + (this.o != null ? this.o.toString() : "null") + " ) ";
    }
}