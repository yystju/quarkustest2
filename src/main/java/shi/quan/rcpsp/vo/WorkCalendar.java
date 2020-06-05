package shi.quan.rcpsp.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.vo.Duo;
import shi.quan.common.vo.Quartet;
import shi.quan.rcpsp.util.RangeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorkCalendar<TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>> {
    private static final Logger logger = LoggerFactory.getLogger(WorkCalendar.class);

    public static final int TYPE_WORK = 0;
    public static final int TYPE_BREAK = 1;

    private List<Quartet<TimeType, TimeType, AmountType, Integer>> calendarList = new ArrayList<>();

    public WorkCalendar() {
    }

    public AmountType getMinimalAmountByTimeRange(TimeType start, TimeType end) {
        Duo<TimeType, TimeType> range = Duo.duo(start, end);
        List<Quartet<TimeType, TimeType, AmountType, Integer>> filtered = this.calendarList.stream()
                .filter(q -> q.getFour() != TYPE_BREAK && !RangeUtil.isEmptyRange(RangeUtil.intersect(range, Duo.duo(q.getOne(), q.getTwo()))))
                .collect(Collectors.toList());

        return filtered.stream().map(q -> q.getThree()).min(Comparable::compareTo).orElse(null);
    }

    public List<Duo<TimeType, TimeType>> getTypedTimeRangesByTimeRange(TimeType start, TimeType end, int type) {
        Duo<TimeType, TimeType> range = Duo.duo(start, end);
        return this.calendarList.stream()
                .filter(q -> q.getFour() == type && !RangeUtil.isEmptyRange(RangeUtil.intersect(range, Duo.duo(q.getOne(), q.getTwo()))))
                .map(q-> Duo.duo(q.getOne(), q.getTwo()))
                .collect(Collectors.toList());
    }

    public WorkCalendar(List<Quartet<TimeType, TimeType, AmountType, Integer>> calendarList) {
        this.calendarList = calendarList;
    }

    public List<Quartet<TimeType, TimeType, AmountType, Integer>> getCalendarList() {
        return calendarList;
    }

    public void setCalendarList(List<Quartet<TimeType, TimeType, AmountType, Integer>> calendarList) {
        this.calendarList = calendarList;
    }
}
