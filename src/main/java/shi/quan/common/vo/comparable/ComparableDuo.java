package shi.quan.common.vo.comparable;

import java.util.Comparator;

public class ComparableDuo<K extends Comparable<K>, V extends Comparable<V>> implements Comparable<ComparableDuo<K, V>> {
    private K k;
    private V v;

    public static <K extends Comparable<K>, V extends Comparable<V>> ComparableDuo<K, V> duo(K k, V v) {
        return new ComparableDuo<K, V>(k, v);
    }

    public ComparableDuo() {}

    public ComparableDuo(K k, V v) {
        this.k = k;
        this.v = v;
    }

    @Override
    public int compareTo(ComparableDuo<K, V> o) {
        return Comparator
                .comparing(ComparableDuo<K, V>::getK)
                .thenComparing(ComparableDuo<K, V>::getV)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "(" + k + ", " + v + ')';
    }

    public K getK() {
        return k;
    }

    public void setK(K k) {
        this.k = k;
    }

    public V getV() {
        return v;
    }

    public void setV(V v) {
        this.v = v;
    }
}
