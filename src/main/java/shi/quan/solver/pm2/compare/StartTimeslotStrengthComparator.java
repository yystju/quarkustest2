package shi.quan.solver.pm2.compare;

import shi.quan.solver.pm2.vo.Timeslot;

import java.io.Serializable;
import java.util.Comparator;

public class StartTimeslotStrengthComparator implements Comparator<Timeslot>, Serializable {
    @Override
    public int compare(Timeslot timeslot1, Timeslot timeslot2) {
        return  Comparator.comparing(Timeslot::getStartTime).thenComparing(Timeslot::getDuration).thenComparing(Timeslot::getId).compare(timeslot1, timeslot2);
    }
}
