package shi.quan.solver.pm2.constraint;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.vo.Duo;
import shi.quan.solver.pm2.util.DateTimeUtil;
import shi.quan.solver.pm2.vo.Task;
import shi.quan.solver.pm2.vo.Timeslot;
import shi.quan.solver.pm2.vo.Workplace;

public class PM2ConstraintProvider implements ConstraintProvider {
    private static final Logger logger = LoggerFactory.getLogger(PM2ConstraintProvider.class);

    private boolean isVerbose = true;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                timeslotBasicConflict(constraintFactory),
                workplaceConflict(constraintFactory),
                workplacePrecisionConflict(constraintFactory),
                timeslotCapacityConflict(constraintFactory),
                taskDurationBasicConflict(constraintFactory),

                taskDurationEfficiency(constraintFactory),
                taskStartTimeEfficiency(constraintFactory),
        };
    }

    private Constraint timeslotBasicConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Task.class)
                .filter(((task) -> {
                    boolean rc = DateTimeUtil.isAfter(task.getTimeslotStart(), task.getTimeslotEnd());

                    if(isVerbose && rc) {
                        logger.info("[timeslotBasicConflict] rc : {}", rc);
                        logger.info("\ttask.getTimeslotStart() : {}", task.getTimeslotStart());
                        logger.info("\ttask.getTimeslotEnd() : {}", task.getTimeslotEnd());
                    }

                    return rc;
                }))
                .penalize("Timeslot Basic Conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint workplaceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(Task.class)
                .filter(((task1, task2) -> {
                    boolean rc = task1.getWorkplace().getId() == task2.getWorkplace().getId() && DateTimeUtil.isTimeOverlap(task1, task2);

                    if(rc) {
                        logger.info("[workplaceConflict] rc : {}", rc);
                        logger.info("\ttask1 : {} ~ {} on {}", task1.getTimeslotStart(), task1.getTimeslotEnd(), task1.getWorkplace());
                        logger.info("\ttask2 : {} ~ {} on {}", task2.getTimeslotStart(), task2.getTimeslotEnd(), task2.getWorkplace());
                    }
                    return rc;
                }))
                .penalize("Workplace Conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint taskDurationBasicConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Task.class)
                .filter(((task) -> {
                    long taskDuration = task.getDuration();
                    long timeSlotDuration = DateTimeUtil.duration(task.getTimeslotStart(), task.getTimeslotEnd());

                    if(isVerbose && taskDuration > timeSlotDuration) {
                        logger.info("[taskDurationBasicConflict]taskDuration : {}, timeSlotDuration : {}", taskDuration, timeSlotDuration);
                        logger.info("\ttask.getTimeslotStart() : {}", task.getTimeslotStart());
                        logger.info("\ttask.getTimeslotEnd() : {}", task.getTimeslotEnd());
                    }

                    return taskDuration > timeSlotDuration;
                }))
                .penalize("Task Duration Basic Conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint workplacePrecisionConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Task.class)
                .join(Workplace.class)
                .filter(((task, workplace) -> task.getWorkplace().getId() == workplace.getId() && task.getPrecision() > workplace.getMaxPrecision()))
                .penalize("Workplace Precision conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint timeslotCapacityConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Timeslot.class)
                .join(Task.class)
                .join(Workplace.class)
                .filter(((timeslot, task, workplace) -> DateTimeUtil.isTimeOverlap(timeslot, task) && task.getWorkplace().getId() == workplace.getId()))
                .groupBy((timeslot, task, workplace) -> Duo.duo(timeslot, workplace), ConstraintCollectors.toList((timeslot, task, workplace) -> task))
                .filter(((keyPair, tasks) -> {
                    long slotDuration = keyPair.getK().getDuration();

                    long totalTaskDuration = tasks.stream().mapToLong(t-> DateTimeUtil.intersectionMillis(keyPair.getK(), t, false)).sum();

                    if(isVerbose && slotDuration < totalTaskDuration) {
                        //To show the verbose...
                        tasks.stream().mapToLong(t-> DateTimeUtil.intersectionMillis(keyPair.getK(), t, true)).sum();

                        logger.info("[timeslotCapacityConflict] slotDuration : {}, totalTaskDuration : {}", slotDuration, totalTaskDuration);
                        logger.info("\ttimeslot : {}", keyPair.getK());
                        logger.info("\tworkplace : {}", keyPair.getV());
                        logger.info("\ttasks : {}", tasks);
                    }

                    return slotDuration < totalTaskDuration;
                }))
                .penalize("Timeslot Capacity conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint taskDurationEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Task.class)
                .penalize("Task Duration Efficiency"
                        , HardSoftScore.ONE_SOFT
                        , (task) -> {
                            int softScore = (int)(DateTimeUtil.duration(task.getTimeslotStart(), task.getTimeslotEnd()) / 1000L);

                            if(softScore < 0) softScore = Integer.MAX_VALUE;
//                            if(isVerbose) logger.info("[taskDurationEfficiency] task : {}, softScore : {}", task, softScore);
                            return softScore;
                        });
    }

    private Constraint taskStartTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Task.class)
                .penalize("Task Start Time Efficiency"
                        , HardSoftScore.ONE_SOFT
                        , (task) -> {
                            int softScore = (int)(DateTimeUtil.millis(task.getTimeslotStart()) % (24L * 60L * 60L * 1000L));
//                            if(isVerbose) logger.info("[taskStartTimeEfficiency] task : {}, softScore : {}", task, softScore);
                            return softScore;
                        });
    }
}
