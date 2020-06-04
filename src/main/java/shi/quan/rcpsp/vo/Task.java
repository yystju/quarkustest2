package shi.quan.rcpsp.vo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Task<TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>> implements Comparable<Task<TimeType, PayloadType, AmountType>> {
    private String id;
    private String name;

    private TimeType minimumEstimationTime;
    private TimeType maximumEstimationTime;
    private TimeType mostLikedEstimationTime;

    private PayloadType payload;

    private Map<String, AmountType> resourceMap = new HashMap<>();

    private TimeEstimationStrategy<TimeType> strategy;


    private TimeType plannedStartTime;
    private TimeType plannedEndTime;

    private Map<String, ResourceInstance<TimeType, AmountType>> chosenResources;

    public Task() {
    }

    public Task(String id, String name, PayloadType payload) {
        this.id = id;
        this.name = name;
        this.payload = payload;
    }

    public Task(String id, String name, TimeType minimumEstimationTime, TimeType maximumEstimationTime, TimeType mostLikedEstimationTime, PayloadType payload, TimeEstimationStrategy<TimeType> strategy) {
        this(id, name, minimumEstimationTime, maximumEstimationTime, mostLikedEstimationTime, payload, strategy, new HashMap<>());
    }


    public Task(String id, String name, TimeType minimumEstimationTime, TimeType maximumEstimationTime, TimeType mostLikedEstimationTime, PayloadType payload, TimeEstimationStrategy<TimeType> strategy, Map<String, AmountType> resourceMap) {
        this.id = id;
        this.name = name;
        this.minimumEstimationTime = minimumEstimationTime;
        this.maximumEstimationTime = maximumEstimationTime;
        this.mostLikedEstimationTime = mostLikedEstimationTime;
        this.payload = payload;
        this.strategy = strategy;

        this.resourceMap = resourceMap;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", String.valueOf(this.id), String.valueOf(this.payload));
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public int compareTo(Task<TimeType, PayloadType, AmountType> o) {
        return Comparator
                .comparing(Task<TimeType, PayloadType, AmountType>::getId)
                .thenComparing(Task<TimeType, PayloadType, AmountType>::getName)
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

    public TimeType getMinimumEstimationTime() {
        return minimumEstimationTime;
    }

    public void setMinimumEstimationTime(TimeType minimumEstimationTime) {
        this.minimumEstimationTime = minimumEstimationTime;
    }

    public TimeType getMaximumEstimationTime() {
        return maximumEstimationTime;
    }

    public void setMaximumEstimationTime(TimeType maximumEstimationTime) {
        this.maximumEstimationTime = maximumEstimationTime;
    }

    public TimeType getMostLikedEstimationTime() {
        return mostLikedEstimationTime;
    }

    public void setMostLikedEstimationTime(TimeType mostLikedEstimationTime) {
        this.mostLikedEstimationTime = mostLikedEstimationTime;
    }

    public PayloadType getPayload() {
        return payload;
    }

    public void setPayload(PayloadType payload) {
        this.payload = payload;
    }

    public TimeEstimationStrategy<TimeType> getStrategy() {
        return strategy;
    }

    public void setStrategy(TimeEstimationStrategy<TimeType> strategy) {
        this.strategy = strategy;
    }

    public Map<String, AmountType> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, AmountType> resourceMap) {
        this.resourceMap = resourceMap;
    }

    public TimeType getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(TimeType plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public TimeType getPlannedEndTime() {
        return plannedEndTime;
    }

    public void setPlannedEndTime(TimeType plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    public Map<String, ResourceInstance<TimeType, AmountType>> getChosenResources() {
        return chosenResources;
    }

    public void setChosenResources(Map<String, ResourceInstance<TimeType, AmountType>> chosenResources) {
        this.chosenResources = chosenResources;
    }
}
