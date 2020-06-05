package shi.quan.common.vo;

public class Solo<V> {
    private V v;

    public static <V> Solo<V> solo(V v) {
        return new Solo<V>(v);
    }

    public Solo() {}

    public Solo(V v) {
        this.v = v;
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
