package shi.quan.rcpsp.vo;

import shi.quan.rcpsp.util.RangeUtil;

import java.util.Comparator;

public class ResourceInstance<TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>> implements Comparable<ResourceInstance<TimeType, AmountType>> {
    private String id;
    private String name;
    private Resource<TimeType, AmountType> resource;

    RangeUtil.ResourceAmountProvider<TimeType, AmountType> provider;

    public ResourceInstance() {
    }

    public ResourceInstance(Resource<TimeType, AmountType> resource, String id, String name, RangeUtil.ResourceAmountProvider<TimeType, AmountType> provider) {
        this.resource = resource;
        this.id = id;
        this.name = name;
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(ResourceInstance<TimeType, AmountType> o) {
        return Comparator
                .comparing(ResourceInstance<TimeType, AmountType>::getId)
                .thenComparing(ResourceInstance<TimeType, AmountType>::getName)
                .compare(this, o);
    }

    public AmountType getResourceByTimeRange(TimeType start, TimeType end) {
        return this.getProvider().getResourceByTimeRange(start, end);
    }

    public TimeType getResourceExtraTime(TimeType start, TimeType end) {
        return this.getProvider().getResourceExtraTime(start, end);
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
        return this.provider != null ? provider : this.resource.getProvider();
    }

    public void setProvider(RangeUtil.ResourceAmountProvider<TimeType, AmountType> provider) {
        this.provider = provider;
    }
}
