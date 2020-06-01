package shi.quan.common;

import java.util.Comparator;

public class Duo<K extends Comparable<K>, V extends Comparable<V>> implements Comparable<Duo<K, V>> {
    private K k;
    private V v;

    public static <K extends Comparable<K>, V extends Comparable<V>> Duo<K, V> pair(K k, V v) {
        return new Duo<K, V>(k, v);
    }

    public Duo() {}

    public Duo(K k, V v) {
        this.k = k;
        this.v = v;
    }

    @Override
    public int compareTo(Duo<K, V> o) {
        return Comparator
                .comparing(Duo<K, V>::getK)
                .thenComparing(Duo<K, V>::getV)
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
