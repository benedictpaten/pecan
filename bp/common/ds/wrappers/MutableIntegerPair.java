package bp.common.ds.wrappers;

public class MutableIntegerPair
                               extends MutableInteger {
    public int j = 0;

    public MutableIntegerPair(final int i, final int j) {
        super(i);
        this.j = j;
    }
}