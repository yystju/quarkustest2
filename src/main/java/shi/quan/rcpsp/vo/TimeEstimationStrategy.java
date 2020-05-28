package shi.quan.rcpsp.vo;

public interface TimeEstimationStrategy<TimeType> {
    TimeType calcMostLikedEstimationTime(TimeType minimumEstimationTime, TimeType maximumEstimationTime);
}
