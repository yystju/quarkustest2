package shi.quan.rcpsp.vo;

public class Task<TimeType, PayloadType> {
    private String id;
    private String name;
    private TimeType minimumEstimationTime;
    private TimeType maximumEstimationTime;
    private TimeType mostLikedEstimationTime;

    private PayloadType payload;

    private TimeEstimationStrategy<TimeType> strategy;

    public Task() {
    }

    public Task(String id, String name, PayloadType payload) {
        this.id = id;
        this.name = name;
        this.payload = payload;
    }

    public Task(String id, String name, TimeType minimumEstimationTime, TimeType maximumEstimationTime, TimeType mostLikedEstimationTime, PayloadType payload, TimeEstimationStrategy<TimeType> strategy) {
        this.id = id;
        this.name = name;
        this.minimumEstimationTime = minimumEstimationTime;
        this.maximumEstimationTime = maximumEstimationTime;
        this.mostLikedEstimationTime = mostLikedEstimationTime;
        this.payload = payload;
        this.strategy = strategy;
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

    @Override
    public String toString() {
        return String.format("%s(%s)", this.name, this.id);
    }
}