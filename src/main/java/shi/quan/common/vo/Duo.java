package shi.quan.common.vo;

public class Duo<K, V> {
    private K k;
    private V v;

    public static <K, V> Duo<K, V> duo(K k, V v) {
        return new Duo<K, V>(k, v);
    }

    public Duo() {}

    public Duo(K k, V v) {
        this.k = k;
        this.v = v;
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
