package shi.quan.solver.pm.constraint;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.solver.pm.vo.Task;
import shi.quan.solver.pm.vo.Workplace;

import javax.resource.spi.work.Work;

public class PMConstraintProvider implements ConstraintProvider {
    private static final Logger logger = LoggerFactory.getLogger(PMConstraintProvider.class);

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                workplaceConflict(constraintFactory),
                workplacePrecisionConflict(constraintFactory),
                timeslotCapacityConflict(constraintFactory),
        };
    }

    private Constraint workplaceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(Task.class,
                        Joiners.equal(Task::getWorkplace),
                        Joiners.equal(Task::getTimeslot))
                .penalize("Workplace conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint workplacePrecisionConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Task.class)
                .join(Workplace.class)
                .filter(((task, workplace) -> task.getWorkplace() == workplace && task.getPrecision() > workplace.getMaxPrecision()))
                .penalize("Workplace Precision conflict", HardSoftScore.ONE_HARD.multiply(100.0));
    }

    private Constraint timeslotCapacityConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Task.class)
                .groupBy(Task::getTimeslot, ConstraintCollectors.toList())
                .filter(((timeslot, tasks) -> {
                    long sum = tasks.stream().mapToLong(t->t.getDuration()).sum();
                    return timeslot.getDuration() < sum;
                }))
                .penalize("Timeslot Capacity conflict", HardSoftScore.ONE_HARD.multiply(10.0));
    }
}
