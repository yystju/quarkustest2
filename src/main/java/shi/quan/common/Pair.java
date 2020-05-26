package shi.quan.common;

import java.util.Comparator;

public class Pair<K extends Comparable<K>, V extends Comparable<V>> implements Comparable<Pair<K, V>> {
    private K k;
    private V v;

    public static <K extends Comparable<K>, V extends Comparable<V>> Pair<K, V> pair(K k, V v) {
        return new Pair<K, V>(k, v);
    }

    public Pair() {}

    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
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

    @Override
    public int compareTo(Pair<K, V> o) {
        return Comparator
                .comparing(Pair<K, V>::getK)
                .thenComparing(Pair<K, V>::getV)
                .compare(this, o);
    }
}
