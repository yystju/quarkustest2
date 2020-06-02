package shi.quan.common.vo.comparable;

import java.util.Comparator;

public class ComparableSolo<V extends Comparable<V>> implements Comparable<ComparableSolo<V>> {
    private V v;

    public static <V extends Comparable<V>> ComparableSolo<V> solo(V v) {
        return new ComparableSolo<V>(v);
    }

    public ComparableSolo() {}

    public ComparableSolo(V v) {
        this.v = v;
    }

    @Override
    public int compareTo(ComparableSolo<V> o) {
        return Comparator
                .comparing(ComparableSolo<V>::getV)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "(" + v + ')';
    }

    public V getV() {
        return v;
    }

    public void setV(V v) {
        this.v = v;
    }
}
