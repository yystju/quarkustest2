package shi.quan.rcpsp.vo;

import shi.quan.rcpsp.util.RangeUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Resource<TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>> implements Comparable<Resource<TimeType, AmountType>> {
    private String id;
    private String name;

    private List<ResourceInstance<TimeType, AmountType>> instanceList = new ArrayList<>();

    RangeUtil.ResourceAmountProvider<TimeType, AmountType> provider;

    public Resource() {
    }

    public Resource(String id, String name, RangeUtil.ResourceAmountProvider<TimeType, AmountType> provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", instances=" + this.instanceList +
                '}';
    }

    @Override
    public int compareTo(Resource<TimeType, AmountType> o) {
        return Comparator
                .comparing(Resource<TimeType, AmountType>::getId)
                .thenComparing(Resource<TimeType, AmountType>::getName)
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

    public RangeUtil.ResourceAmountProvider<TimeType, AmountType> getProvider() {
        return provider;
    }

    public void setProvider(RangeUtil.ResourceAmountProvider<TimeType, AmountType> provider) {
        this.provider = provider;
    }

    public List<ResourceInstance<TimeType, AmountType>> getInstanceList() {
        return instanceList;
    }

    public void setInstanceList(List<ResourceInstance<TimeType, AmountType>> instanceList) {
        this.instanceList = instanceList;
    }
}
