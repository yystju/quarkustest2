package shi.quan.common;

import java.util.Comparator;

public class Solo<V extends Comparable<V>> implements Comparable<Solo<V>> {
    private V v;

    public static <V extends Comparable<V>> Solo<V> solo(V v) {
        return new Solo<V>(v);
    }

    public Solo() {}

    public Solo(V v) {
        this.v = v;
    }

    @Override
    public int compareTo(Solo<V> o) {
        return Comparator
                .comparing(Solo<V>::getV)
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
