package shi.quan.rcpsp.vo;

public class Resource<AmountType> {
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
