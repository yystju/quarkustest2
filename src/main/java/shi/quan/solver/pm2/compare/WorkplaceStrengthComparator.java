package shi.quan.solver.pm2.compare;

import shi.quan.solver.pm2.vo.Workplace;

import java.io.Serializable;
import java.util.Comparator;

public class WorkplaceStrengthComparator implements Comparator<Workplace>, Serializable {
    @Override
    public int compare(Workplace workplace1, Workplace workplace2) {
        return  Comparator.comparing(Workplace::getMaxPrecision).thenComparing(Workplace::getId).compare(workplace1, workplace2);
    }
}
