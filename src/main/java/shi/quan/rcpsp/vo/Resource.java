package shi.quan.rcpsp.vo;

import java.util.Comparator;

public class Resource<AmountType extends Comparable<AmountType>> implements Comparable<Resource<AmountType>> {
    private String id;
    private String name;

    private AmountType amount;

    public Resource() {
    }

    public Resource(String id, String name, AmountType amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }

    @Override
    public int compareTo(Resource<AmountType> o) {
        return Comparator
                .comparing(Resource<AmountType>::getId)
                .thenComparing(Resource<AmountType>::getAmount)
                .compare(this, o);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AmountType getAmount() {
        return amount;
    }

    public void setAmount(AmountType amount) {
        this.amount = amount;
    }
}
